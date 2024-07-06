package armadillo.transformers.obfuscators.separate;

import armadillo.transformers.base.BaseTransformer;
import armadillo.transformers.base.DexTransformer;
import armadillo.utils.StringRandom;
import armadillo.utils.SysConfigUtil;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.jf.dexlib2.AccessFlags;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.analysis.ClassPath;
import org.jf.dexlib2.analysis.DexClassProvider;
import org.jf.dexlib2.analysis.reflection.util.ReflectionUtils;
import org.jf.dexlib2.builder.BuilderInstruction;
import org.jf.dexlib2.builder.MutableMethodImplementation;
import org.jf.dexlib2.builder.instruction.*;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.Field;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.MethodParameter;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.formats.*;
import org.jf.dexlib2.iface.reference.FieldReference;
import org.jf.dexlib2.iface.reference.MethodReference;
import org.jf.dexlib2.iface.reference.StringReference;
import org.jf.dexlib2.iface.reference.TypeReference;
import org.jf.dexlib2.immutable.*;
import org.jf.dexlib2.immutable.instruction.*;
import org.jf.dexlib2.immutable.reference.*;
import org.jf.dexlib2.rewriter.Rewriter;
import org.jf.dexlib2.rewriter.Rewriters;

import javax.annotation.Nonnull;
import java.util.*;

public class CallMethodSeparate extends DexTransformer {
    private int separate_total;
    private final HashSet<String> diff = new HashSet<>();
    private final HashSet<ClassDef> classDefs = new HashSet<>();

