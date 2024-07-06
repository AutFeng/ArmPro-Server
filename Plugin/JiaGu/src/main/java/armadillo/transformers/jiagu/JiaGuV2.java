package armadillo.transformers.jiagu;

import armadillo.Constant;
import armadillo.result.ignore;
import armadillo.transformers.base.BaseTransformer;
import armadillo.transformers.base.OtherTransformer;
import armadillo.transformers.jiagu.converter.JniCodeGenerator;
import armadillo.transformers.jiagu.converter.NoneInstructionRewriter;
import armadillo.transformers.jiagu.converter.ReferencesAnalyzer;
import armadillo.transformers.jiagu.converter.ResolverCodeGenerator;
import armadillo.utils.*;
import armadillo.utils.axml.AutoXml.ManifestAppName;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jf.dexlib2.AccessFlags;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.dexbacked.DexBackedClassDef;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.dexbacked.DexBackedMethod;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.MethodParameter;
import org.jf.dexlib2.immutable.ImmutableMethod;
import org.jf.dexlib2.immutable.ImmutableMethodParameter;
import org.jf.dexlib2.immutable.reference.ImmutableMethodProtoReference;
import org.jf.dexlib2.rewriter.*;
import org.jf.dexlib2.writer.io.MemoryDataStore;
import org.jf.dexlib2.writer.pool.DexPool;

