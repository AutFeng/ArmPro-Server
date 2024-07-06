package arsc;

import com.google.devrel.gmscore.tools.apk.arsc.*;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class admob {
    @Test
    public void test() throws Exception {
        BinaryResourceFile resourceFile = BinaryResourceFile.fromInputStream(new FileInputStream("C:\\Users\\Administrator\\Desktop\\a.arsc"));
        for (Chunk chunk : resourceFile.getChunks()) {
            ResourceTableChunk tableChunk = (ResourceTableChunk) chunk;
            StringPoolChunk stringPool = tableChunk.getStringPool();
            int string_index = stringPool.addString("res/xml/arm_paths.xml");
            /**
             * 插入arm_path
             */
            for (PackageChunk packageChunk : tableChunk.getPackages()) {
                int typeChunkSize = 0;
                for (TypeChunk typeChunk : packageChunk.getTypeChunks()) {
                    typeChunkSize = typeChunk.getHeaderSize();
                    break;
                }
                int TypeSpecChunkSize = 0;
                for (TypeSpecChunk typeSpecChunk : packageChunk.getTypeSpecChunks()) {
                    TypeSpecChunkSize = typeSpecChunk.getHeaderSize();
                    break;
                }
                StringPoolChunk keyStringPool = packageChunk.getKeyStringPool();
                StringPoolChunk typeStringPool = packageChunk.getTypeStringPool();
                int type_index = typeStringPool.addString("arm");
                int key_index = keyStringPool.addString("arm_path");
                BinaryResourceConfiguration default_config = null;
                for (TypeChunk typeChunk : packageChunk.getTypeChunks()) {
                    if (typeChunk.getConfiguration().toString().equals("default")) {
                        default_config = typeChunk.getConfiguration();
                        break;
                    }
                }
                packageChunk.addTypeSpecs(new TypeSpecChunk(packageChunk, TypeSpecChunkSize, 0, 0, type_index + 1, new int[1]));
                TypeChunk arm = new TypeChunk(packageChunk, typeChunkSize, 0, 0, type_index + 1, 0, 0);
                arm.setConfiguration(default_config);
                arm.addEntrie(new TypeChunk.Entry(key_index, new BinaryResourceValue(8, BinaryResourceValue.Type.STRING, string_index), arm));
                BinaryResourceIdentifier binaryResourceIdentifier = BinaryResourceIdentifier.create(packageChunk.getId(), arm.getId(), 0);
                packageChunk.addType(arm);
            }
            /**
             * 插入Admob
             */
            for (PackageChunk packageChunk : tableChunk.getPackages()) {
                StringPoolChunk keyStringPool = packageChunk.getKeyStringPool();
                boolean IntegerFlag = false;
                for (TypeChunk typeChunk : packageChunk.getTypeChunks()) {
                    if ("string".equals(typeChunk.getTypeName())) {
                        typeChunk.addEntrie(
                                new TypeChunk.Entry(
                                        keyStringPool.addString("common_google_play_services_unknown_issue"),
                                        new BinaryResourceValue(BinaryResourceValue.SIZE, BinaryResourceValue.Type.STRING, stringPool.addString("%1$s is having trouble with Google Play services. Please try again.")),
                                        typeChunk));
                        typeChunk.addEntrie(
                                new TypeChunk.Entry(
                                        keyStringPool.addString("s1"),
                                        new BinaryResourceValue(BinaryResourceValue.SIZE, BinaryResourceValue.Type.STRING, stringPool.addString("Save image")),
                                        typeChunk));
                        typeChunk.addEntrie(
                                new TypeChunk.Entry(
                                        keyStringPool.addString("s2"),
                                        new BinaryResourceValue(BinaryResourceValue.SIZE, BinaryResourceValue.Type.STRING, stringPool.addString("Allow Ad to store image in Picture gallery?")),
                                        typeChunk));
                        typeChunk.addEntrie(
                                new TypeChunk.Entry(
                                        keyStringPool.addString("s3"),
                                        new BinaryResourceValue(BinaryResourceValue.SIZE, BinaryResourceValue.Type.STRING, stringPool.addString("Accept")),
                                        typeChunk));
                        typeChunk.addEntrie(
                                new TypeChunk.Entry(
                                        keyStringPool.addString("s4"),
                                        new BinaryResourceValue(BinaryResourceValue.SIZE, BinaryResourceValue.Type.STRING, stringPool.addString("Decline")),
                                        typeChunk));
                        typeChunk.addEntrie(
                                new TypeChunk.Entry(
                                        keyStringPool.addString("s5"),
                                        new BinaryResourceValue(BinaryResourceValue.SIZE, BinaryResourceValue.Type.STRING, stringPool.addString("Create calendar event")),
                                        typeChunk));
                        typeChunk.addEntrie(
                                new TypeChunk.Entry(
                                        keyStringPool.addString("s6"),
                                        new BinaryResourceValue(BinaryResourceValue.SIZE, BinaryResourceValue.Type.STRING, stringPool.addString("Allow Ad to create a calendar event?")),
                                        typeChunk));
                        typeChunk.addEntrie(
                                new TypeChunk.Entry(
                                        keyStringPool.addString("s7"),
                                        new BinaryResourceValue(BinaryResourceValue.SIZE, BinaryResourceValue.Type.STRING, stringPool.addString("Test Ad")),
                                        typeChunk));
                    }
                    if ("integer".equals(typeChunk.getTypeName())) {
                        typeChunk.addEntrie(
                                new TypeChunk.Entry(
                                        keyStringPool.addString("google_play_services_version"),
                                        new BinaryResourceValue(BinaryResourceValue.SIZE, BinaryResourceValue.Type.INT_DEC, 12451000),
                                        typeChunk));
                        IntegerFlag = true;
                    }
                }
                if (!IntegerFlag) {
                    int typeChunkSize = 0;
                    for (TypeChunk typeChunk : packageChunk.getTypeChunks()) {
                        typeChunkSize = typeChunk.getHeaderSize();
                        break;
                    }
                    int TypeSpecChunkSize = 0;
                    for (TypeSpecChunk typeSpecChunk : packageChunk.getTypeSpecChunks()) {
                        TypeSpecChunkSize = typeSpecChunk.getHeaderSize();
                        break;
                    }
                    StringPoolChunk typeStringPool = packageChunk.getTypeStringPool();
                    int type_index = typeStringPool.addString("integer");
                    BinaryResourceConfiguration default_config = null;
                    for (TypeChunk typeChunk : packageChunk.getTypeChunks()) {
                        if (typeChunk.getConfiguration().toString().equals("default")) {
                            default_config = typeChunk.getConfiguration();
                            break;
                        }
                    }
                    packageChunk.addTypeSpecs(new TypeSpecChunk(packageChunk, TypeSpecChunkSize, 0, 0, type_index + 1, new int[1]));
                    TypeChunk integer = new TypeChunk(packageChunk, typeChunkSize, 0, 0, type_index + 1, 0, 0);
                    integer.setConfiguration(default_config);
                    integer.addEntrie(new TypeChunk.Entry(
                            keyStringPool.addString("google_play_services_version"),
                            new BinaryResourceValue(BinaryResourceValue.SIZE, BinaryResourceValue.Type.INT_DEC, 12451000),
                            integer));
                    packageChunk.addType(integer);
                }
                for (TypeSpecChunk typeSpecChunk : packageChunk.getTypeSpecChunks()) {
                    if ("string".equals(typeSpecChunk.getTypeName())) {
                        for (int i = 0; i < 8; i++) {
                            typeSpecChunk.addResources(-1);
                        }
                    }
                    if (IntegerFlag && "integer".equals(typeSpecChunk.getTypeName())){
                        typeSpecChunk.addResources(-1);
                    }
                }
            }
        }
        ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream("C:\\Users\\Administrator\\Desktop\\b.apk"));
        zipOutputStream.putNextEntry(new ZipEntry("resources.arsc"));
        zipOutputStream.write(resourceFile.toByteArray(true));
        zipOutputStream.closeEntry();
        zipOutputStream.close();
    }
}
