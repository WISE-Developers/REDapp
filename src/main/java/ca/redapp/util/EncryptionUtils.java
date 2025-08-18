package ca.redapp.util;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class EncryptionUtils {

    protected SecretKey secretKey;
    private RPreferences prefs;


    public EncryptionUtils(RPreferences _prefs){
        if(_prefs != null){
            prefs = _prefs;
            String key = prefs.getString("EncryptionKey", "");
            if(key != null && !key.isEmpty()){
                secretKey = convertStringToSecretKeyto(key);
            }
        }
    }

    private  SecretKey convertStringToSecretKeyto(String encodedKey) {
        byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
        SecretKey originalKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
        return originalKey;
    }

    // Method to decrypt a string
    public  String decrypt(String encryptedData) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
        return new String(decryptedBytes);
    }
}
