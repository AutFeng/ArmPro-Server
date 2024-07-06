package call;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class sssss {
    private static void s(MethodSeparateTest separateTest) {
        separateTest.test();

        MethodSeparateTest.test3();

    }


    public static String aaa(MethodSeparateTest test) throws Exception {
        Method method = test.getClass().getDeclaredMethod("name", int.class);
        if (Modifier.isPublic(method.getModifiers()))
            return test.test4();
        else {
            method.setAccessible(true);
            return (String) method.invoke(test, 100);
        }
    }

}
