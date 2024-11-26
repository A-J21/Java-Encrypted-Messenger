package Java-Project;

import java.util.Date;

public class Message {
    private String content;
    private Date timestamp;
    private String senderId;
    private String receiverId;

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
