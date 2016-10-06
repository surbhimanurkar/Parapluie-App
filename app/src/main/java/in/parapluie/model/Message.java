package in.parapluie.model;

/**
 * Created by surbhimanurkar on 03-03-2016.
 * Defines message received or sent
 */
public class Message {


    private String message;
    private int author;
    private Object time;
    private String image;
    private boolean read;

    private Message(){

    }

    public Message(String message, String image, int author, Object time, boolean read) {
        this.message = message;
        this.author = author;
        this.time = time;
        this.image = image;
        this.read = read;
    }


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getAuthor() {
        return author;
    }

    public void setAuthor(int author) {
        this.author = author;
    }

    public Object getTime() {
        return time;
    }

    public void setTime(Object time) {
        this.time = time;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }
}
