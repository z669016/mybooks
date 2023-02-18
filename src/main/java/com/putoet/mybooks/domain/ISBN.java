package com.putoet.mybooks.domain;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Based on gist <a href="https://gist.github.com/kymmt90/a45ae122faeb78096b2c">kymmt90/Isbn.java</a>
 */
record ISBN(String original, String prefix, String group, String publisher, String bookName, String checkDigit) {
    public static final int LENGTH = 13;
    public static final int OLD_LENGTH = 10;

    public static boolean isValid(String numberSequence) {
        Objects.requireNonNull(numberSequence);

        if (!Pattern.matches("^\\d+(-?\\d+)*$", numberSequence)) return false;

        final String normalizedSequence = removeHyphen(numberSequence);
        return switch (normalizedSequence.length()) {
            case 13 -> isValidAsIsbn13(normalizedSequence);
            case 10 -> isValidAsIsbn10(normalizedSequence);
            default -> false;
        };
    }

    private static boolean isValidAsIsbn13(String number) {
        Objects.requireNonNull(number);

        if (!Pattern.matches("^\\d{" + LENGTH + "}$", number)) throw new IllegalArgumentException();

        final char[] digits = number.toCharArray();
        final int myDigit = computeIsbn13CheckDigit(digits);
        final int checkDigit = digits[LENGTH - 1] - '0';
        return myDigit == 10 && checkDigit == 0 || myDigit == checkDigit;
    }

    private static int computeIsbn13CheckDigit(char[] digits) {
        checkDigits(digits, LENGTH);

        final int[] weights = {1, 3};
        int sum = 0;
        for (int i = 0; i < LENGTH - 1; ++i) {
            sum += (digits[i] - '0') * weights[i % 2];
        }
        return (10 - sum % 10) % 10;
    }

    private static void checkDigits(char[] digits, int length) {
        Objects.requireNonNull(digits);

        if (digits.length != length && digits.length != length - 1) throw new IllegalArgumentException();
        for (char c : digits) {
            if (c < '0' || '9' < c) throw new IllegalArgumentException();
        }
    }

    private static boolean isValidAsIsbn10(String number) {
        Objects.requireNonNull(number);

        if (!Pattern.matches("^\\d{" + OLD_LENGTH + "}$", number)) throw new IllegalArgumentException();

        final char[] digits = number.toCharArray();
        final int myDigit = computeIsbn10CheckDigit(digits);
        if (myDigit == 10) return digits[9] == 'X';
        final int checkDigit = digits[9] - '0';
        return myDigit == 11 && checkDigit == 0 || myDigit == checkDigit;
    }

    private static int computeIsbn10CheckDigit(char[] digits) {
        checkDigits(digits, OLD_LENGTH);

        int sum = 0;
        for (int i = 0, weight = 10; i < 9; ++i, --weight) {
            sum += (digits[i] - '0') * weight;
        }
        return (11 - sum % 11) % 11;
    }

    private static String toISBN13(String isbn10) {
        Objects.requireNonNull(isbn10);

        final String normalizedNumber = removeHyphen(isbn10);
        if (normalizedNumber.length() != OLD_LENGTH) throw new IllegalArgumentException();

        final String isbn13 = "978" + normalizedNumber.substring(0, OLD_LENGTH - 1);
        final int checkDigit = computeIsbn13CheckDigit(isbn13.toCharArray());

        if (isbn10.contains("-")) {
            return "978-" + isbn10.substring(0, isbn10.length() - 2) + "-" + checkDigit;
        } else {
            return "978" + isbn10.substring(0, isbn10.length() - 1) + checkDigit;
        }
    }

    private static String removeHyphen(String s) {
        Objects.requireNonNull(s);

        return s.replace("-", "");
    }

    public static ISBN withISBN(String number) {
        Objects.requireNonNull(number);

        if (!isValid(number)) throw new IllegalArgumentException();

        if (removeHyphen(number).length() == OLD_LENGTH)
            number = toISBN13(number);

        final String[] numbers = number.split("-");
        return numbers.length == 1
                ? new ISBN(number, "", "", "", "", "")
                : new ISBN(number, numbers[0], numbers[1], numbers[2], numbers[3], numbers[4]);
    }

    @Override
    public String toString() {
        return original;
    }
}