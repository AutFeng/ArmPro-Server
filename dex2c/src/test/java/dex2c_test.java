import by.radioegor146.NativeObfuscator;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class dex2c_test {
    @Test
    public void dc() throws IOException {
        List<Path> libs = new ArrayList<>();
        new NativeObfuscator().process(Paths.get("C:\\2j.jar"), Paths.get("C:\\out_test\\jni"), libs, Collections.emptyList(),Collections.emptyList());
    }
}
