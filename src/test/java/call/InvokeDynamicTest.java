package call;

import org.junit.Test;

import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.util.Date;

public class InvokeDynamicTest {
    private static String[] strings;
    private String[] test;
    private static String[] test2;

    static {
        Armadillo();
    }

    public void ToastTest() throws Throwable {
        Toast(this,this);
    }
    public void Toast(Object... objects) throws Throwable {
        callSite(1).dynamicInvoker().invoke();
        callSite(1).dynamicInvoker().invoke(null);
    }

    private static void Armadillo() {
        strings = new String[1000];
        strings[0] = "java.text.SimpleDateFormat:parse:(Ljava/lang/String;)Ljava/util/Date;:virtual";
        strings[1] = "java.util.Date:getTime:()J:virtual";
        strings[2] = "java.util.Date:<init>:()V:constructor";
        strings[3] = "call.InvokeDynamicTest:test:()[Ljava/lang/String;:get";
        strings[4] = "call.InvokeDynamicTest:test:()[Ljava/lang/String;:set";
        strings[5] = "call.InvokeDynamicTest:test2:()[Ljava/lang/String;:get";
        strings[6] = "call.InvokeDynamicTest:test2:()[Ljava/lang/String;:set";
        strings[7] = "call.InvokeDynamicTest$testsuper:test:()I:super";
    }

    private static CallSite callSite(int index) {
        CallSite callSite = null;
        try {
            String[] var = strings[index].split(":");
            MethodType type = MethodType.fromMethodDescriptorString(var[2], ClassLoader.getSystemClassLoader());
            String action = var[3];
            if ("virtual".equals(action)) {
                callSite = new ConstantCallSite(MethodHandles.lookup().
                        findVirtual(Class.forName(var[0]), var[1], type));
            } else if ("static".equals(action)) {
                callSite = new ConstantCallSite(MethodHandles.lookup().
                        findStatic(Class.forName(var[0]), var[1], type));
            } else if ("super".equals(action)) {
                MethodHandles.Lookup lookup = MethodHandles.lookup();
                Field allowedModes = MethodHandles.Lookup.class.getDeclaredField("allowedModes");
                allowedModes.setAccessible(true);
                allowedModes.set(lookup, -1);
                callSite = new ConstantCallSite(lookup.
                        findSpecial(Class.forName(var[0]), var[1], type, Class.forName(var[0])));
            } else if ("constructor".equals(action)) {
                callSite = new ConstantCallSite(MethodHandles.lookup().
                        findConstructor(Class.forName(var[0]), type));
            } else if ("get".equals(action)) {
                try {
                    callSite = new ConstantCallSite(MethodHandles.lookup().
                            findGetter(Class.forName(var[0]), var[1], type.returnType()));
                } catch (Exception e) {
                    callSite = new ConstantCallSite(MethodHandles.lookup().
                            findStaticGetter(Class.forName(var[0]), var[1], type.returnType()));
                }
            } else if ("set".equals(action)) {
                try {
                    callSite = new ConstantCallSite(MethodHandles.lookup().
                            findSetter(Class.forName(var[0]), var[1], type.returnType()));
                } catch (Exception e) {
                    callSite = new ConstantCallSite(MethodHandles.lookup().
                            findStaticSetter(Class.forName(var[0]), var[1], type.returnType()));
                }
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return callSite;
    }

    @Test
    public void test() throws Throwable {
        test = new String[10];
        test2 = new String[20];
        System.out.println("test size " + ((String[]) callSite(3).dynamicInvoker().invoke(this)).length);
        callSite(4).dynamicInvoker().invoke(this, new String[100]);
        System.out.println("test size " + test.length);

        System.out.println("test2 size " + ((String[]) callSite(5).dynamicInvoker().invoke()).length);
        callSite(6).dynamicInvoker().invoke(new String[200]);
        System.out.println("test2 size " + test.length);

        testsuper testsuper = new testsuper();
        System.out.println(callSite(7).dynamicInvoker().invoke(testsuper));

        Date date = (Date) callSite(2).dynamicInvoker().invoke();
        System.out.println("Call Test ->" + callSite(1).dynamicInvoker().invoke(date));
    }

    public static String[] getStrings() {
        return strings;
    }

    public class testsuper extends testsuper2 {
        public int test() {
            return super.test();
        }
    }

    public class testsuper2 {

        public int test() {
            return 64654654;
        }
    }
}
