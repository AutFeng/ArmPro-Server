package armadillo.utils;

import armadillo.Constant;

import java.io.*;

public class LoaderRes {
    private final static String root = "static/";
    private static LoaderRes instance = null;

    public static LoaderRes getInstance() {
        synchronized (LoaderRes.class) {
            if (instance == null)
                instance = new LoaderRes();
        }
        return instance;
    }

    public InputStream getStaticResAsStream(String path) throws FileNotFoundException {
        InputStream resource = LoaderRes.class.getClassLoader().getResourceAsStream(path.startsWith("static/") ? path : root + path);
        if (resource == null)
            resource = new FileInputStream(Constant.getRes() + File.separator + root + path);
        return resource;
    }

    public byte[] getStaticResAsBytes(String path) throws IOException {
        return StreamUtil.readBytes(getStaticResAsStream(path));
    }
}
