package armadillo.transformers.se;

import armadillo.common.SimpleNameFactory;
import armadillo.model.obfuscators.CallMethod;
import com.google.common.collect.Lists;
import org.jf.dexlib2.AccessFlags;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.builder.MutableMethodImplementation;
import org.jf.dexlib2.builder.instruction.*;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.formats.*;
import org.jf.dexlib2.iface.reference.FieldReference;
import org.jf.dexlib2.iface.reference.MethodReference;
import org.jf.dexlib2.iface.reference.Reference;
import org.jf.dexlib2.iface.reference.TypeReference;
import org.jf.dexlib2.immutable.ImmutableClassDef;
import org.jf.dexlib2.immutable.ImmutableMethod;
import org.jf.dexlib2.immutable.ImmutableMethodImplementation;
import org.jf.dexlib2.immutable.reference.ImmutableMethodReference;
import org.jf.dexlib2.immutable.reference.ImmutableStringReference;
import org.jf.dexlib2.immutable.reference.ImmutableTypeReference;
import org.jf.dexlib2.rewriter.Rewriter;
import org.jf.dexlib2.rewriter.RewriterModule;
import org.jf.dexlib2.rewriter.Rewriters;
import org.jf.util.NumberUtils;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class ClassDefRewriter extends RewriterModule {
    private final SimpleNameFactory nameFactory = new SimpleNameFactory();
    private final String type;
    private final String methodName;
    private final List<CallMethod> callMethods;
    private int index;

    public ClassDefRewriter(String type, String methodName, List<CallMethod> callMethods) {
        this.type = type;
        this.methodName = methodName;
        this.callMethods = callMethods;
    }

    @Nonnull
    @Override
    public Rewriter<ClassDef> getClassDefRewriter(@Nonnull Rewriters rewriters) {
        return new Rewriter<ClassDef>() {
            @Nonnull
            @Override
            public ClassDef rewrite(@Nonnull ClassDef classDef) {
                List<Method> methods = new ArrayList<>();
                for (Method value : classDef.getMethods()) {
                    if (value.getImplementation() != null && !value.getName().equals("<init>")) {
                        MutableMethodImplementation mutableImplementation = new MutableMethodImplementation(value.getImplementation());
                        int minRegStart = mutableImplementation.getRegisterCount() + 1;
                        int addCount = 10;
                        for (int i = 0; i < mutableImplementation.getInstructions().size(); i++) {
                            Instruction builderInstruction = mutableImplementation.getInstructions().get(i);
                            List<String> parameters = new ArrayList<>();
                            parameters.add("I");
                            switch (builderInstruction.getOpcode()) {
                                case NEW_ARRAY: {
                                    Instruction22c instruction22c = (Instruction22c) builderInstruction;
                                    TypeReference typeReference = (TypeReference) instruction22c.getReference();
                                    parameters.add("I");
                                    mutableImplementation.replaceInstruction(i, new BuilderInstruction31i(
                                            Opcode.CONST,
                                            minRegStart,
                                            putMethod(nameFactory.nextName(),
                                                    builderInstruction.getOpcode(),
                                                    typeReference,
                                                    parameters,
                                                    MethodHelper.getResultType(typeReference.getType()))));
                                    if (minRegStart > 15) {
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_FROM16, minRegStart + 1, instruction22c.getRegisterB()));
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction3rc(
                                                Opcode.INVOKE_STATIC_RANGE,
                                                minRegStart,
                                                2,
                                                new ImmutableMethodReference(type,
                                                        methodName,
                                                        parameters,
                                                        MethodHelper.getResultType(typeReference.getType()))));
                                    } else {
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction35c(
                                                Opcode.INVOKE_STATIC,
                                                2,
                                                minRegStart,
                                                instruction22c.getRegisterB(),
                                                0,
                                                0,
                                                0,
                                                new ImmutableMethodReference(type,
                                                        methodName,
                                                        parameters,
                                                        MethodHelper.getResultType(typeReference.getType()))));
                                    }
                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction11x(
                                            Opcode.MOVE_RESULT_OBJECT,
                                            instruction22c.getRegisterA()));
                                }
                                break;
                                case FILLED_NEW_ARRAY: {
                                    Instruction35c instruction35c = (Instruction35c) builderInstruction;
                                    TypeReference typeReference = (TypeReference) instruction35c.getReference();
                                    for (int index = 0; index < instruction35c.getRegisterCount(); index++)
                                        parameters.add(MethodHelper.getResultType(typeReference.getType().substring(1)));
                                    mutableImplementation.replaceInstruction(i, new BuilderInstruction31i(
                                            Opcode.CONST,
                                            minRegStart,
                                            putMethod(nameFactory.nextName(),
                                                    builderInstruction.getOpcode(),
                                                    typeReference,
                                                    parameters,
                                                    MethodHelper.getResultType(typeReference.getType()))));
                                    if (minRegStart > 15 || instruction35c.getRegisterCount() >= 5) {
                                        if (typeReference.getType().contains("L") && typeReference.getType().endsWith(";")) {
                                            for (int index = 0; index < instruction35c.getRegisterCount(); index++)
                                                mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_OBJECT_FROM16, minRegStart + index + 1, MethodHelper.getInstruction35cRegisterIndex(instruction35c, index)));
                                        } else {
                                            for (int index = 0; index < instruction35c.getRegisterCount(); index++)
                                                mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_FROM16, minRegStart + index + 1, MethodHelper.getInstruction35cRegisterIndex(instruction35c, index)));
                                        }
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction3rc(
                                                Opcode.INVOKE_STATIC_RANGE,
                                                minRegStart,
                                                instruction35c.getRegisterCount() + 1,
                                                new ImmutableMethodReference(type,
                                                        methodName,
                                                        parameters,
                                                        MethodHelper.getResultType(typeReference.getType()))));
                                    } else {
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction35c(
                                                Opcode.INVOKE_STATIC,
                                                instruction35c.getRegisterCount() + 1,
                                                minRegStart,
                                                instruction35c.getRegisterC(),
                                                instruction35c.getRegisterD(),
                                                instruction35c.getRegisterE(),
                                                instruction35c.getRegisterF(),
                                                new ImmutableMethodReference(type,
                                                        methodName,
                                                        parameters,
                                                        MethodHelper.getResultType(typeReference.getType()))));
                                    }
                                }
                                break;
                                case FILLED_NEW_ARRAY_RANGE: {
                                    Instruction3rc instruction3rc = (Instruction3rc) builderInstruction;
                                    TypeReference typeReference = (TypeReference) instruction3rc.getReference();
                                    for (int index = 0; index < instruction3rc.getRegisterCount(); index++)
                                        parameters.add(MethodHelper.getResultType(typeReference.getType().substring(1)));
                                    if (addCount < instruction3rc.getRegisterCount())
                                        addCount = instruction3rc.getRegisterCount();
                                    mutableImplementation.replaceInstruction(i, new BuilderInstruction31i(
                                            Opcode.CONST,
                                            minRegStart,
                                            putMethod(nameFactory.nextName(),
                                                    builderInstruction.getOpcode(),
                                                    typeReference,
                                                    parameters,
                                                    MethodHelper.getResultType(typeReference.getType()))));
                                    if (typeReference.getType().contains("L") && typeReference.getType().endsWith(";")) {
                                        for (int index = 0; index < instruction3rc.getRegisterCount(); index++)
                                            mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_OBJECT_FROM16, minRegStart + index + 1, instruction3rc.getStartRegister() + index));
                                    } else {
                                        for (int index = 0; index < instruction3rc.getRegisterCount(); index++)
                                            mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_FROM16, minRegStart + index + 1, instruction3rc.getStartRegister() + index));
                                    }
                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction3rc(
                                            Opcode.INVOKE_STATIC_RANGE,
                                            minRegStart,
                                            instruction3rc.getRegisterCount() + 1,
                                            new ImmutableMethodReference(type,
                                                    methodName,
                                                    parameters,
                                                    MethodHelper.getResultType(typeReference.getType()))));
                                }
                                break;
                                case NEW_INSTANCE: {
                                    Instruction21c instruction21c = (Instruction21c) builderInstruction;
                                    TypeReference typeReference = (TypeReference) instruction21c.getReference();
                                    mutableImplementation.replaceInstruction(i, new BuilderInstruction31i(
                                            Opcode.CONST,
                                            minRegStart,
                                            putMethod(nameFactory.nextName(),
                                                    builderInstruction.getOpcode(),
                                                    typeReference,
                                                    parameters,
                                                    MethodHelper.getResultType(typeReference.getType()))));
                                    if (minRegStart > 15) {
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction3rc(
                                                Opcode.INVOKE_STATIC_RANGE,
                                                minRegStart,
                                                1,
                                                new ImmutableMethodReference(type,
                                                        methodName,
                                                        parameters,
                                                        MethodHelper.getResultType(typeReference.getType()))));
                                    } else {
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction35c(
                                                Opcode.INVOKE_STATIC,
                                                1,
                                                minRegStart,
                                                0,
                                                0,
                                                0,
                                                0,
                                                new ImmutableMethodReference(type,
                                                        methodName,
                                                        parameters,
                                                        MethodHelper.getResultType(typeReference.getType()))));
                                    }
                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction11x(
                                            Opcode.MOVE_RESULT_OBJECT,
                                            instruction21c.getRegisterA()));
                                }
                                break;
                                case RETURN_OBJECT: {
                                    Instruction11x instruction11x = (Instruction11x) builderInstruction;
                                    mutableImplementation.replaceInstruction(i, new BuilderInstruction21c(
                                            Opcode.CHECK_CAST,
                                            instruction11x.getRegisterA(), new ImmutableTypeReference(value.getReturnType())));
                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction11x(Opcode.RETURN_OBJECT, instruction11x.getRegisterA()));
                                }
                                break;
                                case THROW: {
                                    Instruction11x instruction11x = (Instruction11x) builderInstruction;
                                    mutableImplementation.replaceInstruction(i, new BuilderInstruction21c(
                                            Opcode.CHECK_CAST,
                                            instruction11x.getRegisterA(), new ImmutableTypeReference("Ljava/lang/Throwable;")));
                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction11x(Opcode.THROW, instruction11x.getRegisterA()));
                                }
                                break;
                                case INVOKE_STATIC: {
                                    Instruction35c instruction35c = (Instruction35c) builderInstruction;
                                    MethodReference reference = (MethodReference) instruction35c.getReference();
                                    parameters.addAll(MethodHelper.getParameters(Lists.newArrayList(reference.getParameterTypes().toArray(new String[0]))));
                                    mutableImplementation.replaceInstruction(i, new BuilderInstruction31i(
                                            Opcode.CONST,
                                            minRegStart,
                                            putMethod(nameFactory.nextName(),
                                                    Opcode.INVOKE_STATIC,
                                                    reference,
                                                    parameters,
                                                    MethodHelper.getResultType(reference.getReturnType()))));
                                    if (minRegStart > 15 || instruction35c.getRegisterCount() >= 5) {
                                        int src_add = 0;
                                        for (int index = 0; index < reference.getParameterTypes().size(); index++) {
                                            switch (reference.getParameterTypes().get(index).toString()) {
                                                case "Z":
                                                case "B":
                                                case "S":
                                                case "C":
                                                case "I":
                                                case "F": {
                                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_FROM16, minRegStart + index + 1 + src_add, MethodHelper.getInstruction35cRegisterIndex(instruction35c, index + src_add)));
                                                }
                                                break;
                                                case "D":
                                                case "J": {
                                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_WIDE_FROM16, minRegStart + index + 1 + src_add, MethodHelper.getInstruction35cRegisterIndex(instruction35c, index + src_add)));
                                                    src_add++;
                                                }
                                                break;
                                                default: {
                                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_OBJECT_FROM16, minRegStart + index + 1 + src_add, MethodHelper.getInstruction35cRegisterIndex(instruction35c, index + src_add)));
                                                }
                                                break;
                                            }
                                        }
                                        int RegSize = mutableImplementation.getReferenceRegSize(reference);
                                        if (addCount < RegSize)
                                            addCount = RegSize;
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction3rc(
                                                Opcode.INVOKE_STATIC_RANGE,
                                                minRegStart,
                                                instruction35c.getRegisterCount() + 1,
                                                new ImmutableMethodReference(type,
                                                        methodName,
                                                        parameters,
                                                        MethodHelper.getResultType(reference.getReturnType()))));
                                    } else {
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction35c(
                                                Opcode.INVOKE_STATIC,
                                                instruction35c.getRegisterCount() + 1,
                                                minRegStart,
                                                instruction35c.getRegisterC(),
                                                instruction35c.getRegisterD(),
                                                instruction35c.getRegisterE(),
                                                instruction35c.getRegisterF(),
                                                new ImmutableMethodReference(type,
                                                        methodName,
                                                        parameters,
                                                        MethodHelper.getResultType(reference.getReturnType()))));
                                    }
                                }
                                break;
                                case INVOKE_STATIC_RANGE: {
                                    Instruction3rc instruction3rc = (Instruction3rc) builderInstruction;
                                    MethodReference reference = (MethodReference) instruction3rc.getReference();
                                    parameters.addAll(MethodHelper.getParameters(Lists.newArrayList(reference.getParameterTypes().toArray(new String[0]))));
                                    mutableImplementation.replaceInstruction(i, new BuilderInstruction31i(
                                            Opcode.CONST,
                                            minRegStart,
                                            putMethod(nameFactory.nextName(),
                                                    Opcode.INVOKE_STATIC,
                                                    reference,
                                                    parameters,
                                                    MethodHelper.getResultType(reference.getReturnType()))));
                                    int src_add = 0;
                                    for (int index = 0; index < reference.getParameterTypes().size(); index++) {
                                        switch (reference.getParameterTypes().get(index).toString()) {
                                            case "Z":
                                            case "B":
                                            case "S":
                                            case "C":
                                            case "I":
                                            case "F": {
                                                mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_FROM16, minRegStart + index + 1 + src_add, instruction3rc.getStartRegister() + index + src_add));
                                            }
                                            break;
                                            case "D":
                                            case "J": {
                                                mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_WIDE_FROM16, minRegStart + index + 1 + src_add, instruction3rc.getStartRegister() + index + src_add));
                                                src_add++;
                                            }
                                            break;
                                            default: {
                                                mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_OBJECT_FROM16, minRegStart + index + 1 + src_add, instruction3rc.getStartRegister() + index + src_add));
                                            }
                                            break;
                                        }
                                    }
                                    int RegSize = mutableImplementation.getReferenceRegSize(reference);
                                    if (addCount < RegSize)
                                        addCount = RegSize;
                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction3rc(
                                            Opcode.INVOKE_STATIC_RANGE,
                                            minRegStart,
                                            instruction3rc.getRegisterCount() + 1,
                                            new ImmutableMethodReference(type,
                                                    methodName,
                                                    parameters,
                                                    MethodHelper.getResultType(reference.getReturnType()))));
                                }
                                break;
                                case INVOKE_INTERFACE:
                                case INVOKE_VIRTUAL: {
                                    Instruction35c instruction35c = (Instruction35c) builderInstruction;
                                    MethodReference reference = (MethodReference) instruction35c.getReference();
                                    parameters.add("Ljava/lang/Object;");
                                    parameters.addAll(MethodHelper.getParameters(Lists.newArrayList(reference.getParameterTypes().toArray(new String[0]))));
                                    mutableImplementation.replaceInstruction(i, new BuilderInstruction31i(
                                            Opcode.CONST,
                                            minRegStart,
                                            putMethod(nameFactory.nextName(),
                                                    Opcode.INVOKE_VIRTUAL,
                                                    reference, parameters,
                                                    MethodHelper.getResultType(reference.getReturnType()))));
                                    if (minRegStart > 15 || instruction35c.getRegisterCount() >= 5) {
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_OBJECT_FROM16, minRegStart + 1, MethodHelper.getInstruction35cRegisterIndex(instruction35c, 0)));
                                        int src_add = 0;
                                        for (int index = 0; index < reference.getParameterTypes().size(); index++) {
                                            switch (reference.getParameterTypes().get(index).toString()) {
                                                case "Z":
                                                case "B":
                                                case "S":
                                                case "C":
                                                case "I":
                                                case "F": {
                                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_FROM16, minRegStart + index + 2 + src_add, MethodHelper.getInstruction35cRegisterIndex(instruction35c, index + src_add + 1)));
                                                }
                                                break;
                                                case "D":
                                                case "J": {
                                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_WIDE_FROM16, minRegStart + index + 2 + src_add, MethodHelper.getInstruction35cRegisterIndex(instruction35c, index + src_add + 1)));
                                                    src_add++;
                                                }
                                                break;
                                                default: {
                                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_OBJECT_FROM16, minRegStart + index + 2 + src_add, MethodHelper.getInstruction35cRegisterIndex(instruction35c, index + src_add + 1)));
                                                }
                                                break;
                                            }
                                        }
                                        int RegSize = mutableImplementation.getReferenceRegSize(reference) + 1;
                                        if (addCount < RegSize)
                                            addCount = RegSize;
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction3rc(
                                                Opcode.INVOKE_STATIC_RANGE,
                                                minRegStart,
                                                instruction35c.getRegisterCount() + 1,
                                                new ImmutableMethodReference(type,
                                                        methodName,
                                                        parameters,
                                                        MethodHelper.getResultType(reference.getReturnType()))));
                                    } else {
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction35c(
                                                Opcode.INVOKE_STATIC,
                                                instruction35c.getRegisterCount() + 1,
                                                minRegStart,
                                                instruction35c.getRegisterC(),
                                                instruction35c.getRegisterD(),
                                                instruction35c.getRegisterE(),
                                                instruction35c.getRegisterF(),
                                                new ImmutableMethodReference(type,
                                                        methodName,
                                                        parameters,
                                                        MethodHelper.getResultType(reference.getReturnType()))));
                                    }
                                }
                                break;
                                case INVOKE_VIRTUAL_RANGE:
                                case INVOKE_INTERFACE_RANGE: {
                                    Instruction3rc instruction3rc = (Instruction3rc) builderInstruction;
                                    MethodReference reference = (MethodReference) instruction3rc.getReference();
                                    parameters.add("Ljava/lang/Object;");
                                    parameters.addAll(MethodHelper.getParameters(Lists.newArrayList(reference.getParameterTypes().toArray(new String[0]))));
                                    mutableImplementation.replaceInstruction(i, new BuilderInstruction31i(Opcode.CONST, minRegStart, putMethod(nameFactory.nextName(), Opcode.INVOKE_VIRTUAL, reference, parameters, MethodHelper.getResultType(reference.getReturnType()))));
                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_OBJECT_FROM16, minRegStart + 1, instruction3rc.getStartRegister()));
                                    int src_add = 0;
                                    for (int index = 0; index < reference.getParameterTypes().size(); index++) {
                                        switch (reference.getParameterTypes().get(index).toString()) {
                                            case "Z":
                                            case "B":
                                            case "S":
                                            case "C":
                                            case "I":
                                            case "F": {
                                                mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_FROM16, minRegStart + index + 2 + src_add, instruction3rc.getStartRegister() + index + 1 + src_add));
                                            }
                                            break;
                                            case "D":
                                            case "J": {
                                                mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_WIDE_FROM16, minRegStart + index + 2 + src_add, instruction3rc.getStartRegister() + index + 1 + src_add));
                                                src_add++;
                                            }
                                            break;
                                            default: {
                                                mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_OBJECT_FROM16, minRegStart + index + 2 + src_add, instruction3rc.getStartRegister() + index + 1 + src_add));
                                            }
                                            break;
                                        }
                                    }
                                    int RegSize = mutableImplementation.getReferenceRegSize(reference) + 1;
                                    if (addCount < RegSize)
                                        addCount = RegSize;
                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction3rc(
                                            Opcode.INVOKE_STATIC_RANGE,
                                            minRegStart,
                                            instruction3rc.getRegisterCount() + 1,
                                            new ImmutableMethodReference(type,
                                                    methodName,
                                                    parameters,
                                                    MethodHelper.getResultType(reference.getReturnType()))));
                                }
                                break;
                                case INVOKE_DIRECT: {
                                    Instruction35c instruction35c = (Instruction35c) builderInstruction;
                                    MethodReference reference = (MethodReference) instruction35c.getReference();
                                    //构造函数
                                    if (reference.getName().equals("<init>")) {
                                        if (reference.getDefiningClass().equals("Ljava/lang/String;")) {
                                            parameters.addAll(MethodHelper.getParameters(Lists.newArrayList(reference.getParameterTypes().toArray(new String[0]))));
                                            mutableImplementation.replaceInstruction(i, new BuilderInstruction31i(Opcode.CONST, minRegStart, putMethod(nameFactory.nextName(), Opcode.INVOKE_DIRECT_EMPTY, reference, parameters, MethodHelper.getResultType(reference.getDefiningClass()))));
                                            if (minRegStart > 15 || instruction35c.getRegisterCount() >= 5) {
                                                int src_add = 0;
                                                for (int index = 0; index < reference.getParameterTypes().size(); index++) {
                                                    switch (reference.getParameterTypes().get(index).toString()) {
                                                        case "Z":
                                                        case "B":
                                                        case "S":
                                                        case "C":
                                                        case "I":
                                                        case "F": {
                                                            mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_FROM16, minRegStart + index + 1 + src_add, MethodHelper.getInstruction35cRegisterIndex(instruction35c, index + 1 + src_add)));
                                                        }
                                                        break;
                                                        case "D":
                                                        case "J": {
                                                            mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_WIDE_FROM16, minRegStart + index + 1 + src_add, MethodHelper.getInstruction35cRegisterIndex(instruction35c, index + src_add + 1 + src_add)));
                                                            src_add++;
                                                        }
                                                        break;
                                                        default: {
                                                            mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_OBJECT_FROM16, minRegStart + index + 1 + src_add, MethodHelper.getInstruction35cRegisterIndex(instruction35c, index + 1 + src_add)));
                                                        }
                                                        break;
                                                    }
                                                }
                                                int RegSize = mutableImplementation.getReferenceRegSize(reference) + 1;
                                                if (addCount < RegSize)
                                                    addCount = RegSize;
                                                mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction3rc(
                                                        Opcode.INVOKE_STATIC_RANGE,
                                                        minRegStart,
                                                        instruction35c.getRegisterCount(),
                                                        new ImmutableMethodReference(type,
                                                                methodName,
                                                                parameters,
                                                                MethodHelper.getResultType(reference.getDefiningClass()))));
                                            } else {
                                                mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction35c(
                                                        Opcode.INVOKE_STATIC,
                                                        instruction35c.getRegisterCount(),
                                                        minRegStart,
                                                        instruction35c.getRegisterD(),
                                                        instruction35c.getRegisterE(),
                                                        instruction35c.getRegisterF(),
                                                        instruction35c.getRegisterG(),
                                                        new ImmutableMethodReference(type,
                                                                methodName,
                                                                parameters,
                                                                MethodHelper.getResultType(reference.getDefiningClass()))));
                                            }
                                            mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction11x(
                                                    Opcode.MOVE_RESULT_OBJECT,
                                                    instruction35c.getRegisterC()));
                                        } else {
                                            parameters.add("Ljava/lang/Object;");
                                            parameters.addAll(MethodHelper.getParameters(Lists.newArrayList(reference.getParameterTypes().toArray(new String[0]))));
                                            mutableImplementation.replaceInstruction(i, new BuilderInstruction31i(Opcode.CONST, minRegStart, putMethod(nameFactory.nextName(), builderInstruction.getOpcode(), reference, parameters, "V")));
                                            if (minRegStart > 15 || instruction35c.getRegisterCount() >= 5) {
                                                mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_OBJECT_FROM16, minRegStart + 1, MethodHelper.getInstruction35cRegisterIndex(instruction35c, 0)));
                                                int src_add = 0;
                                                for (int index = 0; index < reference.getParameterTypes().size(); index++) {
                                                    switch (reference.getParameterTypes().get(index).toString()) {
                                                        case "Z":
                                                        case "B":
                                                        case "S":
                                                        case "C":
                                                        case "I":
                                                        case "F": {
                                                            mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_FROM16, minRegStart + index + 2 + src_add, MethodHelper.getInstruction35cRegisterIndex(instruction35c, index + 1 + src_add)));
                                                        }
                                                        break;
                                                        case "D":
                                                        case "J": {
                                                            mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_WIDE_FROM16, minRegStart + index + 2 + src_add, MethodHelper.getInstruction35cRegisterIndex(instruction35c, index + 1 + src_add)));
                                                            src_add++;
                                                        }
                                                        break;
                                                        default: {
                                                            mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_OBJECT_FROM16, minRegStart + index + 2 + src_add, MethodHelper.getInstruction35cRegisterIndex(instruction35c, index + 1 + src_add)));
                                                        }
                                                        break;
                                                    }
                                                }
                                                int RegSize = mutableImplementation.getReferenceRegSize(reference);
                                                if (addCount < RegSize)
                                                    addCount = RegSize;
                                                mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction3rc(
                                                        Opcode.INVOKE_STATIC_RANGE,
                                                        minRegStart,
                                                        instruction35c.getRegisterCount() + 1,
                                                        new ImmutableMethodReference(type,
                                                                methodName,
                                                                parameters,
                                                                "V")));
                                            } else {
                                                mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction35c(
                                                        Opcode.INVOKE_STATIC,
                                                        instruction35c.getRegisterCount() + 1,
                                                        minRegStart,
                                                        instruction35c.getRegisterC(),
                                                        instruction35c.getRegisterD(),
                                                        instruction35c.getRegisterE(),
                                                        instruction35c.getRegisterF(),
                                                        new ImmutableMethodReference(type,
                                                                methodName,
                                                                parameters,
                                                                "V")));
                                            }
                                        }
                                    }
                                    //非构造函数
                                    else {
                                        parameters.add("Ljava/lang/Object;");
                                        parameters.addAll(MethodHelper.getParameters(Lists.newArrayList(reference.getParameterTypes().toArray(new String[0]))));
                                        mutableImplementation.replaceInstruction(i, new BuilderInstruction31i(Opcode.CONST, minRegStart, putMethod(nameFactory.nextName(), Opcode.INVOKE_VIRTUAL, reference, parameters, MethodHelper.getResultType(reference.getReturnType()))));
                                        if (minRegStart > 15 || instruction35c.getRegisterCount() >= 5) {
                                            mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_OBJECT_FROM16, minRegStart + 1, MethodHelper.getInstruction35cRegisterIndex(instruction35c, 0)));
                                            int src_add = 0;
                                            for (int index = 0; index < reference.getParameterTypes().size(); index++) {
                                                switch (reference.getParameterTypes().get(index).toString()) {
                                                    case "Z":
                                                    case "B":
                                                    case "S":
                                                    case "C":
                                                    case "I":
                                                    case "F": {
                                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_FROM16, minRegStart + index + 2 + src_add, MethodHelper.getInstruction35cRegisterIndex(instruction35c, index + 1 + src_add)));
                                                    }
                                                    break;
                                                    case "D":
                                                    case "J": {
                                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_WIDE_FROM16, minRegStart + index + 2 + src_add, MethodHelper.getInstruction35cRegisterIndex(instruction35c, index + 1 + src_add)));
                                                        src_add++;
                                                    }
                                                    break;
                                                    default: {
                                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_OBJECT_FROM16, minRegStart + index + 2 + src_add, MethodHelper.getInstruction35cRegisterIndex(instruction35c, index + 1 + src_add)));
                                                    }
                                                    break;
                                                }
                                            }
                                            int RegSize = mutableImplementation.getReferenceRegSize(reference) + 1;
                                            if (addCount < RegSize)
                                                addCount = RegSize;
                                            mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction3rc(
                                                    Opcode.INVOKE_STATIC_RANGE,
                                                    minRegStart,
                                                    instruction35c.getRegisterCount() + 1,
                                                    new ImmutableMethodReference(type,
                                                            methodName,
                                                            parameters,
                                                            MethodHelper.getResultType(reference.getReturnType()))));
                                        } else {
                                            mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction35c(
                                                    Opcode.INVOKE_STATIC,
                                                    instruction35c.getRegisterCount() + 1,
                                                    minRegStart,
                                                    instruction35c.getRegisterC(),
                                                    instruction35c.getRegisterD(),
                                                    instruction35c.getRegisterE(),
                                                    instruction35c.getRegisterF(),
                                                    new ImmutableMethodReference(type,
                                                            methodName,
                                                            parameters,
                                                            MethodHelper.getResultType(reference.getReturnType()))));
                                        }
                                    }
                                }
                                break;
                                case INVOKE_DIRECT_RANGE: {
                                    Instruction3rc instruction3rc = (Instruction3rc) builderInstruction;
                                    MethodReference reference = (MethodReference) instruction3rc.getReference();
                                    //构造函数
                                    if (reference.getName().equals("<init>")) {
                                        parameters.add("Ljava/lang/Object;");
                                        parameters.addAll(MethodHelper.getParameters(Lists.newArrayList(reference.getParameterTypes().toArray(new String[0]))));
                                        mutableImplementation.replaceInstruction(i, new BuilderInstruction31i(Opcode.CONST, minRegStart, putMethod(nameFactory.nextName(), Opcode.INVOKE_DIRECT, reference, parameters, "V")));
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_OBJECT_FROM16, minRegStart + 1, instruction3rc.getStartRegister()));
                                        int src_add = 0;
                                        for (int index = 0; index < reference.getParameterTypes().size(); index++) {
                                            switch (reference.getParameterTypes().get(index).toString()) {
                                                case "Z":
                                                case "B":
                                                case "S":
                                                case "C":
                                                case "I":
                                                case "F": {
                                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_FROM16, minRegStart + index + 2 + src_add, instruction3rc.getStartRegister() + index + 1 + src_add));
                                                }
                                                break;
                                                case "D":
                                                case "J": {
                                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_WIDE_FROM16, minRegStart + index + 2 + src_add, instruction3rc.getStartRegister() + index + 1 + src_add));
                                                    src_add++;
                                                }
                                                break;
                                                default: {
                                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_OBJECT_FROM16, minRegStart + index + 2 + src_add, instruction3rc.getStartRegister() + index + 1 + src_add));
                                                }
                                                break;
                                            }
                                        }
                                        int RegSize = mutableImplementation.getReferenceRegSize(reference) + 1;
                                        if (addCount < RegSize)
                                            addCount = RegSize;
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction3rc(
                                                Opcode.INVOKE_STATIC_RANGE,
                                                minRegStart,
                                                instruction3rc.getRegisterCount() + 1,
                                                new ImmutableMethodReference(type,
                                                        methodName,
                                                        parameters,
                                                        "V")));
                                    }
                                    //非构造函数
                                    else {
                                        parameters.add("Ljava/lang/Object;");
                                        parameters.addAll(MethodHelper.getParameters(Lists.newArrayList(reference.getParameterTypes().toArray(new String[0]))));
                                        mutableImplementation.replaceInstruction(i, new BuilderInstruction31i(Opcode.CONST, minRegStart, putMethod(nameFactory.nextName(), Opcode.INVOKE_VIRTUAL, reference, parameters, MethodHelper.getResultType(reference.getReturnType()))));
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_OBJECT_FROM16, minRegStart + 1, instruction3rc.getStartRegister()));
                                        int src_add = 0;
                                        for (int index = 0; index < reference.getParameterTypes().size(); index++) {
                                            switch (reference.getParameterTypes().get(index).toString()) {
                                                case "Z":
                                                case "B":
                                                case "S":
                                                case "C":
                                                case "I":
                                                case "F": {
                                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_FROM16, minRegStart + index + 2 + src_add, instruction3rc.getStartRegister() + index + 1 + src_add));
                                                }
                                                break;
                                                case "D":
                                                case "J": {
                                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_WIDE_FROM16, minRegStart + index + 2 + src_add, instruction3rc.getStartRegister() + index + 1 + src_add));
                                                    src_add++;
                                                }
                                                break;
                                                default: {
                                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_OBJECT_FROM16, minRegStart + index + 2 + src_add, instruction3rc.getStartRegister() + index + 1 + src_add));
                                                }
                                                break;
                                            }
                                        }
                                        int RegSize = mutableImplementation.getReferenceRegSize(reference) + 1;
                                        if (addCount < RegSize)
                                            addCount = RegSize;
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction3rc(
                                                Opcode.INVOKE_STATIC_RANGE,
                                                minRegStart,
                                                instruction3rc.getRegisterCount() + 1,
                                                new ImmutableMethodReference(type,
                                                        methodName,
                                                        parameters,
                                                        MethodHelper.getResultType(reference.getReturnType()))));
                                    }
                                }
                                break;
                                case INVOKE_SUPER: {
                                    Instruction35c instruction35c = (Instruction35c) builderInstruction;
                                    MethodReference reference = (MethodReference) instruction35c.getReference();
                                    parameters.add("Ljava/lang/Object;");
                                    parameters.addAll(MethodHelper.getParameters(Lists.newArrayList(reference.getParameterTypes().toArray(new String[0]))));
                                    mutableImplementation.replaceInstruction(i, new BuilderInstruction31i(Opcode.CONST, minRegStart, putMethod(nameFactory.nextName(), builderInstruction.getOpcode(), reference, parameters, MethodHelper.getResultType(reference.getReturnType()))));
                                    if (minRegStart > 15 || instruction35c.getRegisterCount() >= 5) {
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_OBJECT_FROM16, minRegStart + 1, MethodHelper.getInstruction35cRegisterIndex(instruction35c, 0)));
                                        int src_add = 0;
                                        for (int index = 0; index < reference.getParameterTypes().size(); index++) {
                                            switch (reference.getParameterTypes().get(index).toString()) {
                                                case "Z":
                                                case "B":
                                                case "S":
                                                case "C":
                                                case "I":
                                                case "F": {
                                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_FROM16, minRegStart + index + 2 + src_add, MethodHelper.getInstruction35cRegisterIndex(instruction35c, index + 1 + src_add)));
                                                }
                                                break;
                                                case "D":
                                                case "J": {
                                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_WIDE_FROM16, minRegStart + index + 2 + src_add, MethodHelper.getInstruction35cRegisterIndex(instruction35c, index + 1 + src_add)));
                                                    src_add++;
                                                }
                                                break;
                                                default: {
                                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_OBJECT_FROM16, minRegStart + index + 2 + src_add, MethodHelper.getInstruction35cRegisterIndex(instruction35c, index + 1 + src_add)));
                                                }
                                                break;
                                            }
                                        }
                                        int RegSize = mutableImplementation.getReferenceRegSize(reference) + 1;
                                        if (addCount < RegSize)
                                            addCount = RegSize;
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction3rc(
                                                Opcode.INVOKE_STATIC_RANGE,
                                                minRegStart,
                                                instruction35c.getRegisterCount() + 1,
                                                new ImmutableMethodReference(type,
                                                        methodName,
                                                        parameters,
                                                        MethodHelper.getResultType(reference.getReturnType()))));
                                    } else {
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction35c(
                                                Opcode.INVOKE_STATIC,
                                                instruction35c.getRegisterCount() + 1,
                                                minRegStart,
                                                instruction35c.getRegisterC(),
                                                instruction35c.getRegisterD(),
                                                instruction35c.getRegisterE(),
                                                instruction35c.getRegisterF(),
                                                new ImmutableMethodReference(type,
                                                        methodName,
                                                        parameters,
                                                        MethodHelper.getResultType(reference.getReturnType()))));
                                    }
                                }
                                break;
                                case INVOKE_SUPER_RANGE: {
                                    Instruction3rc instruction3rc = (Instruction3rc) builderInstruction;
                                    MethodReference reference = (MethodReference) instruction3rc.getReference();
                                    parameters.add("Ljava/lang/Object;");
                                    parameters.addAll(MethodHelper.getParameters(Lists.newArrayList(reference.getParameterTypes().toArray(new String[0]))));
                                    mutableImplementation.replaceInstruction(i, new BuilderInstruction31i(Opcode.CONST, minRegStart, putMethod(nameFactory.nextName(), Opcode.INVOKE_SUPER, reference, parameters, MethodHelper.getResultType(reference.getReturnType()))));
                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_OBJECT_FROM16, minRegStart + 1, instruction3rc.getStartRegister()));
                                    int src_add = 0;
                                    for (int index = 0; index < reference.getParameterTypes().size(); index++) {
                                        switch (reference.getParameterTypes().get(index).toString()) {
                                            case "Z":
                                            case "B":
                                            case "S":
                                            case "C":
                                            case "I":
                                            case "F": {
                                                mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_FROM16, minRegStart + index + 2 + src_add, instruction3rc.getStartRegister() + index + 1 + src_add));
                                            }
                                            break;
                                            case "D":
                                            case "J": {
                                                mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_WIDE_FROM16, minRegStart + index + 2 + src_add, instruction3rc.getStartRegister() + index + 1 + src_add));
                                                src_add++;
                                            }
                                            break;
                                            default: {
                                                mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_OBJECT_FROM16, minRegStart + index + 2 + src_add, instruction3rc.getStartRegister() + index + 1 + src_add));
                                            }
                                            break;
                                        }
                                    }
                                    int RegSize = mutableImplementation.getReferenceRegSize(reference) + 1;
                                    if (addCount < RegSize)
                                        addCount = RegSize;
                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction3rc(
                                            Opcode.INVOKE_STATIC_RANGE,
                                            minRegStart,
                                            instruction3rc.getRegisterCount() + 1,
                                            new ImmutableMethodReference(type,
                                                    methodName,
                                                    parameters,
                                                    MethodHelper.getResultType(reference.getReturnType()))));
                                }
                                break;
                                case CONST_STRING: {
                                    Instruction21c instruction21c = (Instruction21c) builderInstruction;
                                    mutableImplementation.replaceInstruction(i, new BuilderInstruction31i(Opcode.CONST, minRegStart, putMethod(nameFactory.nextName(), Opcode.CONST_STRING, instruction21c.getReference(), parameters, "Ljava/lang/Object;")));
                                    if (minRegStart > 15) {
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction3rc(
                                                Opcode.INVOKE_STATIC_RANGE,
                                                minRegStart,
                                                1,
                                                new ImmutableMethodReference(type,
                                                        methodName,
                                                        parameters,
                                                        "Ljava/lang/Object;")));
                                    } else {
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction35c(
                                                Opcode.INVOKE_STATIC,
                                                1,
                                                minRegStart,
                                                0,
                                                0,
                                                0,
                                                0,
                                                new ImmutableMethodReference(type,
                                                        methodName,
                                                        parameters,
                                                        "Ljava/lang/Object;")));
                                    }
                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction11x(
                                            Opcode.MOVE_RESULT_OBJECT,
                                            instruction21c.getRegisterA()));
                                }
                                break;
                                case CONST: {
                                    Instruction31i instruction31i = (Instruction31i) builderInstruction;
                                    if (instruction31i.getNarrowLiteral() == 0)
                                        break;
                                    //float
                                    if (NumberUtils.isLikelyFloat(instruction31i.getNarrowLiteral())) {
                                        mutableImplementation.replaceInstruction(i,
                                                new BuilderInstruction31i(
                                                        Opcode.CONST,
                                                        minRegStart,
                                                        putMethod(nameFactory.nextName(),
                                                                builderInstruction.getOpcode(),
                                                                new ImmutableStringReference(NumberUtils.getLikelyFloat(instruction31i.getNarrowLiteral())),
                                                                parameters,
                                                                "F")));
                                        if (minRegStart > 15) {
                                            mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction3rc(
                                                    Opcode.INVOKE_STATIC_RANGE,
                                                    minRegStart,
                                                    1,
                                                    new ImmutableMethodReference(type,
                                                            methodName,
                                                            parameters,
                                                            "F")));
                                        } else {
                                            mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction35c(
                                                    Opcode.INVOKE_STATIC,
                                                    1,
                                                    minRegStart,
                                                    0,
                                                    0,
                                                    0,
                                                    0,
                                                    new ImmutableMethodReference(type,
                                                            methodName,
                                                            parameters,
                                                            "F")));
                                        }
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction11x(
                                                Opcode.MOVE_RESULT,
                                                instruction31i.getRegisterA()));
                                    }
                                    //int
                                    else {
                                        mutableImplementation.replaceInstruction(i,
                                                new BuilderInstruction31i(
                                                        Opcode.CONST,
                                                        instruction31i.getRegisterA(),
                                                        putMethod(nameFactory.nextName(),
                                                                builderInstruction.getOpcode(),
                                                                new ImmutableStringReference("" + instruction31i.getNarrowLiteral()),
                                                                parameters,
                                                                "I")));
                                        if (instruction31i.getRegisterA() > 15) {
                                            mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction3rc(
                                                    Opcode.INVOKE_STATIC_RANGE,
                                                    instruction31i.getRegisterA(),
                                                    1,
                                                    new ImmutableMethodReference(type,
                                                            methodName,
                                                            parameters,
                                                            "I")));
                                        } else {
                                            mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction35c(
                                                    Opcode.INVOKE_STATIC,
                                                    1,
                                                    instruction31i.getRegisterA(),
                                                    0,
                                                    0,
                                                    0,
                                                    0,
                                                    new ImmutableMethodReference(type,
                                                            methodName,
                                                            parameters,
                                                            "I")));
                                        }
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction11x(
                                                Opcode.MOVE_RESULT,
                                                instruction31i.getRegisterA()));
                                    }
                                }
                                break;
                                case CONST_HIGH16: {
                                    Instruction21ih instruction21ih = (Instruction21ih) builderInstruction;
                                    if (instruction21ih.getNarrowLiteral() == 0)
                                        break;
                                    //float
                                    if (NumberUtils.isLikelyFloat(instruction21ih.getNarrowLiteral())) {
                                        mutableImplementation.replaceInstruction(i,
                                                new BuilderInstruction31i(
                                                        Opcode.CONST,
                                                        minRegStart,
                                                        putMethod(nameFactory.nextName(),
                                                                builderInstruction.getOpcode(),
                                                                new ImmutableStringReference(NumberUtils.getLikelyFloat(instruction21ih.getNarrowLiteral())),
                                                                parameters,
                                                                "F")));
                                        if (minRegStart > 15) {
                                            mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction3rc(
                                                    Opcode.INVOKE_STATIC_RANGE,
                                                    minRegStart,
                                                    1,
                                                    new ImmutableMethodReference(type,
                                                            methodName,
                                                            parameters,
                                                            "F")));
                                        } else {
                                            mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction35c(
                                                    Opcode.INVOKE_STATIC,
                                                    1,
                                                    minRegStart,
                                                    0,
                                                    0,
                                                    0,
                                                    0,
                                                    new ImmutableMethodReference(type,
                                                            methodName,
                                                            parameters,
                                                            "F")));
                                        }
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction11x(
                                                Opcode.MOVE_RESULT,
                                                instruction21ih.getRegisterA()));
                                    }
                                    //int
                                    else {
                                        mutableImplementation.replaceInstruction(i,
                                                new BuilderInstruction31i(
                                                        Opcode.CONST,
                                                        instruction21ih.getRegisterA(),
                                                        putMethod(nameFactory.nextName(),
                                                                builderInstruction.getOpcode(),
                                                                new ImmutableStringReference("" + instruction21ih.getNarrowLiteral()),
                                                                parameters,
                                                                "I")));
                                        if (instruction21ih.getRegisterA() > 15) {
                                            mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction3rc(
                                                    Opcode.INVOKE_STATIC_RANGE,
                                                    instruction21ih.getRegisterA(),
                                                    1,
                                                    new ImmutableMethodReference(type,
                                                            methodName,
                                                            parameters,
                                                            "I")));
                                        } else {
                                            mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction35c(
                                                    Opcode.INVOKE_STATIC,
                                                    1,
                                                    instruction21ih.getRegisterA(),
                                                    0,
                                                    0,
                                                    0,
                                                    0,
                                                    new ImmutableMethodReference(type,
                                                            methodName,
                                                            parameters,
                                                            "I")));
                                        }
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction11x(
                                                Opcode.MOVE_RESULT,
                                                instruction21ih.getRegisterA()));
                                    }
                                }
                                break;
                                case CONST_4: {
                                    Instruction11n instruction11n = (Instruction11n) builderInstruction;
                                    if (instruction11n.getNarrowLiteral() == 0)
                                        break;
                                    mutableImplementation.replaceInstruction(i,
                                            new BuilderInstruction31i(
                                                    Opcode.CONST,
                                                    instruction11n.getRegisterA(),
                                                    putMethod(nameFactory.nextName(),
                                                            builderInstruction.getOpcode(),
                                                            new ImmutableStringReference("" + instruction11n.getNarrowLiteral()),
                                                            parameters,
                                                            "I")));
                                    if (instruction11n.getRegisterA() > 15) {
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction3rc(
                                                Opcode.INVOKE_STATIC_RANGE,
                                                instruction11n.getRegisterA(),
                                                1,
                                                new ImmutableMethodReference(type,
                                                        methodName,
                                                        parameters,
                                                        "I")));
                                    } else {
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction35c(
                                                Opcode.INVOKE_STATIC,
                                                1,
                                                instruction11n.getRegisterA(),
                                                0,
                                                0,
                                                0,
                                                0,
                                                new ImmutableMethodReference(type,
                                                        methodName,
                                                        parameters,
                                                        "I")));
                                    }
                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction11x(
                                            Opcode.MOVE_RESULT,
                                            instruction11n.getRegisterA()));
                                }
                                break;
                                case CONST_16: {
                                    Instruction21s instruction21s = (Instruction21s) builderInstruction;
                                    if (instruction21s.getNarrowLiteral() == 0)
                                        break;
                                    //float
                                    if (NumberUtils.isLikelyFloat(instruction21s.getNarrowLiteral())) {
                                        mutableImplementation.replaceInstruction(i,
                                                new BuilderInstruction31i(
                                                        Opcode.CONST,
                                                        minRegStart,
                                                        putMethod(nameFactory.nextName(),
                                                                builderInstruction.getOpcode(),
                                                                new ImmutableStringReference(NumberUtils.getLikelyFloat(instruction21s.getNarrowLiteral())),
                                                                parameters,
                                                                "F")));
                                        if (minRegStart > 15) {
                                            mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction3rc(
                                                    Opcode.INVOKE_STATIC_RANGE,
                                                    minRegStart,
                                                    1,
                                                    new ImmutableMethodReference(type,
                                                            methodName,
                                                            parameters,
                                                            "F")));
                                        } else {
                                            mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction35c(
                                                    Opcode.INVOKE_STATIC,
                                                    1,
                                                    minRegStart,
                                                    0,
                                                    0,
                                                    0,
                                                    0,
                                                    new ImmutableMethodReference(type,
                                                            methodName,
                                                            parameters,
                                                            "F")));
                                        }
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction11x(
                                                Opcode.MOVE_RESULT,
                                                instruction21s.getRegisterA()));
                                    }
                                    //int
                                    else {
                                        mutableImplementation.replaceInstruction(i,
                                                new BuilderInstruction31i(
                                                        Opcode.CONST,
                                                        instruction21s.getRegisterA(),
                                                        putMethod(nameFactory.nextName(),
                                                                builderInstruction.getOpcode(),
                                                                new ImmutableStringReference("" + instruction21s.getNarrowLiteral()),
                                                                parameters,
                                                                "I")));
                                        if (instruction21s.getRegisterA() > 15) {
                                            mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction3rc(
                                                    Opcode.INVOKE_STATIC_RANGE,
                                                    instruction21s.getRegisterA(),
                                                    1,
                                                    new ImmutableMethodReference(type,
                                                            methodName,
                                                            parameters,
                                                            "I")));
                                        } else {
                                            mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction35c(
                                                    Opcode.INVOKE_STATIC,
                                                    1,
                                                    instruction21s.getRegisterA(),
                                                    0,
                                                    0,
                                                    0,
                                                    0,
                                                    new ImmutableMethodReference(type,
                                                            methodName,
                                                            parameters,
                                                            "I")));
                                        }
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction11x(
                                                Opcode.MOVE_RESULT,
                                                instruction21s.getRegisterA()));
                                    }
                                }
                                break;
                                /*case CONST_WIDE: {
                                    Instruction51l instruction51l = (Instruction51l) builderInstruction;
                                    if (instruction51l.getWideLiteral() == 0)
                                        break;
                                    mutableImplementation.replaceInstruction(i,
                                            new BuilderInstruction31i(
                                                    Opcode.CONST,
                                                    minRegStart,
                                                    putMethod(nameFactory.nextName(),
                                                            builderInstruction.getOpcode(),
                                                            new ImmutableStringReference(
                                                                    NumberUtils.isLikelyDouble(
                                                                            instruction51l.getWideLiteral()) ?
                                                                            NumberUtils.getLikelyDouble(instruction51l.getWideLiteral()) : "" + instruction51l.getWideLiteral()),
                                                            parameters,
                                                            "D")));
                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction51l(
                                            Opcode.CONST_WIDE,
                                            instruction51l.getRegisterA(),
                                            0L
                                    ));
                                    if (minRegStart > 15) {
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction3rc(
                                                Opcode.INVOKE_STATIC_RANGE,
                                                minRegStart,
                                                1,
                                                new ImmutableMethodReference(type,
                                                        methodName,
                                                        parameters,
                                                        "D")));
                                    } else {
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction35c(
                                                Opcode.INVOKE_STATIC,
                                                1,
                                                minRegStart,
                                                0,
                                                0,
                                                0,
                                                0,
                                                new ImmutableMethodReference(type,
                                                        methodName,
                                                        parameters,
                                                        "D")));
                                    }
                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction11x(
                                            Opcode.MOVE_RESULT_WIDE,
                                            instruction51l.getRegisterA()));
                                }
                                break;
                                case CONST_WIDE_16: {
                                    Instruction21s instruction21s = (Instruction21s) builderInstruction;
                                    if (instruction21s.getNarrowLiteral() == 0)
                                        break;
                                    mutableImplementation.replaceInstruction(i,
                                            new BuilderInstruction31i(
                                                    Opcode.CONST,
                                                    minRegStart,
                                                    putMethod(nameFactory.nextName(),
                                                            builderInstruction.getOpcode(),
                                                            new ImmutableStringReference(
                                                                    NumberUtils.isLikelyDouble(instruction21s.getWideLiteral())
                                                                            ?
                                                                            NumberUtils.getLikelyDouble(instruction21s.getWideLiteral())
                                                                            :
                                                                            "" + instruction21s.getNarrowLiteral()
                                                            ),
                                                            parameters,
                                                            "J")));
                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction51l(
                                            Opcode.CONST_WIDE,
                                            instruction21s.getRegisterA(),
                                            0L
                                    ));
                                    if (minRegStart > 15) {
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction3rc(
                                                Opcode.INVOKE_STATIC_RANGE,
                                                minRegStart,
                                                1,
                                                new ImmutableMethodReference(type,
                                                        methodName,
                                                        parameters,
                                                        "J")));
                                    } else {
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction35c(
                                                Opcode.INVOKE_STATIC,
                                                1,
                                                minRegStart,
                                                0,
                                                0,
                                                0,
                                                0,
                                                new ImmutableMethodReference(type,
                                                        methodName,
                                                        parameters,
                                                        "J")));
                                    }
                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction11x(
                                            Opcode.MOVE_RESULT_WIDE,
                                            instruction21s.getRegisterA()));
                                }
                                break;
                                case CONST_WIDE_HIGH16: {
                                    Instruction21lh instruction21lh = (Instruction21lh) builderInstruction;
                                    if (instruction21lh.getWideLiteral() == 0)
                                        break;
                                    mutableImplementation.replaceInstruction(i,
                                            new BuilderInstruction31i(
                                                    Opcode.CONST,
                                                    minRegStart,
                                                    putMethod(nameFactory.nextName(),
                                                            builderInstruction.getOpcode(),
                                                            new ImmutableStringReference(
                                                                    NumberUtils.isLikelyDouble(instruction21lh.getWideLiteral())
                                                                            ?
                                                                            NumberUtils.getLikelyDouble(instruction21lh.getWideLiteral())
                                                                            :
                                                                            "" + instruction21lh.getWideLiteral()
                                                            ),
                                                            parameters,
                                                            "D")));
                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction51l(
                                            Opcode.CONST_WIDE,
                                            instruction21lh.getRegisterA(),
                                            0L
                                    ));
                                    if (minRegStart > 15) {
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction3rc(
                                                Opcode.INVOKE_STATIC_RANGE,
                                                minRegStart,
                                                1,
                                                new ImmutableMethodReference(type,
                                                        methodName,
                                                        parameters,
                                                        "D")));
                                    } else {
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction35c(
                                                Opcode.INVOKE_STATIC,
                                                1,
                                                minRegStart,
                                                0,
                                                0,
                                                0,
                                                0,
                                                new ImmutableMethodReference(type,
                                                        methodName,
                                                        parameters,
                                                        "D")));
                                    }
                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction11x(
                                            Opcode.MOVE_RESULT_WIDE,
                                            instruction21lh.getRegisterA()));
                                }
                                break;
                                case CONST_WIDE_32: {
                                    Instruction31i instruction31i = (Instruction31i) builderInstruction;
                                    if (instruction31i.getNarrowLiteral() == 0)
                                        break;
                                    mutableImplementation.replaceInstruction(i,
                                            new BuilderInstruction31i(
                                                    Opcode.CONST,
                                                    minRegStart,
                                                    putMethod(nameFactory.nextName(),
                                                            builderInstruction.getOpcode(),
                                                            new ImmutableStringReference(
                                                                    NumberUtils.isLikelyDouble(instruction31i.getWideLiteral())
                                                                            ?
                                                                            NumberUtils.getLikelyDouble(instruction31i.getWideLiteral())
                                                                            :
                                                                            "" + instruction31i.getNarrowLiteral()
                                                            ),
                                                            parameters,
                                                            "D")));
                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction51l(
                                            Opcode.CONST_WIDE,
                                            instruction31i.getRegisterA(),
                                            0L
                                    ));
                                    if (minRegStart > 15) {
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction3rc(
                                                Opcode.INVOKE_STATIC_RANGE,
                                                minRegStart,
                                                1,
                                                new ImmutableMethodReference(type,
                                                        methodName,
                                                        parameters,
                                                        "D")));
                                    } else {
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction35c(
                                                Opcode.INVOKE_STATIC,
                                                1,
                                                minRegStart,
                                                0,
                                                0,
                                                0,
                                                0,
                                                new ImmutableMethodReference(type,
                                                        methodName,
                                                        parameters,
                                                        "D")));
                                    }
                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction11x(
                                            Opcode.MOVE_RESULT_WIDE,
                                            instruction31i.getRegisterA()));
                                }
                                break;*/
                                case CONST_STRING_JUMBO: {
                                    Instruction31c instruction31c = (Instruction31c) builderInstruction;
                                    mutableImplementation.replaceInstruction(i, new BuilderInstruction31i(Opcode.CONST, minRegStart, putMethod(nameFactory.nextName(), Opcode.CONST_STRING_JUMBO, instruction31c.getReference(), parameters, "Ljava/lang/Object;")));
                                    if (minRegStart > 15) {
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction3rc(
                                                Opcode.INVOKE_STATIC_RANGE,
                                                minRegStart,
                                                1,
                                                new ImmutableMethodReference(type,
                                                        methodName,
                                                        parameters,
                                                        "Ljava/lang/Object;")));
                                    } else {
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction35c(
                                                Opcode.INVOKE_STATIC,
                                                1,
                                                minRegStart,
                                                0,
                                                0,
                                                0,
                                                0,
                                                new ImmutableMethodReference(type,
                                                        methodName,
                                                        parameters,
                                                        "Ljava/lang/Object;")));
                                    }
                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction11x(
                                            Opcode.MOVE_RESULT_OBJECT,
                                            instruction31c.getRegisterA()));
                                }
                                break;
                                case SGET:
                                case SGET_BYTE:
                                case SGET_CHAR:
                                case SGET_SHORT:
                                case SGET_BOOLEAN: {
                                    Instruction21c instruction21c = (Instruction21c) builderInstruction;
                                    FieldReference fieldReference = (FieldReference) instruction21c.getReference();
                                    mutableImplementation.replaceInstruction(i, new BuilderInstruction31i(Opcode.CONST, minRegStart, putMethod(nameFactory.nextName(), builderInstruction.getOpcode(), fieldReference, parameters, fieldReference.getType())));
                                    if (minRegStart > 15) {
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction3rc(
                                                Opcode.INVOKE_STATIC_RANGE,
                                                minRegStart,
                                                1,
                                                new ImmutableMethodReference(type,
                                                        methodName,
                                                        parameters,
                                                        fieldReference.getType())));
                                    } else {
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction35c(
                                                Opcode.INVOKE_STATIC,
                                                1,
                                                minRegStart,
                                                0,
                                                0,
                                                0,
                                                0,
                                                new ImmutableMethodReference(type,
                                                        methodName,
                                                        parameters,
                                                        fieldReference.getType())));
                                    }
                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction11x(
                                            Opcode.MOVE_RESULT,
                                            instruction21c.getRegisterA()));
                                }
                                break;
                                case SGET_WIDE: {
                                    Instruction21c instruction21c = (Instruction21c) builderInstruction;
                                    FieldReference fieldReference = (FieldReference) instruction21c.getReference();
                                    mutableImplementation.replaceInstruction(i, new BuilderInstruction31i(Opcode.CONST, minRegStart, putMethod(nameFactory.nextName(), builderInstruction.getOpcode(), fieldReference, parameters, fieldReference.getType())));
                                    if (minRegStart > 15) {
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction3rc(
                                                Opcode.INVOKE_STATIC_RANGE,
                                                minRegStart,
                                                1,
                                                new ImmutableMethodReference(type,
                                                        methodName,
                                                        parameters,
                                                        fieldReference.getType())));
                                    } else {
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction35c(
                                                Opcode.INVOKE_STATIC,
                                                1,
                                                minRegStart,
                                                0,
                                                0,
                                                0,
                                                0,
                                                new ImmutableMethodReference(type,
                                                        methodName,
                                                        parameters,
                                                        fieldReference.getType())));
                                    }
                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction11x(
                                            Opcode.MOVE_RESULT_WIDE,
                                            instruction21c.getRegisterA()));
                                }
                                break;
                                case SGET_OBJECT: {
                                    Instruction21c instruction21c = (Instruction21c) builderInstruction;
                                    FieldReference fieldReference = (FieldReference) instruction21c.getReference();
                                    mutableImplementation.replaceInstruction(i, new BuilderInstruction31i(Opcode.CONST, minRegStart, putMethod(nameFactory.nextName(), builderInstruction.getOpcode(), fieldReference, parameters, MethodHelper.getResultType(fieldReference.getType()))));
                                    if (minRegStart > 15) {
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction3rc(
                                                Opcode.INVOKE_STATIC_RANGE,
                                                minRegStart,
                                                1,
                                                new ImmutableMethodReference(type,
                                                        methodName,
                                                        parameters,
                                                        MethodHelper.getResultType(fieldReference.getType()))));
                                    } else {
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction35c(
                                                Opcode.INVOKE_STATIC,
                                                1,
                                                minRegStart,
                                                0,
                                                0,
                                                0,
                                                0,
                                                new ImmutableMethodReference(type,
                                                        methodName,
                                                        parameters,
                                                        MethodHelper.getResultType(fieldReference.getType()))));
                                    }
                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction11x(
                                            Opcode.MOVE_RESULT_OBJECT,
                                            instruction21c.getRegisterA()));
                                }
                                break;
                                case SPUT:
                                case SPUT_BYTE:
                                case SPUT_CHAR:
                                case SPUT_SHORT:
                                case SPUT_BOOLEAN: {
                                    Instruction21c instruction21c = (Instruction21c) builderInstruction;
                                    FieldReference fieldReference = (FieldReference) instruction21c.getReference();
                                    parameters.add(fieldReference.getType());
                                    mutableImplementation.replaceInstruction(i, new BuilderInstruction31i(Opcode.CONST, minRegStart, putMethod(nameFactory.nextName(), builderInstruction.getOpcode(), fieldReference, parameters, "V")));
                                    if (minRegStart > 15) {
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_FROM16, minRegStart + 1, instruction21c.getRegisterA()));
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction3rc(
                                                Opcode.INVOKE_STATIC_RANGE,
                                                minRegStart,
                                                2,
                                                new ImmutableMethodReference(type,
                                                        methodName,
                                                        parameters,
                                                        "V")));
                                    } else {
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction35c(
                                                Opcode.INVOKE_STATIC,
                                                2,
                                                minRegStart,
                                                instruction21c.getRegisterA(),
                                                0,
                                                0,
                                                0,
                                                new ImmutableMethodReference(type,
                                                        methodName,
                                                        parameters,
                                                        "V")));
                                    }
                                }
                                break;
                                case SPUT_OBJECT: {
                                    Instruction21c instruction21c = (Instruction21c) builderInstruction;
                                    FieldReference fieldReference = (FieldReference) instruction21c.getReference();
                                    parameters.add(MethodHelper.getResultType(fieldReference.getType()));
                                    mutableImplementation.replaceInstruction(i, new BuilderInstruction31i(Opcode.CONST, minRegStart, putMethod(nameFactory.nextName(), builderInstruction.getOpcode(), fieldReference, parameters, "V")));
                                    if (minRegStart > 15) {
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_OBJECT_FROM16, minRegStart + 1, instruction21c.getRegisterA()));
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction3rc(
                                                Opcode.INVOKE_STATIC_RANGE,
                                                minRegStart,
                                                2,
                                                new ImmutableMethodReference(type,
                                                        methodName,
                                                        parameters,
                                                        "V")));
                                    } else {
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction35c(
                                                Opcode.INVOKE_STATIC,
                                                2,
                                                minRegStart,
                                                instruction21c.getRegisterA(),
                                                0,
                                                0,
                                                0,
                                                new ImmutableMethodReference(type,
                                                        methodName,
                                                        parameters,
                                                        "V")));
                                    }
                                }
                                break;
                                case IGET:
                                case IGET_BOOLEAN:
                                case IGET_BYTE:
                                case IGET_CHAR:
                                case IGET_SHORT: {
                                    Instruction22c instruction22c = (Instruction22c) builderInstruction;
                                    FieldReference fieldReference = (FieldReference) instruction22c.getReference();
                                    parameters.add("Ljava/lang/Object;");
                                    mutableImplementation.replaceInstruction(i, new BuilderInstruction31i(Opcode.CONST, minRegStart, putMethod(nameFactory.nextName(), builderInstruction.getOpcode(), fieldReference, parameters, fieldReference.getType())));
                                    if (minRegStart > 15) {
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_OBJECT_FROM16, minRegStart + 1, instruction22c.getRegisterB()));
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction3rc(
                                                Opcode.INVOKE_STATIC_RANGE,
                                                minRegStart,
                                                2,
                                                new ImmutableMethodReference(type,
                                                        methodName,
                                                        parameters,
                                                        fieldReference.getType())));
                                    } else {
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction35c(
                                                Opcode.INVOKE_STATIC,
                                                2,
                                                minRegStart,
                                                instruction22c.getRegisterB(),
                                                0,
                                                0,
                                                0,
                                                new ImmutableMethodReference(type,
                                                        methodName,
                                                        parameters,
                                                        fieldReference.getType())));
                                    }
                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction11x(
                                            Opcode.MOVE_RESULT,
                                            instruction22c.getRegisterA()));
                                }
                                break;
                                case IGET_WIDE: {
                                    Instruction22c instruction22c = (Instruction22c) builderInstruction;
                                    FieldReference fieldReference = (FieldReference) instruction22c.getReference();
                                    parameters.add("Ljava/lang/Object;");
                                    mutableImplementation.replaceInstruction(i, new BuilderInstruction31i(Opcode.CONST, minRegStart, putMethod(nameFactory.nextName(), builderInstruction.getOpcode(), fieldReference, parameters, fieldReference.getType())));
                                    if (minRegStart > 15) {
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_OBJECT_FROM16, minRegStart + 1, instruction22c.getRegisterB()));
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction3rc(
                                                Opcode.INVOKE_STATIC_RANGE,
                                                minRegStart,
                                                2,
                                                new ImmutableMethodReference(type,
                                                        methodName,
                                                        parameters,
                                                        fieldReference.getType())));
                                    } else {
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction35c(
                                                Opcode.INVOKE_STATIC,
                                                2,
                                                minRegStart,
                                                instruction22c.getRegisterB(),
                                                0,
                                                0,
                                                0,
                                                new ImmutableMethodReference(type,
                                                        methodName,
                                                        parameters,
                                                        fieldReference.getType())));
                                    }
                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction11x(
                                            Opcode.MOVE_RESULT_WIDE,
                                            instruction22c.getRegisterA()));
                                }
                                break;
                                case IGET_OBJECT: {
                                    Instruction22c instruction22c = (Instruction22c) builderInstruction;
                                    FieldReference fieldReference = (FieldReference) instruction22c.getReference();
                                    parameters.add("Ljava/lang/Object;");
                                    mutableImplementation.replaceInstruction(i, new BuilderInstruction31i(Opcode.CONST, minRegStart, putMethod(nameFactory.nextName(), builderInstruction.getOpcode(), fieldReference, parameters, MethodHelper.getResultType(fieldReference.getType()))));
                                    if (minRegStart > 15) {
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_OBJECT_FROM16, minRegStart + 1, instruction22c.getRegisterB()));
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction3rc(
                                                Opcode.INVOKE_STATIC_RANGE,
                                                minRegStart,
                                                2,
                                                new ImmutableMethodReference(type,
                                                        methodName,
                                                        parameters,
                                                        MethodHelper.getResultType(fieldReference.getType()))));
                                    } else {
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction35c(
                                                Opcode.INVOKE_STATIC,
                                                2,
                                                minRegStart,
                                                instruction22c.getRegisterB(),
                                                0,
                                                0,
                                                0,
                                                new ImmutableMethodReference(type,
                                                        methodName,
                                                        parameters,
                                                        MethodHelper.getResultType(fieldReference.getType()))));
                                    }
                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction11x(
                                            Opcode.MOVE_RESULT_OBJECT,
                                            instruction22c.getRegisterA()));
                                }
                                break;
                                case IPUT:
                                case IPUT_BOOLEAN:
                                case IPUT_BYTE:
                                case IPUT_CHAR:
                                case IPUT_SHORT: {
                                    Instruction22c instruction22c = (Instruction22c) builderInstruction;
                                    FieldReference fieldReference = (FieldReference) instruction22c.getReference();
                                    parameters.add("Ljava/lang/Object;");
                                    parameters.add(fieldReference.getType());
                                    mutableImplementation.replaceInstruction(i, new BuilderInstruction31i(Opcode.CONST, minRegStart, putMethod(nameFactory.nextName(), builderInstruction.getOpcode(), fieldReference, parameters, "V")));
                                    if (minRegStart > 15) {
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_OBJECT_FROM16, minRegStart + 1, instruction22c.getRegisterB()));
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_FROM16, minRegStart + 2, instruction22c.getRegisterA()));
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction3rc(
                                                Opcode.INVOKE_STATIC_RANGE,
                                                minRegStart,
                                                3,
                                                new ImmutableMethodReference(type,
                                                        methodName,
                                                        parameters,
                                                        "V")));
                                    } else {
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction35c(
                                                Opcode.INVOKE_STATIC,
                                                3,
                                                minRegStart,
                                                instruction22c.getRegisterB(),
                                                instruction22c.getRegisterA(),
                                                0,
                                                0,
                                                new ImmutableMethodReference(type,
                                                        methodName,
                                                        parameters,
                                                        "V")));
                                    }
                                }
                                break;
                                case IPUT_WIDE: {
                                    Instruction22c instruction22c = (Instruction22c) builderInstruction;
                                    FieldReference fieldReference = (FieldReference) instruction22c.getReference();
                                    mutableImplementation.replaceInstruction(i, new BuilderInstruction21c(
                                            Opcode.CHECK_CAST,
                                            instruction22c.getRegisterB(), new ImmutableTypeReference(fieldReference.getDefiningClass())));
                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22c(
                                            Opcode.IPUT_WIDE,
                                            instruction22c.getRegisterA(),
                                            instruction22c.getRegisterB(),
                                            fieldReference));
                                }
                                break;
                                case IPUT_OBJECT: {
                                    Instruction22c instruction22c = (Instruction22c) builderInstruction;
                                    FieldReference fieldReference = (FieldReference) instruction22c.getReference();
                                    parameters.add("Ljava/lang/Object;");
                                    parameters.add(MethodHelper.getResultType(fieldReference.getType()));
                                    mutableImplementation.replaceInstruction(i, new BuilderInstruction31i(Opcode.CONST, minRegStart, putMethod(nameFactory.nextName(), builderInstruction.getOpcode(), fieldReference, parameters, "V")));
                                    if (minRegStart > 15) {
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_OBJECT_FROM16, minRegStart + 1, instruction22c.getRegisterB()));
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_OBJECT_FROM16, minRegStart + 2, instruction22c.getRegisterA()));
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction3rc(
                                                Opcode.INVOKE_STATIC_RANGE,
                                                minRegStart,
                                                3,
                                                new ImmutableMethodReference(type,
                                                        methodName,
                                                        parameters,
                                                        "V")));
                                    } else {
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction35c(
                                                Opcode.INVOKE_STATIC,
                                                3,
                                                minRegStart,
                                                instruction22c.getRegisterB(),
                                                instruction22c.getRegisterA(),
                                                0,
                                                0,
                                                new ImmutableMethodReference(type,
                                                        methodName,
                                                        parameters,
                                                        "V")));
                                    }
                                }
                                break;
                                case APUT_SHORT: {
                                    Instruction23x instruction23x = (Instruction23x) builderInstruction;
                                    parameters.add("[S");
                                    parameters.add("S");
                                    parameters.add("I");
                                    mutableImplementation.replaceInstruction(i, new BuilderInstruction31i(Opcode.CONST, minRegStart, putMethod(nameFactory.nextName(), builderInstruction.getOpcode(), new ImmutableStringReference(""), parameters, "V")));
                                    if (minRegStart > 15) {
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_OBJECT_FROM16, minRegStart + 1, instruction23x.getRegisterB()));
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_FROM16, minRegStart + 2, instruction23x.getRegisterA()));
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_FROM16, minRegStart + 3, instruction23x.getRegisterC()));
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction3rc(
                                                Opcode.INVOKE_STATIC_RANGE,
                                                minRegStart,
                                                4,
                                                new ImmutableMethodReference(type,
                                                        methodName,
                                                        parameters,
                                                        "V")));
                                    } else {
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction35c(
                                                Opcode.INVOKE_STATIC,
                                                4,
                                                minRegStart,
                                                instruction23x.getRegisterB(),
                                                instruction23x.getRegisterA(),
                                                instruction23x.getRegisterC(),
                                                0,
                                                new ImmutableMethodReference(type,
                                                        methodName,
                                                        parameters,
                                                        "V")));
                                    }
                                }
                                break;
                                case APUT_BOOLEAN: {
                                    Instruction23x instruction23x = (Instruction23x) builderInstruction;
                                    parameters.add("[Z");
                                    parameters.add("Z");
                                    parameters.add("I");
                                    mutableImplementation.replaceInstruction(i, new BuilderInstruction31i(Opcode.CONST, minRegStart, putMethod(nameFactory.nextName(), builderInstruction.getOpcode(), new ImmutableStringReference(""), parameters, "V")));
                                    if (minRegStart > 15) {
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_OBJECT_FROM16, minRegStart + 1, instruction23x.getRegisterB()));
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_FROM16, minRegStart + 2, instruction23x.getRegisterA()));
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_FROM16, minRegStart + 3, instruction23x.getRegisterC()));
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction3rc(
                                                Opcode.INVOKE_STATIC_RANGE,
                                                minRegStart,
                                                4,
                                                new ImmutableMethodReference(type,
                                                        methodName,
                                                        parameters,
                                                        "V")));
                                    } else {
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction35c(
                                                Opcode.INVOKE_STATIC,
                                                4,
                                                minRegStart,
                                                instruction23x.getRegisterB(),
                                                instruction23x.getRegisterA(),
                                                instruction23x.getRegisterC(),
                                                0,
                                                new ImmutableMethodReference(type,
                                                        methodName,
                                                        parameters,
                                                        "V")));
                                    }
                                }
                                break;
                                case APUT_CHAR: {
                                    Instruction23x instruction23x = (Instruction23x) builderInstruction;
                                    parameters.add("[C");
                                    parameters.add("C");
                                    parameters.add("I");
                                    mutableImplementation.replaceInstruction(i, new BuilderInstruction31i(Opcode.CONST, minRegStart, putMethod(nameFactory.nextName(), builderInstruction.getOpcode(), new ImmutableStringReference(""), parameters, "V")));
                                    if (minRegStart > 15) {
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_OBJECT_FROM16, minRegStart + 1, instruction23x.getRegisterB()));
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_FROM16, minRegStart + 2, instruction23x.getRegisterA()));
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_FROM16, minRegStart + 3, instruction23x.getRegisterC()));
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction3rc(
                                                Opcode.INVOKE_STATIC_RANGE,
                                                minRegStart,
                                                4,
                                                new ImmutableMethodReference(type,
                                                        methodName,
                                                        parameters,
                                                        "V")));
                                    } else {
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction35c(
                                                Opcode.INVOKE_STATIC,
                                                4,
                                                minRegStart,
                                                instruction23x.getRegisterB(),
                                                instruction23x.getRegisterA(),
                                                instruction23x.getRegisterC(),
                                                0,
                                                new ImmutableMethodReference(type,
                                                        methodName,
                                                        parameters,
                                                        "V")));
                                    }
                                }
                                break;
                                case APUT_BYTE: {
                                    Instruction23x instruction23x = (Instruction23x) builderInstruction;
                                    parameters.add("[B");
                                    parameters.add("B");
                                    parameters.add("I");
                                    mutableImplementation.replaceInstruction(i, new BuilderInstruction31i(Opcode.CONST, minRegStart, putMethod(nameFactory.nextName(), builderInstruction.getOpcode(), new ImmutableStringReference(""), parameters, "V")));
                                    if (minRegStart > 15) {
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_OBJECT_FROM16, minRegStart + 1, instruction23x.getRegisterB()));
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_FROM16, minRegStart + 2, instruction23x.getRegisterA()));
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_FROM16, minRegStart + 3, instruction23x.getRegisterC()));
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction3rc(
                                                Opcode.INVOKE_STATIC_RANGE,
                                                minRegStart,
                                                4,
                                                new ImmutableMethodReference(type,
                                                        methodName,
                                                        parameters,
                                                        "V")));
                                    } else {
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction35c(
                                                Opcode.INVOKE_STATIC,
                                                4,
                                                minRegStart,
                                                instruction23x.getRegisterB(),
                                                instruction23x.getRegisterA(),
                                                instruction23x.getRegisterC(),
                                                0,
                                                new ImmutableMethodReference(type,
                                                        methodName,
                                                        parameters,
                                                        "V")));
                                    }
                                }
                                break;
                                case APUT_OBJECT: {
                                    Instruction23x instruction23x = (Instruction23x) builderInstruction;
                                    parameters.add("[Ljava/lang/Object;");
                                    parameters.add("Ljava/lang/Object;");
                                    parameters.add("I");
                                    mutableImplementation.replaceInstruction(i, new BuilderInstruction31i(Opcode.CONST, minRegStart, putMethod(nameFactory.nextName(), builderInstruction.getOpcode(), new ImmutableStringReference(""), parameters, "V")));
                                    if (minRegStart > 15) {
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_OBJECT_FROM16, minRegStart + 1, instruction23x.getRegisterB()));
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_OBJECT_FROM16, minRegStart + 2, instruction23x.getRegisterA()));
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_FROM16, minRegStart + 3, instruction23x.getRegisterC()));
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction3rc(
                                                Opcode.INVOKE_STATIC_RANGE,
                                                minRegStart,
                                                4,
                                                new ImmutableMethodReference(type,
                                                        methodName,
                                                        parameters,
                                                        "V")));
                                    } else {
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction35c(
                                                Opcode.INVOKE_STATIC,
                                                4,
                                                minRegStart,
                                                instruction23x.getRegisterB(),
                                                instruction23x.getRegisterA(),
                                                instruction23x.getRegisterC(),
                                                0,
                                                new ImmutableMethodReference(type,
                                                        methodName,
                                                        parameters,
                                                        "V")));
                                    }
                                }
                                break;
                                case AGET_BOOLEAN: {
                                    Instruction23x instruction23x = (Instruction23x) builderInstruction;
                                    parameters.add("[Z");
                                    parameters.add("I");
                                    mutableImplementation.replaceInstruction(i, new BuilderInstruction31i(Opcode.CONST, minRegStart, putMethod(nameFactory.nextName(), builderInstruction.getOpcode(), new ImmutableStringReference(""), parameters, "Z")));
                                    if (minRegStart > 15) {
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_OBJECT_FROM16, minRegStart + 1, instruction23x.getRegisterB()));
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_FROM16, minRegStart + 2, instruction23x.getRegisterC()));
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction3rc(
                                                Opcode.INVOKE_STATIC_RANGE,
                                                minRegStart,
                                                3,
                                                new ImmutableMethodReference(type,
                                                        methodName,
                                                        parameters,
                                                        "Z")));
                                    } else {
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction35c(
                                                Opcode.INVOKE_STATIC,
                                                3,
                                                minRegStart,
                                                instruction23x.getRegisterB(),
                                                instruction23x.getRegisterC(),
                                                0,
                                                0,
                                                new ImmutableMethodReference(type,
                                                        methodName,
                                                        parameters,
                                                        "Z")));
                                    }
                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction11x(
                                            Opcode.MOVE_RESULT,
                                            instruction23x.getRegisterA()));
                                }
                                break;
                                case AGET_SHORT: {
                                    Instruction23x instruction23x = (Instruction23x) builderInstruction;
                                    parameters.add("[S");
                                    parameters.add("I");
                                    mutableImplementation.replaceInstruction(i, new BuilderInstruction31i(Opcode.CONST, minRegStart, putMethod(nameFactory.nextName(), builderInstruction.getOpcode(), new ImmutableStringReference(""), parameters, "S")));
                                    if (minRegStart > 15) {
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_OBJECT_FROM16, minRegStart + 1, instruction23x.getRegisterB()));
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_FROM16, minRegStart + 2, instruction23x.getRegisterC()));
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction3rc(
                                                Opcode.INVOKE_STATIC_RANGE,
                                                minRegStart,
                                                3,
                                                new ImmutableMethodReference(type,
                                                        methodName,
                                                        parameters,
                                                        "S")));
                                    } else {
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction35c(
                                                Opcode.INVOKE_STATIC,
                                                3,
                                                minRegStart,
                                                instruction23x.getRegisterB(),
                                                instruction23x.getRegisterC(),
                                                0,
                                                0,
                                                new ImmutableMethodReference(type,
                                                        methodName,
                                                        parameters,
                                                        "S")));
                                    }
                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction11x(
                                            Opcode.MOVE_RESULT,
                                            instruction23x.getRegisterA()));
                                }
                                break;
                                case AGET_CHAR: {
                                    Instruction23x instruction23x = (Instruction23x) builderInstruction;
                                    parameters.add("[C");
                                    parameters.add("I");
                                    mutableImplementation.replaceInstruction(i, new BuilderInstruction31i(Opcode.CONST, minRegStart, putMethod(nameFactory.nextName(), builderInstruction.getOpcode(), new ImmutableStringReference(""), parameters, "C")));
                                    if (minRegStart > 15) {
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_OBJECT_FROM16, minRegStart + 1, instruction23x.getRegisterB()));
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_FROM16, minRegStart + 2, instruction23x.getRegisterC()));
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction3rc(
                                                Opcode.INVOKE_STATIC_RANGE,
                                                minRegStart,
                                                3,
                                                new ImmutableMethodReference(type,
                                                        methodName,
                                                        parameters,
                                                        "C")));
                                    } else {
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction35c(
                                                Opcode.INVOKE_STATIC,
                                                3,
                                                minRegStart,
                                                instruction23x.getRegisterB(),
                                                instruction23x.getRegisterC(),
                                                0,
                                                0,
                                                new ImmutableMethodReference(type,
                                                        methodName,
                                                        parameters,
                                                        "C")));
                                    }
                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction11x(
                                            Opcode.MOVE_RESULT,
                                            instruction23x.getRegisterA()));
                                }
                                break;
                                case AGET_BYTE: {
                                    Instruction23x instruction23x = (Instruction23x) builderInstruction;
                                    parameters.add("[B");
                                    parameters.add("I");
                                    mutableImplementation.replaceInstruction(i, new BuilderInstruction31i(Opcode.CONST, minRegStart, putMethod(nameFactory.nextName(), builderInstruction.getOpcode(), new ImmutableStringReference(""), parameters, "B")));
                                    if (minRegStart > 15) {
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_OBJECT_FROM16, minRegStart + 1, instruction23x.getRegisterB()));
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_FROM16, minRegStart + 2, instruction23x.getRegisterC()));
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction3rc(
                                                Opcode.INVOKE_STATIC_RANGE,
                                                minRegStart,
                                                3,
                                                new ImmutableMethodReference(type,
                                                        methodName,
                                                        parameters,
                                                        "B")));
                                    } else {
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction35c(
                                                Opcode.INVOKE_STATIC,
                                                3,
                                                minRegStart,
                                                instruction23x.getRegisterB(),
                                                instruction23x.getRegisterC(),
                                                0,
                                                0,
                                                new ImmutableMethodReference(type,
                                                        methodName,
                                                        parameters,
                                                        "B")));
                                    }
                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction11x(
                                            Opcode.MOVE_RESULT,
                                            instruction23x.getRegisterA()));
                                }
                                break;
                                case ADD_INT_LIT8:
                                case RSUB_INT_LIT8:
                                case MUL_INT_LIT8:
                                case DIV_INT_LIT8:
                                case REM_INT_LIT8:
                                case AND_INT_LIT8:
                                case OR_INT_LIT8:
                                case XOR_INT_LIT8:
                                case SHL_INT_LIT8:
                                case SHR_INT_LIT8:
                                case USHR_INT_LIT8: {
                                    Instruction22b instruction22b = (Instruction22b) builderInstruction;
                                    parameters.add("I");
                                    mutableImplementation.replaceInstruction(i, new BuilderInstruction31i(Opcode.CONST, minRegStart, putMethod(
                                            nameFactory.nextName(),
                                            builderInstruction.getOpcode(),
                                            new ImmutableStringReference("" + instruction22b.getNarrowLiteral()),
                                            parameters,
                                            "I")));
                                    if (minRegStart > 15) {
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_FROM16, minRegStart + 1, instruction22b.getRegisterB()));
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction3rc(
                                                Opcode.INVOKE_STATIC_RANGE,
                                                minRegStart,
                                                2,
                                                new ImmutableMethodReference(type,
                                                        methodName,
                                                        parameters,
                                                        "I")));
                                    } else {
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction35c(
                                                Opcode.INVOKE_STATIC,
                                                2,
                                                minRegStart,
                                                instruction22b.getRegisterB(),
                                                0,
                                                0,
                                                0,
                                                new ImmutableMethodReference(type,
                                                        methodName,
                                                        parameters,
                                                        "I")));
                                    }
                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction11x(
                                            Opcode.MOVE_RESULT,
                                            instruction22b.getRegisterA()));
                                }
                                break;
                                case ADD_INT_LIT16:
                                case RSUB_INT:
                                case MUL_INT_LIT16:
                                case DIV_INT_LIT16:
                                case REM_INT_LIT16:
                                case AND_INT_LIT16:
                                case OR_INT_LIT16:
                                case XOR_INT_LIT16: {
                                    Instruction22s instruction22s = (Instruction22s) builderInstruction;
                                    parameters.add("I");
                                    mutableImplementation.replaceInstruction(i, new BuilderInstruction31i(Opcode.CONST, minRegStart, putMethod(
                                            nameFactory.nextName(),
                                            builderInstruction.getOpcode(),
                                            new ImmutableStringReference("" + instruction22s.getNarrowLiteral()),
                                            parameters,
                                            "I")));
                                    if (minRegStart > 15) {
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_FROM16, minRegStart + 1, instruction22s.getRegisterB()));
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction3rc(
                                                Opcode.INVOKE_STATIC_RANGE,
                                                minRegStart,
                                                2,
                                                new ImmutableMethodReference(type,
                                                        methodName,
                                                        parameters,
                                                        "I")));
                                    } else {
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction35c(
                                                Opcode.INVOKE_STATIC,
                                                2,
                                                minRegStart,
                                                instruction22s.getRegisterB(),
                                                0,
                                                0,
                                                0,
                                                new ImmutableMethodReference(type,
                                                        methodName,
                                                        parameters,
                                                        "I")));
                                    }
                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction11x(
                                            Opcode.MOVE_RESULT,
                                            instruction22s.getRegisterA()));
                                }
                                break;
                                case SUB_INT:
                                case MUL_INT:
                                case DIV_INT:
                                case REM_INT:
                                case AND_INT:
                                case OR_INT:
                                case XOR_INT:
                                case SHL_INT:
                                case SHR_INT:
                                case USHR_INT:
                                case ADD_INT: {
                                    Instruction23x instruction23x = (Instruction23x) builderInstruction;
                                    parameters.add("I");
                                    parameters.add("I");
                                    mutableImplementation.replaceInstruction(i, new BuilderInstruction31i(Opcode.CONST, minRegStart, putMethod(
                                            nameFactory.nextName(),
                                            builderInstruction.getOpcode(),
                                            new ImmutableStringReference(""),
                                            parameters,
                                            "I")));
                                    if (minRegStart > 15) {
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_FROM16, minRegStart + 1, instruction23x.getRegisterB()));
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_FROM16, minRegStart + 2, instruction23x.getRegisterC()));
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction3rc(
                                                Opcode.INVOKE_STATIC_RANGE,
                                                minRegStart,
                                                3,
                                                new ImmutableMethodReference(type,
                                                        methodName,
                                                        parameters,
                                                        "I")));
                                    } else {
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction35c(
                                                Opcode.INVOKE_STATIC,
                                                3,
                                                minRegStart,
                                                instruction23x.getRegisterB(),
                                                instruction23x.getRegisterC(),
                                                0,
                                                0,
                                                new ImmutableMethodReference(type,
                                                        methodName,
                                                        parameters,
                                                        "I")));
                                    }
                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction11x(
                                            Opcode.MOVE_RESULT,
                                            instruction23x.getRegisterA()));
                                }
                                break;
                                case ADD_INT_2ADDR:
                                case SUB_INT_2ADDR:
                                case MUL_INT_2ADDR:
                                case DIV_INT_2ADDR:
                                case REM_INT_2ADDR:
                                case AND_INT_2ADDR:
                                case OR_INT_2ADDR:
                                case XOR_INT_2ADDR:
                                case SHL_INT_2ADDR:
                                case SHR_INT_2ADDR:
                                case USHR_INT_2ADDR: {
                                    Instruction12x instruction12x = (Instruction12x) builderInstruction;
                                    parameters.add("I");
                                    parameters.add("I");
                                    mutableImplementation.replaceInstruction(i, new BuilderInstruction31i(Opcode.CONST, minRegStart, putMethod(
                                            nameFactory.nextName(),
                                            builderInstruction.getOpcode(),
                                            new ImmutableStringReference(""),
                                            parameters,
                                            "I")));
                                    if (minRegStart > 15) {
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_FROM16, minRegStart + 1, instruction12x.getRegisterA()));
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_FROM16, minRegStart + 2, instruction12x.getRegisterB()));
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction3rc(
                                                Opcode.INVOKE_STATIC_RANGE,
                                                minRegStart,
                                                3,
                                                new ImmutableMethodReference(type,
                                                        methodName,
                                                        parameters,
                                                        "I")));
                                    } else {
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction35c(
                                                Opcode.INVOKE_STATIC,
                                                3,
                                                minRegStart,
                                                instruction12x.getRegisterA(),
                                                instruction12x.getRegisterB(),
                                                0,
                                                0,
                                                new ImmutableMethodReference(type,
                                                        methodName,
                                                        parameters,
                                                        "I")));
                                    }
                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction11x(
                                            Opcode.MOVE_RESULT,
                                            instruction12x.getRegisterA()));
                                }
                                break;
                                case NEG_INT:
                                case NOT_INT: {
                                    Instruction12x instruction12x = (Instruction12x) builderInstruction;
                                    parameters.add("I");
                                    mutableImplementation.replaceInstruction(i, new BuilderInstruction31i(Opcode.CONST, minRegStart, putMethod(
                                            nameFactory.nextName(),
                                            builderInstruction.getOpcode(),
                                            new ImmutableStringReference(""),
                                            parameters,
                                            "I")));
                                    if (minRegStart > 15) {
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_FROM16, minRegStart + 1, instruction12x.getRegisterB()));
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction3rc(
                                                Opcode.INVOKE_STATIC_RANGE,
                                                minRegStart,
                                                2,
                                                new ImmutableMethodReference(type,
                                                        methodName,
                                                        parameters,
                                                        "I")));
                                    } else {
                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction35c(
                                                Opcode.INVOKE_STATIC,
                                                2,
                                                minRegStart,
                                                instruction12x.getRegisterB(),
                                                0,
                                                0,
                                                0,
                                                new ImmutableMethodReference(type,
                                                        methodName,
                                                        parameters,
                                                        "I")));
                                    }
                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction11x(
                                            Opcode.MOVE_RESULT,
                                            instruction12x.getRegisterA()));
                                }
                                break;
                            }
                        }
                        mutableImplementation.fixReg(addCount + 5 + mutableImplementation.getMethodReg(value), value);
                        methods.add(new ImmutableMethod(
                                value.getDefiningClass(),
                                value.getName(),
                                value.getParameters(),
                                value.getReturnType(),
                                value.getAccessFlags(),
                                value.getAnnotations(),
                                value.getHiddenApiRestrictions(),
                                new ImmutableMethodImplementation(
                                        mutableImplementation.getRegisterCount(),
                                        mutableImplementation.getInstructions(),
                                        mutableImplementation.getTryBlocks(),
                                        mutableImplementation.getDebugItems())));
                    } else
                        methods.add(value);
                }
                return new ImmutableClassDef(
                        classDef.getType(),
                        classDef.getAccessFlags(),
                        classDef.getSuperclass(),
                        classDef.getInterfaces(),
                        classDef.getSourceFile(),
                        classDef.getAnnotations(),
                        classDef.getFields(),
                        methods);
            }
        };
    }

    private int putMethod(String name, Opcode opcode, Reference reference, List<String> Parameters, String ReturnType) {
        for (CallMethod method : callMethods) {
            if (method.equalsParameter(Parameters) && method.equalsReturn(ReturnType)) {
                CallMethod.Result result = method.putMethod(this.index++, opcode, reference);
                if (result.isExist())
                    this.index--;
                return result.getIndex();
            }
        }
        CallMethod callMethod = new CallMethod(Parameters, ReturnType, name);
        callMethods.add(callMethod);
        return callMethod.putMethod(this.index++, opcode, reference).getIndex();
    }

    public int getIndex() {
        return index;
    }
}
