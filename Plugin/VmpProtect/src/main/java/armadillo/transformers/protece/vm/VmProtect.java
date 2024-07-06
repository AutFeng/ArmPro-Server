package armadillo.transformers.protece.vm;

import armadillo.Constant;
import armadillo.common.SimpleNameFactory;
import armadillo.transformers.base.BaseTransformer;
import armadillo.transformers.base.OtherTransformer;
import armadillo.transformers.protece.vm.converter.*;
import armadillo.transformers.protece.vm.enums.Interpreter;
import armadillo.transformers.protece.vm.vmutils.MethodHelper;
import armadillo.utils.*;
import armadillo.utils.axml.AutoXml.ManifestAppName;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.jf.dexlib2.AccessFlags;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.builder.BuilderInstruction;
import org.jf.dexlib2.builder.MutableMethodImplementation;
import org.jf.dexlib2.builder.instruction.*;
import org.jf.dexlib2.dexbacked.*;
import org.jf.dexlib2.iface.*;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.formats.Instruction35c;
import org.jf.dexlib2.iface.instruction.formats.Instruction3rc;
import org.jf.dexlib2.iface.reference.MethodReference;
import org.jf.dexlib2.iface.reference.TypeReference;
import org.jf.dexlib2.iface.value.*;
import org.jf.dexlib2.immutable.*;
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction10x;
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction21c;
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction35c;
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction3rc;
import org.jf.dexlib2.immutable.reference.ImmutableFieldReference;
import org.jf.dexlib2.immutable.reference.ImmutableMethodReference;
import org.jf.dexlib2.immutable.reference.ImmutableStringReference;
import org.jf.dexlib2.rewriter.*;
import org.jf.dexlib2.util.MethodUtil;
import org.jf.dexlib2.writer.io.MemoryDataStore;
import org.jf.dexlib2.writer.pool.DexPool;

