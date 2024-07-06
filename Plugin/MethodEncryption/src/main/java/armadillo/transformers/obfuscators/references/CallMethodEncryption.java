package armadillo.transformers.obfuscators.references;


import armadillo.transformers.base.BaseTransformer;
import armadillo.transformers.base.DexTransformer;
import armadillo.utils.StreamUtil;
import armadillo.utils.StringRandom;
import armadillo.utils.SysConfigUtil;
import armadillo.utils.axml.EditXml.decode.AXMLDoc;
import armadillo.utils.axml.EditXml.decode.BTagNode;
import armadillo.utils.axml.EditXml.decode.BXMLNode;
import armadillo.utils.axml.EditXml.utils.TypedValue;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.jf.dexlib2.AccessFlags;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.builder.MutableMethodImplementation;
import org.jf.dexlib2.builder.instruction.*;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.Field;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.MethodParameter;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.formats.Instruction35c;
import org.jf.dexlib2.iface.instruction.formats.Instruction3rc;
import org.jf.dexlib2.iface.reference.MethodReference;
import org.jf.dexlib2.immutable.ImmutableClassDef;
import org.jf.dexlib2.immutable.ImmutableField;
import org.jf.dexlib2.immutable.ImmutableMethod;
import org.jf.dexlib2.immutable.ImmutableMethodImplementation;
import org.jf.dexlib2.immutable.reference.ImmutableFieldReference;
import org.jf.dexlib2.immutable.reference.ImmutableMethodProtoReference;
import org.jf.dexlib2.immutable.reference.ImmutableMethodReference;
import org.jf.dexlib2.rewriter.Rewriter;
import org.jf.dexlib2.rewriter.Rewriters;

import javax.annotation.Nonnull;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.zip.ZipEntry;

public class CallMethodEncryption extends DexTransformer {
    private final List<String> types = new ArrayList<>();
    private final HashSet<String> diff = new HashSet<>();
    private final List<ImmutableMethodProtoReference> methodProtoReferences = new ArrayList<>();
    private int invoke_encrypt_total;

