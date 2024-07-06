package call;

import org.junit.Test;

import java.lang.invoke.*;


public class CallTest {
    @Test
    public void testcall() {
        try {
            MethodType type = MethodType.methodType(int.class);
            type = type.appendParameterTypes(int.class);
            MethodHandle mh =
                    MethodHandles.lookup().
                            findVirtual(CallTest.class, "hashCode", type);
            CallSite callSite = new ConstantCallSite(mh);
            int ret = (int) callSite.dynamicInvoker().invoke(this, 100);
            System.out.println(ret);
        } catch (Exception e) {
            e.printStackTrace();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    @Test
    public void testSystemOutcall() {
        try {
            MethodType type = MethodType.fromMethodDescriptorString("(Ljava/lang/String;)V", ClassLoader.getSystemClassLoader());
            System.out.println(type.toString());
            MethodHandle mh =
                    MethodHandles.lookup().
                            findVirtual(System.out.getClass(), "println", type);
            CallSite callSite = new ConstantCallSite(mh);
            callSite.dynamicInvoker().invoke(System.out, "Call System.out");
        } catch (Exception e) {
            e.printStackTrace();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    @Test
    public void testObjectCall() {
        try {
            MethodType type = MethodType.fromMethodDescriptorString("(Lcall/CallTest;)V", ClassLoader.getSystemClassLoader());
            System.out.println(type.toString());
            MethodHandle mh =
                    MethodHandles.lookup().
                            findVirtual(CallTest.class, "testObject", type);
            CallSite callSite = new ConstantCallSite(mh);
            callSite.dynamicInvoker().invoke(new CallTest(), new CallTest());
        } catch (Exception e) {
            e.printStackTrace();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    @Test
    public void testStaticObjectCall() {
        try {
            MethodType type = MethodType.fromMethodDescriptorString("(Lcall/CallTest;)V",ClassLoader.getSystemClassLoader());
            MethodHandle mh =
                    MethodHandles.lookup().
                            findStatic(CallTest.class, "testStaticObject", type);
            CallSite callSite = new ConstantCallSite(mh);
            callSite.dynamicInvoker().invoke(new CallTest());
        } catch (Exception e) {
            e.printStackTrace();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    @Test
    public void testStaticNullCall() {
        try {
            MethodType type = MethodType.fromMethodDescriptorString("()V",ClassLoader.getSystemClassLoader());
            MethodHandle mh =
                    MethodHandles.lookup().
                            findStatic(CallTest.class, "testStaticObject", type);
            CallSite callSite = new ConstantCallSite(mh);
            callSite.dynamicInvoker().invoke();
        } catch (Exception e) {
            e.printStackTrace();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }


    @Override
    public int hashCode() {
        return 2;
    }

    public int hashCode(int test) {
        return test;
    }

    public void testObject(CallTest callTest) {
        System.out.println("Call Object Succecc" + callTest.hashCode());
    }

    public static void testStaticObject(CallTest callTest) {
        System.out.println("Call Static Object Succecc" + callTest.hashCode());
    }

    public static void testStaticObject() {
        System.out.println("Call Static Object Succecc");
    }
}
