/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pop3;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
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

    private String user = "userMachin";
    private ArrayList<Mail> listMail;

    public Server() {
        listMail = new ArrayList();
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

                while (inFromClient.available() > 0) {
                    System.out.println("Début réception");
                    byte[] message = new byte[512];
                    inFromClient.read(message);
                    String stringifiedMessage = message.toString().split(" ")[0];

                    switch (stringifiedMessage) {
                        case Pop3.APOP:
                            apopAction(stringifiedMessage);
                            break;
                        case Pop3.DELETE:
                            break;
                        case Pop3.QUIT:
                            break;
                        case Pop3.RESET:
                            break;
                        case Pop3.RETR:
                            break;
                        case Pop3.STAT:
                            break;
                    }
                }
                System.out.println("Fin réception");

            }
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void deleteAction() {

    }

    private void quitAction() {

    }

    private void resetAction() {

    }

    private String retrieveAction(String stringifiedMessage) {
        String returnMessage = "";
        int numMessage;
        if(currentState != etat.transaction){
            return Pop3.ERR + " Unsupported action in this state";
        }
        String[] param = stringifiedMessage.split(" ");
        if(param.length > 0){
            try {
                numMessage = Integer.parseInt(param[1]) -1;
                if(numMessage < 0 || numMessage >= listMail.size()){
                   returnMessage = Pop3.ERR+" Message not found";
                   return returnMessage;
                }
                if(listMail.get(numMessage).isToDelete()){
                    returnMessage = Pop3.ERR + " This message was deleted";
                }
                
                returnMessage = Pop3.OK + " " + listMail.get(numMessage).getContentLength() + "\r\n" + listMail.get(numMessage)+"\r\n.\r\n";
                return returnMessage;
                    
                
            } catch (Exception e) {
                System.err.println("Wrong parameter in retrieveAction : " + param[1]);
                returnMessage = Pop3.ERR + "Wrong parameter";
            }
        }
        return returnMessage;
    }

    private String statAction(){
        if(etat.transaction != currentState){
            System.out.println("-ERR Unsupported in this state");
            return Pop3.ERR + " Unsupported in this state";
        }
        String returnMessage = Pop3.OK;
        int sizeMessage = 0;
        File dir = new File(user + "\\mails");
        if(dir.exists()){
            File[] dirList = dir.listFiles();
            if(dirList != null){
                for(File child : dirList){
                    try{
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
            String user = temp[1];
            String pass = temp[2];
            byte[] encoded;

            try {

                encoded = Files.readAllBytes(Paths.get(user + "/password.txt"));

                if (pass.equals(new String(encoded, "UTF-8"))) {
                    currentState = etat.transaction;
                    return "OK";
                } else {
                    return "Error : Authentication failed";
                }

            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return "Error : wrong current state";
    }
}
