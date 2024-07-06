package armadillo.utils;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipUtils {
    public static void zipUncompress(String inputFile, String destDirPath) throws Exception {
        File srcFile = new File(inputFile);
        ZipFile zipFile = new ZipFile(srcFile);
        Enumeration<?> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) entries.nextElement();
            if (!entry.isDirectory()) {
                File dirPath = new File(destDirPath, entry.getName());
                if (!dirPath.exists())
                    dirPath.mkdirs();
            } else {
                File targetFile = new File(destDirPath, entry.getName());
                if (!targetFile.getParentFile().exists())
                    targetFile.getParentFile().mkdirs();
                targetFile.createNewFile();
                InputStream is = zipFile.getInputStream(entry);
                FileOutputStream fos = new FileOutputStream(targetFile);
                int len;
                byte[] buf = new byte[1024];
                while ((len = is.read(buf)) != -1) {
                    fos.write(buf, 0, len);
                }
                fos.close();
                is.close();
            }
        }
    }

    public static void zipUncompress( ZipInputStream input, String destDirPath) throws Exception {
        ZipEntry entry = null;
        while ((entry = input.getNextEntry()) != null) {
            if (entry.isDirectory()) {
                File dirPath = new File(destDirPath, entry.getName());
                if (!dirPath.exists())
                    dirPath.mkdirs();
            } else {
                File targetFile = new File(destDirPath, entry.getName());
                if (!targetFile.getParentFile().exists())
                    targetFile.getParentFile().mkdirs();
                targetFile.createNewFile();
                FileOutputStream fos = new FileOutputStream(targetFile);
                byte[] bs = new byte[1024];
                int len;
                while ((len = input.read(bs)) != -1)
                    fos.write(bs, 0, len);
                fos.close();
            }
            input.closeEntry();
        }
        input.close();
    }

    public static void addZipEntry( ZipOutputStream zipOutputStream, ZipEntry zipEntry, InputStream stream) throws IOException {
        zipOutputStream.putNextEntry(zipEntry);
        zipOutputStream.write(StreamUtil.readBytes(stream));
        zipOutputStream.closeEntry();
    }

    public static void addZipEntry( ZipOutputStream zipOutputStream, ZipEntry zipEntry, byte[] bytes) throws IOException {
        zipOutputStream.putNextEntry(zipEntry);
        zipOutputStream.write(bytes);
        zipOutputStream.closeEntry();
    }
}
