package jiagu;

import armadillo.utils.StreamUtil;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class enTest {
    @Test
    public void t() throws IOException {
        byte[] bytes = StreamUtil.readBytes(new FileInputStream("D:\\Project\\StubApp\\app\\src\\main\\assets\\TestProvider.de"));
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) (~bytes[i] & 0x00ff);
        }
        new FileOutputStream("D:\\Project\\StubApp\\app\\src\\main\\assets\\TestProvider.dex").write(bytes);
    }
}
