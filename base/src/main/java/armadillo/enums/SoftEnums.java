package armadillo.enums;

public enum SoftEnums {
    SingleVerify(0x1),
    DrainageGroup(0x2),
    LoginVerify(0x4),
    Notice(0x8),
    Update(0x10),
    CustomModule(0x20),
    Share(0x40),
    Admob(0x80);
    private final int type;

    SoftEnums(int type) {
        this.type = type;
    }

    private final static SoftEnums[] allFlags;

    static {
        allFlags = SoftEnums.values();
    }

    public static SoftEnums[] getFlags(int FlagValue) {
        int size = 0;
        for (SoftEnums Flag : allFlags) {
            if ((FlagValue & Flag.type) != 0) {
                size++;
            }
        }
        SoftEnums[] Flags = new SoftEnums[size];
        int FlagsPosition = 0;
        for (SoftEnums Flag : allFlags) {
            if ((FlagValue & Flag.type) != 0) {
                Flags[FlagsPosition++] = Flag;
            }
        }
        return Flags;
    }

    public int getType() {
        return type;
    }
}
