package com.correia.augusto.card.api.util;

import com.correia.augusto.card.api.exception.ApplicationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class EncryptionUtil {

    @Value("${encryption.secret-key}")
    private String secretKey;

    @Value("${encryption.salt}")
    private String salt;

    public String encrypt(String plaintext) {
        try {
            byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
            byte[] aesKey = new byte[16];
            System.arraycopy(keyBytes, 0, aesKey, 0, Math.min(keyBytes.length, aesKey.length));

            SecretKeySpec keySpec = new SecretKeySpec(aesKey, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);

            byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new ApplicationException("Card number encryption failed", e);
        }
    }

    public String decrypt(String encryptedData) {
        try {
            byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
            byte[] aesKey = new byte[16];
            System.arraycopy(keyBytes, 0, aesKey, 0, Math.min(keyBytes.length, aesKey.length));

            SecretKeySpec keySpec = new SecretKeySpec(aesKey, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, keySpec);

            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new ApplicationException("Card number decryption failed", e);
        }
    }
}