

import java.util.Date;

public class Message {
    private String content;  // Encrypted content
    private Date timestamp;  // Timestamp of the message
    private String senderId; // Sender's User ID
    private String receiverId; // Receiver's User ID

    // Constructor
    public Message(String content, String senderId, String receiverId) {
        this.content = content;
        this.timestamp = new Date();
        this.senderId = senderId;
        this.receiverId = receiverId;
    }

    // Getters
    public String getContent() {
        return content;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    @Override
    public String toString() {
        return "Message{" +
                "content='" + content + '\'' +
                ", timestamp=" + timestamp +
                ", senderId='" + senderId + '\'' +
                ", receiverId='" + receiverId + '\'' +
                '}';
    }
}
