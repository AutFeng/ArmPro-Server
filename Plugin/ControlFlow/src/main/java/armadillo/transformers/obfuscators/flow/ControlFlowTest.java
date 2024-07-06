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

public class ControlFlowTest extends DexTransformer {
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
                    String fieldsName = StringRandom.RandomString();
                    List<Field> fields = Lists.newArrayList(classDef.getFields());
                    fields.add(new ImmutableField(
                            classDef.getType(),
                            fieldsName,
                            "[I",
                            AccessFlags.PRIVATE.getValue() | AccessFlags.STATIC.getValue(),
                            null,
                            null,
                            null));
                    List<Number> hashMap = new ArrayList<>();
                    /**
                     * 跳过接口/注解/枚举类
                     */
                    {
                        if (AccessFlags.INTERFACE.isSet(classDef.getAccessFlags())
                                || AccessFlags.ANNOTATION.isSet(classDef.getAccessFlags())
                                || AccessFlags.ENUM.isSet(classDef.getAccessFlags()))
                            return classDef;
                    }
                    /**
                     * 插入虚假控制流
                     */
                    for (Method method : classDef.getMethods()) {
                        /**
                         * 跳过<clinit>
                         */
                        if (method.getName().equals("<clinit>"))
                            continue;
                        else if (method.getImplementation() != null) {
                            /**
                             * 计算原始本地寄存器最大值
                             */
                            List<AccessFlags> accessFlags = Arrays.asList(AccessFlags.getAccessFlagsForMethod(method.getAccessFlags()));
                            int max_parameter = accessFlags.contains(AccessFlags.STATIC) ? 0 : 1;
                            max_parameter += method.getParameters().size();
                            for (MethodParameter parameter : method.getParameters()) {
                                switch (parameter.charAt(0)) {
                                    case 'J':
                                    case 'D':
                                        max_parameter++;
                                        break;
                                }
                            }
                            int max_local = method.getImplementation().getRegisterCount() - max_parameter;
                            int regIndex;
                            /**
                             * 修改寄存器总量
                             */
                            MutableMethodImplementation mutableImplementation;
                            if (method.getImplementation().getRegisterCount() >= 10) {
                                mutableImplementation = new MutableMethodImplementation(method.getImplementation());
                                regIndex = mutableImplementation.getRegisterCount() + max_parameter + 10;
                                /**
                                 * 非静态
                                 */
                                if (!accessFlags.contains(AccessFlags.STATIC)) {
                                    /**
                                     * this P0
                                     */
                                    {
                                        int src = max_local;
                                        int old = src + max_parameter + 10;
                                        mutableImplementation.addInstruction(0, new BuilderInstruction22x(Opcode.MOVE_OBJECT_FROM16, src, old));
                                    }
                                    /**
                                     * 参数寄存器
                                     */
                                    for (int size = 0; size < method.getParameters().size(); size++) {
                                        MethodParameter type = method.getParameters().get(size);
                                        int src = max_local + 1 + size;
                                        int old = src + max_parameter + 10;
                                        switch (type.getType()) {
                                            case "Z":
                                            case "B":
                                            case "S":
                                            case "C":
                                            case "I":
                                            case "F":
                                                mutableImplementation.addInstruction(0, new BuilderInstruction22x(Opcode.MOVE_FROM16, src, old));
                                                break;
                                            case "D":
                                            case "J":
                                                mutableImplementation.addInstruction(0, new BuilderInstruction22x(Opcode.MOVE_WIDE_FROM16, src, old));
                                                max_local++;
                                                break;
                                            default:
                                                mutableImplementation.addInstruction(0, new BuilderInstruction22x(Opcode.MOVE_OBJECT_FROM16, src, old));
                                                break;
                                        }
                                    }
                                }
                                /**
                                 * 静态
                                 */
                                else {
                                    for (int size = 0; size < method.getParameters().size(); size++) {
                                        MethodParameter type = method.getParameters().get(size);
                                        int src = max_local + size;
                                        int old = src + max_parameter + 10;
                                        switch (type.getType()) {
                                            case "Z":
                                            case "B":
                                            case "S":
                                            case "C":
                                            case "I":
                                            case "F":
                                                mutableImplementation.addInstruction(0, new BuilderInstruction22x(Opcode.MOVE_FROM16, src, old));
                                                break;
                                            case "D":
                                            case "J":
                                                mutableImplementation.addInstruction(0, new BuilderInstruction22x(Opcode.MOVE_WIDE_FROM16, src, old));
                                                max_local++;
                                                break;
                                            default:
                                                mutableImplementation.addInstruction(0, new BuilderInstruction22x(Opcode.MOVE_OBJECT_FROM16, src, old));
                                                break;
                                        }
                                    }
                                }
                            } else {
                                mutableImplementation = new MutableMethodImplementation(
                                        method.getImplementation(),
                                        method,
                                        method.getImplementation().getRegisterCount() + (15 - method.getImplementation().getRegisterCount()));
                                regIndex = 15;
                            }
                            /**
                             * 得到新的最大寄存器
                             */
                            int new_max_local = regIndex - max_parameter;
                            if (regIndex > 15)
                                max_local += max_parameter;

                            //TODO switch未兼容
                            {
                                for (int i = 0; i < mutableImplementation.getInstructions().size(); i++) {
                                    Instruction instruction = mutableImplementation.getInstructions().get(i);
                                    if (instruction.getOpcode() == Opcode.INVOKE_STATIC
                                            || instruction.getOpcode() == Opcode.INVOKE_VIRTUAL
                                            || instruction.getOpcode() == Opcode.INVOKE_DIRECT
                                            || instruction.getOpcode() == Opcode.INVOKE_SUPER
                                            || instruction.getOpcode() == Opcode.INVOKE_CUSTOM) {
                                        Instruction35c instruction35c = (Instruction35c) instruction;
                                        MethodReference reference = (MethodReference) instruction35c.getReference();
                                        if (reference.getReturnType().equals("V")) {
                                            /**
                                             * 原方法执行前
                                             */
                                            {
                                                if (i != 0) {
                                                    if (new Random().nextBoolean()) {
                                                        int k = 0;
                                                        do {
                                                            int v1 = new Random().nextInt(99999999) << i;
                                                            int v2 = new Random().nextInt(99999999) ^ i;
                                                            hashMap.add(v1);
                                                            hashMap.add(v2);
                                                            /**
                                                             * 得到V1
                                                             */
                                                            {
                                                                mutableImplementation.addInstruction(i, new BuilderInstruction21c(Opcode.SGET_OBJECT, max_local + 3, new ImmutableFieldReference(classDef.getType(), fieldsName, "[I")));
                                                                i++;
                                                                mutableImplementation.addInstruction(i, new BuilderInstruction31i(Opcode.CONST, max_local + 4, hashMap.size() - 2));
                                                                i++;
                                                                mutableImplementation.addInstruction(i, new BuilderInstruction23x(Opcode.AGET, max_local + 1, max_local + 3, max_local + 4));
                                                                i++;
                                                            }
                                                            /**
                                                             * 得到V2
                                                             */
                                                            {
                                                                mutableImplementation.addInstruction(i, new BuilderInstruction31i(Opcode.CONST, max_local + 4, hashMap.size() - 1));
                                                                i++;
                                                                mutableImplementation.addInstruction(i, new BuilderInstruction23x(Opcode.AGET, max_local + 2, max_local + 3, max_local + 4));
                                                                i++;
                                                            }
                                                            //v0 =v0 ^ v1
                                                            mutableImplementation.addInstruction(i, new BuilderInstruction23x(Opcode.XOR_INT, max_local + 1, max_local + 1, max_local + 2));
                                                            i++;
                                                            //v1 = v1 & v0
                                                            mutableImplementation.addInstruction(i, new BuilderInstruction23x(Opcode.AND_INT, max_local + 2, max_local + 2, max_local + 1));
                                                            i++;
                                                            /**
                                                             * v15 以上寄存器
                                                             */
                                                            if (max_local + 1 > 15 || max_local + 2 > 15) {
                                                                //v1 = v1 ^ v2
                                                                mutableImplementation.addInstruction(i, new BuilderInstruction23x(Opcode.XOR_INT, max_local + 1, max_local + 1, max_local + 2));
                                                                i++;
                                                                //占位指令
                                                                mutableImplementation.addInstruction(i, new BuilderInstruction10x(Opcode.NOP));
                                                                i++;
                                                                /**
                                                                 * 插入错误的跳转
                                                                 */
                                                                {
                                                                    //v0 = v0 + Random
                                                                    mutableImplementation.addInstruction(i, new BuilderInstruction23x(Opcode.ADD_INT, max_local + 1, max_local + 1, max_local + 2));
                                                                    i++;
                                                                    //if v0 == 0 Goto ++
                                                                    mutableImplementation.addInstruction(i, new BuilderInstruction21t(Opcode.IF_EQZ, max_local + 1, mutableImplementation.newLabelForIndex(i - 1)));
                                                                    i++;
                                                                    //goto 重新计算
                                                                    mutableImplementation.addInstruction(i, new BuilderInstruction10t(Opcode.GOTO, mutableImplementation.newLabelForIndex(i - 3)));
                                                                    i++;
                                                                    //NOP
                                                                    mutableImplementation.addInstruction(i, new BuilderInstruction10x(Opcode.NOP));
                                                                    i++;
                                                                }
                                                                mutableImplementation.addInstruction(i, new BuilderInstruction31t(
                                                                        Opcode.SPARSE_SWITCH,
                                                                        max_local + 1,
                                                                        mutableImplementation.newLabelForIndex(mutableImplementation.getInstructions().size())));
                                                                i++;
                                                                List<SwitchLabelElement> switchLabelElements = new ArrayList<>();
                                                                /**
                                                                 * 计算正确跳转值
                                                                 */
                                                                {
                                                                    v1 = v1 ^ v2;
                                                                    v2 = v2 & v1;
                                                                    v1 = v1 ^ v2;
                                                                    switchLabelElements.add(
                                                                            new SwitchLabelElement(
                                                                                    v1,
                                                                                    mutableImplementation.newLabelForIndex(i - 1)));
                                                                }
                                                                /**
                                                                 * 插入假的跳转节点
                                                                 */
                                                                {
                                                                    switchLabelElements.add(
                                                                            new SwitchLabelElement(
                                                                                    v1 | v2 + new Random().nextInt(9999),
                                                                                    mutableImplementation.newLabelForIndex(i - 6)));
                                                                }
                                                                /**
                                                                 * 插入switch结构体
                                                                 */
                                                                {
                                                                    mutableImplementation.addInstruction(new BuilderSparseSwitchPayload(switchLabelElements));
                                                                }
                                                                mutableImplementation.swapInstructions(i - 6, i - 1);
                                                                k++;
                                                            }
                                                            /**
                                                             * v15 以下寄存器
                                                             */
                                                            else {
                                                                /**
                                                                 * if模式
                                                                 */
                                                                //跳转到下层
                                                                if (k != 0) {
                                                                    mutableImplementation.addInstruction(i - 10, new BuilderInstruction10t(Opcode.GOTO, mutableImplementation.newLabelForIndex(i - 5)));
                                                                    i++;
                                                                }
                                                                //if(v0 != v1) goto 正确地址
                                                                mutableImplementation.addInstruction(i, new BuilderInstruction22t(Opcode.IF_NE, max_local + 1, max_local + 2, mutableImplementation.newLabelForIndex(k != 0 ? i - 10 : i)));
                                                                i++;
                                                                //v0 = v0 + Random
                                                                mutableImplementation.addInstruction(i, new BuilderInstruction22s(Opcode.ADD_INT_LIT16, max_local + 1, max_local + 1, new Random().nextInt(32767)));
                                                                i++;
                                                                //if v0 == 0 Goto ++
                                                                mutableImplementation.addInstruction(i, new BuilderInstruction21t(Opcode.IF_EQZ, max_local + 1, mutableImplementation.newLabelForIndex(i - 1)));
                                                                i++;
                                                                //goto 重新计算
                                                                mutableImplementation.addInstruction(i, new BuilderInstruction10t(Opcode.GOTO, mutableImplementation.newLabelForIndex(i - 5)));
                                                                i++;
                                                                //NOP
                                                                mutableImplementation.addInstruction(i, new BuilderInstruction10x(Opcode.NOP));
                                                                i++;
                                                                //假跳转
                                                                mutableImplementation.addInstruction(i, new BuilderInstruction21t(Opcode.IF_LTZ, max_local + 1, mutableImplementation.newLabelForIndex(i - 2)));
                                                                i++;
                                                                //交换
                                                                mutableImplementation.swapInstructions(i - 1, i - 2);
                                                                k++;
                                                            }
                                                        } while (k < 2);
                                                    } else {
                                                        int k = 0;
                                                        do {
                                                            int v1 = new Random().nextInt(99999999) << i;
                                                            int v2 = new Random().nextInt(99999999) ^ i;
                                                            hashMap.add(v1);
                                                            hashMap.add(v2);
                                                            /**
                                                             * 得到V1
                                                             */
                                                            {
                                                                mutableImplementation.addInstruction(i, new BuilderInstruction21c(Opcode.SGET_OBJECT, max_local + 3, new ImmutableFieldReference(classDef.getType(), fieldsName, "[I")));
                                                                i++;
                                                                mutableImplementation.addInstruction(i, new BuilderInstruction31i(Opcode.CONST, max_local + 4, hashMap.size() - 2));
                                                                i++;
                                                                mutableImplementation.addInstruction(i, new BuilderInstruction23x(Opcode.AGET, max_local + 1, max_local + 3, max_local + 4));
                                                                i++;
                                                            }
                                                            /**
                                                             * 得到V2
                                                             */
                                                            {
                                                                mutableImplementation.addInstruction(i, new BuilderInstruction31i(Opcode.CONST, max_local + 4, hashMap.size() - 1));
                                                                i++;
                                                                mutableImplementation.addInstruction(i, new BuilderInstruction23x(Opcode.AGET, max_local + 2, max_local + 3, max_local + 4));
                                                                i++;
                                                            }
                                                            //v0 =v0 ^ v1
                                                            mutableImplementation.addInstruction(i, new BuilderInstruction23x(Opcode.XOR_INT, max_local + 1, max_local + 1, max_local + 2));
                                                            i++;
                                                            //v1 = v1 & v0
                                                            mutableImplementation.addInstruction(i, new BuilderInstruction23x(Opcode.AND_INT, max_local + 2, max_local + 2, max_local + 1));
                                                            i++;
                                                            /**
                                                             * v15 以上寄存器
                                                             */
                                                            if (max_local + 1 > 15 || max_local + 2 > 15) {
                                                                //v1 = v1 ^ v2
                                                                mutableImplementation.addInstruction(i, new BuilderInstruction23x(Opcode.XOR_INT, max_local + 1, max_local + 1, max_local + 2));
                                                                i++;
                                                                //占位指令
                                                                mutableImplementation.addInstruction(i, new BuilderInstruction10x(Opcode.NOP));
                                                                i++;
                                                                /**
                                                                 * 插入错误的跳转
                                                                 */
                                                                {
                                                                    //v0 = v0 + Random
                                                                    mutableImplementation.addInstruction(i, new BuilderInstruction23x(Opcode.ADD_INT, max_local + 1, max_local + 1, max_local + 2));
                                                                    i++;
                                                                    //if v0 == 0 Goto ++
                                                                    mutableImplementation.addInstruction(i, new BuilderInstruction21t(Opcode.IF_EQZ, max_local + 1, mutableImplementation.newLabelForIndex(i - 1)));
                                                                    i++;
                                                                    //goto 重新计算
                                                                    mutableImplementation.addInstruction(i, new BuilderInstruction10t(Opcode.GOTO, mutableImplementation.newLabelForIndex(i - 3)));
                                                                    i++;
                                                                    //NOP
                                                                    mutableImplementation.addInstruction(i, new BuilderInstruction10x(Opcode.NOP));
                                                                    i++;
                                                                }
                                                                mutableImplementation.addInstruction(i, new BuilderInstruction31t(
                                                                        Opcode.SPARSE_SWITCH,
                                                                        max_local + 1,
                                                                        mutableImplementation.newLabelForIndex(mutableImplementation.getInstructions().size())));
                                                                i++;
                                                                List<SwitchLabelElement> switchLabelElements = new ArrayList<>();
                                                                /**
                                                                 * 计算正确跳转值
                                                                 */
                                                                {
                                                                    v1 = v1 ^ v2;
                                                                    v2 = v2 & v1;
                                                                    v1 = v1 ^ v2;
                                                                    switchLabelElements.add(
                                                                            new SwitchLabelElement(
                                                                                    v1,
                                                                                    mutableImplementation.newLabelForIndex(i - 1)));
                                                                }
                                                                /**
                                                                 * 插入假的跳转节点
                                                                 */
                                                                {
                                                                    switchLabelElements.add(
                                                                            new SwitchLabelElement(
                                                                                    v1 | v2 + new Random().nextInt(9999),
                                                                                    mutableImplementation.newLabelForIndex(i - 6)));
                                                                }
                                                                /**
                                                                 * 插入switch结构体
                                                                 */
                                                                {
                                                                    mutableImplementation.addInstruction(new BuilderSparseSwitchPayload(switchLabelElements));
                                                                }
                                                                mutableImplementation.swapInstructions(i - 6, i - 1);
                                                                k++;
                                                            }
                                                            /**
                                                             * v15 以下寄存器
                                                             */
                                                            else {
                                                                /**
                                                                 * switch模式
                                                                 */
                                                                //v1 = v1 ^ v2
                                                                mutableImplementation.addInstruction(i, new BuilderInstruction23x(Opcode.XOR_INT, max_local + 1, max_local + 1, max_local + 2));
                                                                i++;
                                                                //占位指令
                                                                mutableImplementation.addInstruction(i, new BuilderInstruction10x(Opcode.NOP));
                                                                i++;
                                                                /**
                                                                 * 插入错误的跳转
                                                                 */
                                                                {
                                                                    //v0 = v0 + Random
                                                                    mutableImplementation.addInstruction(i, new BuilderInstruction23x(Opcode.ADD_INT, max_local + 1, max_local + 1, max_local + 2));
                                                                    i++;
                                                                    //if v0 == 0 Goto ++
                                                                    mutableImplementation.addInstruction(i, new BuilderInstruction21t(Opcode.IF_EQZ, max_local + 1, mutableImplementation.newLabelForIndex(i - 1)));
                                                                    i++;
                                                                    //goto 重新计算
                                                                    mutableImplementation.addInstruction(i, new BuilderInstruction10t(Opcode.GOTO, mutableImplementation.newLabelForIndex(i - 3)));
                                                                    i++;
                                                                    //NOP
                                                                    mutableImplementation.addInstruction(i, new BuilderInstruction10x(Opcode.NOP));
                                                                    i++;
                                                                }
                                                                mutableImplementation.addInstruction(i, new BuilderInstruction31t(
                                                                        Opcode.SPARSE_SWITCH,
                                                                        max_local + 1,
                                                                        mutableImplementation.newLabelForIndex(mutableImplementation.getInstructions().size())));
                                                                i++;
                                                                List<SwitchLabelElement> switchLabelElements = new ArrayList<>();
                                                                /**
                                                                 * 计算正确跳转值
                                                                 */
                                                                {
                                                                    v1 = v1 ^ v2;
                                                                    v2 = v2 & v1;
                                                                    v1 = v1 ^ v2;
                                                                    switchLabelElements.add(
                                                                            new SwitchLabelElement(
                                                                                    v1,
                                                                                    mutableImplementation.newLabelForIndex(i - 1)));
                                                                }
                                                                /**
                                                                 * 插入假的跳转节点
                                                                 */
                                                                {
                                                                    switchLabelElements.add(
                                                                            new SwitchLabelElement(
                                                                                    v1 | v2 + new Random().nextInt(9999),
                                                                                    mutableImplementation.newLabelForIndex(i - 6)));
                                                                }
                                                                /**
                                                                 * 插入switch结构体
                                                                 */
                                                                {
                                                                    mutableImplementation.addInstruction(new BuilderSparseSwitchPayload(switchLabelElements));
                                                                }
                                                                mutableImplementation.swapInstructions(i - 6, i - 1);
                                                                k++;
                                                            }
                                                        } while (k < 2);
                                                    }
                                                }
                                            }
                                            /**
                                             * 原方法执行后
                                             */
                                            {
                                                int hash = -(new Random().nextInt(999999) +
                                                        new Random().nextInt(999999) +
                                                        new Random().nextInt(999999));
                                                int v1 = new Random().nextInt(99999999) << i;
                                                int v2 = new Random().nextInt(99999999) ^ i;
                                                hashMap.add(v1);
                                                hashMap.add(v2);
                                                /**
                                                 * 得到V1
                                                 */
                                                {
                                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction21c(Opcode.SGET_OBJECT, max_local + 3, new ImmutableFieldReference(classDef.getType(), fieldsName, "[I")));
                                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction31i(Opcode.CONST, max_local + 4, hashMap.size() - 2));
                                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction23x(Opcode.AGET, max_local + 1, max_local + 3, max_local + 4));
                                                }
                                                /**
                                                 * 得到V2
                                                 */
                                                {
                                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction31i(Opcode.CONST, max_local + 4, hashMap.size() - 1));
                                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction23x(Opcode.AGET, max_local + 2, max_local + 3, max_local + 4));
                                                }
                                                mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction21t(Opcode.IF_EQZ, max_local + 3, mutableImplementation.newLabelForIndex(i - 5)));
                                                mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction23x(Opcode.XOR_INT, max_local + 1, max_local + 1, max_local + 2));
                                                mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction21t(new Random().nextBoolean() ? Opcode.IF_NEZ : Opcode.IF_EQZ, max_local + 1, mutableImplementation.newLabelForIndex(i)));
                                                //const- class v0,Larmadillo/call / test;
                                                {
                                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction21c(
                                                            Opcode.CONST_CLASS,
                                                            max_local + 1,
                                                            new ImmutableTypeReference(method.getDefiningClass())));
                                                }
                                                //invoke-virtual {v0}, Ljava/lang/Class;->getName()Ljava/lang/String;
                                                {
                                                    if (max_local + 1 > 15)
                                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction3rc(
                                                                Opcode.INVOKE_VIRTUAL_RANGE,
                                                                max_local + 1,
                                                                1,
                                                                new ImmutableMethodReference(
                                                                        "Ljava/lang/Class;",
                                                                        "getName",
                                                                        null,
                                                                        "Ljava/lang/String;"
                                                                )));
                                                    else
                                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction35c(
                                                                Opcode.INVOKE_VIRTUAL,
                                                                1,
                                                                max_local + 1,
                                                                0,
                                                                0,
                                                                0,
                                                                0,
                                                                new ImmutableMethodReference(
                                                                        "Ljava/lang/Class;",
                                                                        "getName",
                                                                        null,
                                                                        "Ljava/lang/String;"
                                                                )));
                                                }
                                                //move-result-object v0
                                                {
                                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction11x(
                                                            Opcode.MOVE_RESULT_OBJECT,
                                                            max_local + 1));
                                                }
                                                //invoke-virtual {v0}, Ljava/lang/String;->hashCode()I
                                                {
                                                    if (max_local + 1 > 15)
                                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction3rc(
                                                                Opcode.INVOKE_VIRTUAL_RANGE,
                                                                max_local + 1,
                                                                1,
                                                                new ImmutableMethodReference(
                                                                        "Ljava/lang/String;",
                                                                        "hashCode",
                                                                        null,
                                                                        "I"
                                                                )));
                                                    else
                                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction35c(
                                                                Opcode.INVOKE_VIRTUAL,
                                                                1,
                                                                max_local + 1,
                                                                0,
                                                                0,
                                                                0,
                                                                0,
                                                                new ImmutableMethodReference(
                                                                        "Ljava/lang/String;",
                                                                        "hashCode",
                                                                        null,
                                                                        "I"
                                                                )));
                                                }
                                                //move-result v0
                                                {
                                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction11x(
                                                            Opcode.MOVE_RESULT,
                                                            max_local + 1));
                                                }
                                                //const/16 v1, 0x9999
                                                {
                                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction31i(Opcode.CONST,
                                                            max_local + 2,
                                                            hash));
                                                }
                                                //xor-int v0, v0, v1
                                                {
                                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction23x(
                                                            Opcode.XOR_INT,
                                                            max_local + 1,
                                                            max_local + 1,
                                                            max_local + 2));
                                                }
                                                /**
                                                 * 占位
                                                 */
                                                {
                                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction10x(Opcode.NOP));
                                                }
                                                //sparse-switch v0, :sswitch_data_24
                                                {
                                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction31t(
                                                            Opcode.SPARSE_SWITCH,
                                                            max_local + 1,
                                                            mutableImplementation.newLabelForIndex(mutableImplementation.getInstructions().size())));
                                                }
                                                int key = dexToJavaName(method.getDefiningClass()).hashCode() ^ hash;
                                                List<SwitchLabelElement> switchLabelElements = new ArrayList<>();
                                                /**
                                                 * 正确的跳转
                                                 */
                                                switchLabelElements.add(
                                                        new SwitchLabelElement(
                                                                key,
                                                                mutableImplementation.newLabelForIndex(i + 1)));
                                                /**
                                                 * 虚假的跳转
                                                 */
                                                for (int k = 0; k < new Random().nextInt(10) + 1; k++) {
                                                    int codeIndex1 = Integer.toHexString(key * new Random().nextInt() + 1).hashCode();
                                                    int codeIndex2 = Integer.toHexString(key * new Random().nextInt() + 1).hashCode();
                                                    int codeIndex3 = Integer.toHexString(key * new Random().nextInt() + 1).hashCode();
                                                    switchLabelElements.add(
                                                            new SwitchLabelElement(
                                                                    codeIndex1,
                                                                    mutableImplementation.newLabelForIndex(i - 8)));
                                                    switchLabelElements.add(
                                                            new SwitchLabelElement(
                                                                    codeIndex2,
                                                                    mutableImplementation.newLabelForIndex(i - 16)));
                                                }
                                                mutableImplementation.addInstruction(new BuilderSparseSwitchPayload(switchLabelElements));
                                                mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction10x(Opcode.NOP));
                                                /**
                                                 * 虚假的跳转
                                                 */
                                                for (int k = 0; k < new Random().nextInt(10) + 1; k++) {
                                                    switchLabelElements.add(
                                                            new SwitchLabelElement(
                                                                    Integer.toHexString(key * new Random().nextInt() + 1).hashCode(),
                                                                    mutableImplementation.newLabelForIndex(i)));
                                                }
                                                /**
                                                 * 得到V1
                                                 */
                                                {
                                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction21c(Opcode.SGET_OBJECT, max_local + 3, new ImmutableFieldReference(classDef.getType(), fieldsName, "[I")));
                                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction31i(Opcode.CONST, max_local + 4, hashMap.size() - 2));
                                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction23x(Opcode.AGET, max_local + 1, max_local + 3, max_local + 4));
                                                }
                                                /**
                                                 * 得到V2
                                                 */
                                                {
                                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction31i(Opcode.CONST, max_local + 4, hashMap.size() - 1));
                                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction23x(Opcode.AGET, max_local + 2, max_local + 3, max_local + 4));
                                                }
                                                mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction21t(Opcode.IF_NEZ, max_local + 1, mutableImplementation.newLabelForIndex(i)));
                                                mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction23x(Opcode.XOR_INT, max_local + 1, max_local + 1, max_local + 2));
                                                mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction21t(Opcode.IF_EQZ, max_local + 1, mutableImplementation.newLabelForIndex(i)));
                                                for (int k = -1; k < 3; k++) {
                                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction10x(Opcode.NOP));
                                                }
                                                mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction21t(Opcode.IF_LEZ, max_local + 1, mutableImplementation.newLabelForIndex(i)));
                                                mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction21t(Opcode.IF_EQZ, max_local + 1, mutableImplementation.newLabelForIndex(i - 2)));
                                                {
                                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction31i(Opcode.CONST,
                                                            max_local + 1,
                                                            dexToJavaName(method.getDefiningClass()).hashCode()));
                                                }
                                                /**
                                                 * 抛出异常
                                                 */
                                                mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction21c(Opcode.NEW_INSTANCE, max_local + 1, new ImmutableTypeReference("Ljava/lang/IllegalArgumentException;")));
                                                if (max_local + 1 > 15)
                                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction3rc(Opcode.INVOKE_DIRECT_RANGE, max_local + 1, 1, new ImmutableMethodReference("Ljava/lang/IllegalArgumentException;", "<init>", null, "V")));
                                                else
                                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction35c(Opcode.INVOKE_DIRECT, 1, max_local + 1, 0, 0, 0, 0, new ImmutableMethodReference("Ljava/lang/IllegalArgumentException;", "<init>", null, "V")));
                                                mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction11x(Opcode.THROW, max_local + 1));
                                            }
                                        }
                                    }
                                    //其他
                                    else if (instruction.getOpcode() == Opcode.NEW_INSTANCE
                                            || instruction.getOpcode() == Opcode.MOVE_RESULT_OBJECT
                                            || instruction.getOpcode() == Opcode.MOVE_RESULT
                                            || instruction.getOpcode() == Opcode.MOVE_RESULT_WIDE
                                            || instruction.getOpcode() == Opcode.MOVE
                                            || instruction.getOpcode() == Opcode.MOVE_16
                                            || instruction.getOpcode() == Opcode.MOVE_FROM16
                                            || instruction.getOpcode() == Opcode.MOVE_EXCEPTION
                                            || instruction.getOpcode() == Opcode.MOVE_OBJECT_16
                                            || instruction.getOpcode() == Opcode.MOVE_OBJECT_FROM16
                                            || instruction.getOpcode() == Opcode.IGET_OBJECT
                                            || instruction.getOpcode() == Opcode.IPUT_OBJECT
                                            || instruction.getOpcode() == Opcode.CONST
                                            || instruction.getOpcode() == Opcode.CONST_HIGH16
                                            || instruction.getOpcode() == Opcode.CONST_STRING) {
                                        /**
                                         * 原方法执行后
                                         */
                                        {
                                            int hash = -(new Random().nextInt(999999) +
                                                    new Random().nextInt(999999) +
                                                    new Random().nextInt(999999));
                                            int v1 = new Random().nextInt(99999999) << i;
                                            int v2 = new Random().nextInt(99999999) ^ i;
                                            hashMap.add(v1);
                                            hashMap.add(v2);
                                            /**
                                             * 得到V1
                                             */
                                            {
                                                mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction21c(Opcode.SGET_OBJECT, max_local + 3, new ImmutableFieldReference(classDef.getType(), fieldsName, "[I")));
                                                mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction31i(Opcode.CONST, max_local + 4, hashMap.size() - 2));
                                                mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction23x(Opcode.AGET, max_local + 1, max_local + 3, max_local + 4));
                                            }
                                            /**
                                             * 得到V2
                                             */
                                            {
                                                mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction31i(Opcode.CONST, max_local + 4, hashMap.size() - 1));
                                                mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction23x(Opcode.AGET, max_local + 2, max_local + 3, max_local + 4));
                                            }
                                            mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction21t(Opcode.IF_EQZ, max_local + 3, mutableImplementation.newLabelForIndex(i - 5)));
                                            mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction23x(Opcode.XOR_INT, max_local + 1, max_local + 1, max_local + 2));
                                            mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction21t(new Random().nextBoolean() ? Opcode.IF_NEZ : Opcode.IF_EQZ, max_local + 1, mutableImplementation.newLabelForIndex(i)));
                                            //const- class v0,Larmadillo/call / test;
                                            {
                                                mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction21c(
                                                        Opcode.CONST_CLASS,
                                                        max_local + 1,
                                                        new ImmutableTypeReference(method.getDefiningClass())));
                                            }
                                            //invoke-virtual {v0}, Ljava/lang/Class;->getName()Ljava/lang/String;
                                            {
                                                if (max_local + 1 > 15)
                                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction3rc(
                                                            Opcode.INVOKE_VIRTUAL_RANGE,
                                                            max_local + 1,
                                                            1,
                                                            new ImmutableMethodReference(
                                                                    "Ljava/lang/Class;",
                                                                    "getName",
                                                                    null,
                                                                    "Ljava/lang/String;"
                                                            )));
                                                else
                                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction35c(
                                                            Opcode.INVOKE_VIRTUAL,
                                                            1,
                                                            max_local + 1,
                                                            0,
                                                            0,
                                                            0,
                                                            0,
                                                            new ImmutableMethodReference(
                                                                    "Ljava/lang/Class;",
                                                                    "getName",
                                                                    null,
                                                                    "Ljava/lang/String;"
                                                            )));
                                            }
                                            //move-result-object v0
                                            {
                                                mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction11x(
                                                        Opcode.MOVE_RESULT_OBJECT,
                                                        max_local + 1));
                                            }
                                            //invoke-virtual {v0}, Ljava/lang/String;->hashCode()I
                                            {
                                                if (max_local + 1 > 15)
                                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction3rc(
                                                            Opcode.INVOKE_VIRTUAL_RANGE,
                                                            max_local + 1,
                                                            1,
                                                            new ImmutableMethodReference(
                                                                    "Ljava/lang/String;",
                                                                    "hashCode",
                                                                    null,
                                                                    "I"
                                                            )));
                                                else
                                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction35c(
                                                            Opcode.INVOKE_VIRTUAL,
                                                            1,
                                                            max_local + 1,
                                                            0,
                                                            0,
                                                            0,
                                                            0,
                                                            new ImmutableMethodReference(
                                                                    "Ljava/lang/String;",
                                                                    "hashCode",
                                                                    null,
                                                                    "I"
                                                            )));
                                            }
                                            //move-result v0
                                            {
                                                mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction11x(
                                                        Opcode.MOVE_RESULT,
                                                        max_local + 1));
                                            }
                                            //const/16 v1, 0x9999
                                            {
                                                mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction31i(Opcode.CONST,
                                                        max_local + 2,
                                                        hash));
                                            }
                                            //xor-int v0, v0, v1
                                            {
                                                mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction23x(
                                                        Opcode.XOR_INT,
                                                        max_local + 1,
                                                        max_local + 1,
                                                        max_local + 2));
                                            }
                                            /**
                                             * 占位
                                             */
                                            {
                                                mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction10x(Opcode.NOP));
                                            }
                                            //sparse-switch v0, :sswitch_data_24
                                            {
                                                mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction31t(
                                                        Opcode.SPARSE_SWITCH,
                                                        max_local + 1,
                                                        mutableImplementation.newLabelForIndex(mutableImplementation.getInstructions().size())));
                                            }
                                            int key = dexToJavaName(method.getDefiningClass()).hashCode() ^ hash;
                                            List<SwitchLabelElement> switchLabelElements = new ArrayList<>();
                                            /**
                                             * 正确的跳转
                                             */
                                            switchLabelElements.add(
                                                    new SwitchLabelElement(
                                                            key,
                                                            mutableImplementation.newLabelForIndex(i + 1)));
                                            /**
                                             * 虚假的跳转
                                             */
                                            for (int k = 0; k < new Random().nextInt(10) + 1; k++) {
                                                int codeIndex1 = Integer.toHexString(key * new Random().nextInt() + 1).hashCode();
                                                int codeIndex2 = Integer.toHexString(key * new Random().nextInt() + 1).hashCode();
                                                int codeIndex3 = Integer.toHexString(key * new Random().nextInt() + 1).hashCode();
                                                switchLabelElements.add(
                                                        new SwitchLabelElement(
                                                                codeIndex1,
                                                                mutableImplementation.newLabelForIndex(i - 8)));
                                                switchLabelElements.add(
                                                        new SwitchLabelElement(
                                                                codeIndex2,
                                                                mutableImplementation.newLabelForIndex(i - 16)));
                                            }
                                            mutableImplementation.addInstruction(new BuilderSparseSwitchPayload(switchLabelElements));
                                            mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction10x(Opcode.NOP));
                                            /**
                                             * 虚假的跳转
                                             */
                                            for (int k = 0; k < new Random().nextInt(10) + 1; k++) {
                                                switchLabelElements.add(
                                                        new SwitchLabelElement(
                                                                Integer.toHexString(key * new Random().nextInt() + 1).hashCode(),
                                                                mutableImplementation.newLabelForIndex(i)));
                                            }
                                            /**
                                             * 得到V1
                                             */
                                            {
                                                mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction21c(Opcode.SGET_OBJECT, max_local + 3, new ImmutableFieldReference(classDef.getType(), fieldsName, "[I")));
                                                mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction31i(Opcode.CONST, max_local + 4, hashMap.size() - 2));
                                                mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction23x(Opcode.AGET, max_local + 1, max_local + 3, max_local + 4));
                                            }
                                            /**
                                             * 得到V2
                                             */
                                            {
                                                mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction31i(Opcode.CONST, max_local + 4, hashMap.size() - 1));
                                                mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction23x(Opcode.AGET, max_local + 2, max_local + 3, max_local + 4));
                                            }
                                            mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction21t(Opcode.IF_NEZ, max_local + 1, mutableImplementation.newLabelForIndex(i)));
                                            mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction23x(Opcode.XOR_INT, max_local + 1, max_local + 1, max_local + 2));
                                            mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction21t(Opcode.IF_EQZ, max_local + 1, mutableImplementation.newLabelForIndex(i)));
                                            for (int k = -1; k < 3; k++) {
                                                mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction10x(Opcode.NOP));
                                            }
                                            mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction21t(Opcode.IF_LEZ, max_local + 1, mutableImplementation.newLabelForIndex(i)));
                                            mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction21t(Opcode.IF_EQZ, max_local + 1, mutableImplementation.newLabelForIndex(i - 2)));
                                            {
                                                mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction31i(Opcode.CONST,
                                                        max_local + 1,
                                                        dexToJavaName(method.getDefiningClass()).hashCode()));
                                            }
                                            /**
                                             * 抛出异常
                                             */
                                            mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction21c(Opcode.NEW_INSTANCE, max_local + 1, new ImmutableTypeReference("Ljava/lang/IllegalArgumentException;")));
                                            if (max_local + 1 > 15)
                                                mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction3rc(Opcode.INVOKE_DIRECT_RANGE, max_local + 1, 1, new ImmutableMethodReference("Ljava/lang/IllegalArgumentException;", "<init>", null, "V")));
                                            else
                                                mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction35c(Opcode.INVOKE_DIRECT, 1, max_local + 1, 0, 0, 0, 0, new ImmutableMethodReference("Ljava/lang/IllegalArgumentException;", "<init>", null, "V")));
                                            mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction11x(Opcode.THROW, max_local + 1));
                                        }


                                        /**
                                         * 原方法执行后
                                         */
                                /*{
                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction31i(Opcode.CONST, max_local + 1, 0x255 << i));
                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction31i(Opcode.CONST, max_local + 2, 0x999 ^ 0x255));
                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction21t(Opcode.IF_EQZ, max_local + 1, mutableImplementation.newLabelForIndex(i - 2)));
                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction23x(Opcode.XOR_INT, max_local + 1, max_local + 1, max_local + 1));
                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22t(Opcode.IF_NE, max_local + 1, max_local + 2, mutableImplementation.newLabelForIndex(i)));
                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction10t(Opcode.GOTO, mutableImplementation.newLabelForIndex(i - 2)));
                                    //const- class v0,Larmadillo/call / test;
                                    {
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction21c(
                                                Opcode.CONST_CLASS,
                                                max_local + 1,
                                                new ImmutableTypeReference(method.getDefiningClass())));
                                    }
                                    //invoke-virtual {v0}, Ljava/lang/Class;->getName()Ljava/lang/String;
                                    {
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction35c(
                                                Opcode.INVOKE_VIRTUAL,
                                                1,
                                                max_local + 1,
                                                0,
                                                0,
                                                0,
                                                0,
                                                new ImmutableMethodReference(
                                                        "Ljava/lang/Class;",
                                                        "getName",
                                                        null,
                                                        "Ljava/lang/String;"
                                                )));
                                    }
                                    //move-result-object v0
                                    {
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction11x(
                                                Opcode.MOVE_RESULT_OBJECT,
                                                max_local + 1));
                                    }
                                    //invoke-virtual {v0}, Ljava/lang/String;->hashCode()I
                                    {
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction35c(
                                                Opcode.INVOKE_VIRTUAL,
                                                1,
                                                max_local + 1,
                                                0,
                                                0,
                                                0,
                                                0,
                                                new ImmutableMethodReference(
                                                        "Ljava/lang/String;",
                                                        "hashCode",
                                                        null,
                                                        "I"
                                                )));
                                    }
                                    //move-result v0
                                    {
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction11x(
                                                Opcode.MOVE_RESULT,
                                                max_local + 1));
                                    }
                                    //const/16 v1, 0x9999
                                    {
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction31i(Opcode.CONST,
                                                max_local + 2,
                                                hash));
                                    }
                                    //xor-int/2addr v0, v1
                                    {
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction12x(
                                                Opcode.XOR_INT_2ADDR,
                                                max_local + 1,
                                                max_local + 2));
                                    }
                                    //sparse-switch v0, :sswitch_data_24
                                    {
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction31t(
                                                Opcode.SPARSE_SWITCH,
                                                max_local + 1,
                                                mutableImplementation.newLabelForIndex(mutableImplementation.getInstructions().size())));
                                    }
                                    int key2 = 0x255 << i;
                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction31i(Opcode.CONST, max_local + 2, key2));
                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22t(
                                            Opcode.IF_NE,
                                            max_local + 1,
                                            max_local + 2,
                                            mutableImplementation.newLabelForIndex(i - 4)));
                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction12x(
                                            Opcode.XOR_INT_2ADDR,
                                            max_local + 1,
                                            max_local + 2));
                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction21t(
                                            Opcode.IF_NEZ,
                                            max_local + 1,
                                            mutableImplementation.newLabelForIndex(i)));
                                    int key = dexToJavaName(method.getDefiningClass()).hashCode() ^ hash;
                                    List<SwitchLabelElement> switchLabelElements = new ArrayList<>();
                                    //跳转正确的
                                    switchLabelElements.add(
                                            new SwitchLabelElement(
                                                    key,
                                                    mutableImplementation.newLabelForIndex(i)));
                                    for (int k = 0; k < new Random().nextInt(10) + 1; k++) {
                                        int codeIndex1 = Integer.toHexString(key * new Random().nextInt() + 1).hashCode();
                                        int codeIndex2 = Integer.toHexString(key * new Random().nextInt() + 1).hashCode();
                                        int codeIndex3 = Integer.toHexString(key * new Random().nextInt() + 1).hashCode();
                                        switchLabelElements.add(
                                                new SwitchLabelElement(
                                                        codeIndex1,
                                                        mutableImplementation.newLabelForIndex(i - 1)));
                                        switchLabelElements.add(
                                                new SwitchLabelElement(
                                                        codeIndex2,
                                                        mutableImplementation.newLabelForIndex(i + 1)));
                                    }
                                    mutableImplementation.addInstruction(new BuilderSparseSwitchPayload(switchLabelElements));
                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction31i(Opcode.CONST, max_local + 1, 0x255 << i));
                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction21t(Opcode.IF_NEZ, max_local + 1, mutableImplementation.newLabelForIndex(i)));
                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction23x(Opcode.XOR_INT, max_local + 1, max_local + 1, max_local + 1));
                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction10t(Opcode.GOTO, mutableImplementation.newLabelForIndex(i)));
                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction21t(Opcode.IF_EQZ, max_local + 1, mutableImplementation.newLabelForIndex(i)));
                                    for (int k = -1; k < 3; k++) {
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction10x(Opcode.NOP));
                                    }
                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction31i(Opcode.CONST, max_local + 1, 0x255 << i));
                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction21t(Opcode.IF_LEZ, max_local + 1, mutableImplementation.newLabelForIndex(i)));
                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction21t(Opcode.IF_EQZ, max_local + 1, mutableImplementation.newLabelForIndex(i - 2)));
                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction10t(Opcode.GOTO, mutableImplementation.newLabelForIndex(i - 1)));
                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction11x(Opcode.THROW, max_local + 1));
                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction31i(Opcode.CONST, max_local + 1, 0x255 ^ i << i));
                                    {
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction31i(Opcode.CONST,
                                                max_local + 1,
                                                dexToJavaName(method.getDefiningClass()).hashCode()));
                                    }
                                }*/

