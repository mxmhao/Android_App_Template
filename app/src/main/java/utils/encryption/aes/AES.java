package utils.encryption.aes;

import android.annotation.SuppressLint;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.IntDef;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
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
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import test.mxm.android_app_template.BuildConfig;

/*
* 安卓系统提供了多种密钥算法：
* https://developer.android.google.cn/guide/topics/security/cryptography
* https://developer.android.google.cn/training/articles/keystore
*/
//https://zhuanlan.zhihu.com/p/24255780
public class AES {
    private static final String TAG = "AES";

    private final static String SHA1_PRNG = "SHA1PRNG";

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

    private static final char[] hex = new char[]{
        '0', '1', '2', '3', '4', '5', '6', '7',
        '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    };
    //字节数组转16进制字符串
    private static String byte2HexString(byte[] buf) {
        char[] chs = new char[buf.length * 2];
//        StringBuilder sb = new StringBuilder(buf.length * 2);//字符串很长的时候用此类
//        String tp;
        int index = 0;
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
//            sb.append(hex[(b >>> 4) & 0x0F]);
//            sb.append(hex[b & 0x0F]);
            chs[index] = hex[(b >>> 4) & 0x0F];
            ++index;
            chs[index] = hex[b & 0x0F];
            ++index;
        }
//        return sb.toString();
        return new String(chs);
    }

    //16进制转字节数组
    private static byte[] hexString2Byte(String hexStr) {
        final int len = hexStr.length();
        if (len < 1) return null;
        byte[] result = new byte[len / 2];
        // i/2 == i>>1
//        for (int i = 0; i < len; i+=2) {
//            result[i>>1] = (byte) Integer.parseInt(hexStr.substring(i, i + 2), 16);//出错抛异常
//        }
//        for (int i = 0; i < len; i+=2) {
//            result[i>>1] = (byte) ((Character.digit((int)hexStr.charAt(i), 16) << 4)
//                    | Character.digit((int)hexStr.charAt(i + 1), 16));//出错无抛异常
//        }
        for (int i = 0; i < len; i+=2) {
            result[i>>1] = (byte) ((digit(hexStr.charAt(i)) << 4) | digit(hexStr.charAt(i + 1)));//出错抛异常
        }
        return result;
    }

    private static int digit(char codePoint) {
        int result = -1;
        if ('0' <= codePoint && codePoint <= '9') {
            result = codePoint - '0';
        } else if ('a' <= codePoint && codePoint <= 'f') {
            result = 10 + (codePoint - 'a');
        } else if ('A' <= codePoint && codePoint <= 'F') {
            result = 10 + (codePoint - 'A');
        } else {
            throw new NumberFormatException("For input char: \"" + codePoint + "\"");
        }
        return result;
    }

    // 向量
    private static final byte[] IV = new byte[]{8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 7, 6, 5};

    /*
    单片机参考： https://blog.csdn.net/chengjunchengjun/article/details/109322987

    PKCS5Padding
    PKCS7Padding
    NoPadding
    ZerosPadding
    ISO10126Padding

    总结一下，就是用了 NoPadding 就代表着你对这个数据是否可以被完整分组负有责任，如果不能被完整分组就会报错或者抛出异常。

    然后 ZerosPadding 意思就是在数据块末尾补0x00，注意如果刚开始已经完整分组了也需要补一整个分组的0x00，否则无法解密。

    PKCS7Padding 就是数据个数最后少几个就填充多少个数，具体的做法可以：数据的个数先取余16，然后16减去余数。
    例如{1,2,3,4,5,6,7,8,9}，总共9个数值，取余16后是9，需要补充7个7，则最后数据变为{1,2,3,4,5,6,7,8,9,7,7,7,7,7,7,7}

    PKCS5Padding，PKCS7Padding的子集，块大小固定为8字节。在AES加密当中其实是没有pkcs5的，
    因为AES的分块是16B而pkcs5只能用于8B，所以我们在AES加密中所说的pkcs5指的就是pkcs7。

    由于使用 PKCS7Padding/PKCS5Padding 填充时，最后一个字节肯定为填充数据的长度，所以在解密后可以准确删除填充的数据，
    而使用 ZeroPadding 填充时，没办法区分真实数据与填充数据，所以只适合以\0结尾的字符串加解密。
    为了改善这种不足，ISO10126Padding 采用不足的n-1位补随机数，最后一位补n的做法。
    */
    /**
     * AES加密
     * @param data 要加密的数据
     * @param key 密钥，长度必须是8的整倍数？
     * @return 加密后的数据
     */
    public static byte[] encryptAES128CBC(byte[] data, byte[] key) {
        try {
            // iOS 只支持 PKCS7Padding 加密，或者 iOS 也用NoPadding自己补齐，Android PKCS7Padding 和 PKCS5Padding 都支持。AES/CBC/PKCS7Padding  AES/ECB/PKCS5Padding
            @SuppressLint("GetInstance")
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");

            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(IV));
            int blockSize = cipher.getBlockSize();
            int length = data.length;
            if (length % blockSize == 0) return cipher.doFinal(data);

            // 使用 NoPadding 就要自己补齐不足的字节
            length = length + (blockSize - (length % blockSize));
            byte[] newData = new byte[length];
            System.arraycopy(data, 0, newData, 0, data.length);
            return cipher.doFinal(newData);
        } catch (NoSuchAlgorithmException | InvalidKeyException | InvalidAlgorithmParameterException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException e) {
            Log.e(TAG, "aes128encrypt: ", e);
        }

        return null;
    }

    public static byte[] decryptUseAES128CBC(byte[] data, byte[] key) {
        try {
            @SuppressLint("GetInstance")
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(IV));
            return trimBytes(cipher.doFinal(data));
        } catch (NoSuchAlgorithmException | InvalidKeyException | InvalidAlgorithmParameterException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException e) {
            Log.e(TAG, "aes128encrypt: ", e);
        }

        return null;
    }

    public static byte[] trimBytes(byte[] bytes) {
        int useLen;
        for (useLen = bytes.length - 1; useLen >= 0; useLen--) {
            if (0x00 != bytes[useLen]) {
                byte[] newBytes = new byte[useLen + 1];
                System.arraycopy(bytes, 0, newBytes, 0, newBytes.length);
                return newBytes;
            }
        }
        if (bytes.length == 16) {
            return new byte[]{bytes[0]};
        }
        return bytes;
    }

    public static byte[] encryptAES128CBCPKCS7(byte[] data, byte[] key) {
        try {
            // iOS 只支持 PKCS7Padding 加密，或者 iOS 也用NoPadding自己补齐，Android PKCS7Padding 和 PKCS5Padding 都支持。AES/CBC/PKCS7Padding  AES/ECB/PKCS5Padding
            @SuppressLint("GetInstance")
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");

            // ECB 不需要 IvParameterSpec
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(IV));
            return cipher.doFinal(data);
        } catch (NoSuchAlgorithmException | InvalidKeyException | InvalidAlgorithmParameterException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException e) {
            Log.e(TAG, "aes128encrypt: ", e);
        }

        return null;
    }

    public static byte[] decryptUseAES128CBCPKCS7(byte[] data, byte[] key) {
        try {
            @SuppressLint("GetInstance")
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            // ECB 不需要 IvParameterSpec
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(IV));
            return cipher.doFinal(data);
        } catch (NoSuchAlgorithmException | InvalidKeyException | InvalidAlgorithmParameterException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException e) {
            Log.e(TAG, "aes128encrypt: ", e);
        }

        return null;
    }
}
