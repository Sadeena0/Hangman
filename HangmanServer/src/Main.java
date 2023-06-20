import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Main{
    private static final int PORT = 8000;

    public static void main(String[] args) throws IOException{
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Server started. Listening on port " + PORT);

        while(true){
            // Accept incoming client connections
            Socket clientSocket = serverSocket.accept();
            System.out.println("New client connected: " + clientSocket.getInetAddress().getHostAddress());

            // Start a new thread to handle the client
            Thread thread = new Thread(new Game(clientSocket));
            thread.start();
        }
    }
}