    @Nonnull
    @Override
    public Rewriter<ClassDef> getClassDefRewriter(@Nonnull Rewriters rewriters) {
        return new Rewriter<ClassDef>() {
            @Nonnull
            @Override
            public ClassDef rewrite(@Nonnull ClassDef classDef) {
                if (diff.contains(classDef.getType())) {
                    String newClassDefName = ReflectionUtils.javaToDexName(ReflectionUtils.dexToJavaNameParent(classDef.getType()) + "." + nameFactory.nextName());
                    List<Method> methods = new ArrayList<>();
                    List<Method> newmethods = new ArrayList<>();
                    for (Method method : classDef.getMethods()) {
                        if (method.getImplementation() != null) {
                            MutableMethodImplementation mutableMethodImplementation = new MutableMethodImplementation(method.getImplementation());
                            for (int i = 0; i < mutableMethodImplementation.getInstructions().size(); i++) {
                                Instruction instruction = mutableMethodImplementation.getInstructions().get(i);
                                switch (instruction.getOpcode()) {
                                    case INVOKE_DIRECT:
                                    case INVOKE_VIRTUAL: {
                                        Instruction35c instruction35c = (Instruction35c) instruction;
                                        MethodReference methodReference = (MethodReference) instruction35c.getReference();
                                        if (instruction.getOpcode() == Opcode.INVOKE_DIRECT && methodReference.getName().equals("<init>"))
                                            break;
                                        if (instruction35c.getRegisterCount() < 5
                                                && !methodReference.getParameterTypes().contains("J")
                                                && !methodReference.getParameterTypes().contains("D")) {
                                            String name = nameFactory.nextName();
                                            methods.add(
                                                    createNewVirtualMethod(
                                                            instruction.getOpcode(),
                                                            name,
                                                            classDef.getType(),
                                                            methodReference.getDefiningClass(),
                                                            methodReference.getName(),
                                                            methodReference.getParameterTypes(),
                                                            methodReference.getReturnType()));
                                            List<String> Parameters = Lists.newArrayList(methodReference.getDefiningClass());
                                            for (CharSequence parameterType : methodReference.getParameterTypes())
                                                Parameters.add((String) parameterType);
                                            mutableMethodImplementation.replaceInstruction(i, new BuilderInstruction35c(
                                                    Opcode.INVOKE_STATIC,
                                                    instruction35c.getRegisterCount(),
                                                    instruction35c.getRegisterC(),
                                                    instruction35c.getRegisterD(),
                                                    instruction35c.getRegisterE(),
                                                    instruction35c.getRegisterF(),
                                                    instruction35c.getRegisterG(),
                                                    new ImmutableMethodReference(
                                                            classDef.getType(),
                                                            name,
                                                            Parameters,
                                                            methodReference.getReturnType())));
                                        }
                                    }
                                    break;
                                    case INVOKE_STATIC: {
                                        Instruction35c instruction35c = (Instruction35c) instruction;
                                        MethodReference methodReference = (MethodReference) instruction35c.getReference();
                                        if (!methodReference.getParameterTypes().contains("J")
                                                && !methodReference.getParameterTypes().contains("D")) {
                                            String name = nameFactory.nextName();
                                            methods.add(
                                                    createNewStaticMethod(
                                                            name,
                                                            classDef.getType(),
                                                            methodReference.getDefiningClass(),
                                                            methodReference.getName(),
                                                            methodReference.getParameterTypes(),
                                                            methodReference.getReturnType()));
                                            mutableMethodImplementation.replaceInstruction(i, new BuilderInstruction35c(
                                                    Opcode.INVOKE_STATIC,
                                                    instruction35c.getRegisterCount(),
                                                    instruction35c.getRegisterC(),
                                                    instruction35c.getRegisterD(),
                                                    instruction35c.getRegisterE(),
                                                    instruction35c.getRegisterF(),
                                                    instruction35c.getRegisterG(),
                                                    new ImmutableMethodReference(
                                                            classDef.getType(),
                                                            name,
                                                            methodReference.getParameterTypes(),
                                                            methodReference.getReturnType())));


//                                            ClassPath classPath = new ClassPath(Lists.newArrayList(new DexClassProvider(getDexBackedDexFile())));
//                                            ClassDef pathClassDef = classPath.getClassDefOrNull(methodReference.getDefiningClass());
//                                            /**
//                                             * 用户函数
//                                             */
//                                            if (pathClassDef != null
//                                                    && isPrivate(pathClassDef, methodReference.getName(), Lists.newArrayList(methodReference.getParameterTypes()), methodReference.getReturnType())) {
//                                                String name = nameFactory.nextName();
//                                                methods.add(
//                                                        createNewStaticMethod(
//                                                                name,
//                                                                classDef.getType(),
//                                                                methodReference.getDefiningClass(),
//                                                                methodReference.getName(),
//                                                                methodReference.getParameterTypes(),
//                                                                methodReference.getReturnType()));
//                                                mutableMethodImplementation.replaceInstruction(i, new BuilderInstruction35c(
//                                                        Opcode.INVOKE_STATIC,
//                                                        instruction35c.getRegisterCount(),
//                                                        instruction35c.getRegisterC(),
//                                                        instruction35c.getRegisterD(),
//                                                        instruction35c.getRegisterE(),
//                                                        instruction35c.getRegisterF(),
//                                                        instruction35c.getRegisterG(),
//                                                        new ImmutableMethodReference(
//                                                                classDef.getType(),
//                                                                name,
//                                                                methodReference.getParameterTypes(),
//                                                                methodReference.getReturnType())));
//                                            }
//                                            /**
//                                             * 其他方法
//                                             */
//                                            else {
//                                                String name = nameFactory.nextName();
//                                                newmethods.add(
//                                                        createNewStaticMethod(
//                                                                name,
//                                                                newClassDefName,
//                                                                methodReference.getDefiningClass(),
//                                                                methodReference.getName(),
//                                                                methodReference.getParameterTypes(),
//                                                                methodReference.getReturnType()));
//                                                mutableMethodImplementation.replaceInstruction(i, new BuilderInstruction35c(
//                                                        Opcode.INVOKE_STATIC,
//                                                        instruction35c.getRegisterCount(),
//                                                        instruction35c.getRegisterC(),
//                                                        instruction35c.getRegisterD(),
//                                                        instruction35c.getRegisterE(),
//                                                        instruction35c.getRegisterF(),
//                                                        instruction35c.getRegisterG(),
//                                                        new ImmutableMethodReference(
//                                                                newClassDefName,
//                                                                name,
//                                                                methodReference.getParameterTypes(),
//                                                                methodReference.getReturnType())));
//                                            }
                                        }
                                    }
                                    break;
                                    case INVOKE_STATIC_RANGE: {
                                        Instruction3rc instruction3rc = (Instruction3rc) instruction;
                                        MethodReference methodReference = (MethodReference) instruction3rc.getReference();
                                        if (!methodReference.getParameterTypes().contains("J")
                                                && !methodReference.getParameterTypes().contains("D")) {
                                            String name = nameFactory.nextName();
                                            methods.add(
                                                    createNewStaticRangeMethod(
                                                            name,
                                                            classDef.getType(),
                                                            methodReference.getDefiningClass(),
                                                            methodReference.getName(),
                                                            methodReference.getParameterTypes(),
                                                            methodReference.getReturnType()));
                                            mutableMethodImplementation.replaceInstruction(i, new BuilderInstruction3rc(
                                                    Opcode.INVOKE_STATIC_RANGE,
                                                    instruction3rc.getStartRegister(),
                                                    instruction3rc.getRegisterCount(),
                                                    new ImmutableMethodReference(
                                                            classDef.getType(),
                                                            name,
                                                            methodReference.getParameterTypes(),
                                                            methodReference.getReturnType())));


//                                            ClassPath classPath = new ClassPath(Lists.newArrayList(new DexClassProvider(getDexBackedDexFile())));
//                                            ClassDef pathClassDef = classPath.getClassDefOrNull(methodReference.getDefiningClass());
//                                            /**
//                                             * 用户函数
//                                             */
//                                            if (pathClassDef != null
//                                                    && isPrivate(pathClassDef, methodReference.getName(), Lists.newArrayList(methodReference.getParameterTypes()), methodReference.getReturnType())) {
//                                                String name = nameFactory.nextName();
//                                                methods.add(
//                                                        createNewStaticRangeMethod(
//                                                                name,
//                                                                classDef.getType(),
//                                                                methodReference.getDefiningClass(),
//                                                                methodReference.getName(),
//                                                                methodReference.getParameterTypes(),
//                                                                methodReference.getReturnType()));
//                                                mutableMethodImplementation.replaceInstruction(i, new BuilderInstruction3rc(
//                                                        Opcode.INVOKE_STATIC_RANGE,
//                                                        instruction3rc.getStartRegister(),
//                                                        instruction3rc.getRegisterCount(),
//                                                        new ImmutableMethodReference(
//                                                                classDef.getType(),
//                                                                name,
//                                                                methodReference.getParameterTypes(),
//                                                                methodReference.getReturnType())));
//
//                                            }
//                                            /**
//                                             * 其他方法
//                                             */
//                                            else {
//                                                String name = nameFactory.nextName();
//                                                newmethods.add(
//                                                        createNewStaticRangeMethod(
//                                                                name,
//                                                                newClassDefName,
//                                                                methodReference.getDefiningClass(),
//                                                                methodReference.getName(),
//                                                                methodReference.getParameterTypes(),
//                                                                methodReference.getReturnType()));
//                                                mutableMethodImplementation.replaceInstruction(i, new BuilderInstruction3rc(
//                                                        Opcode.INVOKE_STATIC_RANGE,
//                                                        instruction3rc.getStartRegister(),
//                                                        instruction3rc.getRegisterCount(),
//                                                        new ImmutableMethodReference(
//                                                                newClassDefName,
//                                                                name,
//                                                                methodReference.getParameterTypes(),
//                                                                methodReference.getReturnType())));
//                                            }
                                        }
                                    }
                                    break;
                                    case CONST_STRING_JUMBO:
                                    case CONST_STRING: {
                                        String name = nameFactory.nextName();
                                        StringReference stringReference = null;
                                        int reg = 0;
                                        switch (instruction.getOpcode()) {
                                            case CONST_STRING:
                                                Instruction21c instruction21c = (Instruction21c) instruction;
                                                stringReference = (StringReference) instruction21c.getReference();
                                                reg = instruction21c.getRegisterA();
                                                break;
                                            case CONST_STRING_JUMBO:
                                                Instruction31c instruction31c = (Instruction31c) instruction;
                                                stringReference = (StringReference) instruction31c.getReference();
                                                reg = instruction31c.getRegisterA();
                                                break;
                                        }
                                        newmethods.add(createNewStringMethod(instruction.getOpcode(), name, newClassDefName, stringReference));
                                        mutableMethodImplementation.replaceInstruction(i, new BuilderInstruction35c(
                                                Opcode.INVOKE_STATIC,
                                                0,
                                                0,
                                                0,
                                                0,
                                                0,
                                                0,
                                                new ImmutableMethodReference(
                                                        newClassDefName,
                                                        name,
                                                        null,
                                                        "Ljava/lang/String;")));
                                        mutableMethodImplementation.addInstruction(i++ + 1, new BuilderInstruction11x(Opcode.MOVE_RESULT_OBJECT, reg));
                                    }
                                    break;
                                    case IGET_OBJECT: {
                                        Instruction22c instruction22c = (Instruction22c) instruction;
                                        FieldReference fieldReference = (FieldReference) instruction22c.getReference();
                                        String name = nameFactory.nextName();
                                        methods.add(
                                                createNewPublicGetMethod(
                                                        name,
                                                        classDef.getType(),
                                                        fieldReference.getDefiningClass(),
                                                        fieldReference.getName(),
                                                        Lists.newArrayList(fieldReference.getType(), fieldReference.getDefiningClass()),
                                                        fieldReference.getType()));
                                        mutableMethodImplementation.replaceInstruction(i, new BuilderInstruction35c(
                                                Opcode.INVOKE_STATIC,
                                                1,
                                                instruction22c.getRegisterB(),
                                                0,
                                                0,
                                                0,
                                                0,
                                                new ImmutableMethodReference(
                                                        classDef.getType(),
                                                        name,
                                                        Lists.newArrayList(fieldReference.getDefiningClass()),
                                                        fieldReference.getType())));
                                        mutableMethodImplementation.addInstruction(i++ + 1, new BuilderInstruction11x(Opcode.MOVE_RESULT_OBJECT, instruction22c.getRegisterA()));
                                    }
                                    break;
                                    case IPUT_OBJECT: {
                                        if (!method.getName().equals("<init>")) {
                                            Instruction22c instruction22c = (Instruction22c) instruction;
                                            FieldReference fieldReference = (FieldReference) instruction22c.getReference();
                                            String name = nameFactory.nextName();
                                            methods.add(
                                                    createNewPublicPutMethod(
                                                            name,
                                                            classDef.getType(),
                                                            fieldReference.getDefiningClass(),
                                                            fieldReference.getName(),
                                                            Lists.newArrayList(fieldReference.getType(), fieldReference.getDefiningClass()),
                                                            "V"));
                                            mutableMethodImplementation.replaceInstruction(i, new BuilderInstruction35c(
                                                    Opcode.INVOKE_STATIC,
                                                    2,
                                                    instruction22c.getRegisterA(),
                                                    instruction22c.getRegisterB(),
                                                    0,
                                                    0,
                                                    0,
                                                    new ImmutableMethodReference(
                                                            classDef.getType(),
                                                            name,
                                                            Lists.newArrayList(fieldReference.getType(), fieldReference.getDefiningClass()),
                                                            "V")));
                                        }
                                    }
                                    break;
                                    case NEW_INSTANCE: {
                                        BuilderInstruction lastinstruction = mutableMethodImplementation.getInstructions().get(i + 1);
                                        if (lastinstruction.getOpcode() == Opcode.INVOKE_DIRECT) {
                                            Instruction35c lastinstruction35c = (Instruction35c) lastinstruction;
                                            if (!((MethodReference) lastinstruction35c.getReference()).getName().equals("<init>"))
                                                break;
                                            Instruction21c instruction21c = (Instruction21c) instruction;
                                            Instruction35c instruction35c = (Instruction35c) mutableMethodImplementation.getInstructions().get(i + 1);
                                            MethodReference methodReference = (MethodReference) instruction35c.getReference();
                                            if (!methodReference.getParameterTypes().contains("J")
                                                    && !methodReference.getParameterTypes().contains("D")) {
                                                String name = nameFactory.nextName();
                                                methods.add(
                                                        createNewInitMethod(
                                                                name,
                                                                classDef.getType(),
                                                                methodReference.getDefiningClass(),
                                                                methodReference.getName(),
                                                                methodReference.getParameterTypes(),
                                                                methodReference.getReturnType()));
                                                mutableMethodImplementation.replaceInstruction(i, new BuilderInstruction35c(
                                                        Opcode.INVOKE_STATIC,
                                                        instruction35c.getRegisterCount() - 1,
                                                        instruction35c.getRegisterD(),
                                                        instruction35c.getRegisterE(),
                                                        instruction35c.getRegisterF(),
                                                        instruction35c.getRegisterG(),
                                                        0,
                                                        new ImmutableMethodReference(
                                                                classDef.getType(),
                                                                name,
                                                                methodReference.getParameterTypes(),
                                                                ((TypeReference) instruction21c.getReference()).getType())));
                                                mutableMethodImplementation.replaceInstruction(i + 1, new BuilderInstruction11x(Opcode.MOVE_RESULT_OBJECT, instruction21c.getRegisterA()));
                                            }
                                        }
                                    }
                                    break;
                                }
                            }
                            methods.add(new ImmutableMethod(
                                    method.getDefiningClass(),
                                    method.getName(),
                                    method.getParameters(),
                                    method.getReturnType(),
                                    method.getAccessFlags(),
                                    method.getAnnotations(),
                                    method.getHiddenApiRestrictions(),
                                    mutableMethodImplementation));
                        } else
                            methods.add(method);
                    }
                    if (newmethods.size() > 0) {
                        /**
                         * 创建<init>
                         */
                        {
                            List<Instruction> newInsts = new ArrayList<>();
                            newInsts.add(new ImmutableInstruction35c(
                                    Opcode.INVOKE_DIRECT,
                                    1,
                                    0,
                                    0,
                                    0,
                                    0,
                                    0,
                                    new ImmutableMethodReference(
                                            "Ljava/lang/Object;",
                                            "<init>",
                                            null,
                                            "V")));
                            newInsts.add(new ImmutableInstruction10x(Opcode.RETURN_VOID));
                            newmethods.add(new ImmutableMethod(
                                    newClassDefName,
                                    "<init>",
                                    null,
                                    "V",
                                    AccessFlags.PUBLIC.getValue() | AccessFlags.CONSTRUCTOR.getValue(),
                                    null,
                                    null,
                                    new ImmutableMethodImplementation(1, newInsts, null, null)));
                        }
                        /**
                         * 创建新的ClassDef
                         */
                        {
                            ClassDef newClassDef = new ImmutableClassDef(
                                    newClassDefName,
                                    AccessFlags.PUBLIC.getValue(),
                                    "Ljava/lang/Object;",
                                    null,
                                    StringRandom.RandomString(),
                                    null,
                                    null,
                                    newmethods);
                            classDefs.add(newClassDef);
                        }
                    }
                    separate_total++;
                    return new ImmutableClassDef(
                            classDef.getType(),
                            classDef.getAccessFlags(),
                            classDef.getSuperclass(),
                            classDef.getInterfaces(),
                            classDef.getSourceFile(),
                            classDef.getAnnotations(),
                            classDef.getFields(),
                            methods);
                } else
                    return classDef;
            }
        };
    }

