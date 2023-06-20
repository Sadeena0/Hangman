import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

public class Game implements Runnable{
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String adress;

    private String receivedMessage = "";

    private CountDownLatch latch;
    private volatile String word;
    private String guessedLetters;
    private int mistakes;

    private volatile boolean readingThreadFinished = false;
    private Thread readingThread;

    Game(Socket clientSocket){
        this.socket = clientSocket;
        this.adress = socket.getInetAddress().getHostAddress();
    }

    @Override
    public void run(){
        System.out.println(adress + "\tCreated thread");

        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            //Create a thread for reading
            readingThread = new Thread(() -> {
                try {
                    while(true){
                        receivedMessage = in.readUTF();
                        receivedMessage = receivedMessage.toLowerCase();
                        System.out.println(adress + "\tReceived: " + receivedMessage);
                    }
                } catch(IOException e) {
                    if(!socket.isClosed()){
                        e.printStackTrace();
                    }
                } finally {
                    readingThreadFinished = true;
                }
            });
            readingThread.start();

            //Ask to start playing
            receivedMessage = "";
            out.writeUTF("Please type 'start' to start playing!");
            while(!receivedMessage.equals("start")){ //Wait for response
                Thread.sleep(100); //Sleep to avoid excessive CPU usage
            }

            game:
            while(true){
                //Init game and variables
                out.writeUTF("Initializing game...");
                latch = new CountDownLatch(1);
                word = "";
                guessedLetters = "";
                mistakes = 0;

                //Select word and print initial interface
                selectWord();
                latch.await();
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
                        System.out.println(adress + "\t<-- won");
                        break;
                    }else if(mistakes > 5){
                        out.writeUTF("You lost. The word was " + word);
                        System.out.println(adress + "\t<-- lost");
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
                            break game;
                        }

                        //Win instantly if entire correct word is inputted
                        if(receivedMessage.equals(word)){
                            out.writeUTF("You won! Congratulations!");
                            System.out.println(adress + "\t<-- won");
                            break letter;
                        }

                        if(!receivedMessage.matches("[a-z]")){
                            out.writeUTF("Please enter a singular valid letter.");
                        }else{
                            break;
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
                    System.out.println(adress + "\tRestarting...");
                }else{
                    break;
                }
            }

            closeConnection();
        } catch(Exception e) {
            e.printStackTrace();
        }

        System.out.println(adress + "\tKilled thread");
    }


    private void selectWord(){
        new Thread(() -> {
            try {
                File words;

                out.writeUTF("Please choose one of the following categories:\n- General words\n- Countries\n- Food\n- Sports\n- Dogs");
                while(!receivedMessage.equals("general words") && !receivedMessage.equals("countries") && !receivedMessage.equals("food") && !receivedMessage.equals("sports") && !receivedMessage.equals("dogs")){ //Wait for response
                    Thread.sleep(100); //Sleep to avoid excessive CPU usage
                }

                switch(receivedMessage){
                    case "general words":
                        words = new File("words.txt");
                        break;
                    case "countries":
                        words = new File("countries.txt");
                        break;
                    case "food":
                        words = new File("food.txt");
                        break;
                    case "sports":
                        words = new File("sports.txt");
                        break;
                    case "dogs":
                        words = new File("dogs.txt");
                        break;
                    default:
                        words = new File("words.txt");
                        break;
                }

                ArrayList<String> list = new ArrayList<>();
                Scanner reader = new Scanner(words);

                // Add all words from words.txt to ArrayList
                while(reader.hasNextLine()){
                    list.add(reader.nextLine());
                }

                // Shuffle ArrayList and pick first word
                Collections.shuffle(list);
                word = list.get(0);
                System.out.println(adress + "\tSelected word: " + word);
                latch.countDown();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void closeConnection() throws IOException{
        out.writeUTF("Server connection closing");
        System.out.println(adress + "\tServer closed connection");

        in.close();
        out.close();

        try {
            while(!readingThreadFinished){
                Thread.sleep(100);
            }
            readingThread.join();
        } catch(InterruptedException e) {
            e.printStackTrace();
        }

        socket.close();
    }

    private void printInterface() throws IOException{
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
                "=========\t\t" + "Guessed letters: " + guessedLetters,
                " +----+\n" +
                        " |    |\n" +
                        " |    O\n" +
                        " |     \n" +
                        " |     \n" +
                        " |     \n" +
                        "=========\t\t" + "Guessed letters: " + guessedLetters,
                " +----+\n" +
                        " |    |\n" +
                        " |    O\n" +
                        " |    |\n" +
                        " |     \n" +
                        " |     \n" +
                        "=========\t\t" + "Guessed letters: " + guessedLetters,
                " +----+\n" +
                        " |    |\n" +
                        " |    O\n" +
                        " |   /|\n" +
                        " |     \n" +
                        " |     \n" +
                        "=========\t\t" + "Guessed letters: " + guessedLetters,
                " +----+\n" +
                        " |    |\n" +
                        " |    O\n" +
                        " |   /|\\ \n" +
                        " |     \n" +
                        " |     \n" +
                        "=========\t\t" + "Guessed letters: " + guessedLetters,
                " +----+\n" +
                        " |    |\n" +
                        " |    O\n" +
                        " |   /|\\ \n" +
                        " |   / \n" +
                        " |     \n" +
                        "=========\t\t" + "Guessed letters: " + guessedLetters,
                " +----+\n" +
                        " |    |\n" +
                        " |    O\n" +
                        " |   /|\\ \n" +
                        " |   / \\ \n" +
                        " |     \n" +
                        "=========\t\t" + "Guessed letters: " + guessedLetters,};

        out.writeUTF(hangman[mistakes]);

        //Spacer
        out.writeUTF("");

        //Hidden word
        StringBuilder hiddenWordString = new StringBuilder();
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
    }
}