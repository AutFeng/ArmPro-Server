import org.junit.Test;

import java.io.ByteArrayOutputStream;

public class decode {
    public static String KEY = "lh52000";
    private static final String hexString = "0123456789ABCDEF";

    public static String decode(String str) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(str.length() / 2);
        for (int i = 0; i < str.length(); i += 2) {
            baos.write((hexString.indexOf(str.charAt(i)) << 4) | hexString.indexOf(str.charAt(i + 1)));
        }
        byte[] b = baos.toByteArray();
        int len = b.length;
        int keyLen = KEY.length();
        for (int i2 = 0; i2 < len; i2++) {
            b[i2] = (byte) (b[i2] ^ KEY.charAt(i2 % keyLen));
        }
        return new String(b);
    }

    @Test
    public void ss(){
        System.out.println(Integer.MAX_VALUE);
        System.out.println(decode("88D3B5D689B8DFD0E4D18F90D6B4F38092BBD48D9088D4AFD68AB60F438E8EA3D7988D"));
    }
}
