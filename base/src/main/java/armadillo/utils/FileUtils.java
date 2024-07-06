package armadillo.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class FileUtils {
    public static byte[] toByte(InputStream inputStream) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] bs = new byte[1024];
        int len = 0;
        while ((len = inputStream.read(bs)) != -1) {
            os.write(bs, 0, len);
            os.flush();
        }
        inputStream.close();
        os.close();
        return os.toByteArray();
    }

    public static void delete(String file) {
        delete(new File(file));
    }

    public static void delete(File file) {
        if (!file.exists())
            return;
        if (file.isDirectory()) {
            File[] subs = file.listFiles();
            for (File sub : Objects.requireNonNull(subs)) {
                delete(sub);
            }
        }
        if (!file.delete())
            delete(file);
    }
}
