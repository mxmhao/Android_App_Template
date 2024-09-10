package template;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.cert.Certificate;

public class FingerprintUtils {
    public static void main(String[] args) {
        try {
            // 设定 KeyStore 文件路径、密码以及别名
            String keystorePath = "./test111.keystore";
            String keystorePassword = "123456";
            String alias = "111111";

            // 读取 KeyStore
            FileInputStream fis = new FileInputStream(keystorePath);
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(fis, keystorePassword.toCharArray());

            // 获取证书
            Certificate cert = keyStore.getCertificate(alias);
            if (cert == null) {
                throw new RuntimeException("Certificate not found for alias: " + alias);
            }

            // 计算并打印指纹
            System.out.println("MD5: " + getFingerprint(cert, "MD5"));
            System.out.println("SHA-1: " + getFingerprint(cert, "SHA-1"));
            System.out.println("SHA-256: " + getFingerprint(cert, "SHA-256"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getFingerprint(Certificate cert, String algorithm) {
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            byte[] certBytes = cert.getEncoded();
            byte[] digest = md.digest(certBytes);
            return bytesToHex(digest);
        } catch (Exception e) {
            throw new RuntimeException("Error calculating fingerprint", e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X:", b));
        }
        // Remove the trailing colon
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }
}
