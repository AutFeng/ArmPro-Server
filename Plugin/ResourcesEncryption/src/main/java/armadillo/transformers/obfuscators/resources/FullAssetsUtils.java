package armadillo.transformers.obfuscators.resources;

import armadillo.utils.LoaderRes;
import armadillo.utils.SmaliUtils;
import org.jf.dexlib2.iface.ClassDef;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class FullAssetsUtils {
    private static final String transformation = "AES/CBC/PKCS5Padding";

    public static byte[] write(HashMap<String, byte[]> s, String pass) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(out);
        dataOutputStream.writeBytes("Armadillo");
        //字符串总量
        int size = s.size();
        //写字符串总量大小
        dataOutputStream.writeInt(size);


        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        DataOutputStream mStrings = new DataOutputStream(bOut);
        int[] offset = new int[size];
        int key = 0;
        for (Map.Entry<String, byte[]> entry : s.entrySet()) {
            byte[] buf = entry.getKey().getBytes(StandardCharsets.UTF_8);
            //写字符串byte长度
            mStrings.writeInt(buf.length);
            //写字符串数据
            mStrings.write(buf, 0, buf.length);
            //写数据
            byte[] data = encrypt(entry.getValue(), pass);
            mStrings.write(data);
            offset[key] = data.length;
            key++;
        }
        //写数据偏移
        for (int i : offset)
            dataOutputStream.writeInt(i);
        dataOutputStream.write(bOut.toByteArray());
        dataOutputStream.close();
        return out.toByteArray();
    }

    public static byte[] encrypt(byte[] data, String pass) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            String p = md5(pass).substring(8, 24);
            IvParameterSpec zeroIv = new IvParameterSpec(p.getBytes());
            SecretKeySpec key1 = new SecretKeySpec(p.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance(transformation);
            cipher.init(Cipher.ENCRYPT_MODE, key1, zeroIv);
            CipherInputStream cipherInputStream = new CipherInputStream(new ByteArrayInputStream(data), cipher);
            byte[] cache = new byte[1024];
            int nRead = 0;
            while ((nRead = cipherInputStream.read(cache)) != -1) {
                outputStream.write(cache, 0, nRead);
                outputStream.flush();
            }
            cipherInputStream.close();
            return outputStream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String md5(String string) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] bytes = md5.digest(string.getBytes());
            String result = "";
            for (byte b : bytes) {
                String temp = Integer.toHexString(Integer.parseInt(Integer.toOctalString(b & 0xFF)));
                result += temp;
            }
            return result;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static ClassDef CreateDecrypt(String className) throws Exception {
        byte[] bytes = LoaderRes.getInstance().getStaticResAsBytes("smali/AssetsRes.smali");
        String body = new String(bytes).replace("LArmadillo/Res;", "LArmadillo/" + className + ";");
        return SmaliUtils.assembleSmali(body.getBytes());
    }
}