    private boolean isPrivate(ClassDef pathClassDef, String name, List<CharSequence> Parameters, String returnType) {
        for (Method method : pathClassDef.getMethods()) {
            if (method.getName().equals(name)
                    && equalsParameters(Lists.newArrayList(method.getParameterTypes()), Parameters)
                    && method.getReturnType().equals(returnType)
                    && AccessFlags.PRIVATE.isSet(method.getAccessFlags()))
                return true;
        }
        return false;
    }

    private boolean equalsParameters(List<CharSequence> parameterTypes, List<CharSequence> parameters) {
        if (parameterTypes.size() != parameters.size())
            return false;
        else {
            for (CharSequence parameterType : parameterTypes) {
                if (!parameters.contains(parameterType))
                    return false;
            }
            return true;
        }
    }

    @Override
    public void transform() throws Exception {
        separate_total = 0;
        diff.clear();
        if (configuration != null) {
            JsonArray separate = new JsonParser().parse(configuration).getAsJsonObject().getAsJsonArray(Long.toString(262144));
            if (separate == null) return;
            for (JsonElement jsonElement : separate)
                diff.add(jsonElement.getAsString());
        }
    }

    @Override
    public String getResult() {
        return String.format(Objects.requireNonNull(SysConfigUtil.getLanguageConfigUtil(getLanguageEnums(), "method.separate")), separate_total);
    }

