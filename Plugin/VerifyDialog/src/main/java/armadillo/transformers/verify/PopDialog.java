package armadillo.transformers.verify;

import armadillo.Constant;
import armadillo.common.SimpleNameFactory;
import armadillo.transformers.base.BaseTransformer;
import armadillo.transformers.base.OtherTransformer;
import armadillo.mapper.UserSoftMapper;
import armadillo.model.UserSoft;
import armadillo.utils.*;
import armadillo.utils.axml.AutoXml.ManifestAppName;
import armadillo.utils.axml.AutoXml.ManifestParse;
import armadillo.utils.axml.AutoXml.xml.decode.AXmlDecoder;
import armadillo.utils.axml.AutoXml.xml.decode.AXmlResourceParser;
import armadillo.utils.axml.AutoXml.xml.decode.XmlPullParser;
import armadillo.utils.axml.EditXml.decode.AXMLDoc;
import armadillo.utils.axml.EditXml.decode.BTagNode;
import armadillo.utils.axml.EditXml.decode.BXMLNode;
import armadillo.utils.axml.EditXml.editor.ContentProviderEditor;
import armadillo.utils.axml.EditXml.editor.MetaDataEditor;
import armadillo.utils.axml.EditXml.editor.PermissionEditor;
import armadillo.utils.axml.EditXml.editor.SpActivityEditor;
import armadillo.utils.axml.EditXml.utils.TypedValue;
import com.google.common.collect.Lists;
import com.google.devrel.gmscore.tools.apk.arsc.*;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.ibatis.session.SqlSession;
import org.jf.dexlib2.AccessFlags;
import org.jf.dexlib2.AnnotationVisibility;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.builder.MutableMethodImplementation;
import org.jf.dexlib2.builder.instruction.BuilderInstruction35c;
import org.jf.dexlib2.dexbacked.DexBackedClassDef;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.formats.Instruction21c;
import org.jf.dexlib2.iface.instruction.formats.Instruction35c;
import org.jf.dexlib2.iface.reference.MethodReference;
import org.jf.dexlib2.iface.reference.StringReference;
import org.jf.dexlib2.immutable.*;
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction10x;
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction21c;
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction35c;
import org.jf.dexlib2.immutable.reference.ImmutableMethodReference;
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
import java.util.zip.ZipOutputStream;

public class PopDialog extends OtherTransformer {
    private final HashMap<String, String> resMap = new HashMap<>();
    private DexFile radomDexFile;
    private HashMap<String, String> classMap;

