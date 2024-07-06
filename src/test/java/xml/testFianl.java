package xml;

public class testFianl {
    public final String test;

    public testFianl(String test) {
        this.test = test;
    }

    public static final testFianl createController(String callbacks) {
        return new testFianl(callbacks);
    }
}
