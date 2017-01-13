
package pop3;

/**
 * Class ResponsePop3
 * 
 * @author Joris BOULAND
 * @author Tommy CABRELLI
 * @author Mélanie DUBREUIL
 * @author Ophélie EOUZAN
 */
public class ResponsePop3
{
    private ResponseType type;
    private String statut = "";
    private String message = "";
    
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
        
        private ResponseType(int nbParts)
        {
            this.nbParts = nbParts;
        }
    }

    /**
     * Constructor to send message
     * @param type
     * @param statut
     * @param message 
     */
    public ResponsePop3(ResponseType type, String statut, String message)
    {
        this.type = type;
        this.statut = statut; // Pop3.OK OU Pop3.ERR
        this.message = message;
    }
    
    /**
     * Constructor to retrieve message
     * @param type
     * @param message 
     * @throws java.lang.Exception 
     */
    public ResponsePop3(ResponseType type, String message) throws Exception
    {
        String[] rawMessage = message.split(Pop3.SEPARATOR);
        
        if (!rawMessage[0].equalsIgnoreCase(Pop3.OK) && !rawMessage[0].equalsIgnoreCase(Pop3.ERR)) {
            // Error
            throw new Exception("Le format de la réponse est invalide.");
        }

        statut = rawMessage[0];
        
        if (statut.equalsIgnoreCase(Pop3.ERR)) {
            this.type = ResponseType.ERR;
        } else {
            this.type = type;
        }
    }
    
    @Override
    public String toString()
    {
        String s = "";
        
        switch (type) {
            case ERR :
                s += this.statut + " " + this.message;
                break;
            case APOP_OK:
                s += this.statut + " " + this.message;
                break;
            // TODO all cases
        }
        
        return s;
    }
}
