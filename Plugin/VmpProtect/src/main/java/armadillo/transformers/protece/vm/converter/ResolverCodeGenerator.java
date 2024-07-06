package armadillo.transformers.protece.vm.converter;

import armadillo.transformers.protece.vm.vmutils.MethodHelper;
import armadillo.transformers.protece.vm.vmutils.ModifiedUtf8;
import org.jf.dexlib2.iface.reference.FieldReference;
import org.jf.dexlib2.iface.reference.MethodReference;
import org.jf.dexlib2.iface.reference.StringReference;
import org.jf.dexlib2.iface.reference.TypeReference;
import org.jf.dexlib2.util.MethodUtil;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ResolverCodeGenerator {
    private final ReferencesAnalyzer referencesAnalyzer;

    public ResolverCodeGenerator(ReferencesAnalyzer referencesAnalyzer) {
        this.referencesAnalyzer = referencesAnalyzer;
    }

    public void generate(Writer writer) throws IOException {
        writer.write("#include \"GlobalCache.h\"\n");
        writer.write("#include \"interpreter.h\"\n");
        writer.write("#include <pthread.h>\n\n");
        writer.write("" +
                "#define FIND_CLASS_BY_NAME(_className)                          \\\n" +
                "    clazz = env->FindClass( _className);                        \\\n" +
                "    if (clazz == NULL) {                                        \\\n" +
                "        env->ExceptionClear();                                  \\\n" +
                "        vmThrowNoClassDefFoundError(env, _className);           \\\n" +
                "        return NULL;                                            \\\n" +
                "    }\n\n");
        generateResolver(writer);
        generateMethodRefs(writer);
        generateFieldRefs(writer);
        generateTypeRefs(writer);
        generateStringRefs(writer);
        if (referencesAnalyzer.getStringReferencesMapIndex().size() == 0)
            writer.write("\n\nstatic jstring gStrings[0];");
         else
            writer.write(String.format("\n\nstatic jstring gStrings[%d] = {0};", referencesAnalyzer.getStringReferencesMapIndex().size()));
        writer.write("\n\n\n" +
                "static const vmField *vmResolveField(JNIEnv *env, uint32_t idx, bool isStatic) {\n" +
                "    vmField *field = &gFields[idx];\n" +
                "    if (field->fieldId == NULL) {\n" +
                "        FieldIds fieldId = gFieldIds[idx];\n" +
                "        jclass clazz = env->FindClass((const char *) (gBaseStrPtr + fieldId.classIdx));\n" +
                "        if (clazz == NULL) {\n" +
                "            env->ExceptionClear();\n" +
                "            vmThrowNoClassDefFoundError(env, (const char *) (gBaseStrPtr + fieldId.classIdx));\n" +
                "            return NULL;\n" +
                "        }\n" +
                "        const char *type = (const char *) (gBaseStrPtr + fieldId.typeIdx);\n" +
                "        const char *name = (const char *) (gBaseStrPtr + fieldId.nameIdx);\n" +
                "        field->classIdx = fieldId.classIdx;\n" +
                "        field->type = (*type == '[') ? 'L' : *type;\n" +
                "        jfieldID fid;\n" +
                "        if (isStatic)\n" +
                "            fid = env->GetStaticFieldID(clazz, name, type);\n" +
                "        else\n" +
                "            fid = env->GetFieldID(clazz, name, type);\n" +
                "        if (fid == NULL) {\n" +
                "            env->DeleteLocalRef(clazz);\n" +
                "            env->ExceptionClear();\n" +
                "            vmThrowNoSuchFieldError(env, name);\n" +
                "            return NULL;\n" +
                "        }\n" +
                "        env->DeleteLocalRef(clazz);\n" +
                "        field->fieldId = fid;\n" +
                "    }\n" +
                "    return field;\n" +
                "}\n" +
                "\n" +
                "static const vmMethod *vmResolveMethod(JNIEnv *env, uint32_t idx, bool isStatic) {\n" +
                "    vmMethod *method = &gMethods[idx];\n" +
                "    if (method->methodId == NULL) {\n" +
                "        MethodIds methodId = gMethodIds[idx];\n" +
                "        jclass clazz = env->FindClass((const char *) (gBaseStrPtr + methodId.classIdx));\n" +
                "        if (clazz == NULL) {\n" +
                "            env->ExceptionClear();\n" +
                "            vmThrowNoClassDefFoundError(env, (const char *) (gBaseStrPtr + methodId.classIdx));\n" +
                "            return NULL;\n" +
                "        }\n" +
                "        method->shorty = (const char *) (gBaseStrPtr + methodId.shortyIdx);\n" +
                "        method->classIdx = methodId.classIdx;\n" +
                "        const char *name = (const char *) (gBaseStrPtr + methodId.nameIdx);\n" +
                "        const char *sig = (const char *) (gBaseStrPtr + methodId.sigIdx);\n" +
                "        jmethodID mid;\n" +
                "        if (isStatic)\n" +
                "            mid = env->GetStaticMethodID(clazz, name, sig);\n" +
                "        else\n" +
                "            mid = env->GetMethodID(clazz, name, sig);\n" +
                "        if (mid == NULL) {\n" +
                "            env->DeleteLocalRef(clazz);\n" +
                "            env->ExceptionClear();\n" +
                "            vmThrowNoSuchMethodError(env, name);\n" +
                "            return NULL;\n" +
                "        }\n" +
                "        env->DeleteLocalRef(clazz);\n" +
                "        method->methodId = mid;\n" +
                "    }\n" +
                "    return method;\n" +
                "}\n" +
                "\n" +
                "static const char *vmResolveTypeUtf(uint32_t idx) {\n" +
                "    return (const char *) (gBaseStrPtr + gTypeIds[idx].idx);\n" +
                "}\n" +
                "\n" +
                "static pthread_mutex_t str_mutex = PTHREAD_MUTEX_INITIALIZER;\n"+
                "\n"+
                "static jstring vmString(JNIEnv *env, uint32_t idx) {\n" +
                "    if (gStrings[idx] == NULL) {\n" +
                "        pthread_mutex_lock(&str_mutex);\n" +
                "        jstring str = env->NewStringUTF((const char *) (gBaseStrPtr + gStringIds[idx].off));\n" +
                "        gStrings[idx] = (jstring) env->NewGlobalRef(str);\n" +
                "        pthread_mutex_unlock(&str_mutex);\n" +
                "        return str;\n" +
                "    } else\n" +
                "        return (jstring) env->NewLocalRef(gStrings[idx]);\n" +
                "}\n"+
                "\n" +
                "static jclass vmResolveClass(JNIEnv *env, uint32_t idx) {\n" +
                "    jclass clazz = getCacheClass(env, (const char *) (gBaseStrPtr + idx));\n" +
                "    if (clazz != NULL) {\n" +
                "        return (jclass) env->NewLocalRef(clazz);\n" +
                "    }\n" +
                "    clazz = env->FindClass((const char *) (gBaseStrPtr + idx));\n" +
                "    if (clazz == NULL) {\n" +
                "        env->ExceptionClear();\n" +
                "        vmThrowNoClassDefFoundError(env, (const char *) (gBaseStrPtr + idx));\n" +
                "        return NULL;\n" +
                "    }\n" +
                "    return clazz;\n" +
                "}\n" +
                "\n" +
                "static jclass vmFindClass(JNIEnv *env, const char *type) {\n" +
                "    jclass clazz = getCacheClass(env, type);\n" +
                "    if (clazz != NULL) {\n" +
                "        return (jclass) env->NewLocalRef(clazz);\n" +
                "    }\n" +
                "    if (*type == 'L') {\n" +
                "        size_t len = strlen(type);\n" +
                "        char *clazzName = (char *) malloc(len * sizeof(char));\n" +
                "        strncpy(clazzName, type + 1, len - 2);\n" +
                "        clazzName[len - 2] = 0;\n" +
                "        FIND_CLASS_BY_NAME(clazzName);\n" +
                "        free(clazzName);" +
                "        return clazz;\n" +
                "    }\n" +
                "    FIND_CLASS_BY_NAME(type);\n" +
                "    return clazz;\n" +
                "}\n" +
                "\n" +
                "static const VmResolver vmResolver = {\n" +
                "        .vmResolveField = vmResolveField,\n" +
                "        .vmResolveMethod = vmResolveMethod,\n" +
                "        .vmResolveTypeUtf = vmResolveTypeUtf,\n" +
                "        .vmResolveClass = vmResolveClass,\n" +
                "        .vmFindClass = vmFindClass,\n" +
                "        .vmString = vmString,\n" +
                "};");
    }

    private void generateResolver(Writer writer) throws IOException {
        writer.write("static const uint8_t gBaseStrPtr[] = {\n\t");
        for (int i = 0; i < referencesAnalyzer.getString_Pool().size(); i++) {
            if (i != 0 && i % 10 == 0)
                writer.write("\n\t");
            writer.write(String.format("0x%02x,", referencesAnalyzer.getString_Pool().get(i) & 0xFF));
        }
        writer.write("\n};\n");
        writer.flush();
    }

    private void generateFieldRefs(Writer writer) throws IOException {
        writer.write("\n" +
                "typedef struct {\n" +
                "    uint32_t classIdx;\n" +
                "    uint32_t nameIdx;\n" +
                "    uint32_t typeIdx;\n" +
                "} FieldIds;\n");
        writer.write("static const FieldIds gFieldIds[] = {\n");
        for (FieldReference reference : referencesAnalyzer.getFieldReferencesMapIndex().keySet()) {
            int classNameIdx = referencesAnalyzer.getOffset(MethodHelper.genJniClass(reference.getDefiningClass()));
            int nameIdx = referencesAnalyzer.getOffset(reference.getName());
            int typeIdx = referencesAnalyzer.getOffset(reference.getType());
            writer.write(String.format(
                    "    {.classIdx=%d, .nameIdx=%d, .typeIdx=%d},\n",
                    classNameIdx, nameIdx, typeIdx));
        }
        writer.write("};\n");
        if (referencesAnalyzer.getFieldReferencesMapIndex().keySet().size() == 0)
            writer.write("static vmField gFields[0];\n");
        else
            writer.write(String.format("static vmField gFields[%d] = {0};\n", referencesAnalyzer.getFieldReferencesMapIndex().keySet().size()));
        writer.flush();
    }

    private void generateMethodRefs(Writer writer) throws IOException {
        writer.write("\n" +
                "typedef struct {\n" +
                "    uint32_t classIdx;\n" +
                "    uint32_t nameIdx;\n" +
                "    uint32_t shortyIdx;\n" +
                "    uint32_t sigIdx;\n" +
                "} MethodIds;\n");
        writer.write("static const MethodIds gMethodIds[] = {\n");
        for (MethodReference reference : referencesAnalyzer.getMethodReferencesMapIndex().keySet()) {
            int classNameIdx = referencesAnalyzer.getOffset(MethodHelper.genJniClass(reference.getDefiningClass()));
            int nameIdx = referencesAnalyzer.getOffset(reference.getName());
            int shortyIdx = referencesAnalyzer.getOffset(MethodHelper.genShorty(reference));
            int sigIdx = referencesAnalyzer.getOffset(MethodHelper.genMethodSig(reference));
            writer.write(String.format(
                    "    {.classIdx=%d, .nameIdx=%d, .shortyIdx=%d, .sigIdx=%d},\n",
                    classNameIdx, nameIdx, shortyIdx, sigIdx));
        }
        writer.write("};\n");
        if (referencesAnalyzer.getMethodReferencesMapIndex().keySet().size() == 0)
            writer.write("static vmMethod gMethods[0];\n");
        else
            writer.write(String.format("static vmMethod gMethods[%d] = {0};\n", referencesAnalyzer.getMethodReferencesMapIndex().keySet().size()));
        writer.flush();
    }

    private void generateTypeRefs(Writer writer) throws IOException {
        writer.write("\n" +
                "typedef struct {\n" +
                "    uint32_t idx;\n" +
                "} TypeIds;\n");
        writer.write("static const TypeIds gTypeIds[] = {\n");
        for (TypeReference typeReference : referencesAnalyzer.getTypeReferencesMapIndex().keySet())
            writer.write(String.format("    {.idx=%d},\n", referencesAnalyzer.getOffset(MethodHelper.genJniClass(typeReference.getType()))));
        writer.write("};\n");
        writer.flush();
    }

    private void generateStringRefs(Writer writer) throws IOException {
        writer.write("\n" +
                "typedef struct {\n" +
                "    uint32_t off;\n" +
                "} StringIds;\n");

        writer.write("static const StringIds gStringIds[] = {\n");
        for (StringReference reference : referencesAnalyzer.getStringReferencesMapIndex().keySet()) {
            int off = referencesAnalyzer.getOffset(reference.getString());
            writer.write(String.format(
                    "    {.off=%d},\n",
                    off));
        }
        writer.write("};\n");
        writer.flush();
    }
}
