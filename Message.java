/**
 * This class contains attributes related to the message that will be sent from
 * the user to the server.
 * @author Ali Jalil, Josh Egwaikhide, Nick Chalardsoontornvatee
 */
import java.util.Date;

public class Message {
    private String content;  // Encrypted content
    private Date timestamp;  // Timestamp of the message

    // Constructor
    public Message(String content, String senderId, String receiverId) {
        this.content = content;
        this.timestamp = new Date();
    }

    // Getters
    public String getContent() {
        return content;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "Message{" +
                "content='" + content + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
