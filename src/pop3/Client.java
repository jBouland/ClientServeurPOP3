
package pop3;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import pop3.RequestPop3.CommandPop3;
import pop3.ResponsePop3.ResponseType;

/**
 * Class Client
 * 
 * @author Joris BOULAND
 * @author Tommy CABRELLI
 * @author Mélanie DUBREUIL
 * @author Ophélie EOUZAN
 */
public class Client
{
    public final static int TCP_CONNEXION_ATTEMPT = 3;
    public final static int USER_CONNEXION_ATTEMPT = 3;
    
    // User parameters
    private String username = ""; // TODO
    private String password = ""; // TODO

    // Client parameters & variables
    private boolean delete = false;
    private State state = State.CLOSED;
    private Mail[] mails = null;
    
    // TCP connexion
    private int port = 110;
    private Socket socket;
    private BufferedInputStream in = null;
    private DataOutputStream out = null;
    
    // Error management
    private int code = 0;
    private Exception exception = null;
    private String errorMsg = "";

    public enum State
    {
        CLOSED,
        WAIT_READY,
        WAIT_APOP,
        WAIT_STAT,
        WAIT_FOR_RETRIEVE,
        WAIT_DELETE,
        WAIT_QUIT,
        BOX_READY,
        WAIT_RESET,
        DISPLAY_MESSAGE
    }
    
    public Client(String hostName, int port)
    {
        this.port = port;
        try {
            socket = new Socket(hostName, port);
            in = new BufferedInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException ex) {
            if (ex instanceof SocketException) {
            } else if (ex instanceof UnknownHostException) {
            } else {
            }
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean isDelete() {
        return delete;
    }

    public void setDelete(boolean delete) {
        this.delete = delete;
    }

    public Mail[] getMails() {
        return mails;
    }

    public void setMails(Mail[] mails) {
        this.mails = mails;
    }

    public void run() throws Exception
    {
        // Initialisation
        ResponsePop3 serverResponse = null;
        state = State.WAIT_READY;
        System.out.println("Connexion au serveur sur le port " + port + "...");
        
        try {
            serverResponse = this.readFromServer();
            if (serverResponse.isOk()) {
                // Connexion utilisateur
                System.out.println("Connexion serveur réussie sur le port " + socket.getPort());
                state = State.WAIT_APOP;
                for (int i = 0; i < USER_CONNEXION_ATTEMPT; i++) {
                    this.userConnection(username, password);
                    if (state != State.WAIT_APOP) {
                        System.out.println("Utilisateur " + username + " authentifié avec succès.");
                        break;
                    }
                }
                
                // Envoi STAT
                this.sendStatRequest();
                serverResponse = this.readFromServer();
                if (serverResponse.isOk()) {
                    // Retrieve mails
                    this.retrieveMails(serverResponse.getNbMails());

                    // Suite
                }
            }
            throw new Exception(serverResponse.getMessage());
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            System.out.println(state + ": "+ ex.getMessage());
        }
    }
    
    private void userConnection(String username, String password) throws IOException, Exception
    {
        // TODO MD5
        String clientMsg = Pop3.APOP + Pop3.SEPARATOR + username + Pop3.SEPARATOR + password;
        
        // Send request
        out.writeBytes(clientMsg);
        out.flush();
        
        // Wait server response
        ResponsePop3 response = this.readFromServer();
        if (response.isOk()) {
            state = State.WAIT_FOR_RETRIEVE;
        } else if (response.isErr()) {
            state = State.WAIT_APOP;
        } else {
            throw new Exception("La réponse du serveur n'est pas correcte.");
        }
    }
    
    private void sendStatRequest() throws IOException
    {
        this.sendRequest(new RequestPop3(CommandPop3.STAT));
        state = State.WAIT_STAT;
    }
    
    private Mail[] retrieveMails(int nbMails) throws Exception
    {
        
        Mail[] emails = new Mail[nbMails];

        // Retrieve mails
        for (int i = 0; i < nbMails; i++) {
            // Retrieve mail i
            
            Mail mail = this.retrieveMail(i + 1);
            emails[i] = mail;
            
            // Delete email i
            if (delete) {
                this.deleteMail(i + 1);
            }
        }

        return emails;
    }
    
    private Mail retrieveMail(int id) throws IOException, Exception {        
        // Set State
        state = State.WAIT_FOR_RETRIEVE;
        
        // Initialisation
        int[] params = new int[] {id};
        this.sendRequest(new RequestPop3(CommandPop3.RETRIEVE, params));
        
        // Handle request & response
        ResponsePop3 retrieveResponse = this.readFromServer();
        if (retrieveResponse.isOk()) {
            Mail m = retrieveResponse.getMail();
            writeInsideDirectory(m.getMessageID(),m);
            return m;
        } else {
            throw new Exception(retrieveResponse.getMessage());
        }
    }
    
    private void writeInsideDirectory(int messageId, Mail mail) throws IOException {
        
        File dir = new File(System.getProperty("user.dir") + "\\ClientMail\\");
        if (!dir.exists()) {
            dir.mkdir();
            File dir2 = new File(System.getProperty("user.dir") + "\\ClientMail\\"+ this.username);
            if(!dir2.exists()){
                dir2.mkdir();
            }
        }
        
        File file = new File(System.getProperty("user.dir") + "\\ClientMail\\" + this.username + "\\" + messageId);
        
        FileOutputStream outputStream = null;
        outputStream = new FileOutputStream(file);        
        outputStream.write(mail.getContent().getBytes());
        outputStream.close();
    }
    
    private boolean deleteMail(int id) throws IOException, Exception {
        // Set State
        state = State.WAIT_DELETE;
        
        // Initialisation
        int[] params = new int[] {id};
        this.sendRequest(new RequestPop3(CommandPop3.DELETE, params));
        
        // Handle request & response
        ResponsePop3 retrieveResponse = this.readFromServer();
        if (retrieveResponse.isErr()) {
            System.err.println(state + ": DELETE - " + retrieveResponse.getMessage());
        }
        return retrieveResponse.isOk();
    }

    private ResponsePop3 readFromServer() throws IOException, Exception
    {
        // TODO : Check if that works
        int dataRead , i;
        ArrayList<Byte> datas = new ArrayList();
        //while (in.available() == 0);
        while ((dataRead = in.read()) != -1) {
            byte b = (byte) dataRead;
            datas.add(b);
            if (b == -1) break;
        }
        byte[] data = new byte[datas.size()];
        for (i = 0; i < datas.size(); i++) {
            data[i] = datas.get(i);
        }

        String response = new String(data);
        System.out.println(response);

        return new ResponsePop3(getExpectedResponseType(), response);
    }
    
    private void sendRequest(RequestPop3 request) throws IOException
    {
        out.writeBytes(request.toString());
        out.flush();
    }
    
    private ResponseType getExpectedResponseType()
    {
        switch (state) {
            case WAIT_APOP:
                return ResponseType.APOP_OK;
            case WAIT_READY:
                return ResponseType.READY_OK;
            case WAIT_QUIT:
                return ResponseType.QUIT_OK;
            case WAIT_FOR_RETRIEVE:
                return ResponseType.RETR_OK;
            case WAIT_DELETE:
                return ResponseType.DELE_OK;
            case WAIT_STAT:
                return ResponseType.STAT_OK;
            default:
                return ResponseType.ERR;
        }
    }

    public static void main(String[] args) {
        String host = "localhost";
        int port = 110;
        
        Client client = new Client(host, port);
        try {
            client.run();
        } catch (Exception ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
