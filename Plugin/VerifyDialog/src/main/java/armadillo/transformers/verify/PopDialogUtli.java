package armadillo.transformers.verify;

import com.google.common.collect.Lists;
import org.jf.dexlib2.AccessFlags;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.immutable.ImmutableClassDef;
import org.jf.dexlib2.immutable.ImmutableMethod;
import org.jf.dexlib2.immutable.ImmutableMethodImplementation;
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction10x;
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction35c;
import org.jf.dexlib2.immutable.reference.ImmutableMethodReference;

import java.util.ArrayList;
import java.util.List;

public class PopDialogUtli {
    public static ClassDef CreateRodom(String superClass,String cls){
        List<Method> methods = new ArrayList<>();
        /**
         * init
         */
        {
            methods.add(new ImmutableMethod(
                    cls,
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
                                            superClass,
                                            "<init>",
                                            null,
                                            "V")),
                                    new ImmutableInstruction10x(Opcode.RETURN_VOID)),
                            null,
                            null)));
        }
        return new ImmutableClassDef(
                cls,
                AccessFlags.PUBLIC.getValue(),
                superClass,
                null,
                null,
                null,
                null,
                methods);
    }
}
