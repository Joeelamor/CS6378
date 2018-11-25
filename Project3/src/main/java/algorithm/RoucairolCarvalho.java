package algorithm;


import time.ScalarClock;

public class RoucairolCarvalho {
    public enum State {
        IDLE, PENDING, BUSY
    }

    public enum Operation {
        REPLY, DEFER, NOP, EXEC, SEND_DEFER;
    }

    private State state;
    private int receivedReplies;
    private int nodeNum;
    private ScalarClock latestRequestTimestamp;

    public RoucairolCarvalho(int nodeNum) {
        this.state = State.IDLE;
        this.receivedReplies = 0;
        this.nodeNum = nodeNum;
        this.latestRequestTimestamp = null;
    }

    public Operation receiveRequest(int from, int timestamp) {
        ScalarClock messageTime = new ScalarClock(from, timestamp);
        if (state == State.IDLE || (state == State.PENDING && latestRequestTimestamp.compareTo(messageTime) > 0)) {
            return Operation.REPLY;
        }
        return Operation.DEFER;
    }

    public Operation createRequest(ScalarClock requestTimeStamp) throws Exception {
        if (state != State.IDLE)
            throw new Exception("This state must be idle");
        latestRequestTimestamp = requestTimeStamp;
        state = State.PENDING;
        return Operation.NOP;
    }

    public Operation receiveReply() throws Exception {
        if (state != State.PENDING)
            throw new Exception("This state must be pending");
        receivedReplies += 1;
        if (receivedReplies == nodeNum - 1) {
            state = State.BUSY;
            receivedReplies = 0;
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
}
