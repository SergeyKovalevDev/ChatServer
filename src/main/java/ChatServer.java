import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ChatServer {
    private final List<Client> clients = new ArrayList<>();
    private final ServerSocket serverSocket;

    public ChatServer() throws ServerException {
        try {
            // creating a server socket on port 1234
            serverSocket = new ServerSocket(1234);
        } catch (IOException e) {
            throw new ServerException("I/O error occurs when opening the socket");
        } catch (SecurityException e) {
            throw new ServerException("Security manager exists and its checkListen " +
                    "method doesn't allow the operation");
        } catch (IllegalArgumentException e) {
            throw new ServerException("The port parameter is outside the specified range " +
                    "of valid port values, which is between 0 and 65535, inclusive");
        }
    }

    public void sendAll(String message) {
        for (Client client :
                clients) {
            client.receive(message);
        }
    }

    public void serverStarter() {
        while(true) {
            System.out.println("Waiting...");
            try {
                // waiting for a client from the network
                Socket socket = serverSocket.accept();
                System.out.println("Client connected!");
                // creating a client
                clients.add(new Client(socket, this));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        try {
            new ChatServer().serverStarter();
        } catch (ServerException e) {
            System.out.println(e.getMessage());
            System.out.println("The program is stopped");
        }
    }
}
