package com.example.eksamensprojekt.utils;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {

    private PasswordUtil() {

    }

    // https://www.mindrot.org/projects/jBCrypt/ dokumentation for forståelse
    // hasher plain password med salt
    //Only used for registration and password change
    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    // checker om plainPassword hash er samme som hashedPassword
    //only used for login authentication
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
    /* evt. en validate method ?
    public static void validatePasswordStrength(String password)
    - Checks: Length, uppercase, lowercase, digit
    - Used: Before hashing (registration, password change)
 */
}
