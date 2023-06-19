package Client;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Main{
    public static void main(String[] args) throws IOException{
        int port = 8000;
        String host = "localhost";
        Socket socket;
        Scanner reader = new Scanner(System.in);

        socket = new Socket(host, port);
        DataInputStream in = new DataInputStream(socket.getInputStream());
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());

        while(true){
            //TODO: make Thread to constantly listen, writing anything Client receives
            new Thread(() -> { //Listener thread
                try {
                    while(true){
                        System.out.println(in.readUTF());
                    }
                } catch(IOException e) {
                    e.printStackTrace();
                }
            }).start();

            //TODO: make Thread to constantly write, sending anything after user presses enter
            new Thread(() -> { //Writer thread
                try {
                    while(true){
                        out.writeUTF(reader.nextLine());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
}