package armadillo.transformers.obfuscators.debug;

import armadillo.common.SimpleNameFactory;
import armadillo.transformers.base.BaseTransformer;
import armadillo.transformers.base.DexTransformer;
import armadillo.utils.StringRandom;
import armadillo.utils.SysConfigUtil;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.builder.MutableMethodImplementation;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.MethodParameter;
import org.jf.dexlib2.iface.debug.DebugItem;
import org.jf.dexlib2.immutable.ImmutableClassDef;
import org.jf.dexlib2.immutable.ImmutableMethod;
import org.jf.dexlib2.immutable.ImmutableMethodImplementation;
import org.jf.dexlib2.immutable.ImmutableMethodParameter;
import org.jf.dexlib2.immutable.debug.ImmutableLineNumber;
import org.jf.dexlib2.immutable.reference.ImmutableMethodProtoReference;
import org.jf.dexlib2.rewriter.Rewriter;
import org.jf.dexlib2.rewriter.Rewriters;

import javax.annotation.Nonnull;
import java.util.*;

public class InsertDebug extends DexTransformer {
    private int insert_total;
    @Nonnull
    @Override
    public Rewriter<ClassDef> getClassDefRewriter(@Nonnull Rewriters rewriters) {
        return new Rewriter<ClassDef>() {
            @Nonnull
            @Override
            public ClassDef rewrite(@Nonnull ClassDef classDef) {
                List<Method> methods = new ArrayList<>();
                for (Method method : classDef.getMethods()) {
                    if (method.getImplementation() != null) {
                        /**
                         * 随机参数名
                         */
                        List<MethodParameter> methodParameters = new ArrayList<>();
                        for (MethodParameter methodParameter : method.getParameters())
                            methodParameters.add(new ImmutableMethodParameter(
                                    methodParameter.getType(),
                                    methodParameter.getAnnotations(),
                                    nameFactory.randomName()));
                        /**
                         * 插入line
                         */
                        MutableMethodImplementation mutableMethodImplementation = new MutableMethodImplementation(method.getImplementation());
                        List<DebugItem> debugItems = new ArrayList<>();
                        for (int i = 0; i < mutableMethodImplementation.getInstructions().size(); i++)
                            debugItems.add(new ImmutableLineNumber(mutableMethodImplementation.newLabelForIndex(i).getCodeAddress(), new Random().nextInt(999999999)));
                        /**
                         * 生成新的Method
                         */
                        methods.add(new ImmutableMethod(
                                method.getDefiningClass(),
                                method.getName(),
                                methodParameters,
                                method.getReturnType(),
                                method.getAccessFlags(),
                                method.getAnnotations(),
                                method.getHiddenApiRestrictions(),
                                new ImmutableMethodImplementation(
                                        method.getImplementation().getRegisterCount(),
                                        method.getImplementation().getInstructions(),
                                        method.getImplementation().getTryBlocks(),
                                        debugItems)));
                    } else
                        methods.add(method);
                }
                insert_total++;
                return new ImmutableClassDef(
                        classDef.getType(),
                        classDef.getAccessFlags(),
                        classDef.getSuperclass(),
                        classDef.getInterfaces(),
                        nameFactory.randomName(),
                        classDef.getAnnotations(),
                        classDef.getFields(),
                        methods);
            }
        };
    }

    @Override
    public void transform() throws Exception {
        insert_total = 0;
    }

    @Override
    public String getResult() {
        return String.format(Objects.requireNonNull(SysConfigUtil.getLanguageConfigUtil(getLanguageEnums(), "debug.insert")), insert_total);
    }

    @Override
    public int priority() {
        return 1;
    }

    @Override
    public int compareTo(BaseTransformer o) {
        return priority() - o.priority();
    }
}
