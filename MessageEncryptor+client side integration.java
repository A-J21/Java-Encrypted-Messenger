import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

public class Client {
    private static final String AUTH_KEY = "placeholderKey"; // Server authentication key
    private static String userId; // User ID for generating the cipher key

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            // Get user information
            System.out.print("Enter your User ID: ");
            userId = scanner.nextLine();

            System.out.print("Enter the server address: ");
            String serverAddress = scanner.nextLine();

            System.out.print("Enter the server port: ");
            int serverPort = scanner.nextInt();
            scanner.nextLine(); // Consume newline character

            // Connect to the server
            Socket socket = new Socket(serverAddress, serverPort);
            System.out.println("Connected to the server at " + serverAddress + ":" + serverPort);

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Authenticate with the server
            out.println(AUTH_KEY);

            // start a new thread for receiving and decrypting messages from the server
            new Thread(()-> {
                try {
                    String incomingMessage;
                    while ((incomingMessage = in.readLine()) != null)
                    // decrypt incoming message
                        String decryptedMessage=decryptMessage(incomingMessage, userId);
                        System.out.println("Server:"+ decryptMessage);
                    }
                } catch (IOException|NoSuchAlgorithmException e) {
                    System.err.println("Error recieving message from server: " + e.getMessage());
                }
            }).start();

            //Read and send messages

            while(true) {
                System.out.print("Enter receiver's User ID: ");
                String receiverId = scanner.nextLine();

                System.out.print("You: ");
                String content = scanner.nextLine();

                // create a message and encrypt it

                String encryptedMessage = encryptMessage(content, userId);
                //send encrypted message  to server
                out.println(encryptedMessage);
            }
        } catch (IOException | NoSuchAlgorithmException e) {
            System.err.println("Error recieving message from server: " + e.getMessage());
        }
}


// Encrypt a message using the user's ID as the cipher key
private static String encryptMessage(String message, String userId) throws NoSuchAlgorithmException{
    byte[] cipherKey = generateCipherkey(userId);//generate cipher key
    byte[] encryptedBytes = xorEncrypt(message.getBytes(StandardCharsets.UTF_8), cipherKey); // XOR encryption
    return bytesTohex(encryptedBytes); // convert to hex string for sending  
}

private static String decryptMessage(String encryptedHex, String userId) throws NoSuchAlgorithmException{
   byte[] cipherKey = generateCipherkey(userId); // generate a cipher key
   byte[] encryptedBytes = hexToBytes(encryptedHex);// convert hex string to byte array
   byte[] decryptedBytes = xorEncrypt( encryptedBytes, cipherKey); // XOR decryption
   return new String( decryptedBytes, StandardCharsets.UTF_8 ); // Convert to string

}

// Generate a cipher key based on the user's ID
private static byte[] generateCipherkey( String userId ) throws NoSuchAlgorithmException{
    MessageDigest digest = MessageDigest.getInstance("SHA-256");//Use SHA-256 hashing
    return digest.digest(userId.getBytes(StandardCharsets.UTF_8));//Return hashed bytes
}
            
// Perform XOR encryption or decryption 

private static byte[] xorEncrypt(byte[] message, byte[] key){
    byte[] result = new byte [message.length];
    for (int i = 0; i < message.length; i++){
        result[i] = (byte) (message[i] ^ key[i % key.length]); // XOR with repeating key
    }
    return result;
}

// Convert a byte array to  a hexadecimal string 
private static String bytesTohex(byte[] bytes){
    StringBuilder sb = new StringBuilder();
    for(byte b : bytes){
        sb.append(String.format("%02x",b)); // convert byte ti 2-digit hex
    }
    return sb.toString();     
}

private static byte[] hexTobytes(String hex){
    int len = hex.length();
    byte[] bytes = new byte[len/ 2];
    for(int i = 0; i < len; i+= 2){
        bytes[i/2] =(byte) (Character.digit(hex.charAt(i), 16)<<4)
    }
    return bytes;
}