    @Nonnull
    @Override
    public Rewriter<ClassDef> getClassDefRewriter(@Nonnull Rewriters rewriters) {
        return new Rewriter<ClassDef>() {
            @Nonnull
            @Override
            public ClassDef rewrite(@Nonnull ClassDef classDef) {
                types.clear();
                if (diff.contains(classDef.getType())) {
                    Method clinit = null;
                    List<Method> newMethod = new ArrayList<>();
                    String fieldName = nameFactory.nextName();
                    String methodName = nameFactory.nextName();
                    for (Method method : classDef.getMethods()) {
                        if ("<clinit>".equals(method.getName()))
                            clinit = method;
                        else if (method.getImplementation() != null) {
                            MutableMethodImplementation mutableImplementation = new MutableMethodImplementation(method.getImplementation());
                            int minStartReg = mutableImplementation.getRegisterCount();
                            int addCount = 10;
                            for (int i = 0; i < mutableImplementation.getInstructions().size(); i++) {
                                Instruction builderInstruction = mutableImplementation.getInstructions().get(i);
                                switch (builderInstruction.getOpcode()) {
                                    case INVOKE_DIRECT:
                                    case INVOKE_VIRTUAL:
                                    case INVOKE_STATIC: {
                                        Instruction35c instruction35c = (Instruction35c) builderInstruction;
                                        MethodReference reference = (MethodReference) instruction35c.getReference();
                                        if (builderInstruction.getOpcode() == Opcode.INVOKE_DIRECT && reference.getName().equals("<init>"))
                                            break;
                                        StringBuilder type = new StringBuilder();
                                        type.append(dexToJavaName(reference.getDefiningClass()))
                                                .append(":");
                                        type.append(reference.getName())
                                                .append(":");
                                        type.append("(")
                                                .append(String.join("", reference.getParameterTypes()))
                                                .append(")")
                                                .append(reference.getReturnType())
                                                .append(":");
                                        switch (builderInstruction.getOpcode()) {
                                            case INVOKE_DIRECT:
                                            case INVOKE_VIRTUAL:
                                                type.append("virtual");
                                                break;
                                            case INVOKE_STATIC:
                                                type.append("static");
                                                break;
                                        }
                                        types.add(type.toString());
                                        int type_index = types.indexOf(type.toString());
                                        //const v0, 0x1
                                        {
                                            mutableImplementation.replaceInstruction(i, new BuilderInstruction31i(Opcode.CONST, minStartReg, type_index));
                                        }
                                        //sget-object v1, Larmadillo/call/test;->strings:[Ljava/lang/String;
                                        {
                                            mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction21c(
                                                    Opcode.SGET_OBJECT,
                                                    minStartReg + 1,
                                                    new ImmutableFieldReference(classDef.getType(),
                                                            fieldName,
                                                            "[Ljava/lang/String;")));
                                        }
                                        //invoke-static {}, Ljava/lang/invoke/MethodHandles;->lookup()Ljava/lang/invoke/MethodHandles$Lookup;
                                        {
                                            mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction3rc(
                                                    Opcode.INVOKE_STATIC_RANGE,
                                                    0,
                                                    0,
                                                    new ImmutableMethodReference("Ljava/lang/invoke/MethodHandles;",
                                                            "lookup",
                                                            null,
                                                            "Ljava/lang/invoke/MethodHandles$Lookup;")));
                                        }
                                        //move-result-object v2
                                        {
                                            mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction11x(Opcode.MOVE_RESULT_OBJECT, minStartReg + 2));
                                        }
                                        //invoke-static {v0, v1, v2}, Larmadillo/call/InvokeDynamic;->callSite(I[Ljava/lang/String;Ljava/lang/invoke/MethodHandles$Lookup;)Ljava/lang/invoke/CallSite;
                                        {
                                            mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction3rc(
                                                    Opcode.INVOKE_STATIC_RANGE,
                                                    minStartReg,
                                                    3,
                                                    new ImmutableMethodReference("Larmadillo/call/InvokeDynamic;",
                                                            "callSite",
                                                            Lists.newArrayList("I", "[Ljava/lang/String;", "Ljava/lang/invoke/MethodHandles$Lookup;"),
                                                            "Ljava/lang/invoke/CallSite;")));
                                        }
                                        //move-result-object v0
                                        {
                                            mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction11x(Opcode.MOVE_RESULT_OBJECT, minStartReg));
                                        }
                                        //invoke-virtual {v0}, Ljava/lang/invoke/CallSite;->dynamicInvoker()Ljava/lang/invoke/MethodHandle;
                                        {
                                            mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction3rc(
                                                    Opcode.INVOKE_VIRTUAL_RANGE,
                                                    minStartReg,
                                                    1,
                                                    new ImmutableMethodReference("Ljava/lang/invoke/CallSite;",
                                                            "dynamicInvoker",
                                                            null,
                                                            "Ljava/lang/invoke/MethodHandle;")));
                                        }
                                        //move-result-object v0
                                        {
                                            mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction11x(Opcode.MOVE_RESULT_OBJECT, minStartReg));
                                        }
                                        //invoke-polymorphic { v0, p0, v1 }, Ljava/lang/invoke/MethodHandle;->invoke([Ljava/lang/Object;)Ljava/lang/Object;, (Lcom/cloud/autosms/MainActivity;I)V
                                        {
                                            List<CharSequence> parameters = new ArrayList<>();
                                            if (builderInstruction.getOpcode() != Opcode.INVOKE_STATIC)
                                                parameters.add(reference.getDefiningClass());
                                            parameters.addAll(reference.getParameterTypes());
                                            ImmutableMethodProtoReference methodProtoReference = new ImmutableMethodProtoReference(parameters, reference.getReturnType());
                                            methodProtoReferences.add(methodProtoReference);
                                            if (instruction35c.getRegisterCount() == 5 || minStartReg > 15) {
                                                int src_add = 0;
                                                if (builderInstruction.getOpcode() != Opcode.INVOKE_STATIC) {
                                                    mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_OBJECT_FROM16, minStartReg + 1, MethodHelper.getInstruction35cRegisterIndex(instruction35c, 0)));
                                                    src_add++;
                                                }
                                                for (int index = 0; index < reference.getParameterTypes().size(); index++) {
                                                    switch (reference.getParameterTypes().get(index).toString()) {
                                                        case "Z":
                                                        case "B":
                                                        case "S":
                                                        case "C":
                                                        case "I":
                                                        case "F": {
                                                            mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_FROM16, minStartReg + index + 1 + src_add, MethodHelper.getInstruction35cRegisterIndex(instruction35c, index + src_add)));
                                                        }
                                                        break;
                                                        case "D":
                                                        case "J": {
                                                            mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_WIDE_FROM16, minStartReg + index + 1 + src_add, MethodHelper.getInstruction35cRegisterIndex(instruction35c, index + src_add)));
                                                            src_add++;
                                                        }
                                                        break;
                                                        default: {
                                                            mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_OBJECT_FROM16, minStartReg + index + 1 + src_add, MethodHelper.getInstruction35cRegisterIndex(instruction35c, index + src_add)));
                                                        }
                                                        break;
                                                    }
                                                }
                                                int RegSize = mutableImplementation.getReferenceRegSize(reference) + 1;
                                                if (addCount < RegSize)
                                                    addCount = RegSize;
                                                mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction4rcc(
                                                        Opcode.INVOKE_POLYMORPHIC_RANGE,
                                                        minStartReg,
                                                        instruction35c.getRegisterCount() + 1,
                                                        new ImmutableMethodReference("Ljava/lang/invoke/MethodHandle;",
                                                                "invoke",
                                                                Lists.newArrayList("[Ljava/lang/Object;"),
                                                                "Ljava/lang/Object;"),
                                                        methodProtoReference));
                                            } else
                                                mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction45cc(
                                                        Opcode.INVOKE_POLYMORPHIC,
                                                        instruction35c.getRegisterCount() + 1,
                                                        minStartReg,
                                                        instruction35c.getRegisterC(),
                                                        instruction35c.getRegisterD(),
                                                        instruction35c.getRegisterE(),
                                                        instruction35c.getRegisterF(),
                                                        new ImmutableMethodReference("Ljava/lang/invoke/MethodHandle;",
                                                                "invoke",
                                                                Lists.newArrayList("[Ljava/lang/Object;"),
                                                                "Ljava/lang/Object;"),
                                                        methodProtoReference));
                                        }
                                        invoke_encrypt_total++;
                                    }
                                    break;
                                    case INVOKE_DIRECT_RANGE:
                                    case INVOKE_VIRTUAL_RANGE:
                                    case INVOKE_STATIC_RANGE: {
                                        Instruction3rc instruction3rc = (Instruction3rc) builderInstruction;
                                        MethodReference reference = (MethodReference) instruction3rc.getReference();
                                        if (builderInstruction.getOpcode() == Opcode.INVOKE_DIRECT_RANGE && reference.getName().equals("<init>"))
                                            break;
                                        StringBuilder type = new StringBuilder();
                                        type.append(dexToJavaName(reference.getDefiningClass()))
                                                .append(":");
                                        type.append(reference.getName())
                                                .append(":");
                                        type.append("(")
                                                .append(String.join("", reference.getParameterTypes()))
                                                .append(")")
                                                .append(reference.getReturnType())
                                                .append(":");
                                        switch (builderInstruction.getOpcode()) {
                                            case INVOKE_DIRECT_RANGE:
                                            case INVOKE_VIRTUAL_RANGE:
                                                type.append("virtual");
                                                break;
                                            case INVOKE_STATIC_RANGE:
                                                type.append("static");
                                                break;
                                        }
                                        types.add(type.toString());
                                        int type_index = types.indexOf(type.toString());
                                        //const v0, 0x1
                                        {
                                            mutableImplementation.replaceInstruction(i, new BuilderInstruction31i(Opcode.CONST, minStartReg, type_index));
                                        }
                                        //sget-object v1, Larmadillo/call/test;->strings:[Ljava/lang/String;
                                        {
                                            mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction21c(
                                                    Opcode.SGET_OBJECT,
                                                    minStartReg + 1,
                                                    new ImmutableFieldReference(classDef.getType(),
                                                            fieldName,
                                                            "[Ljava/lang/String;")));
                                        }
                                        //invoke-static {}, Ljava/lang/invoke/MethodHandles;->lookup()Ljava/lang/invoke/MethodHandles$Lookup;
                                        {
                                            mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction3rc(
                                                    Opcode.INVOKE_STATIC_RANGE,
                                                    0,
                                                    0,
                                                    new ImmutableMethodReference("Ljava/lang/invoke/MethodHandles;",
                                                            "lookup",
                                                            null,
                                                            "Ljava/lang/invoke/MethodHandles$Lookup;")));
                                        }
                                        //move-result-object v2
                                        {
                                            mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction11x(Opcode.MOVE_RESULT_OBJECT, minStartReg + 2));
                                        }
                                        //invoke-static {v0, v1, v2}, Larmadillo/call/InvokeDynamic;->callSite(I[Ljava/lang/String;Ljava/lang/invoke/MethodHandles$Lookup;)Ljava/lang/invoke/CallSite;
                                        {
                                            mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction3rc(
                                                    Opcode.INVOKE_STATIC_RANGE,
                                                    minStartReg,
                                                    3,
                                                    new ImmutableMethodReference("Larmadillo/call/InvokeDynamic;",
                                                            "callSite",
                                                            Lists.newArrayList("I", "[Ljava/lang/String;", "Ljava/lang/invoke/MethodHandles$Lookup;"),
                                                            "Ljava/lang/invoke/CallSite;")));
                                        }
                                        //move-result-object v0
                                        {
                                            mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction11x(Opcode.MOVE_RESULT_OBJECT, minStartReg));
                                        }
                                        //invoke-virtual {v0}, Ljava/lang/invoke/CallSite;->dynamicInvoker()Ljava/lang/invoke/MethodHandle;
                                        {
                                            mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction3rc(
                                                    Opcode.INVOKE_VIRTUAL_RANGE,
                                                    minStartReg,
                                                    1,
                                                    new ImmutableMethodReference("Ljava/lang/invoke/CallSite;",
                                                            "dynamicInvoker",
                                                            null,
                                                            "Ljava/lang/invoke/MethodHandle;")));
                                        }
                                        //move-result-object v0
                                        {
                                            mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction11x(Opcode.MOVE_RESULT_OBJECT, minStartReg));
                                        }
                                        //invoke-polymorphic { v0, p0, v1 }, Ljava/lang/invoke/MethodHandle;->invoke([Ljava/lang/Object;)Ljava/lang/Object;, (Lcom/cloud/autosms/MainActivity;I)V
                                        {
                                            List<CharSequence> parameters = new ArrayList<>();
                                            if (builderInstruction.getOpcode() != Opcode.INVOKE_STATIC_RANGE)
                                                parameters.add(reference.getDefiningClass());
                                            parameters.addAll(reference.getParameterTypes());
                                            ImmutableMethodProtoReference methodProtoReference = new ImmutableMethodProtoReference(parameters, reference.getReturnType());
                                            methodProtoReferences.add(methodProtoReference);
                                            int src_add = 0;
                                            if (builderInstruction.getOpcode() != Opcode.INVOKE_STATIC_RANGE) {
                                                mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_OBJECT_FROM16, minStartReg + 1, instruction3rc.getStartRegister()));
                                                src_add++;
                                            }
                                            for (int index = 0; index < reference.getParameterTypes().size(); index++) {
                                                switch (reference.getParameterTypes().get(index).toString()) {
                                                    case "Z":
                                                    case "B":
                                                    case "S":
                                                    case "C":
                                                    case "I":
                                                    case "F": {
                                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_FROM16, minStartReg + index + 1 + src_add, instruction3rc.getStartRegister() + index + src_add));
                                                    }
                                                    break;
                                                    case "D":
                                                    case "J": {
                                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_WIDE_FROM16, minStartReg + index + 1 + src_add, instruction3rc.getStartRegister() + index + src_add));
                                                        src_add++;
                                                    }
                                                    break;
                                                    default: {
                                                        mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction22x(Opcode.MOVE_OBJECT_FROM16, minStartReg + index + 1 + src_add, instruction3rc.getStartRegister() + index + src_add));
                                                    }
                                                    break;
                                                }
                                            }
                                            int RegSize = mutableImplementation.getReferenceRegSize(reference) + 1;
                                            if (addCount < RegSize)
                                                addCount = RegSize;
                                            mutableImplementation.addInstruction(i++ + 1, new BuilderInstruction4rcc(
                                                    Opcode.INVOKE_POLYMORPHIC_RANGE,
                                                    minStartReg,
                                                    instruction3rc.getRegisterCount() + 1,
                                                    new ImmutableMethodReference("Ljava/lang/invoke/MethodHandle;",
                                                            "invoke",
                                                            Lists.newArrayList("[Ljava/lang/Object;"),
                                                            "Ljava/lang/Object;"),
                                                    methodProtoReference));
                                        }
                                        invoke_encrypt_total++;
                                    }
                                    break;
