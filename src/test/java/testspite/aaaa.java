package testspite;

public final class aaaa {
    private static volatile aaaa instance = null;
    private String aaa = "aaaa";
    aaaa() {
        bbbb.m35163(this);
    }
    public static aaaa getInstance() {
        synchronized (aaaa.class) {
            if (instance == null) {
                instance = bbbb.m1136();
            }
        }
        return instance;
    }

    public String getAaa() {
        return aaa;
    }
}
