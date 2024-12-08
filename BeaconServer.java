import java.io.*;
import java.net.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The BeaconServer class represents a server that accepts incoming client connections 
 * and broadcasts messages to all connected clients. It listens for client connections 
 * on a specified port and spawns a new thread to handle each connected client. The server 
 * maintains a list of authenticated clients and sends messages to clients in a chat-like manner.
 * 
 * <p>
 * The server supports user authentication, message broadcasting, and updating the list of connected users.
 * </p>
 * 
 * <p>
 * The server operates on port 31234 by default.
 * </p>
 * 
 *  * <p>
 * Authors: Ali Jalil, Josh Egwaikhide, Nick Chalardsoontornvatee
 * </p>
 */
public class BeaconServer {
    
    private static final int PORT = 31234;  // Port number on which the server listens
    private static CopyOnWriteArrayList<ClientHandler> clients = new CopyOnWriteArrayList<>();  // List of connected clients

    /**
     * Main entry point for the BeaconServer. This method creates a server socket, accepts incoming connections, 
     * and starts a new thread for each client connection.
     * 
     * @param args Command-line arguments (not used).
     */
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is running and waiting for connections...");

            // Accept incoming connections
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket);

                // Create a new client handler for the connected client
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clients.add(clientHandler);
                new Thread(clientHandler).start();  // Start client handler in a new thread
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Broadcasts a message to all clients except the sender.
     * 
     * @param message The message to broadcast.
     * @param sender The client handler who sent the message (will not receive the message).
     */
    public static void broadcast(Message message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(message);  // Send the message to all other clients
            }
        }
    }

    /**
     * The ClientHandler class represents a handler for individual client connections.
     * It is responsible for authenticating the client, receiving and sending messages, 
     * and managing the client connection lifecycle.
     */
    private static class ClientHandler implements Runnable {
        private Socket clientSocket;  // The socket representing the client connection
        private PrintWriter out;  // Output stream for sending messages to the client
        private BufferedReader in;  // Input stream for receiving messages from the client
        private String userId;  // The user ID of the client
        private boolean authenticated = false;  // Whether the client is authenticated

        /**
         * Constructor for ClientHandler. Initializes the input and output streams for the client socket.
         * 
         * @param socket The socket representing the client connection.
         */
        public ClientHandler(Socket socket) {
            this.clientSocket = socket;

            try {
                // Create input and output streams for communication
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * The main execution method for the client handler. Handles authentication, 
         * receiving messages from the client, and broadcasting messages to other clients.
         */
        @Override
        public void run() {
            try {
                // Authenticate the client
                authenticate();

                if (authenticated) {
                    System.out.println("User Accepted: " + userId);

                    // Send the user list to the newly connected client
                    updateUserList();

                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        if (inputLine.startsWith("[DISCONNECT]")) {
                            System.out.println(userId + " has disconnected");
                            Message disconnectMessage = new Message(userId + " has disconnected", userId, null);
                            BeaconServer.broadcast(disconnectMessage, this);
                            disconnect();
                            break;
                        }

                        System.out.println("Received message from " + userId + ": " + inputLine);

                        String formattedMessage = userId + ": " + inputLine;
                        Message message = new Message(formattedMessage, userId, null);
                        BeaconServer.broadcast(message, this);
                    }
                }
            } catch (Exception e) {
                System.out.println("Not verified user");
            }
        }

        /**
         * Authenticates the client by reading the user ID from the input stream.
         * If the user ID is valid, the client is authenticated; otherwise, the client is disconnected.
         */
        private void authenticate() {
            try {
                userId = in.readLine();
                if (userId != null && !userId.isEmpty()) {
                    authenticated = true;
                    System.out.println("Client is authenticated: " + userId);
                } else {
                    System.out.println("Unauthorized user");
                    disconnect();
                }
            } catch (Exception e) {
                System.err.println("Error during authentication: " + e.getMessage());
                disconnect();
            }
        }

        /**
         * Sends a message to the client.
         * 
         * @param message The message to send to the client.
         */
        public void sendMessage(Message message) {
            out.println(message.getContent());
        }

        /**
         * Updates the user list and sends it to all connected clients.
         * The user list is formatted as a string "[USERLIST]: user1, user2, ...".
         */
        private static void updateUserList() {
            StringBuilder userListString = new StringBuilder("[USERLIST]: ");
            for (ClientHandler client : clients) {
                userListString.append(client.userId).append(", ");
            }
            
            if (userListString.length() > 12) {
                userListString.setLength(userListString.length() - 2);
            }
            for (ClientHandler client : clients) {
                client.sendMessage(new Message(userListString.toString(), "Server", null));
            }
        }

        /**
         * Closes the client connection and removes the client from the server's list of connected clients.
         */
        private void disconnect() {
            try {
                if (clientSocket != null) clientSocket.close();
                clients.remove(this);
                System.out.println("Client has disconnected: " + userId);
                updateUserList();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
