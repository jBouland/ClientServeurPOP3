/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pop3;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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

    private String user = "undefined";
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
                    String response;

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
                            response = apopAction();
                            break;
                        case Pop3.STAT:
                            response = apopAction();
                            break;
                    }
                }
                System.out.println("Fin réception, envoie de la réponse");
                sendMessage(connectionSocket, user);

            }
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private String deleteAction() {
        return "not supported yet";
    }

    private String quitAction() {
        return "not supported yet";

    }

    private String resetAction() {
        return "not supported yet";

    }

    private String retrieveAction() {
        return "not supported yet";
    }

    private String statAction() {
        String returnMessage = "+OK ";
        int sizeMessage = 0;
        File dir = new File(user);
        if (dir.exists()) {
            File[] dirList = dir.listFiles();
            if (dirList != null) {
                for (File child : dirList) {
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(child));
                        StringBuilder sb = new StringBuilder();
                        String line = br.readLine();
                        while (line != null) {
                            sb.append(line);
                            sb.append(System.lineSeparator());
                            line = br.readLine();
                        }

                        String message = sb.toString();
                        Mail newMail = new Mail(message.getBytes());
                        sizeMessage += newMail.getContent().length;
                        listMail.add(newMail);

                    } catch (IOException ex) {
                        System.err.println("Une erreur est survenue pendant la lecture du message");
                        Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
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
