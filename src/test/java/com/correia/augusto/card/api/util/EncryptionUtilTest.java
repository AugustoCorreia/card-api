package com.correia.augusto.card.api.util;

import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;

import static org.junit.Assert.*;

public class EncryptionUtilTest {


    @Test
    public void test_encrypt_returns_different_value_than_plaintext() {
        EncryptionUtil encryptionUtil = new EncryptionUtil();
        ReflectionTestUtils.setField(encryptionUtil, "secretKey", "testSecretKey123");
        ReflectionTestUtils.setField(encryptionUtil, "salt", "testSalt");
        String plaintext = "4111111111111111";
    
        String encrypted = encryptionUtil.encrypt(plaintext);
    
        assertNotNull(encrypted);
        assertNotEquals(plaintext, encrypted);
        assertFalse(encrypted.isEmpty());
        assertTrue(Base64.getDecoder().decode(encrypted).length > 0);
    }

    @Test
    public void test_encrypt_empty_string() {
        EncryptionUtil encryptionUtil = new EncryptionUtil();
        ReflectionTestUtils.setField(encryptionUtil, "secretKey", "testSecretKey123");
        ReflectionTestUtils.setField(encryptionUtil, "salt", "testSalt");
        String plaintext = "";
    
        String encrypted = encryptionUtil.encrypt(plaintext);
    
        assertNotNull(encrypted);
        assertNotEquals(plaintext, encrypted);
    
        String decrypted = encryptionUtil.decrypt(encrypted);
        assertEquals(plaintext, decrypted);
    }
}