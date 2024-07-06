import armadillo.utils.axml.AutoXml.util.StreamUtil;
import org.junit.Test;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class ResTest {
    private static DataInputStream data;
    private static final String transformation = "AES/CBC/PKCS5Padding";
    private static final String pass = "Armadillo";
    private static HashMap<String, byte[]> hashMap = new HashMap<>();

    @Test
    public void test() throws IOException {
        HashMap<String, byte[]> testMap = new HashMap<>();
        testMap.put("测试1", "测试1的数据".getBytes());
        testMap.put("测试2", "测试2的数据".getBytes());
        byte[] body = write(testMap);
        new FileOutputStream("C:\\Users\\Administrator\\Desktop\\test.res").write(body);
        data = new DataInputStream(new ByteArrayInputStream(body));
        System.out.println(new String(StreamUtil.readBytes(of("测试2"))));
        System.out.println(new String(StreamUtil.readBytes(of("测试1"))));
        System.out.println(new String(StreamUtil.readBytes(of("测试2"))));
        System.out.println(new String(StreamUtil.readBytes(of("测试1"))));
    }

    public static InputStream of(String name) {
        try {
            //寻找缓存
            if (hashMap.containsKey(name))
                return new ByteArrayInputStream(hashMap.get(name));
            if (data == null)
                data = new DataInputStream(ResTest.class.getResourceAsStream("Armadillo.res"));
            else {
                byte[] magic = new byte[9];
                data.readFully(magic);
                if ("Armadillo".equals(new String(magic))) {
                    //字符串总量
                    int stringCount = data.readInt();
                    //数据对应偏移
                    int[] m_dataOffsets = readIntArray(stringCount);
                    int i = 0;
                    for (int dataOffsets : m_dataOffsets) {
                        //读字符串长度
                        int length = data.readInt();
                        //读字符串
                        byte[] bytes = new byte[length];
                        data.readFully(bytes);
                        String m_name = new String(bytes, "UTF-8");
                        if (!m_name.equals(name))
                            data.skip(dataOffsets);
                        else {
                            bytes = new byte[dataOffsets];
                            data.readFully(bytes);
                            bytes = decrypt(bytes);
                            hashMap.put(m_name, bytes);
                            return new ByteArrayInputStream(bytes);
                        }
                    }
                }
            }
            data.reset();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                data.reset();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private static int[] readIntArray(int length) throws IOException {
        int[] array = new int[length];
        for (int i = 0; i < length; i++)
            array[i] = data.readInt();
        return array;
    }

    public static byte[] write(HashMap<String, byte[]> s) throws IOException {
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
            byte[] buf = entry.getKey().getBytes("UTF-8");
            //写字符串byte长度
            mStrings.writeInt(buf.length);
            //写字符串数据
            mStrings.write(buf, 0, buf.length);
            //写数据
            byte[] data = encrypt(entry.getValue());
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

    private static byte[] encrypt(byte[] data) {
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

    private static byte[] decrypt(byte[] data) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            String p = md5(pass).substring(8, 24);
            IvParameterSpec zeroIv = new IvParameterSpec(p.getBytes());
            SecretKeySpec key1 = new SecretKeySpec(p.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance(transformation);
            cipher.init(Cipher.DECRYPT_MODE, key1, zeroIv);
            CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, cipher);
            byte [] buffer = new byte [1024];
            int r;
            while ((r = inputStream.read(buffer)) >= 0) {
                cipherOutputStream.write(buffer, 0, r);
            }
            cipherOutputStream.close();
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
}
