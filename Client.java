/**
 * The Client class represents a client-side application that allows users to connect to a server,
 * send and receive messages, and view a list of users currently online. The user will supply their 
 * User ID (username) and the server's address (IP or domain) to establish a connection. Once connected, 
 * the client can send messages to other users and view incoming messages in a chat interface.
 * 
 * This class utilizes Swing for the graphical user interface (GUI) and handles networking through 
 * sockets. The user can disconnect from the server at any time, which will close the connection and 
 * revert to the login screen.
 * 
 * The client also displays an updated list of users who are currently online, which is dynamically 
 * updated when users connect or disconnect.
 * 
 * <p>
 * Authors: Ali Jalil, Josh Egwaikhide, Nick Chalardsoontornvatee
 * </p>
 */
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.security.NoSuchAlgorithmException;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.ArrayList;

public class Client {
    private static String userId;  // User's unique identifier (username)
    private static Socket socket;  // Socket for client-server communication
    private static PrintWriter out;  // Output stream for sending messages to the server
    private static BufferedReader in;  // Input stream for receiving messages from the server
    private static JFrame frame;  // Main JFrame for the client GUI
    private static JTextArea chatArea;  // Text area displaying the chat messages
    private static JTextField messageField;  // Text field for entering messages
    private static JButton sendButton;  // Button to send messages
    private static JButton loginButton;  // Button to log in
    private static JTextField userIdField;  // Text field for entering the user ID
    private static JTextField serverAddressField;  // Text field for entering the server address
    private static JPanel loginPanel;  // Panel for the login UI
    private static JPanel chatPanel;  // Panel for the chat UI
    private static CardLayout cardLayout;  // Layout manager to switch between panels
    private static JPanel containerPanel;  // Panel that holds both login and chat panels
    private static JList<String> userList;  // JList to display the list of online users
    private static DefaultListModel<String> userListModel;  // Model to hold the user list data

    /**
     * Main entry point of the client application. It initializes the GUI and sets up the login panel.
     * This method is called when the application is launched.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Client::initializeUI);
    }

    /**
     * Initializes the user interface for the client. It creates the main frame and the panels for login and chat.
     * The login panel is shown first, and the chat panel is only shown after a successful login.
     */
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

    /**
     * Creates the login user interface, where the user enters their user ID and server address.
     * The user can click the login button or press Enter to initiate the login process.
     */
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

    /**
     * Creates the chat user interface, where the user can send and receive messages, 
     * and view the list of online users.
     */
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

    /**
     * Handles the login process by connecting to the specified server and switching to the chat interface.
     * It also starts a new thread to receive messages from the server.
     */
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

    /**
     * Handles the disconnection process by closing the socket and reverting to the login interface.
     */
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

    /**
     * Sends the message entered by the user to the server. The message is encrypted before sending.
     */
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

    /**
     * Receives messages from the server. This method listens for incoming messages and updates the chat area accordingly.
     */
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

    /**
     * Updates the list of online users in the user list UI component.
     * 
     * @param message The message containing the updated user list, formatted as "[USERLIST]: user1, user2, ...".
     */
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
