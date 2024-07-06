package armadillo.transformers.verify;

import armadillo.Constant;
import armadillo.mapper.UserSoftMapper;
import armadillo.model.UserSoft;
import armadillo.transformers.base.BaseTransformer;
import armadillo.transformers.base.OtherTransformer;
import armadillo.utils.*;
import armadillo.utils.axml.AutoXml.ManifestParse;
import armadillo.utils.axml.AutoXml.xml.decode.AXmlDecoder;
import armadillo.utils.axml.AutoXml.xml.decode.AXmlResourceParser;
import armadillo.utils.axml.AutoXml.xml.decode.XmlPullParser;
import armadillo.utils.axml.EditXml.decode.AXMLDoc;
import armadillo.utils.axml.EditXml.decode.BTagNode;
import armadillo.utils.axml.EditXml.decode.BXMLNode;
import armadillo.utils.axml.EditXml.editor.ContentProviderEditor;
import armadillo.utils.axml.EditXml.editor.PermissionEditor;
import armadillo.utils.axml.EditXml.utils.TypedValue;
import com.google.devrel.gmscore.tools.apk.arsc.*;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.ibatis.session.SqlSession;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.formats.Instruction21c;
import org.jf.dexlib2.iface.reference.StringReference;
import org.jf.dexlib2.immutable.ImmutableClassDef;
import org.jf.dexlib2.immutable.ImmutableMethod;
import org.jf.dexlib2.immutable.ImmutableMethodImplementation;
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction21c;
import org.jf.dexlib2.immutable.reference.ImmutableStringReference;
import org.jf.dexlib2.rewriter.DexRewriter;
import org.jf.dexlib2.rewriter.Rewriter;
import org.jf.dexlib2.rewriter.RewriterModule;
import org.jf.dexlib2.rewriter.Rewriters;
import org.jf.dexlib2.writer.io.MemoryDataStore;
import org.jf.dexlib2.writer.pool.DexPool;

