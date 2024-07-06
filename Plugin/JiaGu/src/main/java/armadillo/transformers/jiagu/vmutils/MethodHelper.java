package armadillo.transformers.jiagu.vmutils;

import org.jf.dexlib2.iface.reference.MethodReference;
import org.jf.dexlib2.util.MethodUtil;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MethodHelper {

    public static String genTypeInNative(CharSequence type) {
        char cType = type.charAt(0);
        switch (cType) {
            case 'V':
                return "void";
            case 'Z':
                return "jboolean";
            case 'B':
                return "jbyte";
            case 'S':
                return "jshort";
            case 'C':
                return "jchar";
            case 'I':
                return "jint";
            case 'J':
                return "jlong";
            case 'F':
                return "jfloat";
            case 'D':
                return "jdouble";
            case 'L':
                return "jobject";
            case '[':
                return "jobject";
            default:
                return null;
        }
    }

    public static String genParamTypeListInNative(MethodReference mr) {
        List<? extends CharSequence> params = mr.getParameterTypes();
        StringBuilder paramsList = new StringBuilder();
        paramsList.append("JNIEnv *env, jobject thiz");
        int length = params.size();
        for (int i = 0; i < length; i++) {
            paramsList.append(", ");
            paramsList.append(genTypeInNative(params.get(i)));
            paramsList.append(" ");
            paramsList.append(String.format("p%d", i));
        }
        return paramsList.toString();
    }

    public static String genMethodNameNative(String clz, String name) {
        return escapeCppNameString(clz + "_" + name);
    }

    public static String escapeCppNameString(String value) {
        Matcher m = Pattern.compile("([^a-zA-Z_0-9])").matcher(value);
        StringBuffer sb = new StringBuffer(value.length());
        while (m.find()) {
            m.appendReplacement(sb, String.valueOf((int) m.group(1).charAt(0)));
        }
        m.appendTail(sb);
        String output = sb.toString();
        if (output.length() > 0 && (output.charAt(0) >= '0' && output.charAt(0) <= '9')) {
            output = "_" + output;
        }
        return output;
    }

    @Nonnull
    public static String genJniClass(@Nonnull String type) {
        if (type.charAt(0) == 'L') {
            return type.substring(1, type.length() - 1);
        }
        return type;
    }

    @Nonnull
    public static String genShorty(@Nonnull MethodReference methodReference) {
        return MethodUtil.getShorty(methodReference.getParameterTypes(), methodReference.getReturnType());
    }

    @Nonnull
    public static String genMethodSig(@Nonnull MethodReference reference) {
        List<? extends CharSequence> parameterTypes = reference.getParameterTypes();
        StringBuilder sig = new StringBuilder();
        sig.append("(");
        for (CharSequence parameterType : parameterTypes)
            sig.append(parameterType);
        sig.append(")");
        String returnType = reference.getReturnType();
        sig.append(returnType);
        return sig.toString();
    }
}
