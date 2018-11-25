import conn.Message;
import conn.RequestGenerator;
import algorithm.RoucairolCarvalho;
import parser.Parser;
import time.ScalarClock;
import conn.Conn;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Map;

public class Node {
    private ScalarClock time;
    private Map<Integer, String[]> connectionList;
    private int nodeId;
    private int port;
    private int totalNumber;
    private Conn conn;


    public Node(Map<Integer, String[]> connectionList, int nodeId, int port, int totalNumber) {
        this.connectionList = connectionList;
        this.time = new ScalarClock(nodeId, nodeId); // use id value as initial timestamp
        this.nodeId = nodeId;
        this.port = port;
        this.totalNumber = totalNumber;
    }

    private void init() {
        this.conn = new Conn(this.nodeId, this.port, this.time);
        for (Map.Entry<Integer, String[]> entry : connectionList.entrySet()) {
            try {
                if (nodeId < entry.getKey())
                    continue;
                conn.connect(entry.getKey(), entry.getValue()[0], Integer.parseInt(entry.getValue()[1]));
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Unable to connect to existing host");
            }
        }

    }

    private void start(RequestGenerator requestGenerator) {
        requestGenerator.setId(id);
        requestGenerator.setQueue(messageQueue);
        requestGenerator.setSenderMap(senderMap);
        requestGenerator.setTime(time);
        Thread requestGeneratorThread = new Thread(requestGenerator);
        requestGeneratorThread.start();

        RoucairolCarvalho ra = new RoucairolCarvalho(nodeNum);
        RoucairolCarvalho.Operation op;
        ArrayList<Integer> deferredReplies = new ArrayList<>(nodeNum - 1);
        while (true) {
            if (messageQueue.isEmpty()) {
                continue;
            }
            op = RoucairolCarvalho.Operation.NOP;
            Message message = conn.getMessage();
            try {
                switch (message.getType()) {
                    case REQ:
                        if (message.getSenderId() == id) {
                            op = ra.createRequest(new ScalarClock(id, message.getTimestamp()));
                        } else
                            op = ra.receiveRequest(message.getSenderId(), message.getTimestamp());
                        break;
                    case RPY:
                        op = ra.receiveReply();
                        break;
                    case FINISH:
                        op = ra.exitCriticalSection();
                        requestGenerator.notifyNewRequest();
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                switch (op) {
                    case REPLY:
                        this.send(message.getSenderId(), new Message(this.id, Message.Type.RPY, time.incrementAndGet()));
                        break;
                    case DEFER:
                        deferredReplies.add(message.getSenderId());
                        break;
                    case SEND_DEFER:
                        for (int target : deferredReplies) {
                            this.send(target, new Message(this.id, Message.Type.RPY, time.incrementAndGet()));
                        }
                        deferredReplies.clear();
                        break;
                    case EXEC:
                        executeCriticalSection();
                        break;
                }
            }
        }
    }


    private void executeCriticalSection() {
        try {
            System.out.println("!!!!!!ENTER CRITICAL SECTION!!!!!!");
            Thread.sleep(3000);
            messageQueue.offer(new Message(this.id, Message.Type.FINISH, time.incrementAndGet()));
            System.out.println("Exit critical section");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


    private void send(int id, Message message) {
        senderMap.get(id).queue.offer(message);
    }


    public static void main(String[] args) throws FileNotFoundException, InvalidNodeNumberFormatException {

        Parser parser = new Parser();
        String hostName = "";
        try {
            hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        parser.parseFile(args[0], hostName);
        Map<Integer, String[]> connectionList = parser.getConnectionList();
        int nodeId = parser.getNodeId();
        int port = parser.getPort();
        int totalNumber = parser.getTotalNumber();
        int interReqDelay = parser.getInterReqDelay();
        int csExecTime = parser.getCsExecTime();
        int reqNum = parser.getReqNum();
        Node node = new Node(connectionList, nodeId, port, totalNumber);
        node.init();
        RequestGenerator requestGenerator = new RequestGenerator(ReqGenTimeFloor, ReqGenTimeRange, ReqGenTimeUnit);
        node.start(requestGenerator);
    }
}
