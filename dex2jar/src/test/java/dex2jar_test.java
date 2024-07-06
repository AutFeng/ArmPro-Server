import com.googlecode.d2j.dex.Dex2jar;
import com.googlecode.d2j.reader.BaseDexFileReader;
import com.googlecode.d2j.reader.MultiDexFileReader;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class dex2jar_test {
    @Test
    public void test2j() throws Exception {
        BaseDexFileReader reader = MultiDexFileReader.open(Files.readAllBytes(new File("C:\\Users\\Administrator\\Desktop\\test111.dex").toPath()));
        Dex2jar.from(reader)
                .reUseReg(false)
                .topoLogicalSort()
                .skipDebug(true)
                .optimizeSynchronized(true)
                .printIR(false)
                .noCode(false)
                .skipExceptions(false)
                .to(new File("C:\\Users\\Administrator\\Desktop\\2j.jar").toPath());
    }
}
