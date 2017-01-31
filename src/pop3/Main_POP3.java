
package pop3;

/**
 * Class Main
 * 
 * @author Joris BOULAND
 * @author Tommy CABRELLI
 * @author Mélanie DUBREUIL
 * @author Ophélie EOUZAN
 */
public class Main_POP3 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Server s = new Server(3000);
        s.start();
    }
    
}
