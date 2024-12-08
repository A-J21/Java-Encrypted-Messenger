import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * The {@code EncryptedMessage} class provides methods for encrypting and decrypting messages
 * using the XOR cipher and a master shared key. The encryption and decryption process involves 
 * the use of the SHA-256 hash of a master key to generate an encryption key, which is then 
 * used for XOR-based encryption and decryption.
 * 
 * <p>This class contains static methods for:</p>
 * <ul>
 *   <li>Encrypting a message using the XOR cipher.</li>
 *   <li>Decrypting an encrypted message using the XOR cipher.</li>
 *   <li>Generating an encryption key using SHA-256.</li>
 *   <li>Converting byte arrays to hexadecimal strings and vice versa.</li>
 * </ul>
 * 
 * @author Ali Jalil, Josh Egwaikhide, Nick Chalardsoontornvatee
 */
public class EncryptedMessage {

    /**
     * The master key used for generating the encryption key. 
     * This key is shared between the users and is used in the key generation process.
     */
    private static final String MASTER_KEY = "SHARED_keybetweenusers456";

    /**
     * Encrypts a message using the user's ID and a shared master key.
     * The message is encrypted using XOR encryption with a key derived from the master key.
     * 
     * @param message The message to be encrypted.
     * @param userId The user ID. (Currently not used in the encryption process but can be extended.)
     * @return The encrypted message as a hexadecimal string.
     * @throws NoSuchAlgorithmException If the SHA-256 algorithm is not available.
     */
    public static String encryptMessage(String message, String userId) throws NoSuchAlgorithmException {
        byte[] key = generateKey();
        byte[] encryptedBytes = xorEncrypt(message.getBytes(StandardCharsets.UTF_8), key);
        return bytesToHex(encryptedBytes);
    }

    /**
     * Decrypts an encrypted message that was previously encrypted using the {@link #encryptMessage(String, String)} method.
     * The encrypted message is in hexadecimal format, which is converted to bytes and decrypted using XOR encryption.
     * 
     * @param encryptedHex The encrypted message in hexadecimal format.
     * @param userId The user ID (currently not used in the decryption process but can be extended).
     * @return The decrypted message as a string.
     * @throws NoSuchAlgorithmException If the SHA-256 algorithm is not available.
     */
    public static String decryptMessage(String encryptedHex, String userId) throws NoSuchAlgorithmException {
        try {
            byte[] cipherKey = generateKey(); // generate a cipher key
            byte[] encryptedBytes = hexTobytes(encryptedHex); // convert hex string to byte array
            byte[] decryptedBytes = xorEncrypt(encryptedBytes, cipherKey); // XOR decryption
            return new String(decryptedBytes, StandardCharsets.UTF_8); // Convert to string
        } catch (IllegalArgumentException e) {
            System.err.println("Error decrypting message: " + e.getMessage());
            return "[Invalid encrypted message]";
        }
    }

    /**
     * Generates an encryption key using the SHA-256 hash algorithm applied to the master key.
     * 
     * @return A byte array representing the encryption key.
     * @throws NoSuchAlgorithmException If the SHA-256 algorithm is not available.
     */
    private static byte[] generateKey() throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return digest.digest(MASTER_KEY.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Performs XOR encryption or decryption on the input data using the specified key.
     * This method is used for both encryption and decryption as XOR is a reversible operation.
     * 
     * @param data The data to be encrypted or decrypted.
     * @param key The key to be used for the XOR operation.
     * @return The resulting byte array after applying XOR.
     */
    private static byte[] xorEncrypt(byte[] data, byte[] key) {
        byte[] result = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = (byte) (data[i] ^ key[i % key.length]);
        }
        return result;
    }

    /**
     * Converts a byte array to its hexadecimal string representation.
     * 
     * @param bytes The byte array to be converted.
     * @return A hexadecimal string representation of the byte array.
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * Converts a hexadecimal string to a byte array.
     * 
     * @param hex The hexadecimal string to be converted.
     * @return A byte array representation of the hexadecimal string.
     * @throws IllegalArgumentException If the hex string is invalid (e.g., odd length or non-hex characters).
     */
    private static byte[] hexTobytes(String hex) {
        if (hex == null || hex.length() % 2 != 0) {
            throw new IllegalArgumentException("Invalid hexadecimal input. Length must be even and non-null.");
        }
        int len = hex.length();
        byte[] bytes = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            int firstDigit = Character.digit(hex.charAt(i), 16);
            int secondDigit = Character.digit(hex.charAt(i + 1), 16);
            if (firstDigit == -1 || secondDigit == -1) {
                throw new IllegalArgumentException("Invalid hexadecimal character detected.");
            }
            bytes[i / 2] = (byte) ((firstDigit << 4) + secondDigit);
        }
        return bytes;
    }
}
