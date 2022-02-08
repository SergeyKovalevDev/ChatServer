import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ChatServer {
    private final List<Client> clients = new ArrayList<>();
    private final ServerSocket serverSocket;

    public ChatServer(int port) throws ServerException {
        try {
            // creating a server socket on port 1234
            serverSocket = new ServerSocket(port);
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

    public void serverStarter() throws ServerException {
        System.out.println("Waiting...");
        while(true) {
            try {
                // waiting for a client from the network
                Socket socket = serverSocket.accept();
                Client client = new Client(socket);
                clients.add(client);
                new Thread (client).start();
                System.out.println("Client #" + client.hashCode() + " connected on " + socket);
                // creating a client
            } catch (SocketTimeoutException e) {
                throw new ServerException("Timeout was previously set with setSoTimeout and the " +
                        "timeout has been reached");
            } catch (IOException e) {
                throw new ServerException("I/O error occurs when waiting for a connection");
            } catch (SecurityException e) {
                throw new ServerException("Security manager exists and its checkAccept method " +
                        "doesn't allow the operation");
            } catch (java.nio.channels.IllegalBlockingModeException e) {
                throw new ServerException("Socket has an associated channel, the channel is in " +
                        "non-blocking mode, and there is no connection ready to be accepted");
            }
        }
    }

    private class Client implements Runnable {
        private final Socket socket;
        private PrintStream out;

        public Client(Socket socket){
            this.socket = socket;
        }

        private void receive(String message) {
            out.println(message);
        }

        public void run() {
            try {
                // get input and output streams
                // creating input and output tools
                Scanner in = new Scanner(socket.getInputStream());
                out = new PrintStream(socket.getOutputStream());

                // read from the network and write to the network
                out.println("Welcome to chat!");
                String input = in.nextLine();
                while (!input.equals("bye")) {
                    for (Client client : clients) client.receive(input);
                    input = in.nextLine();
                }
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        try {
            new ChatServer(1234).serverStarter();
        } catch (ServerException e) {
            System.out.println(e.getMessage());
            System.out.println("The program is stopped");
        }
    }
}
