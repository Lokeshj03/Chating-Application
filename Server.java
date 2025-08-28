import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static Set<ClientHandler> clientHandlers = new HashSet<>();

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(1234);
        System.out.println("Server started... Waiting for clients.");

        while (true) {
            Socket socket = serverSocket.accept();
            System.out.println("New client connected: " + socket);
            ClientHandler handler = new ClientHandler(socket);
            clientHandlers.add(handler);
            new Thread(handler).start();
        }
    }

    // Broadcast message to all clients
    static void broadcast(String message, ClientHandler excludeUser) {
        for (ClientHandler aClient : clientHandlers) {
            if (aClient != excludeUser) {
                aClient.sendMessage(message);
            }
        }
    }

    // Remove client
    static void removeClient(ClientHandler client) {
        clientHandlers.remove(client);
        System.out.println("Client removed: " + client.socket);
    }

    // Inner class for handling clients
    static class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter writer;
        private String username;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                writer = new PrintWriter(socket.getOutputStream(), true);

                writer.println("Enter username:");
                username = reader.readLine();
                System.out.println(username + " joined.");
                broadcast(username + " has joined the chat.", this);

                String msg;
                while ((msg = reader.readLine()) != null) {
                    if (msg.startsWith("@")) { // private message e.g. @user hello
                        String[] splitMsg = msg.split(" ", 2);
                        String targetUser = splitMsg[0].substring(1);
                        String privateMsg = splitMsg[1];
                        sendPrivateMessage(targetUser, privateMsg);
                    } else {
                        broadcast(username + ": " + msg, this);
                    }
                }
            } catch (IOException e) {
                System.out.println("Error: " + e.getMessage());
            } finally {
                removeClient(this);
                broadcast(username + " has left the chat.", this);
                try {
                    socket.close();
                } catch (IOException e) {}
            }
        }

        void sendMessage(String message) {
            writer.println(message);
        }

        void sendPrivateMessage(String targetUser, String message) {
            for (ClientHandler client : clientHandlers) {
                if (client.username != null && client.username.equals(targetUser)) {
                    client.sendMessage("(Private) " + username + ": " + message);
                    return;
                }
            }
            sendMessage("User " + targetUser + " not found.");
        }
    }
}
