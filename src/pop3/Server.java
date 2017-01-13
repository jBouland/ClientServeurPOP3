/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pop3;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Epulapp
 */
public class Server extends Thread {


    private enum etat {

        autorize,
        transaction,
        update
    };

    private etat currentState;
    private boolean closeConnection;

    private String user = "undefined";
    private ArrayList<Mail> listMail;

    public Server() {
        listMail = new ArrayList();
        closeConnection = false;
        this.start();
    }

    public void run() {
        currentState = etat.autorize;
        try {

            ServerSocket welcomeSocket = new ServerSocket(1080);
            while (true) {
                Socket connectionSocket = welcomeSocket.accept();
                System.out.println("Connection acceptée !");
                BufferedInputStream inFromClient = new BufferedInputStream(connectionSocket.getInputStream());
                String response = "undefined";

                while (inFromClient.available() > 0) {
                    System.out.println("Début réception");
                    byte[] message = new byte[512];
                    inFromClient.read(message);
                    String stringifiedMessage = message.toString().split(" ")[0];

                    switch (stringifiedMessage) {
                        case Pop3.APOP:
                            response = apopAction(stringifiedMessage);
                            break;
                        case Pop3.DELETE:
                            response = deleteAction();
                            break;
                        case Pop3.QUIT:
                            response = quitAction();
                            break;
                        case Pop3.RESET:
                            response = resetAction();
                            break;
                        case Pop3.RETR:
                            response = retrieveAction();
                            break;
                        case Pop3.STAT:
                            response = statAction();
                            break;
                    }
                }
                if (!response.equals("undefined")) {
                    sendMessage(connectionSocket, response);
                }
                if(closeConnection){
                    connectionSocket.close();
                    welcomeSocket.close();
                    
                }

            }
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private String deleteAction() {
        return "not supported yet";
    }

    private String quitAction() {
        String returnedMessage = "";
        if (etat.transaction == currentState) {
            try {
                int nbsuppression = 0;
                for (int i = 0; i < listMail.size(); i++) {
                    if (listMail.get(i).isToDelete()) {
                        listMail.remove(i);
                        nbsuppression++;
                    }
                }
                returnedMessage = Pop3.OK + " " + nbsuppression + " mails supprimés";

            } catch (Exception e) {
                returnedMessage = Pop3.ERR + " Erreur de suppression";
            }

        }

        closeConnection = true;
        returnedMessage += " Fermeture de la connection";
        
        return returnedMessage;

    }

    private String resetAction() {
        return "not supported yet";

    }

    private String retrieveAction() {
        return "not supported yet";
    }

    private String statAction() {
        if (etat.transaction != currentState) {
            System.out.println("-ERR Unsupported in this state");
            return Pop3.ERR + " Unsupported in this state";
        }
        String returnMessage = Pop3.OK;
        int sizeMessage = 0;
        File dir = new File(user + "\\mails");
        if (dir.exists()) {
            File[] dirList = dir.listFiles();
            if (dirList != null) {
                for (File child : dirList) {
                    try {
                        Mail newMail = new Mail(Files.readAllBytes(child.toPath()));
                        sizeMessage += newMail.getContent().length;
                        listMail.add(newMail);

                    } catch (IOException ex) {
                        System.err.println("Une erreur est survenue pendant la lecture du message");
                        Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                        return Pop3.ERR + " internal server error";
                    }
                }
            }
        }
        returnMessage += " " + listMail.size() + " " + sizeMessage;

        return returnMessage;
    }

    private String apopAction(String message) {

        if (currentState == etat.autorize) {
            String[] temp = message.split(" ");
            user = temp[1];
            String pass = temp[2];
            byte[] encoded;

            try {
                encoded = Files.readAllBytes(Paths.get(user + "/password.txt"));

                if (pass.equals(new String(encoded, "UTF-8"))) {
                    currentState = etat.transaction;
                    return Pop3.OK;
                } else {
                    return Pop3.ERR + " Authentication failed";
                }

            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return Pop3.ERR + " wrong current state";
    }

    private void sendMessage(Socket connectionSocket, String msg) {
        PrintWriter writeSock = null;
        try {
            writeSock = new PrintWriter(connectionSocket.getOutputStream());
            OutputStream out = connectionSocket.getOutputStream();
            out.write(msg.getBytes("UTF-8"));
            out.flush();
            out.close();
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            writeSock.close();
        }

    }
}
