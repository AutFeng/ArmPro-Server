package uuid;

import org.junit.Test;

public class UUIDLenTest {
    @Test
    public void testLen() {
        System.out.println(1 << 5 & 1 << 5);
        System.out.println(1 << 5 & 1 << 4);
    }
}
