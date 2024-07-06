package armadillo.transformers.verify;

import armadillo.utils.StringRandom;
import org.jf.dexlib2.dexbacked.DexBackedClassDef;
import org.jf.dexlib2.iface.*;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.formats.Instruction21c;
import org.jf.dexlib2.iface.instruction.formats.Instruction22c;
import org.jf.dexlib2.iface.instruction.formats.Instruction35c;
import org.jf.dexlib2.iface.instruction.formats.Instruction3rc;
import org.jf.dexlib2.iface.reference.FieldReference;
import org.jf.dexlib2.iface.reference.MethodReference;
import org.jf.dexlib2.iface.reference.TypeReference;
import org.jf.dexlib2.immutable.*;
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction21c;
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction22c;
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction35c;
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction3rc;
import org.jf.dexlib2.immutable.reference.ImmutableFieldReference;
import org.jf.dexlib2.immutable.reference.ImmutableMethodReference;
import org.jf.dexlib2.immutable.reference.ImmutableTypeReference;
import org.jf.dexlib2.rewriter.*;

import javax.annotation.Nonnull;
import java.util.*;

public class ClassRename extends RewriterModule {
    private final HashMap<String, String> classMap = new HashMap<>();

    @Nonnull
    @Override
    public Rewriter<ClassDef> getClassDefRewriter(@Nonnull Rewriters rewriters) {
        return new ClassDefRewriter(rewriters) {
            @Nonnull
            @Override
            public ClassDef rewrite(@Nonnull ClassDef classDef) {
                /**
                 * 修复Method
                 */
                List<Method> methods = new ArrayList<>();
                for (Method method : classDef.getMethods()) {
                    /**
                     * 修复参数类型
                     */
                    List<MethodParameter> methodParameters = new ArrayList<>();
                    for (MethodParameter parameter : method.getParameters())
                        methodParameters.add(new ImmutableMethodParameter(
                                getType(parameter.getType()),
                                parameter.getAnnotations(),
                                parameter.getName()
                        ));
                    /**
                     * 修复指令
                     */
                    if (method.getImplementation() != null) {
                        List<Instruction> instructions = new ArrayList<>();
                        for (Instruction instruction : method.getImplementation().getInstructions()) {
                            switch (instruction.getOpcode()) {
                                case INVOKE_STATIC:
                                case INVOKE_DIRECT:
                                case INVOKE_VIRTUAL:
                                case INVOKE_INTERFACE:
                                case INVOKE_CUSTOM:
                                case INVOKE_SUPER: {
                                    Instruction35c instruction35c = (Instruction35c) instruction;
                                    MethodReference reference = (MethodReference) instruction35c.getReference();
                                    Rewriter<MethodReference> rewriter = getMethodReferenceRewriter(rewriters);
                                    MethodReference methodReference = rewriter.rewrite(new ImmutableMethodReference(
                                            reference.getDefiningClass(),
                                            reference.getName(),
                                            reference.getParameterTypes(),
                                            reference.getReturnType()
                                    ));
                                    instructions.add(new ImmutableInstruction35c(
                                            instruction.getOpcode(),
                                            instruction35c.getRegisterCount(),
                                            instruction35c.getRegisterC(),
                                            instruction35c.getRegisterD(),
                                            instruction35c.getRegisterE(),
                                            instruction35c.getRegisterF(),
                                            instruction35c.getRegisterG(),
                                            methodReference
                                    ));
                                }
                                break;
                                case INVOKE_SUPER_RANGE:
                                case INVOKE_INTERFACE_RANGE:
                                case INVOKE_CUSTOM_RANGE:
                                case INVOKE_DIRECT_RANGE:
                                case INVOKE_STATIC_RANGE:
                                case INVOKE_VIRTUAL_RANGE: {
                                    Instruction3rc instruction3rc = (Instruction3rc) instruction;
                                    MethodReference reference = (MethodReference) instruction3rc.getReference();
                                    Rewriter<MethodReference> rewriter = getMethodReferenceRewriter(rewriters);
                                    MethodReference methodReference = rewriter.rewrite(new ImmutableMethodReference(
                                            reference.getDefiningClass(),
                                            reference.getName(),
                                            reference.getParameterTypes(),
                                            reference.getReturnType()
                                    ));
                                    instructions.add(new ImmutableInstruction3rc(
                                            instruction.getOpcode(),
                                            instruction3rc.getStartRegister(),
                                            instruction3rc.getRegisterCount(),
                                            methodReference
                                    ));
                                }
                                break;
                                case SGET:
                                case SGET_OBJECT:
                                case SGET_WIDE:
                                case SGET_BOOLEAN:
                                case SGET_SHORT:
                                case SGET_CHAR:
                                case SGET_BYTE:
                                case SPUT:
                                case SPUT_BOOLEAN:
                                case SPUT_SHORT:
                                case SPUT_WIDE:
                                case SPUT_CHAR:
                                case SPUT_BYTE:
                                case SPUT_OBJECT: {
                                    Instruction21c instruction21c = (Instruction21c) instruction;
                                    FieldReference reference = (FieldReference) instruction21c.getReference();
                                    Rewriter<FieldReference> rewriter = getFieldReferenceRewriter(rewriters);
                                    FieldReference fieldReference = rewriter.rewrite(new ImmutableFieldReference(
                                            reference.getDefiningClass(),
                                            reference.getName(),
                                            reference.getType()
                                    ));
                                    instructions.add(new ImmutableInstruction21c(
                                            instruction.getOpcode(),
                                            instruction21c.getRegisterA(),
                                            fieldReference
                                    ));
                                }
                                break;
                                case IGET:
                                case IGET_WIDE:
                                case IGET_CHAR:
                                case IGET_BYTE:
                                case IGET_QUICK:
                                case IGET_SHORT:
                                case IGET_OBJECT:
                                case IGET_BOOLEAN:
                                case IPUT:
                                case IPUT_WIDE:
                                case IPUT_BYTE:
                                case IPUT_CHAR:
                                case IPUT_QUICK:
                                case IPUT_SHORT:
                                case IPUT_OBJECT:
                                case IPUT_BOOLEAN: {
                                    Instruction22c instruction22c = (Instruction22c) instruction;
                                    FieldReference reference = (FieldReference) instruction22c.getReference();
                                    Rewriter<FieldReference> rewriter = getFieldReferenceRewriter(rewriters);
                                    FieldReference fieldReference = rewriter.rewrite(new ImmutableFieldReference(
                                            reference.getDefiningClass(),
                                            reference.getName(),
                                            reference.getType()
                                    ));
                                    instructions.add(new ImmutableInstruction22c(
                                            instruction.getOpcode(),
                                            instruction22c.getRegisterA(),
                                            instruction22c.getRegisterB(),
                                            fieldReference
                                    ));
                                }
                                break;
                                case CONST_CLASS:
                                case CHECK_CAST:
                                case NEW_INSTANCE: {
                                    Instruction21c instruction21c = (Instruction21c) instruction;
                                    TypeReference reference = (TypeReference) instruction21c.getReference();
                                    instructions.add(new ImmutableInstruction21c(
                                            instruction.getOpcode(),
                                            instruction21c.getRegisterA(),
                                            new ImmutableTypeReference(
                                                    getType(reference.getType())
                                            )
                                    ));
                                }
                                break;
                                case NEW_ARRAY:
                                case INSTANCE_OF: {
                                    Instruction22c instruction22c = (Instruction22c) instruction;
                                    TypeReference reference = (TypeReference) instruction22c.getReference();
                                    instructions.add(new ImmutableInstruction22c(
                                            instruction.getOpcode(),
                                            instruction22c.getRegisterA(),
                                            instruction22c.getRegisterB(),
                                            new ImmutableTypeReference(
                                                    getType(reference.getType())
                                            )
                                    ));
                                }
                                break;
                                case FILLED_NEW_ARRAY: {
                                    Instruction35c instruction35c = (Instruction35c) instruction;
                                    TypeReference reference = (TypeReference) instruction35c.getReference();
                                    instructions.add(new ImmutableInstruction35c(
                                            instruction.getOpcode(),
                                            instruction35c.getRegisterCount(),
                                            instruction35c.getRegisterC(),
                                            instruction35c.getRegisterD(),
                                            instruction35c.getRegisterE(),
                                            instruction35c.getRegisterF(),
                                            instruction35c.getRegisterG(),
                                            new ImmutableTypeReference(
                                                    getType(reference.getType())
                                            )
                                    ));
                                }
                                break;
                                case FILLED_NEW_ARRAY_RANGE: {
                                    Instruction3rc instruction3rc = (Instruction3rc) instruction;
                                    TypeReference reference = (TypeReference) instruction3rc.getReference();
                                    instructions.add(new ImmutableInstruction3rc(
                                            instruction.getOpcode(),
                                            instruction3rc.getStartRegister(),
                                            instruction3rc.getRegisterCount(),
                                            new ImmutableTypeReference(
                                                    getType(reference.getType())
                                            )
                                    ));
                                }
                                break;
                                default:
                                    instructions.add(instruction);
                                    break;
                            }
                        }
                        methods.add(new ImmutableMethod(
                                getType(classDef.getType()),
                                method.getName(),
                                methodParameters,
                                getType(method.getReturnType()),
                                method.getAccessFlags(),
                                method.getAnnotations(),
                                method.getHiddenApiRestrictions(),
                                new ImmutableMethodImplementation(
                                        method.getImplementation().getRegisterCount(),
                                        instructions,
                                        method.getImplementation().getTryBlocks(),
                                        method.getImplementation().getDebugItems()
                                )));
                    } else {
                        methods.add(new ImmutableMethod(
                                getType(classDef.getType()),
                                method.getName(),
                                methodParameters,
                                getType(method.getReturnType()),
                                method.getAccessFlags(),
                                method.getAnnotations(),
                                method.getHiddenApiRestrictions(),
                                method.getImplementation()));
                    }
                }
                /**
                 * 修复Field
                 */
                List<Field> fields = new ArrayList<>();
                for (Field field : classDef.getFields()) {
                    fields.add(new ImmutableField(
                            getType(field.getDefiningClass()),
                            field.getName(),
                            getType(field.getType()),
                            field.getAccessFlags(),
                            field.getInitialValue(),
                            field.getAnnotations(),
                            field.getHiddenApiRestrictions()
                    ));
                }
                /**
                 * 修复接口
                 */
                List<String> Interfaces = new ArrayList<>();
                for (String defInterface : classDef.getInterfaces()) {
                    Interfaces.add(getType(defInterface));
                }
                /**
                 * 修复Class类名
                 */
                return new ImmutableClassDef(
                        getType(classDef.getType()),
                        classDef.getAccessFlags(),
                        getType(classDef.getSuperclass()),
                        Interfaces,
                        StringRandom.RandomString(),
                        classDef.getAnnotations(),
                        fields,
                        methods);
            }
        };
    }

