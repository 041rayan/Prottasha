package com.prottasha.auth.domain;

public final class PasswordPolicy {

    public static final int MIN_LENGTH = 8;
    public static final int MAX_LENGTH = 4096;

    public enum Result {
        VALID,
        TOO_SHORT,
        TOO_LONG,
        NEEDS_UPPERCASE,
        NEEDS_LOWERCASE,
        NEEDS_NUMBER
    }

    private PasswordPolicy() {
    }

    public static Result validate(String password) {
        if (password == null || password.length() < MIN_LENGTH) {
            return Result.TOO_SHORT;
        }
        if (password.length() > MAX_LENGTH) {
            return Result.TOO_LONG;
        }

        boolean uppercase = false;
        boolean lowercase = false;
        boolean number = false;
        for (int offset = 0; offset < password.length();) {
            int codePoint = password.codePointAt(offset);
            uppercase |= Character.isUpperCase(codePoint);
            lowercase |= Character.isLowerCase(codePoint);
            number |= Character.isDigit(codePoint);
            offset += Character.charCount(codePoint);
        }
        if (!uppercase) {
            return Result.NEEDS_UPPERCASE;
        }
        if (!lowercase) {
            return Result.NEEDS_LOWERCASE;
        }
        return number ? Result.VALID : Result.NEEDS_NUMBER;
    }
}
