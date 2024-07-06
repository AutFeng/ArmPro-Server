package armadillo.transformers.obfuscators.flow;


import armadillo.transformers.base.BaseTransformer;
import armadillo.transformers.base.DexTransformer;
import armadillo.utils.StringRandom;
import armadillo.utils.SysConfigUtil;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.jf.dexlib2.AccessFlags;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.builder.MutableMethodImplementation;
import org.jf.dexlib2.builder.SwitchLabelElement;
import org.jf.dexlib2.builder.instruction.*;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.Field;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.MethodParameter;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.formats.Instruction35c;
import org.jf.dexlib2.iface.reference.MethodReference;
import org.jf.dexlib2.immutable.ImmutableClassDef;
import org.jf.dexlib2.immutable.ImmutableField;
import org.jf.dexlib2.immutable.ImmutableMethod;
import org.jf.dexlib2.immutable.ImmutableMethodImplementation;
import org.jf.dexlib2.immutable.instruction.*;
import org.jf.dexlib2.immutable.reference.ImmutableFieldReference;
import org.jf.dexlib2.immutable.reference.ImmutableMethodProtoReference;
import org.jf.dexlib2.immutable.reference.ImmutableMethodReference;
import org.jf.dexlib2.immutable.reference.ImmutableTypeReference;
import org.jf.dexlib2.rewriter.Rewriter;
import org.jf.dexlib2.rewriter.Rewriters;

import javax.annotation.Nonnull;
import java.util.*;

public class ControlFlow extends DexTransformer {
    private int flow_total;
    private final HashSet<String> diff = new HashSet<>();

