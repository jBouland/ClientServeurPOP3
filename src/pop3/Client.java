
package pop3;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
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
    private boolean delete = false;
    private Socket socket;
    private State state = State.LAUNCHED;
    
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
            this.socket = new Socket(hostName, port);
        } catch (IOException ex) {
            if (ex instanceof SocketException) {
            } else if (ex instanceof UnknownHostException) {
            } else {
            }
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void getMessage()
    {

    }

    public static void main(String[] args) {
        String host = "localhost";
        int port = 110;
        
        Client client = new Client(host, port);
        client.getMessage();
    }
}
