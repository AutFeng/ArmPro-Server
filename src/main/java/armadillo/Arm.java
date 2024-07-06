package armadillo;


import armadillo.enums.LanguageEnums;
import armadillo.model.SysUser;
import armadillo.result.TaskInfo;
import armadillo.result.ignore;
import armadillo.transformers.base.BaseTransformer;
import armadillo.transformers.base.DexTransformer;
import armadillo.transformers.base.OtherTransformer;
import armadillo.utils.RedisUtil;
import armadillo.utils.ZipUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.google.common.collect.Lists;
import org.apache.log4j.Logger;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.immutable.reference.ImmutableMethodProtoReference;
import org.jf.dexlib2.rewriter.DexRewriter;
import org.jf.dexlib2.writer.io.MemoryDataStore;
import org.jf.dexlib2.writer.pool.DexPool;

import javax.annotation.Nonnull;
import java.io.*;
import java.util.*;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class Arm {
    private final Logger logger = Logger.getLogger(Arm.class);
    private final ZipFile zipFile;
    private final List<BaseTransformer> baseTransformers = new ArrayList<>();
    private final String uuid;
    private final LanguageEnums languageEnums;
    private final long start = System.currentTimeMillis();
    private final List<TaskInfo> task;
    private final List<byte[]> data;
    public final List<ImmutableMethodProtoReference> methodProtoReferences = new ArrayList<>();
    public final HashMap<String, byte[]> ReplacerRes = new HashMap<>();
    public final HashSet<byte[]> Add_Classes = new HashSet<>();
    private final List<String> ignores = new ArrayList<>();
    private String config;
    private int dexIndex = 1;
    public Opcodes opcodes;
    private SysUser sysUser;

    public Arm(ZipFile zipFile, List<TaskInfo> task, List<byte[]> data, String uuid, LanguageEnums languageEnums) {
        this.zipFile = zipFile;
        this.task = task;
        this.data = data;
        this.uuid = uuid;
        this.languageEnums = languageEnums;
    }

    public void setSysUser(SysUser sysUser) {
        this.sysUser = sysUser;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public void addTransformer(BaseTransformer baseTransformer) {
        baseTransformers.add(baseTransformer);
    }

    public void Run() throws Exception {
        ZipOutputStream zipOutputStream = null;
        try {
            zipOutputStream = new ZipOutputStream(new FileOutputStream(new File(Constant.getTask(), uuid)));
            Collections.sort(baseTransformers);
            if (existsDexTransformer(baseTransformers)) {
                for (int i = 0; i < data.size(); i++) {
                    byte[] bytes = data.get(i);
                    DexBackedDexFile dexBackedDexFile = DexBackedDexFile.fromInputStream(null, new BufferedInputStream(new ByteArrayInputStream(bytes)));
                    HashSet<ClassDef> classes = new HashSet<>(dexBackedDexFile.getClasses());
                    for (BaseTransformer transformer : baseTransformers) {
                        if (transformer instanceof DexTransformer) {
                            transformer.setLanguageEnums(languageEnums);
                            DexTransformer dexTransformer = (DexTransformer) transformer;
                            dexTransformer.init(config, ReplacerRes, Add_Classes, bytes, dexBackedDexFile, zipFile);
                            transformer.transform();
                            List<ClassDef> defs = Lists.newArrayList(new DexRewriter(transformer).getDexFileRewriter().rewrite(new DexFile() {
                                @Nonnull
                                @Override
                                public Set<? extends ClassDef> getClasses() {
                                    return classes;
                                }

                                @Nonnull
                                @Override
                                public Opcodes getOpcodes() {
                                    return opcodes == null ? Opcodes.forApi(dexBackedDexFile.getOpcodes().api) : opcodes;
                                }
                            }).getClasses());
                            classes.clear();
                            classes.addAll(defs);
                            if (transformer.getNewClassDef() != null)
                                classes.addAll(transformer.getNewClassDef());
                            if (transformer.getResult() != null) {
                                task.add(new TaskInfo(i, String.format("classes%d -> %s", i + 1, transformer.getResult())));
                                RedisUtil redisUtil = RedisUtil.getRedisUtil();
                                if (redisUtil.exists(uuid))
                                    redisUtil.setex(uuid, 60 * 60 * 24, JSONArray.toJSONString(task));
                            }
                            if (dexTransformer.getOpcodes() != null)
                                opcodes = dexTransformer.getOpcodes();
                            if (dexTransformer.getMethodProtoReferences() != null)
                                methodProtoReferences.addAll(dexTransformer.getMethodProtoReferences());
                            if (transformer.getIgnores() != null)
                                ignores.addAll(transformer.getIgnores());
                        }
                    }
                    DexPool dexPool = new DexPool(opcodes == null ? Opcodes.forApi(dexBackedDexFile.getOpcodes().api) : opcodes);
                    if (methodProtoReferences.size() > 0) {
                        for (ImmutableMethodProtoReference methodProtoReference : methodProtoReferences)
                            dexPool.protoSection.intern(methodProtoReference);
                    }
                    for (ClassDef classDef : classes)
                        dexPool.internClass(classDef);
                    try {
                        MemoryDataStore dataStore = new MemoryDataStore();
                        dexPool.writeTo(dataStore);
                        ZipUtils.addZipEntry(zipOutputStream, new ZipEntry(dexIndex == 1 ? "classes.dex" : "classes" + dexIndex + ".dex"), Arrays.copyOf(dataStore.getBuffer(), dataStore.getSize()));
                        dexIndex++;
                        dataStore.close();
                    } catch (Exception e) {
                        multidex(zipOutputStream, dexBackedDexFile, classes);
                    }
                }
            }
            if (existsOtherTransformer(baseTransformers)) {
                for (BaseTransformer transformer : baseTransformers) {
                    if (transformer instanceof OtherTransformer) {
                        transformer.setLanguageEnums(languageEnums);
                        OtherTransformer otherTransformer = (OtherTransformer) transformer;
                        otherTransformer.init(config, ReplacerRes, Add_Classes, zipFile, sysUser, uuid);
                        otherTransformer.transform();
                        if (otherTransformer.getResult() != null) {
                            task.add(new TaskInfo(100, String.format("%s", otherTransformer.getResult())));
                            RedisUtil redisUtil = RedisUtil.getRedisUtil();
                            if (redisUtil.exists(uuid))
                                redisUtil.setex(uuid, 60 * 60 * 24, JSONArray.toJSONString(task));
                        }
                        if (transformer.getIgnores() != null)
                            ignores.addAll(transformer.getIgnores());
                    }
                }
            }
            ReplacerRes.put("ignore.json", JSON.toJSONString(new ignore(ignores)).getBytes());
            for (Map.Entry<String, byte[]> entry : ReplacerRes.entrySet()) {
                if (entry.getKey().equals("resources.arsc")) {
                    ZipEntry zipEntry = new ZipEntry(entry.getKey());
                    zipEntry.setMethod(ZipEntry.STORED);
                    zipEntry.setSize(entry.getValue().length);
                    zipEntry.setCompressedSize(entry.getValue().length);
                    CRC32 crc32 = new CRC32();
                    crc32.update(entry.getValue());
                    zipEntry.setCrc(crc32.getValue());
                    ZipUtils.addZipEntry(zipOutputStream, zipEntry, entry.getValue());
                } else
                    ZipUtils.addZipEntry(zipOutputStream, new ZipEntry(entry.getKey()), entry.getValue());
            }
            if (!existsDexTransformer(baseTransformers))
                dexIndex = data.size() + 1;
            for (byte[] bytes : Add_Classes) {
                ZipUtils.addZipEntry(zipOutputStream, new ZipEntry("classes" + dexIndex + ".dex"), bytes);
                dexIndex++;
            }
            zipOutputStream.close();
            logger.info("******************************************************************");
            float time = (float) (System.currentTimeMillis() - start) / 1000;
            if (time > 60)
                logger.info(String.format("任务ID:%s 耗时->%.2f分钟", uuid, time / 60));
            else
                logger.info(String.format("任务ID:%s 耗时->%.2f秒", uuid, time));
            logger.info("******************************************************************");
        }catch (ThreadDeath threadDeath){
            if (zipOutputStream != null)
                zipOutputStream.close();
            throw new ThreadDeath();
        }
    }

    private boolean existsOtherTransformer(List<BaseTransformer> baseTransformers) {
        for (BaseTransformer transformer : baseTransformers) {
            if (transformer instanceof OtherTransformer)
                return true;
        }
        return false;
    }

    private boolean existsDexTransformer(List<BaseTransformer> baseTransformers) {
        for (BaseTransformer transformer : baseTransformers) {
            if (transformer instanceof DexTransformer)
                return true;
        }
        return false;
    }

    private void multidex(ZipOutputStream zipOutputStream, DexBackedDexFile dexBackedDexFile, HashSet<ClassDef> classDefs) throws IOException {
        ClassDef[] defs = new ClassDef[classDefs.size()];
        Lists.newArrayList(classDefs).toArray(defs);
        if (defs.length / 2 < 62000) {
            int arr1_len = defs.length / 2;
            ClassDef[] arr1 = new ClassDef[arr1_len];
            ClassDef[] arr2 = new ClassDef[defs.length - arr1_len];
            System.arraycopy(defs, 0, arr1, 0, arr1.length);
            System.arraycopy(defs, arr1_len, arr2, 0, arr2.length);
            WriteDex(Arrays.asList(arr1), zipOutputStream, dexBackedDexFile);
            WriteDex(Arrays.asList(arr2), zipOutputStream, dexBackedDexFile);
        } else {
            int arr1_len = defs.length / 4;
            ClassDef[] arr1 = new ClassDef[arr1_len];
            ClassDef[] arr2 = new ClassDef[arr1_len * 2];
            ClassDef[] arr3 = new ClassDef[arr1_len * 3];
            ClassDef[] arr4 = new ClassDef[defs.length - arr3.length];
            System.arraycopy(defs, 0, arr1, 0, arr1.length);
            System.arraycopy(defs, arr1.length, arr2, 0, arr2.length);
            System.arraycopy(defs, arr2.length, arr3, 0, arr3.length);
            System.arraycopy(defs, arr3.length, arr4, 0, arr4.length);
            WriteDex(Arrays.asList(arr1), zipOutputStream, dexBackedDexFile);
            WriteDex(Arrays.asList(arr2), zipOutputStream, dexBackedDexFile);
            WriteDex(Arrays.asList(arr3), zipOutputStream, dexBackedDexFile);
            WriteDex(Arrays.asList(arr4), zipOutputStream, dexBackedDexFile);
        }
    }

    private void WriteDex(List<ClassDef> classDefs, ZipOutputStream zipOutputStream, DexBackedDexFile dexBackedDexFile) throws IOException {
        DexPool dexPool = new DexPool(opcodes == null ? Opcodes.forApi(dexBackedDexFile.getOpcodes().api) : opcodes);
        if (methodProtoReferences.size() > 0)
            for (ImmutableMethodProtoReference methodProtoReference : methodProtoReferences)
                dexPool.protoSection.intern(methodProtoReference);
        for (ClassDef def : classDefs)
            if (def != null)
                dexPool.internClass(def);
        MemoryDataStore dataStore = new MemoryDataStore();
        dexPool.writeTo(dataStore);
        ZipUtils.addZipEntry(zipOutputStream, new ZipEntry(dexIndex == 1 ? "classes.dex" : "classes" + dexIndex + ".dex"), Arrays.copyOf(dataStore.getBuffer(), dataStore.getSize()));
        dexIndex++;
        dataStore.close();
    }
}
