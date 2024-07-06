package armadillo.transformers.obfuscators.numbers;


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
import org.jf.dexlib2.immutable.reference.ImmutableTypeReference;

import java.util.ArrayList;
import java.util.List;

public class NumbersUtils {

    /**
     * 加密数字
     *
     * @param classDef
     * @param key
     * @return
     */
    public static Method Create$Method_Num(ClassDef classDef, int key,String methodName) {
        List<Instruction> newInsts = new ArrayList<>();
        //const/4 v7, 0x3
        {
            newInsts.add(new ImmutableInstruction11n(Opcode.CONST_4, 7, 0x3));
        }
        //const/4 v6, 0x2
        {
            newInsts.add(new ImmutableInstruction11n(Opcode.CONST_4, 6, 0x2));
        }
        //const/4 v5, 0x1
        {
            newInsts.add(new ImmutableInstruction11n(Opcode.CONST_4, 5, 0x1));
        }
        //const/4 v1, 0x0
        {
            newInsts.add(new ImmutableInstruction11n(Opcode.CONST_4, 1, 0x0));
        }
        //const/4 v0, 0x4
        {
            newInsts.add(new ImmutableInstruction11n(Opcode.CONST_4, 0, 0x4));
        }
        //new-array v2, v0, [I
        {
            newInsts.add(new ImmutableInstruction22c(Opcode.NEW_ARRAY, 2, 0, new ImmutableTypeReference("[I")));
        }
        //shr-int/lit8 v0, p0, 0x18
        {
            newInsts.add(new ImmutableInstruction22b(Opcode.SHR_INT_LIT8, 0, 9 - 1, 0x18));
        }
        //and-int/lit16 v0, v0, 0xff
        {
            newInsts.add(new ImmutableInstruction22s(Opcode.AND_INT_LIT16, 0, 0, 0xff));
        }
        //aput v0, v2, v7
        {
            newInsts.add(new ImmutableInstruction23x(Opcode.APUT, 0, 2, 7));
        }
        //shr-int/lit8 v0, p0, 0x10
        {
            newInsts.add(new ImmutableInstruction22b(Opcode.SHR_INT_LIT8, 0, 9 - 1, 0x10));
        }
        //and-int/lit16 v0, v0, 0xff
        {
            newInsts.add(new ImmutableInstruction22s(Opcode.AND_INT_LIT16, 0, 0, 0xff));
        }
        //aput v0, v2, v6
        {
            newInsts.add(new ImmutableInstruction23x(Opcode.APUT, 0, 2, 6));
        }
        //shr-int/lit8 v0, p0, 0x8
        {
            newInsts.add(new ImmutableInstruction22b(Opcode.SHR_INT_LIT8, 0, 9 - 1, 0x8));
        }
        //and-int/lit16 v0, v0, 0xff
        {
            newInsts.add(new ImmutableInstruction22s(Opcode.AND_INT_LIT16, 0, 0, 0xff));
        }
        //aput v0, v2, v5
        {
            newInsts.add(new ImmutableInstruction23x(Opcode.APUT, 0, 2, 5));
        }
        //and-int/lit16 v0, p0, 0xff
        {
            newInsts.add(new ImmutableInstruction22s(Opcode.AND_INT_LIT16, 0, 9 - 1, 0xff));
        }
        //aput v0, v2, v1
        {
            newInsts.add(new ImmutableInstruction23x(Opcode.APUT, 0, 2, 1));
        }
        //move v0, v1
        {
            newInsts.add(new ImmutableInstruction12x(Opcode.MOVE, 0, 1));
        }
        //:goto_1e
        //array-length v3, v2
        {
            newInsts.add(new ImmutableInstruction12x(Opcode.ARRAY_LENGTH, 3, 2));
        }
        //TODO
        //if-ge v0, v3, :cond_29
        {
            newInsts.add(new ImmutableInstruction22t(Opcode.IF_GE, 0, 3, 13));
        }
        //aget v3, v2, v0
        {
            newInsts.add(new ImmutableInstruction23x(Opcode.AGET, 3, 2, 0));
        }
        //const v4, 0x186a0
        {
            newInsts.add(new ImmutableInstruction31i(Opcode.CONST, 4, key));
        }
        //xor-int/2addr v3, v4
        {
            newInsts.add(new ImmutableInstruction12x(Opcode.XOR_INT_2ADDR, 3, 4));
        }
        //aput v3, v2, v0
        {
            newInsts.add(new ImmutableInstruction23x(Opcode.APUT, 3, 2, 0));
        }
        //add-int/lit8 v0, v0, 0x1
        {
            newInsts.add(new ImmutableInstruction22s(Opcode.ADD_INT_LIT16, 0, 0, 0x1));
        }
        //TODO
        //goto :goto_1e
        {
            newInsts.add(new ImmutableInstruction10t(Opcode.GOTO, -13));
        }
        //:cond_29
        //aget v0, v2, v1
        {
            newInsts.add(new ImmutableInstruction23x(Opcode.AGET, 0, 2, 1));
        }
        //and-int/lit16 v0, v0, 0xff
        {
            newInsts.add(new ImmutableInstruction22s(Opcode.AND_INT_LIT16, 0, 0, 0xff));
        }
        //aget v1, v2, v5
        {
            newInsts.add(new ImmutableInstruction23x(Opcode.AGET, 1, 2, 5));
        }
        //and-int/lit16 v1, v1, 0xff
        {
            newInsts.add(new ImmutableInstruction22s(Opcode.AND_INT_LIT16, 1, 1, 0xff));
        }
        //shl-int/lit8 v1, v1, 0x8
        {
            newInsts.add(new ImmutableInstruction22b(Opcode.SHL_INT_LIT8, 1, 1, 0x8));
        }
        //or-int/2addr v0, v1
        {
            newInsts.add(new ImmutableInstruction12x(Opcode.OR_INT_2ADDR, 0, 1));
        }
        //aget v1, v2, v6
        {
            newInsts.add(new ImmutableInstruction23x(Opcode.AGET, 1, 2, 6));
        }
        //and-int/lit16 v1, v1, 0xff
        {
            newInsts.add(new ImmutableInstruction22s(Opcode.AND_INT_LIT16, 1, 1, 0xff));
        }
        //shl-int/lit8 v1, v1, 0x10
        {
            newInsts.add(new ImmutableInstruction22b(Opcode.SHL_INT_LIT8, 1, 1, 0x10));
        }
        //or-int/2addr v0, v1
        {
            newInsts.add(new ImmutableInstruction12x(Opcode.OR_INT_2ADDR, 0, 1));
        }
        //aget v1, v2, v7
        {
            newInsts.add(new ImmutableInstruction23x(Opcode.AGET, 1, 2, 7));
        }
        //and-int/lit16 v1, v1, 0xff
        {
            newInsts.add(new ImmutableInstruction22s(Opcode.AND_INT_LIT16, 1, 1, 0xff));
        }
        //shl-int/lit8 v1, v1, 0x18
        {
            newInsts.add(new ImmutableInstruction22b(Opcode.SHL_INT_LIT8, 1, 1, 0x18));
        }
        //or-int/2addr v0, v1
        {
            newInsts.add(new ImmutableInstruction12x(Opcode.OR_INT_2ADDR, 0, 1));
        }
        //return v0
        {
            newInsts.add(new ImmutableInstruction11x(Opcode.RETURN, 0));
        }
        List<MethodParameter> methodParameters = new ArrayList<>();
        methodParameters.add(new ImmutableMethodParameter("I", null, null));
        return new ImmutableMethod(
                classDef.getType(),
                methodName,
                methodParameters,
                "I",
                AccessFlags.PRIVATE.getValue() | AccessFlags.STATIC.getValue(),
                null,
                null,
                new ImmutableMethodImplementation(9, newInsts, null, null));
    }

    /**
     * 加密数字
     *
     * @param src
     * @param key
     * @return
     */
    public static int encrypt(int src, int key) {
        int[] encryptBytes = new int[4];
        encryptBytes[3] = (src >> 24) & 0xFF;
        encryptBytes[2] = (src >> 16) & 0xFF;
        encryptBytes[1] = (src >> 8) & 0xFF;
        encryptBytes[0] = src & 0xFF;
        for (int i = 0; i < encryptBytes.length; i++)
            encryptBytes[i] = encryptBytes[i] ^ key;
        int value = ((encryptBytes[0] & 0xFF)
                | ((encryptBytes[1] & 0xFF) << 8)
                | ((encryptBytes[2] & 0xFF) << 16)
                | ((encryptBytes[3] & 0xFF) << 24));
        return value;
    }
}

