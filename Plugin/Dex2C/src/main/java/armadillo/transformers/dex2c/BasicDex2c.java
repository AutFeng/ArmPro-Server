package armadillo.transformers.dex2c;

import armadillo.Constant;
import armadillo.transformers.base.BaseTransformer;
import armadillo.transformers.base.OtherTransformer;
import armadillo.utils.*;
import armadillo.utils.axml.AutoXml.ManifestParse;
import by.radioegor146.NativeObfuscator;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.googlecode.d2j.dex.Dex2jar;
import com.googlecode.d2j.reader.BaseDexFileReader;
import com.googlecode.d2j.reader.MultiDexFileReader;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.dexbacked.DexBackedClassDef;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.dexbacked.DexBackedMethod;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.immutable.reference.ImmutableMethodProtoReference;
import org.jf.dexlib2.writer.io.MemoryDataStore;
import org.jf.dexlib2.writer.pool.DexPool;

import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.zip.ZipEntry;

public class BasicDex2c extends OtherTransformer {
    protected boolean is_Ollvm() {
        return false;
    }

    private final int method_max_size = SysConfigUtil.getIntConfig("dex2c.method.max.size");
    private final int instruction_max_size = SysConfigUtil.getIntConfig("dex2c.instruction.max.size");
    private final String[] api = {"armeabi", "armeabi-v7a", "arm64-v8a", "x86"};

