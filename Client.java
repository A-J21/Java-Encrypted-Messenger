import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Client {
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
        SwingUtilities.invokeLater(Client::createLoginUI);
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

        // Add KeyListener for Enter key on the userIdField and serverAddressField
        ActionListener enterListener = e -> handleLogin(); // ActionListener handles Enter key
        userIdField.addActionListener(enterListener);
        serverAddressField.addActionListener(enterListener);

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

            
            new Thread(Client::receiveMessages).start();
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
    
            frame.remove(chatPanel);
            createLoginUI();

            frame.revalidate();
            frame.repaint();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Error disconnecting: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    

    private static void handleSendMessage() {
        String message = messageField.getText();
        if (!message.isEmpty()) {
            try {
                // Use the EncryptedMessage class to encrypt the message
                String encryptedMessage = EncryptedMessage.encryptMessage(message, userId);
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
                final String messageToDisplay = incomingMessage;
    
                if (messageToDisplay.contains("has disconnected")) {
                    SwingUtilities.invokeLater(() -> {
                        chatArea.append(messageToDisplay + "\n");
                    });
                } else {
                    String[] parts = messageToDisplay.split(": ", 2);
                    if (parts.length == 2) {
                        String senderName = parts[0];
                        String encryptedMessage = parts[1];
    
                        // Use the EncryptedMessage class to decrypt the message
                        String decryptedMessage = EncryptedMessage.decryptMessage(encryptedMessage, userId);
    
                        SwingUtilities.invokeLater(() -> {
                            chatArea.append(senderName + ": " + decryptedMessage + "\n");
                        });
                    }
                }
            }
        } catch (IOException e) {
            if (!socket.isClosed()) { // Ignore exceptions if socket is intentionally closed
                JOptionPane.showMessageDialog(frame, "Error receiving message: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NoSuchAlgorithmException e) {
            JOptionPane.showMessageDialog(frame, "Decryption error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    
    
}
