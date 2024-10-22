package by.radioegor146;

import by.radioegor146.instructions.InvokeDynamicHandler;
import by.radioegor146.instructions.MethodHandler;
import by.radioegor146.source.ClassSourceBuilder;
import by.radioegor146.source.MainSourceBuilder;
import by.radioegor146.source.StringPool;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import ru.gravit.launchserver.asm.ClassMetadataReader;
import ru.gravit.launchserver.asm.SafeClassWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.zip.ZipOutputStream;

public class NativeObfuscator {


    private final Snippets snippets;
    private final StringPool stringPool;
    private InterfaceStaticClassProvider staticClassProvider;
    private final MethodProcessor methodProcessor;

    private final NodeCache<String> cachedStrings;
    private final NodeCache<String> cachedClasses;
    private final NodeCache<CachedMethodInfo> cachedMethods;
    private final NodeCache<CachedFieldInfo> cachedFields;

    public List<String> exclusions = Collections.emptyList();
    private StringBuilder nativeMethods;
    private final Map<String, InvokeDynamicInsnNode> invokeDynamics;
    private final Map<String, MethodInsnNode> methodHandleInvokes;

    private int currentClassId;

    public NativeObfuscator() {
        stringPool = new StringPool();
        snippets = new Snippets(stringPool);
        cachedStrings = new NodeCache<>("(cstrings[%d])");
        cachedClasses = new NodeCache<>("(cclasses[%d])");
        cachedMethods = new NodeCache<>("(cmethods[%d])");
        cachedFields = new NodeCache<>("(cfields[%d])");
        invokeDynamics = new HashMap<>();
        methodHandleInvokes = new HashMap<>();
        methodProcessor = new MethodProcessor(this);
    }