    @Override
    public void transform() throws Exception {
        JsonObject jsonObject = new JsonParser().parse(getConfiguration()).getAsJsonObject();
        JsonElement mode = jsonObject.get(Long.toString(134217728));
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
                String random = String.format("%s%s", "res/xml/", nameFactory.randomName());
                resMap.put("res/xml/arm_paths.xml", random);
                int string_index = stringPool.addString(random);
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
                    int type_index = typeStringPool.addString(nameFactory.randomName());
                    int key_index = keyStringPool.addString(nameFactory.randomName());
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
            File icon = new File(Constant.getIcon(), key + ".png");
            if (!icon.exists()) {
                FileOutputStream fileOutputStream = new FileOutputStream(icon);
                fileOutputStream.write(StreamUtil.readBytes(getZipFile().getInputStream(new ZipEntry("icon.png"))));
                fileOutputStream.close();
            }
        }
        /**
         * 随机混淆
         */
        DexBackedDexFile verify_dex = DexBackedDexFile.fromInputStream(
                Opcodes.getDefault(),
                new BufferedInputStream(
                        new FileInputStream(
                                new File(Constant.getRes(), "verify.dex"))));
        ClassRename classRename = new ClassRename();
        for (ClassDef classDef : verify_dex.getClasses()) {
            if (!classRename.getClassMap().containsKey(classDef.getType())) {
                if (jsonObject.get("new_cls") != null)
                    classRename.getClassMap().put(classDef.getType(), String.format("L%s/%s;", jsonObject.get("new_cls").getAsString().replace(".", "/"), nameFactory.nextName()));
                else
                    classRename.getClassMap().put(classDef.getType(), String.format("L%s/%s;", new Random().nextBoolean() ? "androidx" : "android", nameFactory.nextName()));
            }
        }
        radomDexFile = new DexRewriter(classRename).getDexFileRewriter().rewrite(verify_dex);
        classMap = classRename.getClassMap();
        final String root_cls = jsonObject.get("new_cls") != null ? jsonObject.get("new_cls").getAsString().replace("/", ".") : "androidx";
        final String cls = String.format("%s.%s", root_cls, nameFactory.nextName());
        final String file_provider = String.format("%s.%s", root_cls, nameFactory.nextName());
        final boolean offline = jsonObject.get("offline").getAsBoolean();
        switch (mode.getAsInt()) {
            /**
             * 全局内容提供者
             */
            case 0: {
                AXMLDoc axmlDoc = new AXMLDoc();
                axmlDoc.parse(new ByteArrayInputStream(axml));
                InsertBasicAxml(axmlDoc, arsc_xml_id, packageName);
                PermissionEditor permissionEditor = new PermissionEditor(axmlDoc);
                permissionEditor.setEditorInfo(new PermissionEditor.EditorInfo()
                        .with(new PermissionEditor.PermissionInfo("android.permission.INTERNET"))
                        .with(new PermissionEditor.PermissionInfo("android.permission.REQUEST_INSTALL_PACKAGES"))
                        .with(new PermissionEditor.PermissionInfo("android.permission.QUERY_ALL_PACKAGES")));
                permissionEditor.commit();
                ContentProviderEditor contentProviderEditor = new ContentProviderEditor(axmlDoc);
                contentProviderEditor.setEditorInfo(new ContentProviderEditor.Editorinfo()
                        .with(new ContentProviderEditor.ProviderInfo(cls, SHAUtils.SHA1(UUID.randomUUID().toString()), false, new Random().nextInt(9999999))));
                contentProviderEditor.commit();
                ByteArrayOutputStream xmlOut = new ByteArrayOutputStream();
                axmlDoc.build(xmlOut);
                axmlDoc.release();
                getReplacerRes().put("AndroidManifest.xml", xmlOut.toByteArray());
                EditConfig(key, ver.toString(), offline, file_provider, cls, "arm.CloudProvider");
            }
            break;
            /**
             * 单例
             */
            case 1: {
                AXMLDoc axmlDoc = new AXMLDoc();
                axmlDoc.parse(new ByteArrayInputStream(axml));
                InsertBasicAxml(axmlDoc, arsc_xml_id, packageName);
                PermissionEditor permissionEditor = new PermissionEditor(axmlDoc);
                permissionEditor.setEditorInfo(new PermissionEditor.EditorInfo()
                        .with(new PermissionEditor.PermissionInfo("android.permission.INTERNET"))
                        .with(new PermissionEditor.PermissionInfo("android.permission.REQUEST_INSTALL_PACKAGES"))
                        .with(new PermissionEditor.PermissionInfo("android.permission.QUERY_ALL_PACKAGES")));
                permissionEditor.commit();
                ByteArrayOutputStream xmlOut = new ByteArrayOutputStream();
                axmlDoc.build(xmlOut);
                axmlDoc.release();
                getReplacerRes().put("AndroidManifest.xml", xmlOut.toByteArray());
                String ActivityClass = jsonObject.get("ActivityClass").getAsString();
                Enumeration<? extends ZipEntry> entries = getZipFile().entries();
                while (entries.hasMoreElements()) {
                    ZipEntry zipEntry = entries.nextElement();
                    if (zipEntry.getName().startsWith("classes") && zipEntry.getName().endsWith(".dex")) {
                        DexBackedDexFile dexBackedDexFile = DexBackedDexFile.fromInputStream(
                                Opcodes.getDefault(),
                                new BufferedInputStream(getZipFile().getInputStream(zipEntry)));
                        ClassDef def = null;
                        for (DexBackedClassDef classDef : dexBackedDexFile.getClasses()) {
                            if (classDef.getType().equals("L" + ActivityClass.replace(".", "/") + ";")) {
                                def = classDef;
                                break;
                            }
                        }
                        if (def != null) {
                            boolean flag = false;
                            List<Method> newMethod = new ArrayList<>();
                            for (Method method : def.getMethods()) {
                                if (method.getName().equals("onCreate")) {
                                    MutableMethodImplementation mutableMethodImplementation = new MutableMethodImplementation(Objects.requireNonNull(method.getImplementation()));
                                    for (int i = 0; i < mutableMethodImplementation.getInstructions().size(); i++) {
                                        Instruction instruction = mutableMethodImplementation.getInstructions().get(i);
                                        if (instruction.getOpcode() == Opcode.INVOKE_SUPER) {
                                            mutableMethodImplementation.addInstruction(i++,
                                                    new BuilderInstruction35c(
                                                            Opcode.INVOKE_STATIC,
                                                            1,
                                                            mutableMethodImplementation.getRegisterCount() - method.getParameters().size() - 1,
                                                            0,
                                                            0,
                                                            0,
                                                            0,
                                                            new ImmutableMethodReference(classMap.get("Larm/Global;"),
                                                                    "Call",
                                                                    Lists.newArrayList("Landroid/app/Activity;"),
                                                                    "V")));
                                        }
                                    }
                                    newMethod.add(new ImmutableMethod(method.getDefiningClass(),
                                            method.getName(),
                                            method.getParameters(),
                                            method.getReturnType(),
                                            method.getAccessFlags(),
                                            method.getAnnotations(),
                                            method.getHiddenApiRestrictions(),
                                            mutableMethodImplementation));
                                    flag = true;
                                } else
                                    newMethod.add(method);
                            }
                            if (!flag) {
                                List<Instruction> newInst = new ArrayList<>();
                                newInst.add(new ImmutableInstruction35c(
                                        Opcode.INVOKE_SUPER,
                                        2,
                                        0,
                                        1,
                                        0,
                                        0,
                                        0,
                                        new ImmutableMethodReference(def.getSuperclass(),
                                                "onCreate",
                                                Lists.newArrayList("Landroid/os/Bundle;"),
                                                "V")));
                                newInst.add(new ImmutableInstruction35c(
                                        Opcode.INVOKE_STATIC,
                                        1,
                                        0,
                                        0,
                                        0,
                                        0,
                                        0,
                                        new ImmutableMethodReference(classMap.get("Larm/Global;"),
                                                "Call",
                                                Lists.newArrayList("Landroid/app/Activity;"),
                                                "V")));
                                newInst.add(new ImmutableInstruction10x(Opcode.RETURN_VOID));
                                Set<ImmutableAnnotation> annotations = new HashSet<>();
                                annotations.add(new ImmutableAnnotation(AnnotationVisibility.RUNTIME, "Ljava/lang/Override;", null));
                                newMethod.add(new ImmutableMethod(def.getType(),
                                        "onStart",
                                        Lists.newArrayList(new ImmutableMethodParameter("Landroid/os/Bundle;", null, null)),
                                        "V",
                                        AccessFlags.PROTECTED.getValue(),
                                        annotations,
                                        null,
                                        new ImmutableMethodImplementation(2,
                                                newInst,
                                                null,
                                                null)));
                            }
                            DexPool dexPool = new DexPool(Opcodes.getDefault());
                            /**
                             * 合并ClassDef
                             */
                            {
                                dexPool.internClass(new ImmutableClassDef(def.getType(),
                                        def.getAccessFlags(),
                                        def.getSuperclass(),
                                        def.getInterfaces(),
                                        def.getSourceFile(),
                                        def.getAnnotations(),
                                        def.getFields(),
                                        newMethod));
                                for (ClassDef classDef : dexBackedDexFile.getClasses()) {
                                    if (!classDef.getType().equals(def.getType()))
                                        dexPool.internClass(classDef);
                                }
                            }
                            MemoryDataStore dataStore = new MemoryDataStore();
                            dexPool.writeTo(dataStore);
                            getReplacerRes().put(zipEntry.getName(), Arrays.copyOf(dataStore.getBuffer(), dataStore.getSize()));
                            dataStore.close();
                        }
                    }
                }
                EditConfig(key, ver.toString(), offline, file_provider, null, null);
            }
            break;
            /**
             * 页面
             */
            case 2: {
                String MainActivity = ManifestParse.parseMainActivity(new ByteArrayInputStream(axml));
                AXMLDoc axmlDoc = new AXMLDoc();
                axmlDoc.parse(new ByteArrayInputStream(axml));
                InsertBasicAxml(axmlDoc, arsc_xml_id, packageName);
                PermissionEditor permissionEditor = new PermissionEditor(axmlDoc);
                permissionEditor.setEditorInfo(new PermissionEditor.EditorInfo()
                        .with(new PermissionEditor.PermissionInfo("android.permission.INTERNET"))
                        .with(new PermissionEditor.PermissionInfo("android.permission.REQUEST_INSTALL_PACKAGES"))
                        .with(new PermissionEditor.PermissionInfo("android.permission.QUERY_ALL_PACKAGES")));
                permissionEditor.commit();
                SpActivityEditor spActivityEditor = new SpActivityEditor(axmlDoc);
                spActivityEditor.SetMainClass(MainActivity);
                spActivityEditor.setEditorInfo(new SpActivityEditor.EditorInfo(cls));
                spActivityEditor.commit();
                MetaDataEditor metaDataEditor = new MetaDataEditor(axmlDoc);
                metaDataEditor.setEditorInfo(new MetaDataEditor.Editorinfo()
                        .with(new MetaDataEditor.DataInfo("MainClass", MainActivity)));
                metaDataEditor.commit();
                ByteArrayOutputStream xmlOut = new ByteArrayOutputStream();
                axmlDoc.build(xmlOut);
                axmlDoc.release();
                getReplacerRes().put("AndroidManifest.xml", xmlOut.toByteArray());
                EditConfig(key, ver.toString(), offline, file_provider, cls, "arm.SplashActivity");
            }
            break;
            /**
             * 全局Application继承
             */
            case 3: {
                /**
                 * 修改AXML
                 */
                {
                    AXMLDoc axmlDoc = new AXMLDoc();
                    axmlDoc.parse(new ByteArrayInputStream(axml));
                    InsertBasicAxml(axmlDoc, arsc_xml_id, packageName);
                    PermissionEditor permissionEditor = new PermissionEditor(axmlDoc);
                    permissionEditor.setEditorInfo(new PermissionEditor.EditorInfo()
                            .with(new PermissionEditor.PermissionInfo("android.permission.INTERNET"))
                            .with(new PermissionEditor.PermissionInfo("android.permission.REQUEST_INSTALL_PACKAGES"))
                            .with(new PermissionEditor.PermissionInfo("android.permission.QUERY_ALL_PACKAGES")));
                    permissionEditor.commit();
                    ByteArrayOutputStream xmlOut = new ByteArrayOutputStream();
                    axmlDoc.build(xmlOut);
                    axmlDoc.release();
                    ManifestAppName.XmlMode xmlMode = new ManifestAppName().parseManifest(new ByteArrayInputStream(xmlOut.toByteArray()), cls);
                    getReplacerRes().put("AndroidManifest.xml", xmlMode.getData());
                    EditApplicationConfig(key, ver.toString(), xmlMode.isCustomApplication() ? xmlMode.getCustomApplicationName() : null, cls, "arm.CloudApp");
                }
            }
            break;
            /**
             * 腾讯定向分享
             */
            case 4: {
                /**
                 * 修改AXML
                 */
                {
                    AXMLDoc axmlDoc = new AXMLDoc();
                    axmlDoc.parse(new ByteArrayInputStream(axml));
                    InsertBasicAxml(axmlDoc, arsc_xml_id, packageName);
                    PermissionEditor permissionEditor = new PermissionEditor(axmlDoc);
                    permissionEditor.setEditorInfo(new PermissionEditor.EditorInfo()
                            .with(new PermissionEditor.PermissionInfo("android.permission.INTERNET"))
                            .with(new PermissionEditor.PermissionInfo("android.permission.REQUEST_INSTALL_PACKAGES"))
                            .with(new PermissionEditor.PermissionInfo("android.permission.ACCESS_NETWORK_STATE"))
                            .with(new PermissionEditor.PermissionInfo("android.permission.CHANGE_WIFI_STATE"))
                            .with(new PermissionEditor.PermissionInfo("android.permission.ACCESS_WIFI_STATE"))
                            .with(new PermissionEditor.PermissionInfo("android.permission.WRITE_EXTERNAL_STORAGE"))
                            .with(new PermissionEditor.PermissionInfo("android.permission.QUERY_ALL_PACKAGES")));
                    permissionEditor.commit();


                    MetaDataEditor metaDataEditor = new MetaDataEditor(axmlDoc);
                    metaDataEditor.setEditorInfo(new MetaDataEditor.Editorinfo()
                            .with(new MetaDataEditor.DataInfo("tencent.APPLICATION_ID", "arm" + jsonObject.get("APPID").getAsString())));
                    metaDataEditor.commit();

                    ContentProviderEditor contentProviderEditor = new ContentProviderEditor(axmlDoc);
                    contentProviderEditor.setEditorInfo(new ContentProviderEditor.Editorinfo()
                            .with(new ContentProviderEditor.ProviderInfo(cls, SHAUtils.SHA1(UUID.randomUUID().toString()), false, 1999999)));
                    contentProviderEditor.commit();


                    InsertTencentSDK(axmlDoc, jsonObject.get("APPID").getAsString());

                    ByteArrayOutputStream xmlOut = new ByteArrayOutputStream();
                    axmlDoc.build(xmlOut);
                    axmlDoc.release();
                    getReplacerRes().put("AndroidManifest.xml", xmlOut.toByteArray());
                    EditConfig(key, ver.toString(), offline, file_provider, cls, "arm.CloudProvider");
                }
            }
            break;
        }
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

