package algorithm;

import time.ScalarClock;

public class RoucairolCarvalho {
  private enum State {
    IDLE, PENDING, BUSY
  }

  public enum Operation {
    REPLY, DEFER, NOP, EXEC, SEND_DEFER, REPLY_W_REQ
  }

  private State state;
  private int receivedReplies;
  private int nodeNum;
  private ScalarClock latestRequestTimestamp;
  private boolean[] keys;
  private int nodeId;

  public boolean[] getKeys() {
    return keys;
  }

  public RoucairolCarvalho(int nodeNum, int nodeId) {
    this.state = State.IDLE;
    this.receivedReplies = 0;
    this.nodeNum = nodeNum;
    this.latestRequestTimestamp = null;
    this.nodeId = nodeId;
    this.keys = new boolean[this.nodeNum];
    for (int i = 0; i < this.nodeNum; i++) {
      if (i >= this.nodeId) {
        this.keys[i] = true;
        this.receivedReplies++;
      }
    }
  }

  public Operation receiveRequest(int from, int timestamp) {
    ScalarClock messageTime = new ScalarClock(from, timestamp);
    if (state == State.IDLE)
      return Operation.REPLY;
    if (state == State.PENDING && latestRequestTimestamp.compareTo(messageTime) > 0)
      return Operation.REPLY_W_REQ;
    return Operation.DEFER;
  }

  public Operation createRequest(ScalarClock requestTimeStamp) throws Exception {
    if (state != State.IDLE)
      throw new Exception("This state must be idle");
    latestRequestTimestamp = requestTimeStamp;
    if (receivedReplies == nodeNum) {
      state = State.BUSY;
      return Operation.EXEC;
    }
    state = State.PENDING;
    return Operation.NOP;
  }

  public Operation receiveReply(int sendId) throws Exception {
    if (state != State.PENDING)
      throw new Exception("This state must be pending");
    receivedReplies += 1;
    keys[sendId] = true;
    if (receivedReplies == nodeNum) {
      state = State.BUSY;
      return Operation.EXEC;
    }
    return Operation.NOP;
  }

  public Operation exitCriticalSection() throws Exception {
    if (state != State.BUSY)
      throw new Exception("This state must be busy");
    state = State.IDLE;
    return Operation.SEND_DEFER;
  }

  public void sendReply(int target) {
    receivedReplies--;
    keys[target] = false;
  }
}
