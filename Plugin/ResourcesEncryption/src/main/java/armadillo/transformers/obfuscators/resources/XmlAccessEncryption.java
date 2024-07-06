package armadillo.transformers.obfuscators.resources;

import armadillo.transformers.base.BaseTransformer;
import armadillo.transformers.base.DexTransformer;
import armadillo.utils.StringRandom;
import armadillo.utils.SysConfigUtil;
import com.google.common.collect.Lists;
import com.google.devrel.gmscore.tools.apk.arsc.*;
import org.jf.dexlib2.AccessFlags;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.builder.MutableMethodImplementation;
import org.jf.dexlib2.builder.instruction.BuilderInstruction11x;
import org.jf.dexlib2.builder.instruction.BuilderInstruction35c;
import org.jf.dexlib2.builder.instruction.BuilderInstruction3rc;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.formats.Instruction21ih;
import org.jf.dexlib2.iface.instruction.formats.Instruction31i;
import org.jf.dexlib2.iface.instruction.formats.Instruction35c;
import org.jf.dexlib2.iface.reference.MethodReference;
import org.jf.dexlib2.immutable.ImmutableClassDef;
import org.jf.dexlib2.immutable.ImmutableMethod;
import org.jf.dexlib2.immutable.ImmutableMethodImplementation;
import org.jf.dexlib2.immutable.reference.ImmutableMethodProtoReference;
import org.jf.dexlib2.immutable.reference.ImmutableMethodReference;
import org.jf.dexlib2.rewriter.Rewriter;
import org.jf.dexlib2.rewriter.Rewriters;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.zip.ZipEntry;

public class XmlAccessEncryption extends DexTransformer {
    private BinaryResourceFile arsc;
    private int xml_encrypt_total;

