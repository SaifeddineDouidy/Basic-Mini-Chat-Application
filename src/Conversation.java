import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class Conversation extends Thread {
    private int nb_client; // Numéro du client
    private Socket socket; // Socket de communication pour ce client
    private ArrayList<Socket> clients; // Liste des sockets des clients connectés

    // Couleurs pour les messages
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_GREEN = "\u001B[32m"; // Message envoyé par le client
    private static final String ANSI_BLUE = "\u001B[34m";  // Message reçu par le client
    private static final String ANSI_RED = "\u001B[31m";   // Message d'erreur
    private static final String ANSI_YELLOW = "\u001B[33m"; // Liste des clients

    // Constructeur de la classe Conversation
    public Conversation(Socket socket, int nb_client, ArrayList<Socket> clients) {
        this.nb_client = nb_client; // Initialisation du numéro du client
        this.socket = socket; // Initialisation du socket du client
        this.clients = clients; // Initialisation de la liste des clients
    }

    @Override
    public void run() {
        try {
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();
            // Création d'un BufferedReader pour lire les messages du client
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Envoi de l'invite pour saisir un message au client au démarrage de la session
            PrintWriter clientWriter = new PrintWriter(socket.getOutputStream(), true);
            clientWriter.println(ANSI_YELLOW + "Veuillez saisir un message (format: numéroClient/message) : " + ANSI_RESET);

            while (true) {
                // Lecture d'un message envoyé par le client
                String data = br.readLine();
                if (data == null) break; // Si le message est null, sortir de la boucle
                if (data.equals("EXIT")) {
                    // Fermez les flux avant de fermer la socket
                    in.close();
                    out.close();
                    socket.close();
                    System.out.println("Client " + nb_client + " s'est déconnecté.");
                    break; // Sortir de la boucle après avoir fermé la socket
                }

                // Affichage du message envoyé par le client
                System.out.println(ANSI_GREEN + "Le message envoyé par client " + nb_client + " est : " + data + ANSI_RESET + "\n");

                // Séparation du message en fonction du caractère '/'
                String[] parts = data.split("/");

                // Vérification que le message est au bon format
                if (parts.length < 2) continue;

                // Récupération du numéro de destination et du message
                int dest = Integer.parseInt(parts[0]);
                String message = parts[1];

                PrintWriter pw = null;
                boolean messageSent = false;

                // Parcours de la liste des clients pour envoyer le message
                for (Socket clientSocket : clients) {
                    if (dest == 0) {
                        // Si la destination est 0, envoyer le message à tous les clients
                        broadcastMessage(message);
                        messageSent = true;
                    }

                    // Vérification si le numéro de destination correspond à un client
                    if (clients.indexOf(clientSocket) + 1 == dest) {
                        pw = new PrintWriter(clientSocket.getOutputStream(), true);
                        pw.println(ANSI_BLUE + "Message reçu de client " + nb_client + ": " + message + ANSI_RESET + "\n");

                        // L'autre client peut maintenant répondre
                        pw.println(ANSI_YELLOW + "Veuillez répondre ou saisir un message : " + ANSI_RESET);

                        messageSent = true; // Indiquer que le message a été envoyé
                        clientWriter.println(ANSI_YELLOW + "Message envoyé. Veuillez saisir un autre message : " + ANSI_RESET);

                        break;
                    }
                }

                // Si le message n'a pas été envoyé, le client n'existe pas
                if (!messageSent) {
                    clientWriter = new PrintWriter(socket.getOutputStream(), true);
                    clientWriter.println(ANSI_RED + "Erreur : Le client avec ID " + dest + " n'existe pas." + ANSI_RESET + "\n");
                }

                // Après chaque traitement, réafficher l'invite pour le prochain message
                clientWriter.println(ANSI_YELLOW + "Veuillez saisir un message (format: numéroClient/message) : " + ANSI_RESET);
            }
        } catch (IOException e) {
            e.printStackTrace(); // Gestion des erreurs d'entrée/sortie
        }
    }

    // Méthode pour envoyer un message à tous les clients connectés, sauf l'expéditeur
    private void broadcastMessage(String message) throws IOException {
        for (Socket clientSocket : clients) {
            if (clientSocket != socket) { // Ne pas envoyer au socket de l'expéditeur
                PrintWriter pw = new PrintWriter(clientSocket.getOutputStream(), true);
                pw.println("Message Broadcast de la part du client " + nb_client + ": " + message + "\n");
            }
        }
    }

    // Méthode pour envoyer la liste des clients connectés à tous les clients
    private void broadcastMessageServer(String message) throws IOException {
        for (Socket clientSocket : clients) {
            PrintWriter pw = new PrintWriter(clientSocket.getOutputStream(), true);
            pw.println(ANSI_YELLOW + message + ANSI_RESET + "\n"); // Envoi du message
        }
    }

    // Méthode pour envoyer la liste des clients connectés au nouveau client
    public void sendConnectedClients() throws IOException {
        StringBuilder connectedClientsMessage = new StringBuilder();
        connectedClientsMessage.append(ANSI_YELLOW + "===== Liste des clients connectés =====\n");
        connectedClientsMessage.append(ANSI_YELLOW + "Le message 0/message est un message BROADCAST\n");
        connectedClientsMessage.append(ANSI_YELLOW + "EXIT pour couper la connexion\n");

        for (int i = 0; i < clients.size(); i++) {
            Socket clientSocket = clients.get(i);
            int clientNumber = i + 1; // Numéro de client (1-indexé)
            String clientAddress = clientSocket.getRemoteSocketAddress().toString(); // Adresse du client
            connectedClientsMessage.append("Client ").append(clientNumber)
                    .append(" (").append(clientAddress).append(")\n");
        }
        connectedClientsMessage.append(ANSI_YELLOW + "=================================" + ANSI_RESET);

        // Envoi de la liste des clients connectés
        broadcastMessageServer(connectedClientsMessage.toString());
    }
}
