package oss.backend.util;

import java.security.SecureRandom;

public class SequenceUtils {
    private static final String NUMBERS = "0123456789";

    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private static final String CHARACTERS = ALPHABET.toUpperCase() + ALPHABET.toLowerCase() ;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public static String getCharactersToken(int length) {
        return generateSequence(CHARACTERS, length);
    }

    public static String getNumbersCode(int length) {
        return generateSequence(NUMBERS, length);
    }

    private static String generateSequence(String symbols, int length) {
        StringBuilder result = new StringBuilder(length);
        for (int idx = 0; idx < length; idx++) {
            result.append(symbols.charAt(SECURE_RANDOM.nextInt(symbols.length())));
        }
        return result.toString();
    }
}
