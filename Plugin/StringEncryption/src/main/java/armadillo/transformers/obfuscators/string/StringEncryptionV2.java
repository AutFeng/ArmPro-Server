package armadillo.transformers.obfuscators.string;


import armadillo.transformers.base.BaseTransformer;
import armadillo.transformers.base.DexTransformer;
import armadillo.utils.SmaliUtils;
import armadillo.utils.SysConfigUtil;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.jf.dexlib2.AccessFlags;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.builder.BuilderInstruction;
import org.jf.dexlib2.builder.MutableMethodImplementation;
import org.jf.dexlib2.builder.instruction.*;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.iface.Field;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.formats.Instruction21c;
import org.jf.dexlib2.iface.reference.StringReference;
import org.jf.dexlib2.iface.value.StringEncodedValue;
import org.jf.dexlib2.immutable.*;
import org.jf.dexlib2.immutable.reference.ImmutableFieldReference;
import org.jf.dexlib2.immutable.reference.ImmutableMethodReference;
import org.jf.dexlib2.immutable.reference.ImmutableTypeReference;
import org.jf.dexlib2.rewriter.DexRewriter;
import org.jf.dexlib2.rewriter.Rewriter;
import org.jf.dexlib2.rewriter.RewriterModule;
import org.jf.dexlib2.rewriter.Rewriters;
import org.jf.dexlib2.writer.io.MemoryDataStore;
import org.jf.dexlib2.writer.pool.DexPool;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class StringEncryptionV2 extends DexTransformer {
    private int string_encrypt_total;
    public static List<Short> shorts = new ArrayList<>();
    public static List<BuilderInstruction> clinit = new ArrayList<>();
    public static final String decSmali = ".class public Lcom/xxx;\n" +
            ".super Ljava/lang/Object;\n" +
            ".method private static $(III)Ljava/lang/String;\n" +
            "    .registers 7\n" +
            "    sub-int v2, p1, p0\n" +
            "    new-array v0, v2, [C\n" +
            "    const/4 v1, 0x0\n" +
            "    :goto_5\n" +
            "    sub-int v2, p1, p0\n" +
            "    if-ge v1, v2, :cond_16\n" +
            "    sget-object v2, {$class}->{$name}:[S\n" +
            "    add-int v3, p0, v1\n" +
            "    aget-short v2, v2, v3\n" +
            "    xor-int/2addr v2, p2\n" +
            "    int-to-char v2, v2\n" +
            "    aput-char v2, v0, v1\n" +
            "    add-int/lit8 v1, v1, 0x1\n" +
            "    goto :goto_5\n" +
            "    :cond_16\n" +
            "    new-instance v2, Ljava/lang/String;\n" +
            "    invoke-direct {v2, v0}, Ljava/lang/String;-><init>([C)V\n" +
            "    return-object v2\n" +
            ".end method";

    @Override
    public int priority() {
        return 0;
    }

    @Nonnull
    @Override
    public Rewriter<ClassDef> getClassDefRewriter(@Nonnull Rewriters rewriters) {
        return classDef -> {
            shorts.clear();
            clinit.clear();
            List<Method> methods = new ArrayList<>();
            for (Method method : classDef.getMethods()) {
                if (method.getImplementation() == null)
                    methods.add(method);
                else
                    methods.add(getMethodRewriter(rewriters).rewrite(method));
            }
            List<Field> fields = new ArrayList<>();
            for (Field field : classDef.getFields()) {
                if (AccessFlags.FINAL.isSet(field.getAccessFlags()) && field.getType().equals("Ljava/lang/String;"))
                    fields.add(getFieldRewriter(rewriters).rewrite(field));
                else
                    fields.add(field);
            }
            if (shorts.size() > 0) {
                AtomicBoolean isInit = new AtomicBoolean(false);
                Iterator<Method> iterator = methods.iterator();
                while (iterator.hasNext()) {
                    Method method = iterator.next();
                    if ("<clinit>".equals(method.getName())) {
                        isInit.set(true);
                        MutableMethodImplementation implementation = new MutableMethodImplementation(method.getImplementation());
                        implementation.addInstruction(0, new BuilderInstruction31i(
                                Opcode.CONST,
                                0,
                                shorts.size()
                        ));
                        implementation.addInstruction(1, new BuilderInstruction22c(
                                Opcode.NEW_ARRAY,
                                0,
                                0,
                                new ImmutableTypeReference("[S")
                        ));
                        implementation.addInstruction(new BuilderArrayPayload(
                                2,
                                new ArrayList<>(shorts)
                        ));
                        implementation.addInstruction(2, new BuilderInstruction31t(
                                Opcode.FILL_ARRAY_DATA,
                                0,
                                implementation.newLabelForIndex(implementation.getInstructions().size() - 1)));
                        implementation.addInstruction(3, new BuilderInstruction21c(
                                Opcode.SPUT_OBJECT,
                                0,
                                new ImmutableFieldReference(
                                        classDef.getType(),
                                        "$",
                                        "[S")));
                        for (int i = 0; i < clinit.size(); i++) {
                            implementation.addInstruction(4 + i, clinit.get(i));
                        }
                        iterator.remove();
                        methods.add(new ImmutableMethod(
                                method.getDefiningClass(),
                                method.getName(),
                                method.getParameters(),
                                method.getReturnType(),
                                method.getAccessFlags(),
                                method.getAnnotations(),
                                method.getHiddenApiRestrictions(),
                                new ImmutableMethodImplementation(
                                        MethodHelper.fixReg(implementation, method, 3),
                                        implementation.getInstructions(),
                                        implementation.getTryBlocks(),
                                        implementation.getDebugItems())));
                        break;
                    }
                }
                if (!isInit.get()) {
                    MutableMethodImplementation mutableMethodImplementation = new MutableMethodImplementation(1);
                    mutableMethodImplementation.addInstruction(new BuilderInstruction10x(Opcode.NOP));
                    mutableMethodImplementation.addInstruction(new BuilderInstruction10x(Opcode.RETURN_VOID));
                    mutableMethodImplementation.addInstruction(0, new BuilderInstruction31i(Opcode.CONST, 0, shorts.size()));
                    mutableMethodImplementation.addInstruction(1, new BuilderInstruction22c(Opcode.NEW_ARRAY, 0, 0, new ImmutableTypeReference("[S")));
                    mutableMethodImplementation.addInstruction(new BuilderArrayPayload(2, new ArrayList<>(shorts)));
                    mutableMethodImplementation.addInstruction(2, new BuilderInstruction31t(Opcode.FILL_ARRAY_DATA, 0, mutableMethodImplementation.newLabelForIndex(mutableMethodImplementation.getInstructions().size() - 1)));
                    mutableMethodImplementation.addInstruction(3, new BuilderInstruction21c(Opcode.SPUT_OBJECT, 0, new ImmutableFieldReference(classDef.getType(), "$", "[S")));
                    for (int i = 0; i < clinit.size(); i++) {
                        mutableMethodImplementation.addInstruction(4 + i, clinit.get(i));
                    }
                    methods.add(new ImmutableMethod(
                            classDef.getType(),
                            "<clinit>",
                            null,
                            "V",
                            AccessFlags.STATIC.getValue() | AccessFlags.CONSTRUCTOR.getValue(),
                            null,
                            null,
                            new ImmutableMethodImplementation(3, mutableMethodImplementation.getInstructions(), null, null)));
                }
                fields.add(new ImmutableField(
                        classDef.getType(),
                        "$",
                        "[S",
                        AccessFlags.PRIVATE.getValue() | AccessFlags.STATIC.getValue(),
                        null,
                        null,
                        null));
                try {
                    MutableMethodImplementation implementation = new MutableMethodImplementation(SmaliUtils.assembleSmali(decSmali
                            .replace("{$class}", classDef.getType())
                            .replace("{$name}", "$")
                            .getBytes()).getMethods().iterator().next().getImplementation());
                    methods.add(new ImmutableMethod(
                            classDef.getType(),
                            "$",
                            Lists.newArrayList(
                                    new ImmutableMethodParameter("I", null, null),
                                    new ImmutableMethodParameter("I", null, null),
                                    new ImmutableMethodParameter("I", null, null)),
                            "Ljava/lang/String;",
                            AccessFlags.PRIVATE.getValue() | AccessFlags.STATIC.getValue(),
                            null,
                            null,
                            new ImmutableMethodImplementation(
                                    implementation.getRegisterCount(),
                                    implementation.getInstructions(),
                                    implementation.getTryBlocks(),
                                    implementation.getDebugItems()
                            )
                    ));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return new ImmutableClassDef(
                        classDef.getType(),
                        classDef.getAccessFlags(),
                        classDef.getSuperclass(),
                        classDef.getInterfaces(),
                        classDef.getSourceFile(),
                        classDef.getAnnotations(),
                        fields,
                        methods
                );
            } else
                return classDef;
        };
    }

    @Nonnull
    @Override
    public Rewriter<Method> getMethodRewriter(@Nonnull Rewriters rewriters) {
        return method -> {
            if (method.getImplementation() != null) {
                MutableMethodImplementation implementation = new MutableMethodImplementation(method.getImplementation());
                int reg = implementation.getRegisterCount();
                for (int i = 0; i < implementation.getInstructions().size(); i++) {
                    Instruction instruction = implementation.getInstructions().get(i);
                    switch (instruction.getOpcode()) {
                        case CONST_STRING:
                            Instruction21c instruction21c = (Instruction21c) instruction;
                            StringReference stringReference = (StringReference) instruction21c.getReference();
                            if (stringReference.getString().isEmpty())
                                continue;
                            StringData stringData = enc(stringReference.getString(), (short) (method.getName().hashCode() ^ new Random().nextInt(9999)));
                            implementation.replaceInstruction(i, new BuilderInstruction31i(
                                    Opcode.CONST,
                                    reg + 1,
                                    stringData.start
                            ));
                            implementation.addInstruction(i + 1, new BuilderInstruction31i(
                                    Opcode.CONST,
                                    reg + 2,
                                    stringData.end
                            ));
                            implementation.addInstruction(i + 2, new BuilderInstruction31i(
                                    Opcode.CONST,
                                    reg + 3,
                                    stringData.key
                            ));
                            implementation.addInstruction(i + 3, new BuilderInstruction3rc(
                                    Opcode.INVOKE_STATIC_RANGE,
                                    reg + 1,
                                    3,
                                    new ImmutableMethodReference(
                                            method.getDefiningClass(),
                                            "$",
                                            Lists.newArrayList("I", "I", "I"),
                                            "Ljava/lang/String;"
                                    )));
                            implementation.addInstruction(i + 4, new BuilderInstruction11x(
                                    Opcode.MOVE_RESULT_OBJECT,
                                    instruction21c.getRegisterA()
                            ));
                            string_encrypt_total++;
                            break;
                    }
                }
                return new ImmutableMethod(
                        method.getDefiningClass(),
                        method.getName(),
                        method.getParameters(),
                        method.getReturnType(),
                        method.getAccessFlags(),
                        method.getAnnotations(),
                        method.getHiddenApiRestrictions(),
                        new ImmutableMethodImplementation(
                                MethodHelper.fixReg(implementation, method, 4),
                                implementation.getInstructions(),
                                implementation.getTryBlocks(),
                                implementation.getDebugItems()
                        ));
            }
            return method;
        };
    }

    @Nonnull
    @Override
    public Rewriter<Field> getFieldRewriter(@Nonnull Rewriters rewriters) {
        return field -> {
            if (field.getInitialValue() instanceof StringEncodedValue) {
                StringEncodedValue stringEncodedValue = (StringEncodedValue) field.getInitialValue();
                if (!stringEncodedValue.getValue().isEmpty()) {
                    int accessFlag = 0;
                    ArrayList<AccessFlags> accessFlags = Lists.newArrayList(AccessFlags.getAccessFlagsForField(field.getAccessFlags()));
                    accessFlags.remove(AccessFlags.FINAL);
                    for (AccessFlags flag : accessFlags) {
                        accessFlag = accessFlag | flag.getValue();
                    }
                    StringData stringData = enc(stringEncodedValue.getValue(), (short) (field.getName().hashCode() ^ new Random().nextInt(9999)));
                    clinit.add(new BuilderInstruction31i(
                            Opcode.CONST,
                            0,
                            stringData.start
                    ));
                    clinit.add(new BuilderInstruction31i(
                            Opcode.CONST,
                            1,
                            stringData.end
                    ));
                    clinit.add(new BuilderInstruction31i(
                            Opcode.CONST,
                            2,
                            stringData.key
                    ));
                    clinit.add(new BuilderInstruction3rc(
                            Opcode.INVOKE_STATIC_RANGE,
                            0,
                            3,
                            new ImmutableMethodReference(
                                    field.getDefiningClass(),
                                    "$",
                                    Lists.newArrayList("I", "I", "I"),
                                    "Ljava/lang/String;"
                            )));
                    clinit.add(new BuilderInstruction11x(
                            Opcode.MOVE_RESULT_OBJECT,
                            0
                    ));
                    clinit.add(new BuilderInstruction21c(
                            Opcode.SPUT_OBJECT,
                            0,
                            new ImmutableFieldReference(
                                    field.getDefiningClass(),
                                    field.getName(),
                                    field.getType())));
                    string_encrypt_total++;
                    return new ImmutableField(
                            field.getDefiningClass(),
                            field.getName(),
                            field.getType(),
                            accessFlag,
                            null,
                            field.getAnnotations(),
                            field.getHiddenApiRestrictions()
                    );
                }
            }
            return field;
        };
    }

    @Override
    public void transform() throws Exception {
        string_encrypt_total = 0;
    }

    @Override
    public String getResult() {
        return String.format(Objects.requireNonNull(SysConfigUtil.getLanguageConfigUtil(getLanguageEnums(), "string.encryption")), string_encrypt_total);
    }


    public static StringData enc(String s, int key) {
        char[] chars = s.toCharArray();
        for (char c : chars) {
            short buff = (short) (c ^ key);
            shorts.add(buff);
        }
        int start = shorts.size() - chars.length;
        int end = start + chars.length;
        return new StringData(start, end, key);
    }

    @Override
    public int compareTo(BaseTransformer o) {
        return 0;
    }

    public static class StringData {
        int start;
        int end;
        int key;

        public StringData(int start, int end, int key) {
            this.start = start;
            this.end = end;
            this.key = key;
        }

        public int getStart() {
            return start;
        }

        public void setStart(int start) {
            this.start = start;
        }

        public int getEnd() {
            return end;
        }

        public void setEnd(int end) {
            this.end = end;
        }

        public int getKey() {
            return key;
        }

        public void setKey(int key) {
            this.key = key;
        }
    }
}