import javax.annotation.Nonnull;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class JiaGuV2 extends OtherTransformer {
    private final List<String> ignores = new ArrayList<>();
    private final List<String> KeepMethodNames = Lists.newArrayList("<init>", "<clinit>");
    private final String[] api = {"armeabi-v7a", "arm64-v8a", "x86", "x86_64"};
    private final StringBuilder functions = new StringBuilder();
    private final StringBuilder call_functions = new StringBuilder();
    private final StringBuilder applicationBuild = new StringBuilder();

    @Override
    public void transform() throws Exception {
        String dirName = String.format("dex_vm_%s", UUID.randomUUID().toString());
        File cacheJni = new File(new File(Constant.getCache(), dirName), "jni");
        try {
            cacheJni.mkdirs();
            ZipUtils.zipUncompress(new ZipInputStream(LoaderRes.getInstance().getStaticResAsStream("zip/JiaGuVm.zip")), cacheJni.getAbsolutePath());
            Enumeration<? extends ZipEntry> entries = getZipFile().entries();
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                if (zipEntry.getName().startsWith("classes") && zipEntry.getName().endsWith("dex")) {
                    DexBackedDexFile dexFile = DexBackedDexFile.fromInputStream(Opcodes.getDefault(), new BufferedInputStream(zipFile.getInputStream(new ZipEntry(zipEntry.getName()))));
                    HashSet<ClassDef> classDefs = new HashSet<>();
                    ReferencesAnalyzer referencesAnalyzer = new ReferencesAnalyzer(KeepMethodNames);
                    for (DexBackedClassDef classDef : dexFile.getClasses()) {
                        if (isExistsOnCreate(classDef)) {
                            referencesAnalyzer.parseClassDef(classDef);
                            classDefs.add(classDef);
                        }
                    }
                    if (classDefs.size() == 0) {
                        getReplacerRes().put(zipEntry.getName(), StreamUtil.readBytes(zipFile.getInputStream(zipEntry)));
                    } else {
                        referencesAnalyzer.makeReference();
                        NoneInstructionRewriter instructionRewriter = new NoneInstructionRewriter();
                        instructionRewriter.loadReferences(referencesAnalyzer);
                        final String dexName = zipEntry.getName().replace(".dex", "");
                        try (FileWriter ResolverCodeWriter = new FileWriter(new File(String.format("%s%sgenerated", cacheJni.getAbsolutePath(), File.separator), String.format("%s_resolver.cpp", dexName)));
                             FileWriter JniCodeWriter = new FileWriter(new File(String.format("%s%sgenerated", cacheJni.getAbsolutePath(), File.separator), String.format("%s_functions.cpp", dexName)))) {
                            ResolverCodeGenerator resolverCodeGenerator = new ResolverCodeGenerator(referencesAnalyzer);
                            resolverCodeGenerator.generate(ResolverCodeWriter);
                            JniCodeGenerator jniCodeGenerator = new JniCodeGenerator(dexName, classDefs, instructionRewriter, KeepMethodNames);
                            jniCodeGenerator.generate(JniCodeWriter);
                        }
                        functions.append(String.format("extern void %s_setup(JNIEnv *env);\n", dexName));
                        call_functions.append(String.format("%s_setup(env);", dexName));
                        DexFile nativeDexFile = new DexRewriter(new RewriterModule() {
                            @Nonnull
                            @Override
                            public Rewriter<Method> getMethodRewriter(@Nonnull Rewriters rewriters) {
                                return new MethodRewriter(rewriters) {
                                    @Nonnull
                                    @Override
                                    public Method rewrite(@Nonnull Method value) {
                                        if (KeepMethodNames.contains(value.getName())
                                                || !"onCreate".equals(value.getName())
                                                || value.getImplementation() == null)
                                            return super.rewrite(value);
                                        else {
                                            List<MethodParameter> parameters = new ArrayList<>();
                                            for (MethodParameter parameter : value.getParameters())
                                                parameters.add(new ImmutableMethodParameter(parameter.getType(), parameter.getAnnotations(), null));
                                            return new ImmutableMethod(value.getDefiningClass(),
                                                    value.getName(),
                                                    parameters,
                                                    value.getReturnType(),
                                                    value.getAccessFlags() | AccessFlags.NATIVE.getValue(),
                                                    value.getAnnotations(),
                                                    null,
                                                    null);
                                        }
                                    }
                                };
                            }
                        }).getDexFileRewriter().rewrite(dexFile);
                        DexPool dexPool = new DexPool(Opcodes.getDefault());
                        nativeDexFile.getClasses().forEach(dexPool::internClass);
                        MemoryDataStore dataStore = new MemoryDataStore();
                        dexPool.writeTo(dataStore);
                        getReplacerRes().put(zipEntry.getName(), Arrays.copyOf(dataStore.getBuffer(), dataStore.getSize()));
                        dataStore.close();
                    }
                }
            }
            //加密Dex
            {
                HashMap<String, byte[]> AddDex = new HashMap<>();
                for (Map.Entry<String, byte[]> entry : getReplacerRes().entrySet()) {
                    if (entry.getKey().startsWith("classes") && entry.getKey().endsWith("dex")) {
                        byte[] bytes = entry.getValue();
                        for (int i = 0; i < bytes.length; i++)
                            bytes[i] = (byte) (~bytes[i] & 0x00ff);
                        AddDex.put("assets/" + entry.getKey(), bytes);
                        ignores.add(entry.getKey());
                    }
                }
                for (String s : ignores) {
                    if (s.startsWith("classes") && s.endsWith("dex"))
                        getReplacerRes().remove(s);
                }
                for (Map.Entry<String, byte[]> entry : AddDex.entrySet()) {
                    getReplacerRes().put(entry.getKey(), entry.getValue());
                }
                AddDex.clear();
            }
            //修改AXML
            {
                byte[] axml = getReplacerRes().get("AndroidManifest.xml");
                if (axml == null)
                    axml = StreamUtil.readBytes(getZipFile().getInputStream(new ZipEntry("AndroidManifest.xml")));
                ManifestAppName appName = new ManifestAppName();
                ManifestAppName.XmlMode xmlMode = appName.parseManifest(new ByteArrayInputStream(axml), "arm.StubApp");
                getReplacerRes().put("AndroidManifest.xml", xmlMode.getData());
                byte[] bytes = LoaderRes.getInstance().getStaticResAsBytes("smali/StubApp_VM.smali");
                String body = new String(bytes).replace("SrcApplication", (xmlMode.isCustomApplication() ? xmlMode.getCustomApplicationName() : "android.app.Application"));
                DexPool dexPool = new DexPool(Opcodes.getDefault());
                dexPool.internClass(SmaliUtils.assembleSmali(body.getBytes()));
                MemoryDataStore dataStore = new MemoryDataStore();
                dexPool.writeTo(dataStore);
                getReplacerRes().put("classes.dex", Arrays.copyOf(dataStore.getBuffer(), dataStore.getSize()));
                dataStore.close();
            }
            applicationBuild
                    .append("APP_STL := c++_static\n")
                    .append("APP_CPPFLAGS += -fvisibility=hidden\n");
            applicationBuild.append("APP_PLATFORM := android-19\n");
            applicationBuild.append("APP_ABI := ");
            if (configuration.contains("so_framework")) {
                JsonArray SO_API = new JsonParser().parse(configuration).getAsJsonObject().getAsJsonArray("so_framework");
                for (JsonElement jsonElement : SO_API) {
                    if (jsonElement.getAsString().equals("armeabi"))
                        throw new Exception("Unsupported architecture: armeabi");
                    if (Arrays.asList(api).contains(jsonElement.getAsString()))
                        applicationBuild.append(jsonElement.getAsString()).append(" ");
                }
                applicationBuild.append("\n");
            } else
                applicationBuild.append("armeabi-v7a x86\n");
            File vm = new File(cacheJni, "vm.cpp");
            byte[] bytes = StreamUtil.readBytes(new FileInputStream(vm));
            vm.delete();
            try (FileOutputStream vm_out = new FileOutputStream(vm);
                 FileOutputStream application_out = new FileOutputStream(new File(cacheJni, "Application.mk"))) {
                vm_out.write(new String(bytes)
                        .replace("${functions}", functions.toString())
                        .replace("${call_functions}", call_functions.toString())
                        .replace("Signer Data", getApkSignatureSHA1(zipFile))
                        .getBytes());
                application_out.write(applicationBuild.toString().getBytes());
            }
            String[] cmd;
            if (OsUtils.isOSLinux())
                cmd = new String[]{"/www/basic/ndk-build",
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
                if (cacheLibs.exists() && cacheLibs.isDirectory())
                    WriteSo(cacheLibs);
                else
                    throw new Exception("NDK Build failed....");
            } else
                throw new Exception("NDK Build failed....");
        } catch (Exception e) {
            if (cacheJni.exists())
                FileUtils.delete(cacheJni.getParentFile());
            throw e;
        } finally {
            if (cacheJni.exists())
                FileUtils.delete(cacheJni.getParentFile());
        }
    }

    private boolean isExistsOnCreate(DexBackedClassDef classDef) {
        for (DexBackedMethod method : classDef.getMethods()) {
            if ("onCreate".equals(method.getName()) && method.getImplementation() != null)
                return true;
        }
        return false;
    }

    @Override
    public List<String> getIgnores() {
        return ignores;
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

    private String getApkSignatureSHA1(ZipFile zipFile) throws Exception {
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry ze = entries.nextElement();
            String name = ze.getName().toUpperCase();
            if (name.startsWith("META-INF/") && (name.endsWith(".RSA") || name.endsWith(".DSA"))) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream(baos);
                CertificateFactory factory = CertificateFactory.getInstance("X.509");
                Collection<? extends Certificate> certificates = factory.generateCertificates(zipFile.getInputStream(ze));
                Iterator<? extends Certificate> iterator = certificates.iterator();
                while (iterator.hasNext()) {
                    Certificate certificate = iterator.next();
                    return SHA1(certificate.getEncoded());
                }
            }
        }
        throw new Exception("META-INF/XXX.RSA (DSA) file not found.");
    }

    private String SHA1(byte[] str) {
        char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        try {
            MessageDigest mdTemp = MessageDigest.getInstance("SHA1");
            mdTemp.update(str);
            byte[] md = mdTemp.digest();
            int j = md.length;
            char[] buf = new char[j * 2];
            int k = 0;
            for (byte byte0 : md) {
                buf[k++] = hexDigits[byte0 >>> 4 & 0xf];
                buf[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(buf);
        } catch (Exception e) {
            return null;
        }
    }
}
