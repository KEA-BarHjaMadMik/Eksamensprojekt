package com.example.eksamensprojekt.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordUtilTest {

    @Test
    void hashPasswordGeneratesDifferentHashesEachTime() {
        String password = "test123";

        String hash1 = PasswordUtil.hashPassword(password);
        String hash2 = PasswordUtil.hashPassword(password);
        String hash3 = PasswordUtil.hashPassword(password);

        System.out.println("Hash 1: " + hash1);
        System.out.println("Hash 2: " + hash2);
        System.out.println("Hash 3: " + hash3);

        assertNotEquals(hash1, hash2);
        assertNotEquals(hash2, hash3);
        assertNotEquals(hash1, hash3);

        assertTrue(PasswordUtil.checkPassword(password, hash1));
        assertTrue(PasswordUtil.checkPassword(password, hash2));
        assertTrue(PasswordUtil.checkPassword(password, hash3));
    }
}
