package armadillo.transformers.obfuscators.string;

import armadillo.common.SimpleNameFactory;
import armadillo.transformers.base.BaseTransformer;
import armadillo.transformers.base.DexTransformer;
import armadillo.utils.StringRandom;
import armadillo.utils.SysConfigUtil;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.builder.MutableMethodImplementation;
import org.jf.dexlib2.builder.instruction.BuilderInstruction11x;
import org.jf.dexlib2.builder.instruction.BuilderInstruction21c;
import org.jf.dexlib2.builder.instruction.BuilderInstruction35c;
import org.jf.dexlib2.builder.instruction.BuilderInstruction3rc;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.formats.Instruction21c;
import org.jf.dexlib2.iface.instruction.formats.Instruction31c;
import org.jf.dexlib2.iface.reference.StringReference;
import org.jf.dexlib2.immutable.ImmutableClassDef;
import org.jf.dexlib2.immutable.ImmutableMethod;
import org.jf.dexlib2.immutable.ImmutableMethodImplementation;
import org.jf.dexlib2.immutable.reference.ImmutableMethodProtoReference;
import org.jf.dexlib2.immutable.reference.ImmutableMethodReference;
import org.jf.dexlib2.immutable.reference.ImmutableStringReference;
import org.jf.dexlib2.rewriter.Rewriter;
import org.jf.dexlib2.rewriter.Rewriters;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.*;

public class StringEncryptionV1 extends DexTransformer {
    private int string_encrypt_total;
    private final HashSet<String> diff = new HashSet<>();

    @Nonnull
    @Override
    public Rewriter<ClassDef> getClassDefRewriter(@Nonnull Rewriters rewriters) {
        return new Rewriter<ClassDef>() {
            @Nonnull
            @Override
            public ClassDef rewrite(@Nonnull ClassDef classDef) {
                if (diff.contains(classDef.getType())) {
                    String randon = nameFactory.nextName();
                    int key1 = new Random().nextInt(99999)
                            + new Random().nextInt(99999)
                            + new Random().nextInt(99999)
                            + new Random().nextInt(99999)
                            + new Random().nextInt(99999);
                    int key2 = String.valueOf(key1).hashCode();
                    int key3 = String.valueOf(key2).hashCode();
                    List<Method> newMethod = new ArrayList<>();
                    for (Method method : classDef.getMethods()) {
                        if (method.getImplementation() != null) {
                            MutableMethodImplementation mutableImplementation = new MutableMethodImplementation(method.getImplementation());
                            for (int i = 0; i < mutableImplementation.getInstructions().size(); i++) {
                                Instruction instruction = mutableImplementation.getInstructions().get(i);
                                switch (instruction.getOpcode()) {
                                    case CONST_STRING:
                                    case CONST_STRING_JUMBO: {
                                        String string = null;
                                        int regA = 0;
                                        if (instruction.getOpcode() == Opcode.CONST_STRING) {
                                            Instruction21c instruction21c = (Instruction21c) instruction;
                                            StringReference stringReference = (StringReference) instruction21c.getReference();
                                            string = stringReference.getString();
                                            regA = instruction21c.getRegisterA();
                                        } else if (instruction.getOpcode() == Opcode.CONST_STRING_JUMBO) {
                                            Instruction31c instruction31c = (Instruction31c) instruction;
                                            StringReference stringReference = (StringReference) instruction31c.getReference();
                                            string = stringReference.getString();
                                            regA = instruction31c.getRegisterA();
                                        }
                                        if (string == null || string.isEmpty())
                                            break;
                                        //const vx,"xxxxxx"
                                        {
                                            mutableImplementation.replaceInstruction(i, new BuilderInstruction21c(Opcode.CONST_STRING, regA, new ImmutableStringReference(StringUtils.encrypt(string, key1, key2, key3))));
                                        }
                                        //invoke-static{vx} Lxxxx/xxxx;->$(Ljava/lang/String;)Ljava/lang/String;
                                        {
                                            if (method.getImplementation().getRegisterCount() > 15) {
                                                //INVOKE_STATIC_RANGE
                                                mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction3rc(
                                                        Opcode.INVOKE_STATIC_RANGE,
                                                        regA,
                                                        1,
                                                        new ImmutableMethodReference(
                                                                classDef.getType(),
                                                                randon,
                                                                Lists.newArrayList("Ljava/lang/String;"),
                                                                "Ljava/lang/String;")));
                                            } else {
                                                mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction35c(
                                                        Opcode.INVOKE_STATIC,
                                                        1,
                                                        regA,
                                                        0,
                                                        0,
                                                        0,
                                                        0,
                                                        new ImmutableMethodReference(
                                                                classDef.getType(),
                                                                randon,
                                                                Lists.newArrayList("Ljava/lang/String;"),
                                                                "Ljava/lang/String;")));
                                            }
                                        }
                                        //move-result-object vx
                                        {
                                            mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction11x(Opcode.MOVE_RESULT_OBJECT, regA));
                                        }
                                        //invoke-virtual {v1}, Ljava/lang/String;->intern()Ljava/lang/String;
                                        {
                                            if (method.getImplementation().getRegisterCount() > 15) {
                                                mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction3rc(
                                                        Opcode.INVOKE_VIRTUAL_RANGE,
                                                        regA,
                                                        1,
                                                        new ImmutableMethodReference(
                                                                "Ljava/lang/String;",
                                                                "intern",
                                                                null,
                                                                "Ljava/lang/String;")));
                                            } else {
                                                mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction35c(
                                                        Opcode.INVOKE_VIRTUAL,
                                                        1,
                                                        regA,
                                                        0,
                                                        0,
                                                        0,
                                                        0,
                                                        new ImmutableMethodReference(
                                                                "Ljava/lang/String;",
                                                                "intern",
                                                                null,
                                                                "Ljava/lang/String;")));
                                            }
                                        }
                                        //move-result-object vx
                                        {
                                            mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction11x(Opcode.MOVE_RESULT_OBJECT, regA));
                                        }
                                        string_encrypt_total++;
                                    }
                                    break;
                                }
                            }
                            newMethod.add(new ImmutableMethod(method.getDefiningClass(),
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
                        } else
                            newMethod.add(method);
                    }
                    newMethod.add(StringUtils.Create$Method(classDef, key1, key2, key3, randon));
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
        string_encrypt_total = 0;
        diff.clear();
        if (configuration != null) {
            JsonArray separate = new JsonParser().parse(configuration).getAsJsonObject().getAsJsonArray(Long.toString(8));
            if (separate == null) return;
            for (JsonElement jsonElement : separate)
                diff.add(jsonElement.getAsString());
        }
    }

    @Override
    public String getResult() {
        return String.format(Objects.requireNonNull(SysConfigUtil.getLanguageConfigUtil(getLanguageEnums(), "string.encryption")), string_encrypt_total);
    }

    @Override
    public int priority() {
        return 100;
    }

    @Override
    public int compareTo(BaseTransformer o) {
        return priority() - o.priority();
    }
}
