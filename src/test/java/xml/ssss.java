package xml;

import java.lang.reflect.Field;

public class ssss {
    public static void set(TestRefle refle)  {
        Class<?> cls = refle.getClass();
        while (true) {
            try {
                Field declaredField = cls.getDeclaredField("test");
                declaredField.setAccessible(true);
                declaredField.set(null,refle);
                return;
            } catch (Exception e) {
                cls = cls.getSuperclass();
            }
        }
    }

    public static String get(TestRefle refle)  {
        Class<?> cls = refle.getClass();
        while (true) {
            try {
                Field declaredField = cls.getDeclaredField("test");
                declaredField.setAccessible(true);
                return (String) declaredField.get(refle);
            } catch (Exception e) {
                cls = cls.getSuperclass();
            }
        }
    }
}
