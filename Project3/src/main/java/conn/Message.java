package conn;

import java.io.Serializable;

public class Message implements Serializable {
    public enum Type {
        REQ, RPY, INI, FINISH, END
    }

    private int senderId;
    private Type type;
    private Integer timestamp;


    public Message(int senderId, Type type, Integer timestamp) {
        this.senderId = senderId;
        this.type = type;
        this.timestamp = timestamp;
    }

    public int getSenderId() {
        return senderId;
    }

    public Type getType() {
        return type;
    }

    public int getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "Conn.Message{" +
                "senderId=" + senderId +
                ", type=" + type +
                ", timestamp=" + timestamp +
                '}';
    }
}
