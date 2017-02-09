
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
    // Separators
    public final static String SEPARATOR = " ";
    public final static String LINE_SEPARATOR = "\r\n";
    public final static String HEADER_SEPARATOR = ":";
    
    // Mail headers
    public final static String HEADER_FROM = "From";
    public final static String HEADER_TO = "To";
    public final static String HEADER_SUBJECT = "Subject";
    public final static String HEADER_DATE = "Date";
    public final static String HEADER_MESSAGE_ID = "Message-ID";
    public final static String HEADER_READ = "Read";
    
    public final static String END_OF_MAIL = ".\r\n";
    
    // Response codes
    public final static String OK = "+OK";
    public final static String ERR = "-ERR";
    
    // Pop3 commands
    public final static String APOP = "APOP";
    public final static String QUIT = "QUIT";
    public final static String STAT = "STAT";
    public final static String DELETE = "DELE";
    public final static String RETR = "RETR";
    public final static String RESET = "RSET";
}
