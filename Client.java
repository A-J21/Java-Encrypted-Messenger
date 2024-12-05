
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
