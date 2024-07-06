import org.junit.Test;

import java.util.Base64;
import java.util.UUID;

public class FlowTest {
    @Test
    public void aaa() throws InterruptedException {
        while (true){
            int uuid = Base64.getEncoder().encode(UUID.randomUUID().toString().getBytes()).length;
            System.out.println(uuid);
            Thread.sleep(200);
        }
//        if (FlowTest.class.getSimpleName().length() == 8) {
//
//        }
    }

    public void test(int i) {
        switch (FlowTest.class.getSimpleName().hashCode() ^ 111111) {
            case 1:
            case 2:
            case 3:
            case 0:
                System.out.println(i);
                break;
            case 5:
                System.exit(0);
                break;
            case -464546486:
                System.exit(1);
                break;
            default:
                System.currentTimeMillis();
                break;
        }
    }
}