    @Override
    public int priority() {
        return 9;
    }

    @Override
    public HashSet<ClassDef> getNewClassDef() {
        return classDefs;
    }

    @Override
    public int compareTo(BaseTransformer o) {
        return priority() - o.priority();
    }

    private Method createNewVirtualMethod(Opcode opcode, String newName, String classDef, String DefiningClass, String Name, List<? extends CharSequence> Parameter, String Return) {
        MutableMethodImplementation methodImplementation = new MutableMethodImplementation(3 + Parameter.size());
        methodImplementation.addInstruction(new BuilderInstruction35c(
                opcode,
                Parameter.size() + 1,
                2,
                3,
                4,
                5,
                6,
                new ImmutableMethodReference(DefiningClass, Name, Parameter, Return)));
        switch (Return) {
            case "Z":
            case "B":
            case "S":
            case "C":
            case "I":
            case "F":
                methodImplementation.addInstruction(new BuilderInstruction11x(Opcode.MOVE_RESULT, 0));
                methodImplementation.addInstruction(new BuilderInstruction11x(Opcode.RETURN, 0));
                break;
            case "D":
            case "J":
                methodImplementation.addInstruction(new BuilderInstruction11x(Opcode.MOVE_RESULT_WIDE, 0));
                methodImplementation.addInstruction(new BuilderInstruction11x(Opcode.RETURN_WIDE, 0));
                break;
            case "V":
                methodImplementation.addInstruction(new BuilderInstruction10x(Opcode.RETURN_VOID));
                break;
            default:
                methodImplementation.addInstruction(new BuilderInstruction11x(Opcode.MOVE_RESULT_OBJECT, 0));
                methodImplementation.addInstruction(new BuilderInstruction11x(Opcode.RETURN_OBJECT, 0));
                break;
        }
        List<MethodParameter> methodParameters = Lists.newArrayList(new ImmutableMethodParameter(DefiningClass, null, null));
        for (CharSequence s : Parameter)
            methodParameters.add(new ImmutableMethodParameter((String) s, null, null));
        return new ImmutableMethod(
                classDef,
                newName,
                methodParameters,
                Return,
                AccessFlags.PUBLIC.getValue() | AccessFlags.STATIC.getValue(),
                null,
                null,
                new ImmutableMethodImplementation(
                        methodImplementation.getRegisterCount(),
                        methodImplementation.getInstructions(),
                        null,
                        null));
    }

