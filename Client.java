import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.security.NoSuchAlgorithmException;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.ArrayList;

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
    private static CardLayout cardLayout;
    private static JPanel containerPanel;
    private static JList<String> userList;
    private static DefaultListModel<String> userListModel;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Client::initializeUI);
    }

    private static void initializeUI() {
        frame = new JFrame("Client Application");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 500);

        cardLayout = new CardLayout();
        containerPanel = new JPanel(cardLayout);

        createLoginUI();
        createChatUI();

        frame.add(containerPanel);
        cardLayout.show(containerPanel, "LoginPanel");
        frame.setVisible(true);
    }

    private static void createLoginUI() {
        loginPanel = new JPanel();
        loginPanel.setLayout(new GridLayout(3, 2));

        loginPanel.add(new JLabel("User ID:"));
        userIdField = new JTextField();
        loginPanel.add(userIdField);

        loginPanel.add(new JLabel("Server Address:"));
        serverAddressField = new JTextField();
        loginPanel.add(serverAddressField);

        loginButton = new JButton("Login");
        loginButton.addActionListener(e -> handleLogin());
        loginPanel.add(loginButton);

        ActionListener loginAction = e -> handleLogin();
        userIdField.addActionListener(loginAction);
        serverAddressField.addActionListener(loginAction);

        containerPanel.add(loginPanel, "LoginPanel");
    }

    private static void createChatUI() {
        chatPanel = new JPanel();
        chatPanel.setLayout(new BorderLayout());

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        chatPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BorderLayout());  
        rightPanel.setPreferredSize(new Dimension(150, 200));  
        rightPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));  
        JPanel userPanel = new JPanel();
        userPanel.setLayout(new BorderLayout());
        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);  
        userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);  
        userPanel.add(new JScrollPane(userList), BorderLayout.CENTER);  
        rightPanel.add(userPanel, BorderLayout.CENTER);  
        chatPanel.add(rightPanel, BorderLayout.EAST); 


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
        messageField.addActionListener(e -> handleSendMessage());

        bottomPanel.add(sendButton, BorderLayout.EAST);
        chatPanel.add(bottomPanel, BorderLayout.SOUTH);

        containerPanel.add(chatPanel, "ChatPanel");
    }

    private static void handleLogin() {
        userId = userIdField.getText();
        String serverAddress = serverAddressField.getText();
        int serverPort = 31234;

        try {
            socket = new Socket(serverAddress, serverPort);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println(userId);

            cardLayout.show(containerPanel, "ChatPanel");
            frame.setSize(600, 500);

            new Thread(Client::receiveMessages).start();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Error connecting to server: " + e.getMessage(), "Connection Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void handleDisconnect() {
        try {
            if (socket != null && !socket.isClosed()) {
                out.println("[DISCONNECT] " + userId);
                socket.close();
            }

            cardLayout.show(containerPanel, "LoginPanel");
            frame.setSize(600, 300);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Error disconnecting: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void handleSendMessage() {
        String message = messageField.getText();
        if (!message.isEmpty()) {
            try {
                String encryptedMessage = EncryptedMessage.encryptMessage(message, userId);
                out.println(encryptedMessage);
                messageField.setText("");

                SwingUtilities.invokeLater(() -> chatArea.append(userId + ": " + message + "\n"));
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

                //  handle user list updates
                if (messageToDisplay.startsWith("[USERLIST]")) {  
                    updateUserList(messageToDisplay);  
                } else {
                    if (messageToDisplay.contains("has disconnected")) {
                        SwingUtilities.invokeLater(() -> chatArea.append(messageToDisplay + "\n"));
                    } else {
                        String[] parts = messageToDisplay.split(": ", 2);
                        if (parts.length == 2) {
                            String senderName = parts[0];
                            String encryptedMessage = parts[1];

                            String decryptedMessage = EncryptedMessage.decryptMessage(encryptedMessage, userId);

                            SwingUtilities.invokeLater(() -> chatArea.append(senderName + ": " + decryptedMessage + "\n"));
                        }
                    }
                }
            }
        } catch (IOException e) {
            if (!socket.isClosed()) {
                JOptionPane.showMessageDialog(frame, "Error receiving message: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NoSuchAlgorithmException e) {
            JOptionPane.showMessageDialog(frame, "Decryption error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    //update the user list 
    private static void updateUserList(String message) {
        // Extract user list from the message
        String[] users = message.split(": ")[1].split(", ");
        userListModel.clear();

        for (String user : users) {
            if (user.equals(userId)) {
                userListModel.addElement("<html><b>" + user + "</b></html>");  
            } else {
                userListModel.addElement(user);  
            }
        }
    }
}
