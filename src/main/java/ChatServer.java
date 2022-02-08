import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ChatServer {
    private static final List<Client> clients = new ArrayList<>();
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

    public static void sendAll(String message) {
        for (Client client :
                clients) {
            client.receive(message);
        }
    }

    public void serverStarter() throws ServerException {
        while(true) {
            System.out.println("Waiting...");
            try {
                // waiting for a client from the network
                Socket socket = serverSocket.accept();
                Client client = new Client(socket);
                System.out.println("Client #" + client.hashCode() + " connected on " + socket);
                // creating a client
                clients.add(client);
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

    public static void main(String[] args) {
        try {
            new ChatServer(1234).serverStarter();
        } catch (ServerException e) {
            System.out.println(e.getMessage());
            System.out.println("The program is stopped");
        }
    }

    private static class Client implements Runnable {
        Socket socket;
        Scanner in;
        PrintStream out;

        public Client(Socket socket){
            this.socket = socket;
            // запускаем поток
            new Thread(this).start();
        }

        void receive(String message) {
            out.println(message);
        }

        public void run() {
            try {
                // получаем потоки ввода и вывода
                InputStream is = socket.getInputStream();
                OutputStream os = socket.getOutputStream();

                // создаем удобные средства ввода и вывода
                in = new Scanner(is);
                out = new PrintStream(os);

                // читаем из сети и пишем в сеть
                out.println("Welcome to chat!");
                String input = in.nextLine();
                while (!input.equals("bye")) {
                    sendAll(input);
                    input = in.nextLine();
                }
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
