
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

public class Client {
    private static final String MASTER_KEY =" SHARED_keybetweenusers456";
    private static String userId;

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Enter your User ID: ");
            userId = scanner.nextLine();

            System.out.print("Enter the server address: ");
            String serverAddress = scanner.nextLine();

            System.out.print("Enter the server port: ");
            int serverPort = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            // Connect to the server
            Socket socket = new Socket(serverAddress, serverPort);
            System.out.println("Connected to the server.");

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Authentication
            out.println(userId);

            
            // Thread for receiving messages
            new Thread(() -> {
                try {
                    String incomingMessage;
                    while ((incomingMessage = in.readLine()) != null) {
                        // Split the message into sender name and encrypted message
                        String[] parts = incomingMessage.split(": ", 2);
                        if (parts.length == 2) {
                            String senderName = parts[0];
                            String encryptedMessage = parts[1];

                          
                            String decryptedMessage = decryptMessage(encryptedMessage, userId);

                            System.out.print("\n" + senderName + ": " + decryptedMessage + "\nYou: ");
                            
                        } else {
                            System.out.println("\n[Invalid message format]");
                        }
                        
                    }
                } catch (IOException | NoSuchAlgorithmException e) {
                    System.err.println("Error receiving message: " + e.getMessage());
                }
            }).start();

            // Sending messages
            while (scanner.hasNextLine()) {
                System.out.print("You: ");
                String content = scanner.nextLine();

                String encryptedContent = encryptMessage(content, userId);
                out.println(encryptedContent);
            }
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

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
}