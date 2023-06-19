package Server;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Scanner;
import java.util.regex.*;

public class Game implements Runnable{
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private String receivedMessage = "";

//    private Lock lock = new ReentrantLock();

    private Character[] validCharacters = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
    private volatile String word;
    private String guessedLetters;
    private int mistakes;

    Game(Socket clientSocket){
        this.socket = clientSocket;
    }

    @Override
    public void run(){
        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            //Create a thread for reading
            new Thread(() -> {
                try {
                    while(true){
                        receivedMessage = in.readUTF(); //Read the message from the input stream
                        receivedMessage = receivedMessage.toLowerCase();
                        System.out.println(socket.getInetAddress().getHostAddress() + "\tReceived: " + receivedMessage);
                    }
                } catch(IOException e) {
                    e.printStackTrace();
                }
            }).start();

            //Ask to start playing
            receivedMessage = "";
            out.writeUTF("Please type 'start' to start playing!");
            while(!receivedMessage.equals("start")){ //Wait for response
                Thread.sleep(100); //Sleep to avoid excessive CPU usage
            }

            //Loop for each new game
            while(true){
                //Init game and variables
                out.writeUTF("Initializing game...");
                word = "";
                guessedLetters = "";
                mistakes = 0;

                //Select word and print initial interface
                selectWord();
                Thread.sleep(100); //TODO ugly thread.sleep(), see bottom todo comment
                printInterface();

                //Loop for each letter
                letter:
                while(true){
                    //Check for wincondition
                    boolean winCondition = true;
                    for(int i = 0; i < word.length(); i++){
                        if(!guessedLetters.contains(String.valueOf(word.charAt(i)))){
                            winCondition = false;
                        }
                    }

                    if(winCondition){
                        out.writeUTF("You won! Congratulations!");
                        System.out.println(socket.getInetAddress().getHostAddress() + "\t<-- won");
                        break;
                    }else if(mistakes > 5){
                        out.writeUTF("You lost. The word was " + word);
                        System.out.println(socket.getInetAddress().getHostAddress() + "\t<-- lost");
                        break;
                    }

                    //Getting letter
                    while(true){
                        receivedMessage = "";
                        out.writeUTF("Guess a letter:");
                        while(receivedMessage.equals("")){ //Wait for response
                            Thread.sleep(100); //Sleep to avoid excessive CPU usage
                        }

                        //Stop if message is stop
                        if(receivedMessage.equals("stop")){
                            closeConnection();
                        }

                        //Win instantly if entire correct word is inputted
                        if(receivedMessage.equals(word)){
                            out.writeUTF("You won! Congratulations!");
                            System.out.println(socket.getInetAddress().getHostAddress() + "\t<-- won");
                            break letter;
                        }

                        //TODO: regex here?
                        //Check if received message is a singular character and valid letter input
                        if (receivedMessage.length() != 1) {
                            out.writeUTF("Please enter just a singular letter.");
                        } else {
                            Pattern pattern = Pattern.compile("[a-z]");
                            Matcher matcher = pattern.matcher(receivedMessage);
                            if (!matcher.matches()) {
                                out.writeUTF("Please enter a valid letter.");
                            } else {
                                break;
                            }
                        }
                    }

                    //If letter isn't already in list of guessed letters, add it
                    if(!guessedLetters.contains(receivedMessage)){
                        guessedLetters += receivedMessage;
                    }

                    //Checking if word contains letter. If it doesn't, add mistake
                    if(!word.contains(receivedMessage)){
                        mistakes++;
                    }

                    //Update interface
                    printInterface();
                }

                //Ask to replay
                receivedMessage = "";
                out.writeUTF("Do you want to play again? Y/N");
                while(!receivedMessage.equals("y") && !receivedMessage.equals("n")){ //Wait for response
                    Thread.sleep(100); //Sleep to avoid excessive CPU usage
                }

                if(receivedMessage.equals("y")){
                    out.writeUTF("Excellent!");
                    System.out.println(socket.getInetAddress().getHostAddress() + "\tRestarting...");
                }else{
                    out.writeUTF("Bye bye!");
                    break;
                }
            }

            System.out.println(socket.getInetAddress().getHostAddress() + "\tclosing connection");
            closeConnection();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void selectWord(){
        new Thread(() -> {
//            lock.lock();
            try {
                ArrayList<String> list = new ArrayList<>();
                File words = new File("words.txt");
                Scanner reader = new Scanner(words);

                //Add all words from words.txt to ArrayList
                while(reader.hasNextLine()){
                    list.add(reader.nextLine());
                }

                //Shuffle ArrayList and pick first word
                Collections.shuffle(list);
                word = list.get(0);
                System.out.println(socket.getInetAddress().getHostAddress() + "\tSelected word: " + word);
            } catch(Exception e) {
                e.printStackTrace();
            }
//            finally {
//                lock.unlock();
//            }
        }).start();
    }

    private void closeConnection() throws IOException{
        //TODO: safely shut down reading thread and close connection (both server and socket side)
        // Should it close from server side or from client side? who knows, not me ¯\_(ツ)_/¯
        socket.shutdownInput();
        socket.shutdownOutput();
        socket.close();
    }

    private void printInterface() throws IOException{
//        lock.lock();
//        try {
            //Spacer
            out.writeUTF("\n\n");

            //Header
            out.writeUTF("██╗░░██╗░█████╗░███╗░░██╗░██████╗░███╗░░░███╗░█████╗░███╗░░██╗\n" +
                    "██║░░██║██╔══██╗████╗░██║██╔════╝░████╗░████║██╔══██╗████╗░██║\n" +
                    "███████║███████║██╔██╗██║██║░░██╗░██╔████╔██║███████║██╔██╗██║\n" +
                    "██╔══██║██╔══██║██║╚████║██║░░╚██╗██║╚██╔╝██║██╔══██║██║╚████║\n" +
                    "██║░░██║██║░░██║██║░╚███║╚██████╔╝██║░╚═╝░██║██║░░██║██║░╚███║\n" +
                    "╚═╝░░╚═╝╚═╝░░╚═╝╚═╝░░╚══╝░╚═════╝░╚═╝░░░░░╚═╝╚═╝░░╚═╝╚═╝░░╚══╝");

            //Spacer
            out.writeUTF("\n");

            //Hangman graphic
            String[] hangman = {" +----+\n" +
                    " |    |\n" +
                    " |     \n" +
                    " |     \n" +
                    " |     \n" +
                    " |     \n" +
                    "=========\t\t" + guessedLetters,
                    " +----+\n" +
                            " |    |\n" +
                            " |    O\n" +
                            " |     \n" +
                            " |     \n" +
                            " |     \n" +
                            "=========\t\t" + guessedLetters,
                    " +----+\n" +
                            " |    |\n" +
                            " |    O\n" +
                            " |    |\n" +
                            " |     \n" +
                            " |     \n" +
                            "=========\t\t" + guessedLetters,
                    " +----+\n" +
                            " |    |\n" +
                            " |    O\n" +
                            " |   /|\n" +
                            " |     \n" +
                            " |     \n" +
                            "=========\t\t" + guessedLetters,
                    " +----+\n" +
                            " |    |\n" +
                            " |    O\n" +
                            " |   /|\\ \n" +
                            " |     \n" +
                            " |     \n" +
                            "=========\t\t" + guessedLetters,
                    " +----+\n" +
                            " |    |\n" +
                            " |    O\n" +
                            " |   /|\\ \n" +
                            " |   / \n" +
                            " |     \n" +
                            "=========\t\t" + guessedLetters,
                    " +----+\n" +
                            " |    |\n" +
                            " |    O\n" +
                            " |   /|\\ \n" +
                            " |   / \\ \n" +
                            " |     \n" +
                            "=========\t\t" + guessedLetters,};

            out.writeUTF(hangman[mistakes]);

            //Spacer
            out.writeUTF("");

            //Hidden word
            StringBuilder hiddenWordString = new StringBuilder();
            //TODO: NullPointerException because word gets called before it's initialized in selectWord() thread
            // Now fixed by using thread.sleep() after calling selectWord().
            // Try to fix using synchronized locks?
            for(int i = 0; i < word.length(); i++){
                if(guessedLetters.contains(String.valueOf(word.charAt(i)))){
                    hiddenWordString.append(word.charAt(i));
                }else{
                    hiddenWordString.append("_");
                }
            }
            out.writeUTF(hiddenWordString.toString());

            //Spacer
            out.writeUTF("");
//        }
//        finally {
//            lock.unlock();
//        }
    }
}