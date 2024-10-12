import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ServeurMT extends Thread {
    private int nb_client = 0; // Compteur de clients connectés
    private ArrayList<Socket> clients; // Liste pour stocker les sockets des clients connectés

    // Constructeur du serveur
    public ServeurMT() {
        clients = new ArrayList<>(); // Initialisation de la liste des clients
    }

    @Override
    public void run() {
        try {
            // Création d'un ServerSocket pour écouter les connexions sur le port 1002
            ServerSocket serveur = new ServerSocket(1001);
            System.out.println("Serveur en attente de connexions...");

            // Boucle infinie pour accepter les connexions des clients
            while (true) {
                // Acceptation d'une nouvelle connexion
                Socket socket = serveur.accept();
                nb_client++; // Incrémentation du compteur de clients
                clients.add(socket); // Ajout du socket à la liste des clients

                // Création d'une nouvelle instance de Conversation pour gérer la communication avec le client
                Conversation conversation = new Conversation(socket, nb_client, clients);
                conversation.sendConnectedClients(); // Envoi de la liste des clients connectés au nouveau client
                conversation.start(); // Démarrage du thread de conversation
            }

        } catch (IOException e) {
            e.printStackTrace(); // Gestion des exceptions d'entrée/sortie
        }
    }

    public static void main(String[] args) {
        // Démarrage du serveur
        ServeurMT serveur = new ServeurMT();
        serveur.start();
    }
}
