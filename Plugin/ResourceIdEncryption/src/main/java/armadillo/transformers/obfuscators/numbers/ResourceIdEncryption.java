package armadillo.transformers.obfuscators.numbers;

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
import org.jf.dexlib2.builder.instruction.BuilderInstruction31i;
import org.jf.dexlib2.builder.instruction.BuilderInstruction35c;
import org.jf.dexlib2.builder.instruction.BuilderInstruction3rc;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.formats.Instruction21ih;
import org.jf.dexlib2.iface.instruction.formats.Instruction31i;
import org.jf.dexlib2.immutable.ImmutableClassDef;
import org.jf.dexlib2.immutable.ImmutableMethod;
import org.jf.dexlib2.immutable.ImmutableMethodImplementation;
import org.jf.dexlib2.immutable.reference.ImmutableMethodProtoReference;
import org.jf.dexlib2.immutable.reference.ImmutableMethodReference;
import org.jf.dexlib2.rewriter.Rewriter;
import org.jf.dexlib2.rewriter.Rewriters;
import org.jf.util.NumberUtils;

import javax.annotation.Nonnull;
import java.util.*;

public class ResourceIdEncryption extends DexTransformer {
    private int numbers_encrypt_total;
    private final HashSet<String> diff = new HashSet<>();
    @Nonnull
    @Override
    public Rewriter<ClassDef> getClassDefRewriter(@Nonnull Rewriters rewriters) {
        return new Rewriter<ClassDef>() {
            @Nonnull
            @Override
            public ClassDef rewrite(@Nonnull ClassDef classDef) {
                if (diff.contains(classDef.getType())){
                    String randon = nameFactory.nextName();
                    int key1 = classDef.getType().hashCode()
                            + new Random().nextInt(99999)
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
                                    case CONST_HIGH16:
                                    case CONST: {
                                        int var = 0;
                                        int regA = 0;
                                        if (instruction.getOpcode() == Opcode.CONST) {
                                            Instruction31i instruction31i = (Instruction31i) instruction;
                                            var = instruction31i.getNarrowLiteral();
                                            regA = instruction31i.getRegisterA();
                                        } else if (instruction.getOpcode() == Opcode.CONST_HIGH16) {
                                            Instruction21ih instruction21ih = (Instruction21ih) instruction;
                                            var = instruction21ih.getNarrowLiteral();
                                            regA = instruction21ih.getRegisterA();
                                        }
                                        if (var >> 24 != 0x7f
                                                || NumberUtils.isLikelyFloat(var)
                                                || NumberUtils.isLikelyDouble(var))
                                            break;
                                        //const/high16 vx,"xxxxxx"
                                        {
                                            mutableImplementation.replaceInstruction(i, new BuilderInstruction31i(Opcode.CONST, regA, NumbersUtils.encrypt(var, key3)));
                                        }
                                        //invoke-static{vx} Lxxxx/xxxx;->$(I)I;
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
                                                                Lists.newArrayList("I"),
                                                                "I")));
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
                                                                Lists.newArrayList("I"),
                                                                "I")));
                                            }
                                        }
                                        //move-result-object vx
                                        {
                                            mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction11x(Opcode.MOVE_RESULT, regA));
                                        }
                                        numbers_encrypt_total++;
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
                                            method.getImplementation().getRegisterCount(),
                                            mutableImplementation.getInstructions(),
                                            mutableImplementation.getTryBlocks(),
                                            mutableImplementation.getDebugItems())));
                        } else
                            newMethod.add(method);
                    }
                    newMethod.add(NumbersUtils.Create$Method_Num(classDef, key3, randon));
                    return new ImmutableClassDef(
                            classDef.getType(),
                            classDef.getAccessFlags(),
                            classDef.getSuperclass(),
                            classDef.getInterfaces(),
                            classDef.getSourceFile(),
                            classDef.getAnnotations(),
                            classDef.getFields(),
                            newMethod);
                }else
                    return classDef;
            }
        };
    }

    @Override
    public void transform() throws Exception {
        numbers_encrypt_total = 0;
        diff.clear();
        if (configuration != null) {
            JsonArray separate = new JsonParser().parse(configuration).getAsJsonObject().getAsJsonArray(Long.toString(32));
            if (separate == null) return;
            for (JsonElement jsonElement : separate)
                diff.add(jsonElement.getAsString());
        }
    }

    @Override
    public String getResult() {
        return String.format(Objects.requireNonNull(SysConfigUtil.getLanguageConfigUtil(getLanguageEnums(), "numbers.encryption")), numbers_encrypt_total);
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
