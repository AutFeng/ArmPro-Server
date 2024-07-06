package armadillo.utils.axml;

import javax.xml.parsers.DocumentBuilderFactory;

public class XmlSecurity {

    private static DocumentBuilderFactory secureDbf = null;

    public static DocumentBuilderFactory getSecureDbf() {
        synchronized (XmlSecurity.class) {
            if (secureDbf == null)
                secureDbf = DocumentBuilderFactory.newInstance();
        }
        return secureDbf;
    }
}
