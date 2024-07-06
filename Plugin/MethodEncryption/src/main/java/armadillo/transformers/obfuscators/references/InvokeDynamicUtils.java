package armadillo.transformers.obfuscators.references;

import armadillo.utils.LoaderRes;
import armadillo.utils.SmaliUtils;
import org.jf.dexlib2.AccessFlags;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.builder.MutableMethodImplementation;
import org.jf.dexlib2.builder.instruction.BuilderInstruction35c;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.immutable.ImmutableMethod;
import org.jf.dexlib2.immutable.ImmutableMethodImplementation;
import org.jf.dexlib2.immutable.instruction.*;
import org.jf.dexlib2.immutable.reference.ImmutableFieldReference;
import org.jf.dexlib2.immutable.reference.ImmutableMethodReference;
import org.jf.dexlib2.immutable.reference.ImmutableStringReference;
import org.jf.dexlib2.immutable.reference.ImmutableTypeReference;

import java.util.ArrayList;
import java.util.List;

public class InvokeDynamicUtils {
    public static Method CreateClinit(ClassDef classDef, String methodName) {
        List<Instruction> newInsts = new ArrayList<>();
        newInsts.add(new ImmutableInstruction35c(Opcode.INVOKE_STATIC,
                0,
                0,
                0,
                0,
                0,
                0,
                new ImmutableMethodReference(classDef.getType(),
                        methodName,
                        null,
                        "V")));
        newInsts.add(new ImmutableInstruction10x(Opcode.RETURN_VOID));
        return new ImmutableMethod(
                classDef.getType(),
                "<clinit>",
                null,
                "V",
                AccessFlags.STATIC.getValue() | AccessFlags.CONSTRUCTOR.getValue(),
                null,
                null,
                new ImmutableMethodImplementation(0, newInsts, null, null));
    }

    public static Method AddClinit(Method clinit, String methodName) {
        MutableMethodImplementation mutableImplementation = new MutableMethodImplementation(clinit.getImplementation());
        mutableImplementation.addInstruction(0, new BuilderInstruction35c(
                Opcode.INVOKE_STATIC,
                0,
                0,
                0,
                0,
                0,
                0,
                new ImmutableMethodReference(clinit.getDefiningClass(),
                        methodName,
                        null,
                        "V")));
        ImmutableMethodImplementation newmi = new ImmutableMethodImplementation(
                clinit.getImplementation().getRegisterCount(),
                mutableImplementation.getInstructions(),
                mutableImplementation.getTryBlocks(),
                null);
        return new ImmutableMethod(clinit.getDefiningClass(),
                clinit.getName(),
                clinit.getParameters(),
                clinit.getReturnType(),
                clinit.getAccessFlags(),
                clinit.getAnnotations(),
                null,
                newmi);
    }

    public static Method CreateArmadillo(ClassDef classDef, String fieldName, String methodName, List<String> types) {
        /**
         *const/4 v0, 0x2
         *new-array v0, v0, [Ljava/lang/String;
         *sput-object v0, Lcall/InvokeDynamicTest;->strings:[Ljava/lang/String;
         *sget-object v0, Lcall/InvokeDynamicTest;->strings:[Ljava/lang/String;
         *const/4 v1, 0x0
         *const-string v2, "test"
         *aput-object v2, v0, v1
         *sget-object v0, Lcall/InvokeDynamicTest;->strings:[Ljava/lang/String;
         *const/4 v1, 0x1
         *const-string v2, "test2"
         *aput-object v2, v0, v1
         */
        List<Instruction> newInsts = new ArrayList<>();
        if (types.size() > 7)
            newInsts.add(new ImmutableInstruction21s(Opcode.CONST_16, 0, types.size()));
        else
            newInsts.add(new ImmutableInstruction11n(Opcode.CONST_4, 0, types.size()));
        newInsts.add(new ImmutableInstruction22c(Opcode.NEW_ARRAY, 0, 0, new ImmutableTypeReference("[Ljava/lang/String;")));
        newInsts.add(new ImmutableInstruction21c(Opcode.SPUT_OBJECT, 0, new ImmutableFieldReference(classDef.getType(),
                fieldName,
                "[Ljava/lang/String;")));
        newInsts.add(new ImmutableInstruction21c(Opcode.SGET_OBJECT, 0, new ImmutableFieldReference(classDef.getType(),
                fieldName,
                "[Ljava/lang/String;")));
        for (int i = 0; i < types.size(); i++) {
            if (i > 7)
                newInsts.add(new ImmutableInstruction21s(Opcode.CONST_16, 1, i));
            else
                newInsts.add(new ImmutableInstruction11n(Opcode.CONST_4, 1, i));
            newInsts.add(new ImmutableInstruction21c(Opcode.CONST_STRING, 2, new ImmutableStringReference(types.get(i))));
            newInsts.add(new ImmutableInstruction23x(Opcode.APUT_OBJECT, 2, 0, 1));
        }
        newInsts.add(new ImmutableInstruction10x(Opcode.RETURN_VOID));
        return new ImmutableMethod(
                classDef.getType(),
                methodName,
                null,
                "V",
                AccessFlags.STATIC.getValue() | AccessFlags.PRIVATE.getValue(),
                null,
                null,
                new ImmutableMethodImplementation(3, newInsts, null, null));
    }

    public static ClassDef CreatecallSite() throws Exception {
        byte[] bytes = LoaderRes.getInstance().getStaticResAsBytes("smali/CallSite.smali");
        return SmaliUtils.assembleSmali(bytes);
    }
}
