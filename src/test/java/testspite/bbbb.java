package testspite;

import java.lang.reflect.Field;

public class bbbb {
    public static aaaa m1136(){
        return new aaaa();
    }

    public static void m35163(aaaa thing) {
        Class cls = thing.getClass();
        while (true) {
            try {
                Field declaredField = cls.getDeclaredField("aaa");
                declaredField.setAccessible(true);
                declaredField.set(thing, "test acc");
                return;
            } catch (Exception e2) {
                cls = cls.getSuperclass();
            }
        }
    }

}
