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

    public Server() {

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

    private void retrieveAction() {

    }

    private void statAction() {
        File dir = new File(user);

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
