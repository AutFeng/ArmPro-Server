package armadillo.transformers.obfuscators.resources;

import armadillo.transformers.base.BaseTransformer;
import armadillo.transformers.base.DexTransformer;
import armadillo.utils.StringRandom;
import armadillo.utils.SysConfigUtil;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.builder.MutableMethodImplementation;
import org.jf.dexlib2.builder.instruction.BuilderInstruction10x;
import org.jf.dexlib2.builder.instruction.BuilderInstruction35c;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.formats.Instruction21c;
import org.jf.dexlib2.iface.instruction.formats.Instruction35c;
import org.jf.dexlib2.iface.reference.MethodReference;
import org.jf.dexlib2.iface.reference.StringReference;
import org.jf.dexlib2.immutable.ImmutableClassDef;
import org.jf.dexlib2.immutable.ImmutableMethod;
import org.jf.dexlib2.immutable.ImmutableMethodImplementation;
import org.jf.dexlib2.immutable.reference.ImmutableMethodProtoReference;
import org.jf.dexlib2.immutable.reference.ImmutableMethodReference;
import org.jf.dexlib2.rewriter.Rewriter;
import org.jf.dexlib2.rewriter.Rewriters;

import javax.annotation.Nonnull;
import java.util.*;

public class AssetsAccessEncryption extends DexTransformer {
    private int assets_encrypt_total;

    @Nonnull
    @Override
    public Rewriter<ClassDef> getClassDefRewriter(@Nonnull Rewriters rewriters) {
        return new Rewriter<ClassDef>() {
            @Nonnull
            @Override
            public ClassDef rewrite(@Nonnull ClassDef classDef) {
                List<Method> newMethod = new ArrayList<>();
                boolean flag = false;
                HashMap<String, String> $map = new HashMap<>();
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
                                    if (i - 1 < 0)
                                        continue;
                                    Instruction const_string = mutableImplementation.getInstructions().get(i - 1);
                                    if (const_string.getOpcode() == Opcode.CONST_STRING) {
                                        /**
                                         * 拿到需要加密的文件名
                                         */
                                        Instruction21c instruction21c = (Instruction21c) const_string;
                                        if (instruction21c.getRegisterA() != Instruction35c.getRegisterD())
                                            continue;
                                        /**
                                         * 移除文件名
                                         */
                                        mutableImplementation.replaceInstruction(i - 1, new BuilderInstruction10x(Opcode.NOP));
                                        StringReference stringReference = (StringReference) instruction21c.getReference();
                                        String file_name = stringReference.getString();
                                        /**
                                         * 替换成解密函数
                                         */
                                        String method_name = StringRandom.RandomString();
                                        mutableImplementation.replaceInstruction(i, new BuilderInstruction35c(
                                                Opcode.INVOKE_STATIC,
                                                0,
                                                0,
                                                0,
                                                0,
                                                0,
                                                0,
                                                new ImmutableMethodReference(
                                                        classDef.getType(),
                                                        method_name,
                                                        null,
                                                        "Ljava/io/InputStream;")));
                                        $map.put(method_name, file_name);
                                        if (!flag)
                                            flag = true;
                                        isHandel = true;
                                        assets_encrypt_total++;
                                    }
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
                if (flag) {
                    newMethod.addAll(AssetsUtils.CreateAsset$(classDef, $map, getZipFile()));
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
        assets_encrypt_total = 0;
    }

    @Override
    public String getResult() {
        return String.format(Objects.requireNonNull(SysConfigUtil.getLanguageConfigUtil(getLanguageEnums(), "assets.encryption")), assets_encrypt_total);
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
