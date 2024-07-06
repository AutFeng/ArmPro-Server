import com.ncgcn.apk.ApkAssist;
import org.junit.Test;

import java.io.File;

public class dllTest {
    static {
        System.load("C:\\Users\\Administrator\\Desktop\\ApkAssist\\ApkAssist64.dll");
    }
    @Test
    public void test(){
        File f  = new File(".");
        try{
            System.out.println(f.getCanonicalPath());
            System.out.println(System.getProperty("user.dir") );
        }
        catch(Exception e)
        {
        }
       // System.out.println(dllTest.class.getClassLoader().getResource("a").toString());
        System.out.println(ApkAssist.parseAxml("C:\\Users\\Administrator\\Desktop\\ApkAssist\\test.txt","C:\\Users\\Administrator\\Desktop\\ApkAssist\\AndroidManifest.xml"));
        System.out.println(ApkAssist.buildAxml("C:\\Users\\Administrator\\Desktop\\ApkAssist\\AndroidManifest2.xml","C:\\Users\\Administrator\\Desktop\\ApkAssist\\test.txt"));
        System.out.println(ApkAssist.mergeAxml("C:\\Users\\Administrator\\Desktop\\ApkAssist\\AndroidManifest3.xml","C:\\Users\\Administrator\\Desktop\\ApkAssist\\test.txt"));
    }
}
