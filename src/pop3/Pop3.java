
package pop3;

/**
 * Class Pop3
 * 
 * @author Joris BOULAND
 * @author Tommy CABRELLI
 * @author Mélanie DUBREUIL
 * @author Ophélie EOUZAN
 */
public class Pop3
{
    public final static String SEPARATOR = " ";
    
    public final static String OK = "+OK";
    public final static String ERR = "-ERR";
    
    public final static String APOP = "APOP";
    public final static String QUIT = "QUIT";
    public final static String STAT = "STAT";
    public final static String DELETE = "DELE";
    public final static String RETR = "RETR";
    public final static String RESET = "RSET";
    
    public enum ResponseType
    {
        APOP_OK(2),
        STAT_OK(3),
        LIST_OK(3),
        RETR_OK(4),
        DELE_OK(4),
        QUIT_OK(2),
        ERR(2);
        
        private final int nbParts;
        
        private MessageType(int nbParts)
        {
            this.nbParts = nbParts;
        }
    }
}
