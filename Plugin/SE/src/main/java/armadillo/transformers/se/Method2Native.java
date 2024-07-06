package armadillo.transformers.se;

import armadillo.Constant;
import armadillo.common.SimpleNameFactory;
import armadillo.model.obfuscators.CallMethod;
import armadillo.model.obfuscators.CallReference;
import armadillo.transformers.base.BaseTransformer;
import armadillo.transformers.base.OtherTransformer;
import armadillo.utils.*;
import by.radioegor146.Util;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.jf.dexlib2.AccessFlags;
import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.builder.MutableMethodImplementation;
import org.jf.dexlib2.builder.instruction.*;
import org.jf.dexlib2.dexbacked.DexBackedClassDef;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.dexbacked.DexBackedMethod;
import org.jf.dexlib2.iface.*;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.formats.*;
import org.jf.dexlib2.iface.reference.*;
import org.jf.dexlib2.immutable.ImmutableClassDef;
import org.jf.dexlib2.immutable.ImmutableMethod;
import org.jf.dexlib2.immutable.ImmutableMethodImplementation;
import org.jf.dexlib2.immutable.ImmutableMethodParameter;
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction10x;
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction21c;
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction35c;
import org.jf.dexlib2.immutable.reference.ImmutableMethodProtoReference;
import org.jf.dexlib2.immutable.reference.ImmutableMethodReference;
import org.jf.dexlib2.immutable.reference.ImmutableStringReference;
import org.jf.dexlib2.immutable.reference.ImmutableTypeReference;
import org.jf.dexlib2.rewriter.DexRewriter;
import org.jf.dexlib2.rewriter.Rewriter;
import org.jf.dexlib2.rewriter.RewriterModule;
import org.jf.dexlib2.rewriter.Rewriters;
import org.jf.dexlib2.writer.io.MemoryDataStore;
import org.jf.dexlib2.writer.pool.DexPool;

