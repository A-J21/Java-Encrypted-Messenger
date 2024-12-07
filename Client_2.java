import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Client_2 {
    private static final String MASTER_KEY = "SHARED_keybetweenusers456";
    private static String userId;
    private static Socket socket;
    private static PrintWriter out;
    private static BufferedReader in;
    private static JFrame frame;
    private static JTextArea chatArea;
    private static JTextField messageField;
    private static JButton sendButton;
    private static JButton loginButton;
    private static JTextField userIdField;
    private static JTextField serverAddressField;
    private static JPanel loginPanel;
    private static JPanel chatPanel;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Client_2::createLoginUI);
    }

    private static void createLoginUI() {
        frame = new JFrame("Client Login");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLayout(new BorderLayout());

        loginPanel = new JPanel();
        loginPanel.setLayout(new GridLayout(3, 2)); // Adjusted grid layout to remove the port field

        loginPanel.add(new JLabel("User ID:"));
        userIdField = new JTextField();
        loginPanel.add(userIdField);

        loginPanel.add(new JLabel("Server Address:"));
        serverAddressField = new JTextField();
        loginPanel.add(serverAddressField);

        loginButton = new JButton("Login");
        loginButton.addActionListener(e -> handleLogin());
        loginPanel.add(loginButton);

        frame.add(loginPanel, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    private static void handleLogin() {
        
        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException e) {
                
            }
        }

        userId = userIdField.getText();
        String serverAddress = serverAddressField.getText();
        int serverPort = 31234; 

       
        try {
            socket = new Socket(serverAddress, serverPort);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

           
            out.println(userId);

            
            frame.remove(loginPanel);
            createChatUI();

            
            new Thread(Client_2::receiveMessages).start();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Error connecting to server: " + e.getMessage(), "Connection Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void createChatUI() {
        chatPanel = new JPanel();
        chatPanel.setLayout(new BorderLayout());

        // Chat area
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        chatPanel.add(scrollPane, BorderLayout.CENTER);

        
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton disconnectButton = new JButton("Disconnect");
        disconnectButton.addActionListener(e -> handleDisconnect());
        topPanel.add(disconnectButton);
        chatPanel.add(topPanel, BorderLayout.NORTH);

      
        JPanel bottomPanel = new JPanel(new BorderLayout());
        messageField = new JTextField();
        bottomPanel.add(messageField, BorderLayout.CENTER);

        sendButton = new JButton("Send");
        sendButton.addActionListener(e -> handleSendMessage());
        bottomPanel.add(sendButton, BorderLayout.EAST);

        chatPanel.add(bottomPanel, BorderLayout.SOUTH);

        frame.add(chatPanel, BorderLayout.CENTER);
        frame.setSize(400, 500);
        frame.setVisible(true);
    }

    private static void handleDisconnect() {
        try {
            if (socket != null && !socket.isClosed()) {
               
                out.println("[DISCONNECT] " + userId);
                socket.close();
            }
    
            
            chatArea.append("[You have disconnected]\n");
    
            
            frame.remove(chatPanel);
            createLoginUI();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Error disconnecting: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    

    private static void handleSendMessage() {
        String message = messageField.getText();
        if (!message.isEmpty()) {
            try {
                String encryptedMessage = encryptMessage(message, userId);
                out.println(encryptedMessage);
                messageField.setText("");

                SwingUtilities.invokeLater(() -> {
                    chatArea.append(userId + ": " + message + "\n");
                });
            } catch (NoSuchAlgorithmException e) {
                JOptionPane.showMessageDialog(frame, "Encryption error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static void receiveMessages() {
        try {
            String incomingMessage;
    
            while ((incomingMessage = in.readLine()) != null) {
               
                if (incomingMessage.contains("has disconnected")) {
                    
                    SwingUtilities.invokeLater(() -> {
                        chatArea.append(incomingMessage + "\n");
                    });
                } else {
                    String[] parts = incomingMessage.split(": ", 2);
                    if (parts.length == 2) {
                        String senderName = parts[0];
                        String encryptedMessage = parts[1];
    
                        String decryptedMessage = decryptMessage(encryptedMessage, userId);
    
                        
                        SwingUtilities.invokeLater(() -> {
                            chatArea.append(senderName + ": " + decryptedMessage + "\n");
                        });
                    }
                }
            }
        } catch (IOException | NoSuchAlgorithmException e) {
            JOptionPane.showMessageDialog(frame, "Error receiving message: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
        } catch (IOException | NoSuchAlgorithmException e) {
            JOptionPane.showMessageDialog(frame, "Error receiving message: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Encrypt a message using user's ID
    private static String encryptMessage(String message, String userId) throws NoSuchAlgorithmException {
        byte[] key = generateKey();
        byte[] encryptedBytes = xorEncrypt(message.getBytes(StandardCharsets.UTF_8), key);
        return bytesToHex(encryptedBytes);
    }

    // Decrypt a message
    private static String decryptMessage(String encryptedHex, String userId) throws NoSuchAlgorithmException {
        try {
            byte[] cipherKey = generateKey(); // generate a cipher key
            byte[] encryptedBytes = hexToBytes(encryptedHex); // convert hex string to byte array
            byte[] decryptedBytes = xorEncrypt(encryptedBytes, cipherKey); // XOR decryption
            return new String(decryptedBytes, StandardCharsets.UTF_8); // Convert to string
        } catch (IllegalArgumentException e) {
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

    // Convert byte array to hex string
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    // Convert hex string to byte array
    private static byte[] hexToBytes(String hex) {
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
