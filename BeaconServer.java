import java.io.*;
import java.net.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class BeaconServer {
    private static final int PORT = 31234;
    private static CopyOnWriteArrayList<ClientHandler> clients = new CopyOnWriteArrayList<>();
    
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is running and waiting for connections..");

            // Accept incoming connections
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket);

                // Create a new client handler for the connected client
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clients.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Broadcast a message to all clients except the sender
    public static void broadcast(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }

    // Internal class to handle client connections
    private static class ClientHandler implements Runnable {
        private Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;
        private String userId; // Store user ID
        private boolean authenticated = false;

        // Constructor
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

        // Run method to handle client communication
        @Override
        public void run() {
            try {
                // Authenticate the client
                authenticate();

                if (authenticated) {
                    String inputLine;
                    // Continue receiving messages from the client
                    while ((inputLine = in.readLine()) != null) {
                        System.out
