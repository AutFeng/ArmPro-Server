import armadillo.Arm;
import org.junit.Test;
import xml.mmm;

import java.lang.reflect.Field;

public class tryTest {
    public static Arm test(Arm arm) {
        return new Arm(null, null, null, null, null);
    }

    @Test
    public void teee() throws NoSuchFieldException, IllegalAccessException {
        mmm sss = new mmm();
        String s = "123";
        sss(s, sss);
        System.out.println(sss);
    }

    public static String sss(String value, mmm sss) throws NoSuchFieldException, IllegalAccessException {
        Field field = sss.getClass().getDeclaredField("a");
        field.setAccessible(true);
        return (String) field.get(sss);
    }
}