    @Override
    public void transform() throws Exception {
        if (configuration != null) {
            JsonArray separate = new JsonParser()
                    .parse(configuration)
                    .getAsJsonObject()
                    .getAsJsonArray(Long.toString(is_Ollvm() ? 16384 : 8192));
            List<String> obf = new ArrayList<>();
            for (JsonElement jsonElement : separate)
                obf.add(jsonElement.getAsString());
            if (obf.size() <= 0) return;
            String dirName = String.format("dex2c_cache_%s", UUID.randomUUID().toString());
            File cacheJar = new File(Constant.getCache(), dirName + ".jar");
            File cacheJni = new File(new File(Constant.getCache(), dirName), "jni");
            File android_mk = new File(cacheJni, "Android.mk");
            File application_mk = new File(cacheJni, "Application.mk");
            DexPool dexPool = new DexPool(Opcodes.getDefault());
            Enumeration<? extends ZipEntry> entries = getZipFile().entries();
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                if (zipEntry.getName().startsWith("classes") && zipEntry.getName().endsWith(".dex")) {
                    try {
                        DexPool old_dexPool = new DexPool(Opcodes.getDefault());
                        DexBackedDexFile dexBackedDexFile = DexBackedDexFile.fromInputStream(null, new BufferedInputStream(zipFile.getInputStream(zipEntry)));
                        for (DexBackedClassDef classDef : dexBackedDexFile.getClasses()) {
                            if (obf.contains(classDef.getType())) {
                                if (Lists.newArrayList(classDef.getMethods()).size() > method_max_size)
                                    throw new Exception(
                                            String.format("%s class:%s,Too many methods:%d max:%d",
                                                    zipEntry.getName(),
                                                    classDef.getType(),
                                                    Lists.newArrayList(classDef.getMethods()).size(),
                                                    method_max_size));
                                for (DexBackedMethod method : classDef.getMethods()) {
                                    if (method.getImplementation() != null && Lists.newArrayList(method.getImplementation().getInstructions()).size() > instruction_max_size)
                                        throw new Exception(
                                                String.format("%s method:%s,Too many instruction:%d max:%d",
                                                        zipEntry.getName(),
                                                        String.format("%s->%s(%s)%s",
                                                                classDef.getType(),
                                                                method.getName(),
                                                                Arrays.toString(Lists.newArrayList(method.getParameterNames()).toArray(new String[0])).substring(1).replace("]", ""),
                                                                method.getReturnType()),
                                                        Lists.newArrayList(method.getImplementation().getInstructions()).size(),
                                                        instruction_max_size));
                                }
                                dexPool.internClass(classDef);
                            } else
                                old_dexPool.internClass(classDef);
                        }
                        MemoryDataStore dataStore = new MemoryDataStore();
                        old_dexPool.writeTo(dataStore);
                        getReplacerRes().put(zipEntry.getName(), Arrays.copyOf(dataStore.getBuffer(), dataStore.getSize()));
                        dataStore.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw e;
                    }
                }
            }
            /**
             * 转换jar
             */
            try {
                MemoryDataStore dataStore = new MemoryDataStore();
                dexPool.writeTo(dataStore);
                BaseDexFileReader reader = MultiDexFileReader.open(Arrays.copyOf(dataStore.getBuffer(), dataStore.getSize()));
                dataStore.close();
                Dex2jar.from(reader)
                        .reUseReg(false)
                        .topoLogicalSort()
                        .skipDebug(true)
                        .optimizeSynchronized(true)
                        .printIR(false)
                        .noCode(false)
                        .skipExceptions(false)
                        .to(cacheJar.toPath());
            } catch (Exception e) {
                e.printStackTrace();
                throw new Exception("Dex Conversion failed....");
            }
            /**
             * 翻译CPP
             */
            try {
                List<Path> libs = new ArrayList<>();
                new NativeObfuscator().process(cacheJar.toPath(), cacheJni.toPath(), libs, Collections.emptyList(), obf);
            } catch (Exception e) {
                e.printStackTrace();
                if (cacheJar.exists())
                    cacheJar.delete();
                if (cacheJni.exists())
                    FileUtils.delete(cacheJni.getParentFile());
                throw new Exception("CPP Conversion failed....");
            }
            /**
             * 转换成Dex
             */
            try {
                if (!exec(new String[]{
                        "java",
                        "-jar",
                        new File(Constant.getRes(), "dx.jar").getAbsolutePath(),
                        "--dex",
                        "--no-locals",
                        "--no-warning",
                        "--no-strict",
                        "--min-sdk-version",
                        ManifestParse.parseManifestSdk(getZipFile().getInputStream(new ZipEntry("AndroidManifest.xml"))).toString(),
                        "--output=" + new File(cacheJni, "classes.dex").getAbsolutePath(),
                        new File(cacheJni, cacheJar.getName()).getAbsolutePath()}))
                    throw new Exception("Dex Build failed....");
            } catch (Exception e) {
                e.printStackTrace();
                if (cacheJar.exists())
                    cacheJar.delete();
                if (cacheJni.exists())
                    FileUtils.delete(cacheJni.getParentFile());
                throw new Exception("Dex Build failed....");
            }
            /**
             * 写编译配置
             */
            try {
                /**
                 * 写Android.mk
                 */
                FileOutputStream android_mk_out = new FileOutputStream(android_mk);
                String androidbuild = "LOCAL_PATH:= $(call my-dir)\n" +
                        "include $(CLEAR_VARS)\n" +
                        "LOCAL_MODULE    := arm\n" +
                        "LOCAL_CPPFLAGS += -std=c++17\n" +
                        "LOCAL_C_INCLUDES := $(LOCAL_PATH)/cpp\n" +
                        "LOCAL_SRC_FILES := $(wildcard $(LOCAL_PATH)/cpp/*.cpp) \\\n" +
                        "$(wildcard $(LOCAL_PATH)/cpp/*.hpp) \\\n" +
                        "$(wildcard $(LOCAL_PATH)/cpp/output/*.cpp) \\\n" +
                        "$(wildcard $(LOCAL_PATH)/cpp/output/*.hpp)\n" +
                        "include $(BUILD_SHARED_LIBRARY)\n";
                android_mk_out.write(androidbuild.getBytes());
                android_mk_out.close();
                /**
                 * 写Application.mk
                 */
                StringBuilder applicationBuild = new StringBuilder();
                applicationBuild
                        .append("APP_STL := c++_static\n");
                if (is_Ollvm())
                    applicationBuild
                            .append("APP_CPPFLAGS += -fvisibility=hidden -mllvm -sobf\n");
                else
                    applicationBuild
                            .append("APP_CPPFLAGS += -fvisibility=hidden\n");
                applicationBuild.append("APP_PLATFORM := android-19\n");
                applicationBuild.append("APP_ABI := ");
                if (configuration.contains("so_framework")) {
                    JsonArray SO_API = new JsonParser().parse(configuration).getAsJsonObject().getAsJsonArray("so_framework");
                    for (JsonElement jsonElement : SO_API) {
                        if (Arrays.asList(api).contains(jsonElement.getAsString()))
                            applicationBuild.append(jsonElement.getAsString().equals("armeabi") ? "armeabi-v7a" : jsonElement.getAsString()).append(" ");
                    }
                    applicationBuild.append("\n");
                } else
                    applicationBuild.append("armeabi-v7a x86\n");
                FileOutputStream application_mk_out = new FileOutputStream(application_mk);
                application_mk_out.write(applicationBuild.toString().getBytes());
                application_mk_out.close();
            } catch (Exception e) {
                if (cacheJar.exists())
                    cacheJar.delete();
                if (cacheJni.exists())
                    FileUtils.delete(cacheJni.getParentFile());
                throw new Exception("Create Config File failed....");
            }
            /**
             * 编译so
             */
            try {
                String[] cmd;
                if (OsUtils.isOSLinux())
                    cmd = new String[]{is_Ollvm() ? "/root/ndk-bundle/ndk-build" : "/www/basic/ndk-build",
                            "-C",
                            cacheJni.getAbsolutePath(),
                            "-j3"};
                else
                    cmd = new String[]{"cmd.exe",
                            "/c",
                            "ndk-build",
                            "-C",
                            cacheJni.getAbsolutePath(),
                            "-j3"};
                if (exec(cmd)) {
                    File cacheLibs = new File(cacheJni.getParentFile(), "libs");
                    if (cacheLibs.exists() && cacheLibs.isDirectory()) {
                        WriteSo(cacheLibs);
                        File dexFile = new File(cacheJni, "classes.dex");
                        if (dexFile.exists())
                            getAdd_Classdex().add(StreamUtil.readBytes(new FileInputStream(dexFile)));
                        else
                            throw new Exception("Dex Build failed....");
                    } else
                        throw new Exception("NDK Build failed....");
                } else
                    throw new Exception("NDK Build failed....");
            } catch (Exception e) {
                if (cacheJar.exists())
                    cacheJar.delete();
                if (cacheJni.exists())
                    FileUtils.delete(cacheJni.getParentFile());
                throw e;
            }
            /**
             * 释放临时文件
             */ finally {
                if (cacheJar.exists())
                    cacheJar.delete();
                if (cacheJni.exists())
                    FileUtils.delete(cacheJni.getParentFile());
            }
        }
    }

    @Override
    public String getResult() {
        return null;
    }

    @Override
    public int priority() {
        return 0;
    }

    @Override
    public int compareTo(BaseTransformer o) {
        return 0;
    }

    private void WriteSo(File dir) {
        for (File so : Objects.requireNonNull(dir.listFiles())) {
            if (so.isDirectory()) {
                WriteSo(so);
            } else if (so.isFile()) {
                try {
                    getReplacerRes().put("lib/" + new File(so.getParent()).getName() + "/" + so.getName(), StreamUtil.readBytes(new FileInputStream(so)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
