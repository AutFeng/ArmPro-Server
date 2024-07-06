package transformers;

import armadillo.transformers.base.BaseTransformer;
import armadillo.transformers.base.DexTransformer;
import org.jf.dexlib2.iface.ClassDef;

import java.util.Set;

public class demo extends DexTransformer {
    @Override
    public void transform() throws Exception {
        System.out.println(String.format("%s", getConfiguration()));
    }

    @Override
    public String getResult() {
        return null;
    }

    @Override
    public int priority() {
        return 0;
    }

    @Override
    public Set<? extends ClassDef> getNewClassDef() {
        return null;
    }

    @Override
    public int compareTo(BaseTransformer o) {
        return 0;
    }
}
