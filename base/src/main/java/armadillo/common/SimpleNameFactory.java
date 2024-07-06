package armadillo.common;

import armadillo.common.factory.NameFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SimpleNameFactory implements NameFactory {
    public String randomName() {
        return randomName(5);
    }

    public String randomName(int length) {
        StringBuilder stringBuffer = new StringBuilder();
        for (int i = 0; i < length; i++) {
            long result = Math.round(Math.random() * 25 + 97);
            stringBuffer.append((char) result);;
        }
        return stringBuffer.toString();
    }


    private static final int CHARACTER_COUNT = 26;

    private static final List cachedMixedCaseNames = new ArrayList();
    private static final List cachedLowerCaseNames = new ArrayList();

    private final boolean generateMixedCaseNames;
    private int index = 0;


    /**
     * Creates a new <code>SimpleNameFactory</code> that generates mixed-case names.
     */
    public SimpleNameFactory() {
        this(true);
    }


    /**
     * Creates a new <code>SimpleNameFactory</code>.
     *
     * @param generateMixedCaseNames a flag to indicate whether the generated
     *                               names will be mixed-case, or lower-case only.
     */
    public SimpleNameFactory(boolean generateMixedCaseNames) {
        this.generateMixedCaseNames = generateMixedCaseNames;
    }


    public void reset() {
        index = 0;
    }


    public String nextName() {
        if (index == 0)
            for (int i = 0; i < new Random().nextInt(CHARACTER_COUNT); i++)
                name(index++);
        return name(index++);
    }


    /**
     * Returns the name at the given index.
     */
    private String name(int index) {
        // Which cache do we need?
        List cachedNames = generateMixedCaseNames ?
                cachedMixedCaseNames :
                cachedLowerCaseNames;

        // Do we have the name in the cache?
        if (index < cachedNames.size()) {
            return (String) cachedNames.get(index);
        }

        // Create a new name and cache it.
        String name = newName(index);
        cachedNames.add(index, name);

        return name;
    }


    /**
     * Creates and returns the name at the given index.
     */
    private String newName(int index) {
        int totalCharacterCount = generateMixedCaseNames ?
                2 * CHARACTER_COUNT :
                CHARACTER_COUNT;

        int baseIndex = index / totalCharacterCount;
        int offset = index % totalCharacterCount;

        char newChar = charAt(offset);

        String newName = baseIndex == 0 ?
                new String(new char[]{newChar}) :
                (name(baseIndex - 1) + newChar);

        return newName;
    }

    private char charAt(int index) {
        return (char) ((index < CHARACTER_COUNT ? 'a' - 0 :
                'A' - CHARACTER_COUNT) + index);
    }

}
