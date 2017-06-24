package the.miner.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Provide method for converting data
 */
public final class GMConverterUtils {
    /**
     * Convert byte to hex
     *
     * @param data byte data
     * @return hex string
     */
    public static String bytesToHex(byte[] data) {
        StringBuilder buf = new StringBuilder();
        for (byte b : data) {
            int halfByte = (b >>> 4) & 0x0F;
            int twoHalf = 0;
            do {
                buf.append((0 <= halfByte) && (halfByte <= 9) ? (char) ('0' + halfByte) : (char) ('a' + (halfByte - 10)));
                halfByte = b & 0x0F;
            } while (twoHalf++ < 1);
        }
        return buf.toString();
    }

    /**
     * Convert string to hash using specific algorithm
     *
     * @param text string
     * @return hash string
     * @throws NoSuchAlgorithmException     algorithm exception
     * @throws UnsupportedEncodingException encoding exception
     */
    private static String hash(String text, String algorithm) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance(algorithm);
            md.update(text.getBytes("UTF-8"), 0, text.length());
            byte[] hashByte = md.digest();
            return bytesToHex(hashByte);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return "";
    }

    /**
     * Convert string to hash using MD5 algorithm
     *
     * @param text string
     * @return MD5 hash string
     */
    public static String MD5(String text) {
        return hash(text, "MD5");
    }

    /**
     * Convert string to hash using SHA-1 algorithm
     *
     * @param text string
     * @return SHA1 hash string
     */
    public static String SHA1(String text) {
        return hash(text, "SHA-1");
    }

    /**
     * Convert string to hash using SHA-224 algorithm
     *
     * @param text string
     * @return SHA224 hash string
     */
    public static String SHA224(String text) {
        return hash(text, "SHA-224");
    }

    /**
     * Convert string to hash using SHA-256 algorithm
     *
     * @param text string
     * @return SHA256 hash string
     */
    public static String SHA256(String text) {
        return hash(text, "SHA-256");
    }

    /**
     * Convert string to hash using SHA-384 algorithm
     *
     * @param text string
     * @return SHA384 hash string
     */
    public static String SHA384(String text) {
        return hash(text, "SHA-384");
    }

    /**
     * Convert string to hash using SHA-512 algorithm
     *
     * @param text string
     * @return SHA512 hash string
     */
    public static String SHA512(String text) {
        return hash(text, "SHA-512");
    }
}
