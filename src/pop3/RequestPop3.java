
package pop3;

/**
 * Class ResponsePop3
 * 
 * @author Joris BOULAND
 * @author Tommy CABRELLI
 * @author Mélanie DUBREUIL
 * @author Ophélie EOUZAN
 */
public class RequestPop3
{
    private CommandPop3 command;
    private int[] parameters;
    private Mail mail = null;

    public enum CommandPop3 // Primitive
    {
        APOP("APOP", 0),
        STAT("STAT", 0),
        DELETE("DELE", 1),
        RETRIEVE("RETR", 1),
        RESET("RSET", 0),
        QUIT("QUIT", 0);

        public final String value;
        public final int nbParameters;

        private CommandPop3(String value, int nbParameters)
        {
            this.value = value;
            this.nbParameters = nbParameters;
        }
    }
    
    public RequestPop3(CommandPop3 command)
    {
        this.command = command;
    }
    
    public RequestPop3(CommandPop3 command, int[] parameters) throws Exception
    {
        this(command);
        
        if (parameters.length <= command.nbParameters) {
            this.parameters = parameters;
        } else {
            throw new Exception("This command only takes " + command.nbParameters + " parameters.");
        }
    }

    public CommandPop3 getCommand() {
        return command;
    }

    public void setCommand(CommandPop3 command) {
        this.command = command;
    }

    public int[] getParameters() {
        return parameters;
    }

    public void setParameters(int[] parameters) {
        this.parameters = parameters;
    }

    public Mail getMail() {
        return mail;
    }

    public void setMail(Mail mail) {
        this.mail = mail;
    }

    @Override
    public String toString()
    {
        String s = command.value;

        for (int i = 0; i < parameters.length; i++) {
            s += Pop3.SEPARATOR + parameters[i];
        }
        s += "\r\n";
        
        if (mail != null) {
            s += mail.toString();
            //s+= "\r\n";
        }

        return s;
    }
}