import javax.annotation.Nonnull;
import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class VmProtect extends OtherTransformer {
    protected boolean is_Ollvm() {
        return false;
    }

    private final StringBuilder applicationBuild = new StringBuilder();
    private final String[] api = {"armeabi-v7a", "arm64-v8a", "x86", "x86_64"};
    private final String[] dex = {"classes", "dex"};
    private final String[] vm = {"arm", "epic", "epic/vm", "arm/vm", "android", "androidx", "core/vm", "android/vm", "androidx/vm"};
    private final SimpleNameFactory nameFactory = new SimpleNameFactory();
    private final List<String> KeepMethodNames = Lists.newArrayList("<init>");
    private final List<String> WithMethodNames = Lists.newArrayList();
    private final List<String> KeepClassDefs = Lists.newArrayList(
            "Landroidx/core/app/CoreComponentFactory;",
            "Landroid/support/v4/app/CoreComponentFactory;");
    private final String jni_load = "#include <jni.h>\n" +
            "#include \"GlobalCache.h\"\n" +
            "\n" +
            "${functions}\n" +
            "\n" +
            "extern \"C\" JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {\n" +
            "    JNIEnv *env = nullptr;\n" +
            "    jint result = -1;\n" +
            "    if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK)\n" +
            "        return result;\n" +
            "    cacheInitial(env);\n" +
            "    ${call_functions}\n" +
            "    return JNI_VERSION_1_6;\n" +
            "}";
    private final StringBuilder functions = new StringBuilder();
    private final StringBuilder call_functions = new StringBuilder();
    private final Interpreter interpreter = Interpreter.switch_vm_2;

    @Override
    public void transform() throws Exception {
        if (configuration != null) {
            JsonArray separate = new JsonParser()
                    .parse(configuration)
                    .getAsJsonObject()
                    .getAsJsonArray(Long.toString(137438953472L));
            List<String> obf = new ArrayList<>();
            for (JsonElement jsonElement : separate)
                obf.add(jsonElement.getAsString());
            if (obf.size() <= 0) return;
            String MainClass = String.format("L%s/%s;", vm[new Random().nextInt(vm.length)], nameFactory.nextName());
            String soName = vm[new Random().nextInt(vm.length)].replace("/", "_");
            byte[] axml = getReplacerRes().get("AndroidManifest.xml");
            if (axml == null)
                axml = StreamUtil.readBytes(getZipFile().getInputStream(new ZipEntry("AndroidManifest.xml")));
            ManifestAppName appName = new ManifestAppName();
            ManifestAppName.XmlMode xmlMode = appName.parseManifest(new ByteArrayInputStream(axml), MainClass.substring(1, MainClass.length() - 1).replace("/", "."));
            getReplacerRes().put("AndroidManifest.xml", xmlMode.getData());
            String dirName = String.format("dex_vm_%s", uuid);
            File cacheJni = new File(new File(Constant.getCache(), dirName), "jni");
            /*List<String> obf = Lists.newArrayList(
                    "Larm/verify/test/Test2;",
                    "Larm/verify/test/Test2$a;",
                    "Larm/verify/test/Test2$a$aa;",
                    "Larm/verify/test/Test2$CallBack$Stub$Proxy;",
                    "Larm/verify/test/Test2$CallBack$Stub;",
                    "Larm/verify/test/Test2$CallBack;",
                    "Larm/verify/test/Test;",
                    "Larm/verify/test/Test$B;");
            File cacheJni = new File("C:\\vm\\jni");*/
            try {
                cacheJni.mkdirs();
                ZipUtils.zipUncompress(new ZipInputStream(LoaderRes.getInstance().getStaticResAsStream("zip/vm.zip")), cacheJni.getAbsolutePath());
                ClassAnalyzer classAnalyzer = new ClassAnalyzer(zipFile, dex);
                ClassAnalyzer SysclassAnalyzer = null;
                try (ZipFile framework = new ZipFile(Constant.getRes() + File.separator + "static" + File.separator + "framework.jar")) {
                    SysclassAnalyzer = new ClassAnalyzer(framework, new String[]{"classes", "dex"});
                }
                Enumeration<? extends ZipEntry> entries = getZipFile().entries();
                while (entries.hasMoreElements()) {
                    ZipEntry zipEntry = entries.nextElement();
                    if (zipEntry.getName().startsWith(dex[0]) && zipEntry.getName().endsWith(dex[1])) {
                        DexBackedDexFile dexFile = DexBackedDexFile.fromInputStream(Opcodes.getDefault(), new BufferedInputStream(zipFile.getInputStream(new ZipEntry(zipEntry.getName()))));
                        HashSet<ClassDef> classDefs = new HashSet<>();
                        ReferencesAnalyzer referencesAnalyzer = new ReferencesAnalyzer(KeepMethodNames, classAnalyzer);
                        for (DexBackedClassDef classDef : dexFile.getClasses()) {
                            if (!obf.contains(classDef.getType())
                                    || KeepClassDefs.contains(classDef.getType())
                                    || SysclassAnalyzer.isExists(classDef.getType())
                                    || AccessFlags.BRIDGE.isSet(classDef.getAccessFlags()))
                                continue;
                            referencesAnalyzer.parseClassDef(classDef);
                            classDefs.add(classDef);
                        }
                        referencesAnalyzer.makeReference();
                        NoneInstructionRewriter instructionRewriter = new NoneInstructionRewriter();
                        instructionRewriter.loadReferences(referencesAnalyzer);
                        final String dexName = zipEntry.getName().replace(".dex", "");
                        try (FileWriter ResolverCodeWriter = new FileWriter(new File(String.format("%s%sgenerated", cacheJni.getAbsolutePath(), File.separator), String.format("%s_resolver.cpp", dexName)));
                             FileWriter JniCodeWriter = new FileWriter(new File(String.format("%s%sgenerated", cacheJni.getAbsolutePath(), File.separator), String.format("%s_functions.cpp", dexName)))) {
                            ResolverCodeGenerator resolverCodeGenerator = new ResolverCodeGenerator(referencesAnalyzer);
                            resolverCodeGenerator.generate(ResolverCodeWriter);
                            JniCodeGenerator jniCodeGenerator = new JniCodeGenerator(interpreter, dexName, classDefs, instructionRewriter, KeepMethodNames, WithMethodNames);
                            jniCodeGenerator.generate(JniCodeWriter);
                        }
                        functions.append(String.format("extern void %s_setup(JNIEnv *env);\n", dexName));
                        call_functions.append(String.format("%s_setup(env);\n", dexName));
                        DexFile nativeDexFile = new DexRewriter(new RewriterModule() {
                            @Nonnull
                            @Override
                            public Rewriter<ClassDef> getClassDefRewriter(@Nonnull Rewriters rewriters) {
                                return new ClassDefRewriter(rewriters) {
                                    @Nonnull
                                    @Override
                                    public ClassDef rewrite(@Nonnull ClassDef classDef) {
                                        if (!classDefs.contains(classDef))
                                            return super.rewrite(classDef);
                                        List<Method> methods = new ArrayList<>();
                                        for (Method value : classDef.getMethods()) {
                                            if (WithMethodNames.size() > 0 && !WithMethodNames.contains(value.getName()))
                                                methods.add(value);
                                            else {
                                                if (KeepMethodNames.contains(value.getName())
                                                        || value.getImplementation() == null)
                                                    methods.add(value);
                                                else {
                                                    List<MethodParameter> parameters = new ArrayList<>();
                                                    for (MethodParameter parameter : value.getParameters())
                                                        parameters.add(new ImmutableMethodParameter(parameter.getType(), parameter.getAnnotations(), null));
                                                    /*init*/
                                                    if ("<init>".equals(value.getName())) {
                                                        List<Instruction> inst = new ArrayList<>();
                                                        MutableMethodImplementation methodImplementation = new MutableMethodImplementation(value.getImplementation());
                                                        for (int i = 0; i < methodImplementation.getInstructions().size(); i++) {
                                                            Instruction instruction = methodImplementation.getInstructions().get(i);
                                                            inst.add(instruction);
                                                            //INVOKE_DIRECT
                                                            if (instruction.getOpcode() == Opcode.INVOKE_DIRECT) {
                                                                Instruction35c instruction35c = (Instruction35c) instruction;
                                                                MethodReference reference = (MethodReference) instruction35c.getReference();
                                                                if ((reference.getDefiningClass().equals(classDef.getSuperclass()) || reference.getDefiningClass().equals(classDef.getType()))
                                                                        && reference.getName().equals("<init>")
                                                                        && reference.getReturnType().equals("V")) {
                                                                    if (methodImplementation.getInstructions().get(i + 1).getOpcode() == Opcode.RETURN_VOID) {
                                                                        methods.add(value);
                                                                        break;
                                                                    }
                                                                    inst.add(new ImmutableInstruction3rc(Opcode.INVOKE_DIRECT_RANGE,
                                                                            value.getImplementation().getRegisterCount() - MethodUtil.getParameterRegisterCount(value),
                                                                            MethodUtil.getParameterRegisterCount(value),
                                                                            new ImmutableMethodReference(classDef.getType(),
                                                                                    MethodHelper.genMethodName(value),
                                                                                    value.getParameterTypes(),
                                                                                    "V")));
                                                                    inst.add(new ImmutableInstruction10x(Opcode.RETURN_VOID));
                                                                    methods.add(new ImmutableMethod(value.getDefiningClass(),
                                                                            value.getName(),
                                                                            parameters,
                                                                            value.getReturnType(),
                                                                            value.getAccessFlags(),
                                                                            value.getAnnotations(),
                                                                            value.getHiddenApiRestrictions(),
                                                                            new ImmutableMethodImplementation(
                                                                                    value.getImplementation().getRegisterCount(),
                                                                                    inst,
                                                                                    null,
                                                                                    null
                                                                            )));

                                                                    break;
                                                                }
                                                            }
                                                            //INVOKE_DIRECT_RANGE
                                                            else if (instruction.getOpcode() == Opcode.INVOKE_DIRECT_RANGE) {
                                                                Instruction3rc instruction3rc = (Instruction3rc) instruction;
                                                                MethodReference reference = (MethodReference) instruction3rc.getReference();
                                                                if ((reference.getDefiningClass().equals(classDef.getSuperclass()) || reference.getDefiningClass().equals(classDef.getType()))
                                                                        && reference.getName().equals("<init>")
                                                                        && reference.getReturnType().equals("V")) {
                                                                    if (methodImplementation.getInstructions().get(i + 1).getOpcode() == Opcode.RETURN_VOID) {
                                                                        methods.add(value);
                                                                        break;
                                                                    }
                                                                    inst.add(new ImmutableInstruction3rc(Opcode.INVOKE_DIRECT_RANGE,
                                                                            value.getImplementation().getRegisterCount() - MethodUtil.getParameterRegisterCount(value),
                                                                            MethodUtil.getParameterRegisterCount(value),
                                                                            new ImmutableMethodReference(classDef.getType(),
                                                                                    MethodHelper.genMethodName(value),
                                                                                    value.getParameterTypes(),
                                                                                    "V")));
                                                                    inst.add(new ImmutableInstruction10x(Opcode.RETURN_VOID));
                                                                    methods.add(new ImmutableMethod(value.getDefiningClass(),
                                                                            value.getName(),
                                                                            parameters,
                                                                            value.getReturnType(),
                                                                            value.getAccessFlags(),
                                                                            value.getAnnotations(),
                                                                            value.getHiddenApiRestrictions(),
                                                                            new ImmutableMethodImplementation(
                                                                                    value.getImplementation().getRegisterCount(),
                                                                                    inst,
                                                                                    null,
                                                                                    null
                                                                            )));
                                                                    break;
                                                                }
                                                            }
                                                        }
                                                        methods.add(new ImmutableMethod(value.getDefiningClass(),
                                                                MethodHelper.genMethodName(value),
                                                                parameters,
                                                                value.getReturnType(),
                                                                AccessFlags.PRIVATE.getValue()
                                                                        | AccessFlags.NATIVE.getValue()
                                                                        | AccessFlags.SYNTHETIC.getValue(),
                                                                value.getAnnotations(),
                                                                null,
                                                                null));
                                                    }
                                                    /*clinit*/
                                                    else if ("<clinit>".equals(value.getName())) {
                                                        List<Instruction> inst = new ArrayList<>();
                                                        inst.add(new ImmutableInstruction35c(Opcode.INVOKE_STATIC,
                                                                0,
                                                                0,
                                                                0,
                                                                0,
                                                                0,
                                                                0,
                                                                new ImmutableMethodReference(classDef.getType(),
                                                                        MethodHelper.genMethodName(value),
                                                                        Lists.newArrayList(),
                                                                        "V")));
                                                        inst.add(new ImmutableInstruction10x(Opcode.RETURN_VOID));
                                                        methods.add(new ImmutableMethod(value.getDefiningClass(),
                                                                value.getName(),
                                                                parameters,
                                                                value.getReturnType(),
                                                                value.getAccessFlags(),
                                                                value.getAnnotations(),
                                                                value.getHiddenApiRestrictions(),
                                                                new ImmutableMethodImplementation(
                                                                        value.getImplementation().getRegisterCount(),
                                                                        inst,
                                                                        null,
                                                                        null
                                                                )));
                                                        methods.add(new ImmutableMethod(value.getDefiningClass(),
                                                                MethodHelper.genMethodName(value),
                                                                parameters,
                                                                value.getReturnType(),
                                                                AccessFlags.PRIVATE.getValue()
                                                                        | AccessFlags.STATIC.getValue()
                                                                        | AccessFlags.NATIVE.getValue()
                                                                        | AccessFlags.SYNTHETIC.getValue(),
                                                                value.getAnnotations(),
                                                                null,
                                                                null));
                                                    }
                                                    /*other*/
                                                    else {
                                                        methods.add(new ImmutableMethod(value.getDefiningClass(),
                                                                value.getName(),
                                                                parameters,
                                                                value.getReturnType(),
                                                                value.getAccessFlags() | AccessFlags.NATIVE.getValue(),
                                                                value.getAnnotations(),
                                                                null,
                                                                null));
                                                    }
                                                }
                                            }
                                        }
                                        return new ImmutableClassDef(
                                                classDef.getType(),
                                                classDef.getAccessFlags(),
                                                classDef.getSuperclass(),
                                                classDef.getInterfaces(),
                                                "EpicVm_Protect",
                                                classDef.getAnnotations(),
                                                classDef.getFields(),
                                                methods);
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
                        if (jsonElement.getAsString().equals("armeabi"))
                            throw new Exception("Unsupported architecture: armeabi");
                        if (Arrays.asList(api).contains(jsonElement.getAsString()))
                            applicationBuild.append(jsonElement.getAsString()).append(" ");
                    }
                    applicationBuild.append("\n");
                } else
                    applicationBuild.append("arm64-v8a x86\n");
                try (FileOutputStream vm_out = new FileOutputStream(new File(cacheJni, "vm.cpp"));
                     FileOutputStream application_out = new FileOutputStream(new File(cacheJni, "Application.mk"))) {
                    vm_out.write(jni_load.replace("${functions}", functions.toString()).replace("${call_functions}", call_functions.toString()).getBytes());
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
                        WriteSo(cacheLibs, soName);
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
            {
                List<Method> methods = new ArrayList<>();
                /**
                 * init
                 */
                {
                    methods.add(new ImmutableMethod(
                            MainClass,
                            "<init>",
                            null,
                            "V",
                            AccessFlags.PUBLIC.getValue() | AccessFlags.CONSTRUCTOR.getValue(),
                            null,
                            null,
                            new ImmutableMethodImplementation(
                                    1,
                                    Lists.newArrayList(
                                            new ImmutableInstruction35c(Opcode.INVOKE_DIRECT
                                                    , 1
                                                    , 0
                                                    , 0
                                                    , 0
                                                    , 0
                                                    , 0
                                                    , new ImmutableMethodReference(
                                                    xmlMode.isCustomApplication() ? "L" + xmlMode.getCustomApplicationName().replace(".", "/") + ";" : "Landroid/app/Application;",
                                                    "<init>",
                                                    null,
                                                    "V")),
                                            new ImmutableInstruction10x(Opcode.RETURN_VOID)),
                                    null,
                                    null)));
                    MutableMethodImplementation methodImplementation = new MutableMethodImplementation(1);
                    methodImplementation.addInstruction(0, new BuilderInstruction21c(Opcode.CONST_STRING, 0, new ImmutableStringReference(soName)));
                    methodImplementation.addInstruction(1, new BuilderInstruction35c(Opcode.INVOKE_STATIC, 1, 0, 0, 0, 0, 0,
                            new ImmutableMethodReference("Ljava/lang/System;",
                                    "loadLibrary",
                                    Lists.newArrayList("Ljava/lang/String;"),
                                    "V")));
                    methodImplementation.addInstruction(new BuilderInstruction10x(Opcode.RETURN_VOID));
                    methods.add(new ImmutableMethod(MainClass,
                            "<clinit>",
                            null,
                            "V",
                            AccessFlags.CONSTRUCTOR.getValue() | AccessFlags.STATIC.getValue(),
                            null,
                            null, methodImplementation));
                }
                ImmutableClassDef classDef = new ImmutableClassDef(
                        MainClass,
                        AccessFlags.PUBLIC.getValue(),
                        xmlMode.isCustomApplication() ? "L" + xmlMode.getCustomApplicationName().replace(".", "/") + ";" : "Landroid/app/Application;",
                        null,
                        null,
                        null,
                        null,
                        methods);
                DexPool dexPool = new DexPool(Opcodes.getDefault());
                dexPool.internClass(classDef);
                MemoryDataStore dataStore = new MemoryDataStore();
                dexPool.writeTo(dataStore);
                getAdd_Classdex().add(Arrays.copyOf(dataStore.getBuffer(), dataStore.getSize()));
                dataStore.close();
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

    private void WriteSo(File dir, String soName) {
        for (File so : Objects.requireNonNull(dir.listFiles())) {
            if (so.isDirectory()) {
                WriteSo(so, soName);
            } else if (so.isFile()) {
                try {
                    if (soName == null)
                        getReplacerRes().put("lib/" + new File(so.getParent()).getName() + "/" + so.getName(), StreamUtil.readBytes(new FileInputStream(so)));
                    else
                        getReplacerRes().put("lib/" + new File(so.getParent()).getName() + "/" + String.format("lib%s.so", soName), StreamUtil.readBytes(new FileInputStream(so)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
