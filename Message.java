import java.util.Date;

/**
 * The {@code Message} class represents a message that is sent from a user to a server.
 * This class contains attributes related to the content of the message and the 
 * timestamp when the message was created. The content is typically an encrypted 
 * message, and the timestamp indicates when the message was sent.
 * 
 * <p>The {@code Message} class is used to encapsulate the message content along with
 * its metadata (i.e., the timestamp). It is commonly used for communication between
 * clients and the server in a chat application.</p>
 * 
 * @author Ali Jalil, Josh Egwaikhide, Nick Chalardsoontornvatee
 */
public class Message {
    
    /**
     * The encrypted content of the message.
     * This field stores the actual message that was sent, typically in an encrypted form.
     */
    private String content;

    /**
     * The timestamp of when the message was created.
     * This field records the time the message was sent or received.
     */
    private Date timestamp;

    /**
     * Constructs a new {@code Message} with the specified content.
     * The timestamp is automatically set to the current date and time.
     *
     * @param content The encrypted content of the message.
     * @param senderId The ID of the sender (currently not used but can be extended).
     * @param receiverId The ID of the receiver (currently not used but can be extended).
     */
    public Message(String content, String senderId, String receiverId) {
        this.content = content;
        this.timestamp = new Date();
    }

    /**
     * Returns the content of the message.
     * 
     * @return The encrypted content of the message.
     */
    public String getContent() {
        return content;
    }

    /**
     * Returns the timestamp of when the message was created.
     * 
     * @return The timestamp of the message.
     */
    public Date getTimestamp() {
        return timestamp;
    }

    /**
     * Returns a string representation of the {@code Message} object.
     * The string includes the message content and the timestamp.
     * 
     * @return A string representation of the message.
     */
    @Override
    public String toString() {
        return "Message{" +
                "content='" + content + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
