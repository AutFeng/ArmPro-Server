package call;

import java.lang.reflect.Method;

public class MethodSeparateTest {
    private static String s;
    protected static String s1;
    private String s2;
    protected String s3;
    public void s(Object object)throws Exception{
        Method method = Class.forName(object.getClass().getName()).getDeclaredMethod("test", int.class);
        method.setAccessible(true);
        method.invoke(object,1);
        a ss = new a();
    }

    private class a{

    }
    protected void test(){

    }

    private void test1(){

    }

    private static void test2(MethodSeparateTest test){

    }

    protected static void test3(){

    }

    protected final String test4(){
        return "";
    }

}
