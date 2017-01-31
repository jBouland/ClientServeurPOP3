/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pop3;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
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
            Socket connectionSocket = null;
            while (!closeConnection) {

                if (connectionSocket == null) {
                    connectionSocket = welcomeSocket.accept();
                    System.out.println("Connection acceptée !");
                }

                BufferedReader in = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));

                String response = "undefined";
                String stringifiedMessage;

                while (!closeConnection && (stringifiedMessage = in.readLine()) != null) {
                    System.out.println("Début réception");
                    response = "undefined";

                    String command = stringifiedMessage.split(" ")[0];
                    ArrayList<String> params = new ArrayList(Arrays.asList(stringifiedMessage.split(" ")));
                    params.remove(0);
                    System.out.println("params : " + params);
                    System.out.println("command : " + command);
                    System.out.println("Pop3 : " + Pop3.APOP);
                    switch (command) {
                        case Pop3.APOP:
                            System.out.println("dans apop ?");
                            response = apopAction(params);
                            break;
                        case Pop3.DELETE:
                            response = deleteAction(params);
                            break;
                        case Pop3.QUIT:
                            response = quitAction();
                            break;
                        case Pop3.RESET:
                            response = resetAction();
                            break;
                        case Pop3.RETR:
                            response = retrieveAction(params);
                            break;
                        case Pop3.STAT:
                            response = statAction();
                            break;
                    }
                    if (!response.equals("undefined")) {
                        sendMessage(connectionSocket, response);
                    }

                    if (closeConnection) {
                        connectionSocket.close();
                        welcomeSocket.close();
                    }
                }

            }
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private String deleteAction(ArrayList<String> param) {
        if (etat.transaction == currentState) {
            try {
                int msgnumber = Integer.parseInt(param.get(0)) - 1;
                if (listMail.size() < msgnumber) {
                    return Pop3.ERR + " Mail not found";
                } else {
                    listMail.get(msgnumber).setToDelete(true);
                    System.out.println("delete : " + listMail.get(msgnumber).isToDelete());
                }

            } catch (Exception e) {
                return Pop3.ERR + " can't set a mail to delete";
            }
        } else {
            return Pop3.ERR + " Unsupported action in this state";
        }
        return Pop3.OK + " mail number " + param.get(0) + " deleted";

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
                returnedMessage = Pop3.OK + " " + nbsuppression + " mails deleted";

            } catch (Exception e) {
                returnedMessage = Pop3.ERR + " Delete error";
            }

        }

        closeConnection = true;
        returnedMessage += " Closing connection...";

        return returnedMessage;

    }

    private String retrieveAction(ArrayList<String> param) {
        String returnMessage = "";
        int numMessage;
        if (currentState != etat.transaction) {
            return Pop3.ERR + " Unsupported action in this state";
        }
        if (param.size() > 0) {
            try {
                numMessage = Integer.parseInt(param.get(0)) - 1;
                if (numMessage < 0 || numMessage >= listMail.size()) {
                    returnMessage = Pop3.ERR + " Message not found";
                    return returnMessage;
                }
                System.out.println("retr : " + listMail.get(numMessage).isToDelete());
                if (listMail.get(numMessage).isToDelete()) {
                    return Pop3.ERR + " This message was deleted";
                }

                returnMessage = Pop3.OK + " " + listMail.get(numMessage).getContentLength() + "\r\n" + listMail.get(numMessage).getContent() + "\r\n.\r\n";
                return returnMessage;

            } catch (Exception e) {
                System.err.println(" Wrong parameter in retrieveAction : " + param.get(0));
                returnMessage = Pop3.ERR + " Wrong parameter";
            }
        } else {
            returnMessage = Pop3.ERR + " Missing parameter";
        }
        return returnMessage;
    }

    private String resetAction() {
        if (etat.transaction != currentState) {
            System.out.println("-ERR Unsupported in this state");
            return Pop3.ERR + " Unsupported in this state";
        }
        for (Mail m : listMail) {
            m.setToDelete(false);
        }
        return Pop3.OK + " Done";
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
                        sizeMessage += newMail.getContentLength();
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

    private String apopAction(ArrayList<String> params) {
        System.out.println("on est dans APOP");

        if (currentState == etat.autorize) {
            try {
                user = params.get(0);
                String pass = params.get(1);
                byte[] encoded;
                encoded = Files.readAllBytes(Paths.get(user + "/password.txt"));

                if (pass.equals(new String(encoded, "UTF-8"))) {
                    currentState = etat.transaction;
                    return Pop3.OK;
                } else {
                    return Pop3.ERR + " Authentication failed";
                }

            } catch (Exception ex) {
                System.out.println("exception");
                return Pop3.ERR + " Authentication failed";
            }
        }
        return Pop3.ERR + " wrong current state";
    }

    private void sendMessage(Socket connectionSocket, String msg) {
        PrintWriter writeSock = null;
        try {
            msg += "\r\n";
            System.out.println(msg);
            writeSock = new PrintWriter(connectionSocket.getOutputStream());
            OutputStream out = connectionSocket.getOutputStream();
            out.write(msg.getBytes("UTF-8"));
            out.flush();
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
        }
    }
}
