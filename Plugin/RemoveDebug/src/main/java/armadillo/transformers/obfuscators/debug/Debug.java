package armadillo.transformers.obfuscators.debug;

import armadillo.transformers.base.BaseTransformer;
import armadillo.transformers.base.DexTransformer;
import armadillo.utils.SysConfigUtil;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.MethodParameter;
import org.jf.dexlib2.immutable.ImmutableClassDef;
import org.jf.dexlib2.immutable.ImmutableMethod;
import org.jf.dexlib2.immutable.ImmutableMethodImplementation;
import org.jf.dexlib2.immutable.ImmutableMethodParameter;
import org.jf.dexlib2.immutable.reference.ImmutableMethodProtoReference;
import org.jf.dexlib2.rewriter.Rewriter;
import org.jf.dexlib2.rewriter.Rewriters;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class Debug extends DexTransformer {
    private int remove_debug_total;

    @Nonnull
    @Override
    public Rewriter<ClassDef> getClassDefRewriter(@Nonnull Rewriters rewriters) {
        return new Rewriter<ClassDef>() {
            @Nonnull
            @Override
            public ClassDef rewrite(@Nonnull ClassDef value) {
                List<Method> methods = new ArrayList<>();
                for (Method method : value.getMethods()) {
                    if (method.getImplementation() != null) {
                        List<MethodParameter> methodParameters = new ArrayList<>();
                        for (MethodParameter methodParameter : method.getParameters())
                            methodParameters.add(new ImmutableMethodParameter(
                                    methodParameter.getType(),
                                    methodParameter.getAnnotations(),
                                    null));
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
                                        null)));
                    } else
                        methods.add(method);
                }
                remove_debug_total++;
                return new ImmutableClassDef(
                        value.getType(),
                        value.getAccessFlags(),
                        value.getSuperclass(),
                        value.getInterfaces(),
                        null,
                        value.getAnnotations(),
                        value.getFields(),
                        methods);
            }
        };
    }

    @Override
    public void transform() throws Exception {
        remove_debug_total = 0;
    }

    @Override
    public String getResult() {
        return String.format(Objects.requireNonNull(SysConfigUtil.getLanguageConfigUtil(getLanguageEnums(), "debug.remove")), remove_debug_total);
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
