
package pop3;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.SocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import pop3.ResponsePop3.ResponseType;

public class Client2 extends Thread {
    
    private final static String CLIENT_HOME_DIR = "\\ClientHome\\";

    public static void main(String[] args) {
        try {
            Client2 client = new Client2("localhost", 3000, "userMachin", "test");
            client.start();
        } catch (Exception ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private String username = "";
    private String password = "";
    private State currentState = State.closed;
    private ResponseType expected = ResponseType.READY_OK;
    private Map<Integer, Mail> mails = new HashMap();
    private int nbMails = 0;
    private boolean deletable = false;
    private boolean connectionClose = false;
    private int currentMail = 1;
    private boolean userConnected = false;
    private String timeStamp = "";
    private SSLSocketFactory factory;
    private Socket socket = null;
    SSLSocket souche;
    private BufferedInputStream /*BufferedReader */in = null;
    private DataOutputStream out = null;
    
    private enum State {
        closed,
        initial, // Connected with TCP (socket)
        apop,
        stat,
        retrieve,
        delete,
        quit
    };

    public Client2(String hostName, int port, String login, String password) {
        this.username = login;
        this.password = password;
        try {
            // Secure socket instanciation
            /*factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            souche = (SSLSocket) factory. createSocket (hostName, port);
            // Chosing cipher suite beyond the ones availible on socket
            souche.getEnabledCipherSuites();
            String[] enCiphersuite = souche.getEnabledCipherSuites();
            souche.setEnabledCipherSuites(enCiphersuite);
            souche.startHandshake();*/
            socket = new Socket(hostName, port);
            currentState = State.initial;
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Map<Integer, Mail> getMails() {
        return mails;
    }   
    
    public boolean isConnected(){
        if (socket != null){return true;}
        return false;
    }
    
    public void serverDialog(){
        try {
            System.out.println("Connexion serveur réussie sur le port " + socket.getPort());            
            ResponsePop3 responsePop = null;
            
            in = new BufferedInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            
            System.out.println("Waiting server...");
            while (!connectionClose) { // Boucle de communication serveur
                
                String serverResponse = readFromServer();
                if (serverResponse.isEmpty()) {
                    continue;
                }
                
                System.out.println(serverResponse);
                responsePop = new ResponsePop3(expected, serverResponse);
                
                switch (currentState) {
                    case initial:
                        if (responsePop.isOk()) {
                            System.out.println("Tentative de connexion de " + username + ":" + password);
                            timeStamp = responsePop.getTimeStamp();
                            if(!timeStamp.isEmpty()){
                                password = encodeMD5(timeStamp.concat(password)); 
                            }
                            this.sendApop(username, password);
                            currentState = State.apop;
                            expected = ResponseType.APOP_OK;
                        } else if (responsePop.isErr()) {
                            responsePop.getMessage();
                        }
                        break;
                    case apop:
                        if (responsePop.isOk()) {
                            System.out.println("Utilisateur " + username + " authentifié avec succès.");
                            this.createLocalUserDirectory(username, password);
                            this.sendStat();
                            userConnected = true;
                            currentState = State.retrieve;
                            expected = ResponseType.STAT_OK;
                        } else if (responsePop.isErr()) {
                            this.sendQuit();
                            currentState = State.quit;
                            expected = ResponseType.QUIT_OK;
                        }
                        break;
                    case retrieve:
                        switch (responsePop.getType()) {
                            case STAT_OK:
                                //System.out.println("Stat OK : " + responsePop.getNbMails());
                                nbMails = responsePop.getNbMails();
                                this.sendRetrieve(currentMail);
                                currentMail++;
                                expected = ResponseType.RETR_OK;
                                break;
                            case RETR_OK:
                                Mail mail = responsePop.getMail();
                                mail.setMessageID(currentMail - 1);
                                
                                // TODO save mail in local folder:
                                this.createLocalMail(mail);
                                
                                // TODO send Delete request if deleteable true
                                if (deletable) {
                                    this.sendDelete(mail.getMessageID());
                                    currentState = State.delete;
                                    expected = ResponseType.DELE_OK;
                                } else {
                                    // Retrieve other mails
                                    this.sendRetrieveOrQuit();
                                }
                                break;
                            case ERR:
                                System.err.println(responsePop.getMessage());
                                break;
                        }
                        break;
                    case delete:
                        if (responsePop.isOk()) {
                            this.sendRetrieveOrQuit();
                        }
                        break;
                    case quit:
                        if (responsePop.isOk()) {
                            connectionClose = true;
                        }
                        break;
                }
                
                if (connectionClose) {
                    in.close();
                    out.close();
                    socket.close();
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Client2.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void localConnection(){
        System.out.println("Lecture des mails locaux :");
        this.readLocalMails();
    }
    
    @Override
    public void run()
    {
        try {
            if (socket != null) {
                System.out.println("Connexion serveur réussie sur le port " + socket.getPort());

                // Informations de connexion utilisateur
                Scanner sc = new Scanner(System.in);
                System.out.println("Login : ");
                username = sc.nextLine();
                System.out.println("Mot de passe : ");
                password = sc.nextLine();

                ResponsePop3 responsePop = null;

                in = new BufferedInputStream(socket.getInputStream());
                out = new DataOutputStream(socket.getOutputStream());

                System.out.println("Waiting server...");
                while (!connectionClose) { // Boucle de communication serveur

                    String serverResponse = readFromServer();
                    if (serverResponse.isEmpty()) {
                        continue;
                    }

                    System.out.println(serverResponse);
                    responsePop = new ResponsePop3(expected, serverResponse);

                    switch (currentState) {
                        case initial:
                            if (responsePop.isOk()) {
                                System.out.println("Tentative de connexion de " + username + ":" + password);
                                timeStamp = responsePop.getTimeStamp();
                                if(!timeStamp.isEmpty() ){
                                   password = encodeMD5(timeStamp.concat(password)); 
                                }                                
                                this.sendApop(username, password);
                                currentState = State.apop;
                                expected = ResponseType.APOP_OK;
                            } else if (responsePop.isErr()) {
                                responsePop.getMessage();
                            }
                            break;
                        case apop:
                            if (responsePop.isOk()) {
                                System.out.println("Utilisateur " + username + " authentifié avec succès.");
                                this.createLocalUserDirectory(username, password);
                                this.sendStat();
                                userConnected = true;
                                currentState = State.retrieve;
                                expected = ResponseType.STAT_OK;
                            } else if (responsePop.isErr()) {
                                this.sendQuit();
                                currentState = State.quit;
                                expected = ResponseType.QUIT_OK;
                            }
                            break;
                        case retrieve:
                            switch (responsePop.getType()) {
                                case STAT_OK:
                                    //System.out.println("Stat OK : " + responsePop.getNbMails());
                                    nbMails = responsePop.getNbMails();
                                    this.sendRetrieve(currentMail);
                                    currentMail++;
                                    expected = ResponseType.RETR_OK;
                                    break;
                                case RETR_OK:
                                    Mail mail = responsePop.getMail();
                                    mail.setMessageID(currentMail - 1);
                                    
                                    // TODO save mail in local folder:
                                    this.createLocalMail(mail);

                                    // TODO send Delete request if deleteable true
                                    if (deletable) {
                                        this.sendDelete(mail.getMessageID());
                                        currentState = State.delete;
                                        expected = ResponseType.DELE_OK;
                                    } else {
                                        // Retrieve other mails
                                        this.sendRetrieveOrQuit();
                                    }
                                    break;
                                case ERR:
                                    System.err.println(responsePop.getMessage());
                                    break;
                            }
                            break;
                        case delete:
                            if (responsePop.isOk()) {
                                this.sendRetrieveOrQuit();
                            }
                            break;
                        case quit:
                            if (responsePop.isOk()) {
                                connectionClose = true;
                            }
                            break;
                    }
                    
                    if (connectionClose) {
                        in.close();
                        out.close();
                        socket.close();
                    }
                }
            }
            
            if (!userConnected) {
                System.out.println("Connexion en local...");
                Scanner sc = new Scanner(System.in);
                System.out.println("Login : ");
                username = sc.nextLine();
                System.out.println("Mot de passe : ");
                password = sc.nextLine();
                //password = encodeMD5(timeStamp.concat(password));
                if (!this.localUserConnection(username, password)) {
                    System.err.println("Mot de passe incorrect");
                    return;
                }
            }

            System.out.println("Lecture des mails locaux :");
            // TODO handle local mails
            this.readLocalMails();

            // Lecture des mails
            if (mails.size() > 0) {
                int current = 0;
                Scanner scanner = new Scanner(System.in);
                do {
                    for (Mail mail : mails.values()) {
                        System.out.print(mail.getMessageID() + " - " + mail.getSubject());

                        if (mail.isRead()) {
                            System.out.println(" [LU]");
                        }
                    }
                    System.out.println("Choisissez le numéro du mail à lire (0 pour quitter): ");
                    current = scanner.nextInt();

                    if (mails.containsKey(current)) {
                        System.out.println("==========================");
                        System.out.println(mails.get(current).toString());
                        System.out.println("==========================");
                    } else if (current != 0) {
                        System.err.println("Numéro invalide");
                    }
                } while (current != 0);
            } else {
                System.out.println("Aucun message dans la boite locale.");
            }
        } catch (IOException ex) {
            if (ex instanceof SocketException) {
                System.err.println("Connexion interrompue par le serveur");
            } else {
                System.err.println(ex.getMessage());
            }
        }
    }
    
    private void sendApop(String login, String pass)
    {
        try {
            String apopRequest = Pop3.APOP + Pop3.SEPARATOR + login + Pop3.SEPARATOR + pass + Pop3.LINE_SEPARATOR;
            out.writeBytes(apopRequest);
            out.flush();
            printRequest(apopRequest);
        } catch (IOException ex) {
            Logger.getLogger(Client2.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void sendStat()
    {
        try {
            String statRequest = Pop3.STAT + Pop3.LINE_SEPARATOR;
            out.writeBytes(statRequest);
            out.flush();
            printRequest(statRequest);
        } catch (IOException ex) {
            Logger.getLogger(Client2.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void sendRetrieve(int id)
    {
        try {
            int[] params = new int[] {id};
            RequestPop3 retrRequest = new RequestPop3(RequestPop3.CommandPop3.RETRIEVE, params);
            out.writeBytes(retrRequest.toString());
            out.flush();
            printRequest(retrRequest.toString());
        } catch (Exception ex) {
            Logger.getLogger(Client2.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void sendDelete(int id)
    {
        try {
            int[] params = new int[] {id};
            RequestPop3 deleteRequest = new RequestPop3(RequestPop3.CommandPop3.DELETE, params);
            out.writeBytes(deleteRequest.toString());
            out.flush();
            printRequest(deleteRequest.toString());
        } catch (Exception ex) {
            Logger.getLogger(Client2.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void sendQuit()
    {
        try {
            String quitRequest = Pop3.QUIT + Pop3.LINE_SEPARATOR;
            out.writeBytes(quitRequest);
            out.flush();
            printRequest(quitRequest);
        } catch (IOException ex) {
            Logger.getLogger(Client2.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void sendRetrieveOrQuit()
    {
        if (currentMail <= nbMails) {
            this.sendRetrieve(currentMail);
            currentMail++;
            expected = ResponseType.RETR_OK;
            currentState = State.retrieve;
        } else {
            this.sendQuit();
            expected = ResponseType.QUIT_OK;
            currentState = State.quit;
        }
    }
    
    private void printRequest(String request)
    {
        System.out.println(">> " + request);
    }

    private void createLocalUserDirectory(String username, String password)
    {
        try {
            File dir = new File(System.getProperty("user.dir") + CLIENT_HOME_DIR + username);
            if (!dir.exists()) {
                if (dir.mkdirs()) {
                    File profile = new File(System.getProperty("user.dir") + CLIENT_HOME_DIR + username + ".txt");
                    FileOutputStream outputStream = null;
                    outputStream = new FileOutputStream(profile);
                    outputStream.write(password.getBytes());
                    outputStream.close();
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Client2.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Client2.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void createLocalMail(Mail mail)
    {
        try {
            File file = new File(System.getProperty("user.dir") + CLIENT_HOME_DIR + username + "\\" + mail.getMessageID());
            FileOutputStream outputStream = null;
            outputStream = new FileOutputStream(file);
            outputStream.write(mail.getContent().getBytes());
            outputStream.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Client2.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Client2.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private boolean localUserConnection(String username, String password)
    {
        try {
            byte[] localPass = Files.readAllBytes(Paths.get(System.getProperty("user.dir") + CLIENT_HOME_DIR + username + ".txt"));
            if (password.equals(new String(localPass, "UTF-8"))) {
                return true;
            }
        } catch (IOException ex) {
            System.err.println("Mauvais identifiants : " + ex.getMessage());
            return false;
        }
        return false;
    }
    
    private void readLocalMails()
    {
        try {
            File userDir = new File(System.getProperty("user.dir") + CLIENT_HOME_DIR + username);
            if (userDir.exists()) {
                File[] dirList = userDir.listFiles();
                if (dirList != null) {
                    for (File child : dirList) {
                        // Getting file name which is mail id
                        int messageId = Integer.parseInt(child.getName());
                        Mail mail = new Mail(Files.readAllBytes(child.toPath()));
                        mail.setMessageID(messageId);
                        mail.setRead(true);
                        mails.put(messageId, mail);
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Client2.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void readLocalMailsForVIew(){
        this.readLocalMails();
    }

    private String readFromServer()
    {
        String response = "";
        byte[] buffer; // 4096
        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        
        try {
            while (in.available() > 0) {
                int buffersize = in.available();
                if (buffersize > 4096) {
                    buffersize = 4096;
                }
                buffer = new byte[buffersize];
                in.read(buffer);
                byteArray.write(buffer);

                String bufferstring = new String(buffer);
                response += bufferstring;
            }
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }
        
        return response;
    }
    
    private String encodeMD5(String print)
    {
        String encrypted = "";
        try {
            byte[] printByte = print.getBytes();
            byte[] bytes = MessageDigest.getInstance("MD5").digest(printByte);
            for (byte b : bytes) {
                encrypted = encrypted + Integer.toHexString(b & 0xFF);
            }
        } catch (Exception ex) {
            Logger.getLogger(Client2.class.getName()).log(Level.SEVERE, null, ex);
        }
        return encrypted;
    }
}
