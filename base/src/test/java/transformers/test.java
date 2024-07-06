package transformers;

import armadillo.transformers.base.BaseTransformer;
import armadillo.transformers.base.DexTransformer;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class test {
    @Test
    public void test() throws Exception {
        List<BaseTransformer> transformers = new ArrayList<>();
        transformers.add(new demo());
        for (BaseTransformer transformer : transformers) {
            if (transformer instanceof DexTransformer) {
                DexTransformer dexTransformer = (DexTransformer) transformer;
               // dexTransformer.init(null, "Test", null, null);
            }
            transformer.transform();
        }
    }
}
