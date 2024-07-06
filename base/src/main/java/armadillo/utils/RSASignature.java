package armadillo.utils;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class RSASignature {
    private final String SIGN_ALGORITHMS = "SHA1WithRSA";
    private String publicKey;
    private static RSASignature instance = null;

    public RSASignature() {
        try {
            publicKey = new String(LoaderRes.getInstance().getStaticResAsBytes("rsa/sign_public.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static RSASignature getInstance() {
        synchronized (LoaderRes.class) {
            if (instance == null)
                instance = new RSASignature();
        }
        return instance;
    }


    public String sign(String content, String privateKey) {
        try {
            PKCS8EncodedKeySpec priPKCS8 = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKey));
            KeyFactory keyf = KeyFactory.getInstance("RSA");
            PrivateKey priKey = keyf.generatePrivate(priPKCS8);
            Signature signature = Signature.getInstance(SIGN_ALGORITHMS);
            signature.initSign(priKey);
            signature.update(content.getBytes());
            byte[] signed = signature.sign();
            return Base64.getEncoder().encodeToString(signed);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean doCheck(byte[] content, byte[] sign) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            byte[] encodedKey = Base64.getDecoder().decode(publicKey);
            PublicKey pubKey = keyFactory.generatePublic(new X509EncodedKeySpec(encodedKey));
            Signature signature = Signature
                    .getInstance(SIGN_ALGORITHMS);
            signature.initVerify(pubKey);
            signature.update(byteMerger(content, "Armadillo1110300103".getBytes()));
            return signature.verify(sign);
        } catch (Exception ignored) {
        }
        return false;
    }

    public byte[] byteMerger(byte[] bt1, byte[] bt2) {
        byte[] bt3 = new byte[bt1.length + bt2.length];
        System.arraycopy(bt1, 0, bt3, 0, bt1.length);
        System.arraycopy(bt2, 0, bt3, bt1.length, bt2.length);
        return bt3;
    }
}
