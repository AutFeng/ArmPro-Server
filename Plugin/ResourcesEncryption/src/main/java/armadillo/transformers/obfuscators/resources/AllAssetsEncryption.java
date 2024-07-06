package armadillo.transformers.obfuscators.resources;

import armadillo.transformers.base.BaseTransformer;
import armadillo.transformers.base.DexTransformer;
import armadillo.utils.SHAUtils;
import armadillo.utils.StringRandom;
import armadillo.utils.SysConfigUtil;
import armadillo.utils.axml.AutoXml.util.StreamUtil;
import com.google.common.collect.Lists;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.builder.MutableMethodImplementation;
import org.jf.dexlib2.builder.instruction.BuilderInstruction35c;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.instruction.Instruction;
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

public class AllAssetsEncryption extends DexTransformer {
    private int assets_encrypt_total;
    private int assets_total;
    private String randon;

    @Nonnull
    @Override
    public Rewriter<ClassDef> getClassDefRewriter(@Nonnull Rewriters rewriters) {
        return new Rewriter<ClassDef>() {
            @Nonnull
            @Override
            public ClassDef rewrite(@Nonnull ClassDef classDef) {
                List<Method> newMethod = new ArrayList<>();
                boolean flag = false;
                for (Method method : classDef.getMethods()) {
                    if (method.getImplementation() != null) {
                        MutableMethodImplementation mutableImplementation = new MutableMethodImplementation(method.getImplementation());
                        boolean isHandel = false;
                        for (int i = 0; i < mutableImplementation.getInstructions().size(); i++) {
                            Instruction instruction = mutableImplementation.getInstructions().get(i);
                            if (instruction.getOpcode() == Opcode.INVOKE_VIRTUAL) {
                                Instruction35c Instruction35c = (Instruction35c) instruction;
                                MethodReference methodReference = (MethodReference) Instruction35c.getReference();
                                if (methodReference.getName().equals("open")
                                        && methodReference.getDefiningClass().equals("Landroid/content/res/AssetManager;")) {
                                    mutableImplementation.replaceInstruction(i, new BuilderInstruction35c(
                                            Opcode.INVOKE_STATIC,
                                            1,
                                            Instruction35c.getRegisterD(),
                                            0,
                                            0,
                                            0,
                                            0,
                                            new ImmutableMethodReference(
                                                    "LArmadillo/" + randon + ";",
                                                    "of",
                                                    Lists.newArrayList("Ljava/lang/String;"),
                                                    "Ljava/io/InputStream;")));
                                    if (!flag)
                                        flag = true;
                                    isHandel = true;
                                    assets_encrypt_total++;
                                }
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
                if (flag)
                    return new ImmutableClassDef(
                            classDef.getType(),
                            classDef.getAccessFlags(),
                            classDef.getSuperclass(),
                            classDef.getInterfaces(),
                            classDef.getSourceFile(),
                            classDef.getAnnotations(),
                            classDef.getFields(),
                            newMethod);
                else
                    return classDef;
            }
        };
    }

    @Override
    public void transform() throws Exception {
        assets_encrypt_total = 0;
        assets_total = 0;
        randon = SHAUtils.SHA1(StringRandom.RandomString());
        if (!getReplacerRes().containsKey("Armadillo.res")) {
            HashMap<String, byte[]> hashMap = new HashMap<>();
            Enumeration<? extends ZipEntry> enumeration = getZipFile().entries();
            while (enumeration.hasMoreElements()) {
                ZipEntry zipEntry = enumeration.nextElement();
                if (zipEntry.isDirectory())
                    continue;
                if (zipEntry.getName().startsWith("assets/")) {
                    assets_total++;
                    hashMap.put(
                            zipEntry.getName().replace("assets/", ""),
                            StreamUtil.readBytes(getZipFile().getInputStream(new ZipEntry(zipEntry.getName()))));
                }
            }
            getReplacerRes().put("Armadillo.res", FullAssetsUtils.write(hashMap, "Armadillo"));
        }
    }

    @Override
    public String getResult() {
        return String.format(Objects.requireNonNull(SysConfigUtil.getLanguageConfigUtil(getLanguageEnums(), "assets.full.encryption")), assets_encrypt_total, assets_total);
    }

    @Override
    public int priority() {
        return 0;
    }

    @Override
    public Set<? extends ClassDef> getNewClassDef() {
        HashSet<ClassDef> classDefs = new HashSet<>();
        try {
            ClassDef classDef = FullAssetsUtils.CreateDecrypt(randon);
            classDefs.add(classDef);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return classDefs;
    }

    @Override
    public int compareTo(BaseTransformer o) {
        return priority() - o.priority();
    }
}
