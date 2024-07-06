package armadillo.transformers.obfuscators.resources;

import armadillo.utils.StreamUtil;
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
import org.jf.dexlib2.immutable.instruction.*;
import org.jf.dexlib2.immutable.reference.ImmutableMethodReference;
import org.jf.dexlib2.immutable.reference.ImmutableStringReference;
import org.jf.dexlib2.immutable.reference.ImmutableTypeReference;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static armadillo.transformers.obfuscators.resources.AssetsUtils.encryptAssets;


public class XmlUtils {
    public static List<Method> CreateXml$(ClassDef classDef, HashMap<String, String> hashMap, ZipFile zipFile) {
        List<Method> methods = new ArrayList<>();
        for (Map.Entry<String, String> entry : hashMap.entrySet()) {
            String key = AssetsUtils.stringToMD5().substring(0, 16);
            String iv = AssetsUtils.stringToMD5().substring(0, 16);
            try {
                InputStream inputStream = zipFile.getInputStream(new ZipEntry(entry.getValue()));
                byte[] bytes = StreamUtil.readBytes(inputStream);
                String en_buff = encryptAssets(bytes, key, iv);
                List<Instruction> newInsts = new ArrayList<>();
                //new-instance v15, Ljava/lang/StringBuffer;
                {
                    newInsts.add(new ImmutableInstruction21c(Opcode.NEW_INSTANCE, 15, new ImmutableTypeReference("Ljava/lang/StringBuffer;")));
                }
                //invoke-direct {v15}, Ljava/lang/StringBuffer;-><init>()V
                {
                    newInsts.add(new ImmutableInstruction35c(
                            Opcode.INVOKE_DIRECT,
                            1,
                            15,
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
                for (String keys : stringSpilt(en_buff, 65534)) {
                    //const-string v20, "aaa"
                    newInsts.add(new ImmutableInstruction21c(Opcode.CONST_STRING, 20, new ImmutableStringReference(keys)));
                    //move-object/from16 v0, v20
                    newInsts.add(new ImmutableInstruction22x(Opcode.MOVE_OBJECT_FROM16, 0, 20));
                    //invoke-virtual {v15, v0}, Ljava/lang/StringBuffer;->append(Ljava/lang/String;)Ljava/lang/StringBuffer;
                    newInsts.add(new ImmutableInstruction35c(
                            Opcode.INVOKE_VIRTUAL,
                            2,
                            15,
                            0,
                            0,
                            0,
                            0,
                            new ImmutableMethodReference(
                                    "Ljava/lang/StringBuffer;",
                                    "append",
                                    Lists.newArrayList("Ljava/lang/String;"),
                                    "Ljava/lang/StringBuffer;")));
                }
                //const-string v16, "1234567890123456"
                {
                    newInsts.add(new ImmutableInstruction21c(Opcode.CONST_STRING, 16, new ImmutableStringReference(key)));
                }
                //const-string v11, "1201230125462244"
                {
                    newInsts.add(new ImmutableInstruction21c(Opcode.CONST_STRING, 11, new ImmutableStringReference(iv)));
                }
                //invoke-virtual {v15}, Ljava/lang/StringBuffer;->toString()Ljava/lang/String;
                {
                    newInsts.add(new ImmutableInstruction35c(
                            Opcode.INVOKE_VIRTUAL,
                            1,
                            15,
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
                //move-result-object v20
                {
                    newInsts.add(new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT, 20));
                }
                //const/16 v21, 0x2
                {
                    newInsts.add(new ImmutableInstruction21s(Opcode.CONST_16, 21, 0x2));
                }
                //invoke-static/range {v20 .. v21}, Landroid/util/Base64;->decode(Ljava/lang/String;I)[B
                {
                    newInsts.add(new ImmutableInstruction3rc(
                            Opcode.INVOKE_STATIC_RANGE,
                            20,
                            2,
                            new ImmutableMethodReference(
                                    "Landroid/util/Base64;",
                                    "decode",
                                    Lists.newArrayList("Ljava/lang/String;", "I"),
                                    "[B")));
                }
                //move-result-object v6
                {
                    newInsts.add(new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT, 6));
                }
                //new-instance v17, Ljavax/crypto/spec/SecretKeySpec;
                {
                    newInsts.add(new ImmutableInstruction21c(Opcode.NEW_INSTANCE, 17, new ImmutableTypeReference("Ljavax/crypto/spec/SecretKeySpec;")));
                }
                //invoke-virtual/range {v16 .. v16}, Ljava/lang/String;->getBytes()[B
                {
                    newInsts.add(new ImmutableInstruction3rc(
                            Opcode.INVOKE_VIRTUAL_RANGE,
                            16,
                            1,
                            new ImmutableMethodReference(
                                    "Ljava/lang/String;",
                                    "getBytes",
                                    null,
                                    "[B")));
                }
                //move-result-object v20
                {
                    newInsts.add(new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT, 20));
                }
                //const-string v21, "AES"
                {
                    newInsts.add(new ImmutableInstruction21c(Opcode.CONST_STRING, 21, new ImmutableStringReference("AES")));
                }
                //move-object/from16 v0, v17
                {
                    newInsts.add(new ImmutableInstruction22x(Opcode.MOVE_OBJECT_FROM16, 0, 17));
                }
                //move-object/from16 v1, v20
                {
                    newInsts.add(new ImmutableInstruction22x(Opcode.MOVE_OBJECT_FROM16, 1, 20));
                }
                //move-object/from16 v2, v21
                {
                    newInsts.add(new ImmutableInstruction22x(Opcode.MOVE_OBJECT_FROM16, 2, 21));
                }
                //invoke-direct {v0, v1, v2}, Ljavax/crypto/spec/SecretKeySpec;-><init>([BLjava/lang/String;)V
                {
                    newInsts.add(new ImmutableInstruction35c(
                            Opcode.INVOKE_DIRECT,
                            3,
                            0,
                            1,
                            2,
                            0,
                            0,
                            new ImmutableMethodReference(
                                    "Ljavax/crypto/spec/SecretKeySpec;",
                                    "<init>",
                                    Lists.newArrayList("[B", "Ljava/lang/String;"),
                                    "V")));
                }
                //const-string v20, "AES/CBC/PKCS5Padding"
                {
                    newInsts.add(new ImmutableInstruction21c(Opcode.CONST_STRING, 20, new ImmutableStringReference("AES/CBC/PKCS5Padding")));
                }
                //invoke-static/range {v20 .. v20}, Ljavax/crypto/Cipher;->getInstance(Ljava/lang/String;)Ljavax/crypto/Cipher;
                {
                    newInsts.add(new ImmutableInstruction3rc(
                            Opcode.INVOKE_STATIC_RANGE,
                            20,
                            1,
                            new ImmutableMethodReference(
                                    "Ljavax/crypto/Cipher;",
                                    "getInstance",
                                    Lists.newArrayList("Ljava/lang/String;"),
                                    "Ljavax/crypto/Cipher;")));
                }
                //move-result-object v7
                {
                    newInsts.add(new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT, 7));
                }
                //new-instance v10, Ljavax/crypto/spec/IvParameterSpec;
                {
                    newInsts.add(new ImmutableInstruction21c(Opcode.NEW_INSTANCE, 10, new ImmutableTypeReference("Ljavax/crypto/spec/IvParameterSpec;")));
                }
                //invoke-virtual {v11}, Ljava/lang/String;->getBytes()[B
                {
                    newInsts.add(new ImmutableInstruction35c(
                            Opcode.INVOKE_VIRTUAL,
                            1,
                            11,
                            0,
                            0,
                            0,
                            0,
                            new ImmutableMethodReference(
                                    "Ljava/lang/String;",
                                    "getBytes",
                                    null,
                                    "[B")));
                }
                //move-result-object v20
                {
                    newInsts.add(new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT, 20));
                }
                //move-object/from16 v0, v20
                {
                    newInsts.add(new ImmutableInstruction22x(Opcode.MOVE_OBJECT_FROM16, 0, 20));
                }
                //invoke-direct {v10, v0}, Ljavax/crypto/spec/IvParameterSpec;-><init>([B)V
                {
                    newInsts.add(new ImmutableInstruction35c(
                            Opcode.INVOKE_DIRECT,
                            2,
                            10,
                            0,
                            0,
                            0,
                            0,
                            new ImmutableMethodReference(
                                    "Ljavax/crypto/spec/IvParameterSpec;",
                                    "<init>",
                                    Lists.newArrayList("[B"),
                                    "V")));
                }
                //const/16 v20, 0x2
                {
                    newInsts.add(new ImmutableInstruction21s(Opcode.CONST_16, 20, 0x2));
                }
                //move/from16 v0, v20
                {
                    newInsts.add(new ImmutableInstruction22x(Opcode.MOVE_FROM16, 0, 20));
                }
                //move-object/from16 v1, v17
                {
                    newInsts.add(new ImmutableInstruction22x(Opcode.MOVE_OBJECT_FROM16, 1, 17));
                }
                //invoke-virtual {v7, v0, v1, v10}, Ljavax/crypto/Cipher;->init(ILjava/security/Key;Ljava/security/spec/AlgorithmParameterSpec;)V
                {
                    newInsts.add(new ImmutableInstruction35c(
                            Opcode.INVOKE_VIRTUAL,
                            4,
                            7,
                            0,
                            1,
                            10,
                            0,
                            new ImmutableMethodReference(
                                    "Ljavax/crypto/Cipher;",
                                    "init",
                                    Lists.newArrayList("I", "Ljava/security/Key;", "Ljava/security/spec/AlgorithmParameterSpec;"),
                                    "V")));
                }
                //const-string v20, "android.content.res.XmlBlock"
                {
                    newInsts.add(new ImmutableInstruction21c(Opcode.CONST_STRING, 20, new ImmutableStringReference("android.content.res.XmlBlock")));
                }
                //invoke-static/range {v20 .. v20}, Ljava/lang/Class;->forName(Ljava/lang/String;)Ljava/lang/Class;
                {
                    newInsts.add(new ImmutableInstruction3rc(
                            Opcode.INVOKE_STATIC_RANGE,
                            20,
                            1,
                            new ImmutableMethodReference(
                                    "Ljava/lang/Class;",
                                    "forName",
                                    Lists.newArrayList("Ljava/lang/String;"),
                                    "Ljava/lang/Class;")));
                }
                //move-result-object v19
                {
                    newInsts.add(new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT, 19));
                }
                //const/16 v20, 0x1
                {
                    newInsts.add(new ImmutableInstruction21s(Opcode.CONST_16, 20, 0x1));
                }
                //move/from16 v0, v20
                {
                    newInsts.add(new ImmutableInstruction22x(Opcode.MOVE_FROM16, 0, 20));
                }
                //new-array v0, v0, [Ljava/lang/Class;
                {
                    newInsts.add(new ImmutableInstruction22c(Opcode.NEW_ARRAY, 0, 0, new ImmutableTypeReference("[Ljava/lang/Class;")));
                }
                //move-object/from16 v20, v0
                {
                    newInsts.add(new ImmutableInstruction22x(Opcode.MOVE_OBJECT_FROM16, 20, 0));
                }
                //const/16 v21, 0x0
                {
                    newInsts.add(new ImmutableInstruction21s(Opcode.CONST_16, 21, 0x0));
                }
                //const-class v22, [B
                {
                    newInsts.add(new ImmutableInstruction21c(Opcode.CONST_CLASS, 22, new ImmutableTypeReference("[B")));
                }
                //aput-object v22, v20, v21
                {
                    newInsts.add(new ImmutableInstruction23x(Opcode.APUT_OBJECT, 22, 20, 21));
                }
                //invoke-virtual/range {v19 .. v20}, Ljava/lang/Class;->getConstructor([Ljava/lang/Class;)Ljava/lang/reflect/Constructor;
                {
                    newInsts.add(new ImmutableInstruction3rc(
                            Opcode.INVOKE_VIRTUAL_RANGE,
                            19,
                            2,
                            new ImmutableMethodReference(
                                    "Ljava/lang/Class;",
                                    "getConstructor",
                                    Lists.newArrayList("[Ljava/lang/Class;"),
                                    "Ljava/lang/reflect/Constructor;")));
                }
                //move-result-object v8
                {
                    newInsts.add(new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT, 8));
                }
                //const/16 v20, 0x1
                {
                    newInsts.add(new ImmutableInstruction21s(Opcode.CONST_16, 20, 0x1));
                }
                //move/from16 v0, v20
                {
                    newInsts.add(new ImmutableInstruction22x(Opcode.MOVE_FROM16, 0, 20));
                }
                //invoke-virtual {v8, v0}, Ljava/lang/reflect/Constructor;->setAccessible(Z)V
                {
                    newInsts.add(new ImmutableInstruction35c(
                            Opcode.INVOKE_VIRTUAL,
                            2,
                            8,
                            0,
                            0,
                            0,
                            0,
                            new ImmutableMethodReference(
                                    "Ljava/lang/reflect/Constructor;",
                                    "setAccessible",
                                    Lists.newArrayList("Z"),
                                    "V")
                    ));
                }
                //const/16 v20, 0x1
                {
                    newInsts.add(new ImmutableInstruction21s(Opcode.CONST_16, 20, 0x1));
                }
                //move/from16 v0, v20
                {
                    newInsts.add(new ImmutableInstruction22x(Opcode.MOVE_FROM16, 0, 20));
                }
                //new-array v0, v0, [Ljava/lang/Object;
                {
                    newInsts.add(new ImmutableInstruction22c(Opcode.NEW_ARRAY, 0, 0, new ImmutableTypeReference("[Ljava/lang/Object;")));
                }
                //move-object/from16 v20, v0
                {
                    newInsts.add(new ImmutableInstruction22x(Opcode.MOVE_OBJECT_FROM16, 20, 0));
                }
                //const/16 v21, 0x0
                {
                    newInsts.add(new ImmutableInstruction21s(Opcode.CONST_16, 21, 0x0));
                }
                //invoke-virtual {v7, v6}, Ljavax/crypto/Cipher;->doFinal([B)[B
                {
                    newInsts.add(new ImmutableInstruction35c(
                            Opcode.INVOKE_VIRTUAL,
                            2,
                            7,
                            6,
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
                //move-result-object v22
                {
                    newInsts.add(new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT, 22));
                }
                //aput-object v22, v20, v21
                {
                    newInsts.add(new ImmutableInstruction23x(Opcode.APUT_OBJECT, 22, 20, 21));
                }
                //move-object/from16 v0, v20
                {
                    newInsts.add(new ImmutableInstruction22x(Opcode.MOVE_OBJECT_FROM16, 0, 20));
                }
                //invoke-virtual {v8, v0}, Ljava/lang/reflect/Constructor;->newInstance([Ljava/lang/Object;)Ljava/lang/Object;
                {
                    newInsts.add(new ImmutableInstruction35c(
                            Opcode.INVOKE_VIRTUAL,
                            2,
                            8,
                            0,
                            0,
                            0,
                            0,
                            new ImmutableMethodReference(
                                    "Ljava/lang/reflect/Constructor;",
                                    "newInstance",
                                    Lists.newArrayList("[Ljava/lang/Object;"),
                                    "Ljava/lang/Object;")
                    ));
                }
                //move-result-object v13
                {
                    newInsts.add(new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT, 13));
                }
                //const-string v20, "newParser"
                {
                    newInsts.add(new ImmutableInstruction21c(Opcode.CONST_STRING, 20, new ImmutableStringReference("newParser")));
                }
                //const/16 v21, 0x0
                {
                    newInsts.add(new ImmutableInstruction21s(Opcode.CONST_16, 21, 0x0));
                }
                //move/from16 v0, v21
                {
                    newInsts.add(new ImmutableInstruction22x(Opcode.MOVE_FROM16, 0, 21));
                }
                //new-array v0, v0, [Ljava/lang/Class;
                {
                    newInsts.add(new ImmutableInstruction22c(Opcode.NEW_ARRAY, 0, 0, new ImmutableTypeReference("[Ljava/lang/Class;")));
                }
                //move-object/from16 v21, v0
                {
                    newInsts.add(new ImmutableInstruction22x(Opcode.MOVE_OBJECT_FROM16, 21, 0));
                }
                //invoke-virtual/range {v19 .. v21}, Ljava/lang/Class;->getDeclaredMethod(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;
                {
                    newInsts.add(new ImmutableInstruction3rc(
                            Opcode.INVOKE_VIRTUAL_RANGE,
                            19,
                            3,
                            new ImmutableMethodReference(
                                    "Ljava/lang/Class;",
                                    "getDeclaredMethod",
                                    Lists.newArrayList("Ljava/lang/String;", "[Ljava/lang/Class;"),
                                    "Ljava/lang/reflect/Method;")
                    ));
                }
                //move-result-object v12
                {
                    newInsts.add(new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT, 12));
                }
                //const/16 v20, 0x1
                {
                    newInsts.add(new ImmutableInstruction21s(Opcode.CONST_16, 20, 0x1));
                }
                //move/from16 v0, v20
                {
                    newInsts.add(new ImmutableInstruction22x(Opcode.MOVE_FROM16, 0, 20));
                }
                //invoke-virtual {v12, v0}, Ljava/lang/reflect/Method;->setAccessible(Z)V
                {
                    newInsts.add(new ImmutableInstruction35c(
                            Opcode.INVOKE_VIRTUAL,
                            2,
                            12,
                            0,
                            0,
                            0,
                            0,
                            new ImmutableMethodReference(
                                    "Ljava/lang/reflect/Method;",
                                    "setAccessible",
                                    Lists.newArrayList("Z"),
                                    "V")
                    ));
                }
                //const/16 v20, 0x0
                {
                    newInsts.add(new ImmutableInstruction21s(Opcode.CONST_16, 20, 0x0));
                }
                //move/from16 v0, v20
                {
                    newInsts.add(new ImmutableInstruction22x(Opcode.MOVE_FROM16, 0, 20));
                }
                //new-array v0, v0, [Ljava/lang/Object;
                {
                    newInsts.add(new ImmutableInstruction22c(Opcode.NEW_ARRAY, 0, 0, new ImmutableTypeReference("[Ljava/lang/Object;")));
                }
                //move-object/from16 v20, v0
                {
                    newInsts.add(new ImmutableInstruction22x(Opcode.MOVE_OBJECT_FROM16, 20, 0));
                }
                //move-object/from16 v0, v20
                {
                    newInsts.add(new ImmutableInstruction22x(Opcode.MOVE_OBJECT_FROM16, 0, 20));
                }
                //invoke-virtual {v12, v13, v0}, Ljava/lang/reflect/Method;->invoke(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;
                {
                    newInsts.add(new ImmutableInstruction35c(
                            Opcode.INVOKE_VIRTUAL,
                            3,
                            12,
                            13,
                            0,
                            0,
                            0,
                            new ImmutableMethodReference(
                                    "Ljava/lang/reflect/Method;",
                                    "invoke",
                                    Lists.newArrayList("Ljava/lang/Object;", "[Ljava/lang/Object;"),
                                    "Ljava/lang/Object;")
                    ));
                }
                //move-result-object v14
                {
                    newInsts.add(new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT, 14));
                }
                //check-cast v14, Landroid/content/res/XmlResourceParser;
                {
                    newInsts.add(new ImmutableInstruction21c(Opcode.CHECK_CAST, 14, new ImmutableTypeReference("Landroid/content/res/XmlResourceParser;")));
                }
                //const-string v20, "android.app.ActivityThread"
                {
                    newInsts.add(new ImmutableInstruction21c(Opcode.CONST_STRING, 20, new ImmutableStringReference("android.app.ActivityThread")));
                }
                //invoke-static/range {v20 .. v20}, Ljava/lang/Class;->forName(Ljava/lang/String;)Ljava/lang/Class;
                {
                    newInsts.add(new ImmutableInstruction3rc(
                            Opcode.INVOKE_STATIC_RANGE,
                            20,
                            1,
                            new ImmutableMethodReference(
                                    "Ljava/lang/Class;",
                                    "forName",
                                    Lists.newArrayList("Ljava/lang/String;"),
                                    "Ljava/lang/Class;")
                    ));
                }
                //move-result-object v3
                {
                    newInsts.add(new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT, 3));
                }
                //const-string v20, "currentApplication"
                {
                    newInsts.add(new ImmutableInstruction21c(Opcode.CONST_STRING, 20, new ImmutableStringReference("currentApplication")));
                }
                //const/16 v21, 0x0
                {
                    newInsts.add(new ImmutableInstruction21s(Opcode.CONST_16, 21, 0x0));
                }
                //move/from16 v0, v21
                {
                    newInsts.add(new ImmutableInstruction22x(Opcode.MOVE_FROM16, 0, 21));
                }
                //new-array v0, v0, [Ljava/lang/Class;
                {
                    newInsts.add(new ImmutableInstruction22c(Opcode.NEW_ARRAY, 0, 0, new ImmutableTypeReference("[Ljava/lang/Class;")));
                }
                //move-object/from16 v21, v0
                {
                    newInsts.add(new ImmutableInstruction22x(Opcode.MOVE_OBJECT_FROM16, 21, 0));
                }
                //move-object/from16 v0, v20
                {
                    newInsts.add(new ImmutableInstruction22x(Opcode.MOVE_OBJECT_FROM16, 0, 20));
                }
                //move-object/from16 v1, v21
                {
                    newInsts.add(new ImmutableInstruction22x(Opcode.MOVE_OBJECT_FROM16, 1, 21));
                }
                //invoke-virtual {v3, v0, v1}, Ljava/lang/Class;->getMethod(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;
                {
                    newInsts.add(new ImmutableInstruction35c(
                            Opcode.INVOKE_VIRTUAL,
                            3,
                            3,
                            0,
                            1,
                            0,
                            0,
                            new ImmutableMethodReference(
                                    "Ljava/lang/Class;",
                                    "getMethod",
                                    Lists.newArrayList("Ljava/lang/String;", "[Ljava/lang/Class;"),
                                    "Ljava/lang/reflect/Method;")
                    ));
                }
                //move-result-object v5
                {
                    newInsts.add(new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT, 5));
                }
                //const/16 v20, 0x0
                {
                    newInsts.add(new ImmutableInstruction21s(Opcode.CONST_16, 20, 0x0));
                }
                //move/from16 v0, v20
                {
                    newInsts.add(new ImmutableInstruction22x(Opcode.MOVE_FROM16, 0, 20));
                }
                //new-array v0, v0, [Ljava/lang/Object;
                {
                    newInsts.add(new ImmutableInstruction22c(Opcode.NEW_ARRAY, 0, 0, new ImmutableTypeReference("[Ljava/lang/Object;")));
                }
                //move-object/from16 v20, v0
                {
                    newInsts.add(new ImmutableInstruction22x(Opcode.MOVE_OBJECT_FROM16, 20, 0));
                }
                //move-object/from16 v0, v20
                {
                    newInsts.add(new ImmutableInstruction22x(Opcode.MOVE_OBJECT_FROM16, 0, 20));
                }
                //invoke-virtual {v5, v3, v0}, Ljava/lang/reflect/Method;->invoke(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;
                {
                    newInsts.add(new ImmutableInstruction35c(
                            Opcode.INVOKE_VIRTUAL,
                            3,
                            5,
                            3,
                            0,
                            0,
                            0,
                            new ImmutableMethodReference(
                                    "Ljava/lang/reflect/Method;",
                                    "invoke",
                                    Lists.newArrayList("Ljava/lang/Object;", "[Ljava/lang/Object;"),
                                    "Ljava/lang/Object;")
                    ));
                }
                //const/16 v20, 0x0
                {
                    newInsts.add(new ImmutableInstruction21s(Opcode.CONST_16, 20, 0x0));
                }
                //move/from16 v0, v20
                {
                    newInsts.add(new ImmutableInstruction22x(Opcode.MOVE_FROM16, 0, 20));
                }
                //new-array v0, v0, [Ljava/lang/Object;
                {
                    newInsts.add(new ImmutableInstruction22c(Opcode.NEW_ARRAY, 0, 0, new ImmutableTypeReference("[Ljava/lang/Object;")));
                }
                //move-object/from16 v20, v0
                {
                    newInsts.add(new ImmutableInstruction22x(Opcode.MOVE_OBJECT_FROM16, 20, 0));
                }
                //move-object/from16 v0, v20
                {
                    newInsts.add(new ImmutableInstruction22x(Opcode.MOVE_OBJECT_FROM16, 0, 20));
                }
                //invoke-virtual {v5, v3, v0}, Ljava/lang/reflect/Method;->invoke(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;
                {
                    newInsts.add(new ImmutableInstruction35c(
                            Opcode.INVOKE_VIRTUAL,
                            3,
                            5,
                            3,
                            0,
                            0,
                            0,
                            new ImmutableMethodReference(
                                    "Ljava/lang/reflect/Method;",
                                    "invoke",
                                    Lists.newArrayList("Ljava/lang/Object;", "[Ljava/lang/Object;"),
                                    "Ljava/lang/Object;")
                    ));
                }
                //move-result-object v4
                {
                    newInsts.add(new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT, 4));
                }
                //check-cast v4, Landroid/app/Application;
                {
                    newInsts.add(new ImmutableInstruction21c(Opcode.CHECK_CAST, 4, new ImmutableTypeReference("Landroid/app/Application;")));
                }
                //invoke-static {v4}, Landroid/view/LayoutInflater;->from(Landroid/content/Context;)Landroid/view/LayoutInflater;
                {
                    newInsts.add(new ImmutableInstruction3rc(
                            Opcode.INVOKE_STATIC_RANGE,
                            23,
                            1,
                            new ImmutableMethodReference(
                                    "Landroid/view/LayoutInflater;",
                                    "from",
                                    Lists.newArrayList("Landroid/content/Context;"),
                                    "Landroid/view/LayoutInflater;")
                    ));
                }
                //move-result-object v20
                {
                    newInsts.add(new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT, 20));
                }
                //const/16 v21, 0x0
                {
                    newInsts.add(new ImmutableInstruction21s(Opcode.CONST_16, 21, 0x0));
                }
                //move-object/from16 v0, v20
                {
                    newInsts.add(new ImmutableInstruction22x(Opcode.MOVE_OBJECT_FROM16, 0, 20));
                }
                //move-object/from16 v1, v21
                {
                    newInsts.add(new ImmutableInstruction22x(Opcode.MOVE_OBJECT_FROM16, 1, 21));
                }
                //invoke-virtual {v0, v14, v1}, Landroid/view/LayoutInflater;->inflate(Lorg/xmlpull/v1/XmlPullParser;Landroid/view/ViewGroup;)Landroid/view/View;
                {
                    newInsts.add(new ImmutableInstruction35c(
                            Opcode.INVOKE_VIRTUAL,
                            3,
                            0,
                            14,
                            1,
                            0,
                            0,
                            new ImmutableMethodReference(
                                    "Landroid/view/LayoutInflater;",
                                    "inflate",
                                    Lists.newArrayList("Lorg/xmlpull/v1/XmlPullParser;", "Landroid/view/ViewGroup;"),
                                    "Landroid/view/View;")
                    ));
                }
                //move-result-object v18
                {
                    newInsts.add(new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT, 18));
                }
                //return-object v18
                {
                    newInsts.add(new ImmutableInstruction11x(Opcode.RETURN_OBJECT, 18));
                }
                //move-exception v9
                {
                    newInsts.add(new ImmutableInstruction11x(Opcode.MOVE_EXCEPTION, 9));
                }
                //new-instance v20, Landroid/util/AndroidRuntimeException;
                {
                    newInsts.add(new ImmutableInstruction21c(Opcode.NEW_INSTANCE, 20, new ImmutableTypeReference("Landroid/util/AndroidRuntimeException;")));
                }
                //move-object/from16 v0, v20
                {
                    newInsts.add(new ImmutableInstruction22x(Opcode.MOVE_OBJECT_FROM16, 0, 20));
                }
                //invoke-direct {v0, v9}, Landroid/util/AndroidRuntimeException;-><init>(Ljava/lang/Exception;)V
                {
                    newInsts.add(new ImmutableInstruction35c(
                            Opcode.INVOKE_DIRECT,
                            2,
                            0,
                            9,
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
                //throw v20
                {
                    newInsts.add(new ImmutableInstruction11x(Opcode.THROW, 20));
                }
                {
                    int end_try = 0;
                    for (Instruction instruction : newInsts) {
                        if (instruction.getOpcode() == Opcode.MOVE_RESULT_OBJECT) {
                            ImmutableInstruction11x instruction11x = (ImmutableInstruction11x) instruction;
                            if (instruction11x.getRegisterA() == 18)
                                break;
                        }
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
                            "Landroid/view/View;",
                            AccessFlags.PUBLIC.getValue(),
                            null,
                            null,
                            new ImmutableMethodImplementation(
                                    24,
                                    newInsts,
                                    Lists.newArrayList(new ImmutableTryBlock(
                                            0,
                                            end_try,
                                            exceptionHandlers)),
                                    null)));
                }
                //diff.add(entry.getValue());
            } catch (Exception ignored) {
            }
        }
        return methods;
    }

    public static List<Method> CreateXmlPull$(ClassDef classDef, HashMap<String, String> hashMap, ZipFile zipFile) {
        List<Method> methods = new ArrayList<>();
        for (Map.Entry<String, String> entry : hashMap.entrySet()) {
            String key = AssetsUtils.stringToMD5().substring(0, 16);
            String iv = AssetsUtils.stringToMD5().substring(0, 16);
            try {
                InputStream inputStream = zipFile.getInputStream(new ZipEntry(entry.getValue()));
                byte[] bytes = StreamUtil.readBytes(inputStream);
                String en_buff = encryptAssets(bytes, key, iv);
                List<Instruction> newInsts = new ArrayList<>();
                //new-instance v15, Ljava/lang/StringBuffer;
                {
                    newInsts.add(new ImmutableInstruction21c(Opcode.NEW_INSTANCE, 15, new ImmutableTypeReference("Ljava/lang/StringBuffer;")));
                }
                //invoke-direct {v15}, Ljava/lang/StringBuffer;-><init>()V
                {
                    newInsts.add(new ImmutableInstruction35c(
                            Opcode.INVOKE_DIRECT,
                            1,
                            15,
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
                for (String keys : stringSpilt(en_buff, 65534)) {
                    //const-string v20, "aaa"
                    newInsts.add(new ImmutableInstruction21c(Opcode.CONST_STRING, 20, new ImmutableStringReference(keys)));
                    //move-object/from16 v0, v20
                    newInsts.add(new ImmutableInstruction22x(Opcode.MOVE_OBJECT_FROM16, 0, 20));
                    //invoke-virtual {v15, v0}, Ljava/lang/StringBuffer;->append(Ljava/lang/String;)Ljava/lang/StringBuffer;
                    newInsts.add(new ImmutableInstruction35c(
                            Opcode.INVOKE_VIRTUAL,
                            2,
                            15,
                            0,
                            0,
                            0,
                            0,
                            new ImmutableMethodReference(
                                    "Ljava/lang/StringBuffer;",
                                    "append",
                                    Lists.newArrayList("Ljava/lang/String;"),
                                    "Ljava/lang/StringBuffer;")));
                }
                //const-string v16, "1234567890123456"
                {
                    newInsts.add(new ImmutableInstruction21c(Opcode.CONST_STRING, 16, new ImmutableStringReference(key)));
                }
                //const-string v11, "1201230125462244"
                {
                    newInsts.add(new ImmutableInstruction21c(Opcode.CONST_STRING, 11, new ImmutableStringReference(iv)));
                }
                //invoke-virtual {v15}, Ljava/lang/StringBuffer;->toString()Ljava/lang/String;
                {
                    newInsts.add(new ImmutableInstruction35c(
                            Opcode.INVOKE_VIRTUAL,
                            1,
                            15,
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
                //move-result-object v20
                {
                    newInsts.add(new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT, 20));
                }
                //const/16 v21, 0x2
                {
                    newInsts.add(new ImmutableInstruction21s(Opcode.CONST_16, 21, 0x2));
                }
                //invoke-static/range {v20 .. v21}, Landroid/util/Base64;->decode(Ljava/lang/String;I)[B
                {
                    newInsts.add(new ImmutableInstruction3rc(
                            Opcode.INVOKE_STATIC_RANGE,
                            20,
                            2,
                            new ImmutableMethodReference(
                                    "Landroid/util/Base64;",
                                    "decode",
                                    Lists.newArrayList("Ljava/lang/String;", "I"),
                                    "[B")));
                }
                //move-result-object v6
                {
                    newInsts.add(new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT, 6));
                }
                //new-instance v17, Ljavax/crypto/spec/SecretKeySpec;
                {
                    newInsts.add(new ImmutableInstruction21c(Opcode.NEW_INSTANCE, 17, new ImmutableTypeReference("Ljavax/crypto/spec/SecretKeySpec;")));
                }
                //invoke-virtual/range {v16 .. v16}, Ljava/lang/String;->getBytes()[B
                {
                    newInsts.add(new ImmutableInstruction3rc(
                            Opcode.INVOKE_VIRTUAL_RANGE,
                            16,
                            1,
                            new ImmutableMethodReference(
                                    "Ljava/lang/String;",
                                    "getBytes",
                                    null,
                                    "[B")));
                }
                //move-result-object v20
                {
                    newInsts.add(new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT, 20));
                }
                //const-string v21, "AES"
                {
                    newInsts.add(new ImmutableInstruction21c(Opcode.CONST_STRING, 21, new ImmutableStringReference("AES")));
                }
                //move-object/from16 v0, v17
                {
                    newInsts.add(new ImmutableInstruction22x(Opcode.MOVE_OBJECT_FROM16, 0, 17));
                }
                //move-object/from16 v1, v20
                {
                    newInsts.add(new ImmutableInstruction22x(Opcode.MOVE_OBJECT_FROM16, 1, 20));
                }
                //move-object/from16 v2, v21
                {
                    newInsts.add(new ImmutableInstruction22x(Opcode.MOVE_OBJECT_FROM16, 2, 21));
                }
                //invoke-direct {v0, v1, v2}, Ljavax/crypto/spec/SecretKeySpec;-><init>([BLjava/lang/String;)V
                {
                    newInsts.add(new ImmutableInstruction35c(
                            Opcode.INVOKE_DIRECT,
                            3,
                            0,
                            1,
                            2,
                            0,
                            0,
                            new ImmutableMethodReference(
                                    "Ljavax/crypto/spec/SecretKeySpec;",
                                    "<init>",
                                    Lists.newArrayList("[B", "Ljava/lang/String;"),
                                    "V")));
                }
                //const-string v20, "AES/CBC/PKCS5Padding"
                {
                    newInsts.add(new ImmutableInstruction21c(Opcode.CONST_STRING, 20, new ImmutableStringReference("AES/CBC/PKCS5Padding")));
                }
                //invoke-static/range {v20 .. v20}, Ljavax/crypto/Cipher;->getInstance(Ljava/lang/String;)Ljavax/crypto/Cipher;
                {
                    newInsts.add(new ImmutableInstruction3rc(
                            Opcode.INVOKE_STATIC_RANGE,
                            20,
                            1,
                            new ImmutableMethodReference(
                                    "Ljavax/crypto/Cipher;",
                                    "getInstance",
                                    Lists.newArrayList("Ljava/lang/String;"),
                                    "Ljavax/crypto/Cipher;")));
                }
                //move-result-object v7
                {
                    newInsts.add(new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT, 7));
                }
                //new-instance v10, Ljavax/crypto/spec/IvParameterSpec;
                {
                    newInsts.add(new ImmutableInstruction21c(Opcode.NEW_INSTANCE, 10, new ImmutableTypeReference("Ljavax/crypto/spec/IvParameterSpec;")));
                }
                //invoke-virtual {v11}, Ljava/lang/String;->getBytes()[B
                {
                    newInsts.add(new ImmutableInstruction35c(
                            Opcode.INVOKE_VIRTUAL,
                            1,
                            11,
                            0,
                            0,
                            0,
                            0,
                            new ImmutableMethodReference(
                                    "Ljava/lang/String;",
                                    "getBytes",
                                    null,
                                    "[B")));
                }
                //move-result-object v20
                {
                    newInsts.add(new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT, 20));
                }
                //move-object/from16 v0, v20
                {
                    newInsts.add(new ImmutableInstruction22x(Opcode.MOVE_OBJECT_FROM16, 0, 20));
                }
                //invoke-direct {v10, v0}, Ljavax/crypto/spec/IvParameterSpec;-><init>([B)V
                {
                    newInsts.add(new ImmutableInstruction35c(
                            Opcode.INVOKE_DIRECT,
                            2,
                            10,
                            0,
                            0,
                            0,
                            0,
                            new ImmutableMethodReference(
                                    "Ljavax/crypto/spec/IvParameterSpec;",
                                    "<init>",
                                    Lists.newArrayList("[B"),
                                    "V")));
                }
                //const/16 v20, 0x2
                {
                    newInsts.add(new ImmutableInstruction21s(Opcode.CONST_16, 20, 0x2));
                }
                //move/from16 v0, v20
                {
                    newInsts.add(new ImmutableInstruction22x(Opcode.MOVE_FROM16, 0, 20));
                }
                //move-object/from16 v1, v17
                {
                    newInsts.add(new ImmutableInstruction22x(Opcode.MOVE_OBJECT_FROM16, 1, 17));
                }
                //invoke-virtual {v7, v0, v1, v10}, Ljavax/crypto/Cipher;->init(ILjava/security/Key;Ljava/security/spec/AlgorithmParameterSpec;)V
                {
                    newInsts.add(new ImmutableInstruction35c(
                            Opcode.INVOKE_VIRTUAL,
                            4,
                            7,
                            0,
                            1,
                            10,
                            0,
                            new ImmutableMethodReference(
                                    "Ljavax/crypto/Cipher;",
                                    "init",
                                    Lists.newArrayList("I", "Ljava/security/Key;", "Ljava/security/spec/AlgorithmParameterSpec;"),
                                    "V")));
                }
                //const-string v20, "android.content.res.XmlBlock"
                {
                    newInsts.add(new ImmutableInstruction21c(Opcode.CONST_STRING, 20, new ImmutableStringReference("android.content.res.XmlBlock")));
                }
                //invoke-static/range {v20 .. v20}, Ljava/lang/Class;->forName(Ljava/lang/String;)Ljava/lang/Class;
                {
                    newInsts.add(new ImmutableInstruction3rc(
                            Opcode.INVOKE_STATIC_RANGE,
                            20,
                            1,
                            new ImmutableMethodReference(
                                    "Ljava/lang/Class;",
                                    "forName",
                                    Lists.newArrayList("Ljava/lang/String;"),
                                    "Ljava/lang/Class;")));
                }
                //move-result-object v19
                {
                    newInsts.add(new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT, 19));
                }
                //const/16 v20, 0x1
                {
                    newInsts.add(new ImmutableInstruction21s(Opcode.CONST_16, 20, 0x1));
                }
                //move/from16 v0, v20
                {
                    newInsts.add(new ImmutableInstruction22x(Opcode.MOVE_FROM16, 0, 20));
                }
                //new-array v0, v0, [Ljava/lang/Class;
                {
                    newInsts.add(new ImmutableInstruction22c(Opcode.NEW_ARRAY, 0, 0, new ImmutableTypeReference("[Ljava/lang/Class;")));
                }
                //move-object/from16 v20, v0
                {
                    newInsts.add(new ImmutableInstruction22x(Opcode.MOVE_OBJECT_FROM16, 20, 0));
                }
                //const/16 v21, 0x0
                {
                    newInsts.add(new ImmutableInstruction21s(Opcode.CONST_16, 21, 0x0));
                }
                //const-class v22, [B
                {
                    newInsts.add(new ImmutableInstruction21c(Opcode.CONST_CLASS, 22, new ImmutableTypeReference("[B")));
                }
                //aput-object v22, v20, v21
                {
                    newInsts.add(new ImmutableInstruction23x(Opcode.APUT_OBJECT, 22, 20, 21));
                }
                //invoke-virtual/range {v19 .. v20}, Ljava/lang/Class;->getConstructor([Ljava/lang/Class;)Ljava/lang/reflect/Constructor;
                {
                    newInsts.add(new ImmutableInstruction3rc(
                            Opcode.INVOKE_VIRTUAL_RANGE,
                            19,
                            2,
                            new ImmutableMethodReference(
                                    "Ljava/lang/Class;",
                                    "getConstructor",
                                    Lists.newArrayList("[Ljava/lang/Class;"),
                                    "Ljava/lang/reflect/Constructor;")));
                }
                //move-result-object v8
                {
                    newInsts.add(new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT, 8));
                }
                //const/16 v20, 0x1
                {
                    newInsts.add(new ImmutableInstruction21s(Opcode.CONST_16, 20, 0x1));
                }
                //move/from16 v0, v20
                {
                    newInsts.add(new ImmutableInstruction22x(Opcode.MOVE_FROM16, 0, 20));
                }
                //invoke-virtual {v8, v0}, Ljava/lang/reflect/Constructor;->setAccessible(Z)V
                {
                    newInsts.add(new ImmutableInstruction35c(
                            Opcode.INVOKE_VIRTUAL,
                            2,
                            8,
                            0,
                            0,
                            0,
                            0,
                            new ImmutableMethodReference(
                                    "Ljava/lang/reflect/Constructor;",
                                    "setAccessible",
                                    Lists.newArrayList("Z"),
                                    "V")
                    ));
                }
                //const/16 v20, 0x1
                {
                    newInsts.add(new ImmutableInstruction21s(Opcode.CONST_16, 20, 0x1));
                }
                //move/from16 v0, v20
                {
                    newInsts.add(new ImmutableInstruction22x(Opcode.MOVE_FROM16, 0, 20));
                }
                //new-array v0, v0, [Ljava/lang/Object;
                {
                    newInsts.add(new ImmutableInstruction22c(Opcode.NEW_ARRAY, 0, 0, new ImmutableTypeReference("[Ljava/lang/Object;")));
                }
                //move-object/from16 v20, v0
                {
                    newInsts.add(new ImmutableInstruction22x(Opcode.MOVE_OBJECT_FROM16, 20, 0));
                }
                //const/16 v21, 0x0
                {
                    newInsts.add(new ImmutableInstruction21s(Opcode.CONST_16, 21, 0x0));
                }
                //invoke-virtual {v7, v6}, Ljavax/crypto/Cipher;->doFinal([B)[B
                {
                    newInsts.add(new ImmutableInstruction35c(
                            Opcode.INVOKE_VIRTUAL,
                            2,
                            7,
                            6,
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
                //move-result-object v22
                {
                    newInsts.add(new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT, 22));
                }
                //aput-object v22, v20, v21
                {
                    newInsts.add(new ImmutableInstruction23x(Opcode.APUT_OBJECT, 22, 20, 21));
                }
                //move-object/from16 v0, v20
                {
                    newInsts.add(new ImmutableInstruction22x(Opcode.MOVE_OBJECT_FROM16, 0, 20));
                }
                //invoke-virtual {v8, v0}, Ljava/lang/reflect/Constructor;->newInstance([Ljava/lang/Object;)Ljava/lang/Object;
                {
                    newInsts.add(new ImmutableInstruction35c(
                            Opcode.INVOKE_VIRTUAL,
                            2,
                            8,
                            0,
                            0,
                            0,
                            0,
                            new ImmutableMethodReference(
                                    "Ljava/lang/reflect/Constructor;",
                                    "newInstance",
                                    Lists.newArrayList("[Ljava/lang/Object;"),
                                    "Ljava/lang/Object;")
                    ));
                }
                //move-result-object v13
                {
                    newInsts.add(new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT, 13));
                }
                //const-string v20, "newParser"
                {
                    newInsts.add(new ImmutableInstruction21c(Opcode.CONST_STRING, 20, new ImmutableStringReference("newParser")));
                }
                //const/16 v21, 0x0
                {
                    newInsts.add(new ImmutableInstruction21s(Opcode.CONST_16, 21, 0x0));
                }
                //move/from16 v0, v21
                {
                    newInsts.add(new ImmutableInstruction22x(Opcode.MOVE_FROM16, 0, 21));
                }
                //new-array v0, v0, [Ljava/lang/Class;
                {
                    newInsts.add(new ImmutableInstruction22c(Opcode.NEW_ARRAY, 0, 0, new ImmutableTypeReference("[Ljava/lang/Class;")));
                }
                //move-object/from16 v21, v0
                {
                    newInsts.add(new ImmutableInstruction22x(Opcode.MOVE_OBJECT_FROM16, 21, 0));
                }
                //invoke-virtual/range {v19 .. v21}, Ljava/lang/Class;->getDeclaredMethod(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;
                {
                    newInsts.add(new ImmutableInstruction3rc(
                            Opcode.INVOKE_VIRTUAL_RANGE,
                            19,
                            3,
                            new ImmutableMethodReference(
                                    "Ljava/lang/Class;",
                                    "getDeclaredMethod",
                                    Lists.newArrayList("Ljava/lang/String;", "[Ljava/lang/Class;"),
                                    "Ljava/lang/reflect/Method;")
                    ));
                }
                //move-result-object v12
                {
                    newInsts.add(new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT, 12));
                }
                //const/16 v20, 0x1
                {
                    newInsts.add(new ImmutableInstruction21s(Opcode.CONST_16, 20, 0x1));
                }
                //move/from16 v0, v20
                {
                    newInsts.add(new ImmutableInstruction22x(Opcode.MOVE_FROM16, 0, 20));
                }
                //invoke-virtual {v12, v0}, Ljava/lang/reflect/Method;->setAccessible(Z)V
                {
                    newInsts.add(new ImmutableInstruction35c(
                            Opcode.INVOKE_VIRTUAL,
                            2,
                            12,
                            0,
                            0,
                            0,
                            0,
                            new ImmutableMethodReference(
                                    "Ljava/lang/reflect/Method;",
                                    "setAccessible",
                                    Lists.newArrayList("Z"),
                                    "V")
                    ));
                }
                //const/16 v20, 0x0
                {
                    newInsts.add(new ImmutableInstruction21s(Opcode.CONST_16, 20, 0x0));
                }
                //move/from16 v0, v20
                {
                    newInsts.add(new ImmutableInstruction22x(Opcode.MOVE_FROM16, 0, 20));
                }
                //new-array v0, v0, [Ljava/lang/Object;
                {
                    newInsts.add(new ImmutableInstruction22c(Opcode.NEW_ARRAY, 0, 0, new ImmutableTypeReference("[Ljava/lang/Object;")));
                }
                //move-object/from16 v20, v0
                {
                    newInsts.add(new ImmutableInstruction22x(Opcode.MOVE_OBJECT_FROM16, 20, 0));
                }
                //move-object/from16 v0, v20
                {
                    newInsts.add(new ImmutableInstruction22x(Opcode.MOVE_OBJECT_FROM16, 0, 20));
                }
                //invoke-virtual {v12, v13, v0}, Ljava/lang/reflect/Method;->invoke(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;
                {
                    newInsts.add(new ImmutableInstruction35c(
                            Opcode.INVOKE_VIRTUAL,
                            3,
                            12,
                            13,
                            0,
                            0,
                            0,
                            new ImmutableMethodReference(
                                    "Ljava/lang/reflect/Method;",
                                    "invoke",
                                    Lists.newArrayList("Ljava/lang/Object;", "[Ljava/lang/Object;"),
                                    "Ljava/lang/Object;")
                    ));
                }
                //move-result-object v14
                {
                    newInsts.add(new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT, 14));
                }
                //check-cast v14, Landroid/content/res/XmlResourceParser;
                {
                    newInsts.add(new ImmutableInstruction21c(Opcode.CHECK_CAST, 14, new ImmutableTypeReference("Landroid/content/res/XmlResourceParser;")));
                }
                //return-object v18
                {
                    newInsts.add(new ImmutableInstruction11x(Opcode.RETURN_OBJECT, 14));
                }
                //move-exception v9
                {
                    newInsts.add(new ImmutableInstruction11x(Opcode.MOVE_EXCEPTION, 9));
                }
                //new-instance v20, Landroid/util/AndroidRuntimeException;
                {
                    newInsts.add(new ImmutableInstruction21c(Opcode.NEW_INSTANCE, 20, new ImmutableTypeReference("Landroid/util/AndroidRuntimeException;")));
                }
                //move-object/from16 v0, v20
                {
                    newInsts.add(new ImmutableInstruction22x(Opcode.MOVE_OBJECT_FROM16, 0, 20));
                }
                //invoke-direct {v0, v9}, Landroid/util/AndroidRuntimeException;-><init>(Ljava/lang/Exception;)V
                {
                    newInsts.add(new ImmutableInstruction35c(
                            Opcode.INVOKE_DIRECT,
                            2,
                            0,
                            9,
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
                //throw v20
                {
                    newInsts.add(new ImmutableInstruction11x(Opcode.THROW, 20));
                }
                {
                    int end_try = 0;
                    for (Instruction instruction : newInsts) {
                        if (instruction.getOpcode() == Opcode.MOVE_RESULT_OBJECT) {
                            ImmutableInstruction11x instruction11x = (ImmutableInstruction11x) instruction;
                            if (instruction11x.getRegisterA() == 18)
                                break;
                        }
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
                            "Landroid/content/res/XmlResourceParser;",
                            AccessFlags.PUBLIC.getValue(),
                            null,
                            null,
                            new ImmutableMethodImplementation(
                                    24,
                                    newInsts,
                                    Lists.newArrayList(new ImmutableTryBlock(
                                            0,
                                            end_try,
                                            exceptionHandlers)),
                                    null)));
                }
                //diff.add(entry.getValue());
            } catch (Exception ignored) {
            }
        }
        return methods;
    }

    private static List<String> stringSpilt(String inputString, int length) {
        int size = inputString.length() / length;
        if (inputString.length() % length != 0) {
            size += 1;
        }
        return getStrList(inputString, length, size);
    }

    private static List<String> getStrList(String inputString, int length, int size) {
        List<String> list = new ArrayList<>();
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
            return str.substring(f);
        } else {
            return str.substring(f, t);
        }
    }

}
