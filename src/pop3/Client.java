
package pop3;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    // Client parameters
    private boolean delete = false;
    private State state = State.LAUNCHED;
    
    // TCP connexion
    private Socket socket;
    private BufferedInputStream in = null;
    private BufferedOutputStream out = null;
    
    // Error management
    private int code = 0;
    private Exception exception = null;
    private String errorMsg = "";

    public enum State
    {
        LAUNCHED,
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
        try {
            socket = new Socket(hostName, port);
            in = new BufferedInputStream(socket.getInputStream());
            out = new BufferedOutputStream(socket.getOutputStream());
        } catch (IOException ex) {
            if (ex instanceof SocketException) {
            } else if (ex instanceof UnknownHostException) {
            } else {
            }
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void connection()
    {
        try {
            String serverMsg = this.readFromServer();
            String[] serverArray = serverMsg.split(" ");

            String request = "";
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    private String readFromServer() throws IOException
    {
        int dataRead , i;
        ArrayList<Byte> datas = new ArrayList();
        while (in.available() == 0);
        while ((dataRead = in.read()) != -1) {
            byte b = (byte) dataRead;
            datas.add(b);
            if (b == -1) break;
        }
        byte[] data = new byte[datas.size()];
        for (i = 0; i < datas.size(); i++) {
            data[i] = datas.get(i);
        }

        return new String(data);
    }

    public static void main(String[] args) {
        String host = "localhost";
        int port = 110;
        
        Client client = new Client(host, port);
        client.connection();
    }
}
