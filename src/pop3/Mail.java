
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
    private String from = "";
    private String to = "";
    private String subject = "";
    private String date = "";
    private String message = ""; // body
    
    public Mail(byte[] data)
    {
        this.hydrate(new String(data));
    }

    public Mail(String data)
    {
        this.hydrate(data);
    }
    
    private void hydrate(String data)
    {
        //this.messageID = id;
        this.content = data;
        this.contentLength = data.length();

        int i = 0;
        String lines[] = this.content.split(Pop3.LINE_SEPARATOR);

        // Read headers
        while(!lines[i].isEmpty() && i < lines.length) {
            //System.out.println(lines[i]);
            String[] line = lines[i].split(Pop3.HEADER_SEPARATOR);
            String[] headerValues = line[1].split(Pop3.SEPARATOR);
            switch (line[0]) {
                case Pop3.HEADER_FROM :
                    this.from = headerValues[1];
                    break;
                case Pop3.HEADER_TO :
                    this.to = headerValues[1];
                    break;
                case Pop3.HEADER_SUBJECT :
                    this.subject = line[1];
                    break;
                case Pop3.HEADER_DATE :
                    this.date = line[1];
                    break;
            } 
            i++;
        }

        String message = "";
        while (i < lines.length && !lines[i].equals(Pop3.END_OF_MAIL)) {
            //System.out.println(lines[i]);
            message += lines[i] + "\n";
            i++;
        }
        
        if (!message.isEmpty()) {
            this.message = message;
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