import javax.annotation.Nonnull;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Method2Native extends OtherTransformer {
    private final HashSet<String> obf = new HashSet<>();
    private final SimpleNameFactory nameFactory = new SimpleNameFactory();
    private final int method_max_size = SysConfigUtil.getIntConfig("se.method.max.size");
    private final int instruction_max_size = SysConfigUtil.getIntConfig("se.instruction.max.size");
    private final String[] api = {"armeabi", "armeabi-v7a", "arm64-v8a", "x86"};
    private List<CallMethod> callMethods;
    private StringPool stringPool;

    protected boolean is_Ollvm() {
        return false;
    }

    @Override
    public void transform() throws Exception {
        if (configuration != null) {
            JsonArray separate = new JsonParser().parse(configuration).getAsJsonObject().getAsJsonArray(Long.toString(is_Ollvm() ? 1073741824 : 536870912));
            if (separate == null) return;
            for (JsonElement jsonElement : separate)
                obf.add(jsonElement.getAsString());
            if (obf.size() <= 0) return;
            Enumeration<? extends ZipEntry> entries = getZipFile().entries();
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                if (zipEntry.getName().startsWith("classes") && zipEntry.getName().endsWith(".dex")) {
                    try {
                        callMethods = new ArrayList<>();
                        stringPool = new StringPool();
                        List<ClassDef> classDefs = new ArrayList<>();
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
                                classDefs.add(classDef);
                            } else
                                old_dexPool.internClass(classDef);
                        }
                        if (classDefs.size() == 0)
                            continue;
                        MemoryDataStore dataStore = new MemoryDataStore();
                        old_dexPool.writeTo(dataStore);
                        getReplacerRes().put(zipEntry.getName(), Arrays.copyOf(dataStore.getBuffer(), dataStore.getSize()));
                        dataStore.close();
                        String type = String.format("Larm/%s;", nameFactory.nextName());
                        String nativeName = String.format("arm_%s", zipEntry.getName().replace(".dex", ""));
                        String methodName = nameFactory.nextName();
                        ClassDefRewriter rewriter = new ClassDefRewriter(type, methodName, callMethods);
                        DexRewriter dexRewriter = new DexRewriter(rewriter);
                        DexFile fix_dex = dexRewriter.getDexFileRewriter().rewrite(new DexFile() {
                            @Nonnull
                            @Override
                            public Set<? extends ClassDef> getClasses() {
                                return new AbstractSet<ClassDef>() {
                                    @Override
                                    public Iterator<ClassDef> iterator() {
                                        return classDefs.iterator();
                                    }

                                    @Override
                                    public int size() {
                                        return classDefs.size();
                                    }
                                };
                            }

                            @Nonnull
                            @Override
                            public Opcodes getOpcodes() {
                                return Opcodes.getDefault();
                            }
                        });
                        DexPool dexPool = new DexPool(Opcodes.getDefault());
                        for (ClassDef def : fix_dex.getClasses())
                            dexPool.internClass(def);
                        dexPool.internClass(getCallClassDef(type, methodName, nativeName));
                        dataStore = new MemoryDataStore();
                        dexPool.writeTo(dataStore);
                        getAdd_Classdex().add(Arrays.copyOf(dataStore.getBuffer(), dataStore.getSize()));
                        dataStore.close();
                        String dirName = String.format("SE_cache_%s", UUID.randomUUID().toString());
                        File cacheJni = new File(new File(Constant.getCache(), dirName), "jni");
                        File android_mk = new File(cacheJni, "Android.mk");
                        File application_mk = new File(cacheJni, "Application.mk");
                        if (!cacheJni.exists())
                            cacheJni.mkdirs();
                        /**
                         * 写CPP
                         */
                        {
                            StringBuilder cpp = new StringBuilder();
                            StringBuilder source = new StringBuilder();
                            StringBuilder method_table = new StringBuilder();
                            cpp.append("#include <jni.h>\n")
                                    .append("#include \"string_pool.hpp\"\n")
                                    .append("#include  <android/log.h>\n")
                                    .append("\n")
                                    .append("#define JNIREG_CLASS \"" + type.substring(1, type.length() - 1) + "\"\n")
                                    .append("#define NELEM(x) ((int)(sizeof(x) / sizeof((x)[0])))\n")
                                    .append("#define TAG \"ARM\"\n")
                                    .append("#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)\n")
                                    .append("#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)\n")
                                    .append("\n")
                                    .append("using namespace std;\n")
                                    .append("\n")
                                    .append("namespace arm {\n")
                                    .append("\n")
                                    .append("jclass clazz_cache[" + (rewriter.getIndex() + 10) + "] = {};\n")
                                    .append("jmethodID mid_cache[" + (rewriter.getIndex() + 10) + "] = {};\n")
                                    .append("jfieldID fid_cache[" + (rewriter.getIndex() + 10) + "] = {};\n")
                                    .append("signed char *string_pool;\n")
                                    .append("\n");
                            method_table.append("static JNINativeMethod method_table[] = {\n");
                            for (CallMethod method : callMethods) {
                                {
                                    method_table.append("        {\"" + methodName + "\",\"(");
                                    for (String Parametertype : method.getParameterTypes())
                                        method_table.append(Parametertype);
                                    method_table.append(")")
                                            .append(method.getReturnType())
                                            .append("\", (void *) ")
                                            .append(method.getName())
                                            .append("},\n");
                                }
                                source.append(getCppType(method.getReturnType())).append(" JNICALL ").append(method.getName()).append(" (JNIEnv *env, jclass clazz, jint index");
                                if (method.getParameterTypes().size() > 1) {
                                    for (int i = 1; i < method.getParameterTypes().size(); i++) {
                                        String s = method.getParameterTypes().get(i);
                                        if (i == 1)
                                            source.append(", ");
                                        if (i == method.getParameterTypes().size() - 1)
                                            source.append(getCppType(s))
                                                    .append(" obj")
                                                    .append(i - 1)
                                                    .append(") {\n");
                                        else
                                            source.append(getCppType(s))
                                                    .append(" obj")
                                                    .append(i - 1)
                                                    .append(", ");
                                    }
                                } else
                                    source.append(") {\n");
                                source.append("  switch (index) {\n");
                                for (Map.Entry<Integer, CallReference> entry : method.getReferences().entrySet()) {
                                    source.append("    case " + entry.getKey() + ":\n    {\n");
                                    switch (entry.getValue().getOpcode()) {
                                        case INVOKE_STATIC: {
                                            MethodReference methodReference = (MethodReference) entry.getValue().getReference();
                                            StringBuilder parameters = new StringBuilder();
                                            parameters.append("(");
                                            for (CharSequence parameterType : methodReference.getParameterTypes())
                                                parameters.append(parameterType.toString());
                                            parameters.append(")" + methodReference.getReturnType());
                                            source.append("      if (env->IsSameObject(clazz_cache[index],NULL)) {\n")
                                                    .append("          if (jclass cls = env->FindClass(" + stringPool.get(dexToNativeName(methodReference.getDefiningClass())) + ")) {\n")
                                                    .append("              clazz_cache[index] = (jclass) env->NewGlobalRef(cls);\n")
                                                    .append("              env->DeleteLocalRef(cls);\n")
                                                    .append("          }\n")
                                                    .append("      }\n")
                                                    .append("      if (mid_cache[index] == NULL) {\n")
                                                    .append("          if (jmethodID mid = env->GetStaticMethodID(clazz_cache[index], " + stringPool.get(methodReference.getName()) + ", " + stringPool.get(parameters.toString()) + ")) {\n")
                                                    .append("              mid_cache[index] = mid;\n")
                                                    .append("          }\n")
                                                    .append("      }\n");
                                            setStaticNative(source, method.getReturnType(), method.getParameterTypes());
                                            source.append("    }\n");
                                        }
                                        break;
                                        case INVOKE_VIRTUAL: {
                                            MethodReference methodReference = (MethodReference) entry.getValue().getReference();
                                            StringBuilder parameters = new StringBuilder();
                                            parameters.append("(");
                                            for (CharSequence parameterType : methodReference.getParameterTypes())
                                                parameters.append(parameterType.toString());
                                            parameters.append(")" + methodReference.getReturnType());
                                            source.append("      env->MonitorEnter(obj0);\n");
                                            source.append("      if (env->IsSameObject(clazz_cache[index],NULL)) {\n")
                                                    .append("          if (jclass cls = env->FindClass(" + stringPool.get(dexToNativeName(methodReference.getDefiningClass())) + ")) {\n")
                                                    .append("              clazz_cache[index] = (jclass) env->NewGlobalRef(cls);\n")
                                                    .append("              env->DeleteLocalRef(cls);\n")
                                                    .append("          }\n")
                                                    .append("      }\n")
                                                    .append("      if (mid_cache[index] == NULL) {\n")
                                                    .append("          if (jmethodID mid = env->GetMethodID(clazz_cache[index], " + stringPool.get(methodReference.getName()) + ", " + stringPool.get(parameters.toString()) + ")) {\n")
                                                    .append("              mid_cache[index] = mid;\n")
                                                    .append("          }\n")
                                                    .append("      }\n");
                                            setvirtualNative(source, method.getReturnType(), method.getParameterTypes());
                                            source.append("    }\n");
                                        }
                                        break;
                                        case INVOKE_SUPER: {
                                            MethodReference methodReference = (MethodReference) entry.getValue().getReference();
                                            StringBuilder parameters = new StringBuilder();
                                            parameters.append("(");
                                            for (CharSequence parameterType : methodReference.getParameterTypes())
                                                parameters.append(parameterType.toString());
                                            parameters.append(")" + methodReference.getReturnType());
                                            source.append("      env->MonitorEnter(obj0);\n");
                                            source.append("      if (env->IsSameObject(clazz_cache[index],NULL)) {\n")
                                                    .append("          if (jclass cls = env->FindClass(" + stringPool.get(dexToNativeName(methodReference.getDefiningClass())) + ")) {\n")
                                                    .append("              clazz_cache[index] = (jclass) env->NewGlobalRef(cls);\n")
                                                    .append("              env->DeleteLocalRef(cls);\n")
                                                    .append("          }\n")
                                                    .append("      }\n")
                                                    .append("      if (mid_cache[index] == NULL) {\n")
                                                    .append("          if (jmethodID mid = env->GetMethodID(clazz_cache[index], " + stringPool.get(methodReference.getName()) + ", " + stringPool.get(parameters.toString()) + ")) {\n")
                                                    .append("              mid_cache[index] = mid;\n")
                                                    .append("          }\n")
                                                    .append("      }\n");
                                            setsuperNative(source, method.getReturnType(), method.getParameterTypes());
                                            source.append("    }\n");
                                        }
                                        break;
                                        case INVOKE_DIRECT_EMPTY: {
                                            MethodReference methodReference = (MethodReference) entry.getValue().getReference();
                                            StringBuilder parameters = new StringBuilder();
                                            parameters.append("(");
                                            for (CharSequence parameterType : methodReference.getParameterTypes())
                                                parameters.append(parameterType.toString());
                                            parameters.append(")" + methodReference.getReturnType());
                                            source.append("      if (env->IsSameObject(clazz_cache[index],NULL)) {\n")
                                                    .append("          if (jclass cls = env->FindClass(" + stringPool.get(dexToNativeName(methodReference.getDefiningClass())) + ")) {\n")
                                                    .append("              clazz_cache[index] = (jclass) env->NewGlobalRef(cls);\n")
                                                    .append("              env->DeleteLocalRef(cls);\n")
                                                    .append("          }\n")
                                                    .append("      }\n")
                                                    .append("      if (mid_cache[index] == NULL) {\n")
                                                    .append("          if (jmethodID mid = env->GetMethodID(clazz_cache[index], " + stringPool.get(methodReference.getName()) + ", " + stringPool.get(parameters.toString()) + ")) {\n")
                                                    .append("              mid_cache[index] = mid;\n")
                                                    .append("          }\n")
                                                    .append("      }\n");
                                            NewObjectNative(source, method.getReturnType(), method.getParameterTypes());
                                            source.append("    }\n");
                                        }
                                        break;
                                        case INVOKE_DIRECT: {
                                            MethodReference methodReference = (MethodReference) entry.getValue().getReference();
                                            StringBuilder parameters = new StringBuilder();
                                            parameters.append("(");
                                            for (CharSequence parameterType : methodReference.getParameterTypes())
                                                parameters.append(parameterType.toString());
                                            parameters.append(")" + methodReference.getReturnType());
                                            source.append("      if (env->IsSameObject(clazz_cache[index],NULL)) {\n")
                                                    .append("          if (jclass cls = env->FindClass(" + stringPool.get(dexToNativeName(methodReference.getDefiningClass())) + ")) {\n")
                                                    .append("              clazz_cache[index] = (jclass) env->NewGlobalRef(cls);\n")
                                                    .append("              env->DeleteLocalRef(cls);\n")
                                                    .append("          }\n")
                                                    .append("      }\n")
                                                    .append("      if (mid_cache[index] == NULL) {\n")
                                                    .append("          if (jmethodID mid = env->GetMethodID(clazz_cache[index], " + stringPool.get(methodReference.getName()) + ", " + stringPool.get(parameters.toString()) + ")) {\n")
                                                    .append("              mid_cache[index] = mid;\n")
                                                    .append("          }\n")
                                                    .append("      }\n");
                                            setNewObjectNative(source, method.getReturnType(), method.getParameterTypes());
                                            source.append("    }\n");
                                        }
                                        break;
                                        case CONST_STRING_JUMBO:
                                        case CONST_STRING: {
                                            StringReference stringReference = (StringReference) entry.getValue().getReference();
                                            source.append("      return env->NewStringUTF(" + stringPool.get(stringReference.getString()) + ");\n    }\n");
                                        }
                                        break;
                                        case CONST_HIGH16:
                                        case CONST_4:
                                        case CONST_16:
                                        case CONST_WIDE_16:
                                        case CONST_WIDE_32:
                                        case CONST_WIDE:
                                        case CONST_WIDE_HIGH16:
                                        case CONST: {
                                            StringReference stringReference = (StringReference) entry.getValue().getReference();
                                            source.append("      return " + stringReference.getString() + ";\n    }\n");
                                        }
                                        break;
                                        case NEW_INSTANCE: {
                                            TypeReference typeReference = (TypeReference) entry.getValue().getReference();
                                            source.append("      if (env->IsSameObject(clazz_cache[index],NULL)) {\n")
                                                    .append("          if (jclass cls = env->FindClass(" + stringPool.get(dexToNativeName(typeReference.getType())) + ")) {\n")
                                                    .append("              clazz_cache[index] = (jclass) env->NewGlobalRef(cls);\n")
                                                    .append("              env->DeleteLocalRef(cls);\n")
                                                    .append("          }\n")
                                                    .append("      }\n")
                                                    .append("      return env->AllocObject(clazz_cache[index]);\n")
                                                    //.append(typeReference.getType().equals("Ljava/lang/String;") ? "      return (jstring)env->AllocObject(clazz_cache[index]);\n" : "      return env->AllocObject(clazz_cache[index]);\n")
                                                    .append("    }\n");
                                        }
                                        break;
                                        case FILLED_NEW_ARRAY:
                                        case FILLED_NEW_ARRAY_RANGE: {
                                            TypeReference typeReference = (TypeReference) entry.getValue().getReference();
                                            if (typeReference.getType().startsWith("[") && typeReference.getType().contains("L") && typeReference.getType().endsWith(";")) {
                                                source.append("      if (env->IsSameObject(clazz_cache[index],NULL)) {\n")
                                                        .append("          if (jclass cls = env->FindClass(" + stringPool.get(ArrayToNativeName(typeReference.getType())) + ")) {\n")
                                                        .append("              clazz_cache[index] = (jclass) env->NewGlobalRef(cls);\n")
                                                        .append("              env->DeleteLocalRef(cls);\n")
                                                        .append("          }\n")
                                                        .append("      }\n");
                                            } else if (typeReference.getType().startsWith("[[")) {
                                                source.append("      if (env->IsSameObject(clazz_cache[index],NULL)) {\n")
                                                        .append("          if (jclass cls = env->FindClass(" + stringPool.get(ArrayToNativeName(typeReference.getType())) + ")) {\n")
                                                        .append("              clazz_cache[index] = (jclass) env->NewGlobalRef(cls);\n")
                                                        .append("              env->DeleteLocalRef(cls);\n")
                                                        .append("          }\n")
                                                        .append("      }\n");
                                            }
                                            NewFilledArray(source, method.getReturnType(), method.getParameterTypes().size() - 1);
                                            source.append("    }\n");
                                        }
                                        break;
                                        case NEW_ARRAY: {
                                            TypeReference typeReference = (TypeReference) entry.getValue().getReference();
                                            if (typeReference.getType().startsWith("[") && typeReference.getType().contains("L") && typeReference.getType().endsWith(";")) {
                                                source.append("      if (env->IsSameObject(clazz_cache[index],NULL)) {\n")
                                                        .append("          if (jclass cls = env->FindClass(" + stringPool.get(ArrayToNativeName(typeReference.getType())) + ")) {\n")
                                                        .append("              clazz_cache[index] = (jclass) env->NewGlobalRef(cls);\n")
                                                        .append("              env->DeleteLocalRef(cls);\n")
                                                        .append("          }\n")
                                                        .append("      }\n");
                                            } else if (typeReference.getType().startsWith("[[")) {
                                                source.append("      if (env->IsSameObject(clazz_cache[index],NULL)) {\n")
                                                        .append("          if (jclass cls = env->FindClass(" + stringPool.get(ArrayToNativeName(typeReference.getType())) + ")) {\n")
                                                        .append("              clazz_cache[index] = (jclass) env->NewGlobalRef(cls);\n")
                                                        .append("              env->DeleteLocalRef(cls);\n")
                                                        .append("          }\n")
                                                        .append("      }\n");
                                            }
                                            NewArray(source, method.getReturnType());
                                            source.append("    }\n");
                                        }
                                        break;
                                        case IGET: {
                                            FieldReference fieldReference = (FieldReference) entry.getValue().getReference();
                                            source.append("      env->MonitorEnter(obj0);\n");
                                            source.append("      if (env->IsSameObject(clazz_cache[index],NULL)) {\n")
                                                    .append("          if (jclass cls = env->FindClass(" + stringPool.get(dexToNativeName(fieldReference.getDefiningClass())) + ")) {\n")
                                                    .append("              clazz_cache[index] = (jclass) env->NewGlobalRef(cls);\n")
                                                    .append("              env->DeleteLocalRef(cls);\n")
                                                    .append("          }\n")
                                                    .append("      }\n")
                                                    .append("      if (fid_cache[index] == NULL) {\n")
                                                    .append("          if (jfieldID fid = env->GetFieldID(clazz_cache[index], " + stringPool.get(fieldReference.getName()) + ", " + stringPool.get(fieldReference.getType()) + ")) {\n")
                                                    .append("              fid_cache[index] = fid;\n")
                                                    .append("          }\n")
                                                    .append("      }\n")
                                                    .append("      " + (fieldReference.getType().equals("I") ? "jint" : "jfloat") + "  obj = env->Get" + (fieldReference.getType().equals("I") ? "Int" : "Float") + "Field(obj0,fid_cache[index]);\n")
                                                    .append("      env->MonitorExit(obj0);\n")
                                                    .append("      return obj;\n")
                                                    .append("    }\n");
                                        }
                                        break;
                                        case IGET_BYTE: {
                                            FieldReference fieldReference = (FieldReference) entry.getValue().getReference();
                                            source.append("      env->MonitorEnter(obj0);\n");
                                            source.append("      if (env->IsSameObject(clazz_cache[index],NULL)) {\n")
                                                    .append("          if (jclass cls = env->FindClass(" + stringPool.get(dexToNativeName(fieldReference.getDefiningClass())) + ")) {\n")
                                                    .append("              clazz_cache[index] = (jclass) env->NewGlobalRef(cls);\n")
                                                    .append("              env->DeleteLocalRef(cls);\n")
                                                    .append("          }\n")
                                                    .append("      }\n")
                                                    .append("      if (fid_cache[index] == NULL) {\n")
                                                    .append("          if (jfieldID fid = env->GetFieldID(clazz_cache[index], " + stringPool.get(fieldReference.getName()) + ", " + stringPool.get(fieldReference.getType()) + ")) {\n")
                                                    .append("              fid_cache[index] = fid;\n")
                                                    .append("          }\n")
                                                    .append("      }\n")
                                                    .append("      jbyte obj = env->GetByteField(obj0,fid_cache[index]);\n")
                                                    .append("      env->MonitorExit(obj0);\n")
                                                    .append("      return obj;\n")
                                                    .append("    }\n");
                                        }
                                        break;
                                        case IGET_CHAR: {
                                            FieldReference fieldReference = (FieldReference) entry.getValue().getReference();
                                            source.append("      env->MonitorEnter(obj0);\n");
                                            source.append("      if (env->IsSameObject(clazz_cache[index],NULL)) {\n")
                                                    .append("          if (jclass cls = env->FindClass(" + stringPool.get(dexToNativeName(fieldReference.getDefiningClass())) + ")) {\n")
                                                    .append("              clazz_cache[index] = (jclass) env->NewGlobalRef(cls);\n")
                                                    .append("              env->DeleteLocalRef(cls);\n")
                                                    .append("          }\n")
                                                    .append("      }\n")
                                                    .append("      if (fid_cache[index] == NULL) {\n")
                                                    .append("          if (jfieldID fid = env->GetFieldID(clazz_cache[index], " + stringPool.get(fieldReference.getName()) + ", " + stringPool.get(fieldReference.getType()) + ")) {\n")
                                                    .append("              fid_cache[index] = fid;\n")
                                                    .append("          }\n")
                                                    .append("      }\n")
                                                    .append("      jchar obj = env->GetCharField(obj0,fid_cache[index]);\n")
                                                    .append("      env->MonitorExit(obj0);\n")
                                                    .append("      return obj;\n")
                                                    .append("    }\n");
                                        }
                                        break;
                                        case IGET_SHORT: {
                                            FieldReference fieldReference = (FieldReference) entry.getValue().getReference();
                                            source.append("      env->MonitorEnter(obj0);\n");
                                            source.append("      if (env->IsSameObject(clazz_cache[index],NULL)) {\n")
                                                    .append("          if (jclass cls = env->FindClass(" + stringPool.get(dexToNativeName(fieldReference.getDefiningClass())) + ")) {\n")
                                                    .append("              clazz_cache[index] = (jclass) env->NewGlobalRef(cls);\n")
                                                    .append("              env->DeleteLocalRef(cls);\n")
                                                    .append("          }\n")
                                                    .append("      }\n")
                                                    .append("      if (fid_cache[index] == NULL) {\n")
                                                    .append("          if (jfieldID fid = env->GetFieldID(clazz_cache[index], " + stringPool.get(fieldReference.getName()) + ", " + stringPool.get(fieldReference.getType()) + ")) {\n")
                                                    .append("              fid_cache[index] = fid;\n")
                                                    .append("          }\n")
                                                    .append("      }\n")
                                                    .append("      jshort obj = env->GetShortField(obj0,fid_cache[index]);\n")
                                                    .append("      env->MonitorExit(obj0);\n")
                                                    .append("      return obj;\n")
                                                    .append("    }\n");
                                        }
                                        break;
                                        case IGET_WIDE: {
                                            FieldReference fieldReference = (FieldReference) entry.getValue().getReference();
                                            source.append("      env->MonitorEnter(obj0);\n");
                                            source.append("      if (env->IsSameObject(clazz_cache[index],NULL)) {\n")
                                                    .append("          if (jclass cls = env->FindClass(" + stringPool.get(dexToNativeName(fieldReference.getDefiningClass())) + ")) {\n")
                                                    .append("              clazz_cache[index] = (jclass) env->NewGlobalRef(cls);\n")
                                                    .append("              env->DeleteLocalRef(cls);\n")
                                                    .append("          }\n")
                                                    .append("      }\n")
                                                    .append("      if (fid_cache[index] == NULL) {\n")
                                                    .append("          if (jfieldID fid = env->GetFieldID(clazz_cache[index], " + stringPool.get(fieldReference.getName()) + ", " + stringPool.get(fieldReference.getType()) + ")) {\n")
                                                    .append("              fid_cache[index] = fid;\n")
                                                    .append("          }\n")
                                                    .append("      }\n")
                                                    .append("      " + (fieldReference.getType().equals("J") ? "jlong" : "jdouble") + "  obj = env->Get" + (fieldReference.getType().equals("J") ? "Long" : "Double") + "Field(obj0,fid_cache[index]);\n")
                                                    .append("      env->MonitorExit(obj0);\n")
                                                    .append("      return obj;\n")
                                                    .append("    }\n");
                                        }
                                        break;
                                        case IGET_BOOLEAN: {
                                            FieldReference fieldReference = (FieldReference) entry.getValue().getReference();
                                            source.append("      env->MonitorEnter(obj0);\n");
                                            source.append("      if (env->IsSameObject(clazz_cache[index],NULL)) {\n")
                                                    .append("          if (jclass cls = env->FindClass(" + stringPool.get(dexToNativeName(fieldReference.getDefiningClass())) + ")) {\n")
                                                    .append("              clazz_cache[index] = (jclass) env->NewGlobalRef(cls);\n")
                                                    .append("              env->DeleteLocalRef(cls);\n")
                                                    .append("          }\n")
                                                    .append("      }\n")
                                                    .append("      if (fid_cache[index] == NULL) {\n")
                                                    .append("          if (jfieldID fid = env->GetFieldID(clazz_cache[index], " + stringPool.get(fieldReference.getName()) + ", " + stringPool.get(fieldReference.getType()) + ")) {\n")
                                                    .append("              fid_cache[index] = fid;\n")
                                                    .append("          }\n")
                                                    .append("      }\n")
                                                    .append("      jboolean obj = env->GetBooleanField(obj0,fid_cache[index]);\n")
                                                    .append("      env->MonitorExit(obj0);\n")
                                                    .append("      return obj;\n")
                                                    .append("    }\n");
                                        }
                                        break;
                                        case IGET_OBJECT: {
                                            FieldReference fieldReference = (FieldReference) entry.getValue().getReference();
                                            source.append("      env->MonitorEnter(obj0);\n");
                                            source.append("      if (env->IsSameObject(clazz_cache[index],NULL)) {\n")
                                                    .append("          if (jclass cls = env->FindClass(" + stringPool.get(dexToNativeName(fieldReference.getDefiningClass())) + ")) {\n")
                                                    .append("              clazz_cache[index] = (jclass) env->NewGlobalRef(cls);\n")
                                                    .append("              env->DeleteLocalRef(cls);\n")
                                                    .append("          }\n")
                                                    .append("      }\n")
                                                    .append("      if (fid_cache[index] == NULL) {\n")
                                                    .append("          if (jfieldID fid = env->GetFieldID(clazz_cache[index], " + stringPool.get(fieldReference.getName()) + ", " + stringPool.get(fieldReference.getType()) + ")) {\n")
                                                    .append("              fid_cache[index] = fid;\n")
                                                    .append("          }\n")
                                                    .append("      }\n")
                                                    .append("      jobject obj = env->GetObjectField(obj0, fid_cache[index]);\n")
                                                    .append("      env->MonitorExit(obj0);\n");
                                            setGetObjectResult(source, fieldReference);
                                            source.append("    }\n");
                                        }
                                        break;
                                        case IPUT: {
                                            FieldReference fieldReference = (FieldReference) entry.getValue().getReference();
                                            source.append("      env->MonitorEnter(obj0);\n");
                                            source.append("      if (env->IsSameObject(clazz_cache[index],NULL)) {\n")
                                                    .append("          if (jclass cls = env->FindClass(" + stringPool.get(dexToNativeName(fieldReference.getDefiningClass())) + ")) {\n")
                                                    .append("              clazz_cache[index] = (jclass) env->NewGlobalRef(cls);\n")
                                                    .append("              env->DeleteLocalRef(cls);\n")
                                                    .append("          }\n")
                                                    .append("      }\n")
                                                    .append("      if (fid_cache[index] == NULL) {\n")
                                                    .append("          if (jfieldID fid = env->GetFieldID(clazz_cache[index], " + stringPool.get(fieldReference.getName()) + ", " + stringPool.get(fieldReference.getType()) + ")) {\n")
                                                    .append("              fid_cache[index] = fid;\n")
                                                    .append("          }\n")
                                                    .append("      }\n")
                                                    .append("      env->Set" + (fieldReference.getType().equals("I") ? "Int" : "Float") + "Field(obj0,fid_cache[index],obj1);\n")
                                                    .append("      env->MonitorExit(obj0);\n")
                                                    .append("      break;\n")
                                                    .append("    }\n");
                                        }
                                        break;
                                        case IPUT_BYTE: {
                                            FieldReference fieldReference = (FieldReference) entry.getValue().getReference();
                                            source.append("      env->MonitorEnter(obj0);\n");
                                            source.append("      if (env->IsSameObject(clazz_cache[index],NULL)) {\n")
                                                    .append("          if (jclass cls = env->FindClass(" + stringPool.get(dexToNativeName(fieldReference.getDefiningClass())) + ")) {\n")
                                                    .append("              clazz_cache[index] = (jclass) env->NewGlobalRef(cls);\n")
                                                    .append("              env->DeleteLocalRef(cls);\n")
                                                    .append("          }\n")
                                                    .append("      }\n")
                                                    .append("      if (fid_cache[index] == NULL) {\n")
                                                    .append("          if (jfieldID fid = env->GetFieldID(clazz_cache[index], " + stringPool.get(fieldReference.getName()) + ", " + stringPool.get(fieldReference.getType()) + ")) {\n")
                                                    .append("              fid_cache[index] = fid;\n")
                                                    .append("          }\n")
                                                    .append("      }\n")
                                                    .append("      env->SetByteField(obj0,fid_cache[index],obj1);\n")
                                                    .append("      env->MonitorExit(obj0);\n")
                                                    .append("      break;\n")
                                                    .append("    }\n");
                                        }
                                        break;
                                        case IPUT_CHAR: {
                                            FieldReference fieldReference = (FieldReference) entry.getValue().getReference();
                                            source.append("      env->MonitorEnter(obj0);\n");
                                            source.append("      if (env->IsSameObject(clazz_cache[index],NULL)) {\n")
                                                    .append("          if (jclass cls = env->FindClass(" + stringPool.get(dexToNativeName(fieldReference.getDefiningClass())) + ")) {\n")
                                                    .append("              clazz_cache[index] = (jclass) env->NewGlobalRef(cls);\n")
                                                    .append("              env->DeleteLocalRef(cls);\n")
                                                    .append("          }\n")
                                                    .append("      }\n")
                                                    .append("      if (fid_cache[index] == NULL) {\n")
                                                    .append("          if (jfieldID fid = env->GetFieldID(clazz_cache[index], " + stringPool.get(fieldReference.getName()) + ", " + stringPool.get(fieldReference.getType()) + ")) {\n")
                                                    .append("              fid_cache[index] = fid;\n")
                                                    .append("          }\n")
                                                    .append("      }\n")
                                                    .append("      env->SetCharField(obj0,fid_cache[index],obj1);\n")
                                                    .append("      env->MonitorExit(obj0);\n")
                                                    .append("      break;\n")
                                                    .append("    }\n");
                                        }
                                        break;
                                        case IPUT_SHORT: {
                                            FieldReference fieldReference = (FieldReference) entry.getValue().getReference();
                                            source.append("      env->MonitorEnter(obj0);\n");
                                            source.append("      if (env->IsSameObject(clazz_cache[index],NULL)) {\n")
                                                    .append("          if (jclass cls = env->FindClass(" + stringPool.get(dexToNativeName(fieldReference.getDefiningClass())) + ")) {\n")
                                                    .append("              clazz_cache[index] = (jclass) env->NewGlobalRef(cls);\n")
                                                    .append("              env->DeleteLocalRef(cls);\n")
                                                    .append("          }\n")
                                                    .append("      }\n")
                                                    .append("      if (fid_cache[index] == NULL) {\n")
                                                    .append("          if (jfieldID fid = env->GetFieldID(clazz_cache[index], " + stringPool.get(fieldReference.getName()) + ", " + stringPool.get(fieldReference.getType()) + ")) {\n")
                                                    .append("              fid_cache[index] = fid;\n")
                                                    .append("          }\n")
                                                    .append("      }\n")
                                                    .append("      env->SetShortField(obj0,fid_cache[index],obj1);\n")
                                                    .append("      env->MonitorExit(obj0);\n")
                                                    .append("      break;\n")
                                                    .append("    }\n");
                                        }
                                        break;
                                        case IPUT_BOOLEAN: {
                                            FieldReference fieldReference = (FieldReference) entry.getValue().getReference();
                                            source.append("      env->MonitorEnter(obj0);\n");
                                            source.append("      if (env->IsSameObject(clazz_cache[index],NULL)) {\n")
                                                    .append("          if (jclass cls = env->FindClass(" + stringPool.get(dexToNativeName(fieldReference.getDefiningClass())) + ")) {\n")
                                                    .append("              clazz_cache[index] = (jclass) env->NewGlobalRef(cls);\n")
                                                    .append("              env->DeleteLocalRef(cls);\n")
                                                    .append("          }\n")
                                                    .append("      }\n")
                                                    .append("      if (fid_cache[index] == NULL) {\n")
                                                    .append("          if (jfieldID fid = env->GetFieldID(clazz_cache[index], " + stringPool.get(fieldReference.getName()) + ", " + stringPool.get(fieldReference.getType()) + ")) {\n")
                                                    .append("              fid_cache[index] = fid;\n")
                                                    .append("          }\n")
                                                    .append("      }\n")
                                                    .append("      env->SetBooleanField(obj0,fid_cache[index],obj1);\n")
                                                    .append("      env->MonitorExit(obj0);\n")
                                                    .append("      break;\n")
                                                    .append("    }\n");
                                        }
                                        break;
                                        case IPUT_OBJECT: {
                                            FieldReference fieldReference = (FieldReference) entry.getValue().getReference();
                                            source.append("      env->MonitorEnter(obj0);\n");
                                            source.append("      if (env->IsSameObject(clazz_cache[index],NULL)) {\n")
                                                    .append("          if (jclass cls = env->FindClass(" + stringPool.get(dexToNativeName(fieldReference.getDefiningClass())) + ")) {\n")
                                                    .append("              clazz_cache[index] = (jclass) env->NewGlobalRef(cls);\n")
                                                    .append("              env->DeleteLocalRef(cls);\n")
                                                    .append("          }\n")
                                                    .append("      }\n")
                                                    .append("      if (fid_cache[index] == NULL) {\n")
                                                    .append("          if (jfieldID fid = env->GetFieldID(clazz_cache[index], " + stringPool.get(fieldReference.getName()) + ", " + stringPool.get(fieldReference.getType()) + ")) {\n")
                                                    .append("              fid_cache[index] = fid;\n")
                                                    .append("          }\n")
                                                    .append("      }\n")
                                                    .append("      env->SetObjectField(obj0,fid_cache[index],obj1);\n")
                                                    .append("      env->MonitorExit(obj0);\n");
                                            source.append("      break;\n")
                                                    .append("    }\n");
                                        }
                                        break;
                                        case SGET: {
                                            FieldReference fieldReference = (FieldReference) entry.getValue().getReference();
                                            source.append("      if (env->IsSameObject(clazz_cache[index],NULL)) {\n")
                                                    .append("          if (jclass cls = env->FindClass(" + stringPool.get(dexToNativeName(fieldReference.getDefiningClass())) + ")) {\n")
                                                    .append("              clazz_cache[index] = (jclass) env->NewGlobalRef(cls);\n")
                                                    .append("              env->DeleteLocalRef(cls);\n")
                                                    .append("          }\n")
                                                    .append("      }\n")
                                                    .append("      if (fid_cache[index] == NULL) {\n")
                                                    .append("          if (jfieldID fid = env->GetStaticFieldID(clazz_cache[index], " + stringPool.get(fieldReference.getName()) + ", " + stringPool.get(fieldReference.getType()) + ")) {\n")
                                                    .append("              fid_cache[index] = fid;\n")
                                                    .append("          }\n")
                                                    .append("      }\n")
                                                    .append("      " + (fieldReference.getType().equals("I") ? "jint" : "jfloat") + " obj = env->GetStatic" + (fieldReference.getType().equals("I") ? "Int" : "Float") + "Field(clazz_cache[index],fid_cache[index]);\n")
                                                    .append("      return obj;\n")
                                                    .append("    }\n");
                                        }
                                        break;
                                        case SGET_BYTE: {
                                            FieldReference fieldReference = (FieldReference) entry.getValue().getReference();
                                            source.append("      if (env->IsSameObject(clazz_cache[index],NULL)) {\n")
                                                    .append("          if (jclass cls = env->FindClass(" + stringPool.get(dexToNativeName(fieldReference.getDefiningClass())) + ")) {\n")
                                                    .append("              clazz_cache[index] = (jclass) env->NewGlobalRef(cls);\n")
                                                    .append("              env->DeleteLocalRef(cls);\n")
                                                    .append("          }\n")
                                                    .append("      }\n")
                                                    .append("      if (fid_cache[index] == NULL) {\n")
                                                    .append("          if (jfieldID fid = env->GetStaticFieldID(clazz_cache[index], " + stringPool.get(fieldReference.getName()) + ", " + stringPool.get(fieldReference.getType()) + ")) {\n")
                                                    .append("              fid_cache[index] = fid;\n")
                                                    .append("          }\n")
                                                    .append("      }\n")
                                                    .append("      jbyte obj = env->GetStaticByteField(clazz_cache[index],fid_cache[index]);\n")
                                                    .append("      return obj;\n")
                                                    .append("    }\n");
                                        }
                                        break;
                                        case SGET_CHAR: {
                                            FieldReference fieldReference = (FieldReference) entry.getValue().getReference();
                                            source.append("      if (env->IsSameObject(clazz_cache[index],NULL)) {\n")
                                                    .append("          if (jclass cls = env->FindClass(" + stringPool.get(dexToNativeName(fieldReference.getDefiningClass())) + ")) {\n")
                                                    .append("              clazz_cache[index] = (jclass) env->NewGlobalRef(cls);\n")
                                                    .append("              env->DeleteLocalRef(cls);\n")
                                                    .append("          }\n")
                                                    .append("      }\n")
                                                    .append("      if (fid_cache[index] == NULL) {\n")
                                                    .append("          if (jfieldID fid = env->GetStaticFieldID(clazz_cache[index], " + stringPool.get(fieldReference.getName()) + ", " + stringPool.get(fieldReference.getType()) + ")) {\n")
                                                    .append("              fid_cache[index] = fid;\n")
                                                    .append("          }\n")
                                                    .append("      }\n")
                                                    .append("      jchar obj = env->GetStaticCharField(clazz_cache[index],fid_cache[index]);\n")
                                                    .append("      return obj;\n")
                                                    .append("    }\n");
                                        }
                                        break;
                                        case SGET_WIDE: {
                                            FieldReference fieldReference = (FieldReference) entry.getValue().getReference();
                                            source.append("      if (env->IsSameObject(clazz_cache[index],NULL)) {\n")
                                                    .append("          if (jclass cls = env->FindClass(" + stringPool.get(dexToNativeName(fieldReference.getDefiningClass())) + ")) {\n")
                                                    .append("              clazz_cache[index] = (jclass) env->NewGlobalRef(cls);\n")
                                                    .append("              env->DeleteLocalRef(cls);\n")
                                                    .append("          }\n")
                                                    .append("      }\n")
                                                    .append("      if (fid_cache[index] == NULL) {\n")
                                                    .append("          if (jfieldID fid = env->GetStaticFieldID(clazz_cache[index], " + stringPool.get(fieldReference.getName()) + ", " + stringPool.get(fieldReference.getType()) + ")) {\n")
                                                    .append("              fid_cache[index] = fid;\n")
                                                    .append("          }\n")
                                                    .append("      }\n")
                                                    .append("      " + (fieldReference.getType().equals("J") ? "jlong" : "jdouble") + " obj = env->GetStatic" + (fieldReference.getType().equals("J") ? "Long" : "Double") + "Field(clazz_cache[index],fid_cache[index]);\n")
                                                    .append("      return obj;\n")
                                                    .append("    }\n");
                                        }
                                        break;
                                        case SGET_SHORT: {
                                            FieldReference fieldReference = (FieldReference) entry.getValue().getReference();
                                            source.append("      if (env->IsSameObject(clazz_cache[index],NULL)) {\n")
                                                    .append("          if (jclass cls = env->FindClass(" + stringPool.get(dexToNativeName(fieldReference.getDefiningClass())) + ")) {\n")
                                                    .append("              clazz_cache[index] = (jclass) env->NewGlobalRef(cls);\n")
                                                    .append("              env->DeleteLocalRef(cls);\n")
                                                    .append("          }\n")
                                                    .append("      }\n")
                                                    .append("      if (fid_cache[index] == NULL) {\n")
                                                    .append("          if (jfieldID fid = env->GetStaticFieldID(clazz_cache[index], " + stringPool.get(fieldReference.getName()) + ", " + stringPool.get(fieldReference.getType()) + ")) {\n")
                                                    .append("              fid_cache[index] = fid;\n")
                                                    .append("          }\n")
                                                    .append("      }\n")
                                                    .append("      jshort obj = env->GetStaticShortField(clazz_cache[index],fid_cache[index]);\n")
                                                    .append("      return obj;\n")
                                                    .append("    }\n");
                                        }
                                        break;
                                        case SGET_BOOLEAN: {
                                            FieldReference fieldReference = (FieldReference) entry.getValue().getReference();
                                            source.append("      if (env->IsSameObject(clazz_cache[index],NULL)) {\n")
                                                    .append("          if (jclass cls = env->FindClass(" + stringPool.get(dexToNativeName(fieldReference.getDefiningClass())) + ")) {\n")
                                                    .append("              clazz_cache[index] = (jclass) env->NewGlobalRef(cls);\n")
                                                    .append("              env->DeleteLocalRef(cls);\n")
                                                    .append("          }\n")
                                                    .append("      }\n")
                                                    .append("      if (fid_cache[index] == NULL) {\n")
                                                    .append("          if (jfieldID fid = env->GetStaticFieldID(clazz_cache[index], " + stringPool.get(fieldReference.getName()) + ", " + stringPool.get(fieldReference.getType()) + ")) {\n")
                                                    .append("              fid_cache[index] = fid;\n")
                                                    .append("          }\n")
                                                    .append("      }\n")
                                                    .append("      jboolean obj = env->GetStaticBooleanField(clazz_cache[index],fid_cache[index]);\n")
                                                    .append("      return obj;\n")
                                                    .append("    }\n");
                                        }
                                        break;
                                        case SGET_OBJECT: {
                                            FieldReference fieldReference = (FieldReference) entry.getValue().getReference();
                                            source.append("      if (env->IsSameObject(clazz_cache[index],NULL)) {\n")
                                                    .append("          if (jclass cls = env->FindClass(" + stringPool.get(dexToNativeName(fieldReference.getDefiningClass())) + ")) {\n")
                                                    .append("              clazz_cache[index] = (jclass) env->NewGlobalRef(cls);\n")
                                                    .append("              env->DeleteLocalRef(cls);\n")
                                                    .append("          }\n")
                                                    .append("      }\n")
                                                    .append("      if (fid_cache[index] == NULL) {\n")
                                                    .append("          if (jfieldID fid = env->GetStaticFieldID(clazz_cache[index], " + stringPool.get(fieldReference.getName()) + ", " + stringPool.get(fieldReference.getType()) + ")) {\n")
                                                    .append("              fid_cache[index] = fid;\n")
                                                    .append("          }\n")
                                                    .append("      }\n")
                                                    .append("      jobject obj = env->GetStaticObjectField(clazz_cache[index],fid_cache[index]);\n");
                                            setGetObjectResult(source, fieldReference);
                                            source.append("    }\n");
                                        }
                                        break;
                                        case SPUT: {
                                            FieldReference fieldReference = (FieldReference) entry.getValue().getReference();
                                            source.append("      if (env->IsSameObject(clazz_cache[index],NULL)) {\n")
                                                    .append("          if (jclass cls = env->FindClass(" + stringPool.get(dexToNativeName(fieldReference.getDefiningClass())) + ")) {\n")
                                                    .append("              clazz_cache[index] = (jclass) env->NewGlobalRef(cls);\n")
                                                    .append("              env->DeleteLocalRef(cls);\n")
                                                    .append("          }\n")
                                                    .append("      }\n")
                                                    .append("      if (fid_cache[index] == NULL) {\n")
                                                    .append("          if (jfieldID fid = env->GetStaticFieldID(clazz_cache[index], " + stringPool.get(fieldReference.getName()) + ", " + stringPool.get(fieldReference.getType()) + ")) {\n")
                                                    .append("              fid_cache[index] = fid;\n")
                                                    .append("          }\n")
                                                    .append("      }\n")
                                                    .append("      env->SetStatic" + (fieldReference.getType().equals("I") ? "Int" : "Float") + "Field(clazz_cache[index],fid_cache[index],obj0);\n")
                                                    .append("      break;\n")
                                                    .append("    }\n");
                                        }
                                        break;
                                        case SPUT_BOOLEAN: {
                                            FieldReference fieldReference = (FieldReference) entry.getValue().getReference();
                                            source.append("      if (env->IsSameObject(clazz_cache[index],NULL)) {\n")
                                                    .append("          if (jclass cls = env->FindClass(" + stringPool.get(dexToNativeName(fieldReference.getDefiningClass())) + ")) {\n")
                                                    .append("              clazz_cache[index] = (jclass) env->NewGlobalRef(cls);\n")
                                                    .append("              env->DeleteLocalRef(cls);\n")
                                                    .append("          }\n")
                                                    .append("      }\n")
                                                    .append("      if (fid_cache[index] == NULL) {\n")
                                                    .append("          if (jfieldID fid = env->GetStaticFieldID(clazz_cache[index], " + stringPool.get(fieldReference.getName()) + ", " + stringPool.get(fieldReference.getType()) + ")) {\n")
                                                    .append("              fid_cache[index] = fid;\n")
                                                    .append("          }\n")
                                                    .append("      }\n")
                                                    .append("      env->SetStaticBooleanField(clazz_cache[index], fid_cache[index], obj0);\n")
                                                    .append("      break;\n")
                                                    .append("    }\n");
                                        }
                                        break;
                                        case SPUT_BYTE: {
                                            FieldReference fieldReference = (FieldReference) entry.getValue().getReference();
                                            source.append("      if (env->IsSameObject(clazz_cache[index],NULL)) {\n")
                                                    .append("          if (jclass cls = env->FindClass(" + stringPool.get(dexToNativeName(fieldReference.getDefiningClass())) + ")) {\n")
                                                    .append("              clazz_cache[index] = (jclass) env->NewGlobalRef(cls);\n")
                                                    .append("              env->DeleteLocalRef(cls);\n")
                                                    .append("          }\n")
                                                    .append("      }\n")
                                                    .append("      if (fid_cache[index] == NULL) {\n")
                                                    .append("          if (jfieldID fid = env->GetStaticFieldID(clazz_cache[index], " + stringPool.get(fieldReference.getName()) + ", " + stringPool.get(fieldReference.getType()) + ")) {\n")
                                                    .append("              fid_cache[index] = fid;\n")
                                                    .append("          }\n")
                                                    .append("      }\n")
                                                    .append("      env->SetStaticByteField(clazz_cache[index],fid_cache[index],obj0);\n")
                                                    .append("      break;\n")
                                                    .append("    }\n");
                                        }
                                        break;
                                        case SPUT_CHAR: {
                                            FieldReference fieldReference = (FieldReference) entry.getValue().getReference();
                                            source.append("      if (env->IsSameObject(clazz_cache[index],NULL)) {\n")
                                                    .append("          if (jclass cls = env->FindClass(" + stringPool.get(dexToNativeName(fieldReference.getDefiningClass())) + ")) {\n")
                                                    .append("              clazz_cache[index] = (jclass) env->NewGlobalRef(cls);\n")
                                                    .append("              env->DeleteLocalRef(cls);\n")
                                                    .append("          }\n")
                                                    .append("      }\n")
                                                    .append("      if (fid_cache[index] == NULL) {\n")
                                                    .append("          if (jfieldID fid = env->GetStaticFieldID(clazz_cache[index], " + stringPool.get(fieldReference.getName()) + ", " + stringPool.get(fieldReference.getType()) + ")) {\n")
                                                    .append("              fid_cache[index] = fid;\n")
                                                    .append("          }\n")
                                                    .append("      }\n")
                                                    .append("      env->SetStaticCharField(clazz_cache[index],fid_cache[index],obj0);\n")
                                                    .append("      break;\n")
                                                    .append("    }\n");
                                        }
                                        break;
                                        case SPUT_SHORT: {
                                            FieldReference fieldReference = (FieldReference) entry.getValue().getReference();
                                            source.append("      if (env->IsSameObject(clazz_cache[index],NULL)) {\n")
                                                    .append("          if (jclass cls = env->FindClass(" + stringPool.get(dexToNativeName(fieldReference.getDefiningClass())) + ")) {\n")
                                                    .append("              clazz_cache[index] = (jclass) env->NewGlobalRef(cls);\n")
                                                    .append("              env->DeleteLocalRef(cls);\n")
                                                    .append("          }\n")
                                                    .append("      }\n")
                                                    .append("      if (fid_cache[index] == NULL) {\n")
                                                    .append("          if (jfieldID fid = env->GetStaticFieldID(clazz_cache[index], " + stringPool.get(fieldReference.getName()) + ", " + stringPool.get(fieldReference.getType()) + ")) {\n")
                                                    .append("              fid_cache[index] = fid;\n")
                                                    .append("          }\n")
                                                    .append("      }\n")
                                                    .append("      env->SetStaticShortField(clazz_cache[index],fid_cache[index],obj0);\n")
                                                    .append("      break;\n")
                                                    .append("    }\n");
                                        }
                                        break;
                                        case SPUT_OBJECT: {
                                            FieldReference fieldReference = (FieldReference) entry.getValue().getReference();
                                            source.append("      if (env->IsSameObject(clazz_cache[index],NULL)) {\n")
                                                    .append("          if (jclass cls = env->FindClass(" + stringPool.get(dexToNativeName(fieldReference.getDefiningClass())) + ")) {\n")
                                                    .append("              clazz_cache[index] = (jclass) env->NewGlobalRef(cls);\n")
                                                    .append("              env->DeleteLocalRef(cls);\n")
                                                    .append("          }\n")
                                                    .append("      }\n")
                                                    .append("      if (fid_cache[index] == NULL) {\n")
                                                    .append("          if (jfieldID fid = env->GetStaticFieldID(clazz_cache[index], " + stringPool.get(fieldReference.getName()) + ", " + stringPool.get(fieldReference.getType()) + ")) {\n")
                                                    .append("              fid_cache[index] = fid;\n")
                                                    .append("          }\n")
                                                    .append("      }\n")
                                                    .append("      env->SetStaticObjectField(clazz_cache[index],fid_cache[index],obj0);\n")
                                                    .append("      break;\n")
                                                    .append("    }\n");
                                        }
                                        break;
                                        case APUT_BOOLEAN: {
                                            source.append("      jboolean *booleans = env->GetBooleanArrayElements(obj0, 0);\n")
                                                    .append("      int len = env->GetArrayLength(obj0);\n")
                                                    .append("      booleans[obj2] = obj1;\n")
                                                    .append("      env->SetBooleanArrayRegion(obj0, 0, len, booleans);\n")
                                                    .append("      break;\n")
                                                    .append("    }\n");
                                        }
                                        break;
                                        case APUT_SHORT: {
                                            source.append("      jshort *shorts = env->GetShortArrayElements(obj0, 0);\n")
                                                    .append("      int len = env->GetArrayLength(obj0);\n")
                                                    .append("      shorts[obj2] = obj1;\n")
                                                    .append("      env->SetShortArrayRegion(obj0, 0, len, shorts);\n")
                                                    .append("      break;\n")
                                                    .append("    }\n");
                                        }
                                        break;
                                        case APUT_CHAR: {
                                            source.append("      jchar *chars = env->GetCharArrayElements(obj0, 0);\n")
                                                    .append("      int len = env->GetArrayLength(obj0);\n")
                                                    .append("      chars[obj2] = obj1;\n")
                                                    .append("      env->SetCharArrayRegion(obj0, 0, len, chars);\n")
                                                    .append("      break;\n")
                                                    .append("    }\n");
                                        }
                                        break;
                                        case APUT_BYTE: {
                                            source.append("      jbyte *bytes = env->GetByteArrayElements(obj0, 0);\n")
                                                    .append("      int len = env->GetArrayLength(obj0);\n")
                                                    .append("      bytes[obj2] = obj1;\n")
                                                    .append("      env->SetByteArrayRegion(obj0, 0, len, bytes);\n")
                                                    .append("      break;\n")
                                                    .append("    }\n");
                                        }
                                        break;
                                        case APUT_OBJECT: {
                                            source.append("      env->SetObjectArrayElement(obj0, obj2, obj1);\n")
                                                    .append("      break;\n")
                                                    .append("    }\n");
                                        }
                                        break;
                                        case AGET_BOOLEAN: {
                                            source.append("      jboolean *booleans = env->GetBooleanArrayElements(obj0, 0);\n")
                                                    .append("      return booleans[obj1];\n")
                                                    .append("    }\n");
                                        }
                                        break;
                                        case AGET_SHORT: {
                                            source.append("      jshort *shorts = env->GetShortArrayElements(obj0, 0);\n")
                                                    .append("      return shorts[obj1];\n")
                                                    .append("    }\n");
                                        }
                                        break;
                                        case AGET_CHAR: {
                                            source.append("      jchar *chars = env->GetCharArrayElements(obj0, 0);\n")
                                                    .append("      return chars[obj1];\n")
                                                    .append("    }\n");
                                        }
                                        break;
                                        case AGET_BYTE: {
                                            source.append("      jbyte *bytes = env->GetByteArrayElements(obj0, 0);\n")
                                                    .append("      return bytes[obj1];\n")
                                                    .append("    }\n");
                                        }
                                        break;
                                        case AGET_OBJECT: {
                                            source.append("      return env->GetObjectArrayElement(obj0, obj1);\n")
                                                    .append("    }\n");
                                        }
                                        break;
                                        case ADD_INT_LIT8:
                                        case ADD_INT_LIT16:
                                            source.append("      return obj0 + " + ((StringReference) entry.getValue().getReference()).getString() + ";\n    }\n");
                                            break;
                                        case MUL_INT_LIT16:
                                        case MUL_INT_LIT8:
                                            source.append("      return obj0 * " + ((StringReference) entry.getValue().getReference()).getString() + ";\n    }\n");
                                            break;
                                        case XOR_INT_LIT8:
                                        case XOR_INT_LIT16:
                                            source.append("      return obj0 ^ " + ((StringReference) entry.getValue().getReference()).getString() + ";\n    }\n");
                                            break;
                                        case OR_INT_LIT8:
                                        case OR_INT_LIT16:
                                            source.append("      return obj0 | " + ((StringReference) entry.getValue().getReference()).getString() + ";\n    }\n");
                                            break;
                                        case AND_INT_LIT8:
                                        case AND_INT_LIT16:
                                            source.append("      return obj0 & " + ((StringReference) entry.getValue().getReference()).getString() + ";\n    }\n");
                                            break;
                                        case SHL_INT_LIT8:
                                            source.append("      return obj0 << (" + ((StringReference) entry.getValue().getReference()).getString() + " & 0x1f);\n    }\n");
                                            break;
                                        case SHR_INT_LIT8:
                                        case USHR_INT_LIT8:
                                            source.append("      return obj0 >> (" + ((StringReference) entry.getValue().getReference()).getString() + " & 0x1f);\n    }\n");
                                            break;
                                        case REM_INT_LIT16:
                                        case REM_INT_LIT8:
                                            source.append("      return obj0 % " + ((StringReference) entry.getValue().getReference()).getString() + ";\n    }\n");
                                            break;
                                        case DIV_INT_LIT8:
                                        case DIV_INT_LIT16:
                                            source.append("      return obj0 / " + ((StringReference) entry.getValue().getReference()).getString() + ";\n    }\n");
                                            break;
                                        case RSUB_INT:
                                        case RSUB_INT_LIT8:
                                            source.append("      return " + ((StringReference) entry.getValue().getReference()).getString() + " - obj0;\n    }\n");
                                            break;
                                        case ADD_INT_2ADDR:
                                        case ADD_INT:
                                            source.append("      return obj0 + obj1;\n    }\n");
                                            break;
                                        case SUB_INT_2ADDR:
                                        case SUB_INT:
                                            source.append("      return obj0 - obj1;\n    }\n");
                                            break;
                                        case MUL_INT_2ADDR:
                                        case MUL_INT:
                                            source.append("      return obj0 * obj1;\n    }\n");
                                            break;
                                        case DIV_INT_2ADDR:
                                        case DIV_INT:
                                            source.append("      return obj0 / obj1;\n    }\n");
                                            break;
                                        case REM_INT_2ADDR:
                                        case REM_INT:
                                            source.append("      return (int)obj0 % (int)obj1;\n    }\n");
                                            break;
                                        case AND_INT_2ADDR:
                                        case AND_INT:
                                            source.append("      return obj0 & obj1;\n    }\n");
                                            break;
                                        case OR_INT_2ADDR:
                                        case OR_INT:
                                            source.append("      return obj0 | obj1;\n    }\n");
                                            break;
                                        case XOR_INT_2ADDR:
                                        case XOR_INT:
                                            source.append("      return obj0 ^ obj1;\n    }\n");
                                            break;
                                        case SHL_INT_2ADDR:
                                        case SHL_INT:
                                            source.append("      return obj0 << (obj1 & 0x1f);\n    }\n");
                                            break;
                                        case SHR_INT_2ADDR:
                                        case USHR_INT_2ADDR:
                                        case USHR_INT:
                                        case SHR_INT:
                                            source.append("      return obj0 >> (obj1 & 0x1f);\n    }\n");
                                            break;
                                        case NEG_INT:
                                            source.append("      return -obj0;\n    }\n");
                                            break;
                                        case NOT_INT:
                                            source.append("      return ~obj0;\n    }\n");
                                            break;
                                    }
                                }
                                source.append("  }\n");
                                getCppResultType(source, method.getReturnType());
                                source.append("}\n");
                            }
                            method_table.append("};\n")
                                    .append("\n");
                            cpp.append(source.toString())
                                    .append("\n")
                                    .append(method_table.toString())
                                    .append("int register_ndk_load(JNIEnv *env) {\n")
                                    .append("    string_pool = native_jvm::string_pool::get_pool();\n")
                                    .append("    jclass clazz = env->FindClass(JNIREG_CLASS);\n")
                                    .append("    if (clazz == nullptr)\n")
                                    .append("       return JNI_FALSE;\n")
                                    .append("    if (env->RegisterNatives(clazz, method_table, NELEM(method_table)) < 0)\n")
                                    .append("       return JNI_FALSE;\n")
                                    .append("    return JNI_TRUE;\n")
                                    .append("}\n")
                                    .append("}\n")
                                    .append("\n")
                                    .append("extern \"C\"\n")
                                    .append("JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved) {\n")
                                    .append("    JNIEnv *env;\n")
                                    .append("    jint result = -1;\n")
                                    .append("    if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK)\n")
                                    .append("       return result;\n")
                                    .append("    if (!arm::register_ndk_load(env))\n")
                                    .append("       return result;\n")
                                    .append("    return JNI_VERSION_1_6;\n")
                                    .append("}");
                            try (FileOutputStream cpp_out = new FileOutputStream(new File(cacheJni, "test.cpp"));
                                 FileOutputStream string_pool_cpp_out = new FileOutputStream(new File(cacheJni, "string_pool.cpp"));
                                 FileOutputStream string_pool_hpp_out = new FileOutputStream(new File(cacheJni, "string_pool.hpp"))) {
                                cpp_out.write(cpp.toString().getBytes());
                                string_pool_cpp_out.write(stringPool.build().getBytes());
                                string_pool_hpp_out.write(Util.readResource("sources/string_pool.hpp").getBytes());
                            }
                        }
                        /**
                         * 写Android.mk
                         */
                        {
                            FileOutputStream android_mk_out = new FileOutputStream(android_mk);
                            String androidbuild = "LOCAL_PATH:= $(call my-dir)\n" +
                                    "\n" +
                                    "include $(CLEAR_VARS)\n" +
                                    "LOCAL_MODULE    := " + nativeName + "\n" +
                                    "LOCAL_CPPFLAGS += -std=c++17\n" +
                                    "LOCAL_LDLIBS:=-llog\n" +
                                    "LOCAL_SRC_FILES := test.cpp \\\n" +
                                    "                   string_pool.cpp \\\n" +
                                    "                   string_pool.hpp \n" +
                                    "include $(BUILD_SHARED_LIBRARY)";
                            android_mk_out.write(androidbuild.getBytes());
                            android_mk_out.close();
                        }
                        /**
                         * 写Application.mk
                         */
                        {
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
                                        "-j",
                                        "3"};
                            else
                                cmd = new String[]{"cmd.exe",
                                        "/c",
                                        "ndk-build",
                                        "-C",
                                        cacheJni.getAbsolutePath(),
                                        "-j",
                                        "" + Runtime.getRuntime().availableProcessors() / 2};
                            if (exec(cmd)) {
                                File cacheLibs = new File(cacheJni.getParentFile(), "libs");
                                if (cacheLibs.exists() && cacheLibs.isDirectory()) {
                                    WriteSo(cacheLibs);
                                } else
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
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw e;
                    }
                }
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

    public ClassDef getCallClassDef(String type, String methodName, String nativeName) {
        List<Method> methods = new ArrayList<>();
        /**
         * clinit
         */
        {
            methods.add(new ImmutableMethod(
                    type,
                    "<clinit>",
                    null,
                    "V",
                    AccessFlags.STATIC.getValue() | AccessFlags.CONSTRUCTOR.getValue(),
                    null,
                    null,
                    new ImmutableMethodImplementation(
                            1,
                            Lists.newArrayList(
                                    new ImmutableInstruction21c(Opcode.CONST_STRING
                                            , 0
                                            , new ImmutableStringReference(nativeName)),
                                    new ImmutableInstruction35c(Opcode.INVOKE_STATIC
                                            , 1
                                            , 0
                                            , 0
                                            , 0
                                            , 0
                                            , 0
                                            , new ImmutableMethodReference(
                                            "Ljava/lang/System;",
                                            "loadLibrary",
                                            Lists.newArrayList("Ljava/lang/String;"),
                                            "V")),
                                    new ImmutableInstruction10x(Opcode.RETURN_VOID)),
                            null,
                            null)));
        }
        /**
         * init
         */
        {
            methods.add(new ImmutableMethod(
                    type,
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
                                            "Ljava/lang/Object;",
                                            "<init>",
                                            null,
                                            "V")),
                                    new ImmutableInstruction10x(Opcode.RETURN_VOID)),
                            null,
                            null)));
        }
        /**
         * native方法
         */
        {
            for (CallMethod method : callMethods) {
                List<MethodParameter> parameters = new ArrayList<>();
                for (String parameterType : method.getParameterTypes())
                    parameters.add(new ImmutableMethodParameter(parameterType, null, null));
                methods.add(new ImmutableMethod(
                        type,
                        methodName,
                        parameters,
                        method.getReturnType(),
                        AccessFlags.PUBLIC.getValue() | AccessFlags.STATIC.getValue() | AccessFlags.NATIVE.getValue(),
                        null,
                        null,
                        null));
            }
        }
        return new ImmutableClassDef(
                type,
                AccessFlags.PUBLIC.getValue(),
                "Ljava/lang/Object;",
                null,
                null,
                null,
                null,
                methods);
    }

    private String dexToNativeName(String dexName) {
        if (dexName.charAt(0) == '[')
            return dexName;
        return dexName.substring(1, dexName.length() - 1);
    }

    private String ArrayToNativeName(String dexName) {
        String result = dexName.substring(1);
        if (result.startsWith("L"))
            result = result.substring(1, result.length() - 1);
        return result;
    }

    private String getCppType(String type) {
        if (type.startsWith("[")
                && type.contains("L")
                && type.endsWith(";"))
            return "jobjectArray";
        else if (type.startsWith("[["))
            return "jobjectArray";
        switch (type) {
            case "Ljava/lang/String;":
                return "jstring";
            case "Ljava/lang/Object;":
                return "jobject";
            case "I":
                return "jint";
            case "Z":
                return "jboolean";
            case "J":
                return "jlong";
            case "D":
                return "jdouble";
            case "B":
                return "jbyte";
            case "F":
                return "jfloat";
            case "S":
                return "jshort";
            case "C":
                return "jchar";
            case "[I":
                return "jintArray";
            case "[Z":
                return "jbooleanArray";
            case "[J":
                return "jlongArray";
            case "[D":
                return "jdoubleArray";
            case "[B":
                return "jbyteArray";
            case "[F":
                return "jfloatArray";
            case "[S":
                return "jshortArray";
            case "[C":
                return "jcharArray";
        }
        return "void";
    }

    private void getCppResultType(StringBuilder cpp, String type) {
        if (type.startsWith("[")
                && type.contains("L")
                && type.endsWith(";")) {
            cpp.append("  return nullptr;\n");
            return;
        } else if (type.startsWith("[") || type.startsWith("L")) {
            cpp.append("  return nullptr;\n");
            return;
        }
        switch (type) {
            case "I":
            case "J":
            case "D":
            case "F":
            case "B":
            case "C":
            case "S":
                cpp.append("  return 0;\n");
                break;
            case "Z":
                cpp.append("  return false;\n");
                break;
            case "V":
                cpp.append("\n");
                break;
        }
    }

    private void setStaticNative(StringBuilder cpp, String type, List<String> ParameterTypes) {
        if (type.startsWith("[")
                && type.contains("L")
                && type.endsWith(";")) {
            cpp.append("      jobject obj = env->CallStaticObjectMethod(clazz_cache[index], mid_cache[index]");
            for (int i = 1; i < ParameterTypes.size(); i++)
                cpp.append(", ").append("obj").append(i - 1);
            cpp.append(");\n      return (jobjectArray)(obj);\n");
            return;
        }
        switch (type) {
            case "V": {
                cpp.append("      env->CallStaticVoidMethod(clazz_cache[index], mid_cache[index]");
                for (int i = 1; i < ParameterTypes.size(); i++)
                    cpp.append(", ").append("obj").append(i - 1);
                cpp.append(");\n      break;\n");
            }
            break;
            case "Ljava/lang/String;": {
                cpp.append("      jobject obj = env->CallStaticObjectMethod(clazz_cache[index], mid_cache[index]");
                for (int i = 1; i < ParameterTypes.size(); i++)
                    cpp.append(", ").append("obj").append(i - 1);
                cpp.append(");\n      return (jstring)(obj);\n");
            }
            break;
            case "Ljava/lang/Object;": {
                cpp.append("      jobject obj = env->CallStaticObjectMethod(clazz_cache[index], mid_cache[index]");
                for (int i = 1; i < ParameterTypes.size(); i++)
                    cpp.append(", ").append("obj").append(i - 1);
                cpp.append(");\n      return obj;\n");
            }
            break;
            case "I": {
                cpp.append("      jint obj = env->CallStaticIntMethod(clazz_cache[index], mid_cache[index]");
                for (int i = 1; i < ParameterTypes.size(); i++)
                    cpp.append(", ").append("obj").append(i - 1);
                cpp.append(");\n      return obj;\n");
            }
            break;
            case "Z": {
                cpp.append("      jboolean obj = env->CallStaticBooleanMethod(clazz_cache[index], mid_cache[index]");
                for (int i = 1; i < ParameterTypes.size(); i++)
                    cpp.append(", ").append("obj").append(i - 1);
                cpp.append(");\n      return obj;\n");
            }
            break;
            case "J": {
                cpp.append("      jlong obj = env->CallStaticLongMethod(clazz_cache[index], mid_cache[index]");
                for (int i = 1; i < ParameterTypes.size(); i++)
                    cpp.append(", ").append("obj").append(i - 1);
                cpp.append(");\n      return obj;\n");
            }
            break;
            case "D": {
                cpp.append("      jdouble obj = env->CallStaticDoubleMethod(clazz_cache[index], mid_cache[index]");
                for (int i = 1; i < ParameterTypes.size(); i++)
                    cpp.append(", ").append("obj").append(i - 1);
                cpp.append(");\n      return obj;\n");
            }
            break;
            case "B": {
                cpp.append("      jbyte obj = env->CallStaticByteMethod(clazz_cache[index], mid_cache[index]");
                for (int i = 1; i < ParameterTypes.size(); i++)
                    cpp.append(", ").append("obj").append(i - 1);
                cpp.append(");\n      return obj;\n");
            }
            break;
            case "F": {
                cpp.append("      jfloat obj = env->CallStaticFloatMethod(clazz_cache[index], mid_cache[index]");
                for (int i = 1; i < ParameterTypes.size(); i++)
                    cpp.append(", ").append("obj").append(i - 1);
                cpp.append(");\n      return obj;\n");
            }
            break;
            case "S": {
                cpp.append("      jshort obj = env->CallStaticShortMethod(clazz_cache[index], mid_cache[index]");
                for (int i = 1; i < ParameterTypes.size(); i++)
                    cpp.append(", ").append("obj").append(i - 1);
                cpp.append(");\n      return obj;\n");
            }
            break;
            case "C": {
                cpp.append("      jchar obj = env->CallStaticCharMethod(clazz_cache[index], mid_cache[index]");
                for (int i = 1; i < ParameterTypes.size(); i++)
                    cpp.append(", ").append("obj").append(i - 1);
                cpp.append(");\n      return obj;\n");
            }
            break;
            case "[[I":
            case "[[Z":
            case "[[J":
            case "[[D":
            case "[[B":
            case "[[F":
            case "[[S":
            case "[[C": {
                cpp.append("      jobject obj = env->CallStaticObjectMethod(clazz_cache[index], mid_cache[index]");
                for (int i = 1; i < ParameterTypes.size(); i++)
                    cpp.append(", ").append("obj").append(i - 1);
                cpp.append(");\n      return (jobjectArray)(obj);\n");
            }
            break;
            case "[I": {
                cpp.append("      jobject obj = env->CallStaticObjectMethod(clazz_cache[index], mid_cache[index]");
                for (int i = 1; i < ParameterTypes.size(); i++)
                    cpp.append(", ").append("obj").append(i - 1);
                cpp.append(");\n      return (jintArray)(obj);\n");
            }
            break;
            case "[Z": {
                cpp.append("      jobject obj = env->CallStaticObjectMethod(clazz_cache[index], mid_cache[index]");
                for (int i = 1; i < ParameterTypes.size(); i++)
                    cpp.append(", ").append("obj").append(i - 1);
                cpp.append(");\n      return (jbooleanArray)(obj);\n");
            }
            break;
            case "[J": {
                cpp.append("      jobject obj = env->CallStaticObjectMethod(clazz_cache[index], mid_cache[index]");
                for (int i = 1; i < ParameterTypes.size(); i++)
                    cpp.append(", ").append("obj").append(i - 1);
                cpp.append(");\n      return (jlongArray)(obj);\n");
            }
            break;
            case "[D": {
                cpp.append("      jobject obj = env->CallStaticObjectMethod(clazz_cache[index], mid_cache[index]");
                for (int i = 1; i < ParameterTypes.size(); i++)
                    cpp.append(", ").append("obj").append(i - 1);
                cpp.append(");\n      return (jdoubleArray)(obj);\n");
            }
            break;
            case "[B": {
                cpp.append("      jobject obj = env->CallStaticObjectMethod(clazz_cache[index], mid_cache[index]");
                for (int i = 1; i < ParameterTypes.size(); i++)
                    cpp.append(", ").append("obj").append(i - 1);
                cpp.append(");\n      return (jbyteArray)(obj);\n");
            }
            break;
            case "[F": {
                cpp.append("      jobject obj = env->CallStaticObjectMethod(clazz_cache[index], mid_cache[index]");
                for (int i = 1; i < ParameterTypes.size(); i++)
                    cpp.append(", ").append("obj").append(i - 1);
                cpp.append(");\n      return (jfloatArray)(obj);\n");
            }
            break;
            case "[S": {
                cpp.append("      jobject obj = env->CallStaticObjectMethod(clazz_cache[index], mid_cache[index]");
                for (int i = 1; i < ParameterTypes.size(); i++)
                    cpp.append(", ").append("obj").append(i - 1);
                cpp.append(");\n      return (jshortArray)(obj);\n");
            }
            break;
            case "[C": {
                cpp.append("      jobject obj = env->CallStaticObjectMethod(clazz_cache[index], mid_cache[index]");
                for (int i = 1; i < ParameterTypes.size(); i++)
                    cpp.append(", ").append("obj").append(i - 1);
                cpp.append(");\n      return (jcharArray)(obj);\n");
            }
            break;
        }
    }

    private void setvirtualNative(StringBuilder cpp, String type, List<String> ParameterTypes) {
        if (type.startsWith("[")
                && type.contains("L")
                && type.endsWith(";")) {
            cpp.append("      jobject obj = env->CallObjectMethod(obj0, mid_cache[index]");
            for (int i = 2; i < ParameterTypes.size(); i++)
                cpp.append(", ").append("obj").append(i - 1);
            cpp.append(");\n      env->MonitorExit(obj0);\n      return (jobjectArray)(obj);\n");
            return;
        }
        switch (type) {
            case "V": {
                cpp.append("      env->CallVoidMethod(obj0, mid_cache[index]");
                for (int i = 2; i < ParameterTypes.size(); i++)
                    cpp.append(", ").append("obj").append(i - 1);
                cpp.append(");\n      env->MonitorExit(obj0);\n      break;\n");
            }
            break;
            case "Ljava/lang/String;": {
                cpp.append("      jobject obj = env->CallObjectMethod(obj0, mid_cache[index]");
                for (int i = 2; i < ParameterTypes.size(); i++)
                    cpp.append(", ").append("obj").append(i - 1);
                cpp.append(");\n      env->MonitorExit(obj0);\n      return (jstring)(obj);\n");
            }
            break;
            case "Ljava/lang/Object;": {
                cpp.append("      jobject obj = env->CallObjectMethod(obj0, mid_cache[index]");
                for (int i = 2; i < ParameterTypes.size(); i++)
                    cpp.append(", ").append("obj").append(i - 1);
                cpp.append(");\n");
                cpp.append("      env->MonitorExit(obj0);\n      return obj;\n");
            }
            break;
            case "I": {
                cpp.append("      jint obj = env->CallIntMethod(obj0, mid_cache[index]");
                for (int i = 2; i < ParameterTypes.size(); i++)
                    cpp.append(", ").append("obj").append(i - 1);
                cpp.append(");\n      env->MonitorExit(obj0);\n      return obj;\n");
            }
            break;
            case "Z": {
                cpp.append("      jboolean obj = env->CallBooleanMethod(obj0, mid_cache[index]");
                for (int i = 2; i < ParameterTypes.size(); i++)
                    cpp.append(", ").append("obj").append(i - 1);
                cpp.append(");\n      env->MonitorExit(obj0);\n      return obj;\n");
            }
            break;
            case "J": {
                cpp.append("      jlong obj = env->CallLongMethod(obj0, mid_cache[index]");
                for (int i = 2; i < ParameterTypes.size(); i++)
                    cpp.append(", ").append("obj").append(i - 1);
                cpp.append(");\n      env->MonitorExit(obj0);\n      return obj;\n");
            }
            break;
            case "D": {
                cpp.append("      jdouble obj = env->CallDoubleMethod(obj0, mid_cache[index]");
                for (int i = 2; i < ParameterTypes.size(); i++)
                    cpp.append(", ").append("obj").append(i - 1);
                cpp.append(");\n      env->MonitorExit(obj0);\n      return obj;\n");
            }
            break;
            case "B": {
                cpp.append("      jbyte obj = env->CallByteMethod(obj0, mid_cache[index]");
                for (int i = 2; i < ParameterTypes.size(); i++)
                    cpp.append(", ").append("obj").append(i - 1);
                cpp.append(");\n      env->MonitorExit(obj0);\n      return obj;\n");
            }
            break;
            case "F": {
                cpp.append("      jfloat obj = env->CallFloatMethod(obj0, mid_cache[index]");
                for (int i = 2; i < ParameterTypes.size(); i++)
                    cpp.append(", ").append("obj").append(i - 1);
                cpp.append(");\n      env->MonitorExit(obj0);\n      return obj;\n");
            }
            break;
            case "S": {
                cpp.append("      jshort obj = env->CallShortMethod(obj0, mid_cache[index]");
                for (int i = 2; i < ParameterTypes.size(); i++)
                    cpp.append(", ").append("obj").append(i - 1);
                cpp.append(");\n      env->MonitorExit(obj0);\n      return obj;\n");
            }
            break;
            case "C": {
                cpp.append("      jchar obj = env->CallCharMethod(obj0, mid_cache[index]");
                for (int i = 2; i < ParameterTypes.size(); i++)
                    cpp.append(", ").append("obj").append(i - 1);
                cpp.append(");\n      env->MonitorExit(obj0);\n      return obj;\n");
            }
            break;
            case "[[I":
            case "[[Z":
            case "[[J":
            case "[[D":
            case "[[B":
            case "[[F":
            case "[[S":
            case "[[C": {
                cpp.append("      jobject obj = env->CallObjectMethod(obj0, mid_cache[index]");
                for (int i = 2; i < ParameterTypes.size(); i++)
                    cpp.append(", ").append("obj").append(i - 1);
                cpp.append(");\n      env->MonitorExit(obj0);\n      return (jobjectArray)(obj);\n");
            }
            break;
            case "[I": {
                cpp.append("      jobject obj = env->CallObjectMethod(obj0, mid_cache[index]");
                for (int i = 2; i < ParameterTypes.size(); i++)
                    cpp.append(", ").append("obj").append(i - 1);
                cpp.append(");\n      env->MonitorExit(obj0);\n      return (jintArray)(obj);\n");
            }
            break;
            case "[Z": {
                cpp.append("      jobject obj = env->CallObjectMethod(obj0, mid_cache[index]");
                for (int i = 2; i < ParameterTypes.size(); i++)
                    cpp.append(", ").append("obj").append(i - 1);
                cpp.append(");\n      env->MonitorExit(obj0);\n      return (jbooleanArray)(obj);\n");
            }
            break;
            case "[J": {
                cpp.append("      jobject obj = env->CallObjectMethod(obj0, mid_cache[index]");
                for (int i = 2; i < ParameterTypes.size(); i++)
                    cpp.append(", ").append("obj").append(i - 1);
                cpp.append(");\n      env->MonitorExit(obj0);\n      return (jlongArray)(obj);\n");
            }
            break;
            case "[D": {
                cpp.append("      jobject obj = env->CallObjectMethod(obj0, mid_cache[index]");
                for (int i = 2; i < ParameterTypes.size(); i++)
                    cpp.append(", ").append("obj").append(i - 1);
                cpp.append(");\n      env->MonitorExit(obj0);\n      return (jdoubleArray)(obj);\n");
            }
            break;
            case "[B": {
                cpp.append("      jobject obj = env->CallObjectMethod(obj0, mid_cache[index]");
                for (int i = 2; i < ParameterTypes.size(); i++)
                    cpp.append(", ").append("obj").append(i - 1);
                cpp.append(");\n      env->MonitorExit(obj0);\n      return (jbyteArray)(obj);\n");
            }
            break;
            case "[F": {
                cpp.append("      jobject obj = env->CallObjectMethod(obj0, mid_cache[index]");
                for (int i = 2; i < ParameterTypes.size(); i++)
                    cpp.append(", ").append("obj").append(i - 1);
                cpp.append(");\n      env->MonitorExit(obj0);\n      return (jfloatArray)(obj);\n");
            }
            break;
            case "[S": {
                cpp.append("      jobject obj = env->CallObjectMethod(obj0, mid_cache[index]");
                for (int i = 2; i < ParameterTypes.size(); i++)
                    cpp.append(", ").append("obj").append(i - 1);
                cpp.append(");\n      env->MonitorExit(obj0);\n      return (jshortArray)(obj);\n");
            }
            break;
            case "[C": {
                cpp.append("      jobject obj = env->CallObjectMethod(obj0, mid_cache[index]");
                for (int i = 2; i < ParameterTypes.size(); i++)
                    cpp.append(", ").append("obj").append(i - 1);
                cpp.append(");\n      env->MonitorExit(obj0);\n      return (jcharArray)(obj);\n");
            }
            break;
        }
    }

    private void setsuperNative(StringBuilder cpp, String type, List<String> ParameterTypes) {
        if (type.startsWith("[")
                && type.contains("L")
                && type.endsWith(";")) {
            cpp.append("      jobject obj = env->CallNonvirtualObjectMethod(obj0, clazz_cache[index], mid_cache[index]");
            for (int i = 2; i < ParameterTypes.size(); i++)
                cpp.append(", ").append("obj").append(i - 1);
            cpp.append(");\n      env->MonitorExit(obj0);\n      return (jobjectArray)(obj);\n");
            return;
        }
        switch (type) {
            case "V": {
                cpp.append("      env->CallNonvirtualVoidMethod(obj0, clazz_cache[index], mid_cache[index]");
                for (int i = 2; i < ParameterTypes.size(); i++)
                    cpp.append(", ").append("obj").append(i - 1);
                cpp.append(");\n      env->MonitorExit(obj0);\n      break;\n");
            }
            break;
            case "Ljava/lang/String;": {
                cpp.append("      jobject obj = env->CallNonvirtualObjectMethod(obj0, clazz_cache[index], mid_cache[index]");
                for (int i = 2; i < ParameterTypes.size(); i++)
                    cpp.append(", ").append("obj").append(i - 1);
                cpp.append(");\n      env->MonitorExit(obj0);\n      return (jstring)(obj);\n");
            }
            break;
            case "Ljava/lang/Object;": {
                cpp.append("      jobject obj = env->CallNonvirtualObjectMethod(obj0, clazz_cache[index], mid_cache[index]");
                for (int i = 2; i < ParameterTypes.size(); i++)
                    cpp.append(", ").append("obj").append(i - 1);
                cpp.append(");\n      env->MonitorExit(obj0);\n      return obj;\n");
            }
            break;
            case "I": {
                cpp.append("      jint obj = env->CallNonvirtualIntMethod(obj0, clazz_cache[index], mid_cache[index]");
                for (int i = 2; i < ParameterTypes.size(); i++)
                    cpp.append(", ").append("obj").append(i - 1);
                cpp.append(");\n      env->MonitorExit(obj0);\n      return obj;\n");
            }
            break;
            case "Z": {
                cpp.append("      jboolean obj = env->CallNonvirtualBooleanMethod(obj0, clazz_cache[index], mid_cache[index]");
                for (int i = 2; i < ParameterTypes.size(); i++)
                    cpp.append(", ").append("obj").append(i - 1);
                cpp.append(");\n      env->MonitorExit(obj0);\n      return obj;\n");
            }
            break;
            case "J": {
                cpp.append("      jlong obj = env->CallNonvirtualLongMethod(obj0, clazz_cache[index], mid_cache[index]");
                for (int i = 2; i < ParameterTypes.size(); i++)
                    cpp.append(", ").append("obj").append(i - 1);
                cpp.append(");\n      env->MonitorExit(obj0);\n      return obj;\n");
            }
            break;
            case "D": {
                cpp.append("      jdouble obj = env->CallNonvirtualDoubleMethod(obj0, clazz_cache[index], mid_cache[index]");
                for (int i = 2; i < ParameterTypes.size(); i++)
                    cpp.append(", ").append("obj").append(i - 1);
                cpp.append(");\n      env->MonitorExit(obj0);\n      return obj;\n");
            }
            break;
            case "B": {
                cpp.append("      jbyte obj = env->CallNonvirtualByteMethod(obj0, clazz_cache[index], mid_cache[index]");
                for (int i = 2; i < ParameterTypes.size(); i++)
                    cpp.append(", ").append("obj").append(i - 1);
                cpp.append(");\n      env->MonitorExit(obj0);\n      return obj;\n");
            }
            break;
            case "F": {
                cpp.append("      jfloat obj = env->CallNonvirtualFloatMethod(obj0, clazz_cache[index], mid_cache[index]");
                for (int i = 2; i < ParameterTypes.size(); i++)
                    cpp.append(", ").append("obj").append(i - 1);
                cpp.append(");\n      env->MonitorExit(obj0);\n      return obj;\n");
            }
            break;
            case "S": {
                cpp.append("      jshort obj = env->CallNonvirtualShortMethod(obj0, clazz_cache[index], mid_cache[index]");
                for (int i = 2; i < ParameterTypes.size(); i++)
                    cpp.append(", ").append("obj").append(i - 1);
                cpp.append(");\n      env->MonitorExit(obj0);\n      return obj;\n");
            }
            break;
            case "C": {
                cpp.append("      jchar obj = env->CallNonvirtualCharMethod(obj0, clazz_cache[index], mid_cache[index]");
                for (int i = 2; i < ParameterTypes.size(); i++)
                    cpp.append(", ").append("obj").append(i - 1);
                cpp.append(");\n      env->MonitorExit(obj0);\n      return obj;\n");
            }
            break;
            case "[[I":
            case "[[Z":
            case "[[J":
            case "[[D":
            case "[[B":
            case "[[F":
            case "[[S":
            case "[[C": {
                cpp.append("      jobject obj = env->CallNonvirtualObjectMethod(obj0, clazz_cache[index], mid_cache[index]");
                for (int i = 2; i < ParameterTypes.size(); i++)
                    cpp.append(", ").append("obj").append(i - 1);
                cpp.append(");\n      env->MonitorExit(obj0);\n      return (jobjectArray)(obj);\n");
            }
            break;
            case "[I": {
                cpp.append("      jobject obj = env->CallNonvirtualObjectMethod(obj0, clazz_cache[index], mid_cache[index]");
                for (int i = 2; i < ParameterTypes.size(); i++)
                    cpp.append(", ").append("obj").append(i - 1);
                cpp.append(");\n      env->MonitorExit(obj0);\n      return (jintArray)(obj);\n");
            }
            break;
            case "[Z": {
                cpp.append("      jobject obj = env->CallNonvirtualObjectMethod(obj0, clazz_cache[index], mid_cache[index]");
                for (int i = 2; i < ParameterTypes.size(); i++)
                    cpp.append(", ").append("obj").append(i - 1);
                cpp.append(");\n      env->MonitorExit(obj0);\n      return (jbooleanArray)(obj);\n");
            }
            break;
            case "[J": {
                cpp.append("      jobject obj = env->CallNonvirtualObjectMethod(obj0, clazz_cache[index], mid_cache[index]");
                for (int i = 2; i < ParameterTypes.size(); i++)
                    cpp.append(", ").append("obj").append(i - 1);
                cpp.append(");\n      env->MonitorExit(obj0);\n      return (jlongArray)(obj);\n");
            }
            break;
            case "[D": {
                cpp.append("      jobject obj = env->CallNonvirtualObjectMethod(obj0, clazz_cache[index], mid_cache[index]");
                for (int i = 2; i < ParameterTypes.size(); i++)
                    cpp.append(", ").append("obj").append(i - 1);
                cpp.append(");\n      env->MonitorExit(obj0);\n      return (jdoubleArray)(obj);\n");
            }
            break;
            case "[B": {
                cpp.append("      jobject obj = env->CallNonvirtualObjectMethod(obj0, clazz_cache[index], mid_cache[index]");
                for (int i = 2; i < ParameterTypes.size(); i++)
                    cpp.append(", ").append("obj").append(i - 1);
                cpp.append(");\n      env->MonitorExit(obj0);\n      return (jbyteArray)(obj);\n");
            }
            break;
            case "[F": {
                cpp.append("      jobject obj = env->CallNonvirtualObjectMethod(obj0, clazz_cache[index], mid_cache[index]");
                for (int i = 2; i < ParameterTypes.size(); i++)
                    cpp.append(", ").append("obj").append(i - 1);
                cpp.append(");\n      env->MonitorExit(obj0);\n      return (jfloatArray)(obj);\n");
            }
            break;
            case "[S": {
                cpp.append("      jobject obj = env->CallNonvirtualObjectMethod(obj0, clazz_cache[index], mid_cache[index]");
                for (int i = 2; i < ParameterTypes.size(); i++)
                    cpp.append(", ").append("obj").append(i - 1);
                cpp.append(");\n      env->MonitorExit(obj0);\n      return (jshortArray)(obj);\n");
            }
            break;
            case "[C": {
                cpp.append("      jobject obj = env->CallNonvirtualObjectMethod(obj0, clazz_cache[index], mid_cache[index]");
                for (int i = 2; i < ParameterTypes.size(); i++)
                    cpp.append(", ").append("obj").append(i - 1);
                cpp.append(");\n      env->MonitorExit(obj0);\n      return (jcharArray)(obj);\n");
            }
            break;
            default:
                System.out.println("未知Type:" + type);
                break;
        }
    }

    private void NewObjectNative(StringBuilder cpp, String type, List<String> ParameterTypes) {
        switch (type) {
            case "Ljava/lang/Object;": {
                cpp.append("      jobject obj = env->NewObject(clazz_cache[index], mid_cache[index]");
                for (int i = 1; i < ParameterTypes.size(); i++)
                    cpp.append(", ").append("obj").append(i - 1);
                cpp.append(");\n");
                cpp.append("      return obj;\n");
            }
            break;
            case "Ljava/lang/String;": {
                cpp.append("      jobject obj = env->NewObject(clazz_cache[index], mid_cache[index]");
                for (int i = 1; i < ParameterTypes.size(); i++)
                    cpp.append(", ").append("obj").append(i - 1);
                cpp.append(");\n      return (jstring)(obj);\n");
            }
            break;
            default:
                System.out.println("未知类型:" + type);
                break;
        }
    }

    private void setNewObjectNative(StringBuilder cpp, String type, List<String> ParameterTypes) {
        cpp.append("      env->MonitorEnter(obj0);\n");
        cpp.append("      env->CallVoidMethod(obj0, mid_cache[index]");
        for (int i = 2; i < ParameterTypes.size(); i++)
            cpp.append(", ").append("obj").append(i - 1);
        cpp.append(");\n");
        cpp.append("      env->MonitorExit(obj0);\n      break;\n");
    }

    private void setGetObjectResult(StringBuilder source, FieldReference fieldReference) {
        if (fieldReference.getType().startsWith("[")
                && fieldReference.getType().contains("L")) {
            source.append("      return (jobjectArray)(obj);\n");
            return;
        } else if (fieldReference.getType().startsWith("[[[")) {
            source.append("      return (jobjectArray)(obj);\n");
            return;
        }
        switch (fieldReference.getType()) {
            case "Ljava/lang/String;":
                source.append("      return (jstring)(obj);\n");
                break;
            case "[Z":
                source.append("      return (jbooleanArray)(obj);\n");
                break;
            case "[I":
                source.append("      return (jintArray)(obj);\n");
                break;
            case "[J":
                source.append("      return (jlongArray)(obj);\n");
                break;
            case "[S":
                source.append("      return (jshortArray)(obj);\n");
                break;
            case "[C":
                source.append("      return (jcharArray)(obj);\n");
                break;
            case "[D":
                source.append("      return (jdoubleArray)(obj);\n");
                break;
            case "[B":
                source.append("      return (jbyteArray)(obj);\n");
                break;
            case "[F":
                source.append("      return (jfloatArray)(obj);\n");
                break;
            case "[[I":
            case "[[Z":
            case "[[J":
            case "[[D":
            case "[[B":
            case "[[F":
            case "[[S":
            case "[[C":
                source.append("      return (jobjectArray)(obj);\n");
                break;
            default:
                source.append("      return obj;\n");
                break;
        }
    }

    private void NewArray(StringBuilder source, String type) {
        switch (type) {
            case "[I": {
                source.append("      return env->NewIntArray(obj0);\n");
            }
            break;
            case "[Z": {
                source.append("      return env->NewBooleanArray(obj0);\n");
            }
            break;
            case "[J": {
                source.append("      return env->NewLongArray(obj0);\n");
            }
            break;
            case "[D": {
                source.append("      return env->NewDoubleArray(obj0);\n");
            }
            break;
            case "[B": {
                source.append("      return env->NewByteArray(obj0);\n");
            }
            break;
            case "[F": {
                source.append("      return env->NewFloatArray(obj0);\n");
            }
            break;
            case "[S": {
                source.append("      return env->NewShortArray(obj0);\n");
            }
            break;
            case "[C": {
                source.append("      return env->NewCharArray(obj0);\n");
            }
            break;
            default: {
                source.append("      return env->NewObjectArray(obj0, clazz_cache[index],NULL);\n");
            }
            break;
        }
    }

    private void NewFilledArray(StringBuilder source, String type, int i) {
        switch (type) {
            case "[I": {
                source.append("      jintArray array = env->NewIntArray(" + i + ");\n")
                        .append("      jint *ints = env->GetIntArrayElements(array, 0);\n")
                        .append("      int len = env->GetArrayLength(array);\n");
                for (int index = 0; index < i; index++)
                    source.append("      ints[" + index + "] = obj").append(i).append(";\n");
                source.append("      env->SetIntArrayRegion(array, 0, len, ints);\n")
                        .append("      return array;\n");
            }
            break;
            case "[Z": {
                source.append("      jbooleanArray array = env->NewBooleanArray(" + i + ");\n")
                        .append("      jboolean *booleans = env->GetBooleanArrayElements(array, 0);\n")
                        .append("      int len = env->GetArrayLength(array);\n");
                for (int index = 0; index < i; index++)
                    source.append("      booleans[" + index + "] = obj").append(i).append(";\n");
                source.append("      env->SetBooleanArrayRegion(array, 0, len, booleans);\n")
                        .append("      return array;\n");
            }
            break;
            case "[J": {
                source.append("      jlongArray array = env->NewLongArray(" + i + ");\n")
                        .append("      jlong *longs = env->GetLongArrayElements(array, 0);\n")
                        .append("      int len = env->GetArrayLength(array);\n");
                for (int index = 0; index < i; index++)
                    source.append("      longs[" + index + "] = obj").append(i).append(";\n");
                source.append("      env->SetLongArrayRegion(array, 0, len, longs);\n")
                        .append("      return array;\n");
            }
            break;
            case "[D": {
                source.append("      jdoubleArray array = env->NewDoubleArray(" + i + ");\n")
                        .append("      jdouble *doubles = env->GetDoubleArrayElements(array, 0);\n")
                        .append("      int len = env->GetArrayLength(array);\n");
                for (int index = 0; index < i; index++)
                    source.append("      doubles[" + index + "] = obj").append(i).append(";\n");
                source.append("      env->SetDoubleArrayRegion(array, 0, len, doubles);\n")
                        .append("      return array;\n");
            }
            break;
            case "[B": {
                source.append("      jbyteArray array = env->NewByteArray(" + i + ");\n")
                        .append("      jbyte *bytes = env->GetByteArrayElements(array, 0);\n")
                        .append("      int len = env->GetArrayLength(array);\n");
                for (int index = 0; index < i; index++)
                    source.append("      doubles[" + index + "] = obj").append(i).append(";\n");
                source.append("      env->SetByteArrayRegion(array, 0, len, bytes);\n")
                        .append("      return array;\n");
            }
            break;
            case "[F": {
                source.append("      jfloatArray array = env->NewFloatArray(" + i + ");\n")
                        .append("      jfloat *floats = env->GetFloatArrayElements(array, 0);\n")
                        .append("      int len = env->GetArrayLength(array);\n");
                for (int index = 0; index < i; index++)
                    source.append("      floats[" + index + "] = obj").append(i).append(";\n");
                source.append("      env->SetFloatArrayRegion(array, 0, len, floats);\n")
                        .append("      return array;\n");
            }
            break;
            case "[S": {
                source.append("      jshortArray array = env->NewShortArray(" + i + ");\n")
                        .append("      jshort *shorts = env->GetShortArrayElements(array, 0);\n")
                        .append("      int len = env->GetArrayLength(array);\n");
                for (int index = 0; index < i; index++)
                    source.append("      shorts[" + index + "] = obj").append(i).append(";\n");
                source.append("      env->SetShortArrayRegion(array, 0, len, shorts);\n")
                        .append("      return array;\n");
            }
            break;
            case "[C": {
                source.append("      jcharArray array = env->NewCharArray(" + i + ");\n")
                        .append("      jchar *chars = env->GetCharArrayElements(array, 0);\n")
                        .append("      int len = env->GetArrayLength(array);\n");
                for (int index = 0; index < i; index++)
                    source.append("      chars[" + index + "] = obj").append(i).append(";\n");
                source.append("      env->SetCharArrayRegion(array, 0, len, chars);\n")
                        .append("      return array;\n");
            }
            break;
            default: {
                source.append("      jobjectArray array = env->NewObjectArray(" + i + ", clazz_cache[index],NULL);\n");
                for (int index = 0; index < i; index++)
                    source.append("      env->SetObjectArrayElement(array," + index + ", obj").append(index).append(");\n");
                source.append("      return array;\n");
            }
            break;
        }
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
