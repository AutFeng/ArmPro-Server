package armadillo.call;

import org.junit.Test;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

public class test {
    private static String[] strings;


    static {
        strings = new String[2];
        strings[0] = "armadillo.call.test$aaaa:out:(Ljava/lang/String;)V:super";
    }

    @Test
    public void test() throws Throwable {
        testsuper testsuper = new testsuper();
        Method method = testsuper.getClass().getMethod("String", null);
        method.invoke(testsuper);


        MethodHandles.Lookup lookup = MethodHandles.lookup();
    }

    public class testsuper extends aaaa {

    }

    public class aaaa extends bbbb {

        protected void out(String string) {
            System.out.println(string);
        }

        private void outp(String string) {
            System.out.println(string);
        }
    }

    public class bbbb {
        public void test(String string) {
            System.out.println(string);
        }

        public String String() {
            System.out.println("调用成功");
            return "bbbb{}";
        }
    }

    public static void test(boolean a,
                            byte b,
                            short c,
                            char d,
                            int e,
                            long f,
                            float g,
                            double h,
                            test test,
                            int aaa,
                            int bbb,
                            int ccc,
                            int ddd,
                            int eee,
                            int fff,
                            int hhh,
                            int iii,
                            boolean cxcx) {
        boolean aa = a;
        byte bb = b;
        short cc = c;
        char dd = d;
        int ee = e;
        long ff = f;
        float gg = g;
        double hh = h;
        double ssss = hh;
        test ssssss = test;
        int aaaa = aaa;
        int bbbb = bbb;
        int cccc = ccc;
        int dddd = ddd;
        int eeee = eee;
        int ffff = fff;
        int hhhh = hhh;
        int iiii = iii;
        boolean ttttt = cxcx;
        Object object = (Object) ssssss;
        int zzz = 100;
        Object object1 = (Object) zzz;
        boolean kkkk = true;
        Object object2 = (Object) kkkk;
        System.out.println(ttttt);
    }


    @Test
    public void aaa() {
        //System.out.println(test.class.getClass().getSimpleName());
//        if ("   ".length() != 1) {
//            int sss = 2017238805;
//            System.out.println(sss);
//        }else
//            System.out.println();


//        switch (test.class.getSimpleName().hashCode() ^ test.class.getName().hashCode()) {
//            case 375777739:
//                System.out.println();
//                break;
//            case 100:
//            case 200:
//                System.exit(1);
//        }
    }

    @Test
    public static void ccccc() {
        test.class.getSimpleName();
//        switch (this.getClass().getSimpleName().hashCode() ^ this.getClass().getName().hashCode()) {
//            case 375777739:
//                System.out.println();
//                break;
//            case 100:
//            case 200:
//                System.exit(1);
//        }
    }
}
