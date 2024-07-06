package armadillo.transformers.hook;

import armadillo.Constant;
import armadillo.result.SignerInfo;
import armadillo.result.ignore;
import armadillo.transformers.base.BaseTransformer;
import armadillo.transformers.base.OtherTransformer;
import armadillo.utils.*;
import armadillo.utils.axml.AutoXml.ManifestAppName;
import armadillo.utils.axml.EditXml.decode.AXMLDoc;
import armadillo.utils.axml.EditXml.decode.BTagNode;
import armadillo.utils.axml.EditXml.decode.BXMLNode;
import armadillo.utils.axml.EditXml.utils.TypedValue;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.gson.*;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.dexbacked.DexBackedClassDef;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.immutable.reference.ImmutableMethodProtoReference;
import org.jf.dexlib2.writer.io.MemoryDataStore;
import org.jf.dexlib2.writer.pool.DexPool;

import java.io.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class New360 extends OtherTransformer {
    private final List<String> ignores = new ArrayList<>();

    @Override
    public void transform() throws Exception {
        byte[] axml = getReplacerRes().get("AndroidManifest.xml");
        if (axml == null)
            axml = StreamUtil.readBytes(getZipFile().getInputStream(new ZipEntry("AndroidManifest.xml")));
        JsonElement type_json = new JsonParser().parse(getConfiguration()).getAsJsonObject().get(Long.toString(0x1000000000L));
        File cacheLibs = null;
        File cacheCpp = null;
        JsonObject jsonObject = new JsonParser()
                .parse(configuration)
                .getAsJsonObject();
        String signer = jsonObject.has("app_signer") ? jsonObject.get("app_signer").getAsString() : ApkSignerUtils.getApkSignatureData(getZipFile());
        ManifestAppName appName = new ManifestAppName();
        ManifestAppName.XmlMode xmlMode = appName.parseManifest(new ByteArrayInputStream(axml), "arm.ArmKill");
        getReplacerRes().put("AndroidManifest.xml", xmlMode.getData());
        switch (type_json.getAsInt()) {
            /**
             * Sandhook
             */
            case 0:
            case 1: {
                DexPool dexPool = new DexPool(Opcodes.getDefault());
                DexBackedDexFile sandhook_dex = DexBackedDexFile.fromInputStream(Opcodes.getDefault(), new BufferedInputStream(LoaderRes.getInstance().getStaticResAsStream("dex/new_360_hook.dex")));
                for (DexBackedClassDef dexClass : sandhook_dex.getClasses())
                    dexPool.internClass(dexClass);
                if (xmlMode.isCustomApplication() && xmlMode.getCustomApplicationName().equals("com.stub.StubApp")) {
                    Enumeration<? extends ZipEntry> entries = getZipFile().entries();
                    while (entries.hasMoreElements()) {
                        ZipEntry zipEntry = entries.nextElement();
                        if (zipEntry.getName().startsWith("classes") && zipEntry.getName().endsWith(".dex")) {
                            DexBackedDexFile dexFile =
                                    DexBackedDexFile.fromInputStream(
                                            Opcodes.getDefault(),
                                            new BufferedInputStream(getZipFile().getInputStream(zipEntry)));
                            for (DexBackedClassDef classDef : dexFile.getClasses())
                                dexPool.internClass(classDef);
                            ignores.add(zipEntry.getName());
                        }
                    }
                    MemoryDataStore dataStore = new MemoryDataStore();
                    dexPool.writeTo(dataStore);
                    getReplacerRes().put("classes.dex", Arrays.copyOf(dataStore.getBuffer(), dataStore.getSize()));
                    dataStore.close();
                } else {
                    MemoryDataStore dataStore = new MemoryDataStore();
                    dexPool.writeTo(dataStore);
                    getAdd_Classdex().add(Arrays.copyOf(dataStore.getBuffer(), dataStore.getSize()));
                    dataStore.close();
                }
                cacheCpp = new File(Constant.getCache(), SHAUtils.SHA1(getUuid()) + "-signer-cache");
                if (!cacheCpp.exists())
                    cacheCpp.mkdirs();
                ZipUtils.zipUncompress(new ZipInputStream(LoaderRes.getInstance().getStaticResAsStream("zip/new_360_hook.zip")), cacheCpp.getAbsolutePath());
                File cpp_file = new File(cacheCpp.getAbsolutePath() +
                        File.separator +
                        "jni" +
                        File.separator +
                        "cpp" +
                        File.separator +
                        "output" +
                        File.separator +
                        "arm_ArmKill.cpp");
                byte[] cpp_bytes = StreamUtil.readBytes(new FileInputStream(cpp_file));
                MessageDigest md5 = MessageDigest.getInstance("MD5");
                md5.update((signer + "ArmEpic").getBytes());
                byte[] byteArray = md5.digest();
                StringBuilder sb = new StringBuilder();
                for (byte b : byteArray)
                    sb.append(String.format("%02x", b));
                String cpp_body = new String(cpp_bytes)
                        .replace("Signer Data", signer + "ArmEpic")
                        .replace("SrcApplication", (xmlMode.isCustomApplication() ? xmlMode.getCustomApplicationName() : "android.app.Application"))
                        .replace("MD5校验值", sb.toString());
                FileOutputStream outputStream = new FileOutputStream(cpp_file);
                outputStream.write(cpp_body.getBytes());
                outputStream.close();
                String so_api = "APP_ABI := armeabi-v7a";
                if (getConfiguration().contains("so_framework")) {
                    StringBuilder builder = new StringBuilder();
                    JsonArray SO_API = new JsonParser().parse(getConfiguration()).getAsJsonObject().getAsJsonArray("so_framework");
                    for (JsonElement jsonElement : SO_API) {
                        if (jsonElement.getAsString().equals("x86") || jsonElement.getAsString().equals("x86_64"))
                            continue;
                        if (jsonElement.getAsString().equals("armeabi")) {
                            if (!builder.toString().contains("armeabi-v7a"))
                                builder.append("armeabi-v7a").append(" ");
                        } else
                            builder.append(jsonElement.getAsString()).append(" ");
                    }
                    so_api = "APP_ABI := " + builder.toString();
                }
                cpp_file = new File(cacheCpp.getAbsolutePath() +
                        File.separator +
                        "jni" +
                        File.separator +
                        "Application.mk");
                cpp_body = new String(StreamUtil.readBytes(new FileInputStream(cpp_file))) + "\n" + so_api;
                outputStream = new FileOutputStream(cpp_file);
                outputStream.write(cpp_body.getBytes());
                outputStream.close();
                String[] cmd;
                if (OsUtils.isOSLinux())
                    cmd = new String[]{"/root/ndk-bundle/ndk-build",
                            "-C",
                            cacheCpp + File.separator + "jni",
                            "-j",
                            "3"};
                else
                    cmd = new String[]{"cmd.exe",
                            "/c",
                            "ndk-build",
                            "-C",
                            cacheCpp + File.separator + "jni",
                            "-j",
                            "3"};
                try {
                    if (exec(cmd)) {
                        cacheLibs = new File(cacheCpp, "libs");
                        if (!cacheLibs.exists() || !cacheLibs.isDirectory()) {
                            FileUtils.delete(cacheCpp);
                            throw new Exception("NDK Build failed....");
                        }
                        if (getConfiguration().contains("so_framework")) {
                            JsonArray SO_API = new JsonParser().parse(getConfiguration()).getAsJsonObject().getAsJsonArray("so_framework");
                            for (JsonElement jsonElement : SO_API) {
                                switch (jsonElement.getAsString()) {
                                    case "armeabi":
                                        getReplacerRes().put("lib/armeabi/libArmEpic.so", StreamUtil.readBytes(new FileInputStream(cacheLibs + File.separator + "armeabi-v7a" + File.separator + "libArmEpic.so")));
                                        getReplacerRes().put("lib/armeabi/libEpic.so", LoaderRes.getInstance().getStaticResAsBytes("so/epic/armeabi-v7a/libEpic.so"));
                                        break;
                                    case "armeabi-v7a":
                                        getReplacerRes().put("lib/armeabi-v7a/libArmEpic.so", StreamUtil.readBytes(new FileInputStream(cacheLibs + File.separator + "armeabi-v7a" + File.separator + "libArmEpic.so")));
                                        getReplacerRes().put("lib/armeabi-v7a/libEpic.so", LoaderRes.getInstance().getStaticResAsBytes("so/epic/armeabi-v7a/libEpic.so"));
                                        break;
                                    case "arm64-v8a":
                                        getReplacerRes().put("lib/arm64-v8a/libArmEpic.so", StreamUtil.readBytes(new FileInputStream(cacheLibs + File.separator + "arm64-v8a" + File.separator + "libArmEpic.so")));
                                        getReplacerRes().put("lib/arm64-v8a/libEpic.so", LoaderRes.getInstance().getStaticResAsBytes("so/epic/arm64-v8a/libEpic.so"));
                                        break;
                                }
                            }
                        } else {
                            getReplacerRes().put("lib/armeabi-v7a/libArmEpic.so", StreamUtil.readBytes(new FileInputStream(cacheLibs + File.separator + "armeabi-v7a" + File.separator + "libArmEpic.so")));
                            getReplacerRes().put("lib/armeabi-v7a/libEpic.so", LoaderRes.getInstance().getStaticResAsBytes("so/epic/armeabi-v7a/libEpic.so"));
                        }
                    } else {
                        FileUtils.delete(cacheCpp);
                        throw new Exception("NDK Build failed....");
                    }
                } catch (ThreadDeath threadDeath) {
                    if (Constant.isDevelopment())
                        logger.info("NDK编译/删除临时文件");
                    FileUtils.delete(cacheCpp);
                    throw new ThreadDeath();
                } finally {
                    FileUtils.delete(cacheCpp);
                }
            }
            break;
        }
        getReplacerRes().put("assets/Epic/dex文件存放路径.txt", "Dex文件过多。无法合并,放到该目录可动态加载".getBytes());
        switch (type_json.getAsInt()) {
            case 0: {
                SignerInfo signerInfo = new SignerInfo(Lists.newArrayList(new SignerInfo.Signer("assets/Arm_Epic", ZipEntry.DEFLATED)));
                getReplacerRes().put("Signer_mode", new Gson().toJson(signerInfo).getBytes());
            }
            break;
            case 1: {
                getReplacerRes().put("assets/Arm_Epic", StreamUtil.readBytes(new FileInputStream(new File(Constant.getTmp(), getUuid()))));
            }
            break;
        }
    }

    @Override
    public String getResult() {
        return Objects.requireNonNull(SysConfigUtil.getLanguageConfigUtil(getLanguageEnums(), "signer.pro.tips"));
    }

    @Override
    public int priority() {
        return 0;
    }

    @Override
    public List<String> getIgnores() {
        return ignores;
    }

    @Override
    public int compareTo(BaseTransformer o) {
        return 0;
    }
}
