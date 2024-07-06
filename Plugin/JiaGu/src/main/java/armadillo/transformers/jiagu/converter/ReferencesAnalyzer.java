package armadillo.transformers.jiagu.converter;

import armadillo.transformers.jiagu.vmutils.MethodHelper;
import armadillo.transformers.jiagu.vmutils.ModifiedUtf8;
import com.google.common.collect.Sets;
import org.jf.dexlib2.iface.*;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.ReferenceInstruction;
import org.jf.dexlib2.iface.reference.FieldReference;
import org.jf.dexlib2.iface.reference.MethodReference;
import org.jf.dexlib2.iface.reference.StringReference;
import org.jf.dexlib2.iface.reference.TypeReference;
import org.jf.dexlib2.util.MethodUtil;

import javax.annotation.Nonnull;
import java.io.UTFDataFormatException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 分析并收集某个/多个类的所有引用
 * 并重新排序引用Index
 */
public class ReferencesAnalyzer {
    private int length = 0;
    private final Map<String, Integer> pool = new HashMap<>();
    private final List<Byte> String_Pool = new ArrayList<>();

    private final LinkedHashMap<MethodReference, Integer> methodReferencesMapIndex = new LinkedHashMap<>();
    private final List<MethodReference> methodReferences = new ArrayList<>();

    private final LinkedHashMap<FieldReference, Integer> fieldReferencesMapIndex = new LinkedHashMap<>();
    private final List<FieldReference> fieldReferences = new ArrayList<>();

    private final LinkedHashMap<TypeReference, Integer> typeReferencesMapIndex = new LinkedHashMap<>();
    private final List<TypeReference> typeReferences = new ArrayList<>();

    private final LinkedHashMap<StringReference, Integer> stringReferencesMapIndex = new LinkedHashMap<>();
    private final List<StringReference> stringReferences = new ArrayList<>();

    private final List<String> KeepMethodNames;

    public ReferencesAnalyzer(List<String> keepMethodNames) {
        this.KeepMethodNames = keepMethodNames;
    }

    public void parseClassDef(ClassDef classDef) {
        for (Method method : classDef.getMethods()) {
            if (KeepMethodNames.contains(method.getName())
                    || !"onCreate".equals(method.getName())
                    || method.getImplementation() == null)
                continue;
            collectReferences(method.getImplementation());
        }
    }

    public void makeReference() throws UTFDataFormatException {
        List<MethodReference> methodReferenceList = methodReferences.stream().distinct().collect(Collectors.toList());
        Collections.sort(methodReferenceList);
        for (int i = 0; i < methodReferenceList.size(); i++)
            methodReferencesMapIndex.put(methodReferenceList.get(i), i);

        List<FieldReference> fieldReferenceList = fieldReferences.stream().distinct().collect(Collectors.toList());
        Collections.sort(fieldReferenceList);
        for (int i = 0; i < fieldReferenceList.size(); i++)
            fieldReferencesMapIndex.put(fieldReferenceList.get(i), i);

        List<TypeReference> typeReferenceList = typeReferences.stream().distinct().collect(Collectors.toList());
        Collections.sort(typeReferenceList);
        for (int i = 0; i < typeReferenceList.size(); i++)
            typeReferencesMapIndex.put(typeReferenceList.get(i), i);

        List<StringReference> stringReferenceList = stringReferences.stream().distinct().collect(Collectors.toList());
        Collections.sort(stringReferenceList);
        for (int i = 0; i < stringReferenceList.size(); i++)
            stringReferencesMapIndex.put(stringReferenceList.get(i), i);

        for (MethodReference methodReference : methodReferenceList) {
            putPool(MethodHelper.genJniClass(methodReference.getDefiningClass()));
            putPool(methodReference.getName());
            putPool(MethodHelper.genMethodSig(methodReference));
            putPool(MethodHelper.genShorty(methodReference));
        }
        for (FieldReference fieldReference : fieldReferenceList) {
            putPool(MethodHelper.genJniClass(fieldReference.getDefiningClass()));
            putPool(fieldReference.getName());
            putPool(fieldReference.getType());
        }
        for (TypeReference typeReference : typeReferenceList)
            putPool(MethodHelper.genJniClass(typeReference.getType()));
        for (StringReference stringReference : stringReferenceList)
            putPool(stringReference.getString());
    }

