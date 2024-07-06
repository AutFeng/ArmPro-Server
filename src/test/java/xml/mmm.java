package xml;

import armadillo.Application;
import armadillo.Constant;
import armadillo.result.TaskInfo;
import org.apache.log4j.PropertyConfigurator;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.iface.ClassDef;
import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class mmm {
    private String a;
    public String b;

    public static void aaa() {

    }

    private static void bbb() {

    }

    @Test
    public void testNode() {
        try {
            Application.InitDir();
            PropertyConfigurator.configure(mmm.class.getClassLoader().getResourceAsStream(Constant.getProfile() + "/log4j.properties"));
            List<TaskInfo> task = new ArrayList<>();
            String uuid = "Test.zip";
            List<HashSet<ClassDef>> dexs = new ArrayList<>();
            ZipFile zipFile = new ZipFile("C:\\Users\\Administrator\\Desktop\\混淆后.zip");
            Enumeration<? extends ZipEntry> enumeration = zipFile.entries();
            while (enumeration.hasMoreElements()) {
                ZipEntry zipEntry = enumeration.nextElement();
                if (zipEntry.isDirectory())
                    continue;
                if (zipEntry.getName().startsWith("classes") && zipEntry.getName().endsWith("dex"))
                    dexs.add(new HashSet<>(DexBackedDexFile.fromInputStream(Opcodes.getDefault(),new BufferedInputStream(zipFile.getInputStream(new ZipEntry(zipEntry.getName())))).getClasses()));
                else
                    continue;
            }
            new Tree(dexs);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
