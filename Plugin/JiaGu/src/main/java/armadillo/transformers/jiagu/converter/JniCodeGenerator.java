package armadillo.transformers.jiagu.converter;

import armadillo.transformers.jiagu.vmutils.MethodHelper;
import com.google.common.collect.Lists;
import org.jf.dexlib2.AccessFlags;
import org.jf.dexlib2.dexbacked.DexBuffer;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.util.MethodUtil;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class JniCodeGenerator {
    private final String dexName;
    private final Set<ClassDef> defs;
    private final InstructionRewriter instructionRewriter;
    private final List<String> KeepMethodNames;
    public StringBuilder method_builder = new StringBuilder();
    public HashMap<String, List<String>> RegisterNatives = new HashMap<>();

    public JniCodeGenerator(String dexName, Set<ClassDef> defs, InstructionRewriter instructionRewriter, List<String> keepMethodNames) {
        this.dexName = dexName;
        this.defs = defs;
        this.instructionRewriter = instructionRewriter;
        this.KeepMethodNames = keepMethodNames;
    }

    public void generate(Writer writer) throws IOException {
        writer.write("" +
                "#include <jni.h>\n" +
                "#include \"interpreter_switch.h\"\n" +
                String.format("#include \"%s_resolver.cpp\"\n", dexName) +
                "\n" +
                "#define NELEM(x) ((int)(sizeof(x) / sizeof((x)[0])))\n" +
                "#define SET_REGISTER_FLOAT(_idx, _val)      (*((float*) &regs[(_idx)]) = (_val));\n" +
                "#define SET_REGISTER_WIDE(_idx, _val)       (regs[(_idx)] =(int64_t) (_val));\n" +
                "#define SET_REGISTER_DOUBLE(_idx, _val)     (*((double*) &regs[(_idx)]) = (_val));\n\n\n");
        for (ClassDef def : defs) {
            String key = def.getType().substring(1, def.getType().length() - 1);
            for (Method method : def.getMethods()) {
                if (KeepMethodNames.contains(method.getName())
                        || !"onCreate".equals(method.getName())
                        || method.getImplementation() == null)
                    continue;
                if (RegisterNatives.get(key) != null)
                    RegisterNatives.get(key).add(String.format("\t{\"%s\", \"%s\", (void *) %s},\n",
                            method.getName(),
                            MethodHelper.genMethodSig(method),
                            MethodHelper.genMethodNameNative(method.getDefiningClass(), method.getName() + Arrays.toString(method.getParameterTypes().toArray(new CharSequence[0])) + method.getReturnType())));
                else
                    RegisterNatives.put(key, Lists.newArrayList(String.format("\t{\"%s\", \"%s\", (void *) %s},\n",
                            method.getName(),
                            MethodHelper.genMethodSig(method),
                            MethodHelper.genMethodNameNative(method.getDefiningClass(), method.getName() + Arrays.toString(method.getParameterTypes().toArray(new CharSequence[0])) + method.getReturnType()))));
                //写注释
                writer.write(String.format("//%s->%s%s\n", key, method.getName(), MethodHelper.genMethodSig(method)));
                //写方法名
                writer.write(String.format("%s %s(%s) {\n",
                        MethodHelper.genTypeInNative(method.getReturnType()),
                        MethodHelper.genMethodNameNative(method.getDefiningClass(), method.getName() + Arrays.toString(method.getParameterTypes().toArray(new CharSequence[0])) + method.getReturnType()),
                        MethodHelper.genParamTypeListInNative(method)));
                //写寄存器
                {
                    StringBuilder regs = new StringBuilder();
                    regs.append(String.format("\tuint64_t regs[%d];\n", method.getImplementation().getRegisterCount()));
                    int local_reg = method.getImplementation().getRegisterCount() - MethodUtil.getParameterRegisterCount(method);
                    int preg = local_reg;
                    for (int i = 0; i < local_reg; i++) {
                        regs.append(String.format("\tregs[%d] = 0;\n", i));
                    }
                    if (!AccessFlags.STATIC.isSet(method.getAccessFlags())) {
                        regs.append(String.format("\tregs[%d] = (uint64_t)thiz;\n", preg));
                        preg++;
                    }
                    for (int i = 0; i < method.getParameterTypes().size(); i++) {
                        switch (method.getParameterTypes().get(i).charAt(0)) {
                            case 'D':
                                regs.append(String.format("\tregs[%d] = 0;\n", preg));
                                regs.append(String.format("\tSET_REGISTER_DOUBLE(%d,p%d)\n", preg, i));
                                regs.append(String.format("\tregs[%d] = 0;\n", preg + 1));
                                preg += 2;
                                break;
                            case 'J':
                                regs.append(String.format("\tregs[%d] = 0;\n", preg));
                                regs.append(String.format("\tSET_REGISTER_WIDE(%d,p%d)\n", preg, i));
                                regs.append(String.format("\tregs[%d] = 0;\n", preg + 1));
                                preg += 2;
                                break;
                            case 'F':
                                regs.append(String.format("\tregs[%d] = 0;\n", preg));
                                regs.append(String.format("\tSET_REGISTER_FLOAT(%d,p%d)\n", preg, i));
                                preg++;
                                break;
                            case '[':
                            case 'L':
                                regs.append(String.format("\tregs[%d] = (uint64_t)p%d;\n", preg, i));
                                preg++;
                                break;
                            default:
                                regs.append(String.format("\tregs[%d] = p%d;\n", preg, i));
                                preg++;
                                break;
                        }
                    }
                    writer.write(regs.toString());
                    writer.write("\n");
                    StringBuilder reg_flags = new StringBuilder();
                    reg_flags.append(String.format("\tuint8_t reg_flags[%d];\n", method.getImplementation().getRegisterCount()));
                    preg = local_reg;
                    for (int i = 0; i < local_reg; i++) {
                        reg_flags.append(String.format("\treg_flags[%d] = 0;\n", i));
                    }
                    if (!AccessFlags.STATIC.isSet(method.getAccessFlags())) {
                        reg_flags.append(String.format("\treg_flags[%d] = 1;\n", preg));
                        preg++;
                    }
                    for (int i = 0; i < method.getParameterTypes().size(); i++) {
                        switch (method.getParameterTypes().get(i).charAt(0)) {
                            case '[':
                            case 'L':
                                reg_flags.append(String.format("\treg_flags[%d] = 1;\n", preg));
                                preg++;
                                break;
                            case 'D':
                            case 'J':
                                reg_flags.append(String.format("\treg_flags[%d] = 0;\n", preg));
                                reg_flags.append(String.format("\treg_flags[%d] = 0;\n", preg + 1));
                                preg++;
                                break;
                            default:
                                reg_flags.append(String.format("\treg_flags[%d] = 0;\n", preg));
                                preg++;
                                break;
                        }
                    }
                    writer.write(reg_flags.toString());
                    writer.write("\n");
                }
                //写指令
                {
                    byte[] instructionData = instructionRewriter.instructionRewriter(method.getImplementation());
                    writer.append("\tstatic const uint16_t inst[] = {");
                    final int dataLength = instructionData.length;
                    final DexBuffer instructionBuf = new DexBuffer(instructionData);
                    for (int offset = 0; offset < dataLength; offset += 2) {
                        if (offset % 20 == 0) {
                            writer.append("\n\t");
                        }
                        writer.append(String.format("0x%04x, ", instructionBuf.readUshort(offset)));
                    }
                    writer.append("\n\t};\n");
                }
                //写try数据
                {
                    final byte[] tries = instructionRewriter.handleTries(method.getImplementation());
                    StringBuilder triesBuilder = new StringBuilder();
                    if (tries.length == 0) {
                        triesBuilder.append("\tconst uint8_t *tries = NULL;\n");
                    } else {
                        triesBuilder.append("\tstatic const uint8_t tries[] = {");
                        for (int i = 0; i < tries.length; i++) {
                            if (i % 10 == 0)
                                triesBuilder.append("\n\t");
                            triesBuilder.append(String.format("0x%02x, ", tries[i] & 0xFF));
                        }
                        triesBuilder.append("\n\t};\n");
                    }
                    writer.write(triesBuilder.toString());
                }
                //写执行语句
                writer.write("" +
                        "\tconst VmCode vmCode = {inst, regs, reg_flags, tries};\n" +
                        "\tjvalue result = ExecuteSwitchImpl(env, &vmCode, &vmResolver);\n");
                String ret = "";
                switch (method.getReturnType().charAt(0)) {
                    case 'I':
                        ret = "\treturn result.i;";
                        break;
                    case 'B':
                        ret = "\treturn result.b;";
                        break;
                    case 'Z':
                        ret = "\treturn result.z;";
                        break;
                    case 'C':
                        ret = "\treturn result.c;";
                        break;
                    case 'S':
                        ret = "\treturn result.s;";
                        break;
                    case 'F':
                        ret = "\treturn result.f;";
                        break;
                    case 'J':
                        ret = "\treturn result.j;";
                        break;
                    case 'D':
                        ret = "\treturn result.d;";
                        break;
                    case 'V':
                        ret = "\treturn;";
                        break;
                    default:
                        ret = "\treturn result.l;";
                        break;
                }
                writer.write(ret);
                writer.write("\n}\n");
            }
        }
        RegisterNatives.forEach((key, value) -> {
            StringBuilder method_build = new StringBuilder();
            value.forEach(method_build::append);
            String method_table = String.format("static JNINativeMethod %s[] = {\n%s\n};\n",
                    MethodHelper.genMethodNameNative(key, "method_table"),
                    method_build.toString());
            method_builder.append(method_table);
        });
        method_builder.append(String.format("void %s_setup(JNIEnv *env) {\n", dexName));
        RegisterNatives.forEach((key, value) -> {
            method_builder.append("\t{\n")
                    .append(String.format("\t\tjclass clazz = env->FindClass(\"%s\");\n", key))
                    .append("\t\tif (clazz == NULL) {\n")
                    .append(String.format("\t\t\tvmThrowNoClassDefFoundError(env, \"%s\");\n", key))
                    .append("\t\t\tenv->ExceptionDescribe();\n")
                    .append("\t\t\tenv->ExceptionClear();\n")
                    .append("\t\t} else\n")
                    .append(String.format("\t\t\tenv->RegisterNatives(clazz, %s, NELEM(%s));\n",
                            MethodHelper.genMethodNameNative(key, "method_table"),
                            MethodHelper.genMethodNameNative(key, "method_table")))
                    .append("\t}\n");
        });
        method_builder.append("}\n");
        writer.write(method_builder.toString());
    }
}