    private Method createNewStaticMethod(String newName, String classDef, String DefiningClass, String Name, List<? extends CharSequence> Parameter, String Return) {
        MutableMethodImplementation methodImplementation = new MutableMethodImplementation(2 + Parameter.size());
        methodImplementation.addInstruction(new BuilderInstruction35c(
                Opcode.INVOKE_STATIC,
                Parameter.size(),
                2,
                3,
                4,
                5,
                6,
                new ImmutableMethodReference(DefiningClass, Name, Parameter, Return)));
        switch (Return) {
            case "Z":
            case "B":
            case "S":
            case "C":
            case "I":
            case "F":
                methodImplementation.addInstruction(new BuilderInstruction11x(Opcode.MOVE_RESULT, 0));
                methodImplementation.addInstruction(new BuilderInstruction11x(Opcode.RETURN, 0));
                break;
            case "D":
            case "J":
                methodImplementation.addInstruction(new BuilderInstruction11x(Opcode.MOVE_RESULT_WIDE, 0));
                methodImplementation.addInstruction(new BuilderInstruction11x(Opcode.RETURN_WIDE, 0));
                break;
            case "V":
                methodImplementation.addInstruction(new BuilderInstruction10x(Opcode.RETURN_VOID));
                break;
            default:
                methodImplementation.addInstruction(new BuilderInstruction11x(Opcode.MOVE_RESULT_OBJECT, 0));
                methodImplementation.addInstruction(new BuilderInstruction11x(Opcode.RETURN_OBJECT, 0));
                break;
        }
        List<MethodParameter> methodParameters = new ArrayList<>();
        for (CharSequence s : Parameter)
            methodParameters.add(new ImmutableMethodParameter((String) s, null, null));
        return new ImmutableMethod(
                classDef,
                newName,
                methodParameters,
                Return,
                AccessFlags.PUBLIC.getValue() | AccessFlags.STATIC.getValue(),
                null,
                null,
                new ImmutableMethodImplementation(
                        methodImplementation.getRegisterCount(),
                        methodImplementation.getInstructions(),
                        null,
                        null));
    }

    private Method createNewStaticRangeMethod(String newName, String classDef, String DefiningClass, String Name, List<? extends CharSequence> Parameter, String Return) {
        MutableMethodImplementation methodImplementation = new MutableMethodImplementation(2 + Parameter.size());
        methodImplementation.addInstruction(new BuilderInstruction3rc(
                Opcode.INVOKE_STATIC_RANGE,
                2,
                Parameter.size(),
                new ImmutableMethodReference(DefiningClass, Name, Parameter, Return)));
        switch (Return) {
            case "Z":
            case "B":
            case "S":
            case "C":
            case "I":
            case "F":
                methodImplementation.addInstruction(new BuilderInstruction11x(Opcode.MOVE_RESULT, 0));
                methodImplementation.addInstruction(new BuilderInstruction11x(Opcode.RETURN, 0));
                break;
            case "D":
            case "J":
                methodImplementation.addInstruction(new BuilderInstruction11x(Opcode.MOVE_RESULT_WIDE, 0));
                methodImplementation.addInstruction(new BuilderInstruction11x(Opcode.RETURN_WIDE, 0));
                break;
            case "V":
                methodImplementation.addInstruction(new BuilderInstruction10x(Opcode.RETURN_VOID));
                break;
            default:
                methodImplementation.addInstruction(new BuilderInstruction11x(Opcode.MOVE_RESULT_OBJECT, 0));
                methodImplementation.addInstruction(new BuilderInstruction11x(Opcode.RETURN_OBJECT, 0));
                break;
        }
        List<MethodParameter> methodParameters = new ArrayList<>();
        for (CharSequence s : Parameter)
            methodParameters.add(new ImmutableMethodParameter((String) s, null, null));
        return new ImmutableMethod(
                classDef,
                newName,
                methodParameters,
                Return,
                AccessFlags.PUBLIC.getValue() | AccessFlags.STATIC.getValue(),
                null,
                null,
                new ImmutableMethodImplementation(
                        methodImplementation.getRegisterCount(),
                        methodImplementation.getInstructions(),
                        null,
                        null));
    }

