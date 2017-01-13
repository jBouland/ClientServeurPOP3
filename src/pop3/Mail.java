
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
    
    public Mail(byte[] data)
    {
        this.content = new String(data);
        this.contentLength = data.length;
    }

    public Mail(String data)
    {
        this.content = data;
        this.contentLength = data.getBytes().length;
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
}