    @Nonnull
    @Override
    public Rewriter<ClassDef> getClassDefRewriter(@Nonnull Rewriters rewriters) {
        return new Rewriter<ClassDef>() {
            @Nonnull
            @Override
            public ClassDef rewrite(@Nonnull ClassDef classDef) {
                List<Method> newMethod = new ArrayList<>();
                boolean flag = false;
                HashMap<String, String> $map1 = new HashMap<>();
                HashMap<String, String> $map2 = new HashMap<>();
                for (Method method : classDef.getMethods()) {
                    if (AccessFlags.STATIC.isSet(method.getAccessFlags()))
                        newMethod.add(method);
                    else {
                        if (method.getImplementation() != null) {
                            MutableMethodImplementation mutableImplementation = new MutableMethodImplementation(method.getImplementation());
                            boolean isHandel = false;
                            for (int i = 0; i < mutableImplementation.getInstructions().size(); i++) {
                                Instruction instruction = mutableImplementation.getInstructions().get(i);
                                switch (instruction.getOpcode()) {
                                    case INVOKE_VIRTUAL: {
                                        Instruction35c Instruction35c = (Instruction35c) instruction;
                                        MethodReference methodReference = (MethodReference) Instruction35c.getReference();
                                        switch (methodReference.getName()) {
                                            case "setContentView": {
                                                if (methodReference.getParameterTypes().size() > 0
                                                        && methodReference.getParameterTypes().get(0).equals("I")
                                                        && methodReference.getReturnType().equals("V")) {
                                                    if (i - 1 > 0) {
                                                        Instruction id_index = mutableImplementation.getInstructions().get(i - 1);
                                                        if (id_index.getOpcode() == Opcode.CONST_HIGH16 || id_index.getOpcode() == Opcode.CONST) {
                                                            int id = -1;
                                                            int vx = -1;
                                                            if (id_index.getOpcode() == Opcode.CONST_HIGH16) {
                                                                Instruction21ih instruction21ih = (Instruction21ih) id_index;
                                                                if (instruction21ih.getRegisterA() != Instruction35c.getRegisterD())
                                                                    continue;
                                                                id = instruction21ih.getNarrowLiteral();
                                                                vx = instruction21ih.getRegisterA();
                                                            } else if (id_index.getOpcode() == Opcode.CONST) {
                                                                Instruction31i instruction31i = (Instruction31i) id_index;
                                                                if (instruction31i.getRegisterA() != Instruction35c.getRegisterD())
                                                                    continue;
                                                                id = instruction31i.getNarrowLiteral();
                                                                vx = instruction31i.getRegisterA();
                                                            }
                                                            if (id == -1)
                                                                continue;
                                                            /**
                                                             * 寻找资源id对应的xml
                                                             */
                                                            String hex_id = "0x" + Integer.toHexString(id);
                                                            String file_name = null;
                                                            {
                                                                List<Chunk> chunks = arsc.getChunks();
                                                                for (Chunk chunk : chunks) {
                                                                    if (chunk instanceof ResourceTableChunk) {
                                                                        ResourceTableChunk resourceTableChunk = (ResourceTableChunk) chunk;
                                                                        for (PackageChunk packageChunk : resourceTableChunk.getPackages())
                                                                            for (TypeChunk typeChunk : packageChunk.getTypeChunks()) {
                                                                                for (Map.Entry<Integer, TypeChunk.Entry> entry : typeChunk.getEntries().entrySet()) {
                                                                                    BinaryResourceIdentifier binaryResourceIdentifier = BinaryResourceIdentifier.create(packageChunk.getId(), typeChunk.getId(), (int) entry.getKey());
                                                                                    if (entry.getValue().value() == null)
                                                                                        continue;
                                                                                    if (hex_id.equals(binaryResourceIdentifier.toString()))
                                                                                        if (entry.getValue().value().data() > ((ResourceTableChunk) chunk).getStringPool().getStringCount()
                                                                                                || entry.getValue().value().data() < 0)
                                                                                            file_name = null;
                                                                                        else
                                                                                            file_name = ((ResourceTableChunk) chunk).getStringPool().getString(entry.getValue().value().data());
                                                                                }
                                                                            }
                                                                    }
                                                                }
                                                            }
                                                            if (file_name == null)
                                                                continue;
                                                            /**
                                                             * 替换成解密加载函数
                                                             */
                                                            {
                                                                String method_name = StringRandom.RandomString();
                                                                int p0 = method.getImplementation().getRegisterCount() - method.getParameters().size() - 1;
                                                                if (p0 > 15)
                                                                    mutableImplementation.replaceInstruction(i, new BuilderInstruction3rc(
                                                                            Opcode.INVOKE_VIRTUAL_RANGE,
                                                                            p0,
                                                                            1,
                                                                            new ImmutableMethodReference(
                                                                                    classDef.getType(),
                                                                                    method_name,
                                                                                    null,
                                                                                    "Landroid/view/View;")));
                                                                else
                                                                    mutableImplementation.replaceInstruction(i, new BuilderInstruction35c(
                                                                            Opcode.INVOKE_VIRTUAL,
                                                                            1,
                                                                            p0,
                                                                            0,
                                                                            0,
                                                                            0,
                                                                            0,
                                                                            new ImmutableMethodReference(
                                                                                    classDef.getType(),
                                                                                    method_name,
                                                                                    null,
                                                                                    "Landroid/view/View;")));
                                                                mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction11x(Opcode.MOVE_RESULT_OBJECT, vx));
                                                                mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction35c(
                                                                        Opcode.INVOKE_VIRTUAL,
                                                                        2,
                                                                        ((BuilderInstruction35c) instruction).getRegisterC(),
                                                                        ((BuilderInstruction35c) instruction).getRegisterD(),
                                                                        0,
                                                                        0,
                                                                        0,
                                                                        new ImmutableMethodReference(
                                                                                methodReference.getDefiningClass(),
                                                                                "setContentView",
                                                                                Lists.newArrayList("Landroid/view/View;"),
                                                                                "V")));
                                                                $map1.put(method_name, file_name);
                                                            }
                                                            flag = true;
                                                            isHandel = true;
                                                            xml_encrypt_total++;
                                                        }
                                                    }
                                                }
                                            }
                                            break;
                                            case "inflate": {
                                                if (methodReference.getParameterTypes().size() == 2
                                                        && methodReference.getParameterTypes().get(0).equals("I")
                                                        && methodReference.getParameterTypes().get(1).equals("Landroid/view/ViewGroup;")
                                                        && methodReference.getReturnType().equals("Landroid/view/View;")) {
                                                    if (i - 2 > 0) {
                                                        Instruction id_index = mutableImplementation.getInstructions().get(i - 2);
                                                        if (id_index.getOpcode() == Opcode.CONST_HIGH16
                                                                || id_index.getOpcode() == Opcode.CONST) {
                                                            //获取需要加密的xml id
                                                            int id = -1;
                                                            int vx = -1;
                                                            if (id_index.getOpcode() == Opcode.CONST_HIGH16) {
                                                                Instruction21ih instruction21ih = (Instruction21ih) id_index;
                                                                if (instruction21ih.getRegisterA() != Instruction35c.getRegisterD())
                                                                    continue;
                                                                id = instruction21ih.getNarrowLiteral();
                                                                vx = instruction21ih.getRegisterA();
                                                            } else if (id_index.getOpcode() == Opcode.CONST) {
                                                                Instruction31i instruction31i = (Instruction31i) id_index;
                                                                if (instruction31i.getRegisterA() != Instruction35c.getRegisterD())
                                                                    continue;
                                                                id = instruction31i.getNarrowLiteral();
                                                                vx = instruction31i.getRegisterA();
                                                            }
                                                            if (id == -1)
                                                                continue;
                                                            //寻找资源id对应的xml
                                                            String hex_id = "0x" + Integer.toHexString(id);
                                                            String file_name = null;
                                                            {
                                                                List<Chunk> chunks = arsc.getChunks();
                                                                for (Chunk chunk : chunks) {
                                                                    if (chunk instanceof ResourceTableChunk) {
                                                                        ResourceTableChunk resourceTableChunk = (ResourceTableChunk) chunk;
                                                                        for (PackageChunk packageChunk : resourceTableChunk.getPackages())
                                                                            for (TypeChunk typeChunk : packageChunk.getTypeChunks()) {
                                                                                for (Map.Entry<Integer, TypeChunk.Entry> entry : typeChunk.getEntries().entrySet()) {
                                                                                    BinaryResourceIdentifier binaryResourceIdentifier = BinaryResourceIdentifier.create(packageChunk.getId(), typeChunk.getId(), (int) entry.getKey());
                                                                                    if (entry.getValue().value() == null)
                                                                                        continue;
                                                                                    if (hex_id.equals(binaryResourceIdentifier.toString()))
                                                                                        if (entry.getValue().value().data() > ((ResourceTableChunk) chunk).getStringPool().getStringCount()
                                                                                                || entry.getValue().value().data() < 0)
                                                                                            file_name = null;
                                                                                        else
                                                                                            file_name = ((ResourceTableChunk) chunk).getStringPool().getString(entry.getValue().value().data());

                                                                                }
                                                                            }
                                                                    }
                                                                }
                                                            }
                                                            if (file_name == null)
                                                                continue;
                                                            //invoke-static {}, Lxxxx/xxxx;->$()Landroid/view/View;
                                                            {
                                                                String method_name = StringRandom.RandomString();
                                                                int p0 = method.getImplementation().getRegisterCount() - method.getParameters().size() - 1;
                                                                if (p0 > 15)
                                                                    mutableImplementation.replaceInstruction(i, new BuilderInstruction3rc(
                                                                            Opcode.INVOKE_VIRTUAL_RANGE,
                                                                            p0,
                                                                            1,
                                                                            new ImmutableMethodReference(
                                                                                    classDef.getType(),
                                                                                    method_name,
                                                                                    null,
                                                                                    "Landroid/content/res/XmlResourceParser;")));
                                                                else
                                                                    mutableImplementation.replaceInstruction(i, new BuilderInstruction35c(
                                                                            Opcode.INVOKE_VIRTUAL,
                                                                            1,
                                                                            p0,
                                                                            0,
                                                                            0,
                                                                            0,
                                                                            0,
                                                                            new ImmutableMethodReference(
                                                                                    classDef.getType(),
                                                                                    method_name,
                                                                                    null,
                                                                                    "Landroid/content/res/XmlResourceParser;")));
                                                                mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction11x(Opcode.MOVE_RESULT_OBJECT, vx));
                                                                mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction35c(
                                                                        Opcode.INVOKE_VIRTUAL,
                                                                        3,
                                                                        ((BuilderInstruction35c) instruction).getRegisterC(),
                                                                        ((BuilderInstruction35c) instruction).getRegisterD(),
                                                                        ((BuilderInstruction35c) instruction).getRegisterE(),
                                                                        0,
                                                                        0,
                                                                        new ImmutableMethodReference(
                                                                                "Landroid/view/LayoutInflater;",
                                                                                "inflate",
                                                                                Lists.newArrayList("Lorg/xmlpull/v1/XmlPullParser;", "Landroid/view/ViewGroup;"),
                                                                                "Landroid/view/View;")));
                                                                $map2.put(method_name, file_name);
                                                            }
                                                            flag = true;
                                                            isHandel = true;
                                                            xml_encrypt_total++;
                                                        }
                                                    }
                                                } else if (methodReference.getParameterTypes().size() == 3
                                                        && methodReference.getParameterTypes().get(0).equals("I")
                                                        && methodReference.getParameterTypes().get(1).equals("Landroid/view/ViewGroup;")
                                                        && methodReference.getParameterTypes().get(2).equals("Z")
                                                        && methodReference.getReturnType().equals("Landroid/view/View;")) {
                                                    if (i - 1 > 0) {
                                                        for (int k = i; k > -1; k--) {
                                                            Instruction id_index = mutableImplementation.getInstructions().get(k);
                                                            if (id_index.getOpcode() == Opcode.CONST_HIGH16
                                                                    || id_index.getOpcode() == Opcode.CONST) {
                                                                //获取需要加密的xml id
                                                                int id = -1;
                                                                int vx = -1;
                                                                if (id_index.getOpcode() == Opcode.CONST_HIGH16) {
                                                                    Instruction21ih instruction21ih = (Instruction21ih) id_index;
                                                                    if (instruction21ih.getRegisterA() != Instruction35c.getRegisterD())
                                                                        continue;
                                                                    id = instruction21ih.getNarrowLiteral();
                                                                    vx = instruction21ih.getRegisterA();
                                                                } else if (id_index.getOpcode() == Opcode.CONST) {
                                                                    Instruction31i instruction31i = (Instruction31i) id_index;
                                                                    if (instruction31i.getRegisterA() != Instruction35c.getRegisterD())
                                                                        continue;
                                                                    id = instruction31i.getNarrowLiteral();
                                                                    vx = instruction31i.getRegisterA();
                                                                }
                                                                if (id == -1)
                                                                    continue;
                                                                //寻找资源id对应的xml
                                                                String hex_id = "0x" + Integer.toHexString(id);
                                                                String file_name = null;
                                                                {
                                                                    List<Chunk> chunks = arsc.getChunks();
                                                                    for (Chunk chunk : chunks) {
                                                                        if (chunk instanceof ResourceTableChunk) {
                                                                            ResourceTableChunk resourceTableChunk = (ResourceTableChunk) chunk;
                                                                            for (PackageChunk packageChunk : resourceTableChunk.getPackages())
                                                                                for (TypeChunk typeChunk : packageChunk.getTypeChunks()) {
                                                                                    for (Map.Entry<Integer, TypeChunk.Entry> entry : typeChunk.getEntries().entrySet()) {
                                                                                        BinaryResourceIdentifier binaryResourceIdentifier = BinaryResourceIdentifier.create(packageChunk.getId(), typeChunk.getId(), (int) entry.getKey());
                                                                                        if (entry.getValue().value() == null)
                                                                                            continue;
                                                                                        //logger.info(((ResourceTableChunk) chunk).getStringPool().getString(entry.getValue().value().data()) + " -> " + binaryResourceIdentifier.toString());
                                                                                        if (hex_id.equals(binaryResourceIdentifier.toString()))
                                                                                            if (entry.getValue().value().data() > ((ResourceTableChunk) chunk).getStringPool().getStringCount()
                                                                                                    || entry.getValue().value().data() < 0)
                                                                                                file_name = null;
                                                                                            else
                                                                                                file_name = ((ResourceTableChunk) chunk).getStringPool().getString(entry.getValue().value().data());
                                                                                    }
                                                                                }
                                                                        }
                                                                    }
                                                                }
                                                                if (file_name == null)
                                                                    continue;
                                                                //invoke-static {}, Lxxxx/xxxx;->$()Landroid/view/View;
                                                                {
                                                                    String method_name = StringRandom.RandomString();
                                                                    int p0 = method.getImplementation().getRegisterCount() - method.getParameters().size() - 1;
                                                                    if (p0 > 15)
                                                                        mutableImplementation.replaceInstruction(i, new BuilderInstruction3rc(
                                                                                Opcode.INVOKE_VIRTUAL_RANGE,
                                                                                p0,
                                                                                1,
                                                                                new ImmutableMethodReference(
                                                                                        classDef.getType(),
                                                                                        method_name,
                                                                                        null,
                                                                                        "Landroid/content/res/XmlResourceParser;")));
                                                                    else
                                                                        mutableImplementation.replaceInstruction(i, new BuilderInstruction35c(
                                                                                Opcode.INVOKE_VIRTUAL,
                                                                                1,
                                                                                p0,
                                                                                0,
                                                                                0,
                                                                                0,
                                                                                0,
                                                                                new ImmutableMethodReference(
                                                                                        classDef.getType(),
                                                                                        method_name,
                                                                                        null,
                                                                                        "Landroid/content/res/XmlResourceParser;")));
                                                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction11x(Opcode.MOVE_RESULT_OBJECT, vx));
                                                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction35c(
                                                                            Opcode.INVOKE_VIRTUAL,
                                                                            4,
                                                                            ((BuilderInstruction35c) instruction).getRegisterC(),
                                                                            ((BuilderInstruction35c) instruction).getRegisterD(),
                                                                            ((BuilderInstruction35c) instruction).getRegisterE(),
                                                                            ((BuilderInstruction35c) instruction).getRegisterF(),
                                                                            0,
                                                                            new ImmutableMethodReference(
                                                                                    "Landroid/view/LayoutInflater;",
                                                                                    "inflate",
                                                                                    Lists.newArrayList("Lorg/xmlpull/v1/XmlPullParser;", "Landroid/view/ViewGroup;", "Z"),
                                                                                    "Landroid/view/View;")));
                                                                    $map2.put(method_name, file_name);
                                                                }
                                                                flag = true;
                                                                isHandel = true;
                                                                xml_encrypt_total++;
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            break;
                                        }
                                    }
                                    break;
                                }
                            }
                            if (isHandel)
                                newMethod.add(new ImmutableMethod(
                                        method.getDefiningClass(),
                                        method.getName(),
                                        method.getParameters(),
                                        method.getReturnType(),
                                        method.getAccessFlags(),
                                        method.getAnnotations(),
                                        method.getHiddenApiRestrictions(),
                                        new ImmutableMethodImplementation(
                                                mutableImplementation.getRegisterCount(),
                                                mutableImplementation.getInstructions(),
                                                mutableImplementation.getTryBlocks(),
                                                mutableImplementation.getDebugItems())));
                            else
                                newMethod.add(method);
                        } else
                            newMethod.add(method);
                    }
                }
                if (flag) {
                    newMethod.addAll(XmlUtils.CreateXml$(classDef, $map1, getZipFile()));
                    newMethod.addAll(XmlUtils.CreateXmlPull$(classDef, $map2, getZipFile()));
                    return new ImmutableClassDef(
                            classDef.getType(),
                            classDef.getAccessFlags(),
                            classDef.getSuperclass(),
                            classDef.getInterfaces(),
                            classDef.getSourceFile(),
                            classDef.getAnnotations(),
                            classDef.getFields(),
                            newMethod);
                } else
                    return classDef;
            }
        };
    }

    @Override
    public void transform() throws Exception {
        xml_encrypt_total = 0;
        if (arsc == null)
            arsc = BinaryResourceFile.fromInputStream(getZipFile().getInputStream(new ZipEntry("resources.arsc")));
    }

    @Override
    public String getResult() {
        return String.format(Objects.requireNonNull(SysConfigUtil.getLanguageConfigUtil(getLanguageEnums(), "xml.encryption")), xml_encrypt_total);
    }

    @Override
    public int priority() {
        return 0;
    }

    @Override
    public int compareTo(BaseTransformer o) {
        return priority() - o.priority();
    }
}
