package armadillo.utils;

import java.util.UUID;

public class CardRadom {
    public static String radomCard() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            if (i == 3)
                stringBuilder.append(SHAUtils.SHA1(UUID.randomUUID().toString() + System.currentTimeMillis()).toUpperCase(), 10, 14);
            else
                stringBuilder.append(SHAUtils.SHA1(UUID.randomUUID().toString() + System.currentTimeMillis()).toUpperCase(), 10, 14).append("-");
        }
        return stringBuilder.toString();
    }
}
