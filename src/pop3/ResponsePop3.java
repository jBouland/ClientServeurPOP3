
package pop3;

import java.security.MessageDigest;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    private ResponseType type = null;
    private String statut = "";
    private String message = "";
    private String timeStamp = "";

    private int nbMails = 0;
    private int mailSize = 0;
    private Mail mail = null;

    public enum ResponseType
    {
        READY_OK(2),
        APOP_OK(2),
        STAT_OK(3),
        LIST_OK(3),
        RETR_OK(4),
        DELE_OK(4),
        QUIT_OK(2),
        ERR(2);
        
        public final int nbParts;
        
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

    public ResponsePop3(ResponseType type, String message)
    {
        try {
            this.hydrateResponse(type, message);
        } catch (Exception ex) {
            Logger.getLogger(ResponsePop3.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void hydrateResponse(ResponseType responseType, String message) throws Exception
    {
        String[] rawResponse = message.split(Pop3.LINE_SEPARATOR);
        
        // Read first line
        String[] rawFirstLine = rawResponse[0].split(Pop3.SEPARATOR);
        if (!rawFirstLine[0].equalsIgnoreCase(Pop3.OK) && !rawFirstLine[0].equalsIgnoreCase(Pop3.ERR)) {
            // Error
            throw new Exception("Le format de la réponse est invalide.");
        }

        // Setting message statut
        statut = rawFirstLine[0];
        if (statut.equalsIgnoreCase(Pop3.ERR)) {
            type = ResponseType.ERR;
        } else {
            type = responseType;
        }

        // Hydrating response
        String[] parameters = rawResponse[0].split(Pop3.SEPARATOR, type.nbParts);
        switch (type) {
            case STAT_OK :
                this.nbMails = Integer.valueOf(parameters[1]);
                this.mailSize = Integer.valueOf(parameters[2]);
                break;
            case READY_OK :
                if(parameters[1].indexOf('<') != -1 && (parameters[1].indexOf('>') != -1)){
                    String print = message.substring(parameters[1].indexOf('<'),parameters[1].indexOf('>'));
                    this.timeStamp = print; 
                } else {
                    System.err.println("Server not secured : no timestamp detected"); 
                }
                
                break;
            case LIST_OK:
                break;
            case RETR_OK:
                this.mailSize = Integer.valueOf(parameters[1]);
                // Hydrating mail
                String mailString = message.substring(rawResponse[0].length() + Pop3.LINE_SEPARATOR.length());
                this.mail = new Mail(mailString);
                break;
            case DELE_OK:
                this.nbMails = Integer.valueOf(parameters[2]);
                break;
            default: // ERR, LIST, QUIT_OK...
                this.message = parameters[1];
                break;
        }
    }
    
    public boolean isOk()
    {
        return statut.equalsIgnoreCase(Pop3.OK);
    }
    
    public boolean isErr()
    {
        return statut.equalsIgnoreCase(Pop3.ERR);
    }

    public ResponseType getType() {
        return type;
    }

    public void setType(ResponseType type) {
        this.type = type;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getNbMails() {
        return nbMails;
    }

    public void setNbMails(int nbMails) {
        this.nbMails = nbMails;
    }

    public int getMailSize() {
        return mailSize;
    }

    public void setMailSize(int mailSize) {
        this.mailSize = mailSize;
    }

    public Mail getMail() {
        return mail;
    }

    public void setMail(Mail mail) {
        this.mail = mail;
    }

    public String getTimeStamp() {
        return timeStamp;
    }
    
    @Override
    public String toString()
    {
        String s = "";
        
        switch (type) {
//            case ERR :
//                break;
//            case APOP_OK:
//                break;
            default:
                s += this.statut + Pop3.SEPARATOR + this.message + Pop3.LINE_SEPARATOR;
            // TODO all cases
        }
        
        return s;
    }
}
