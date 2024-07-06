package armadillo.model.obfuscators;

import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.iface.reference.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CallMethod {
    private List<String> ParameterTypes;
    private String ReturnType;
    private String name;
    private HashMap<Integer, CallReference> references = new HashMap<>();

    public CallMethod(List<String> parameterTypes, String returnType, String name) {
        this.ParameterTypes = parameterTypes;
        this.ReturnType = returnType;
        this.name = name;
    }

    public List<String> getParameterTypes() {
        return ParameterTypes;
    }

    public void setParameterTypes(List<String> parameterTypes) {
        ParameterTypes = parameterTypes;
    }

    public String getReturnType() {
        return ReturnType;
    }

    public void setReturnType(String returnType) {
        ReturnType = returnType;
    }

    public HashMap<Integer, CallReference> getReferences() {
        return references;
    }

    public void setReferences(HashMap<Integer, CallReference> references) {
        this.references = references;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Result putMethod(int index, Opcode opcode, Reference reference) {
        for (Map.Entry<Integer, CallReference> entry : references.entrySet()) {
            Reference src = entry.getValue().getReference();
            if (entry.getValue().getOpcode() == opcode) {
                switch (opcode) {
                    case INVOKE_INTERFACE:
                    case INVOKE_SUPER:
                    case INVOKE_STATIC:
                    case INVOKE_DIRECT:
                    case INVOKE_VIRTUAL: {
                        MethodReference methodReference = (MethodReference) src;
                        MethodReference old = (MethodReference) reference;
                        if (methodReference.getDefiningClass().equals(old.getDefiningClass())
                                && methodReference.getReturnType().equals(old.getReturnType())
                                && methodReference.getName().equals(old.getName())
                                && equalsParameter(methodReference.getParameterTypes(), old.getParameterTypes()))
                            return new Result(entry.getKey(), true);
                    }
                    break;
                    case CONST_STRING_JUMBO:
                    case CONST_STRING:
                    case ADD_INT_LIT8:
                    case RSUB_INT_LIT8:
                    case MUL_INT_LIT8:
                    case DIV_INT_LIT8:
                    case REM_INT_LIT8:
                    case AND_INT_LIT8:
                    case OR_INT_LIT8:
                    case XOR_INT_LIT8:
                    case SHL_INT_LIT8:
                    case SHR_INT_LIT8:
                    case USHR_INT_LIT8:
                    case ADD_INT_LIT16:
                    case RSUB_INT:
                    case MUL_INT_LIT16:
                    case DIV_INT_LIT16:
                    case REM_INT_LIT16:
                    case AND_INT_LIT16:
                    case OR_INT_LIT16:
                    case XOR_INT_LIT16: {
                        StringReference stringReference = (StringReference) src;
                        StringReference old = (StringReference) reference;
                        if (stringReference.getString().equals(old.getString()))
                            return new Result(entry.getKey(), true);
                    }
                    break;
                    case FILLED_NEW_ARRAY:
                    case FILLED_NEW_ARRAY_RANGE:
                    case NEW_ARRAY:
                    case NEW_INSTANCE: {
                        TypeReference typeReference = (TypeReference) src;
                        TypeReference old = (TypeReference) reference;
                        if (typeReference.getType().equals(old.getType()))
                            return new Result(entry.getKey(), true);
                    }
                    break;
                    case SPUT:
                    case SPUT_BYTE:
                    case SPUT_CHAR:
                    case SPUT_SHORT:
                    case SPUT_BOOLEAN:
                    case SPUT_OBJECT:

                    case SGET:
                    case SGET_BYTE:
                    case SGET_CHAR:
                    case SGET_SHORT:
                    case SGET_BOOLEAN:
                    case SGET_WIDE:
                    case SGET_OBJECT:

                    case IPUT:
                    case IPUT_BYTE:
                    case IPUT_CHAR:
                    case IPUT_SHORT:
                    case IPUT_BOOLEAN:
                    case IPUT_OBJECT:

                    case IGET:
                    case IGET_BYTE:
                    case IGET_CHAR:
                    case IGET_SHORT:
                    case IGET_BOOLEAN:
                    case IGET_WIDE:
                    case IGET_OBJECT: {
                        FieldReference fieldReference = (FieldReference) src;
                        FieldReference old = (FieldReference) reference;
                        if (fieldReference.getDefiningClass().equals(old.getDefiningClass())
                                && fieldReference.getType().equals(old.getType())
                                && fieldReference.getName().equals(old.getName()))
                            return new Result(entry.getKey(), true);
                    }
                    break;
                    case APUT_OBJECT:
                    case APUT_BYTE:
                    case APUT_BOOLEAN:
                    case APUT_SHORT:
                    case APUT_CHAR:
                    case AGET_BYTE:
                    case AGET_CHAR:
                    case AGET_SHORT:
                    case AGET_BOOLEAN:
                    case AGET_OBJECT:
                    case SUB_INT:
                    case MUL_INT:
                    case DIV_INT:
                    case REM_INT:
                    case AND_INT:
                    case OR_INT:
                    case XOR_INT:
                    case SHL_INT:
                    case SHR_INT:
                    case USHR_INT:
                    case ADD_INT:
                    case NEG_INT:
                    case NOT_INT:
                    case ADD_INT_2ADDR:
                    case SUB_INT_2ADDR:
                    case MUL_INT_2ADDR:
                    case DIV_INT_2ADDR:
                    case REM_INT_2ADDR:
                    case AND_INT_2ADDR:
                    case OR_INT_2ADDR:
                    case XOR_INT_2ADDR:
                    case SHL_INT_2ADDR:
                    case SHR_INT_2ADDR:
                    case USHR_INT_2ADDR:
                        return new Result(entry.getKey(), true);
                }
            }
        }
        references.put(index, new CallReference(reference, opcode));
        return new Result(index, false);
    }

    public static class Result {
        int index;
        boolean isExist;

        public Result(int index, boolean isExist) {
            this.index = index;
            this.isExist = isExist;
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public boolean isExist() {
            return isExist;
        }

        public void setExist(boolean exist) {
            isExist = exist;
        }
    }

    public boolean equalsReturn(String returnType) {
        return ReturnType.equals(returnType);
    }

    public boolean equalsParameter(List<String> parameterTypes) {
        if (parameterTypes.size() == 0 && ParameterTypes.size() == 0)
            return true;
        else if (parameterTypes.size() != ParameterTypes.size())
            return false;
        else
            return Arrays.equals(ParameterTypes.toArray(), parameterTypes.toArray());
    }

    public boolean equalsParameter(List<? extends CharSequence> src, List<? extends CharSequence> old) {
        if (src.size() == 0 && old.size() == 0)
            return true;
        else if (src.size() != old.size())
            return false;
        else {
            for (int i = 0; i < src.size(); i++) {
                if (!src.get(i).toString().equals(old.get(i).toString()))
                    return false;
            }
        }
        return true;
    }
}
