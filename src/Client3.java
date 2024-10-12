import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.io.*;

public class Client3 {
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_GREEN = "\u001B[32m"; // Message envoyé par le client
    private static final String ANSI_BLUE = "\u001B[34m";  // Message reçu par le client


    public static void main(String[] args) {
        int port = 1001;
        InetAddress address;

        try {
            address = InetAddress.getByName("localhost");
            Socket soc = new Socket(address, port);
            System.out.println(ANSI_GREEN + "Connecté au serveur." + ANSI_RESET);

            // Thread pour recevoir les messages
            Thread receivemsg = new Thread(() -> {
                try {
                    BufferedReader entree = new BufferedReader(new InputStreamReader(soc.getInputStream()));

                    while (true) {
                        String rep = entree.readLine();
                        if (rep == null) break;

                        // Affichage des messages reçus
                        System.out.println("\n" + ANSI_BLUE + rep + ANSI_RESET);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            receivemsg.start();

            // Thread pour envoyer des messages
            Thread sendmsg = new Thread(() -> {
                try {
                    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                    PrintWriter sortie = new PrintWriter(soc.getOutputStream(), true);

                    while (true) {
                        String data = br.readLine(); // Lire les données entrées par l'utilisateur
                        sortie.println(data); // Envoyer les données au serveur
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            sendmsg.start();

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
