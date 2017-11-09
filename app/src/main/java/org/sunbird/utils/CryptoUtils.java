package org.sunbird.utils;

import android.util.Base64;

import java.security.Key;
import java.security.MessageDigest;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by juspay on 4/18/17.
 */

public class CryptoUtils {
    private final static String LOG_TAG = CryptoUtils.class.getName();

    public static byte[] sha256Bytes(String s) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(s.getBytes("UTF-8"));
        } catch (Exception ex) {
//            Logger.e(LOG_TAG, "Error generating hash ", ex);
        }
        return null;
    }

    private static byte[] sha256Bytes(byte[] s) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(s);
        } catch (Exception ex) {
//            Logger.e(LOG_TAG, "Error generating hash ", ex);
        }
        return null;
    }

    public static String generateAesHexKey() {
        try {
            final KeyGenerator instance = KeyGenerator.getInstance("AES");
            instance.init(256);
            return bytesToHex(instance.generateKey().getEncoded());
        } catch (Exception ex) {
//            Logger.e(LOG_TAG, "Error generating aes key ", ex);
        }
        return null;
    }

    public static byte[] aesEncrypt(final byte[] array, final byte[] array2) throws Exception {
        final SecretKeySpec secretKeySpec = new SecretKeySpec(array2, "AES");
        final IvParameterSpec ivParameterSpec = new IvParameterSpec(new byte[16]);
        final Cipher instance = Cipher.getInstance("AES/CBC/PKCS7Padding");
        instance.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
        return instance.doFinal(array);
    }

    private static byte[] aesDecrypt(final byte[] array, final byte[] array2) throws Exception {
        final SecretKeySpec secretKeySpec = new SecretKeySpec(array2, "AES");
        final IvParameterSpec ivParameterSpec = new IvParameterSpec(new byte[16]);
        final Cipher instance = Cipher.getInstance("AES/CBC/PKCS7Padding");
        instance.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
        return instance.doFinal(array);
    }

    private static byte[] aesEncrypt(final byte[] array, String hexKey) throws Exception {
        return aesEncrypt(array, hexStringToByteArray(hexKey));
    }

    private static byte[] aesDecrypt(final byte[] array, String hexKey) throws Exception {
        return aesDecrypt(array, hexStringToByteArray(hexKey));
    }

    private static String bytesToHex(byte[] bytes) {
        final char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public static String getChecksum(String payload, String aesKey) {
        try {
            byte[] sha256 = sha256Bytes(payload);
            return Base64.encodeToString(aesEncrypt(sha256, hexStringToByteArray(aesKey)), Base64.DEFAULT);
        } catch (Exception exception) {
//            Logger.e(LOG_TAG, "Error while signing  ", exception);
        }
        return null;
    }

    private static String getHexString(byte[] hash) {
        if (hash != null) {
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        }
        return "";
    }

    @Deprecated
    private static Key generateKey(String keyValue) throws Exception {
        Key key = new SecretKeySpec(keyValue.getBytes("UTF-8"), "AES");
        return key;
    }

    @Deprecated
    public static String decrypt(String encryptedText, String keyText) throws Exception {
        Key key = generateKey(keyText);
        Cipher chiper = Cipher.getInstance("AES");
        chiper.init(Cipher.DECRYPT_MODE, key);
        byte[] decordedValue = Base64.decode(encryptedText, 0);
        byte[] decValue = chiper.doFinal(decordedValue);
        return new String(decValue);
    }

    private static byte[] generateKeyAes(String keyValue, String salt) throws Exception {
        KeySpec spec = new PBEKeySpec(keyValue.toCharArray(), salt.getBytes(), 1, 256); // AES-256
        SecretKeyFactory f = SecretKeyFactory.getInstance("PBEWITHSHA256AND128BITAES-CBC-BC");
        return f.generateSecret(spec).getEncoded();
    }

    public static String encryptAes(String plainText, String keyText, String salt) throws Exception {
        byte[] key = generateKeyAes(keyText, salt);
        return Base64.encodeToString(aesEncrypt(plainText.getBytes(), key), 0);
    }


    public static String decryptAes(String encryptedText, String keyText, String salt) throws Exception {
        byte[] key = generateKeyAes(keyText, salt);
        byte[] decodedValue = Base64.decode(encryptedText, 0);
        return new String(aesDecrypt(decodedValue, key));
    }

    public static String getSha256MessageDigestInHex(String message) {
        byte[] hash = sha256Bytes(message);
        return getHexString(hash);
    }

    public static String getSha256MessageDigestInHex(byte[] message) {
        byte[] hash = sha256Bytes(message);
        return getHexString(hash);
    }
}

