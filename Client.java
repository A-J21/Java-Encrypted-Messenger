import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
    private static final String AUTH_KEY = "placeholderKey"; y
    private static String userId; 
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            
            System.out.print("Enter your User ID: ");
            userId = scanner.nextLine();

            System.out.print("Enter the server address: ");
            String serverAddress = scanner.nextLine();

            System.out.print("Enter the server port: ");
            int serverPort = scanner.nextInt();
            scanner.nextLine(); 

            // Connect to the server
            Socket socket = new Socket(serverAddress, serverPort);
            System.out.println("Connected to the server at " + serverAddress + ":" + serverPort);

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Authenticate
            out.println(AUTH_KEY);

            // Start a incoming messages
            new Thread(() -> {
                try {
                    String incomingMessage;
                    while ((incomingMessage = in.readLine()) != null) {
                        
                        String decryptedMessage = "[Decrypted message goes here]";
                        System.out.println("Server: " + decryptedMessage);
                    }
                } catch (IOException e) {
                    System.err.println("Error receiving message: " + e.getMessage());
                }
            }).start();

            // Read/send messages
            while (true) {
                System.out.print("Enter receiver's User ID: ");
                String receiverId = scanner.nextLine();

                System.out.print("You: ");
                String content = scanner.nextLine();

               
                Message message = new Message(content, userId, receiverId);

               
                String encryptedMessage = "[Encrypted message goes here]";

                
                out.println(encryptedMessage);
            }
        } catch (IOException e) {
            System.err.println("Error connecting to the server: " + e.getMessage());
        }
    }
}
