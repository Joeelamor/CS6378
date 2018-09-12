import conn.Conn;
import conn.Message;
import parser.InvalidNodeNumberFormatException;
import parser.Parser;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public class Node {
    private Map<Integer, String[]> connectionList;
    private int nodeId;
    private int port;
    private int totalNumber;
    private Conn conn;
    private Map<Integer, Integer> khop;

    public Node(Map<Integer, String[]> connectionList, int nodeId, int port, int totalNumber) {
        this.connectionList = connectionList;
        this.nodeId = nodeId;
        this.port = port;
        this.totalNumber = totalNumber;
        this.khop = new HashMap<>();
        for (int i = 0; i < this.totalNumber; i++) {
            if (i == nodeId)
                khop.put(i, 0);
            else
                khop.put(i, -1);
        }
    }

    public void init() {
        this.conn = new Conn(this.nodeId, this.port);
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

    public void start() {
        new Thread(() -> {
            while (true) {
                conn.broadcast(new Message(nodeId, khop));
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        while (true) {
            Message message = conn.getMessage();
            Map<Integer, Integer> neighbor = message.getNeighbors();
            for (Map.Entry<Integer, Integer> entry : neighbor.entrySet()) {
                int key = entry.getKey();
                int value = entry.getValue();
                if (value == -1)
                    continue;
                System.out.println("received:");
                int tmp = value + 1;
                if (tmp < khop.get(key)) {
                    khop.put(key, tmp);
                }
                System.out.println("" + tmp + " " + khop.get(key));
            }
        }

    }

    public static void main(String[] args) throws FileNotFoundException, InvalidNodeNumberFormatException {
        Parser parser = new Parser(args[0]);
        String hostName = "";
        try {
            hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        parser.parseFile(hostName);
        Map<Integer, String[]> connectionList = parser.getConnectionList();
        int nodeId = parser.getNodeId();
        int port = parser.getPort();
        int totalNumber = parser.getTotalNumber();
        Node node = new Node(connectionList, nodeId, port, totalNumber);
        node.init();
        node.start();
    }
}