//                                        case INVOKE_VIRTUAL: {
//                                            Instruction35c instruction35c = (Instruction35c) builderInstruction;
//                                            MethodReference reference = (MethodReference) instruction35c.getReference();
//                                            StringBuilder type = new StringBuilder();
//                                            type.append(dexToJavaName(reference.getDefiningClass()))
//                                                    .append(":");
//                                            type.append(reference.getName())
//                                                    .append(":");
//                                            type.append("(")
//                                                    .append(String.join("", reference.getParameterTypes()))
//                                                    .append(")")
//                                                    .append(reference.getReturnType())
//                                                    .append(":");
//                                            //type.append(builderInstruction.getOpcode().name.replace("invoke-", ""));
//                                            type.append("virtual");
//                                            types.add(type.toString());
//                                            int type_index = types.indexOf(type.toString());
//                                            //const v0, 0x1
//                                            {
//                                                mutableImplementation.replaceInstruction(i, new BuilderInstruction31i(Opcode.CONST, minStartReg, type_index));
//                                            }
//                                            //sget-object v1, Larmadillo/call/test;->strings:[Ljava/lang/String;
//                                            {
//                                                i = i + 1;
//                                                mutableImplementation.addInstruction(i+++1, new BuilderInstruction21c(
//                                                        Opcode.SGET_OBJECT,
//                                                        maxIndex + 1,
//                                                        new ImmutableFieldReference(classDef.getType(),
//                                                                fieldName,
//                                                                "[Ljava/lang/String;")));
//                                            }
//                                            //invoke-static {}, Ljava/lang/invoke/MethodHandles;->lookup()Ljava/lang/invoke/MethodHandles$Lookup;
//                                            {
//                                                i = i + 1;
//                                                if (maxIndex > 15)
//                                                    mutableImplementation.addInstruction(i, new BuilderInstruction3rc(
//                                                            Opcode.INVOKE_STATIC_RANGE,
//                                                            0,
//                                                            0,
//                                                            new ImmutableMethodReference("Ljava/lang/invoke/MethodHandles;",
//                                                                    "lookup",
//                                                                    null,
//                                                                    "Ljava/lang/invoke/MethodHandles$Lookup;")));
//                                                else
//                                                    mutableImplementation.addInstruction(i, new BuilderInstruction35c(
//                                                            Opcode.INVOKE_STATIC,
//                                                            0,
//                                                            0,
//                                                            0,
//                                                            0,
//                                                            0,
//                                                            0,
//                                                            new ImmutableMethodReference("Ljava/lang/invoke/MethodHandles;",
//                                                                    "lookup",
//                                                                    null,
//                                                                    "Ljava/lang/invoke/MethodHandles$Lookup;")));
//                                            }
//                                            //move-result-object v0
//                                            {
//                                                i = i + 1;
//                                                mutableImplementation.addInstruction(i, new BuilderInstruction11x(Opcode.MOVE_RESULT_OBJECT, maxIndex + 2));
//                                            }
//                                            //invoke-static {v0, v1, v2}, Larmadillo/call/InvokeDynamic;->callSite(I[Ljava/lang/String;Ljava/lang/invoke/MethodHandles$Lookup;)Ljava/lang/invoke/CallSite;
//                                            {
//                                                i = i + 1;
//                                                if (maxIndex + 2 > 15)
//                                                    mutableImplementation.addInstruction(i, new BuilderInstruction3rc(
//                                                            Opcode.INVOKE_STATIC_RANGE,
//                                                            maxIndex,
//                                                            3,
//                                                            new ImmutableMethodReference("Larmadillo/call/InvokeDynamic;",
//                                                                    "callSite",
//                                                                    Lists.newArrayList("I", "[Ljava/lang/String;", "Ljava/lang/invoke/MethodHandles$Lookup;"),
//                                                                    "Ljava/lang/invoke/CallSite;")));
//                                                else
//                                                    mutableImplementation.addInstruction(i, new BuilderInstruction35c(
//                                                            Opcode.INVOKE_STATIC,
//                                                            3,
//                                                            maxIndex,
//                                                            maxIndex + 1,
//                                                            maxIndex + 2,
//                                                            0,
//                                                            0,
//                                                            new ImmutableMethodReference("Larmadillo/call/InvokeDynamic;",
//                                                                    "callSite",
//                                                                    Lists.newArrayList("I", "[Ljava/lang/String;", "Ljava/lang/invoke/MethodHandles$Lookup;"),
//                                                                    "Ljava/lang/invoke/CallSite;")));
//                                            }
//                                            //move-result-object v0
//                                            {
//                                                i = i + 1;
//                                                mutableImplementation.addInstruction(i, new BuilderInstruction11x(Opcode.MOVE_RESULT_OBJECT, maxIndex));
//                                            }
//                                            //invoke-virtual {v0}, Ljava/lang/invoke/CallSite;->dynamicInvoker()Ljava/lang/invoke/MethodHandle;
//                                            {
//                                                i = i + 1;
//                                                if (maxIndex > 15)
//                                                    mutableImplementation.addInstruction(i, new BuilderInstruction3rc(
//                                                            Opcode.INVOKE_VIRTUAL_RANGE,
//                                                            maxIndex,
//                                                            1,
//                                                            new ImmutableMethodReference("Ljava/lang/invoke/CallSite;",
//                                                                    "dynamicInvoker",
//                                                                    null,
//                                                                    "Ljava/lang/invoke/MethodHandle;")));
//                                                else
//                                                    mutableImplementation.addInstruction(i, new BuilderInstruction35c(
//                                                            Opcode.INVOKE_VIRTUAL,
//                                                            1,
//                                                            maxIndex,
//                                                            0,
//                                                            0,
//                                                            0,
//                                                            0,
//                                                            new ImmutableMethodReference("Ljava/lang/invoke/CallSite;",
//                                                                    "dynamicInvoker",
//                                                                    null,
//                                                                    "Ljava/lang/invoke/MethodHandle;")));
//                                            }
//                                            //move-result-object v0
//                                            {
//                                                i = i + 1;
//                                                mutableImplementation.addInstruction(i, new BuilderInstruction11x(Opcode.MOVE_RESULT_OBJECT, maxIndex));
//                                            }
//                                            //invoke-polymorphic { v0, p0, v1 }, Ljava/lang/invoke/MethodHandle;->invoke([Ljava/lang/Object;)Ljava/lang/Object;, (Lcom/cloud/autosms/MainActivity;I)V
//                                            {
//                                                i = i + 1;
//                                                List<CharSequence> parameters = new ArrayList<>();
//                                                if (builderInstruction35c.getRegisterCount() > 0) {
//                                                    //&& builderInstruction35c.getRegisterC() == mutableImplementation.getRegisterCount() - method.getParameters().size() - 1) {
//                                                    parameters.add(reference.getDefiningClass());
//                                                }
//                                                parameters.addAll(reference.getParameterTypes());
//                                                ImmutableMethodProtoReference methodProtoReference = new ImmutableMethodProtoReference(parameters, reference.getReturnType());
//                                                methodProtoReferences.add(methodProtoReference);
//                                                mutableImplementation.addInstruction(i, new BuilderInstruction45cc(
//                                                        Opcode.INVOKE_POLYMORPHIC,
//                                                        builderInstruction35c.getRegisterCount() + 1,
//                                                        maxIndex,
//                                                        builderInstruction35c.getRegisterC(),
//                                                        builderInstruction35c.getRegisterD(),
//                                                        builderInstruction35c.getRegisterE(),
//                                                        builderInstruction35c.getRegisterF(),
//                                                        new ImmutableMethodReference("Ljava/lang/invoke/MethodHandle;",
//                                                                "invoke",
//                                                                Lists.newArrayList("[Ljava/lang/Object;"),
//                                                                "Ljava/lang/Object;"),
//                                                        methodProtoReference));
//                                            }
//                                            invoke_encrypt_total++;
//                                        }
//                                        break;
//                                        case INVOKE_DIRECT: {
//                                            Instruction35c builderInstruction35c = (Instruction35c) builderInstruction;
//                                            MethodReference reference = (MethodReference) builderInstruction35c.getReference();
//                                            //TODO 构造函数不支持
//                                            if (reference.getName().equals("<init>"))
//                                                break;
//                                            if (builderInstruction35c.getRegisterCount() >= 5)
//                                                break;
//                                            if (maxIndex + 1 > 15)
//                                                break;
//                                            StringBuffer type = new StringBuffer();
//                                            type.append(dexToJavaName(reference.getDefiningClass()) + ":");
//                                            type.append(reference.getName() + ":");
//                                            type.append("(" + String.join("", reference.getParameterTypes()) + ")" + reference.getReturnType() + ":");
//                                            type.append("virtual");
//                                            types.add(type.toString());
//                                            int type_index = types.indexOf(type.toString());
//                                            //const/4 v0, 0x1
//                                            {
//                                                if (type_index > 7)
//                                                    mutableImplementation.replaceInstruction(i, new BuilderInstruction21s(Opcode.CONST_16, maxIndex, type_index));
//                                                else
//                                                    mutableImplementation.replaceInstruction(i, new BuilderInstruction11n(Opcode.CONST_4, maxIndex, type_index));
//                                            }
//                                            //sget-object v1, Larmadillo/call/test;->strings:[Ljava/lang/String;
//                                            {
//                                                i = i + 1;
//                                                mutableImplementation.addInstruction(i, new BuilderInstruction21c(
//                                                        Opcode.SGET_OBJECT,
//                                                        maxIndex + 1,
//                                                        new ImmutableFieldReference(classDef.getType(),
//                                                                fieldName,
//                                                                "[Ljava/lang/String;")));
//                                            }
//                                            //invoke-static {}, Ljava/lang/invoke/MethodHandles;->lookup()Ljava/lang/invoke/MethodHandles$Lookup;
//                                            {
//                                                i = i + 1;
//                                                if (maxIndex > 15)
//                                                    mutableImplementation.addInstruction(i, new BuilderInstruction3rc(
//                                                            Opcode.INVOKE_STATIC_RANGE,
//                                                            0,
//                                                            0,
//                                                            new ImmutableMethodReference("Ljava/lang/invoke/MethodHandles;",
//                                                                    "lookup",
//                                                                    null,
//                                                                    "Ljava/lang/invoke/MethodHandles$Lookup;")));
//                                                else
//                                                    mutableImplementation.addInstruction(i, new BuilderInstruction35c(
//                                                            Opcode.INVOKE_STATIC,
//                                                            0,
//                                                            0,
//                                                            0,
//                                                            0,
//                                                            0,
//                                                            0,
//                                                            new ImmutableMethodReference("Ljava/lang/invoke/MethodHandles;",
//                                                                    "lookup",
//                                                                    null,
//                                                                    "Ljava/lang/invoke/MethodHandles$Lookup;")));
//                                            }
//                                            //move-result-object v0
//                                            {
//                                                i = i + 1;
//                                                mutableImplementation.addInstruction(i, new BuilderInstruction11x(Opcode.MOVE_RESULT_OBJECT, maxIndex + 2));
//                                            }
//                                            //invoke-static {v0, v1, v2}, Larmadillo/call/InvokeDynamic;->callSite(I[Ljava/lang/String;Ljava/lang/invoke/MethodHandles$Lookup;)Ljava/lang/invoke/CallSite;
//                                            {
//                                                i = i + 1;
//                                                if (maxIndex + 2 > 15)
//                                                    mutableImplementation.addInstruction(i, new BuilderInstruction3rc(
//                                                            Opcode.INVOKE_STATIC_RANGE,
//                                                            maxIndex,
//                                                            3,
//                                                            new ImmutableMethodReference("Larmadillo/call/InvokeDynamic;",
//                                                                    "callSite",
//                                                                    Lists.newArrayList("I", "[Ljava/lang/String;", "Ljava/lang/invoke/MethodHandles$Lookup;"),
//                                                                    "Ljava/lang/invoke/CallSite;")));
//                                                else
//                                                    mutableImplementation.addInstruction(i, new BuilderInstruction35c(
//                                                            Opcode.INVOKE_STATIC,
//                                                            3,
//                                                            maxIndex,
//                                                            maxIndex + 1,
//                                                            maxIndex + 2,
//                                                            0,
//                                                            0,
//                                                            new ImmutableMethodReference("Larmadillo/call/InvokeDynamic;",
//                                                                    "callSite",
//                                                                    Lists.newArrayList("I", "[Ljava/lang/String;", "Ljava/lang/invoke/MethodHandles$Lookup;"),
//                                                                    "Ljava/lang/invoke/CallSite;")));
//                                            }
//                                            //move-result-object v0
//                                            {
//                                                i = i + 1;
//                                                mutableImplementation.addInstruction(i, new BuilderInstruction11x(Opcode.MOVE_RESULT_OBJECT, maxIndex));
//                                            }
//                                            //invoke-virtual {v0}, Ljava/lang/invoke/CallSite;->dynamicInvoker()Ljava/lang/invoke/MethodHandle;
//                                            {
//                                                i = i + 1;
//                                                if (maxIndex > 15)
//                                                    mutableImplementation.addInstruction(i, new BuilderInstruction3rc(
//                                                            Opcode.INVOKE_VIRTUAL_RANGE,
//                                                            maxIndex,
//                                                            1,
//                                                            new ImmutableMethodReference("Ljava/lang/invoke/CallSite;",
//                                                                    "dynamicInvoker",
//                                                                    null,
//                                                                    "Ljava/lang/invoke/MethodHandle;")));
//                                                else
//                                                    mutableImplementation.addInstruction(i, new BuilderInstruction35c(
//                                                            Opcode.INVOKE_VIRTUAL,
//                                                            1,
//                                                            maxIndex,
//                                                            0,
//                                                            0,
//                                                            0,
//                                                            0,
//                                                            new ImmutableMethodReference("Ljava/lang/invoke/CallSite;",
//                                                                    "dynamicInvoker",
//                                                                    null,
//                                                                    "Ljava/lang/invoke/MethodHandle;")));
//                                            }
//                                            //move-result-object v0
//                                            {
//                                                i = i + 1;
//                                                mutableImplementation.addInstruction(i, new BuilderInstruction11x(Opcode.MOVE_RESULT_OBJECT, maxIndex));
//                                            }
//                                            //invoke-polymorphic { v0, p0, v1 }, Ljava/lang/invoke/MethodHandle;->invoke([Ljava/lang/Object;)Ljava/lang/Object;, (Lcom/cloud/autosms/MainActivity;I)V
//                                            //invoke-direct {v2, p0}, Larmadillo/call/test$testsuper;-><init>(Larmadillo/call/test;)V
//                                            {
//                                                i = i + 1;
//                                                List<CharSequence> parameters = new ArrayList<>();
//                                                if (builderInstruction35c.getRegisterCount() > 0)
//                                                    parameters.add(reference.getDefiningClass());
//                                                parameters.addAll(reference.getParameterTypes());
//                                                ImmutableMethodProtoReference methodProtoReference = new ImmutableMethodProtoReference(parameters, reference.getReturnType());
//                                                methodProtoReferences.add(methodProtoReference);
//                                                mutableImplementation.addInstruction(i, new BuilderInstruction45cc(
//                                                        Opcode.INVOKE_POLYMORPHIC,
//                                                        builderInstruction35c.getRegisterCount() + 1,
//                                                        maxIndex,
//                                                        builderInstruction35c.getRegisterC(),
//                                                        builderInstruction35c.getRegisterD(),
//                                                        builderInstruction35c.getRegisterE(),
//                                                        builderInstruction35c.getRegisterF(),
//                                                        new ImmutableMethodReference("Ljava/lang/invoke/MethodHandle;",
//                                                                "invoke",
//                                                                Lists.newArrayList("[Ljava/lang/Object;"),
//                                                                "Ljava/lang/Object;"),
//                                                        methodProtoReference));
//                                            }
//                                            invoke_encrypt_total++;
//                                        }
//                                        break;


