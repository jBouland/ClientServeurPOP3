
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
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class Server
 * 
 * @author Joris BOULAND
 * @author Tommy CABRELLI
 * @author Mélanie DUBREUIL
 * @author Ophélie EOUZAN
 */
public class Server extends Thread {

    private enum etat {
        initial,
        authorize,
        transaction,
        update
    };
    
    private String chaineControl;

    private int port;
    private etat currentState = etat.initial;
    private boolean closeConnection = false;

    private String user = "undefined";
    private ArrayList<Mail> listMail = new ArrayList();
    
    public Server(int port)
    {
        this.port = port;
    }

    @Override
    public void run() {
        try {
            // Initialization
            ServerSocket welcomeSocket = new ServerSocket(port);
            Socket connectionSocket = null;
            while (true) {

                if (connectionSocket == null) {
                    connectionSocket = welcomeSocket.accept();
                    System.out.println("Connection acceptée !");
                    currentState = etat.initial;
                    closeConnection = false;
                    sendMessage(connectionSocket, readyAction());
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
                    //System.out.println("Pop3 : " + Pop3.APOP);
                    switch (command) {
                        case Pop3.APOP:
                            //System.out.println("dans apop ?");
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
                        connectionSocket = null;
                        //welcomeSocket.close();
                    }
                }

            }
        } catch (IOException ex) {
            if (ex instanceof SocketException) {
                System.err.println("Connexion interrompue par le client");
            }
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

            } catch (NumberFormatException e) {
                System.err.println("Exception: " + e.getMessage());
                return Pop3.ERR + " can't set a mail to delete";
            }
        } else {
            return Pop3.ERR + " Unsupported action in this state";
        }
        return Pop3.OK + " mail number " + param.get(0) + " deleted";

    }

    private String quitAction() {
        String returnedMessage = "";
        if (etat.transaction == currentState || currentState == etat.authorize) {
            //delete all mail from cache
            listMail = new ArrayList<Mail>();
            try {
                int nbsuppression = 0;
                for (int i = 0; i < listMail.size(); i++) {
                    if (listMail.get(i).isToDelete()) {
                        listMail.remove(i);
                        i--;
                        nbsuppression++;
                    }
                }
                returnedMessage = Pop3.OK + " " + nbsuppression + " mails deleted";

            } catch (Exception e) {
                returnedMessage = Pop3.ERR + " Delete error";
            }

        }

        closeConnection = true;
        returnedMessage += " Connection ended";

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

            } catch (NumberFormatException e) {
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
            //System.out.println("-ERR Unsupported in this state");
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
        //System.out.println("on est dans APOP");

        if (currentState == etat.authorize) {
            try {
                user = params.get(0);
                String pass = params.get(1);
                
                byte[] encoded = Files.readAllBytes(Paths.get(user + "/password.txt"));
                
                //Create the MD5 to compare with the client
                String encrypted = "";
                String password = new String(encoded, "UTF-8");
                System.out.println(chaineControl + " " + password);
                chaineControl = chaineControl.concat(new String(encoded, "UTF-8"));
                byte[] str_code = MessageDigest.getInstance("MD5").digest(chaineControl.getBytes());               
                for (byte b : str_code) {
                    encrypted = encrypted + Integer.toHexString(b & 0xFF);
                }
                
                if (pass.equals(encrypted)) {
                    currentState = etat.transaction;
                    return Pop3.OK + " " + user + " authenticated";
                } else {
                    return Pop3.ERR + " Authentication failed";
                }

            } catch (IOException | IndexOutOfBoundsException ex) {
                System.err.println(ex.getMessage());
                if (ex instanceof IndexOutOfBoundsException) {
                    System.err.println("APOP requires 2 parameters : user and password");
                }
                return Pop3.ERR + " Authentication failed";
            } catch (NoSuchAlgorithmException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return Pop3.ERR + " wrong current state";
    }
    
    private String readyAction() {
        if (currentState == etat.initial) {
            currentState = etat.authorize;
            //Create a timestamp
            Date d = new Date();
            //Timestamp timestamp = new Timestamp(new Date(),null);
            // TODO : Format conforme à la norme ?
            Long timestamp = d.getTime();
            chaineControl = "<" + timestamp + "@serverTMOJ>";
            System.out.println(chaineControl);
            
            return Pop3.OK + " POP3 server ready " + chaineControl;
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
