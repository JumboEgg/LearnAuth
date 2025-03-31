package ssafy.d210.backend.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ssafy.d210.backend.exception.service.DecryptedException;
import ssafy.d210.backend.exception.service.EncryptedException;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class AES256Util {

    private final String key;
    private final String iv;

    public AES256Util(@Value("${encryption.key") String key) {
        this.key = key;
        this.iv = key.substring(0, 16);
    }

    public String encrypt(String text) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(iv.getBytes(StandardCharsets.UTF_8));
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

            byte[] encrypted = cipher.doFinal(text.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new EncryptedException("암호화 중 오류가 발생했습니다.", e);
        }
    }

    public String decrypt(String encrypted) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(iv.getBytes(StandardCharsets.UTF_8));
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

            byte[] decodedBytes = Base64.getDecoder().decode(encrypted);
            byte[] decrypted = cipher.doFinal(decodedBytes);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new DecryptedException("복호화 중 오류가 발생했습니다.", e);
        }
    }
}