import javax.annotation.Nonnull;
import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Admob extends OtherTransformer {
    @Override
    public void transform() throws Exception {
        JsonObject jsonObject = new JsonParser().parse(getConfiguration()).getAsJsonObject();
        byte[] axml = getReplacerRes().get("AndroidManifest.xml");
        if (axml == null)
            axml = StreamUtil.readBytes(getZipFile().getInputStream(new ZipEntry("AndroidManifest.xml")));
        byte[] arsc = getReplacerRes().get("resources.arsc");
        if (arsc == null)
            arsc = StreamUtil.readBytes(getZipFile().getInputStream(new ZipEntry("resources.arsc")));
        int arsc_xml_id = 0;
        /**
         * arsc插入属性
         */
        {
            BinaryResourceFile resourceFile = BinaryResourceFile.fromInputStream(new ByteArrayInputStream(arsc));
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
                    arsc_xml_id = Integer.parseInt(binaryResourceIdentifier.toString().substring(2), 16);
                }
            }
            getReplacerRes().put("resources.arsc", resourceFile.toByteArray());
        }
        Integer ver = ManifestParse.parseManifestVer(new ByteArrayInputStream(axml));
        String key;
        String packageName = ManifestParse.parseManifestPackageName(new ByteArrayInputStream(axml));
        try (SqlSession sqlSession = MyBatisUtil.getSqlSession(true)) {
            /**
             * 创建软件/读取KEY
             */
            {
                UserSoftMapper userSoftMapper = sqlSession.getMapper(UserSoftMapper.class);
                UserSoft userSoft = userSoftMapper.findUserIdAndPack(getSysUser().getId(), packageName);
                if (userSoft == null) {
                    UserSoft soft = new UserSoft();
                    soft.setUserId(getSysUser().getId());
                    soft.setPackageName(packageName);
                    soft.setVersion(ver);
                    soft.setName(jsonObject.get("name").getAsString());
                    key = SHAUtils.SHA1(UUID.randomUUID().toString());
                    soft.setAppkey(key);
                    soft.setHandle(0);
                    userSoftMapper.insert(soft);
                } else {
                    key = userSoft.getAppkey();
                    userSoft.setVersion(ver);
                    userSoftMapper.updateByPrimaryKey(userSoft);
                }
                /**
                 * 写图标
                 */
                File icon = new File(Constant.getIcon(), key + ".png");
                if (!icon.exists()) {
                    FileOutputStream fileOutputStream = new FileOutputStream(icon);
                    fileOutputStream.write(StreamUtil.readBytes(getZipFile().getInputStream(new ZipEntry("icon.png"))));
                    fileOutputStream.close();
                }
            }
        }
        String cls = rodomCls();
        /**
         * 修改AXML
         */
        {
            AXMLDoc axmlDoc = new AXMLDoc();
            axmlDoc.parse(new ByteArrayInputStream(axml));
            InsertBasicAxml(axmlDoc, arsc_xml_id, packageName, jsonObject.get("key").getAsString());
            PermissionEditor permissionEditor = new PermissionEditor(axmlDoc);
            permissionEditor.setEditorInfo(new PermissionEditor.EditorInfo()
                    .with(new PermissionEditor.PermissionInfo("android.permission.INTERNET"))
                    .with(new PermissionEditor.PermissionInfo("android.permission.REQUEST_INSTALL_PACKAGES"))
                    .with(new PermissionEditor.PermissionInfo("android.permission.QUERY_ALL_PACKAGES"))
                    .with(new PermissionEditor.PermissionInfo("android.permission.ACCESS_NETWORK_STATE"))
                    .with(new PermissionEditor.PermissionInfo("android.permission.CHANGE_WIFI_STATE"))
                    .with(new PermissionEditor.PermissionInfo("android.permission.ACCESS_WIFI_STATE"))
                    .with(new PermissionEditor.PermissionInfo("android.permission.WRITE_EXTERNAL_STORAGE"))
                    .with(new PermissionEditor.PermissionInfo("android.permission.READ_EXTERNAL_STORAGE"))
                    .with(new PermissionEditor.PermissionInfo("android.permission.WAKE_LOCK")));
            permissionEditor.commit();
            ContentProviderEditor contentProviderEditor = new ContentProviderEditor(axmlDoc);
            contentProviderEditor.setEditorInfo(new ContentProviderEditor.Editorinfo()
                    .with(new ContentProviderEditor.ProviderInfo(cls, SHAUtils.SHA1(UUID.randomUUID().toString()), false, 1999999)));
            contentProviderEditor.commit();
            ByteArrayOutputStream xmlOut = new ByteArrayOutputStream();
            axmlDoc.build(xmlOut);
            axmlDoc.release();
            getReplacerRes().put("AndroidManifest.xml", xmlOut.toByteArray());
        }
        EditConfig(key, ver.toString(), cls, "arm.CloudProvider");
        WriteRes();
    }

    @Override
    public String getResult() {
        return null;
    }

    @Override
    public int priority() {
        return 0;
    }

    @Override
    public int compareTo(BaseTransformer o) {
        return 0;
    }

    private void EditConfig(String key, String ver, String cls, String superCls) throws Exception {
        /**
         * 修改配置信息
         */
        {
            String config_class = SysConfigUtil.getConfigUtil("verify.class");
            DexBackedDexFile verify_dex = DexBackedDexFile.fromInputStream(
                    Opcodes.getDefault(),
                    new BufferedInputStream(
                            new FileInputStream(
                                    new File(Constant.getRes(), "admob.dex"))));
            DexFile dexFile = new DexRewriter(new RewriterModule() {
                @Nonnull
                @Override
                public Rewriter<ClassDef> getClassDefRewriter(@Nonnull Rewriters rewriters) {
                    return new Rewriter<ClassDef>() {
                        @Nonnull
                        @Override
                        public ClassDef rewrite(@Nonnull ClassDef value) {
                            if (value.getType().equals(config_class)) {
                                List<Method> methods = new ArrayList<>();
                                for (Method method : value.getMethods()) {
                                    if (method.getName().equals("<clinit>")) {
                                        List<Instruction> NewInst = new ArrayList<>();
                                        for (Instruction instruction : Objects.requireNonNull(method.getImplementation()).getInstructions()) {
                                            if (instruction.getOpcode() == Opcode.CONST_STRING) {
                                                Instruction21c instruction21c = (Instruction21c) instruction;
                                                StringReference stringReference = (StringReference) instruction21c.getReference();
                                                if (stringReference.getString().equals("key"))
                                                    NewInst.add(new ImmutableInstruction21c(Opcode.CONST_STRING,
                                                            instruction21c.getRegisterA(),
                                                            new ImmutableStringReference(key)));
                                                else if (stringReference.getString().equals("ver"))
                                                    NewInst.add(new ImmutableInstruction21c(Opcode.CONST_STRING,
                                                            instruction21c.getRegisterA(),
                                                            new ImmutableStringReference(ver)));
                                                else
                                                    NewInst.add(instruction);
                                            } else
                                                NewInst.add(instruction);
                                        }
                                        methods.add(new ImmutableMethod(method.getDefiningClass(),
                                                method.getName(),
                                                method.getParameters(),
                                                method.getReturnType(),
                                                method.getAccessFlags(),
                                                method.getAnnotations(),
                                                method.getHiddenApiRestrictions(),
                                                new ImmutableMethodImplementation(
                                                        method.getImplementation().getRegisterCount(),
                                                        NewInst,
                                                        method.getImplementation().getTryBlocks(),
                                                        method.getImplementation().getDebugItems())));
                                    } else
                                        methods.add(method);
                                }
                                return new ImmutableClassDef(value.getType(),
                                        value.getAccessFlags(),
                                        value.getSuperclass(),
                                        value.getInterfaces(),
                                        value.getSourceFile(),
                                        value.getAnnotations(),
                                        value.getFields(),
                                        methods);
                            } else
                                return value;
                        }
                    };
                }
            }).getDexFileRewriter().rewrite(verify_dex);
            DexPool dexPool = new DexPool(Opcodes.getDefault());
            for (ClassDef classDef : dexFile.getClasses())
                dexPool.internClass(classDef);
            dexPool.internClass(PopDialogUtli.CreateRodom(
                    "L" + superCls.replace(".", "/") + ";",
                    "L" + cls.replace(".", "/") + ";"));
            MemoryDataStore dataStore = new MemoryDataStore();
            dexPool.writeTo(dataStore);
            getAdd_Classdex().add(Arrays.copyOf(dataStore.getBuffer(), dataStore.getSize()));
            dataStore.close();
        }
    }

    private void WriteRes() throws Exception {
        try (ZipFile zipFile = new ZipFile(new File(Constant.getRes(), "admob.zip"))) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                getReplacerRes().put(zipEntry.getName(), StreamUtil.readBytes(zipFile.getInputStream(zipEntry)));
            }
        }
    }

    private void InsertBasicAxml(AXMLDoc axmlDoc, int arsc_xml_id, String packageName, String key) {
        /**
         * 支持HTTP
         */
        {
            for (BXMLNode child : axmlDoc.getManifestNode().getChildren()) {
                BTagNode node = (BTagNode) child;
                if ("application".equals(node.getRawName())) {
                    BTagNode.Attribute[] attributes = node.getAttribute();
                    boolean flag = false;
                    for (BTagNode.Attribute attribute : attributes) {
                        if (attribute.Name.equals("usesCleartextTraffic")) {
                            flag = !flag;
                            attribute.setValue(TypedValue.TYPE_INT_BOOLEAN, 1);
                        }
                    }
                    if (flag)
                        node.setAttribute(attributes);
                    else {
                        List<BTagNode.Attribute> new_attr = new ArrayList<>(Arrays.asList(attributes));
                        BTagNode.Attribute debuggable = new BTagNode.Attribute(axmlDoc.getmStringBlock(), "http://schemas.android.com/apk/res/android", "usesCleartextTraffic", TypedValue.TYPE_INT_BOOLEAN);
                        debuggable.setValue(TypedValue.TYPE_INT_BOOLEAN, 1);
                        new_attr.add(debuggable);
                        node.setAttribute(new_attr.toArray(new BTagNode.Attribute[0]));
                    }
                }
            }
        }
        /**
         * 插入7.0+应用安装内容提供者
         */
        {
            BTagNode tagNode = new BTagNode(axmlDoc.getmStringBlock(), null, "provider");
            List<BTagNode.Attribute> attributes = new ArrayList<>();
            BTagNode.Attribute attribute_name = new BTagNode.Attribute(axmlDoc.getmStringBlock(), "http://schemas.android.com/apk/res/android", "name", TypedValue.TYPE_STRING);
            BTagNode.Attribute attribute_authorities = new BTagNode.Attribute(axmlDoc.getmStringBlock(), "http://schemas.android.com/apk/res/android", "authorities", TypedValue.TYPE_STRING);
            BTagNode.Attribute attribute_exported = new BTagNode.Attribute(axmlDoc.getmStringBlock(), "http://schemas.android.com/apk/res/android", "exported", TypedValue.TYPE_INT_BOOLEAN);
            BTagNode.Attribute attribute_initOrder = new BTagNode.Attribute(axmlDoc.getmStringBlock(), "http://schemas.android.com/apk/res/android", "grantUriPermissions", TypedValue.TYPE_INT_BOOLEAN);

            attribute_name.setString("arm.FileProvider");
            attributes.add(attribute_name);

            attribute_authorities.setString("arm." + packageName + ".FileProvider");
            attributes.add(attribute_authorities);

            attribute_exported.setValue(TypedValue.TYPE_INT_BOOLEAN, 0);
            attributes.add(attribute_exported);

            attribute_initOrder.setValue(TypedValue.TYPE_INT_BOOLEAN, 1);
            attributes.add(attribute_initOrder);
            tagNode.setAttribute(attributes.toArray(new BTagNode.Attribute[attributes.size()]));

            attributes.clear();
            BTagNode meta_data = new BTagNode(axmlDoc.getmStringBlock(), null, "meta-data");
            BTagNode.Attribute meta_data_name = new BTagNode.Attribute(axmlDoc.getmStringBlock(), "http://schemas.android.com/apk/res/android", "name", TypedValue.TYPE_STRING);
            BTagNode.Attribute meta_data_resource = new BTagNode.Attribute(axmlDoc.getmStringBlock(), "http://schemas.android.com/apk/res/android", "resource", TypedValue.TYPE_REFERENCE);
            meta_data_name.setString("android.support.FILE_PROVIDER_PATHS");
            attributes.add(meta_data_name);
            meta_data_resource.setValue(TypedValue.TYPE_REFERENCE, arsc_xml_id);
            attributes.add(meta_data_resource);
            meta_data.setAttribute(attributes.toArray(new BTagNode.Attribute[attributes.size()]));
            tagNode.addChild(meta_data);
            axmlDoc.getApplicationNode().addChild(tagNode);
        }
        /**
         * 占坑
         */
        {
            BTagNode activity = new BTagNode(axmlDoc.getmStringBlock(), null, "activity");
            BTagNode.Attribute activity_name = new BTagNode.Attribute(axmlDoc.getmStringBlock(), "http://schemas.android.com/apk/res/android", "name", TypedValue.TYPE_STRING);
            activity_name.setString("arm.proxy.ProxyActivity");
            activity.addAttribute(activity_name);

            BTagNode service = new BTagNode(axmlDoc.getmStringBlock(), null, "service");
            BTagNode.Attribute service_name = new BTagNode.Attribute(axmlDoc.getmStringBlock(), "http://schemas.android.com/apk/res/android", "name", TypedValue.TYPE_STRING);
            service_name.setString("arm.proxy.ProxyService");
            service.addAttribute(service_name);

            axmlDoc.getApplicationNode().addChild(activity, service);
        }
        /**
         * Admob
         */
        {
            BTagNode activity = new BTagNode(axmlDoc.getmStringBlock(), null, "activity");
            {
                BTagNode.Attribute activity_name = new BTagNode.Attribute(axmlDoc.getmStringBlock(), "http://schemas.android.com/apk/res/android", "name", TypedValue.TYPE_STRING);
                activity_name.setString("com.google.android.gms.ads.AdActivity");
                activity.addAttribute(activity_name);

                BTagNode.Attribute activity_theme = new BTagNode.Attribute(axmlDoc.getmStringBlock(), "http://schemas.android.com/apk/res/android", "theme", TypedValue.TYPE_REFERENCE);
                activity_theme.setValue(TypedValue.TYPE_REFERENCE, 0x0103000f);
                activity.addAttribute(activity_theme);

                BTagNode.Attribute activity_exported = new BTagNode.Attribute(axmlDoc.getmStringBlock(), "http://schemas.android.com/apk/res/android", "exported", TypedValue.TYPE_INT_BOOLEAN);
                activity_exported.setValue(TypedValue.TYPE_INT_BOOLEAN, 0);
                activity.addAttribute(activity_exported);

                BTagNode.Attribute activity_configChanges = new BTagNode.Attribute(axmlDoc.getmStringBlock(), "http://schemas.android.com/apk/res/android", "configChanges", TypedValue.TYPE_INT_DEC);
                activity_configChanges.setValue(TypedValue.TYPE_INT_DEC, 0x0010 | 0x0020 | 0x0080 | 0x0100 | 0x0400 | 0x0800 | 0x0200);
                activity.addAttribute(activity_configChanges);
            }
            BTagNode service = new BTagNode(axmlDoc.getmStringBlock(), null, "service");
            {
                BTagNode.Attribute service_name = new BTagNode.Attribute(axmlDoc.getmStringBlock(), "http://schemas.android.com/apk/res/android", "name", TypedValue.TYPE_STRING);
                service_name.setString("com.google.android.gms.ads.AdService");
                service.addAttribute(service_name);

                BTagNode.Attribute service_enabled = new BTagNode.Attribute(axmlDoc.getmStringBlock(), "http://schemas.android.com/apk/res/android", "enabled", TypedValue.TYPE_INT_BOOLEAN);
                service_enabled.setValue(TypedValue.TYPE_INT_BOOLEAN, 1);
                service.addAttribute(service_enabled);

                BTagNode.Attribute service_exported = new BTagNode.Attribute(axmlDoc.getmStringBlock(), "http://schemas.android.com/apk/res/android", "exported", TypedValue.TYPE_INT_BOOLEAN);
                service_exported.setValue(TypedValue.TYPE_INT_BOOLEAN, 0);
                service.addAttribute(service_exported);
            }
            BTagNode meta_data = new BTagNode(axmlDoc.getmStringBlock(), null, "meta-data");
            {
                BTagNode.Attribute meta_data_name = new BTagNode.Attribute(axmlDoc.getmStringBlock(), "http://schemas.android.com/apk/res/android", "name", TypedValue.TYPE_STRING);
                meta_data_name.setString("com.google.android.gms.version");
                meta_data.addAttribute(meta_data_name);

                BTagNode.Attribute meta_data_value = new BTagNode.Attribute(axmlDoc.getmStringBlock(), "http://schemas.android.com/apk/res/android", "value", TypedValue.TYPE_REFERENCE);
                meta_data_value.setValue(TypedValue.TYPE_INT_DEC, 12451000);
                meta_data.addAttribute(meta_data_value);
            }
            BTagNode key_data = new BTagNode(axmlDoc.getmStringBlock(), null, "meta-data");
            {
                BTagNode.Attribute meta_data_name = new BTagNode.Attribute(axmlDoc.getmStringBlock(), "http://schemas.android.com/apk/res/android", "name", TypedValue.TYPE_STRING);
                meta_data_name.setString("com.google.android.gms.ads.APPLICATION_ID");
                key_data.addAttribute(meta_data_name);

                BTagNode.Attribute meta_data_value = new BTagNode.Attribute(axmlDoc.getmStringBlock(), "http://schemas.android.com/apk/res/android", "value", TypedValue.TYPE_STRING);
                meta_data_value.setString(key);
                key_data.addAttribute(meta_data_value);
            }
            ContentProviderEditor contentProviderEditor = new ContentProviderEditor(axmlDoc);
            contentProviderEditor.setEditorInfo(new ContentProviderEditor.Editorinfo()
                    .with(new ContentProviderEditor.ProviderInfo("com.google.android.gms.ads.MobileAdsInitProvider", packageName + ".mobileadsinitprovider", false, 100)));
            contentProviderEditor.commit();
            axmlDoc.getApplicationNode().addChild(activity, service, meta_data, key_data);
        }
    }

    private String rodomCls() {
        return "com." + StringRandom.RandomString();
    }
}
