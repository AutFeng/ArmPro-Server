package armadillo.plugin;

import armadillo.utils.StreamUtil;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class PluginClassloader extends ClassLoader {
    private final String classpath;

    public PluginClassloader(ClassLoader parent, String classpath) {
        super(parent);
        this.classpath = classpath;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] bytes = loadClassData(name);
        if (bytes != null)
            return defineClass(name, bytes, 0, bytes.length);
        return super.findClass(name);
    }

    private byte[] loadClassData(String cls) {
        if (classpath != null) {
            if (classpath.endsWith(".zip") || classpath.endsWith(".jar")) {
                try (ZipFile zipFile = new ZipFile(classpath)) {
                    InputStream stream = zipFile.getInputStream(new ZipEntry(cls.replace(".", "/") + ".class"));
                    return StreamUtil.readBytes(stream);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}