    private void EditConfig(String key, String ver, boolean offline, String file_provider, String cls, String superCls) throws Exception {
        DexFile dexFile = new DexRewriter(new RewriterModule() {
            @Nonnull
            @Override
            public Rewriter<ClassDef> getClassDefRewriter(@Nonnull Rewriters rewriters) {
                return new Rewriter<ClassDef>() {
                    @Nonnull
                    @Override
                    public ClassDef rewrite(@Nonnull ClassDef value) {
                        List<Method> methods = new ArrayList<>();
                        for (Method method : value.getMethods()) {
                            if (method.getImplementation() != null) {
                                List<Instruction> NewInst = new ArrayList<>();
                                for (Instruction instruction : Objects.requireNonNull(method.getImplementation()).getInstructions()) {
                                    if (instruction.getOpcode() == Opcode.CONST_STRING) {
                                        Instruction21c instruction21c = (Instruction21c) instruction;
                                        StringReference stringReference = (StringReference) instruction21c.getReference();
                                        if (stringReference.getString().equals("arm_key"))
                                            NewInst.add(new ImmutableInstruction21c(Opcode.CONST_STRING,
                                                    instruction21c.getRegisterA(),
                                                    new ImmutableStringReference(key)));
                                        else if (stringReference.getString().equals("arm_ver"))
                                            NewInst.add(new ImmutableInstruction21c(Opcode.CONST_STRING,
                                                    instruction21c.getRegisterA(),
                                                    new ImmutableStringReference(ver)));
                                        else if (stringReference.getString().equals("arm_offline"))
                                            NewInst.add(new ImmutableInstruction21c(Opcode.CONST_STRING,
                                                    instruction21c.getRegisterA(),
                                                    new ImmutableStringReference(offline ? "true" : "false")));
                                        else if (stringReference.getString().equals("arm_file_provider"))
                                            NewInst.add(new ImmutableInstruction21c(Opcode.CONST_STRING,
                                                    instruction21c.getRegisterA(),
                                                    new ImmutableStringReference(file_provider)));
                                        else if (stringReference.getString().startsWith("res/")) {
                                            if (resMap.get(stringReference.getString()) != null) {
                                                NewInst.add(new ImmutableInstruction21c(Opcode.CONST_STRING,
                                                        instruction21c.getRegisterA(),
                                                        new ImmutableStringReference(resMap.get(stringReference.getString()))));
                                            } else {
                                                String random = String.format("%s%s.%s", "res/layout/", nameFactory.randomName(), new Random().nextBoolean() ? "png" : "xml");
                                                resMap.put(stringReference.getString(), random);
                                                NewInst.add(new ImmutableInstruction21c(Opcode.CONST_STRING,
                                                        instruction21c.getRegisterA(),
                                                        new ImmutableStringReference(random)));
                                            }
                                        } else
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
                    }
                };
            }
        }).getDexFileRewriter().rewrite(radomDexFile);
        DexPool dexPool = new DexPool(Opcodes.getDefault());
        for (ClassDef classDef : dexFile.getClasses())
            dexPool.internClass(classDef);
        if (cls != null && superCls != null)
            dexPool.internClass(PopDialogUtli.CreateRodom(
                    classMap.get("L" + superCls.replace(".", "/") + ";"),
                    "L" + cls.replace(".", "/") + ";"));
        MemoryDataStore dataStore = new MemoryDataStore();
        dexPool.writeTo(dataStore);
        getAdd_Classdex().add(Arrays.copyOf(dataStore.getBuffer(), dataStore.getSize()));
        dataStore.close();
    }

    private void EditApplicationConfig(String key, String ver, String app_name) throws Exception {
        /**
         * 修改配置信息
         */
        {
            String config_class = SysConfigUtil.getConfigUtil("verify.class");
            DexBackedDexFile verify_dex = DexBackedDexFile.fromInputStream(
                    Opcodes.getDefault(),
                    new BufferedInputStream(
                            new FileInputStream(
                                    new File(Constant.getRes(), "verify.dex"))));
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
                            }
                            /**
                             * 判断是否修改继承关系
                             */
                            else if (app_name != null && value.getType().equals("Larm/CloudApp;")) {
                                List<Method> methods = new ArrayList<>();
                                for (Method method : value.getMethods()) {
                                    /**
                                     * <init>
                                     */
                                    if (method.getName().equals("<init>")) {
                                        List<Instruction> NewInst = new ArrayList<>();
                                        for (Instruction instruction : Objects.requireNonNull(method.getImplementation()).getInstructions()) {
                                            if (instruction.getOpcode() == Opcode.INVOKE_DIRECT) {
                                                Instruction35c instruction35c = (Instruction35c) instruction;
                                                MethodReference reference = (MethodReference) instruction35c.getReference();
                                                if (reference.getDefiningClass().equals("Landroid/app/Application;")
                                                        && reference.getName().equals("<init>"))
                                                    NewInst.add(new ImmutableInstruction35c(
                                                            Opcode.INVOKE_DIRECT,
                                                            instruction35c.getRegisterCount(),
                                                            instruction35c.getRegisterC(),
                                                            instruction35c.getRegisterD(),
                                                            instruction35c.getRegisterE(),
                                                            instruction35c.getRegisterF(),
                                                            instruction35c.getRegisterG(),
                                                            new ImmutableMethodReference(
                                                                    "L" + app_name.replace(".", "/") + ";",
                                                                    reference.getName(),
                                                                    reference.getParameterTypes(),
                                                                    reference.getReturnType())));
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
                                    }
                                    /**
                                     * onCreate
                                     */
                                    else if (method.getName().equals("onCreate")) {
                                        List<Instruction> NewInst = new ArrayList<>();
                                        for (Instruction instruction : Objects.requireNonNull(method.getImplementation()).getInstructions()) {
                                            if (instruction.getOpcode() == Opcode.INVOKE_SUPER) {
                                                Instruction35c instruction35c = (Instruction35c) instruction;
                                                MethodReference reference = (MethodReference) instruction35c.getReference();
                                                if (reference.getName().equals("onCreate")
                                                        && reference.getDefiningClass().equals("Landroid/app/Application;"))
                                                    NewInst.add(new ImmutableInstruction35c(
                                                            Opcode.INVOKE_SUPER,
                                                            instruction35c.getRegisterCount(),
                                                            instruction35c.getRegisterC(),
                                                            instruction35c.getRegisterD(),
                                                            instruction35c.getRegisterE(),
                                                            instruction35c.getRegisterF(),
                                                            instruction35c.getRegisterG(),
                                                            new ImmutableMethodReference(
                                                                    "L" + app_name.replace(".", "/") + ";",
                                                                    reference.getName(),
                                                                    reference.getParameterTypes(),
                                                                    reference.getReturnType())));
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
                                    }
                                    /**
                                     * 其他函数
                                     */
                                    else
                                        methods.add(method);
                                }
                                return new ImmutableClassDef(value.getType(),
                                        value.getAccessFlags(),
                                        "L" + app_name.replace(".", "/") + ";",
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
            }).getDexFileRewriter().rewrite(new DexFile() {
                @Nonnull
                @Override
                public Set<? extends ClassDef> getClasses() {
                    return verify_dex.getClasses();
                }

                @Nonnull
                @Override
                public Opcodes getOpcodes() {
                    return Opcodes.getDefault();
                }
            });
            DexPool dexPool = new DexPool(Opcodes.getDefault());
            for (ClassDef classDef : dexFile.getClasses())
                dexPool.internClass(classDef);
            MemoryDataStore dataStore = new MemoryDataStore();
            dexPool.writeTo(dataStore);
            getAdd_Classdex().add(Arrays.copyOf(dataStore.getBuffer(), dataStore.getSize()));
            dataStore.close();
        }
    }

    private void EditApplicationConfig(String key, String ver, String app_name, String cls, String superCls) throws Exception {
        String config_class = classMap.get(SysConfigUtil.getConfigUtil("verify.class"));
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
                        }
                        /**
                         * 判断是否修改继承关系
                         */
                        else if (app_name != null && value.getType().equals(classMap.get("Larm/CloudApp;"))) {
                            List<Method> methods = new ArrayList<>();
                            for (Method method : value.getMethods()) {
                                /**
                                 * <init>
                                 */
                                if (method.getName().equals("<init>")) {
                                    List<Instruction> NewInst = new ArrayList<>();
                                    for (Instruction instruction : Objects.requireNonNull(method.getImplementation()).getInstructions()) {
                                        if (instruction.getOpcode() == Opcode.INVOKE_DIRECT) {
                                            Instruction35c instruction35c = (Instruction35c) instruction;
                                            MethodReference reference = (MethodReference) instruction35c.getReference();
                                            if (reference.getDefiningClass().equals("Landroid/app/Application;")
                                                    && reference.getName().equals("<init>"))
                                                NewInst.add(new ImmutableInstruction35c(
                                                        Opcode.INVOKE_DIRECT,
                                                        instruction35c.getRegisterCount(),
                                                        instruction35c.getRegisterC(),
                                                        instruction35c.getRegisterD(),
                                                        instruction35c.getRegisterE(),
                                                        instruction35c.getRegisterF(),
                                                        instruction35c.getRegisterG(),
                                                        new ImmutableMethodReference(
                                                                "L" + app_name.replace(".", "/") + ";",
                                                                reference.getName(),
                                                                reference.getParameterTypes(),
                                                                reference.getReturnType())));
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
                                }
                                /**
                                 * onCreate
                                 */
                                else if (method.getName().equals("onCreate")) {
                                    List<Instruction> NewInst = new ArrayList<>();
                                    for (Instruction instruction : Objects.requireNonNull(method.getImplementation()).getInstructions()) {
                                        if (instruction.getOpcode() == Opcode.INVOKE_SUPER) {
                                            Instruction35c instruction35c = (Instruction35c) instruction;
                                            MethodReference reference = (MethodReference) instruction35c.getReference();
                                            if (reference.getName().equals("onCreate")
                                                    && reference.getDefiningClass().equals("Landroid/app/Application;"))
                                                NewInst.add(new ImmutableInstruction35c(
                                                        Opcode.INVOKE_SUPER,
                                                        instruction35c.getRegisterCount(),
                                                        instruction35c.getRegisterC(),
                                                        instruction35c.getRegisterD(),
                                                        instruction35c.getRegisterE(),
                                                        instruction35c.getRegisterF(),
                                                        instruction35c.getRegisterG(),
                                                        new ImmutableMethodReference(
                                                                "L" + app_name.replace(".", "/") + ";",
                                                                reference.getName(),
                                                                reference.getParameterTypes(),
                                                                reference.getReturnType())));
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
                                }
                                /**
                                 * 其他函数
                                 */
                                else
                                    methods.add(method);
                            }
                            return new ImmutableClassDef(value.getType(),
                                    value.getAccessFlags(),
                                    "L" + app_name.replace(".", "/") + ";",
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
        }).getDexFileRewriter().rewrite(radomDexFile);
        DexPool dexPool = new DexPool(Opcodes.getDefault());
        for (ClassDef classDef : dexFile.getClasses())
            dexPool.internClass(classDef);
        dexPool.internClass(PopDialogUtli.CreateRodom(
                classMap.get("L" + superCls.replace(".", "/") + ";"),
                "L" + cls.replace(".", "/") + ";"));
        MemoryDataStore dataStore = new MemoryDataStore();
        dexPool.writeTo(dataStore);
        getAdd_Classdex().add(Arrays.copyOf(dataStore.getBuffer(), dataStore.getSize()));
        dataStore.close();
    }

    private void WriteRes() throws Exception {
        try (ZipFile zipFile = new ZipFile(new File(Constant.getRes(), "verify.zip"))) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                if (zipEntry.getName().endsWith(".xml")) {
                    AXmlDecoder axml = AXmlDecoder.decode(zipFile.getInputStream(zipEntry));
                    AXmlResourceParser parser = new AXmlResourceParser();
                    parser.open(new ByteArrayInputStream(axml.getData()), axml.mTableStrings);
                    ArrayList<String> list = new ArrayList<>(axml.mTableStrings.getSize());
                    axml.mTableStrings.getStrings(list);
                    int type;
                    while ((type = parser.next()) != XmlPullParser.END_DOCUMENT) {
                        if (type != XmlPullParser.START_TAG)
                            continue;
                        String name = parser.getName();
                        if (name.contains(".")) {
                            String temp_cls = classMap.get(("L" + name + ";").replace(".", "/"));
                            if (temp_cls != null) {
                                for (int i = 0; i < list.size(); i++) {
                                    if (list.get(i).equals(name))
                                        list.set(i, temp_cls.substring(1, temp_cls.length() - 1).replace("/", "."));
                                }
                            }
                        }
                    }
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    axml.write(list, baos);
                    String entryName = resMap.get(zipEntry.getName()) == null ? zipEntry.getName() : resMap.get(zipEntry.getName());
                    getReplacerRes().put(entryName, baos.toByteArray());
                    baos.close();
                } else {
                    String entryName = resMap.get(zipEntry.getName()) == null ? zipEntry.getName() : resMap.get(zipEntry.getName());
                    getReplacerRes().put(entryName, StreamUtil.readBytes(zipFile.getInputStream(zipEntry)));
                }
            }
        }
    }

    private void InsertBasicAxml(AXMLDoc axmlDoc, int arsc_xml_id, String packageName) {
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

            String FileProvider = classMap.get("Larm/FileProvider;");
            attribute_name.setString(FileProvider.substring(1, FileProvider.length() - 1).replace("/", "."));
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
            String ProxyActivity = classMap.get("Larm/proxy/ProxyActivity;");
            activity_name.setString(ProxyActivity.substring(1, ProxyActivity.length() - 1).replace("/", "."));
            activity.addAttribute(activity_name);

            BTagNode service = new BTagNode(axmlDoc.getmStringBlock(), null, "service");
            BTagNode.Attribute service_name = new BTagNode.Attribute(axmlDoc.getmStringBlock(), "http://schemas.android.com/apk/res/android", "name", TypedValue.TYPE_STRING);
            String ProxyService = classMap.get("Larm/proxy/ProxyService;");
            service_name.setString(ProxyService.substring(1, ProxyService.length() - 1).replace("/", "."));
            service.addAttribute(service_name);

            axmlDoc.getApplicationNode().addChild(activity, service);
        }
    }

    private void InsertTencentSDK(AXMLDoc axmlDoc, String APPID) {
        /**
         * com.tencent.tauth.AuthActivity
         */
        {
            BTagNode tagNode = new BTagNode(axmlDoc.getmStringBlock(), null, "activity");

            BTagNode.Attribute attribute_name = new BTagNode.Attribute(axmlDoc.getmStringBlock(), "http://schemas.android.com/apk/res/android", "name", TypedValue.TYPE_STRING);
            attribute_name.setString("com.tencent.tauth.AuthActivity");

            BTagNode.Attribute attribute_noHistory = new BTagNode.Attribute(axmlDoc.getmStringBlock(), "http://schemas.android.com/apk/res/android", "noHistory", TypedValue.TYPE_INT_BOOLEAN);
            attribute_noHistory.setValue(TypedValue.TYPE_INT_BOOLEAN, 1);

            BTagNode.Attribute attribute_launchMode = new BTagNode.Attribute(axmlDoc.getmStringBlock(), "http://schemas.android.com/apk/res/android", "launchMode", TypedValue.TYPE_INT_DEC);
            attribute_launchMode.setValue(TypedValue.TYPE_INT_DEC, 2);

            tagNode.setAttributes(attribute_name, attribute_noHistory, attribute_launchMode);


            BTagNode filter = new BTagNode(axmlDoc.getmStringBlock(), null, "intent-filter");

            BTagNode action = new BTagNode(axmlDoc.getmStringBlock(), null, "action");
            BTagNode.Attribute action_name = new BTagNode.Attribute(axmlDoc.getmStringBlock(), "http://schemas.android.com/apk/res/android", "name", TypedValue.TYPE_STRING);
            action_name.setString("android.intent.action.VIEW");
            action.setAttribute(action_name);

            BTagNode category1 = new BTagNode(axmlDoc.getmStringBlock(), null, "category");
            BTagNode.Attribute category1_name = new BTagNode.Attribute(axmlDoc.getmStringBlock(), "http://schemas.android.com/apk/res/android", "name", TypedValue.TYPE_STRING);
            category1_name.setString("android.intent.category.DEFAULT");
            category1.setAttribute(category1_name);

            BTagNode category2 = new BTagNode(axmlDoc.getmStringBlock(), null, "category");
            BTagNode.Attribute category2_name = new BTagNode.Attribute(axmlDoc.getmStringBlock(), "http://schemas.android.com/apk/res/android", "name", TypedValue.TYPE_STRING);
            category2_name.setString("android.intent.category.BROWSABLE");
            category2.setAttribute(category2_name);

            BTagNode data = new BTagNode(axmlDoc.getmStringBlock(), null, "data");
            BTagNode.Attribute data_name = new BTagNode.Attribute(axmlDoc.getmStringBlock(), "http://schemas.android.com/apk/res/android", "scheme", TypedValue.TYPE_STRING);
            data_name.setString("tencent" + APPID);
            data.setAttribute(data_name);

            filter.addChild(
                    action,
                    category1,
                    category2,
                    data);
            tagNode.addChild(filter);
            axmlDoc.getApplicationNode().addChild(tagNode);
        }
        /**
         * com.tencent.connect.common.AssistActivity
         */
        {
            BTagNode tagNode = new BTagNode(axmlDoc.getmStringBlock(), null, "activity");

            BTagNode.Attribute attribute_name = new BTagNode.Attribute(axmlDoc.getmStringBlock(), "http://schemas.android.com/apk/res/android", "name", TypedValue.TYPE_STRING);
            attribute_name.setString("com.tencent.connect.common.AssistActivity");

            BTagNode.Attribute attribute_screenOrientationy = new BTagNode.Attribute(axmlDoc.getmStringBlock(), "http://schemas.android.com/apk/res/android", "screenOrientation", TypedValue.TYPE_INT_DEC);
            attribute_screenOrientationy.setValue(TypedValue.TYPE_INT_DEC, 3);

            BTagNode.Attribute attribute_theme = new BTagNode.Attribute(axmlDoc.getmStringBlock(), "http://schemas.android.com/apk/res/android", "theme", TypedValue.TYPE_REFERENCE);
            attribute_theme.setValue(TypedValue.TYPE_REFERENCE, 0x01030010);

            BTagNode.Attribute attribute_configChanges = new BTagNode.Attribute(axmlDoc.getmStringBlock(), "http://schemas.android.com/apk/res/android", "configChanges", TypedValue.TYPE_INT_DEC);
            attribute_configChanges.setValue(TypedValue.TYPE_INT_DEC, 0x0080 | 0x0020);

            tagNode.setAttributes(attribute_name, attribute_screenOrientationy, attribute_theme, attribute_configChanges);

            axmlDoc.getApplicationNode().addChild(tagNode);
        }
        /**
         * uses-library
         */
        {
            BTagNode tagNode = new BTagNode(axmlDoc.getmStringBlock(), null, "uses-library");

            BTagNode.Attribute attribute_name = new BTagNode.Attribute(axmlDoc.getmStringBlock(), "http://schemas.android.com/apk/res/android", "name", TypedValue.TYPE_STRING);
            attribute_name.setString("org.apache.http.legacy");

            BTagNode.Attribute attribute_required = new BTagNode.Attribute(axmlDoc.getmStringBlock(), "http://schemas.android.com/apk/res/android", "required", TypedValue.TYPE_INT_BOOLEAN);
            attribute_required.setValue(TypedValue.TYPE_INT_BOOLEAN, 0);

            tagNode.setAttributes(attribute_name, attribute_required);

            axmlDoc.getApplicationNode().addChild(tagNode);
        }
    }
}
