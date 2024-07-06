package armadillo.utils;

import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.TokenSource;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.writer.builder.DexBuilder;
import org.jf.smali.LexerErrorInterface;
import org.jf.smali.smaliFlexLexer;
import org.jf.smali.smaliParser;
import org.jf.smali.smaliTreeWalker;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class SmaliUtils {
    public static ClassDef assembleSmali(byte[] smali)
            throws Exception {
        InputStreamReader reader = new InputStreamReader(new BufferedInputStream(new ByteArrayInputStream(smali)), StandardCharsets.UTF_8);
        LexerErrorInterface lexer = new smaliFlexLexer(reader, 15);
        CommonTokenStream tokens = new CommonTokenStream((TokenSource) lexer);
        smaliParser parser = new smaliParser(tokens);
        parser.setVerboseErrors(false);
        parser.setAllowOdex(false);
        parser.setApiLevel(15);
        smaliParser.smali_file_return result = parser.smali_file();
        if (parser.getNumberOfSyntaxErrors() > 0 || lexer.getNumberOfSyntaxErrors() > 0) {
            return null;
        }
        CommonTree t = result.getTree();
        CommonTreeNodeStream treeStream = new CommonTreeNodeStream(t);
        treeStream.setTokenStream(tokens);
        smaliTreeWalker dexGen = new smaliTreeWalker(treeStream);
        dexGen.setApiLevel(15);
        dexGen.setVerboseErrors(false);
        dexGen.setDexBuilder(new DexBuilder(Opcodes.getDefault()));
        return dexGen.smali_file();
    }
}
