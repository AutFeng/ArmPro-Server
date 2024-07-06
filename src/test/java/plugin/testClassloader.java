package plugin;

import armadillo.plugin.PluginClassloader;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class testClassloader {
    @Test
    public void test() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        PluginClassloader classloader = new PluginClassloader(ClassLoader.getSystemClassLoader(), "D:\\工程项目\\armardillo\\Plugin\\build\\libs\\Plugin-1.0-SNAPSHOT.jar");
        Class<?> loadClass = classloader.loadClass("armadillo.test");
        Method method = loadClass.getDeclaredMethod("a");
        method.invoke(null);
    }
}
