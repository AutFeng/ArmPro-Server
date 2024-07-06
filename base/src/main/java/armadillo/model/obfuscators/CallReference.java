package armadillo.model.obfuscators;

import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.iface.reference.Reference;

public class CallReference {
    private Reference reference;
    private Opcode opcode;

    public CallReference(Reference reference, Opcode opcode) {
        this.reference = reference;
        this.opcode = opcode;
    }

    public Reference getReference() {
        return reference;
    }

    public void setReference(Reference reference) {
        this.reference = reference;
    }

    public Opcode getOpcode() {
        return opcode;
    }

    public void setOpcode(Opcode opcode) {
        this.opcode = opcode;
    }
}
