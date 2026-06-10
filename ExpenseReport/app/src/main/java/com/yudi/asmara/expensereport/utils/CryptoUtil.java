package com.yudi.asmara.expensereport.utils;

import android.util.Base64;

import com.yudi.asmara.expensereport.utils.AppConfig;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class CryptoUtil {

    private static SecretKeySpec getKey() throws Exception {
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] key = AppConfig.SECURE_PREF_PASSWORD.getBytes(StandardCharsets.UTF_8);
        key = sha.digest(key);
        return new SecretKeySpec(key, "AES");
    }

    public static String encrypt(String data) {
        try {
            SecretKeySpec secretKey = getKey();
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return Base64.encodeToString(cipher.doFinal(data.getBytes(StandardCharsets.UTF_8)), Base64.DEFAULT);
        } catch (Exception e) {
            return null;
        }
    }

    public static String decrypt(String data) {
        try {
            SecretKeySpec secretKey = getKey();
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return new String(cipher.doFinal(Base64.decode(data, Base64.DEFAULT)), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return null;
        }
    }

}