//                                //方法前
//                                if (i != 0) {
//                                    Instruction beforeInstruction = mutableImplementation.getInstructions().get(i - 1);
//                                    if ((beforeInstruction.getOpcode() == Opcode.INVOKE_VIRTUAL
//                                            || beforeInstruction.getOpcode() == Opcode.INVOKE_STATIC
//                                            || beforeInstruction.getOpcode() == Opcode.INVOKE_SUPER
//                                            || beforeInstruction.getOpcode() == Opcode.INVOKE_CUSTOM
//                                            || beforeInstruction.getOpcode() == Opcode.INVOKE_DIRECT)
//                                            && ((MethodReference) ((Instruction35c) beforeInstruction).getReference()).getReturnType().equals("V")) {
//                                        //方法前
//                                        {
//                                            int k = 0;
//                                            do {
//                                                //const v0 xxxx
//                                                mutableImplementation.addInstruction(i, new BuilderInstruction31i(Opcode.CONST, max_local + 1, 0x255 << i));
//                                                i++;
//                                                //const v1 xxxx
//                                                mutableImplementation.addInstruction(i, new BuilderInstruction31i(Opcode.CONST, max_local + 2, 0x255 ^ i));
//                                                i++;
//                                                //v0 =v0 ^ v1
//                                                mutableImplementation.addInstruction(i, new BuilderInstruction23x(Opcode.XOR_INT, max_local + 1, max_local + 1, max_local + 2));
//                                                i++;
//                                                //v1 = v1 & v0
//                                                mutableImplementation.addInstruction(i, new BuilderInstruction23x(Opcode.AND_INT, max_local + 2, max_local + 2, max_local + 1));
//                                                i++;
//                                                //跳转到下层
//                                                if (k != 0) {
//                                                    mutableImplementation.addInstruction(i - 14, new BuilderInstruction10t(Opcode.GOTO, mutableImplementation.newLabelForIndex(i - 5)));
//                                                    i++;
//                                                }
//                                                //if(v0 != v1) goto 正确地址
//                                                mutableImplementation.addInstruction(i, new BuilderInstruction22t(Opcode.IF_NE, max_local + 1, max_local + 2, mutableImplementation.newLabelForIndex(k != 0 ? i - 14 : i)));
//                                                i++;
//                                                //v0 = v0 + Random
//                                                mutableImplementation.addInstruction(i, new BuilderInstruction22s(Opcode.ADD_INT_LIT16, max_local + 1, max_local + 1, new Random().nextInt(32767)));
//                                                i++;
//                                                //if v0 == 0 Goto ++
//                                                mutableImplementation.addInstruction(i, new BuilderInstruction21t(Opcode.IF_EQZ, max_local + 1, mutableImplementation.newLabelForIndex(i - 1)));
//                                                i++;
//                                                //goto 重新计算
//                                                mutableImplementation.addInstruction(i, new BuilderInstruction10t(Opcode.GOTO, mutableImplementation.newLabelForIndex(i - 5)));
//                                                i++;
//                                                //NOP
//                                                mutableImplementation.addInstruction(i, new BuilderInstruction10x(Opcode.NOP));
//                                                i++;
//                                                //假跳转
//                                                mutableImplementation.addInstruction(i, new BuilderInstruction21t(Opcode.IF_LTZ, max_local + 1, mutableImplementation.newLabelForIndex(i - 2)));
//                                                i++;
//                                                //交互
//                                                mutableImplementation.swapInstructions(i - 1, i - 2);
//                                                k++;
//                                            } while (k < 2);
//                                        }
//                                    }
//                                }
//                                //方法后
//                                {
//                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction31i(Opcode.CONST, max_local + 1, 0x255 << i));
//                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction31i(Opcode.CONST, max_local + 2, 0x999 ^ 0x255));
//                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction21t(Opcode.IF_EQZ, max_local + 1, mutableImplementation.newLabelForIndex(i - 2)));
//                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction23x(Opcode.XOR_INT, max_local + 1, max_local + 1, max_local + 1));
//                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22t(Opcode.IF_NE, max_local + 1, max_local + 2, mutableImplementation.newLabelForIndex(i)));
//                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction10t(Opcode.GOTO, mutableImplementation.newLabelForIndex(i - 2)));
//                                    //const- class v0,Larmadillo/call / test;
//                                    {
//                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction21c(
//                                                Opcode.CONST_CLASS,
//                                                max_local + 1,
//                                                new ImmutableTypeReference(method.getDefiningClass())));
//                                    }
//                                    //invoke-virtual {v0}, Ljava/lang/Class;->getName()Ljava/lang/String;
//                                    {
//                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction35c(
//                                                Opcode.INVOKE_VIRTUAL,
//                                                1,
//                                                max_local + 1,
//                                                0,
//                                                0,
//                                                0,
//                                                0,
//                                                new ImmutableMethodReference(
//                                                        "Ljava/lang/Class;",
//                                                        "getName",
//                                                        null,
//                                                        "Ljava/lang/String;"
//                                                )));
//                                    }
//                                    //move-result-object v0
//                                    {
//                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction11x(
//                                                Opcode.MOVE_RESULT_OBJECT,
//                                                max_local + 1));
//                                    }
//                                    //invoke-virtual {v0}, Ljava/lang/String;->hashCode()I
//                                    {
//                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction35c(
//                                                Opcode.INVOKE_VIRTUAL,
//                                                1,
//                                                max_local + 1,
//                                                0,
//                                                0,
//                                                0,
//                                                0,
//                                                new ImmutableMethodReference(
//                                                        "Ljava/lang/String;",
//                                                        "hashCode",
//                                                        null,
//                                                        "I"
//                                                )));
//                                    }
//                                    //move-result v0
//                                    {
//                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction11x(
//                                                Opcode.MOVE_RESULT,
//                                                max_local + 1));
//                                    }
//                                    //const/16 v1, 0x9999
//                                    {
//                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction31i(Opcode.CONST,
//                                                max_local + 2,
//                                                hash));
//                                    }
//                                    //xor-int/2addr v0, v1
//                                    {
//                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction12x(
//                                                Opcode.XOR_INT_2ADDR,
//                                                max_local + 1,
//                                                max_local + 2));
//                                    }
//                                    //sparse-switch v0, :sswitch_data_24
//                                    {
//                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction31t(
//                                                Opcode.SPARSE_SWITCH,
//                                                max_local + 1,
//                                                mutableImplementation.newLabelForIndex(mutableImplementation.getInstructions().size())));
//                                    }
//                                    int key2 = 0x255 << i;
//                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction31i(Opcode.CONST, max_local + 2, key2));
//                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22t(
//                                            Opcode.IF_NE,
//                                            max_local + 1,
//                                            max_local + 2,
//                                            mutableImplementation.newLabelForIndex(i - 4)));
//                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction12x(
//                                            Opcode.XOR_INT_2ADDR,
//                                            max_local + 1,
//                                            max_local + 2));
//                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction21t(
//                                            Opcode.IF_NEZ,
//                                            max_local + 1,
//                                            mutableImplementation.newLabelForIndex(i)));
//                                    int key = dexToJavaName(method.getDefiningClass()).hashCode() ^ hash;
//                                    List<SwitchLabelElement> switchLabelElements = new ArrayList<>();
//                                    //跳转正确的
//                                    switchLabelElements.add(
//                                            new SwitchLabelElement(
//                                                    key,
//                                                    mutableImplementation.newLabelForIndex(i)));
//                                    for (int k = 0; k < new Random().nextInt(10) + 1; k++) {
//                                        int codeIndex1 = Integer.toHexString(key * new Random().nextInt() + 1).hashCode();
//                                        int codeIndex2 = Integer.toHexString(key * new Random().nextInt() + 1).hashCode();
//                                        int codeIndex3 = Integer.toHexString(key * new Random().nextInt() + 1).hashCode();
//                                        switchLabelElements.add(
//                                                new SwitchLabelElement(
//                                                        codeIndex1,
//                                                        mutableImplementation.newLabelForIndex(i - 1)));
//                                        switchLabelElements.add(
//                                                new SwitchLabelElement(
//                                                        codeIndex2,
//                                                        mutableImplementation.newLabelForIndex(i + 1)));
//                                    }
//                                    mutableImplementation.addInstruction(new BuilderSparseSwitchPayload(switchLabelElements));
//                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction31i(Opcode.CONST, max_local + 1, 0x255 << i));
//                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction21t(Opcode.IF_NEZ, max_local + 1, mutableImplementation.newLabelForIndex(i)));
//                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction23x(Opcode.XOR_INT, max_local + 1, max_local + 1, max_local + 1));
//                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction10t(Opcode.GOTO, mutableImplementation.newLabelForIndex(i)));
//                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction21t(Opcode.IF_EQZ, max_local + 1, mutableImplementation.newLabelForIndex(i)));
//                                    for (int k = -1; k < 3; k++) {
//                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction10x(Opcode.NOP));
//                                    }
//                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction31i(Opcode.CONST, max_local + 1, 0x255 << i));
//                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction21t(Opcode.IF_LEZ, max_local + 1, mutableImplementation.newLabelForIndex(i)));
//                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction21t(Opcode.IF_EQZ, max_local + 1, mutableImplementation.newLabelForIndex(i - 2)));
//                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction10t(Opcode.GOTO, mutableImplementation.newLabelForIndex(i - 1)));
//                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction11x(Opcode.THROW, max_local + 1));
//                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction31i(Opcode.CONST, max_local + 1, 0x255 ^ i << i));
//                                    {
//                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction31i(Opcode.CONST,
//                                                max_local + 1,
//                                                dexToJavaName(method.getDefiningClass()).hashCode()));
//                                    }
//                                }
                                    }
                                }
                            }


                            //TODO 虚假跳转兼容
                            {
                                for (int k = 0; k < new Random().nextInt(5) + 1; k++) {
                                    int i = 0;
                                    int v1 = Math.abs(new Random().nextInt(99999999));
                                    int v2 = Math.abs(new Random().nextInt(99999999));
                                    hashMap.add(v1);
                                    hashMap.add(v2);
                                    /**
                                     * 得到V1
                                     */
                                    {
                                        mutableImplementation.addInstruction(i++, new BuilderInstruction21c(Opcode.SGET_OBJECT, max_local + 3, new ImmutableFieldReference(classDef.getType(), fieldsName, "[I")));
                                        mutableImplementation.addInstruction(i++, new BuilderInstruction31i(Opcode.CONST, max_local + 4, hashMap.size() - 2));
                                        mutableImplementation.addInstruction(i++, new BuilderInstruction23x(Opcode.AGET, max_local + 1, max_local + 3, max_local + 4));
                                    }
                                    /**
                                     * 得到V2
                                     */
                                    {
                                        mutableImplementation.addInstruction(i++, new BuilderInstruction31i(Opcode.CONST, max_local + 4, hashMap.size() - 1));
                                        mutableImplementation.addInstruction(i++, new BuilderInstruction23x(Opcode.AGET, max_local + 2, max_local + 3, max_local + 4));
                                    }
                                    /**
                                     * switch结构
                                     */
                                    if (max_local + 1 > 15 || max_local + 2 > 15) {
                                        mutableImplementation.addInstruction(i++, new BuilderInstruction23x(Opcode.XOR_INT, max_local + 1, max_local + 1, max_local + 2));
                                        mutableImplementation.addInstruction(i++, new BuilderInstruction31t(
                                                Opcode.SPARSE_SWITCH,
                                                max_local + 1,
                                                mutableImplementation.newLabelForIndex(mutableImplementation.getInstructions().size())));
                                        List<SwitchLabelElement> switchLabelElements = new ArrayList<>();
                                        switchLabelElements.add(
                                                new SwitchLabelElement(
                                                        v1 ^ v2,
                                                        mutableImplementation.newLabelForIndex(i)));
                                        switchLabelElements.add(
                                                new SwitchLabelElement(
                                                        v1 | v2 + new Random().nextInt(99999999),
                                                        mutableImplementation.newLabelForIndex(i - 4)));
                                        mutableImplementation.addInstruction(new BuilderSparseSwitchPayload(switchLabelElements));
                                        /**
                                         * 跳转正确的代码
                                         */
                                        mutableImplementation.addInstruction(i++, new BuilderInstruction10t(Opcode.GOTO, mutableImplementation.newLabelForIndex(i - 1)));
                                        mutableImplementation.addInstruction(i++, new BuilderInstruction23x(Opcode.XOR_INT, max_local + 1, max_local + 1, max_local + 2));
                                        /**
                                         * 死循环
                                         */
                                        mutableImplementation.addInstruction(i++, new BuilderInstruction31i(Opcode.CONST, max_local + 1, new Random().nextInt(9999999)));
                                        mutableImplementation.addInstruction(i++, new BuilderInstruction21t(Opcode.IF_NEZ, max_local + 1, mutableImplementation.newLabelForIndex(i - 2)));
                                        mutableImplementation.addInstruction(i++, new BuilderInstruction10t(Opcode.GOTO, mutableImplementation.newLabelForIndex(i - 2)));
                                        mutableImplementation.addInstruction(i++, new BuilderInstruction23x(Opcode.OR_INT, max_local + 2, max_local + 2, max_local + 1));
                                        mutableImplementation.addInstruction(i++, new BuilderInstruction31t(
                                                Opcode.SPARSE_SWITCH,
                                                max_local + 2,
                                                mutableImplementation.newLabelForIndex(mutableImplementation.getInstructions().size())));
                                        List<SwitchLabelElement> switchLabelElements2 = new ArrayList<>();
                                        switchLabelElements2.add(
                                                new SwitchLabelElement(
                                                        new Random().nextInt(99999999),
                                                        mutableImplementation.newLabelForIndex(i)));
                                        switchLabelElements2.add(
                                                new SwitchLabelElement(
                                                        new Random().nextInt(99999999),
                                                        mutableImplementation.newLabelForIndex(i - 5)));
                                        switchLabelElements2.add(
                                                new SwitchLabelElement(
                                                        v2,
                                                        mutableImplementation.newLabelForIndex(i - 5)));
                                        mutableImplementation.addInstruction(new BuilderSparseSwitchPayload(switchLabelElements2));
                                    }
                                    /**
                                     * 随机if/switch结构
                                     */
                                    else {
                                        if (new Random().nextBoolean()) {
                                            mutableImplementation.addInstruction(i++, new BuilderInstruction23x(Opcode.XOR_INT, max_local + 1, max_local + 1, max_local + 2));
                                            mutableImplementation.addInstruction(i++, new BuilderInstruction31t(
                                                    Opcode.SPARSE_SWITCH,
                                                    max_local + 1,
                                                    mutableImplementation.newLabelForIndex(mutableImplementation.getInstructions().size())));
                                            List<SwitchLabelElement> switchLabelElements = new ArrayList<>();
                                            switchLabelElements.add(
                                                    new SwitchLabelElement(
                                                            v1 ^ v2,
                                                            mutableImplementation.newLabelForIndex(i)));
                                            switchLabelElements.add(
                                                    new SwitchLabelElement(
                                                            v1 | v2 + new Random().nextInt(99999999),
                                                            mutableImplementation.newLabelForIndex(i - 4)));
                                            mutableImplementation.addInstruction(new BuilderSparseSwitchPayload(switchLabelElements));
                                            /**
                                             * 跳转正确的代码
                                             */
                                            mutableImplementation.addInstruction(i++, new BuilderInstruction10t(Opcode.GOTO, mutableImplementation.newLabelForIndex(i - 1)));
                                            mutableImplementation.addInstruction(i++, new BuilderInstruction23x(Opcode.XOR_INT, max_local + 1, max_local + 1, max_local + 2));
                                            /**
                                             * 死循环
                                             */
                                            mutableImplementation.addInstruction(i++, new BuilderInstruction31i(Opcode.CONST, max_local + 1, new Random().nextInt(9999999)));
                                            mutableImplementation.addInstruction(i++, new BuilderInstruction21t(Opcode.IF_NEZ, max_local + 1, mutableImplementation.newLabelForIndex(i - 2)));
                                            mutableImplementation.addInstruction(i++, new BuilderInstruction10t(Opcode.GOTO, mutableImplementation.newLabelForIndex(i - 2)));
                                            mutableImplementation.addInstruction(i++, new BuilderInstruction23x(Opcode.OR_INT, max_local + 2, max_local + 2, max_local + 1));
                                            mutableImplementation.addInstruction(i++, new BuilderInstruction31t(
                                                    Opcode.SPARSE_SWITCH,
                                                    max_local + 2,
                                                    mutableImplementation.newLabelForIndex(mutableImplementation.getInstructions().size())));
                                            List<SwitchLabelElement> switchLabelElements2 = new ArrayList<>();
                                            switchLabelElements2.add(
                                                    new SwitchLabelElement(
                                                            new Random().nextInt(99999999),
                                                            mutableImplementation.newLabelForIndex(i)));
                                            switchLabelElements2.add(
                                                    new SwitchLabelElement(
                                                            new Random().nextInt(99999999),
                                                            mutableImplementation.newLabelForIndex(i - 5)));
                                            switchLabelElements2.add(
                                                    new SwitchLabelElement(
                                                            v2,
                                                            mutableImplementation.newLabelForIndex(i - 5)));
                                            mutableImplementation.addInstruction(new BuilderSparseSwitchPayload(switchLabelElements2));
                                        } else {
                                            /**
                                             * 跳转正确的代码
                                             */
                                            mutableImplementation.addInstruction(i++, new BuilderInstruction10t(Opcode.GOTO, mutableImplementation.newLabelForIndex(i - 1)));
                                            mutableImplementation.addInstruction(i++, new BuilderInstruction23x(Opcode.XOR_INT, max_local + 1, max_local + 1, max_local + 2));
                                            /**
                                             * 死循环
                                             */
                                            mutableImplementation.addInstruction(i++, new BuilderInstruction10t(Opcode.GOTO, mutableImplementation.newLabelForIndex(i - 2)));
                                            mutableImplementation.addInstruction(i++, new BuilderInstruction23x(Opcode.OR_INT, max_local + 2, max_local + 2, max_local + 1));
                                            v1 = max_local + 1;
                                            v2 = max_local + 2;
                                            if (v1 != v2)
                                                mutableImplementation.addInstruction(i++, new BuilderInstruction22t(Opcode.IF_NE, v1, v2, mutableImplementation.newLabelForIndex(i - 2)));
                                            else
                                                mutableImplementation.addInstruction(i++, new BuilderInstruction22t(Opcode.IF_EQ, v1, v2, mutableImplementation.newLabelForIndex(i - 2)));
                                            mutableImplementation.swapInstructions(i - 5, i - 1);
                                        }
                                    }
                                }
                            }


                            //TODO Try兼容严重卡顿
                            {
//                        List<BuilderTryBlock> tryBlocks = new ArrayList<>();
//                        int size = mutableImplementation.getInstructions().size();
//                        for (int i = 0; i < size; i += 5) {
//                            mutableImplementation.addInstruction(new BuilderInstruction11x(Opcode.MOVE_EXCEPTION, max_local + 1));
//                            mutableImplementation.addInstruction(new BuilderInstruction35c(
//                                    Opcode.INVOKE_VIRTUAL,
//                                    1,
//                                    max_local + 1,
//                                    0,
//                                    0,
//                                    0,
//                                    0,
//                                    new ImmutableMethodReference(
//                                            "Ljava/util/RuntimeException;",
//                                            "printStackTrace",
//                                            null,
//                                            "V")));
//                            mutableImplementation.addInstruction(new BuilderInstruction10t(Opcode.GOTO, mutableImplementation.newLabelForIndex(0)));
//                            tryBlocks.add(
//                                    new BuilderTryBlock(mutableImplementation.newLabelForIndex(i),
//                                            mutableImplementation.newLabelForIndex(i + 1),
//                                            "Ljava/util/RuntimeException;",
//                                            mutableImplementation.newLabelForIndex(mutableImplementation.getInstructions().size() - 1)));
//                        }
//                        tryBlocks.addAll(mutableImplementation.getTryBlocks());
                            }

                            ImmutableMethodImplementation newmi = new ImmutableMethodImplementation(
                                    regIndex,
                                    mutableImplementation.getInstructions(),
                                    mutableImplementation.getTryBlocks(),
                                    mutableImplementation.getDebugItems());
                            methods.add(new ImmutableMethod(
                                    method.getDefiningClass(),
                                    method.getName(),
                                    method.getParameters(),
                                    method.getReturnType(),
                                    method.getAccessFlags(),
                                    method.getAnnotations(),
                                    method.getHiddenApiRestrictions(),
                                    newmi));
                            flow_total++;
                        }
                        /**
                         * 跳过其他函数
                         */
                        else
                            methods.add(method);
                    }
                    /**
                     * 判断是否有static结构体
                     */
                    {
                        boolean flag = false;
                        for (Method method : classDef.getMethods()) {
                            if (method.getImplementation() == null)
                                continue;
                            /**
                             * 有static静态体
                             */
                            if (method.getName().equals("<clinit>")) {
                                MutableMethodImplementation mutableImplementation = new MutableMethodImplementation(method.getImplementation(), method, method.getImplementation().getRegisterCount() + 1);
                                for (int i = 0; i < mutableImplementation.getInstructions().size(); i++) {
                                    mutableImplementation.addInstruction(i++, new BuilderInstruction31i(Opcode.CONST, 0, hashMap.size()));
                                    mutableImplementation.addInstruction(i++, new BuilderInstruction22c(Opcode.NEW_ARRAY, 0, 0, new ImmutableTypeReference("[I")));
                                    mutableImplementation.addInstruction(i++, new BuilderInstruction31t(Opcode.FILL_ARRAY_DATA, 0, mutableImplementation.newLabelForIndex(mutableImplementation.getInstructions().size())));
                                    mutableImplementation.addInstruction(new BuilderArrayPayload(4, hashMap));
                                    mutableImplementation.addInstruction(i++, new BuilderInstruction21c(Opcode.SPUT_OBJECT, 0, new ImmutableFieldReference(classDef.getType(), fieldsName, "[I")));
                                    ImmutableMethodImplementation newmi = new ImmutableMethodImplementation(
                                            mutableImplementation.getRegisterCount(),
                                            mutableImplementation.getInstructions(),
                                            mutableImplementation.getTryBlocks(),
                                            mutableImplementation.getDebugItems());
                                    methods.add(new ImmutableMethod(
                                            method.getDefiningClass(),
                                            method.getName(),
                                            method.getParameters(),
                                            method.getReturnType(),
                                            method.getAccessFlags(),
                                            method.getAnnotations(),
                                            method.getHiddenApiRestrictions(),
                                            newmi));
                                    flag = true;
                                    break;
                                }
                            }
                        }
                        /**
                         * 创建新的static静态体
                         */
                        if (!flag) {
                            List<Instruction> newInsts = new ArrayList<>();
                            newInsts.add(new ImmutableInstruction31i(Opcode.CONST, 0, hashMap.size()));
                            newInsts.add(new ImmutableInstruction22c(Opcode.NEW_ARRAY, 0, 0, new ImmutableTypeReference("[I")));
                            newInsts.add(new ImmutableInstruction31t(Opcode.FILL_ARRAY_DATA, 0, 7));
                            newInsts.add(new ImmutableInstruction21c(Opcode.SPUT_OBJECT, 0, new ImmutableFieldReference(classDef.getType(), fieldsName, "[I")));
                            newInsts.add(new ImmutableInstruction10x(Opcode.RETURN_VOID));
                            newInsts.add(new ImmutableInstruction10x(Opcode.NOP));
                            newInsts.add(new ImmutableArrayPayload(4, hashMap));
                            methods.add(new ImmutableMethod(
                                    classDef.getType(),
                                    "<clinit>",
                                    null,
                                    "V",
                                    AccessFlags.STATIC.getValue() | AccessFlags.CONSTRUCTOR.getValue(),
                                    null,
                                    null,
                                    new ImmutableMethodImplementation(1, newInsts, null, null)));
                        }
                    }
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

    private String dexToJavaName(String dexName) {
        if (dexName.charAt(0) == '[') {
            return dexName.replace('/', '.');
        }
        return dexName.replace('/', '.').substring(1, dexName.length() - 1);
    }

}