    private Method createNewStringMethod(Opcode opcode, String newName, String classDef, StringReference stringReference) {
        MutableMethodImplementation methodImplementation = new MutableMethodImplementation(1);
        switch (opcode) {
            case CONST_STRING_JUMBO:
                methodImplementation.addInstruction(new BuilderInstruction31c(
                        opcode,
                        0,
                        stringReference));
                break;
            case CONST_STRING:
                methodImplementation.addInstruction(new BuilderInstruction21c(
                        opcode,
                        0,
                        stringReference));
                break;
        }
        methodImplementation.addInstruction(new BuilderInstruction11x(Opcode.RETURN_OBJECT, 0));
        return new ImmutableMethod(
                classDef,
                newName,
                null,
                "Ljava/lang/String;",
                AccessFlags.PUBLIC.getValue() | AccessFlags.STATIC.getValue(),
                null,
                null,
                new ImmutableMethodImplementation(
                        methodImplementation.getRegisterCount(),
                        methodImplementation.getInstructions(),
                        null,
                        null));
    }

    private Method createNewPublicPutMethod(String newName, String classDef, String DefiningClass, String Name, List<? extends CharSequence> Parameter, String Return) {
        MutableMethodImplementation methodImplementation = new MutableMethodImplementation(2);
        methodImplementation.addInstruction(new BuilderInstruction22c(
                Opcode.IPUT_OBJECT,
                0,
                1,
                new ImmutableFieldReference(
                        DefiningClass,
                        Name,
                        (String) Parameter.get(0))));
        methodImplementation.addInstruction(new BuilderInstruction10x(Opcode.RETURN_VOID));
        List<MethodParameter> methodParameters = new ArrayList<>();
        for (CharSequence s : Parameter)
            methodParameters.add(new ImmutableMethodParameter((String) s, null, null));
        return new ImmutableMethod(
                classDef,
                newName,
                methodParameters,
                "V",
                AccessFlags.PUBLIC.getValue() | AccessFlags.STATIC.getValue(),
                null,
                null,
                new ImmutableMethodImplementation(
                        methodImplementation.getRegisterCount(),
                        methodImplementation.getInstructions(),
                        null,
                        null));
    }

    private Method createNewPublicGetMethod(String newName, String classDef, String DefiningClass, String Name, List<? extends CharSequence> Parameter, String Return) {
        MutableMethodImplementation methodImplementation = new MutableMethodImplementation(2);
        methodImplementation.addInstruction(new BuilderInstruction22c(
                Opcode.IGET_OBJECT,
                0,
                1,
                new ImmutableFieldReference(
                        DefiningClass,
                        Name,
                        (String) Parameter.get(0))));
        methodImplementation.addInstruction(new BuilderInstruction21c(
                Opcode.CHECK_CAST,
                0,
                new ImmutableTypeReference(Return)));
        methodImplementation.addInstruction(new BuilderInstruction11x(Opcode.RETURN_OBJECT, 0));
        return new ImmutableMethod(
                classDef,
                newName,
                Lists.newArrayList(new ImmutableMethodParameter(DefiningClass, null, null)),
                Return,
                AccessFlags.PUBLIC.getValue() | AccessFlags.STATIC.getValue(),
                null,
                null,
                new ImmutableMethodImplementation(
                        methodImplementation.getRegisterCount(),
                        methodImplementation.getInstructions(),
                        null,
                        null));
    }

    private Method createNewInitMethod(String newName, String classDef, String DefiningClass, String Name, List<? extends CharSequence> Parameter, String Return) {
        MutableMethodImplementation methodImplementation = new MutableMethodImplementation(1 + Parameter.size());
        methodImplementation.addInstruction(new BuilderInstruction21c(
                Opcode.NEW_INSTANCE,
                0,
                new ImmutableTypeReference(DefiningClass)));
        methodImplementation.addInstruction(new BuilderInstruction35c(
                Opcode.INVOKE_DIRECT,
                Parameter.size() + 1,
                0,
                1,
                2,
                3,
                4,
                new ImmutableMethodReference(DefiningClass, Name, Parameter, Return)));
        methodImplementation.addInstruction(new BuilderInstruction11x(Opcode.RETURN_OBJECT, 0));
        List<MethodParameter> methodParameters = new ArrayList<>();
        for (CharSequence s : Parameter)
            methodParameters.add(new ImmutableMethodParameter((String) s, null, null));
        return new ImmutableMethod(
                classDef,
                newName,
                methodParameters,
                DefiningClass,
                AccessFlags.PUBLIC.getValue() | AccessFlags.STATIC.getValue(),
                null,
                null,
                new ImmutableMethodImplementation(
                        methodImplementation.getRegisterCount(),
                        methodImplementation.getInstructions(),
                        null,
                        null));
    }

