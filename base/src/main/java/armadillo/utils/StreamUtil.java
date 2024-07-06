package armadillo.utils;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class StreamUtil {

    public static byte[] readBytes(InputStream is) throws IOException {
        byte[] buf = new byte[10240];
        int num;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while ((num = is.read(buf)) != -1)
            baos.write(buf, 0, num);
        byte[] b = baos.toByteArray();
        baos.close();
        is.close();
        return b;
    }

    public byte[] read(InputStream is) throws IOException {
        byte[] buf = new byte[10240];
        int num;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while ((num = is.read(buf)) != -1)
            baos.write(buf, 0, num);
        byte[] b = baos.toByteArray();
        baos.close();
        is.close();
        return b;
    }
}