    @Nonnull
    @Override
    public Rewriter<ClassDef> getClassDefRewriter(@Nonnull Rewriters rewriters) {
        return new Rewriter<ClassDef>() {
            @Nonnull
            @Override
            public ClassDef rewrite(@Nonnull ClassDef classDef) {
                if (diff.contains(classDef.getType())) {
                    List<Method> methods = new ArrayList<>();
                    HashMap<String, List<Number>> map = new HashMap<>();
                    boolean isexistClinit = false;
                    for (Method method : classDef.getMethods()) {
                        if (method.getName().equals("<clinit>"))
                            isexistClinit = true;
                        else if (method.getImplementation() != null) {
                            List<Number> hashMap = new ArrayList<>();
                            String fieldsName = nameFactory.nextName();
                            int minStartReg = method.getImplementation().getRegisterCount() + 1;
                            MutableMethodImplementation mutableImplementation = new MutableMethodImplementation(method.getImplementation());
                            mutableImplementation.fixReg(5 + mutableImplementation.getMethodReg(method), method);
                            for (int i = 0; i < mutableImplementation.getInstructions().size(); i++) {
                                Instruction instruction = mutableImplementation.getInstructions().get(i);
                                switch (instruction.getOpcode()) {
                                    case INVOKE_STATIC:
                                    case INVOKE_VIRTUAL:
                                    case INVOKE_DIRECT:
                                    case INVOKE_SUPER:
                                    case INVOKE_CUSTOM: {
                                        if (method.getName().equals("<init>"))
                                            break;
                                        Instruction35c instruction35c = (Instruction35c) instruction;
                                        MethodReference reference = (MethodReference) instruction35c.getReference();
                                        if (!reference.getName().equals("<init>")) {
                                            Opcode LastOpcde = mutableImplementation.getInstructions().get(i + 1).getOpcode();
                                            if (LastOpcde == Opcode.MOVE_RESULT_OBJECT
                                                    || LastOpcde == Opcode.MOVE_RESULT
                                                    || LastOpcde == Opcode.MOVE_RESULT_WIDE)
                                                i++;
                                            if (!reference.getReturnType().equals("V")) {
                                                if (LastOpcde == Opcode.MOVE_RESULT_OBJECT
                                                        || LastOpcde == Opcode.MOVE_RESULT
                                                        || LastOpcde == Opcode.MOVE_RESULT_WIDE)
                                                    break;
                                            }
                                            /**
                                             * sget-object v0,Lxxx/xxx->name:[I
                                             * const v1,0x0
                                             * aget v1,v0,v1
                                             * if-ltz v1, :正确的代码 小于0跳转
                                             * :goto_28
                                             * const v0,0x1
                                             * xor-int v0,v0,v1
                                             * and-int v0,v1,v0
                                             * if-gtz v0, :正确的代码 大于0跳转
                                             * goto :goto_28
                                             */
                                            int v0 = new Random().nextInt(99999999);
                                            int v1 = new Random().nextInt(99999999);
                                            hashMap.add(v1);
                                            mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction21c(
                                                    Opcode.SGET_OBJECT,
                                                    minStartReg,
                                                    new ImmutableFieldReference(
                                                            classDef.getType(),
                                                            fieldsName,
                                                            "[I")));
                                            mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction31i(
                                                    Opcode.CONST,
                                                    minStartReg + 1,
                                                    hashMap.size() - 1));
                                            mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction23x(
                                                    Opcode.AGET,
                                                    minStartReg + 1,
                                                    minStartReg,
                                                    minStartReg + 1));
                                            //mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction10x(Opcode.NOP));
                                            mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction21t(
                                                    Opcode.IF_LTZ,
                                                    minStartReg + 1,
                                                    mutableImplementation.newLabelForIndex(i)));
                                            mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction31i(
                                                    Opcode.CONST,
                                                    minStartReg,
                                                    v0));
                                            mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction23x(
                                                    Opcode.XOR_INT,
                                                    minStartReg,
                                                    minStartReg,
                                                    minStartReg + 1));
                                            boolean isAnd = new Random().nextBoolean();
                                            if (isAnd)
                                                mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction23x(
                                                        Opcode.AND_INT,
                                                        minStartReg,
                                                        minStartReg + 1,
                                                        minStartReg));
                                            else
                                                mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction23x(
                                                        Opcode.REM_INT,
                                                        minStartReg,
                                                        minStartReg + 1,
                                                        minStartReg));
                                            //得到计算后的正确结果
                                            int value = isAnd ? v1 & (v0 ^ v1) : v1 % (v0 ^ v1);
                                            if (new Random().nextBoolean()) {
                                                mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction31i(
                                                        Opcode.CONST,
                                                        minStartReg + 1,
                                                        value));
                                                if (new Random().nextBoolean()) {
                                                    if (minStartReg > 15 || minStartReg + 1 > 15) {
                                                        if (value < 0)
                                                            mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction21t(
                                                                    Opcode.IF_LEZ,
                                                                    minStartReg,
                                                                    mutableImplementation.newLabelForIndex(i)));
                                                        else
                                                            mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction21t(
                                                                    Opcode.IF_GTZ,
                                                                    minStartReg,
                                                                    mutableImplementation.newLabelForIndex(i)));
                                                    } else {
                                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22t(
                                                                Opcode.IF_EQ,
                                                                minStartReg,
                                                                minStartReg + 1,
                                                                mutableImplementation.newLabelForIndex(i)));
                                                    }
                                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction10t(
                                                            Opcode.GOTO,
                                                            mutableImplementation.newLabelForIndex(i - 5)));
                                                } else {
                                                    if (minStartReg > 15 || minStartReg + 1 > 15)
                                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction10x(Opcode.NOP));
                                                    else
                                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22t(
                                                                Opcode.IF_NE,
                                                                minStartReg,
                                                                minStartReg + 1,
                                                                mutableImplementation.newLabelForIndex(i)));
                                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction10t(
                                                            Opcode.GOTO,
                                                            mutableImplementation.newLabelForIndex(i)));
                                                }
                                            } else {
                                                if (new Random().nextBoolean())
                                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction21t(
                                                            Opcode.IF_GTZ,
                                                            minStartReg,
                                                            mutableImplementation.newLabelForIndex(i)));
                                                else {
                                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction21t(
                                                            Opcode.IF_EQZ,
                                                            minStartReg,
                                                            mutableImplementation.newLabelForIndex(0)));
                                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction10t(
                                                            Opcode.GOTO,
                                                            mutableImplementation.newLabelForIndex(i)));
                                                }
                                                if (value > 0)
                                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction10t(
                                                            Opcode.GOTO,
                                                            mutableImplementation.newLabelForIndex(i - 4)));
                                                else
                                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction10t(
                                                            Opcode.GOTO,
                                                            mutableImplementation.newLabelForIndex(i)));
                                            }
                                        }
                                    }
                                    break;
                                }
                            }
                            methods.add(new ImmutableMethod(
                                    method.getDefiningClass(),
                                    method.getName(),
                                    method.getParameters(),
                                    method.getReturnType(),
                                    method.getAccessFlags(),
                                    method.getAnnotations(),
                                    method.getHiddenApiRestrictions(),
                                    new ImmutableMethodImplementation(
                                            mutableImplementation.getRegisterCount(),
                                            mutableImplementation.getInstructions(),
                                            mutableImplementation.getTryBlocks(),
                                            mutableImplementation.getDebugItems())));
                            if (hashMap.size() > 0)
                                map.put(fieldsName, hashMap);
                        } else
                            methods.add(method);
                    }
                    //插入field
                    {
                        if (isexistClinit) {
                            for (Method method : classDef.getMethods()) {
                                if (method.getName().equals("<clinit>")) {
                                    MutableMethodImplementation mutableMethodImplementation = new MutableMethodImplementation(method.getImplementation());
                                    for (Map.Entry<String, List<Number>> entry : map.entrySet()) {
                                        mutableMethodImplementation.addInstruction(0, new BuilderInstruction31i(Opcode.CONST, 0, entry.getValue().size()));
                                        mutableMethodImplementation.addInstruction(1, new BuilderInstruction22c(Opcode.NEW_ARRAY, 0, 0, new ImmutableTypeReference("[I")));
                                        mutableMethodImplementation.addInstruction(new BuilderArrayPayload(4, entry.getValue()));
                                        mutableMethodImplementation.addInstruction(2, new BuilderInstruction31t(Opcode.FILL_ARRAY_DATA, 0, mutableMethodImplementation.newLabelForIndex(mutableMethodImplementation.getInstructions().size() - 1)));
                                        mutableMethodImplementation.addInstruction(3, new BuilderInstruction21c(Opcode.SPUT_OBJECT, 0, new ImmutableFieldReference(classDef.getType(), entry.getKey(), "[I")));
                                    }
                                    methods.add(new ImmutableMethod(
                                            method.getDefiningClass(),
                                            method.getName(),
                                            method.getParameters(),
                                            method.getReturnType(),
                                            method.getAccessFlags(),
                                            method.getAnnotations(),
                                            method.getHiddenApiRestrictions(),
                                            new ImmutableMethodImplementation(
                                                    mutableMethodImplementation.getRegisterCount() == 0 ? 1 : mutableMethodImplementation.getRegisterCount(),
                                                    mutableMethodImplementation.getInstructions(),
                                                    mutableMethodImplementation.getTryBlocks(),
                                                    mutableMethodImplementation.getDebugItems())));
                                    break;
                                }
                            }
                        } else {
                            MutableMethodImplementation mutableMethodImplementation = new MutableMethodImplementation(1);
                            mutableMethodImplementation.addInstruction(new BuilderInstruction10x(Opcode.NOP));
                            mutableMethodImplementation.addInstruction(new BuilderInstruction10x(Opcode.RETURN_VOID));
                            for (Map.Entry<String, List<Number>> entry : map.entrySet()) {
                                mutableMethodImplementation.addInstruction(0, new BuilderInstruction31i(Opcode.CONST, 0, entry.getValue().size()));
                                mutableMethodImplementation.addInstruction(1, new BuilderInstruction22c(Opcode.NEW_ARRAY, 0, 0, new ImmutableTypeReference("[I")));
                                mutableMethodImplementation.addInstruction(new BuilderArrayPayload(4, entry.getValue()));
                                mutableMethodImplementation.addInstruction(2, new BuilderInstruction31t(Opcode.FILL_ARRAY_DATA, 0, mutableMethodImplementation.newLabelForIndex(mutableMethodImplementation.getInstructions().size() - 1)));
                                mutableMethodImplementation.addInstruction(3, new BuilderInstruction21c(Opcode.SPUT_OBJECT, 0, new ImmutableFieldReference(classDef.getType(), entry.getKey(), "[I")));
                            }
                            methods.add(new ImmutableMethod(
                                    classDef.getType(),
                                    "<clinit>",
                                    null,
                                    "V",
                                    AccessFlags.STATIC.getValue() | AccessFlags.CONSTRUCTOR.getValue(),
                                    null,
                                    null,
                                    new ImmutableMethodImplementation(1, mutableMethodImplementation.getInstructions(), null, null)));
                        }
                    }
                    List<Field> fields = Lists.newArrayList(classDef.getFields());
                    for (String key : map.keySet())
                        fields.add(new ImmutableField(classDef.getType(), key, "[I", AccessFlags.PRIVATE.getValue() | AccessFlags.STATIC.getValue(), null, null, null));
                    flow_total++;
                    return new ImmutableClassDef(
                            classDef.getType(),
                            classDef.getAccessFlags(),
                            classDef.getSuperclass(),
                            classDef.getInterfaces(),
                            classDef.getSourceFile(),
                            classDef.getAnnotations(),
                            fields,
                            methods);
                } else
                    return classDef;
            }
        };
    }

    @Override
    public void transform() throws Exception {
        flow_total = 0;
        diff.clear();
        if (configuration != null) {
            JsonArray separate = new JsonParser().parse(configuration).getAsJsonObject().getAsJsonArray(Long.toString(128));
            if (separate == null) return;
            for (JsonElement jsonElement : separate)
                diff.add(jsonElement.getAsString());
        }
    }

    @Override
    public String getResult() {
        return String.format(Objects.requireNonNull(SysConfigUtil.getLanguageConfigUtil(getLanguageEnums(), "class.flow")), flow_total);
    }

    @Override
    public int priority() {
        return 300;
    }

    @Override
    public int compareTo(BaseTransformer o) {
        return priority() - o.priority();
    }
}

