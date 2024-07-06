import armadillo.Application;
import armadillo.Arm;
import armadillo.Constant;
import armadillo.enums.LanguageEnums;
import armadillo.plugin.PluginClassloader;
import armadillo.result.TaskInfo;
import armadillo.transformers.base.BaseTransformer;
import armadillo.utils.FileUtils;
import armadillo.utils.StringPool;
import org.apache.log4j.PropertyConfigurator;
import org.jf.dexlib2.AccessFlags;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class InvoketTest {
    @Test
    public void Test() {
        try {
            Application.InitDir();
            PropertyConfigurator.configure(InvoketTest.class.getClassLoader().getResourceAsStream(Constant.getProfile() + "/log4j.properties"));
            List<TaskInfo> task = new ArrayList<>();
            String uuid = "Test.zip";
            List<byte[]> dexs = new ArrayList<>();
            //ZipFile zipFile = new ZipFile("C:\\Users\\Administrator\\Desktop\\6666.apk");
            ZipFile zipFile = new ZipFile("C:\\Users\\Administrator\\AndroidStudioProjects\\ArmJiagu\\Test\\build\\intermediates\\javac\\debug\\classes\\arm\\verify\\test\\Test.zip");
            Enumeration<? extends ZipEntry> enumeration = zipFile.entries();
            while (enumeration.hasMoreElements()) {
                ZipEntry zipEntry = enumeration.nextElement();
                if (zipEntry.getName().startsWith("classes") && zipEntry.getName().endsWith("dex"))
                    dexs.add(FileUtils.toByte(zipFile.getInputStream(new ZipEntry(zipEntry.getName()))));
            }
            Arm arm = new Arm(zipFile, task, dexs, uuid, LanguageEnums.DEFAULT);
            arm.setConfig("{\"137438953472\":[\"Larm/verify/test/Test;\"],\"METHOD_SEPARATE\":[\"Lcom/Ks/MainActivity;\"]}");
            {
                PluginClassloader pluginClassloader = new PluginClassloader(
                        ClassLoader.getSystemClassLoader(),
                        Constant.getPlugin().getAbsolutePath() + File.separator + "VmpProtect-1.0-SNAPSHOT.jar");
                Class<?> loadClass = pluginClassloader.loadClass("armadillo.transformers.protece.vm.VmProtect");
                Object newInstance = loadClass.newInstance();
                arm.addTransformer((BaseTransformer) newInstance);
            }
            arm.Run();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static String rc4(String jiami) {
        String aKey = "ArmVmp";
        int[] iS = new int[256];
        byte[] iK = new byte[256];
        for (int i = 0; i < 256; i++)
            iS[i] = i;
        int j = 1;
        for (short i = 0; i < 256; i++) {
            System.out.println("char:" +(char) aKey.charAt((i % aKey.length())));
            iK[i] = (byte) aKey.charAt((i % aKey.length()));
        }
        j = 0;
        for (int i = 0; i < 255; i++) {
            j = (j + iS[i] + iK[i]) % 256;
            int temp = iS[i];
            iS[i] = iS[j];
            iS[j] = temp;
        }
        int i = 0;
        j = 0;
        char[] iInputChar = jiami.toCharArray();
        char[] iOutputChar = new char[iInputChar.length];
        for (short x = 0; x < iInputChar.length; x++) {
            i = (i + 1) % 256;
            j = (j + iS[i]) % 256;
            int temp = iS[i];
            iS[i] = iS[j];
            iS[j] = temp;
            int t = (iS[i] + (iS[j] % 256)) % 256;
            int iY = iS[t];
            char iCY = (char) iY;
            iOutputChar[x] = (char) (iInputChar[x] ^ iCY);
        }
        return new String(iOutputChar);
    }

    @Test
    public void aaa(){
        AccessFlags[] accessFlagsForField = AccessFlags.getAccessFlagsForField(0xa);
        for (AccessFlags flags : accessFlagsForField) {
            System.out.println(flags.toString());
        }
    }
}
