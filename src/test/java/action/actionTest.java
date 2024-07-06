package action;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class actionTest {
    @Test
    public void testaction() {
        //System.out.println(ActionEnumsTest.PROXYHOOK10.getType() | ActionEnumsTest.PROXYHOOK11.getType());
        long type = 0;
        List<ActionEnumsTest> actionEnumsTests = new ArrayList<>();
        actionEnumsTests.add(ActionEnumsTest.PROXYHOOK6);
        for (ActionEnumsTest enumsTest : actionEnumsTests) {
            type = type|enumsTest.getType();
        }
        for (ActionEnumsTest flag : ActionEnumsTest.getFlags(
                type
                //| ActionEnumsTest.SHELL.getType()
                //| ActionEnumsTest.XML_ENCRYPT.getType()
                //| ActionEnumsTest.STRING_ENCRYPT.getType()
                //| ActionEnumsTest.INVOKEDYNAMIC.getType()
                //| ActionEnumsTest.NUMBERS_ENCRYPT.getType()
                //| ActionEnumsTest.ASSETS_FULL_ENCRYPT.getType()
                //| ActionEnumsTest.FLOWOBFUSCATOR.getType()
                //| ActionEnumsTest.VPN_CHECK.getType()
                //| ActionEnumsTest.PROXYHOOK11.getType()
        )) {
            System.out.println(flag.getFlagName());
        }
//        for (ActionEnumsTest flag : ActionEnumsTest.getFlags(
//                ActionEnumsTest.SHELL.getType())) {
//            System.out.println(flag.getFlagName());
//        }
//        System.out.println(actionTest.class.getName());
    }
}
