package armadillo.utils;

import java.util.Properties;

public class OsUtils {
    public static boolean isOSLinux() {
        Properties prop = System.getProperties();
        String os = prop.getProperty("os.name");
        return os != null && os.toLowerCase().contains("linux");
    }
}
