package armadillo.utils;

import armadillo.common.SimpleNameFactory;

import java.util.Random;

public class StringRandom {
    public static String RandomString() {
        //return RandomString(40);
        return new SimpleNameFactory().randomName();
    }

    public static String RandomString(int size) {
        int start = Integer.parseInt("064B", 16);
        int end = Integer.parseInt("065F", 16);
        char[] chars = new char[size];
        for (int i = 0; i < size; i++)
            chars[i] = (char) (new Random().nextInt(end - start + 1) + start);
        return new String(chars);
    }
}
