package armadillo.transformers.jiagu.converter;


import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.Opcodes;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class NoneInstructionRewriter extends InstructionRewriter {
    public NoneInstructionRewriter() {
        super(Opcodes.forDexVersion(39));
    }

    @Override
    public int replaceOpcode(Opcode opcode) {
        final Short value = opcodes.getOpcodeValue(opcode);
        if (value == null) {
            throw new RuntimeException("Invalid opcode " + opcode);
        }
        return value;
    }

    @Nonnull
    @Override
    protected List<Opcode> getOpcodeList() {
        final List<Opcode> opcodeList = new ArrayList<>();
        for (int i = 0; i < 256; i++) {
            opcodeList.add(opcodes.getOpcodeByValue(i));
        }
        return opcodeList;
    }
}