    @Nonnull
    @Override
    public Rewriter<MethodReference> getMethodReferenceRewriter(@Nonnull Rewriters rewriters) {
        return new MethodReferenceRewriter(rewriters) {
            @Nonnull
            @Override
            public MethodReference rewrite(@Nonnull MethodReference methodReference) {
                List<String> parameterTypes = new ArrayList<>();
                for (CharSequence parameterType : methodReference.getParameterTypes())
                    parameterTypes.add(getType(parameterType.toString()));
                return new ImmutableMethodReference(
                        getType(methodReference.getDefiningClass()),
                        methodReference.getName(),
                        parameterTypes,
                        getType(methodReference.getReturnType())
                );
            }
        };
    }

    @Nonnull
    @Override
    public Rewriter<FieldReference> getFieldReferenceRewriter(@Nonnull Rewriters rewriters) {
        return new FieldReferenceRewriter(rewriters) {
            @Nonnull
            @Override
            public FieldReference rewrite(@Nonnull FieldReference fieldReference) {
                return new ImmutableFieldReference(
                        getType(fieldReference.getDefiningClass()),
                        fieldReference.getName(),
                        getType(fieldReference.getType())
                );
            }
        };
    }

    public HashMap<String, String> getClassMap() {
        return classMap;
    }

    private String getType(String src) {
        if (src == null)
            return null;
        if (src.charAt(0) == '[' && src.contains("L")) {
            String index = src.substring(src.indexOf("L"));
            if (classMap.get(index) == null)
                return src;
            else
                return src.substring(0, src.indexOf("L")) + classMap.get(index);
        } else {
            if (classMap.get(src) == null)
                return src;
            else
                return classMap.get(src);
        }
    }
}
