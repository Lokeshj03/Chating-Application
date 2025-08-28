import java.io.*;
import java.net.*;

public class ChatClient {
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;

    public ChatClient(String serverAddress, int port) {
        try {
            socket = new Socket(serverAddress, port);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);

            new Thread(new ReceivedMessagesHandler()).start();

            BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
            String input;
            while ((input = consoleReader.readLine()) != null) {
                writer.println(input);
            }
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private class ReceivedMessagesHandler implements Runnable {
        public void run() {
            String msg;
            try {
                while ((msg = reader.readLine()) != null) {
                    System.out.println(msg);
                }
            } catch (IOException e) {
                System.out.println("Connection closed.");
            }
        }
    }

    public static void main(String[] args) {
        new ChatClient("localhost", 1234);
    }
}