    public int getMethodItemIndex(MethodReference reference) {
        return methodReferencesMapIndex.get(reference);
    }

    public int getFieldItemIndex(FieldReference reference) {
        return fieldReferencesMapIndex.get(reference);
    }

    public int getTypeItemIndex(TypeReference reference) {
        return typeReferencesMapIndex.get(reference);
    }

    public int getStringItemIndex(StringReference reference) {
        return stringReferencesMapIndex.get(reference);
    }

    private void collectReferences(MethodImplementation implementation) {
        for (Instruction instruction : implementation.getInstructions()) {
            switch (instruction.getOpcode()) {
                case IGET_BYTE:
                case IGET_BOOLEAN:
                case IGET_CHAR:
                case IGET_SHORT:
                case IGET:
                case IGET_WIDE:
                case IGET_OBJECT:
                case IPUT_BYTE:
                case IPUT_BOOLEAN:
                case IPUT_CHAR:
                case IPUT_SHORT:
                case IPUT:
                case IPUT_WIDE:
                case IPUT_OBJECT:
                case SGET_BYTE:
                case SGET_BOOLEAN:
                case SGET_CHAR:
                case SGET_SHORT:
                case SGET:
                case SGET_WIDE:
                case SGET_OBJECT:
                case SPUT_BYTE:
                case SPUT_BOOLEAN:
                case SPUT_CHAR:
                case SPUT_SHORT:
                case SPUT:
                case SPUT_WIDE:
                case SPUT_OBJECT: {
                    FieldReference reference = (FieldReference) ((ReferenceInstruction) instruction).getReference();
                    fieldReferences.add(reference);
                    break;
                }
                case CONST_STRING:
                case CONST_STRING_JUMBO: {
                    StringReference reference = (StringReference) ((ReferenceInstruction) instruction).getReference();
                    stringReferences.add(reference);
                    break;
                }
                case CONST_CLASS:
                case CHECK_CAST:
                case INSTANCE_OF:
                case NEW_INSTANCE:
                case NEW_ARRAY:
                case FILLED_NEW_ARRAY:
                case FILLED_NEW_ARRAY_RANGE: {
                    TypeReference reference = (TypeReference) ((ReferenceInstruction) instruction).getReference();
                    typeReferences.add(reference);
                    break;
                }
                case INVOKE_STATIC:
                case INVOKE_STATIC_RANGE:
                case INVOKE_DIRECT:
                case INVOKE_DIRECT_RANGE:
                case INVOKE_SUPER:
                case INVOKE_SUPER_RANGE:
                case INVOKE_INTERFACE:
                case INVOKE_INTERFACE_RANGE:
                case INVOKE_VIRTUAL:
                case INVOKE_VIRTUAL_RANGE: {
                    MethodReference reference = (MethodReference) ((ReferenceInstruction) instruction).getReference();
                    methodReferences.add(reference);
                    break;
                }
            }
        }
        for (TryBlock<? extends ExceptionHandler> block : implementation.getTryBlocks()) {
            for (ExceptionHandler exceptionHandler : block.getExceptionHandlers())
                if (exceptionHandler.getExceptionTypeReference() != null)
                    typeReferences.add(exceptionHandler.getExceptionTypeReference());
        }
    }

    public HashMap<MethodReference, Integer> getMethodReferencesMapIndex() {
        return methodReferencesMapIndex;
    }

    public HashMap<FieldReference, Integer> getFieldReferencesMapIndex() {
        return fieldReferencesMapIndex;
    }

    public HashMap<TypeReference, Integer> getTypeReferencesMapIndex() {
        return typeReferencesMapIndex;
    }

    public HashMap<StringReference, Integer> getStringReferencesMapIndex() {
        return stringReferencesMapIndex;
    }

    public int getOffset(String s) {
        return pool.get(s);
    }

    public void putPool(String s) throws UTFDataFormatException {
        if (!pool.containsKey(s)) {
            pool.put(s, length);
            byte[] bytes = ModifiedUtf8.encode(s);
            for (byte b : bytes) {
                String_Pool.add(b);
            }
            String_Pool.add((byte) 0);
            length += bytes.length + 1;
        }
    }

    public List<Byte> getString_Pool() {
        return String_Pool;
    }

}