    private Method createNewPrivatePutMethod(String newName, String classDef, String DefiningClass, String Name, List<? extends CharSequence> Parameter, String Return) {
        List<Instruction> newInst = new ArrayList<>();
        int start_local = 2;
        int p0 = start_local + 1;
        int p1 = start_local + 2;
        int p2 = start_local + 3;
        int p3 = start_local + 4;
        int p4 = start_local + 5;
        /**
         * invoke-virtual {p1}, Ljava/lang/Object;->getClass()Ljava/lang/Class;
         */
        {
            newInst.add(new ImmutableInstruction35c(
                    Opcode.INVOKE_VIRTUAL,
                    1,
                    p1,
                    0,
                    0,
                    0,
                    0,
                    new ImmutableMethodReference(
                            "Ljava/lang/Object;",
                            "getClass",
                            null,
                            "Ljava/lang/Class;")));
        }
        /**
         * move-result-object v0
         */
        {
            newInst.add(new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT, 0));
        }
        /**
         * const-string v1, "获取的对象name"
         */
        {
            newInst.add(new ImmutableInstruction21c(Opcode.CONST_STRING, 1, new ImmutableStringReference(Name)));
        }
        /**
         * invoke-virtual {v0, v1}, Ljava/lang/Class;->getDeclaredField(Ljava/lang/String;)Ljava/lang/reflect/Field;
         */
        {
            newInst.add(new ImmutableInstruction35c(
                    Opcode.INVOKE_VIRTUAL,
                    2,
                    0,
                    1,
                    0,
                    0,
                    0,
                    new ImmutableMethodReference(
                            "Ljava/lang/Class;",
                            "getDeclaredField",
                            Lists.newArrayList("Ljava/lang/String;"),
                            "Ljava/lang/reflect/Field;")));
        }
        /**
         * move-result-object v1
         */
        {
            newInst.add(new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT, 1));
        }
        /**
         * const/4 v2, 0x1 true
         */
        {
            newInst.add(new ImmutableInstruction11n(Opcode.CONST_4, 2, 0x1));
        }
        /**
         * invoke-virtual {v1, v2}, Ljava/lang/reflect/Field;->setAccessible(Z)V
         */
        {
            newInst.add(new ImmutableInstruction35c(
                    Opcode.INVOKE_VIRTUAL,
                    2,
                    1,
                    2,
                    0,
                    0,
                    0,
                    new ImmutableMethodReference(
                            "Ljava/lang/reflect/Field;",
                            "setAccessible",
                            Lists.newArrayList("Z"),
                            "V")));
        }
        /**
         * invoke-virtual {v1, p1, p0}, Ljava/lang/reflect/Field;->set(Ljava/lang/Object;Ljava/lang/Object;)V
         */
        {
            newInst.add(new ImmutableInstruction35c(
                    Opcode.INVOKE_VIRTUAL,
                    3,
                    1,
                    p1,
                    p0,
                    0,
                    0,
                    new ImmutableMethodReference(
                            "Ljava/lang/reflect/Field;",
                            "set",
                            Lists.newArrayList("Ljava/lang/Object;", "Ljava/lang/Object;"),
                            "V")));
        }
        /**
         * return-void
         */
        {
            newInst.add(new ImmutableInstruction10x(Opcode.RETURN_VOID));
        }
        /**
         * move-exception v2
         */
        {
            newInst.add(new ImmutableInstruction11x(Opcode.MOVE_EXCEPTION, 2));
        }
        /**
         * invoke-virtual {v0}, Ljava/lang/Class;->getSuperclass()Ljava/lang/Class;
         */
        {
            newInst.add(new ImmutableInstruction35c(
                    Opcode.INVOKE_VIRTUAL,
                    1,
                    0,
                    0,
                    0,
                    0,
                    0,
                    new ImmutableMethodReference(
                            "Ljava/lang/Class;",
                            "getSuperclass",
                            null,
                            "Ljava/lang/Class;")));
        }
        /**
         * move-result-object v0
         */
        {
            newInst.add(new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT, 0));
        }
        /**
         * goto :goto_4
         */
        {
            newInst.add(new ImmutableInstruction10t(Opcode.GOTO, -19));
        }
        int handlerCodeAddress = 0;
        for (Instruction instruction : newInst) {
            if (instruction.getOpcode() == Opcode.MOVE_EXCEPTION)
                break;
            handlerCodeAddress += instruction.getCodeUnits();
        }
        List<ImmutableExceptionHandler> exceptionHandlers = new ArrayList<>();
        exceptionHandlers.add(new ImmutableExceptionHandler("Ljava/lang/Exception;", handlerCodeAddress));
        List<MethodParameter> methodParameters = new ArrayList<>();
        for (CharSequence s : Parameter)
            methodParameters.add(new ImmutableMethodParameter((String) s, null, null));
        return new ImmutableMethod(
                classDef,
                newName,
                methodParameters,
                "V",
                AccessFlags.PUBLIC.getValue() | AccessFlags.STATIC.getValue(),
                null,
                null,
                new ImmutableMethodImplementation(
                        start_local + 1 + methodParameters.size(),
                        newInst,
                        Lists.newArrayList(new ImmutableTryBlock(4, 14, exceptionHandlers)),
                        null));
    }

    private Method createNewPrivateGetMethod(String newName, String classDef, String DefiningClass, String Name, List<? extends CharSequence> Parameter, String Return) {
        List<Instruction> newInst = new ArrayList<>();
        int start_local = 2;
        int p0 = start_local + 1;
        int p1 = start_local + 2;
        int p2 = start_local + 3;
        int p3 = start_local + 4;
        int p4 = start_local + 5;
        /**
         * invoke-virtual {p1}, Ljava/lang/Object;->getClass()Ljava/lang/Class;
         */
        {
            newInst.add(new ImmutableInstruction35c(
                    Opcode.INVOKE_VIRTUAL,
                    1,
                    p1,
                    0,
                    0,
                    0,
                    0,
                    new ImmutableMethodReference(
                            "Ljava/lang/Object;",
                            "getClass",
                            null,
                            "Ljava/lang/Class;")));
        }
        /**
         * move-result-object v0
         */
        {
            newInst.add(new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT, 0));
        }
        /**
         * const-string v1, "获取的对象name"
         */
        {
            newInst.add(new ImmutableInstruction21c(Opcode.CONST_STRING, 1, new ImmutableStringReference(Name)));
        }
        /**
         * invoke-virtual {v0, v1}, Ljava/lang/Class;->getDeclaredField(Ljava/lang/String;)Ljava/lang/reflect/Field;
         */
        {
            newInst.add(new ImmutableInstruction35c(
                    Opcode.INVOKE_VIRTUAL,
                    2,
                    0,
                    1,
                    0,
                    0,
                    0,
                    new ImmutableMethodReference(
                            "Ljava/lang/Class;",
                            "getDeclaredField",
                            Lists.newArrayList("Ljava/lang/String;"),
                            "Ljava/lang/reflect/Field;")));
        }
        /**
         * move-result-object v1
         */
        {
            newInst.add(new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT, 1));
        }
        /**
         * const/4 v2, 0x1 true
         */
        {
            newInst.add(new ImmutableInstruction11n(Opcode.CONST_4, 2, 0x1));
        }
        /**
         * invoke-virtual {v1, v2}, Ljava/lang/reflect/Field;->setAccessible(Z)V
         */
        {
            newInst.add(new ImmutableInstruction35c(
                    Opcode.INVOKE_VIRTUAL,
                    2,
                    1,
                    2,
                    0,
                    0,
                    0,
                    new ImmutableMethodReference(
                            "Ljava/lang/reflect/Field;",
                            "setAccessible",
                            Lists.newArrayList("Z"),
                            "V")));
        }
        /**
         * invoke-virtual {v1, p0}, Ljava/lang/reflect/Field;->get(Ljava/lang/Object;)Ljava/lang/Object;
         */
        {
            newInst.add(new ImmutableInstruction35c(
                    Opcode.INVOKE_VIRTUAL,
                    2,
                    1,
                    p1,
                    0,
                    0,
                    0,
                    new ImmutableMethodReference(
                            "Ljava/lang/reflect/Field;",
                            "get",
                            Lists.newArrayList("Ljava/lang/Object;"),
                            "Ljava/lang/Object;")));
        }
        /**
         * move-result-object v1
         */
        {
            newInst.add(new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT, 1));
        }
        /**
         * check-cast v1, Ljava/lang/String;
         */
        {
            newInst.add(new ImmutableInstruction21c(Opcode.CHECK_CAST, 1, new ImmutableTypeReference(Return)));
        }
        /**
         * return-object v1
         */
        {
            newInst.add(new ImmutableInstruction11x(Opcode.RETURN_OBJECT, 1));
        }
        /**
         * move-exception v2
         */
        {
            newInst.add(new ImmutableInstruction11x(Opcode.MOVE_EXCEPTION, 2));
        }
        /**
         * invoke-virtual {v0}, Ljava/lang/Class;->getSuperclass()Ljava/lang/Class;
         */
        {
            newInst.add(new ImmutableInstruction35c(
                    Opcode.INVOKE_VIRTUAL,
                    1,
                    0,
                    0,
                    0,
                    0,
                    0,
                    new ImmutableMethodReference(
                            "Ljava/lang/Class;",
                            "getSuperclass",
                            null,
                            "Ljava/lang/Class;")));
        }
        /**
         * move-result-object v0
         */
        {
            newInst.add(new ImmutableInstruction11x(Opcode.MOVE_RESULT_OBJECT, 0));
        }
        /**
         * goto :goto_4
         */
        {
            newInst.add(new ImmutableInstruction10t(Opcode.GOTO, -22));
        }
        int handlerCodeAddress = 0;
        for (Instruction instruction : newInst) {
            if (instruction.getOpcode() == Opcode.MOVE_EXCEPTION)
                break;
            handlerCodeAddress += instruction.getCodeUnits();
        }
        List<ImmutableExceptionHandler> exceptionHandlers = new ArrayList<>();
        exceptionHandlers.add(new ImmutableExceptionHandler("Ljava/lang/Exception;", handlerCodeAddress));
        return new ImmutableMethod(
                classDef,
                newName,
                Lists.newArrayList(new ImmutableMethodParameter(DefiningClass, null, null)),
                Return,
                AccessFlags.PUBLIC.getValue() | AccessFlags.STATIC.getValue(),
                null,
                null,
                new ImmutableMethodImplementation(
                        start_local + 1 + Parameter.size(),
                        newInst,
                        Lists.newArrayList(new ImmutableTryBlock(4, 17, exceptionHandlers)),
                        null));
    }
}
