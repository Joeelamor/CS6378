package conn;

import java.io.Serializable;

public class Message implements Serializable {
  public enum Type {
    REQ, RPY, INI, FINISH, END
  }

  private int senderId;
  private Type type;
  private Integer timestamp;
  private Integer data;

  public Message(int senderId, Type type, Integer timestamp, Integer data) {
    this.senderId = senderId;
    this.type = type;
    this.timestamp = timestamp;
    this.data = data;
  }

  public Message(int senderId, Type type, Integer timestamp) {
    this(senderId, type, timestamp, 0);
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

  public Integer getData() {
    return data;
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
