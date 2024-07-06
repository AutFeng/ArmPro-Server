package action;

import java.util.HashMap;

public enum ActionEnumsTest {
    SHELL(0x1, "Shell"),
    ASSETS_ENCRYPT(0x2, "Assets_encrypt"),
    XML_ENCRYPT(0x4, "Xml_encrypt"),
    STRING_ENCRYPT(0x8, "String_encrypt"),
    INVOKEDYNAMIC(0x10, "InvokeDynamic"),
    NUMBERS_ENCRYPT(0x20, "Numbers_encrypt"),
    ASSETS_FULL_ENCRYPT(0x40, "Assets_full_encrypt"),
    FLOWOBFUSCATOR(0x80, "FlowObfuscator"),
    VPN_CHECK(0x100, "VpnCheck"),
    XPOSED_CHECK(0x200, "XposedCheck"),
    HOOK_VER(0x400, "HookVer"),
    HOOK_INSTALL(0x800, "HookInstall"),
    HOOK_SIGN(0x1000, "HookSigner"),
    DEX2C(0x2000, "Dex2C"),
    DEX2C_OLLVM(0x4000, "Dex2C-Ollvm"),
    RESFULL(0x8000, "ResFull"),
    REMOVE_DEBUG(0x10000, "Remove_debug"),
    INSERT_DEBUG(0x20000, "Insert_debug"),
    METHOD_SEPARATE(0x40000, "Method_separation"),
    SIGNERV1(0x80000, "SignerV1_Check"),
    SIGNERV2(0x100000, "SignerV2_Check"),
    EMULATARCHECK(0x200000, "Emulator_Check"),
    JIAGUV1(0x400000, "JiaGu_V1"),
    JIAGUV2(0x800000, "JiaGu_V2"),
    DIALOGHOOK(0x1000000, "Dialog_Hook"),
    SIGN_PRO(0x2000000, "HookSign_Pro"),
    PROXYHOOK(0x4000000, "Proxy_Hook"),
    PROXYHOOK1(0x8000000, "Proxy_Hook1"),
    PROXYHOOK2(0x10000000, "Proxy_Hook2"),
    PROXYHOOK3(0x20000000, "Proxy_Hook3"),
    PROXYHOOK4(0x40000000, "Proxy_Hook4"),
    PROXYHOOK5(0x80000000L, "Proxy_Hook5"),
    PROXYHOOK6(0x100000000L, "Proxy_Hook6"),
    PROXYHOOK7(0x200000000L, "Proxy_Hook7");
//    PROXYHOOK8(0x400000000L, "Proxy_Hook8"),
//    PROXYHOOK9(0x800000000L, "Proxy_Hook9"),
//    PROXYHOOK10(0x1000000000L, "Proxy_Hook10"),
//    PROXYHOOK11(0x2000000000L, "Proxy_Hook11");
//    PROXYHOOK12(0x4000000000L, "Proxy_Hook12"),
//    PROXYHOOK13(0x8000000000L, "Proxy_Hook13"),
//    PROXYHOOK14(0x10000000000L, "Proxy_Hook14"),
//    PROXYHOOK15(0x20000000000L, "Proxy_Hook15"),
//    PROXYHOOK16(0x40000000000L, "Proxy_Hook16"),
//    PROXYHOOK17(0x80000000000L, "Proxy_Hook17"),
//    PROXYHOOK18(0x100000000000L, "Proxy_Hook18"),
//    PROXYHOOK19(0x200000000000L, "Proxy_Hook19"),
//    PROXYHOOK20(0x400000000000L, "Proxy_Hook20"),
//    PROXYHOOK21(0x800000000000L, "Proxy_Hook19"),
//    PROXYHOOK22(0x1000000000000L, "Proxy_Hook19"),
//    PROXYHOOK23(0x2000000000000L, "Proxy_Hook19"),
//    PROXYHOOK24(0x4000000000000L, "Proxy_Hook19"),
//    PROXYHOOK25(0x8000000000000L, "Proxy_Hook19"),
//    PROXYHOOK26(0x10000000000000L, "Proxy_Hook19"),
//    PROXYHOOK27(0x20000000000000L, "Proxy_Hook19"),
//    PROXYHOOK28(0x40000000000000L, "Proxy_Hook19"),
//    PROXYHOOK29(0x80000000000000L, "Proxy_Hook19"),
//    PROXYHOOK30(0x100000000000000L, "Proxy_Hook19"),
//    PROXYHOOK31(0x200000000000000L, "Proxy_Hook19"),
//    PROXYHOOK32(0x400000000000000L, "Proxy_Hook19"),
//    PROXYHOOK33(0x800000000000000L, "Proxy_Hook19"),
//    PROXYHOOK34(0x1000000000000000L, "Proxy_Hook19"),
//    PROXYHOOK35(0x2000000000000000L, "Proxy_Hook19"),
//    PROXYHOOK36(0x4000000000000000L, "Proxy_Hook19"),
//    PROXYHOOK37(0x8000000000000000L, "Proxy_Hook37");
    private long type;
    private String flagName;

    private final static ActionEnumsTest[] allFlags;
    private static HashMap<String, ActionEnumsTest> FlagsByName;

    static {
        allFlags = ActionEnumsTest.values();
        FlagsByName = new HashMap<>();
        for (ActionEnumsTest Flag : allFlags) {
            FlagsByName.put(Flag.flagName, Flag);
        }
    }

    public static ActionEnumsTest[] getFlags(long FlagValue) {
        int size = 0;
        for (ActionEnumsTest Flag : allFlags) {
            if ((FlagValue & Flag.type) != 0) {
                size++;
            }
        }

        ActionEnumsTest[] Flags = new ActionEnumsTest[size];
        int FlagsPosition = 0;
        for (ActionEnumsTest Flag : allFlags) {
            if ((FlagValue & Flag.type) != 0) {
                Flags[FlagsPosition++] = Flag;
            }
        }
        return Flags;
    }

    ActionEnumsTest(long type, String flagName) {
        this.type = type;
        this.flagName = flagName;
    }

    public long getType() {
        return type;
    }

    public String getFlagName() {
        return flagName;
    }
}
