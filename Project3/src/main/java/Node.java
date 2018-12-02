import conn.Message;
import algorithm.RoucairolCarvalho;
import conn.Sender;
import time.ScalarClock;
import conn.Conn;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Node implements Runnable {
  private ScalarClock time;
  private Map<Integer, String[]> connectionList;
  private int nodeId;
  private int port;
  private int totalNumber;
  private Conn conn;
  private ConcurrentLinkedQueue<Message> messageQueue;
  private RoucairolCarvalho ra;

  final private Lock mutex = new ReentrantLock();
  final private Condition condition = mutex.newCondition();
  final private Semaphore sema = new Semaphore(0);

  final AtomicInteger outboundMessagesCount = new AtomicInteger();

  public int getNodeId() {
    return nodeId;
  }

  public Node(Map<Integer, String[]> connectionList, int nodeId, int port, int totalNumber) {
    this.connectionList = connectionList;
    this.time = new ScalarClock(nodeId, nodeId); // use id value as initial timestamp
    this.nodeId = nodeId;
    this.port = port;
    this.totalNumber = totalNumber;
    this.messageQueue = new ConcurrentLinkedQueue<>();
    this.ra = new RoucairolCarvalho(totalNumber, nodeId);
  }

  public void init() {
    this.conn = new Conn(this.nodeId, this.port, this.time, this.messageQueue, this.sema);
    for (Map.Entry<Integer, String[]> entry : connectionList.entrySet()) {
      try {
        if (nodeId <= entry.getKey())
          continue;
        conn.connect(entry.getKey(), entry.getValue()[0], Integer.parseInt(entry.getValue()[1]));
      } catch (IOException e) {
        e.printStackTrace();
        System.err.println("Unable to connect to existing host");
      }
    }

    sema.acquireUninterruptibly(totalNumber - 1);

  }

  @Override
  public void run() {
    RoucairolCarvalho.Operation op;
    ArrayList<Integer> deferredReplies = new ArrayList<>(totalNumber - 1);
    while (true) {
      op = RoucairolCarvalho.Operation.NOP;
      Message message = getMessage();
      try {
        switch (message.getType()) {
          case REQ:
            if (message.getSenderId() == nodeId) {
              broadcast(message);
              op = ra.createRequest(new ScalarClock(nodeId, message.getTimestamp()));
            } else
              op = ra.receiveRequest(message.getSenderId(), message.getTimestamp());
            break;
          case RPY:
            op = ra.receiveReply(message.getSenderId());
            break;
          case FINISH:
            op = ra.exitCriticalSection();
            break;
          case END:
            sema.release();
            outboundMessagesCount.addAndGet(message.getData());
            break;
        }
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        switch (op) {
          case REPLY:
            send(message.getSenderId(), new Message(nodeId, Message.Type.RPY, time.incrementAndGet()));
            ra.sendReply(message.getSenderId());
            break;
          case REPLY_W_REQ:
            send(message.getSenderId(), new Message(nodeId, Message.Type.REQ, time.incrementAndGet()));
            send(message.getSenderId(), new Message(nodeId, Message.Type.RPY, time.incrementAndGet()));
            ra.sendReply(message.getSenderId());
            break;
          case DEFER:
            deferredReplies.add(message.getSenderId());
            break;
          case SEND_DEFER:
            for (int target : deferredReplies) {
              send(target, new Message(nodeId, Message.Type.RPY, time.incrementAndGet()));
              ra.sendReply(target);
            }
            deferredReplies.clear();
            mutex.lock();
            try {
              condition.signalAll();
              break;
            } finally {
              mutex.unlock();
            }
          case EXEC:
            mutex.lock();
            try {
              condition.signalAll();
              break;
            } finally {
              mutex.unlock();
            }
        }
      }
    }
  }

  public void csEnter() {
    mutex.lock();
    try {
      Message reqMsg = new Message(nodeId, Message.Type.REQ, time.incrementAndGet());
      messageQueue.offer(reqMsg);
      condition.awaitUninterruptibly();
    } finally {
      mutex.unlock();
    }

  }

  public Message getMessage() {
    while (true) {
      if (messageQueue.isEmpty())
        continue;
      return messageQueue.poll();
    }

  }

  public void csLeave() {
    mutex.lock();
    try {
      messageQueue.offer(new Message(nodeId, Message.Type.FINISH, time.incrementAndGet()));
      condition.awaitUninterruptibly();
    } finally {
      mutex.unlock();
    }
  }

  public void end() {
    if (nodeId != 0) {
      send(0, new Message(nodeId, Message.Type.END, time.incrementAndGet(), outboundMessagesCount.get()));
    } else {
      sema.acquireUninterruptibly(totalNumber - 1);
    }
  }

  public int getTotalMessagesCount() {
    return outboundMessagesCount.get();
  }

  private void broadcast(Message message) {
    boolean[] keys = ra.getKeys();
    for (int i = 0; i < keys.length; i++) {
      if (!keys[i]) {
        send(i, message);
      }
    }
  }

  private void send(int id, Message message) {
    System.out.printf("%d send %s to %d\n", nodeId, message, id);
    outboundMessagesCount.getAndIncrement();
    conn.send(id, message);
  }
}
