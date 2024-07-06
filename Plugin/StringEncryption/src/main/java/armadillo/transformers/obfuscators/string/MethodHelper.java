package armadillo.transformers.obfuscators.string;

import org.jf.dexlib2.AccessFlags;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.builder.MutableMethodImplementation;
import org.jf.dexlib2.builder.instruction.BuilderInstruction22x;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.util.MethodUtil;

public class MethodHelper {
    public static int fixReg(MutableMethodImplementation mutableMethodImplementation, Method value, int addCount) {
        int oldparameterStart = mutableMethodImplementation.getRegisterCount() - MethodUtil.getParameterRegisterCount(value);
        int registerCount = oldparameterStart + addCount + MethodUtil.getParameterRegisterCount(value);
        if (value.getParameters().size() == 0 && AccessFlags.STATIC.isSet(value.getAccessFlags()))
            return registerCount;
        int newparameterStart = registerCount - MethodUtil.getParameterRegisterCount(value);
        if (!AccessFlags.STATIC.isSet(value.getAccessFlags())) {
            mutableMethodImplementation.addInstruction(0, new BuilderInstruction22x(Opcode.MOVE_OBJECT_FROM16, oldparameterStart, newparameterStart));
            oldparameterStart++;
            newparameterStart++;
        }
        for (int i = 0; i < value.getParameters().size(); i++) {
            switch (value.getParameters().get(i).charAt(0)) {
                case 'Z':
                case 'B':
                case 'S':
                case 'C':
                case 'I':
                case 'F':
                    mutableMethodImplementation.addInstruction(i + (AccessFlags.STATIC.isSet(value.getAccessFlags()) ? 0 : 1), new BuilderInstruction22x(Opcode.MOVE_FROM16, oldparameterStart, newparameterStart));
                    oldparameterStart++;
                    newparameterStart++;
                    break;
                case 'D':
                case 'J':
                    mutableMethodImplementation.addInstruction(i + (AccessFlags.STATIC.isSet(value.getAccessFlags()) ? 0 : 1), new BuilderInstruction22x(Opcode.MOVE_WIDE_FROM16, oldparameterStart, newparameterStart));
                    oldparameterStart += 2;
                    newparameterStart += 2;
                    break;
                default:
                    mutableMethodImplementation.addInstruction(i + (AccessFlags.STATIC.isSet(value.getAccessFlags()) ? 0 : 1), new BuilderInstruction22x(Opcode.MOVE_OBJECT_FROM16, oldparameterStart, newparameterStart));
                    oldparameterStart++;
                    newparameterStart++;
                    break;
            }
        }
        return registerCount;
    }
}
