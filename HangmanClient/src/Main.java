import java.io.*;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Main{
    public static void main(String[] args) throws IOException, InterruptedException{
        int port = 8000;
        String host = "localhost";
        Socket socket;

        socket = new Socket(host, port);
        DataInputStream in = new DataInputStream(socket.getInputStream());
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());

        BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();

        Thread writingThread;
        Thread readingThread;

        final boolean[] running = {true};

        //Create a thread for writing
        writingThread = new Thread(() -> {
            try {
                while(running[0]){
                    String message = messageQueue.take(); //Blocking call to wait for a message
                    out.writeUTF(message); //Write the message to the output stream
                    out.flush();
                }
            } catch(InterruptedException | IOException e) {
                e.printStackTrace();
            }
        });
        writingThread.start();

        //Create a thread for reading
        readingThread = new Thread(() -> {
            try {
                while(true){
                    String receivedMessage = in.readUTF(); //Read the message from the input stream
                    System.out.println(receivedMessage);

                    if(receivedMessage.equals("Server connection closing")){
                        System.out.println("Stopping threads, in/out streams and closing socket...");
                        running[0] = false; //Stop writing thread
                        in.close();
                        out.close();
                        socket.close();

                        //Exit program
                        System.exit(0);
                    }
                }
            } catch(IOException e) {
                e.printStackTrace();
            }
        });
        readingThread.start();

        //Continue reading input and enqueue messages for writing
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String input;
        while((input = reader.readLine()) != null){
            messageQueue.offer(input); //Enqueue the input message for writing
        }
    }
}