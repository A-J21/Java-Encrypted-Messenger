  // Encrypt a message using user's ID
  private static String encryptMessage(String message, String userId) throws NoSuchAlgorithmException {
    byte[] key = generateKey();
    byte[] encryptedBytes = xorEncrypt(message.getBytes(StandardCharsets.UTF_8), key);
    return bytesToHex(encryptedBytes);
}

// Decrypt a message
private static String decryptMessage(String encryptedHex, String userId) throws NoSuchAlgorithmException{
try {
byte[] cipherKey = generateKey(); // generate a cipher key
byte[] encryptedBytes = hexTobytes(encryptedHex);// convert hex string to byte array
byte[] decryptedBytes = xorEncrypt( encryptedBytes, cipherKey); // XOR decryption
return new String( decryptedBytes, StandardCharsets.UTF_8 ); // Convert to string
} catch (IllegalArgumentException e){
    System.err.println("Error decrypting message: " + e.getMessage());
    return "[Invalid encrypted message]";
}
}

// Generate a key using SHA-256
private static byte[] generateKey() throws NoSuchAlgorithmException {
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    return digest.digest(MASTER_KEY.getBytes(StandardCharsets.UTF_8));
}

// XOR encryption/decryption
private static byte[] xorEncrypt(byte[] data, byte[] key) {
    byte[] result = new byte[data.length];
    for (int i = 0; i < data.length; i++) {
        result[i] = (byte) (data[i] ^ key[i % key.length]);
    }
    return result;
}

// Convert byte array to hex string`
private static String bytesToHex(byte[] bytes) {
    StringBuilder sb = new StringBuilder();
    for (byte b : bytes) {
        sb.append(String.format("%02x", b));
    }
    return sb.toString();
}

// Convert hex string to byte array
private static byte[] hexTobytes(String hex){
    if (hex == null || hex.length()% 2 != 0){
    throw new IllegalArgumentException("Invalid hexadecimal input. Length must be even and non-null.");
    }
    int len = hex.length();
    byte[] bytes = new byte [len /2];
    for (int i=0; i<len; i += 2){
    int firstDigit = Character.digit(hex.charAt(i), 16);
    int secondDigit = Character.digit(hex.charAt(i + 1), 16);
    if (firstDigit ==-1 || secondDigit == -1){
        throw new IllegalArgumentException("Invalid hexadecimal character detected."); 
        }
    bytes[i / 2] = (byte) ((firstDigit << 4) + secondDigit);
        }
    return bytes;
}
