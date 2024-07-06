package armadillo.transformers.protece.vm.converter;

import com.google.common.collect.Maps;
import org.jf.dexlib2.AccessFlags;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.base.reference.BaseFieldReference;
import org.jf.dexlib2.dexbacked.DexBackedClassDef;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.dexbacked.DexBackedOdexFile;
import org.jf.dexlib2.iface.Annotation;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.Field;
import org.jf.dexlib2.iface.reference.FieldReference;

import javax.annotation.Nonnull;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


public class ClassAnalyzer {
    private final Map<String, ClassDef> originClasses = Maps.newHashMap();
    private final ZipFile zipFile;
    private final String[] dex;

    public ClassAnalyzer(ZipFile zipFile, String[] dex) throws IOException {
        this.zipFile = zipFile;
        this.dex = dex;
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (entry.getName().startsWith(dex[0]) && entry.getName().endsWith(dex[1])) {
                DexBackedDexFile dexBackedDexFile = DexBackedDexFile.fromInputStream(Opcodes.getDefault(), new BufferedInputStream(zipFile.getInputStream(entry)));
                for (DexBackedClassDef classDef : dexBackedDexFile.getClasses()) {
                    originClasses.put(classDef.getType(), classDef);
                }
            }
        }
    }

    public FieldReference getDirectFieldRef(FieldReference reference) {
        final String fieldName = reference.getName();
        final String fieldType = reference.getType();
        final ClassDef classDef = originClasses.get(reference.getDefiningClass());
        if (classDef == null) {
            return reference;
        }
        final ClassDef newClassDef = findFieldDefiningClass(classDef, fieldName, fieldType);
        if (newClassDef != null) {
            return new BaseFieldReference() {
                @Nonnull
                @Override
                public String getDefiningClass() {
                    return newClassDef.getType();
                }

                @Nonnull
                @Override
                public String getName() {
                    return fieldName;
                }

                @Nonnull
                @Override
                public String getType() {
                    return fieldType;
                }
            };
        }
        return reference;
    }

    private ClassDef findFieldDefiningClass(ClassDef classDef, String fieldName, String fieldType) {
        if (classDef == null)
            return null;
        for (Field field : classDef.getStaticFields()) {
            if (field.getName().equals(fieldName) && field.getType().equals(fieldType))
                return classDef;
        }
        ClassDef definingClass = null;
        for (String defInterface : classDef.getInterfaces()) {
            definingClass = findFieldDefiningClass(originClasses.get(defInterface), fieldName, fieldType);
            if (definingClass != null)
                break;
        }
        if (definingClass == null)
            return findFieldDefiningClass(originClasses.get(classDef.getSuperclass()), fieldName, fieldType);
        return definingClass;

    }

    public boolean isExists(String type) {
        Set<String> set = originClasses.keySet();
        return set.contains(type);
    }
}