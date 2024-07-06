package armadillo.transformers.obfuscators.references;

import org.jf.dexlib2.iface.instruction.formats.Instruction35c;

import java.util.ArrayList;
import java.util.List;

public class MethodHelper {
    public static String getResultType(String type) {
        if (type.startsWith("[")
                && type.contains("L")
                && type.endsWith(";"))
            return type.split("L")[0] + "Ljava/lang/Object;";
        else if (type.startsWith("L") && type.endsWith(";"))
            return "Ljava/lang/Object;";
        else
            return type;
    }

    public static List<String> getParameters(List<String> parameterTypes) {
        List<String> ParameterTypes = new ArrayList<>();
        for (String type : parameterTypes) {
            if (type.startsWith("[")
                    && type.contains("L")
                    && type.endsWith(";"))
                ParameterTypes.add(type.split("L")[0] + "Ljava/lang/Object;");
            else if (type.startsWith("L") && type.endsWith(";"))
                ParameterTypes.add("Ljava/lang/Object;");
            else
                ParameterTypes.add(type);
        }
        return ParameterTypes;
    }

    public static int getInstruction35cRegisterIndex(Instruction35c instruction35c, int index) {
        switch (index) {
            case 0:
                return instruction35c.getRegisterC();
            case 1:
                return instruction35c.getRegisterD();
            case 2:
                return instruction35c.getRegisterE();
            case 3:
                return instruction35c.getRegisterF();
            case 4:
                return instruction35c.getRegisterG();
        }
        return 0;
    }


}