    public void process(Path inputJarPath, Path outputDir, List<Path> libs, List<String> exclusions, List<String> obf) throws IOException {
        libs.add(inputJarPath);
        this.exclusions = exclusions;
        ClassMetadataReader metadataReader = new ClassMetadataReader(libs.stream().map(x -> {
            try {
                return new JarFile(x.toFile());
            } catch (IOException ex) {
                return null;
            }
        }).collect(Collectors.toList()));

        Path cppDir = outputDir.resolve("cpp");
        Path cppOutput = cppDir.resolve("output");
        Files.createDirectories(cppOutput);

        Util.copyResource("sources/native_jvm.cpp", cppDir);
        Util.copyResource("sources/native_jvm.hpp", cppDir);
        Util.copyResource("sources/native_jvm_output.hpp", cppDir);
        Util.copyResource("sources/string_pool.hpp", cppDir);

        MainSourceBuilder mainSourceBuilder = new MainSourceBuilder();

        File jarFile = inputJarPath.toAbsolutePath().toFile();
        try (JarFile jar = new JarFile(jarFile);
             ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(outputDir.resolve(jarFile.getName())))) {
            staticClassProvider = new InterfaceStaticClassProvider("arm");
            jar.stream().forEach(entry -> {
                try {
                    if (!entry.getName().endsWith(".class")) {
                        Util.writeEntry(jar, out, entry);
                        return;
                    }
                    if (!obf.contains("L" + entry.getName().replace(".class", "") + ";")) {
                        Util.writeEntry(jar, out, entry);
                        return;
                    }
                    /*if (!entry.getName().startsWith("com/toshiba/dataanalyse/")) {
                        Util.writeEntry(jar, out, entry);
                        return;
                    }*/
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    try (InputStream in = jar.getInputStream(entry)) {
                        Util.transfer(in, baos);
                    }
                    byte[] src = baos.toByteArray();
                    if (Util.byteArrayToInt(Arrays.copyOfRange(src, 0, 4)) != 0xCAFEBABE) {
                        Util.writeEntry(out, entry.getName(), src);
                        return;
                    }

                    nativeMethods = new StringBuilder();

                    ClassReader classReader = new ClassReader(src);
                    ClassNode classNode = new ClassNode(Opcodes.ASM5);
                    classReader.accept(classNode, 0);

                    if (classNode.methods.stream().noneMatch(MethodProcessor::shouldProcess) || exclusions.contains(classNode.name)) {
                        Util.writeEntry(out, entry.getName(), src);
                        return;
                    }
                    if (classNode.methods.stream().noneMatch(x -> x.name.equals("<clinit>"))) {
                        classNode.methods.add(new MethodNode(Opcodes.ASM5, Opcodes.ACC_STATIC,
                                "<clinit>", "()V", null, new String[0]));
                    }

                    staticClassProvider.newClass();

                    invokeDynamics.clear();
                    methodHandleInvokes.clear();

                    cachedStrings.clear();
                    cachedClasses.clear();
                    cachedMethods.clear();
                    cachedFields.clear();

                    try (ClassSourceBuilder cppBuilder = new ClassSourceBuilder(cppOutput, classNode.name, stringPool)) {
                        StringBuilder instructions = new StringBuilder();

                        classNode.sourceFile = "Arm_Dex2C";
                        for (int i = 0; i < classNode.methods.size(); i++) {
                            MethodNode method = classNode.methods.get(i);
                            MethodContext context = new MethodContext(this, method, i, classNode, currentClassId);
                            methodProcessor.processMethod(context);
                            instructions.append(context.output.toString().replace("\n", "\n    "));
                            nativeMethods.append(context.nativeMethods);
                            if ((classNode.access & Opcodes.ACC_INTERFACE) > 0) {
                                method.access &= ~Opcodes.ACC_NATIVE;
                            }
                        }

                        invokeDynamics.forEach((key, value) -> InvokeDynamicHandler.processIndy(classNode, key, value));
                        methodHandleInvokes.forEach((key, value) -> MethodHandler.processMethodHandleInvoke(classNode, key, value));

                        classNode.version = 52;
                        ClassWriter classWriter = new SafeClassWriter(metadataReader,
                                Opcodes.ASM5 | ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
                        classNode.accept(classWriter);
                        Util.writeEntry(out, entry.getName(), classWriter.toByteArray());

                        cppBuilder.addHeader(cachedStrings.size(), cachedClasses.size(), cachedMethods.size(), cachedFields.size());
                        cppBuilder.addInstructions(instructions.toString());
                        cppBuilder.registerMethods(cachedStrings, cachedClasses, nativeMethods.toString(), staticClassProvider);

                        mainSourceBuilder.addHeader(cppBuilder.getHppFilename());
                        mainSourceBuilder.registerClassMethods(currentClassId, cppBuilder.getFilename());
                    }

                    currentClassId++;
                } catch (IOException ex) {
                }
            });

            for (ClassNode ifaceStaticClass : staticClassProvider.getReadyClasses()) {
                ClassWriter classWriter = new SafeClassWriter(metadataReader, Opcodes.ASM5 | ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
                ifaceStaticClass.accept(classWriter);
                Util.writeEntry(out, ifaceStaticClass.name + ".class", classWriter.toByteArray());
            }

            Manifest mf = jar.getManifest();
            ClassNode loaderClass = new ClassNode();
            loaderClass.sourceFile = "Arm_Dex2c";
            loaderClass.name = "arm/Loader";
            loaderClass.version = 52;
            loaderClass.superName = "java/lang/Object";
            loaderClass.access = Opcodes.ACC_PUBLIC;
            MethodNode registerNativesForClassMethod = new MethodNode(Opcodes.ASM5, Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_NATIVE, "registerNativesForClass", "(I)V", null, new String[0]);
            loaderClass.methods.add(registerNativesForClassMethod);
            MethodNode mainMethod = new MethodNode(Opcodes.ASM5, Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
            mainMethod.instructions.add(new LdcInsnNode("arm"));
            mainMethod.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/System", "loadLibrary",
                    "(Ljava/lang/String;)V"));
            mainMethod.instructions.add(new InsnNode(Opcodes.RETURN));
            loaderClass.methods.add(mainMethod);
            ClassWriter classWriter = new SafeClassWriter(metadataReader, Opcodes.ASM5 | ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
            loaderClass.accept(classWriter);
            Util.writeEntry(out, "arm/Loader.class", classWriter.toByteArray());
            out.closeEntry();
            metadataReader.close();
        }
        Files.write(cppDir.resolve("string_pool.cpp"), stringPool.build().getBytes(StandardCharsets.UTF_8));
        Files.write(cppDir.resolve("native_jvm_output.cpp"), mainSourceBuilder.build("arm", currentClassId)
                .getBytes(StandardCharsets.UTF_8));
    }

    public Snippets getSnippets() {
        return snippets;
    }

    public StringPool getStringPool() {
        return stringPool;
    }

    public InterfaceStaticClassProvider getStaticClassProvider() {
        return staticClassProvider;
    }

    public NodeCache<String> getCachedStrings() {
        return cachedStrings;
    }

    public NodeCache<String> getCachedClasses() {
        return cachedClasses;
    }

    public NodeCache<CachedMethodInfo> getCachedMethods() {
        return cachedMethods;
    }

    public NodeCache<CachedFieldInfo> getCachedFields() {
        return cachedFields;
    }

    public String getNativeDir() {
        return "arm";
    }

    public Map<String, InvokeDynamicInsnNode> getInvokeDynamics() {
        return invokeDynamics;
    }

    public Map<String, MethodInsnNode> getMethodHandleInvokes() {
        return methodHandleInvokes;
    }
}
