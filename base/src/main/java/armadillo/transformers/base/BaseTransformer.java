package armadillo.transformers.base;

import armadillo.Constant;
import armadillo.common.SimpleNameFactory;
import armadillo.enums.LanguageEnums;
import org.apache.log4j.Logger;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.immutable.reference.ImmutableMethodProtoReference;
import org.jf.dexlib2.rewriter.RewriterModule;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public abstract class BaseTransformer extends RewriterModule implements Comparable<BaseTransformer> {
    protected final Logger logger = Logger.getLogger(BaseTransformer.class);
    protected final SimpleNameFactory nameFactory = new SimpleNameFactory();
    protected LanguageEnums languageEnums;

    public void setLanguageEnums(LanguageEnums languageEnums) {
        this.languageEnums = languageEnums;
    }

    public LanguageEnums getLanguageEnums() {
        return languageEnums;
    }

    public Logger getLogger() {
        return logger;
    }

    public abstract void transform() throws Exception;

    public abstract String getResult();

    public abstract int priority();

    public Set<? extends ClassDef> getNewClassDef() {
        return null;
    }

    public Opcodes getOpcodes() {
        return null;
    }

    public List<ImmutableMethodProtoReference> getMethodProtoReferences() {
        return null;
    }

    public List<String> getIgnores() {
        return null;
    }

    protected boolean exec(String[] command) throws IOException, InterruptedException, ThreadDeath {
        Process process = null;
        try {
            if (Constant.isDevelopment())
                logger.info(String.format("执行外部命令:%s", Arrays.toString(command)));
            process = Runtime.getRuntime().exec(command);
            InputStream inputStream = process.getInputStream();
            LineNumberReader stream = new LineNumberReader(new InputStreamReader(inputStream));
            while (stream.readLine() != null) {
            }
            int exit = process.waitFor();
            if (Constant.isDevelopment())
                logger.info("返回值:" + exit);
            return exit == 0;
        } catch (ThreadDeath threadDeath) {
            if (process != null) {
                if (Constant.isDevelopment())
                    logger.info("销毁Process");
                process.destroy();
            }
            throw new ThreadDeath();
        } finally {
            if (process != null)
                process.destroy();
        }
    }
}
