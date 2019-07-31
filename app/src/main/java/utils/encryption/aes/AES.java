package utils.encryption.aes;

import android.annotation.SuppressLint;
import android.os.Build;
import android.support.annotation.IntDef;
import android.text.TextUtils;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import test.mxm.android_app_template.BuildConfig;

public class AES {
    private static final String TAG = "AES";

    private final static String SHA1_PRNG = "SHA1PRNG";
    private static final int KEY_SIZE = 32;

    /**
     * Aes加密/解密
     *
     * @param content  字符串
     * @param secretKey 密钥
     * @param type     加密：{@link Cipher#ENCRYPT_MODE}，解密：{@link Cipher#DECRYPT_MODE}
     * @return 加密/解密结果字符串
     */
    @SuppressLint({"DeletedProvider", "GetInstance"})
    public static String des(String content, String secretKey, @AESType int type) {
        if (TextUtils.isEmpty(content) || TextUtils.isEmpty(secretKey)) {
            return null;
        }
        try {
            SecretKeySpec secretKeySpec;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {//Crypto provider 在 Android N（7.0）中已弃用，在Android P（9.0） 中已删除。
                secretKeySpec = deriveKeyInsecurely(secretKey);
            } else {
                secretKeySpec = fixSmallVersion(secretKey);
            }
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(type, secretKeySpec);
            if (type == Cipher.ENCRYPT_MODE) {//加密
                byte[] byteContent = content.getBytes("utf-8");
                return byte2HexString(cipher.doFinal(byteContent));
            } else {//解密
                byte[] byteContent = hexString2Byte(content);
                return new String(cipher.doFinal(byteContent));
            }
        } catch (NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException |
                UnsupportedEncodingException | InvalidKeyException | NoSuchPaddingException |
                NoSuchProviderException e) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "des: ", e);
            }
        }
        return null;
    }

    /*
     * 生成随机数，可以当做动态的密钥 加密和解密的密钥必须一致，不然将不能解密
     */
    public static String generateKey() {
        try {
            SecureRandom localSecureRandom = SecureRandom.getInstance(SHA1_PRNG);
            byte[] bytesKey = new byte[16];
            localSecureRandom.nextBytes(bytesKey);
            String strKey = byte2HexString(bytesKey);
            return strKey;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @SuppressLint("DeletedProvider")
    private static SecretKeySpec fixSmallVersion(String secretKey) throws NoSuchAlgorithmException, NoSuchProviderException {
        KeyGenerator generator = KeyGenerator.getInstance("AES");
        SecureRandom secureRandom;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            secureRandom = SecureRandom.getInstance(SHA1_PRNG, new CryptoProvider());
        } else {
            secureRandom = SecureRandom.getInstance(SHA1_PRNG, "Crypto");
        }
        secureRandom.setSeed(secretKey.getBytes());
        generator.init(128, secureRandom);
        byte[] enCodeFormat = generator.generateKey().getEncoded();
        return new SecretKeySpec(enCodeFormat, "AES");
    }

    private static SecretKeySpec deriveKeyInsecurely(String secretKey) {
        byte[] passwordBytes = secretKey.getBytes(StandardCharsets.US_ASCII);
        return new SecretKeySpec(InsecureSHA1PRNGKeyDerivator.deriveInsecureKey(passwordBytes, KEY_SIZE), "AES");
    }

    private static final char[] hex = new char[]{
        '0', '1', '2', '3', '4', '5', '6', '7',
        '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    };
    //字节数组转16进制字符串
    private static String byte2HexString(byte buf[]) {

        StringBuilder sb = new StringBuilder();
//        String tp;
        for (byte b : buf) {
            //一：
//            sb.append(String.format("%02X", b & 0xFF));//性能最差，差的不止一个数量级的
            //二：性能第三
//            sb.append(Integer.toHexString((b >>> 4) & 0x0F));//byte会被强转成int类型，从而带来符号问题，所以需要（& 0x0F）
//            sb.append(Integer.toHexString(b & 0x0F));
            //三：性能第二
//            tp = Integer.toHexString(b & 0xFF);//byte会被强转成int类型，同理
//            if (tp.length() < 2) sb.append('0');
//            sb.append(tp);
            //四：性能最好
            sb.append(hex[(b >>> 4) & 0x0F]);
            sb.append(hex[b & 0x0F]);
        }
        return sb.toString();
    }

    //16进制转字节数组
    private static byte[] hexString2Byte(String hexStr) {
        final int len = hexStr.length();
        if (len < 1) return null;
        byte[] result = new byte[len / 2];
        for (int i = 0, index = i * 2; i < len/2; i++, index = i * 2) {
            result[i] = (byte) Integer.parseInt(hexStr.substring(index, index + 2), 16);
        }
        return result;
    }

    @IntDef({Cipher.ENCRYPT_MODE, Cipher.DECRYPT_MODE})
    @interface AESType {}

    private static final class CryptoProvider extends Provider {
        CryptoProvider() {
            super("Crypto", 1.0, "HARMONY (SHA1 digest; SecureRandom; SHA1withDSA signature)");
            put("SecureRandom.SHA1PRNG", "org.apache.harmony.security.provider.crypto.SHA1PRNG_SecureRandomImpl");
            put("SecureRandom.SHA1PRNG ImplementedIn", "Software");
        }
    }
}
