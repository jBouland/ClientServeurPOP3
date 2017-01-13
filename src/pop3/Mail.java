
package pop3;

/**
 * Class Mail
 * 
 * @author Joris BOULAND
 * @author Tommy CABRELLI
 * @author Mélanie DUBREUIL
 * @author Ophélie EOUZAN
 */
public class Mail
{
    private boolean read = false;
    private boolean toDelete = false;
    private String content;
    private int contentLength;
    private int messageID;
    private String from;
    private String to;
    private String subject;
    private String date;
    private String message;
    
    public Mail(byte[] data)
    {
        this.content = new String(data);
    }

    public Mail(String data, int id)
    {
        this.content = data;
        this.contentLength = data.length();
        this.setMessageID(id);
        String lines[] = this.content.split("\\r?\\n");
        // TODO : Rendre le traitement plus modulaire. Exemple : le message n'est pas à la norme ...
        for(int i = 0 ; i < lines.length ; i++){
            if(i < 6){
               String line[] = lines[i].split(" ");
                switch(i){
                    case 0 :
                        this.setFrom(line[1]);
                        break;
                    case 1 :
                        this.setTo(line[1]);
                        break;
                    case 2 :
                        this.setSubject(line[1]);
                        break;
                    case 3 :
                        this.setDate(line[1]);
                        break;
                    default :
                        break;
                } 
            } else if(i >=6 && i < lines.length){
                String message = this.getMessage();
                // Verifier que ça n'enlève pas les sauts de ligne
                String concat = message.concat(lines[i]);
                this.setMessage(concat);
            }
        }
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public boolean isToDelete() {
        return toDelete;
    }

    public void setToDelete(boolean toDelete) {
        this.toDelete = toDelete;
    }

    public String getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = new String(content);
    }

    public int getContentLength() {
        return contentLength;
    }

    public void setContentLength(int contentLength) {
        this.contentLength = contentLength;
    }

    public int getMessageID() {
        return messageID;
    }

    public void setMessageID(int messageID) {
        this.messageID = messageID;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    
    @Override
    public String toString()
    {
        return content;
    }
}
