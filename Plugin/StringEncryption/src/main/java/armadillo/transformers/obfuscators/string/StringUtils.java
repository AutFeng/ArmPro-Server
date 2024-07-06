package armadillo.transformers.obfuscators.string;


import com.google.common.collect.Lists;
import org.jf.dexlib2.AccessFlags;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.MethodParameter;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.immutable.ImmutableMethod;
import org.jf.dexlib2.immutable.ImmutableMethodImplementation;
import org.jf.dexlib2.immutable.ImmutableMethodParameter;
import org.jf.dexlib2.immutable.instruction.*;
import org.jf.dexlib2.immutable.reference.ImmutableMethodReference;
import org.jf.dexlib2.immutable.reference.ImmutableTypeReference;

import java.util.ArrayList;
import java.util.List;

public class StringUtils {
    /**
     * 创建解密函数
     *
     * @param classDef
     * @return
     */
    public static Method Create$Method(ClassDef classDef, int key1, int key2, int key3,String methodName) {
        List<Instruction> newInsts = new ArrayList<>();
        //new-instance v1, Ljava/lang/StringBuilder;
        {
            newInsts.add(new ImmutableInstruction21c(Opcode.NEW_INSTANCE, 1, new ImmutableTypeReference("Ljava/lang/StringBuilder;")));
        }
        //invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V
        {
            newInsts.add(new ImmutableInstruction35c(
                    Opcode.INVOKE_DIRECT,
                    1,
                    1,
                    0,
                    0,
                    0,
                    0,
                    new ImmutableMethodReference(
                            "Ljava/lang/StringBuilder;",
                            "<init>",
                            null,
                            "V")));
        }
        //invoke-virtual {p0}, Ljava/lang/String;->toCharArray()[C
        {
            newInsts.add(new ImmutableInstruction35c(
                    Opcode.INVOKE_VIRTUAL,
                    1,
                    5,
                    0,
                    0,
                    0,
                    0,
                    new ImmutableMethodReference(
                            "Ljava/lang/String;",
                            "toCharArray",
                            null,
                            "[C")));
        }
        //move-result-object v2
        {
            newInsts.add(new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT, 2));
        }
        //const/4 v0, 0x0
        {
            newInsts.add(new ImmutableInstruction11n(Opcode.CONST_4, 0, 0));
        }
        //array-length v3, v2
        {
            newInsts.add(new ImmutableInstruction12x(Opcode.ARRAY_LENGTH, 3, 2));
        }
        //if-ge v0, v3, :cond_3e
        {
            newInsts.add(new ImmutableInstruction22t(Opcode.IF_GE, 0, 3, 51));
        }
        //rem-int/lit8 v3, v0, 0x4
        {
            newInsts.add(new ImmutableInstruction22b(Opcode.REM_INT_LIT8, 3, 0, 0x4));
        }
        //packed-switch v3, :pswitch_data_44
        {
            newInsts.add(new ImmutableInstruction31t(Opcode.PACKED_SWITCH, 3, 53));
        }
        //aget-char v3, v2, v0
        {
            newInsts.add(new ImmutableInstruction23x(Opcode.AGET_CHAR, 3, 2, 0));
        }
        //xor-int/lit8 v3, v3, -0x1
        {
            newInsts.add(new ImmutableInstruction22b(Opcode.XOR_INT_LIT8, 3, 3, -0x1));
        }
        //int-to-char v3, v3
        {
            newInsts.add(new ImmutableInstruction12x(Opcode.INT_TO_CHAR, 3, 3));
        }
        //invoke-virtual {v1, v3}, Ljava/lang/StringBuilder;->append(C)Ljava/lang/StringBuilder;
        {
            newInsts.add(new ImmutableInstruction35c(
                    Opcode.INVOKE_VIRTUAL,
                    2,
                    1,
                    3,
                    0,
                    0,
                    0,
                    new ImmutableMethodReference(
                            "Ljava/lang/StringBuilder;",
                            "append",
                            Lists.newArrayList("C"),
                            "Ljava/lang/StringBuilder;")));
        }
        //add-int/lit8 v0, v0, 0x1
        {
            newInsts.add(new ImmutableInstruction22b(Opcode.ADD_INT_LIT8, 0, 0, 0x1));
        }
        //goto :goto_a
        {
            newInsts.add(new ImmutableInstruction10t(Opcode.GOTO, -18));
        }
        //aget-char v3, v2, v0
        {
            newInsts.add(new ImmutableInstruction23x(Opcode.AGET_CHAR, 3, 2, 0));
        }
        //const v4, 0x56445676
        {
            newInsts.add(new ImmutableInstruction31i(Opcode.CONST, 4, key1));
        }
        //xor-int/2addr v3, v4
        {
            newInsts.add(new ImmutableInstruction12x(Opcode.XOR_INT_2ADDR, 3, 4));
        }
        //int-to-char v3, v3
        {
            newInsts.add(new ImmutableInstruction12x(Opcode.INT_TO_CHAR, 3, 3));
        }
        //invoke-virtual {v1, v3}, Ljava/lang/StringBuilder;->append(C)Ljava/lang/StringBuilder;
        {
            newInsts.add(new ImmutableInstruction35c(
                    Opcode.INVOKE_VIRTUAL,
                    2,
                    1,
                    3,
                    0,
                    0,
                    0,
                    new ImmutableMethodReference(
                            "Ljava/lang/StringBuilder;",
                            "append",
                            Lists.newArrayList("C"),
                            "Ljava/lang/StringBuilder;")));
        }
        //goto :goto_1a
        {
            newInsts.add(new ImmutableInstruction10t(Opcode.GOTO, -13));
        }
        //aget-char v3, v2, v0
        {
            newInsts.add(new ImmutableInstruction23x(Opcode.AGET_CHAR, 3, 2, 0));
        }
        //const v4, 0x7894645
        {
            newInsts.add(new ImmutableInstruction31i(Opcode.CONST, 4, key2));
        }
        //xor-int/2addr v3, v4
        {
            newInsts.add(new ImmutableInstruction12x(Opcode.XOR_INT_2ADDR, 3, 4));
        }
        //int-to-char v3, v3
        {
            newInsts.add(new ImmutableInstruction12x(Opcode.INT_TO_CHAR, 3, 3));
        }
        // invoke-virtual {v1, v3}, Ljava/lang/StringBuilder;->append(C)Ljava/lang/StringBuilder;
        {
            newInsts.add(new ImmutableInstruction35c(
                    Opcode.INVOKE_VIRTUAL,
                    2,
                    1,
                    3,
                    0,
                    0,
                    0,
                    new ImmutableMethodReference(
                            "Ljava/lang/StringBuilder;",
                            "append",
                            Lists.newArrayList("C"),
                            "Ljava/lang/StringBuilder;")));
        }
        //goto :goto_1a
        {
            newInsts.add(new ImmutableInstruction10t(Opcode.GOTO, -24));
        }
        //aget-char v3, v2, v0
        {
            newInsts.add(new ImmutableInstruction23x(Opcode.AGET_CHAR, 3, 2, 0));
        }
        //const v4, 0x6546416    # 3.99463E-35f
        {
            newInsts.add(new ImmutableInstruction31i(Opcode.CONST, 4, key3));
        }
        //xor-int/2addr v3, v4
        {
            newInsts.add(new ImmutableInstruction12x(Opcode.XOR_INT_2ADDR, 3, 4));
        }
        //int-to-char v3, v3
        {
            newInsts.add(new ImmutableInstruction12x(Opcode.INT_TO_CHAR, 3, 3));
        }
        //invoke-virtual {v1, v3}, Ljava/lang/StringBuilder;->append(C)Ljava/lang/StringBuilder;
        {
            newInsts.add(new ImmutableInstruction35c(
                    Opcode.INVOKE_VIRTUAL,
                    2,
                    1,
                    3,
                    0,
                    0,
                    0,
                    new ImmutableMethodReference(
                            "Ljava/lang/StringBuilder;",
                            "append",
                            Lists.newArrayList("C"),
                            "Ljava/lang/StringBuilder;")));
        }
        //goto :goto_1a
        {
            newInsts.add(new ImmutableInstruction10t(Opcode.GOTO, -35));
        }
        //invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;
        {
            newInsts.add(new ImmutableInstruction35c(
                    Opcode.INVOKE_VIRTUAL,
                    1,
                    1,
                    0,
                    0,
                    0,
                    0,
                    new ImmutableMethodReference(
                            "Ljava/lang/StringBuilder;",
                            "toString",
                            null,
                            "Ljava/lang/String;")));
        }
        //move-result-object v0
        {
            newInsts.add(new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT, 0));
        }
        //return-object v0
        {
            newInsts.add(new ImmutableInstruction11x(Opcode.RETURN_OBJECT, 0));
        }
        //nop
        {
            newInsts.add(new ImmutableInstruction10x(Opcode.NOP));
        }
        //pswitch_data_44
        {
            List<ImmutableSwitchElement> switchElements = new ArrayList<>();
            switchElements.add(new ImmutableSwitchElement(0, 14));
            switchElements.add(new ImmutableSwitchElement(1, 25));
            switchElements.add(new ImmutableSwitchElement(2, 36));
            newInsts.add(new ImmutablePackedSwitchPayload(switchElements));
        }
        List<MethodParameter> methodParameters = new ArrayList<>();
        methodParameters.add(new ImmutableMethodParameter("Ljava/lang/String;", null, null));
        return new ImmutableMethod(
                classDef.getType(),
                methodName,
                methodParameters,
                "Ljava/lang/String;",
                AccessFlags.PRIVATE.getValue() | AccessFlags.STATIC.getValue(),
                null,
                null,
                new ImmutableMethodImplementation(6, newInsts, null, null));
    }

    /**
     * 加密字符串
     *
     * @param src
     * @return
     */
    public static String encrypt(String src, int key1, int key2, int key3) {
        StringBuilder sb = new StringBuilder();
        char[] chars = src.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            switch (i % 4) {
                case 0:
                    sb.append((char) (chars[i] ^ key1));
                    break;
                case 1:
                    sb.append((char) (chars[i] ^ key2));
                    break;
                case 2:
                    sb.append((char) (chars[i] ^ key3));
                    break;
                default:
                    sb.append((char) (chars[i] ^ 0xFFFFFFFF));
                    break;
            }
        }
        return sb.toString();
    }
}

