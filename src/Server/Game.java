package Server;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

public class Game implements Runnable{
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private boolean gameIsRunning;

    private String word;
    private Character guessedLetter;
    private int mistakes;

    Game(Socket clientSocket){
        this.socket = clientSocket;
    }

    @Override
    public void run(){
        try {
            //Init in/out streams
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            out.writeUTF("Please type back 'confirm' to check communication");
            while(in.readUTF().isEmpty()){ //Wait for confirmation
            }

            while(true){ //Loop for each new game (until user closes connection)
                out.writeUTF("Initializing game..."); //Init game and variables
                gameIsRunning = true;
                selectWord();

                while(gameIsRunning){ //Loop for each turn (as long as game is running)
                    printInterface();
                    out.writeUTF("Guess next letter:");
                    while(in.readUTF().isEmpty()){
                        guessedLetter = in.readChar();
                    } //Wait for letter



                }

            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void selectWord(){
        new Thread(() -> {
            try {
                ArrayList<String> list = new ArrayList<>();
                File words = new File("words.txt");
                Scanner reader = new Scanner(words);

                while(reader.hasNextLine()){
                    list.add(reader.nextLine());
                }

                Collections.shuffle(list);
                word = list.get(0);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }).start();
    }


    private void printInterface() throws IOException{
        out.writeUTF(   "██╗░░██╗░█████╗░███╗░░██╗░██████╗░███╗░░░███╗░█████╗░███╗░░██╗\n" +
                            "██║░░██║██╔══██╗████╗░██║██╔════╝░████╗░████║██╔══██╗████╗░██║\n" +
                            "███████║███████║██╔██╗██║██║░░██╗░██╔████╔██║███████║██╔██╗██║\n" +
                            "██╔══██║██╔══██║██║╚████║██║░░╚██╗██║╚██╔╝██║██╔══██║██║╚████║\n" +
                            "██║░░██║██║░░██║██║░╚███║╚██████╔╝██║░╚═╝░██║██║░░██║██║░╚███║\n" +
                            "╚═╝░░╚═╝╚═╝░░╚═╝╚═╝░░╚══╝░╚═════╝░╚═╝░░░░░╚═╝╚═╝░░╚═╝╚═╝░░╚══╝");
        out.writeUTF(   "                                                              \n" +
                            "                                                              ");

        //TODO: write hangman, based on how many mistakes made
        //...

        //TODO: write right amount of _'s by using foreach loop based on wordlength
        switch(word.length()){
            case 1:
                out.writeUTF("_");
                break;
            case 2:
                out.writeUTF("__");
                break;
            case 3:
                out.writeUTF("___");
                break;
            case 4:
                out.writeUTF("____");
                break;
            case 5:
                out.writeUTF("_____");
                break;
            case 6:
                out.writeUTF("______");
                break;
            case 7:
                out.writeUTF("_______");
                break;
            case 8:
                out.writeUTF("________");
                break;
            case 9:
                out.writeUTF("__________");
                break;
            case 10:
                out.writeUTF("___________");
                break;
            case 11:
                out.writeUTF("____________");
                break;
            case 12:
                out.writeUTF("_____________");
                break;
            case 13:
                out.writeUTF("______________");
                break;
            case 14:
                out.writeUTF("_______________");
                break;
        }
    }
}