//                                case INVOKE_STATIC_RANGE: {
//                                    Instruction3rc instruction3rc = (Instruction3rc) builderInstruction;
//                                    MethodReference reference = (MethodReference) instruction3rc.getReference();
//                                    if (instruction3rc.getRegisterCount() >= 5)
//                                        break;
//                                    StringBuffer type = new StringBuffer();
//                                    type.append(dexToJavaName(reference.getDefiningClass()) + ":");
//                                    type.append(reference.getName() + ":");
//                                    type.append("(" + String.join("", reference.getParameterTypes()) + ")" + reference.getReturnType() + ":");
//                                    type.append("static");
//                                    types.add(type.toString());
//                                    int type_index = types.indexOf(type.toString());
//                                    //const/4 v0, 0x1
//                                    {
//                                        mutableImplementation.replaceInstruction(i, new BuilderInstruction21s(Opcode.CONST_16, maxIndex, type_index));
//                                    }
//                                    //sget-object v1, Larmadillo/call/test;->strings:[Ljava/lang/String;
//                                    {
//                                        i = i + 1;
//                                        mutableImplementation.addInstruction(i, new BuilderInstruction21c(
//                                                Opcode.SGET_OBJECT,
//                                                maxIndex + 1,
//                                                new ImmutableFieldReference(classDef.getType(),
//                                                        fieldName,
//                                                        "[Ljava/lang/String;")));
//                                    }
//                                    //invoke-static {}, Ljava/lang/invoke/MethodHandles;->lookup()Ljava/lang/invoke/MethodHandles$Lookup;
//                                    {
//                                        i = i + 1;
//                                        if (maxIndex > 15)
//                                            mutableImplementation.addInstruction(i, new BuilderInstruction3rc(
//                                                    Opcode.INVOKE_STATIC_RANGE,
//                                                    0,
//                                                    0,
//                                                    new ImmutableMethodReference("Ljava/lang/invoke/MethodHandles;",
//                                                            "lookup",
//                                                            null,
//                                                            "Ljava/lang/invoke/MethodHandles$Lookup;")));
//                                        else
//                                            mutableImplementation.addInstruction(i, new BuilderInstruction35c(
//                                                    Opcode.INVOKE_STATIC,
//                                                    0,
//                                                    0,
//                                                    0,
//                                                    0,
//                                                    0,
//                                                    0,
//                                                    new ImmutableMethodReference("Ljava/lang/invoke/MethodHandles;",
//                                                            "lookup",
//                                                            null,
//                                                            "Ljava/lang/invoke/MethodHandles$Lookup;")));
//                                    }
//                                    //move-result-object v0
//                                    {
//                                        i = i + 1;
//                                        mutableImplementation.addInstruction(i, new BuilderInstruction11x(Opcode.MOVE_RESULT_OBJECT, maxIndex + 2));
//                                    }
//                                    //invoke-static {v0, v1, v2}, Larmadillo/call/InvokeDynamic;->callSite(I[Ljava/lang/String;Ljava/lang/invoke/MethodHandles$Lookup;)Ljava/lang/invoke/CallSite;
//                                    {
//                                        i = i + 1;
//                                        if (maxIndex + 2 > 15)
//                                            mutableImplementation.addInstruction(i, new BuilderInstruction3rc(
//                                                    Opcode.INVOKE_STATIC_RANGE,
//                                                    maxIndex,
//                                                    3,
//                                                    new ImmutableMethodReference("Larmadillo/call/InvokeDynamic;",
//                                                            "callSite",
//                                                            Lists.newArrayList("I", "[Ljava/lang/String;", "Ljava/lang/invoke/MethodHandles$Lookup;"),
//                                                            "Ljava/lang/invoke/CallSite;")));
//                                        else
//                                            mutableImplementation.addInstruction(i, new BuilderInstruction35c(
//                                                    Opcode.INVOKE_STATIC,
//                                                    3,
//                                                    maxIndex,
//                                                    maxIndex + 1,
//                                                    maxIndex + 2,
//                                                    0,
//                                                    0,
//                                                    new ImmutableMethodReference("Larmadillo/call/InvokeDynamic;",
//                                                            "callSite",
//                                                            Lists.newArrayList("I", "[Ljava/lang/String;", "Ljava/lang/invoke/MethodHandles$Lookup;"),
//                                                            "Ljava/lang/invoke/CallSite;")));
//                                    }
//                                    //move-result-object v0
//                                    {
//                                        i = i + 1;
//                                        mutableImplementation.addInstruction(i, new BuilderInstruction11x(Opcode.MOVE_RESULT_OBJECT, maxIndex));
//                                    }
//                                    //invoke-virtual {v0}, Ljava/lang/invoke/CallSite;->dynamicInvoker()Ljava/lang/invoke/MethodHandle;
//                                    {
//                                        i = i + 1;
//                                        if (maxIndex > 15)
//                                            mutableImplementation.addInstruction(i, new BuilderInstruction3rc(
//                                                    Opcode.INVOKE_VIRTUAL_RANGE,
//                                                    maxIndex,
//                                                    1,
//                                                    new ImmutableMethodReference("Ljava/lang/invoke/CallSite;",
//                                                            "dynamicInvoker",
//                                                            null,
//                                                            "Ljava/lang/invoke/MethodHandle;")));
//                                        else
//                                            mutableImplementation.addInstruction(i, new BuilderInstruction35c(
//                                                    Opcode.INVOKE_VIRTUAL,
//                                                    1,
//                                                    maxIndex,
//                                                    0,
//                                                    0,
//                                                    0,
//                                                    0,
//                                                    new ImmutableMethodReference("Ljava/lang/invoke/CallSite;",
//                                                            "dynamicInvoker",
//                                                            null,
//                                                            "Ljava/lang/invoke/MethodHandle;")));
//                                    }
//                                    //move-result-object v0
//                                    {
//                                        i = i + 1;
//                                        mutableImplementation.addInstruction(i, new BuilderInstruction11x(Opcode.MOVE_RESULT_OBJECT, maxIndex));
//                                    }
//                                    //MOVE_OBJECT_FROM16 vx vx
//                                    for (int i1 = 0; i1 < instruction3rc.getRegisterCount(); i1++) {
//                                        i = i + 1;
//                                        mutableImplementation.addInstruction(i,new BuilderInstruction21c(Opcode.CHECK_CAST,instruction3rc.getStartRegister() + i1,new ImmutableTypeReference("Ljava/lang/Object;")));
//                                        i = i + 1;
//                                        mutableImplementation.addInstruction(i, new BuilderInstruction22x(Opcode.MOVE_OBJECT_FROM16, maxIndex + i1 + 1, instruction3rc.getStartRegister() + i1));
//                                    }
//                                    //invoke-polymorphic { v0, p0, v1 }, Ljava/lang/invoke/MethodHandle;->invoke([Ljava/lang/Object;)Ljava/lang/Object;, (Lcom/cloud/autosms/MainActivity;I)V
//                                    {
//                                        i = i + 1;
//                                        List<CharSequence> parameters = new ArrayList<>();
//                                        parameters.addAll(reference.getParameterTypes());
//                                        ImmutableMethodProtoReference methodProtoReference = new ImmutableMethodProtoReference(parameters, reference.getReturnType());
//                                        getArm().methodProtoReferences.add(methodProtoReference);
//                                        mutableImplementation.addInstruction(i, new BuilderInstruction4rcc(
//                                                Opcode.INVOKE_POLYMORPHIC_RANGE,
//                                                maxIndex,
//                                                instruction3rc.getRegisterCount() + 1,
//                                                new ImmutableMethodReference("Ljava/lang/invoke/MethodHandle;",
//                                                        "invoke",
//                                                        Lists.newArrayList("[Ljava/lang/Object;"),
//                                                        "Ljava/lang/Object;"),
//                                                methodProtoReference));
//                                    }
//                                    flag = true;
//                                    invoke_encrypt_total++;
//                                }
//                                break;
//                                case INVOKE_VIRTUAL_RANGE: {
//                                    Instruction3rc instruction3rc = (Instruction3rc) builderInstruction;
//                                    MethodReference reference = (MethodReference) instruction3rc.getReference();
//                                    if (instruction3rc.getRegisterCount() >= 5)
//                                        break;
//                                    StringBuffer type = new StringBuffer();
//                                    type.append(dexToJavaName(reference.getDefiningClass()) + ":");
//                                    type.append(reference.getName() + ":");
//                                    type.append("(" + String.join("", reference.getParameterTypes()) + ")" + reference.getReturnType() + ":");
//                                    type.append("virtual");
//                                    types.add(type.toString());
//                                    int type_index = types.indexOf(type.toString());
//                                    //const/4 v0, 0x1
//                                    {
//                                        mutableImplementation.replaceInstruction(i, new BuilderInstruction21s(Opcode.CONST_16, maxIndex, type_index));
//                                    }
//                                    //sget-object v1, Larmadillo/call/test;->strings:[Ljava/lang/String;
//                                    {
//                                        i = i + 1;
//                                        mutableImplementation.addInstruction(i, new BuilderInstruction21c(
//                                                Opcode.SGET_OBJECT,
//                                                maxIndex + 1,
//                                                new ImmutableFieldReference(classDef.getType(),
//                                                        fieldName,
//                                                        "[Ljava/lang/String;")));
//                                    }
//                                    //invoke-static {}, Ljava/lang/invoke/MethodHandles;->lookup()Ljava/lang/invoke/MethodHandles$Lookup;
//                                    {
//                                        i = i + 1;
//                                        if (maxIndex > 15)
//                                            mutableImplementation.addInstruction(i, new BuilderInstruction3rc(
//                                                    Opcode.INVOKE_STATIC_RANGE,
//                                                    0,
//                                                    0,
//                                                    new ImmutableMethodReference("Ljava/lang/invoke/MethodHandles;",
//                                                            "lookup",
//                                                            null,
//                                                            "Ljava/lang/invoke/MethodHandles$Lookup;")));
//                                        else
//                                            mutableImplementation.addInstruction(i, new BuilderInstruction35c(
//                                                    Opcode.INVOKE_STATIC,
//                                                    0,
//                                                    0,
//                                                    0,
//                                                    0,
//                                                    0,
//                                                    0,
//                                                    new ImmutableMethodReference("Ljava/lang/invoke/MethodHandles;",
//                                                            "lookup",
//                                                            null,
//                                                            "Ljava/lang/invoke/MethodHandles$Lookup;")));
//                                    }
//                                    //move-result-object v0
//                                    {
//                                        i = i + 1;
//                                        mutableImplementation.addInstruction(i, new BuilderInstruction11x(Opcode.MOVE_RESULT_OBJECT, maxIndex + 2));
//                                    }
//                                    //invoke-static {v0, v1, v2}, Larmadillo/call/InvokeDynamic;->callSite(I[Ljava/lang/String;Ljava/lang/invoke/MethodHandles$Lookup;)Ljava/lang/invoke/CallSite;
//                                    {
//                                        i = i + 1;
//                                        if (maxIndex + 2 > 15)
//                                            mutableImplementation.addInstruction(i, new BuilderInstruction3rc(
//                                                    Opcode.INVOKE_STATIC_RANGE,
//                                                    maxIndex,
//                                                    3,
//                                                    new ImmutableMethodReference("Larmadillo/call/InvokeDynamic;",
//                                                            "callSite",
//                                                            Lists.newArrayList("I", "[Ljava/lang/String;", "Ljava/lang/invoke/MethodHandles$Lookup;"),
//                                                            "Ljava/lang/invoke/CallSite;")));
//                                        else
//                                            mutableImplementation.addInstruction(i, new BuilderInstruction35c(
//                                                    Opcode.INVOKE_STATIC,
//                                                    3,
//                                                    maxIndex,
//                                                    maxIndex + 1,
//                                                    maxIndex + 2,
//                                                    0,
//                                                    0,
//                                                    new ImmutableMethodReference("Larmadillo/call/InvokeDynamic;",
//                                                            "callSite",
//                                                            Lists.newArrayList("I", "[Ljava/lang/String;", "Ljava/lang/invoke/MethodHandles$Lookup;"),
//                                                            "Ljava/lang/invoke/CallSite;")));
//                                    }
//                                    //move-result-object v0
//                                    {
//                                        i = i + 1;
//                                        mutableImplementation.addInstruction(i, new BuilderInstruction11x(Opcode.MOVE_RESULT_OBJECT, maxIndex));
//                                    }
//                                    //invoke-virtual {v0}, Ljava/lang/invoke/CallSite;->dynamicInvoker()Ljava/lang/invoke/MethodHandle;
//                                    {
//                                        i = i + 1;
//                                        if (maxIndex > 15)
//                                            mutableImplementation.addInstruction(i, new BuilderInstruction3rc(
//                                                    Opcode.INVOKE_VIRTUAL_RANGE,
//                                                    maxIndex,
//                                                    1,
//                                                    new ImmutableMethodReference("Ljava/lang/invoke/CallSite;",
//                                                            "dynamicInvoker",
//                                                            null,
//                                                            "Ljava/lang/invoke/MethodHandle;")));
//                                        else
//                                            mutableImplementation.addInstruction(i, new BuilderInstruction35c(
//                                                    Opcode.INVOKE_VIRTUAL,
//                                                    1,
//                                                    maxIndex,
//                                                    0,
//                                                    0,
//                                                    0,
//                                                    0,
//                                                    new ImmutableMethodReference("Ljava/lang/invoke/CallSite;",
//                                                            "dynamicInvoker",
//                                                            null,
//                                                            "Ljava/lang/invoke/MethodHandle;")));
//                                    }
//                                    //move-result-object v0
//                                    {
//                                        i = i + 1;
//                                        mutableImplementation.addInstruction(i, new BuilderInstruction11x(Opcode.MOVE_RESULT_OBJECT, maxIndex));
//                                    }
//                                    //MOVE_OBJECT_FROM16 vx vx
//                                    for (int i1 = 0; i1 < instruction3rc.getRegisterCount(); i1++) {
//                                        i = i + 1;
//                                        mutableImplementation.addInstruction(i,new BuilderInstruction21c(Opcode.CHECK_CAST,instruction3rc.getStartRegister() + i1,new ImmutableTypeReference("Ljava/lang/Object;")));
//                                        i = i + 1;
//                                        mutableImplementation.addInstruction(i, new BuilderInstruction22x(Opcode.MOVE_OBJECT_FROM16, maxIndex + i1 + 1, instruction3rc.getStartRegister() + i1));
//                                    }
//                                    //invoke-polymorphic { v0, p0, v1 }, Ljava/lang/invoke/MethodHandle;->invoke([Ljava/lang/Object;)Ljava/lang/Object;, (Lcom/cloud/autosms/MainActivity;I)V
//                                    {
//                                        i = i + 1;
//                                        List<CharSequence> parameters = new ArrayList<>();
//                                        if (instruction3rc.getRegisterCount() > 0)
//                                            parameters.add(reference.getDefiningClass());
//                                        parameters.addAll(reference.getParameterTypes());
//                                        ImmutableMethodProtoReference methodProtoReference = new ImmutableMethodProtoReference(parameters, reference.getReturnType());
//                                        getArm().methodProtoReferences.add(methodProtoReference);
//                                        mutableImplementation.addInstruction(i, new BuilderInstruction4rcc(
//                                                Opcode.INVOKE_POLYMORPHIC_RANGE,
//                                                maxIndex,
//                                                instruction3rc.getRegisterCount() + 1,
//                                                new ImmutableMethodReference("Ljava/lang/invoke/MethodHandle;",
//                                                        "invoke",
//                                                        Lists.newArrayList("[Ljava/lang/Object;"),
//                                                        "Ljava/lang/Object;"),
//                                                methodProtoReference));
//                                    }
//                                    flag = true;
//                                    invoke_encrypt_total++;
//                                }
//                                break;
//                                case INVOKE_DIRECT_RANGE: {
//                                    Instruction3rc instruction3rc = (Instruction3rc) builderInstruction;
//                                    MethodReference reference = (MethodReference) instruction3rc.getReference();
//                                    if (reference.getName().equals("<init>"))
//                                        break;
//                                    if (instruction3rc.getRegisterCount() >= 5)
//                                        break;
//                                    StringBuffer type = new StringBuffer();
//                                    type.append(dexToJavaName(reference.getDefiningClass()) + ":");
//                                    type.append(reference.getName() + ":");
//                                    type.append("(" + String.join("", reference.getParameterTypes()) + ")" + reference.getReturnType() + ":");
//                                    type.append("virtual");
//                                    types.add(type.toString());
//                                    int type_index = types.indexOf(type.toString());
//                                    //const/4 v0, 0x1
//                                    {
//                                        mutableImplementation.replaceInstruction(i, new BuilderInstruction21s(Opcode.CONST_16, maxIndex, type_index));
//                                    }
//                                    //sget-object v1, Larmadillo/call/test;->strings:[Ljava/lang/String;
//                                    {
//                                        i = i + 1;
//                                        mutableImplementation.addInstruction(i, new BuilderInstruction21c(
//                                                Opcode.SGET_OBJECT,
//                                                maxIndex + 1,
//                                                new ImmutableFieldReference(classDef.getType(),
//                                                        fieldName,
//                                                        "[Ljava/lang/String;")));
//                                    }
//                                    //invoke-static {}, Ljava/lang/invoke/MethodHandles;->lookup()Ljava/lang/invoke/MethodHandles$Lookup;
//                                    {
//                                        i = i + 1;
//                                        if (maxIndex > 15)
//                                            mutableImplementation.addInstruction(i, new BuilderInstruction3rc(
//                                                    Opcode.INVOKE_STATIC_RANGE,
//                                                    0,
//                                                    0,
//                                                    new ImmutableMethodReference("Ljava/lang/invoke/MethodHandles;",
//                                                            "lookup",
//                                                            null,
//                                                            "Ljava/lang/invoke/MethodHandles$Lookup;")));
//                                        else
//                                            mutableImplementation.addInstruction(i, new BuilderInstruction35c(
//                                                    Opcode.INVOKE_STATIC,
//                                                    0,
//                                                    0,
//                                                    0,
//                                                    0,
//                                                    0,
//                                                    0,
//                                                    new ImmutableMethodReference("Ljava/lang/invoke/MethodHandles;",
//                                                            "lookup",
//                                                            null,
//                                                            "Ljava/lang/invoke/MethodHandles$Lookup;")));
//                                    }
//                                    //move-result-object v0
//                                    {
//                                        i = i + 1;
//                                        mutableImplementation.addInstruction(i, new BuilderInstruction11x(Opcode.MOVE_RESULT_OBJECT, maxIndex + 2));
//                                    }
//                                    //invoke-static {v0, v1, v2}, Larmadillo/call/InvokeDynamic;->callSite(I[Ljava/lang/String;Ljava/lang/invoke/MethodHandles$Lookup;)Ljava/lang/invoke/CallSite;
//                                    {
//                                        i = i + 1;
//                                        if (maxIndex + 2 > 15)
//                                            mutableImplementation.addInstruction(i, new BuilderInstruction3rc(
//                                                    Opcode.INVOKE_STATIC_RANGE,
//                                                    maxIndex,
//                                                    3,
//                                                    new ImmutableMethodReference("Larmadillo/call/InvokeDynamic;",
//                                                            "callSite",
//                                                            Lists.newArrayList("I", "[Ljava/lang/String;", "Ljava/lang/invoke/MethodHandles$Lookup;"),
//                                                            "Ljava/lang/invoke/CallSite;")));
//                                        else
//                                            mutableImplementation.addInstruction(i, new BuilderInstruction35c(
//                                                    Opcode.INVOKE_STATIC,
//                                                    3,
//                                                    maxIndex,
//                                                    maxIndex + 1,
//                                                    maxIndex + 2,
//                                                    0,
//                                                    0,
//                                                    new ImmutableMethodReference("Larmadillo/call/InvokeDynamic;",
//                                                            "callSite",
//                                                            Lists.newArrayList("I", "[Ljava/lang/String;", "Ljava/lang/invoke/MethodHandles$Lookup;"),
//                                                            "Ljava/lang/invoke/CallSite;")));
//                                    }
//                                    //move-result-object v0
//                                    {
//                                        i = i + 1;
//                                        mutableImplementation.addInstruction(i, new BuilderInstruction11x(Opcode.MOVE_RESULT_OBJECT, maxIndex));
//                                    }
//                                    //invoke-virtual {v0}, Ljava/lang/invoke/CallSite;->dynamicInvoker()Ljava/lang/invoke/MethodHandle;
//                                    {
//                                        i = i + 1;
//                                        if (maxIndex > 15)
//                                            mutableImplementation.addInstruction(i, new BuilderInstruction3rc(
//                                                    Opcode.INVOKE_VIRTUAL_RANGE,
//                                                    maxIndex,
//                                                    1,
//                                                    new ImmutableMethodReference("Ljava/lang/invoke/CallSite;",
//                                                            "dynamicInvoker",
//                                                            null,
//                                                            "Ljava/lang/invoke/MethodHandle;")));
//                                        else
//                                            mutableImplementation.addInstruction(i, new BuilderInstruction35c(
//                                                    Opcode.INVOKE_VIRTUAL,
//                                                    1,
//                                                    maxIndex,
//                                                    0,
//                                                    0,
//                                                    0,
//                                                    0,
//                                                    new ImmutableMethodReference("Ljava/lang/invoke/CallSite;",
//                                                            "dynamicInvoker",
//                                                            null,
//                                                            "Ljava/lang/invoke/MethodHandle;")));
//                                    }
//                                    //move-result-object v0
//                                    {
//                                        i = i + 1;
//                                        mutableImplementation.addInstruction(i, new BuilderInstruction11x(Opcode.MOVE_RESULT_OBJECT, maxIndex));
//                                    }
//                                    //MOVE_OBJECT_FROM16 vx vx
//                                    for (int i1 = 0; i1 < instruction3rc.getRegisterCount(); i1++) {
//                                        i = i + 1;
//                                        mutableImplementation.addInstruction(i,new BuilderInstruction21c(Opcode.CHECK_CAST,instruction3rc.getStartRegister() + i1,new ImmutableTypeReference("Ljava/lang/Object;")));
//                                        i = i + 1;
//                                        mutableImplementation.addInstruction(i, new BuilderInstruction22x(Opcode.MOVE_OBJECT_FROM16, maxIndex + i1 + 1, instruction3rc.getStartRegister() + i1));
//                                    }
//                                    //invoke-polymorphic { v0, p0, v1 }, Ljava/lang/invoke/MethodHandle;->invoke([Ljava/lang/Object;)Ljava/lang/Object;, (Lcom/cloud/autosms/MainActivity;I)V
//                                    {
//                                        i = i + 1;
//                                        List<CharSequence> parameters = new ArrayList<>();
//                                        if (instruction3rc.getRegisterCount() > 0)
//                                            parameters.add(reference.getDefiningClass());
//                                        parameters.addAll(reference.getParameterTypes());
//                                        ImmutableMethodProtoReference methodProtoReference = new ImmutableMethodProtoReference(parameters, reference.getReturnType());
//                                        getArm().methodProtoReferences.add(methodProtoReference);
//                                        mutableImplementation.addInstruction(i, new BuilderInstruction4rcc(
//                                                Opcode.INVOKE_POLYMORPHIC_RANGE,
//                                                maxIndex,
//                                                instruction3rc.getRegisterCount() + 1,
//                                                new ImmutableMethodReference("Ljava/lang/invoke/MethodHandle;",
//                                                        "invoke",
//                                                        Lists.newArrayList("[Ljava/lang/Object;"),
//                                                        "Ljava/lang/Object;"),
//                                                methodProtoReference));
//                                    }
//                                    flag = true;
//                                    invoke_encrypt_total++;
//                                }
//                                break;
//                                case SGET_OBJECT: {
//                                    BuilderInstruction21c builderInstruction21c = (BuilderInstruction21c) builderInstruction;
//                                    FieldReference reference = (FieldReference) builderInstruction21c.getReference();
//                                    if (maxIndex + 1 > 15)
//                                        break;
//                                    mutableImplementation.replaceInstruction(i, builderInstruction21c);
//                                    StringBuffer type = new StringBuffer();
//                                    type.append(dexToJavaName(reference.getDefiningClass()) + ":");
//                                    type.append(reference.getName() + ":");
//                                    type.append("()" + reference.getType() + ":");
//                                    type.append("get");
//                                    types.add(type.toString());
//                                    int type_index = types.indexOf(type.toString());
//                                    //const/4 v0, 0x1
//                                    {
//                                        if (type_index > 7)
//                                            mutableImplementation.replaceInstruction(i, new BuilderInstruction21s(Opcode.CONST_16, maxIndex, type_index));
//                                        else
//                                            mutableImplementation.replaceInstruction(i, new BuilderInstruction11n(Opcode.CONST_4, maxIndex, type_index));
//                                    }
//                                    //sget-object v1, Larmadillo/call/test;->strings:[Ljava/lang/String;
//                                    {
//                                        i = i + 1;
//                                        mutableImplementation.addInstruction(i, new BuilderInstruction21c(
//                                                Opcode.SGET_OBJECT,
//                                                maxIndex + 1,
//                                                new ImmutableFieldReference(classDef.getType(),
//                                                        fieldName,
//                                                        "[Ljava/lang/String;")));
//                                    }
//                                    //invoke-static {}, Ljava/lang/invoke/MethodHandles;->lookup()Ljava/lang/invoke/MethodHandles$Lookup;
//                                    {
//                                        i = i + 1;
//                                        if (maxIndex > 15)
//                                            mutableImplementation.addInstruction(i, new BuilderInstruction3rc(
//                                                    Opcode.INVOKE_STATIC_RANGE,
//                                                    0,
//                                                    0,
//                                                    new ImmutableMethodReference("Ljava/lang/invoke/MethodHandles;",
//                                                            "lookup",
//                                                            null,
//                                                            "Ljava/lang/invoke/MethodHandles$Lookup;")));
//                                        else
//                                            mutableImplementation.addInstruction(i, new BuilderInstruction35c(
//                                                    Opcode.INVOKE_STATIC,
//                                                    0,
//                                                    0,
//                                                    0,
//                                                    0,
//                                                    0,
//                                                    0,
//                                                    new ImmutableMethodReference("Ljava/lang/invoke/MethodHandles;",
//                                                            "lookup",
//                                                            null,
//                                                            "Ljava/lang/invoke/MethodHandles$Lookup;")));
//                                    }
//                                    //move-result-object v0
//                                    {
//                                        i = i + 1;
//                                        mutableImplementation.addInstruction(i, new BuilderInstruction11x(Opcode.MOVE_RESULT_OBJECT, maxIndex + 2));
//                                    }
//                                    //invoke-static {v0, v1, v2}, Larmadillo/call/InvokeDynamic;->callSite(I[Ljava/lang/String;Ljava/lang/invoke/MethodHandles$Lookup;)Ljava/lang/invoke/CallSite;
//                                    {
//                                        i = i + 1;
//                                        if (maxIndex + 2 > 15)
//                                            mutableImplementation.addInstruction(i, new BuilderInstruction3rc(
//                                                    Opcode.INVOKE_STATIC_RANGE,
//                                                    maxIndex,
//                                                    3,
//                                                    new ImmutableMethodReference("Larmadillo/call/InvokeDynamic;",
//                                                            "callSite",
//                                                            Lists.newArrayList("I", "[Ljava/lang/String;", "Ljava/lang/invoke/MethodHandles$Lookup;"),
//                                                            "Ljava/lang/invoke/CallSite;")));
//                                        else
//                                            mutableImplementation.addInstruction(i, new BuilderInstruction35c(
//                                                    Opcode.INVOKE_STATIC,
//                                                    3,
//                                                    maxIndex,
//                                                    maxIndex + 1,
//                                                    maxIndex + 2,
//                                                    0,
//                                                    0,
//                                                    new ImmutableMethodReference("Larmadillo/call/InvokeDynamic;",
//                                                            "callSite",
//                                                            Lists.newArrayList("I", "[Ljava/lang/String;", "Ljava/lang/invoke/MethodHandles$Lookup;"),
//                                                            "Ljava/lang/invoke/CallSite;")));
//                                    }
//                                    //move-result-object v0
//                                    {
//                                        i = i + 1;
//                                        mutableImplementation.addInstruction(i, new BuilderInstruction11x(Opcode.MOVE_RESULT_OBJECT, maxIndex));
//                                    }
//                                    //invoke-virtual {v0}, Ljava/lang/invoke/CallSite;->dynamicInvoker()Ljava/lang/invoke/MethodHandle;
//                                    {
//                                        i = i + 1;
//                                        if (maxIndex > 15)
//                                            mutableImplementation.addInstruction(i, new BuilderInstruction3rc(
//                                                    Opcode.INVOKE_VIRTUAL_RANGE,
//                                                    maxIndex,
//                                                    1,
//                                                    new ImmutableMethodReference("Ljava/lang/invoke/CallSite;",
//                                                            "dynamicInvoker",
//                                                            null,
//                                                            "Ljava/lang/invoke/MethodHandle;")));
//                                        else
//                                            mutableImplementation.addInstruction(i, new BuilderInstruction35c(
//                                                    Opcode.INVOKE_VIRTUAL,
//                                                    1,
//                                                    maxIndex,
//                                                    0,
//                                                    0,
//                                                    0,
//                                                    0,
//                                                    new ImmutableMethodReference("Ljava/lang/invoke/CallSite;",
//                                                            "dynamicInvoker",
//                                                            null,
//                                                            "Ljava/lang/invoke/MethodHandle;")));
//                                    }
//                                    //move-result-object v0
//                                    {
//                                        i = i + 1;
//                                        mutableImplementation.addInstruction(i, new BuilderInstruction11x(Opcode.MOVE_RESULT_OBJECT, maxIndex));
//                                    }
//                                    //invoke-polymorphic { v0, p0, v1 }, Ljava/lang/invoke/MethodHandle;->invoke([Ljava/lang/Object;)Ljava/lang/Object;, (Lcom/cloud/autosms/MainActivity;I)V
//                                    {
//                                        i = i + 1;
//                                        List<CharSequence> parameters = new ArrayList<>();
//                                        ImmutableMethodProtoReference methodProtoReference = new ImmutableMethodProtoReference(parameters, reference.getType());
//                                        getArm().methodProtoReferences.add(methodProtoReference);
//                                        mutableImplementation.addInstruction(i, new BuilderInstruction45cc(
//                                                Opcode.INVOKE_POLYMORPHIC,
//                                                2,
//                                                maxIndex,
//                                                builderInstruction21c.getRegisterA(),
//                                                0,
//                                                0,
//                                                0,
//                                                new ImmutableMethodReference("Ljava/lang/invoke/MethodHandle;",
//                                                        "invoke",
//                                                        Lists.newArrayList("[Ljava/lang/Object;"),
//                                                        "Ljava/lang/Object;"),
//                                                methodProtoReference));
//                                    }
//                                    flag = true;
//                                }
//                                break;
                                }
                            }
                            mutableImplementation.fixReg(addCount + mutableImplementation.getMethodReg(method), method);
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
                        } else
                            newMethod.add(method);
                    }
                    if (types.size() > 0) {
                        newMethod.add(InvokeDynamicUtils.CreateArmadillo(classDef, fieldName, methodName, types));
                        if (clinit != null)
                            newMethod.add(InvokeDynamicUtils.AddClinit(clinit, methodName));
                        else
                            newMethod.add(InvokeDynamicUtils.CreateClinit(classDef, methodName));
                        List<Field> fields = new ArrayList<>();
                        for (Field field : classDef.getFields())
                            fields.add(field);
                        fields.add(new ImmutableField(classDef.getType(),
                                fieldName,
                                "[Ljava/lang/String;",
                                AccessFlags.PRIVATE.getValue() | AccessFlags.STATIC.getValue(),
                                null,
                                null,
                                null));
                        return new ImmutableClassDef(
                                classDef.getType(),
                                classDef.getAccessFlags(),
                                classDef.getSuperclass(),
                                classDef.getInterfaces(),
                                classDef.getSourceFile(),
                                classDef.getAnnotations(),
                                fields,
                                newMethod);
                    } else
                        return classDef;
                } else
                    return classDef;
            }
        };
    }

    @Override
    public void transform() throws Exception {
        invoke_encrypt_total = 0;
        diff.clear();
        if (configuration != null) {
            JsonArray separate = new JsonParser().parse(configuration).getAsJsonObject().getAsJsonArray(Long.toString(16));
            if (separate == null) return;
            for (JsonElement jsonElement : separate)
                diff.add(jsonElement.getAsString());
            byte[] xml = getReplacerRes().get("AndroidManifest.xml");
            if (xml == null)
                xml = StreamUtil.readBytes(getZipFile().getInputStream(new ZipEntry("AndroidManifest.xml")));
            AXMLDoc axmlDoc = new AXMLDoc();
            axmlDoc.parse(new ByteArrayInputStream(xml));
            for (BXMLNode child : axmlDoc.getManifestNode().getChildren()) {
                BTagNode node = (BTagNode) child;
                if ("uses-sdk".equals(node.getRawName())) {
                    BTagNode.Attribute[] attributes = node.getAttribute();
                    for (BTagNode.Attribute attribute : attributes) {
                        if (attribute.Name.equals("minSdkVersion")) {
                            if (attribute.mValue < 26)
                                attribute.setValue(TypedValue.TYPE_INT_DEC, 26);
                        } else if (attribute.Name.equals("targetSdkVersion")) {
                            if (attribute.mValue < 26)
                                attribute.setValue(TypedValue.TYPE_INT_DEC, 30);
                        }
                    }
                    node.setAttribute(attributes);
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    axmlDoc.build(outputStream);
                    axmlDoc.release();
                    getReplacerRes().put("AndroidManifest.xml", outputStream.toByteArray());
                    break;
                }
            }
        }
    }

    @Override
    public String getResult() {
        return String.format(Objects.requireNonNull(SysConfigUtil.getLanguageConfigUtil(getLanguageEnums(), "invokedynamic.encryption")), invoke_encrypt_total);
    }

    @Override
    public int priority() {
        return 10;
    }

    @Override
    public Set<? extends ClassDef> getNewClassDef() {
        HashSet<ClassDef> classDefs = new HashSet<>();
        try {
            classDefs.add(InvokeDynamicUtils.CreatecallSite());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return classDefs;
    }

    @Override
    public Opcodes getOpcodes() {
        return Opcodes.forDexVersion(38);
    }

    @Override
    public List<ImmutableMethodProtoReference> getMethodProtoReferences() {
        return methodProtoReferences;
    }

    private String dexToJavaName(String dexName) {
        if (dexName.charAt(0) == '[') {
            return dexName.replace('/', '.');
        }
        return dexName.replace('/', '.').substring(1, dexName.length() - 1);
    }

    @Override
    public int compareTo(BaseTransformer o) {
        return priority() - o.priority();
    }
}
