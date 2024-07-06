package armadillo.transformers.obfuscators.resources;

import com.google.common.collect.Lists;
import org.jf.dexlib2.AccessFlags;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.immutable.ImmutableExceptionHandler;
import org.jf.dexlib2.immutable.ImmutableMethod;
import org.jf.dexlib2.immutable.ImmutableMethodImplementation;
import org.jf.dexlib2.immutable.ImmutableTryBlock;
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction11n;
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction11x;
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction21c;
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction35c;
import org.jf.dexlib2.immutable.reference.ImmutableMethodReference;
import org.jf.dexlib2.immutable.reference.ImmutableStringReference;
import org.jf.dexlib2.immutable.reference.ImmutableTypeReference;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class AssetsUtils {
    public static void decrypt(String sSrc, String key, String ivParameter, String file_name) throws Exception {
        try {
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec iv = new IvParameterSpec(ivParameter.getBytes());
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            /*ByteArrayOutputStream out = new ByteArrayOutputStream();
            ByteArrayInputStream in = new ByteArrayInputStream(Base64.getDecoder().decode(sSrc));
            CipherOutputStream cipherOutputStream = new CipherOutputStream(out, cipher);
            byte[] buffer = new byte[1024];
            int r;
            while ((r = in.read(buffer)) >= 0) {
                cipherOutputStream.write(buffer, 0, r);
            }
            out.close();
            cipherOutputStream.close();*/
            FileOutputStream fileOutputStream = new FileOutputStream("C:\\" + file_name);
            fileOutputStream.write(cipher.doFinal(Base64.getDecoder().decode(sSrc)));
            fileOutputStream.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static String stringToMD5() {
        byte[] secretBytes = null;
        try {
            secretBytes = MessageDigest.getInstance("md5").digest(
                    (System.currentTimeMillis() + new Random().nextInt(999999999) + UUID.randomUUID().toString()).getBytes());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("没有这个md5算法！");
        }
        String md5code = new BigInteger(1, secretBytes).toString(16);
        for (int i = 0; i < 32 - md5code.length(); i++) {
            md5code = "0" + md5code;
        }
        return md5code;
    }

    public static byte[] fileToByte(InputStream in) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream(2048);
        byte[] cache = new byte[1024];
        int nRead = 0;
        while ((nRead = in.read(cache)) != -1) {
            out.write(cache, 0, nRead);
            out.flush();
        }
        out.close();
        in.close();
        return out.toByteArray();
    }

    public static String encryptAssets(byte[] sSrc, String sKey, String ivParameter) {
        try {
            //System.out.println(sKey);
            //System.out.println(ivParameter);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec skeySpec = new SecretKeySpec(sKey.getBytes(), "AES");
            IvParameterSpec iv = new IvParameterSpec(ivParameter.getBytes());
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
            /*ByteArrayOutputStream out = new ByteArrayOutputStream();
            CipherInputStream cipherInputStream = new CipherInputStream(new BufferedInputStream(new ByteArrayInputStream(sSrc)), cipher);
            byte[] cache = new byte[1024];
            int nRead = 0;
            while ((nRead = cipherInputStream.read(cache)) != -1) {
                out.write(cache, 0, nRead);
                out.flush();
            }
            cipherInputStream.close();
            out.close();*/
            return Base64.getEncoder().encodeToString(cipher.doFinal(sSrc));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private static List<String> stringSpilt(String inputString, int length) {
        int size = inputString.length() / length;
        if (inputString.length() % length != 0) {
            size += 1;
        }
        return getStrList(inputString, length, size);
    }

    private static List<String> getStrList(String inputString, int length, int size) {
        List<String> list = new ArrayList<String>();
        for (int index = 0; index < size; index++) {
            String childStr = substring(inputString, index * length,
                    (index + 1) * length);
            list.add(childStr);
        }
        return list;
    }

    private static String substring(String str, int f, int t) {
        if (f > str.length())
            return null;
        if (t > str.length()) {
            return str.substring(f, str.length());
        } else {
            return str.substring(f, t);
        }
    }

    public static List<Method> CreateAsset$(ClassDef classDef, HashMap<String, String> hashMap, ZipFile zipFile) {
        List<Method> methods = new ArrayList<>();
        for (Map.Entry<String, String> entry : hashMap.entrySet()) {
            String key = stringToMD5().substring(0, 16);
            String iv = stringToMD5().substring(0, 16);
            try {
                InputStream inputStream = zipFile.getInputStream(new ZipEntry("assets/" + entry.getValue()));
                byte[] bytes = fileToByte(inputStream);
                String en_buff = encryptAssets(bytes, key, iv);
                List<Instruction> newInsts = new ArrayList<>();
                //new-instance v5, Ljava/io/ByteArrayOutputStream;
                {
                    newInsts.add(new ImmutableInstruction21c(Opcode.NEW_INSTANCE, 5, new ImmutableTypeReference("Ljava/io/ByteArrayOutputStream;")));
                }
                //invoke-direct {v5}, Ljava/io/ByteArrayOutputStream;-><init>()V
                {
                    newInsts.add(new ImmutableInstruction35c(
                            Opcode.INVOKE_DIRECT,
                            1,
                            5,
                            0,
                            0,
                            0,
                            0,
                            new ImmutableMethodReference(
                                    "Ljava/io/ByteArrayOutputStream;",
                                    "<init>",
                                    null,
                                    "V")));
                }
                //new-instance v6, Ljava/lang/StringBuffer;
                {
                    newInsts.add(new ImmutableInstruction21c(Opcode.NEW_INSTANCE, 6, new ImmutableTypeReference("Ljava/lang/StringBuffer;")));
                }
                //invoke-direct {v6}, Ljava/lang/StringBuffer;-><init>()V
                {
                    newInsts.add(new ImmutableInstruction35c(
                            Opcode.INVOKE_DIRECT,
                            1,
                            6,
                            0,
                            0,
                            0,
                            0,
                            new ImmutableMethodReference(
                                    "Ljava/lang/StringBuffer;",
                                    "<init>",
                                    null,
                                    "V")));
                }
                //StringBuffer stringBuffer = new StringBuffer();
                for (String keys : stringSpilt(en_buff, 65534)) {
                    //stringBuffer.append(keys);
                    //System.out.println(keys);
                    //const-string v9, "aaa"
                    newInsts.add(new ImmutableInstruction21c(Opcode.CONST_STRING, 9, new ImmutableStringReference(keys)));
                    //invoke-virtual {v6, v9}, Ljava/lang/StringBuffer;->append(Ljava/lang/String;)Ljava/lang/StringBuffer;
                    newInsts.add(new ImmutableInstruction35c(
                            Opcode.INVOKE_VIRTUAL,
                            2,
                            6,
                            9,
                            0,
                            0,
                            0,
                            new ImmutableMethodReference(
                                    "Ljava/lang/StringBuffer;",
                                    "append",
                                    Lists.newArrayList("Ljava/lang/String;"),
                                    "Ljava/lang/StringBuffer;")));
                }
                /*decrypt(stringBuffer.toString(), key, iv, entry.getValue());
                decrypt(en_buff, key, iv, entry.getValue() + "2");
                if (stringBuffer.toString().equals(en_buff))
                    System.out.println("分割后数据一致");
                else
                    System.out.println("分割后数据不一致");*/
                //const-string v7, "1234567890123456"
                {
                    newInsts.add(new ImmutableInstruction21c(Opcode.CONST_STRING, 7, new ImmutableStringReference(key)));
                }
                //const-string v4, "1201230125462244"
                {
                    newInsts.add(new ImmutableInstruction21c(Opcode.CONST_STRING, 4, new ImmutableStringReference(iv)));
                }
                //invoke-virtual {v6}, Ljava/lang/StringBuffer;->toString()Ljava/lang/String;
                {
                    newInsts.add(new ImmutableInstruction35c(
                            Opcode.INVOKE_VIRTUAL,
                            1,
                            6,
                            0,
                            0,
                            0,
                            0,
                            new ImmutableMethodReference(
                                    "Ljava/lang/StringBuffer;",
                                    "toString",
                                    null,
                                    "Ljava/lang/String;")));
                }
                //move-result-object v9
                {
                    newInsts.add(new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT, 9));
                }
                //const/4 v10, 0x2
                {
                    newInsts.add(new ImmutableInstruction11n(Opcode.CONST_4, 10, 0x2));
                }
                //invoke-static {v9, v10}, Landroid/util/Base64;->decode(Ljava/lang/String;I)[B
                {
                    newInsts.add(new ImmutableInstruction35c(
                            Opcode.INVOKE_STATIC,
                            2,
                            9,
                            10,
                            0,
                            0,
                            0,
                            new ImmutableMethodReference(
                                    "Landroid/util/Base64;",
                                    "decode",
                                    Lists.newArrayList("Ljava/lang/String;", "I"),
                                    "[B")));
                }
                //move-result-object v0
                {
                    newInsts.add(new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT, 0));
                }
                //new-instance v8, Ljavax/crypto/spec/SecretKeySpec;
                {
                    newInsts.add(new ImmutableInstruction21c(Opcode.NEW_INSTANCE, 8, new ImmutableTypeReference("Ljavax/crypto/spec/SecretKeySpec;")));
                }
                //invoke-virtual {v7}, Ljava/lang/String;->getBytes()[B
                {
                    newInsts.add(new ImmutableInstruction35c(
                            Opcode.INVOKE_VIRTUAL,
                            1,
                            7,
                            0,
                            0,
                            0,
                            0,
                            new ImmutableMethodReference(
                                    "Ljava/lang/String;",
                                    "getBytes",
                                    null,
                                    "[B")
                    ));
                }
                //move-result-object v9
                {
                    newInsts.add(new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT, 9));
                }
                //const-string v10, "AES"
                {
                    newInsts.add(new ImmutableInstruction21c(Opcode.CONST_STRING, 10, new ImmutableStringReference("AES")));
                }
                //invoke-direct {v8, v9, v10}, Ljavax/crypto/spec/SecretKeySpec;-><init>([BLjava/lang/String;)V
                {
                    newInsts.add(new ImmutableInstruction35c(
                            Opcode.INVOKE_DIRECT,
                            3,
                            8,
                            9,
                            10,
                            0,
                            0,
                            new ImmutableMethodReference(
                                    "Ljavax/crypto/spec/SecretKeySpec;",
                                    "<init>",
                                    Lists.newArrayList("[B", "Ljava/lang/String;"),
                                    "V")
                    ));
                }
                //const-string v9, "AES/CBC/PKCS5Padding"
                {
                    newInsts.add(new ImmutableInstruction21c(Opcode.CONST_STRING, 9, new ImmutableStringReference("AES/CBC/PKCS5Padding")));
                }
                //invoke-static {v9}, Ljavax/crypto/Cipher;->getInstance(Ljava/lang/String;)Ljavax/crypto/Cipher;
                {
                    newInsts.add(new ImmutableInstruction35c(
                            Opcode.INVOKE_STATIC,
                            1,
                            9,
                            0,
                            0,
                            0,
                            0,
                            new ImmutableMethodReference(
                                    "Ljavax/crypto/Cipher;",
                                    "getInstance",
                                    Lists.newArrayList("Ljava/lang/String;"),
                                    "Ljavax/crypto/Cipher;")
                    ));
                }
                //move-result-object v1
                {
                    newInsts.add(new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT, 1));
                }
                //new-instance v3, Ljavax/crypto/spec/IvParameterSpec;
                {
                    newInsts.add(new ImmutableInstruction21c(Opcode.NEW_INSTANCE, 3, new ImmutableTypeReference("Ljavax/crypto/spec/IvParameterSpec;")));
                }
                //invoke-virtual {v4}, Ljava/lang/String;->getBytes()[B
                {
                    newInsts.add(new ImmutableInstruction35c(
                            Opcode.INVOKE_VIRTUAL,
                            1,
                            4,
                            0,
                            0,
                            0,
                            0,
                            new ImmutableMethodReference(
                                    "Ljava/lang/String;",
                                    "getBytes",
                                    null,
                                    "[B")
                    ));
                }
                //move-result-object v9
                {
                    newInsts.add(new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT, 9));
                }
                //invoke-direct {v3, v9}, Ljavax/crypto/spec/IvParameterSpec;-><init>([B)V
                {
                    newInsts.add(new ImmutableInstruction35c(
                            Opcode.INVOKE_DIRECT,
                            2,
                            3,
                            9,
                            0,
                            0,
                            0,
                            new ImmutableMethodReference(
                                    "Ljavax/crypto/spec/IvParameterSpec;",
                                    "<init>",
                                    Lists.newArrayList("[B"),
                                    "V")
                    ));
                }
                //const/4 v9, 0x2
                {
                    newInsts.add(new ImmutableInstruction11n(Opcode.CONST_4, 9, 0x2));
                }
                //invoke-virtual {v1, v9, v8, v3}, Ljavax/crypto/Cipher;->init(ILjava/security/Key;Ljava/security/spec/AlgorithmParameterSpec;)V
                {
                    newInsts.add(new ImmutableInstruction35c(
                            Opcode.INVOKE_VIRTUAL,
                            4,
                            1,
                            9,
                            8,
                            3,
                            0,
                            new ImmutableMethodReference(
                                    "Ljavax/crypto/Cipher;",
                                    "init",
                                    Lists.newArrayList("I", "Ljava/security/Key;", "Ljava/security/spec/AlgorithmParameterSpec;"),
                                    "V")
                    ));
                }
                //invoke-virtual {v1, v0}, Ljavax/crypto/Cipher;->doFinal([B)[B
                {
                    newInsts.add(new ImmutableInstruction35c(
                            Opcode.INVOKE_VIRTUAL,
                            2,
                            1,
                            0,
                            0,
                            0,
                            0,
                            new ImmutableMethodReference(
                                    "Ljavax/crypto/Cipher;",
                                    "doFinal",
                                    Lists.newArrayList("[B"),
                                    "[B")
                    ));
                }
                //move-result-object v9
                {
                    newInsts.add(new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT, 9));
                }
                //invoke-virtual {v5, v9}, Ljava/io/ByteArrayOutputStream;->write([B)V
                {
                    newInsts.add(new ImmutableInstruction35c(
                            Opcode.INVOKE_VIRTUAL,
                            2,
                            5,
                            9,
                            0,
                            0,
                            0,
                            new ImmutableMethodReference(
                                    "Ljava/io/ByteArrayOutputStream;",
                                    "write",
                                    Lists.newArrayList("[B"),
                                    "V")
                    ));
                }
                //invoke-virtual {v5}, Ljava/io/ByteArrayOutputStream;->close()V
                {
                    newInsts.add(new ImmutableInstruction35c(
                            Opcode.INVOKE_VIRTUAL,
                            1,
                            5,
                            0,
                            0,
                            0,
                            0,
                            new ImmutableMethodReference(
                                    "Ljava/io/ByteArrayOutputStream;",
                                    "close",
                                    null,
                                    "V")
                    ));
                }
                //new-instance v9, Ljava/io/ByteArrayInputStream;
                {
                    newInsts.add(new ImmutableInstruction21c(Opcode.NEW_INSTANCE, 9, new ImmutableTypeReference("Ljava/io/ByteArrayInputStream;")));
                }
                //invoke-virtual {v5}, Ljava/io/ByteArrayOutputStream;->toByteArray()[B
                {
                    newInsts.add(new ImmutableInstruction35c(
                            Opcode.INVOKE_VIRTUAL,
                            1,
                            5,
                            0,
                            0,
                            0,
                            0,
                            new ImmutableMethodReference(
                                    "Ljava/io/ByteArrayOutputStream;",
                                    "toByteArray",
                                    null,
                                    "[B")
                    ));
                }
                //move-result-object v10
                {
                    newInsts.add(new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT, 10));
                }
                //invoke-direct {v9, v10}, Ljava/io/ByteArrayInputStream;-><init>([B)V
                {
                    newInsts.add(new ImmutableInstruction35c(
                            Opcode.INVOKE_DIRECT,
                            2,
                            9,
                            10,
                            0,
                            0,
                            0,
                            new ImmutableMethodReference(
                                    "Ljava/io/ByteArrayInputStream;",
                                    "<init>",
                                    Lists.newArrayList("[B"),
                                    "V")
                    ));
                }
                //return-object v9
                {
                    newInsts.add(new ImmutableInstruction11x(Opcode.RETURN_OBJECT, 9));
                }
                //move-exception v2
                {
                    newInsts.add(new ImmutableInstruction11x(Opcode.MOVE_EXCEPTION, 2));
                }
                //new-instance v9, Landroid/util/AndroidRuntimeException;
                {
                    newInsts.add(new ImmutableInstruction21c(Opcode.NEW_INSTANCE, 9, new ImmutableTypeReference("Landroid/util/AndroidRuntimeException;")));
                }
                //invoke-direct {v9, v2}, Landroid/util/AndroidRuntimeException;-><init>(Ljava/lang/Exception;)V
                {
                    newInsts.add(new ImmutableInstruction35c(
                            Opcode.INVOKE_DIRECT,
                            2,
                            9,
                            2,
                            0,
                            0,
                            0,
                            new ImmutableMethodReference(
                                    "Landroid/util/AndroidRuntimeException;",
                                    "<init>",
                                    Lists.newArrayList("Ljava/lang/Exception;"),
                                    "V")
                    ));
                }
                //throw v9
                {
                    newInsts.add(new ImmutableInstruction11x(Opcode.THROW, 9));
                }
                {
                    int end_try = 0;
                    for (Instruction instruction : newInsts) {
                        if (instruction.getOpcode() == Opcode.RETURN_OBJECT)
                            break;
                        end_try += instruction.getCodeUnits();
                    }
                    int handlerCodeAddress = 0;
                    for (Instruction instruction : newInsts) {
                        if (instruction.getOpcode() == Opcode.MOVE_EXCEPTION)
                            break;
                        handlerCodeAddress += instruction.getCodeUnits();
                    }
                    List<ImmutableExceptionHandler> exceptionHandlers = new ArrayList<>();
                    exceptionHandlers.add(new ImmutableExceptionHandler("Ljava/lang/Exception;", handlerCodeAddress));
                    methods.add(new ImmutableMethod(
                            classDef.getType(),
                            entry.getKey(),
                            null,
                            "Ljava/io/InputStream;",
                            AccessFlags.PRIVATE.getValue() | AccessFlags.STATIC.getValue(),
                            null,
                            null,
                            new ImmutableMethodImplementation(
                                    11,
                                    newInsts,
                                    Lists.newArrayList(new ImmutableTryBlock(
                                            0,
                                            end_try,
                                            exceptionHandlers)),
                                    null)));
                }
            } catch (Exception ignored) {
            }
        }
        return methods;
    }
}
