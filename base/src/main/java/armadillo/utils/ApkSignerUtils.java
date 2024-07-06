package armadillo.utils;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Base64;
import java.util.Collection;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ApkSignerUtils {
    public static String getApkSignatureData(ZipFile zipFile) throws Exception {
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry ze = entries.nextElement();
            String name = ze.getName().toUpperCase();
            if (name.startsWith("META-INF/") && (name.endsWith(".RSA") || name.endsWith(".DSA"))) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream(baos);
                CertificateFactory factory = CertificateFactory.getInstance("X.509");
                Collection<? extends Certificate> certificates = factory.generateCertificates(zipFile.getInputStream(ze));
                dos.write(certificates.size());
                for (Certificate certificate : certificates) {
                    byte[] encoded = certificate.getEncoded();
                    dos.writeInt(encoded.length);
                    dos.write(encoded);
                }
                return Base64.getEncoder().encodeToString(baos.toByteArray());
            }
        }
        throw new Exception("META-INF/XXX.RSA (DSA) file not found.");
    }
}
