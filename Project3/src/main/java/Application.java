import parser.Parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Scanner;

public class Application {
    private int interReqDelay;
    private int csExecTime;
    private int reqNum;
    private Node node;

    public Application(Node node, int interReqDelay, int csExecTime, int reqNum) {
        this.node = node;
        this.interReqDelay = interReqDelay;
        this.csExecTime = csExecTime;
        this.reqNum = reqNum;
    }

    public void start() throws IOException {
        int i = 0;
        String filename = System.getProperty("user.home") + "/launch/record.txt";
        while (i < reqNum) {
            File f = new File(filename);
            if (!f.exists()) {
                f.createNewFile();
            }
            int generateTime = (int)expo(interReqDelay);
            try {
                Thread.sleep(generateTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(node.getNodeId() + "is trying to enter critical section");
            long reqTime = new Date().getTime();
            node.csEnter();
            long enterTime = new Date().getTime();
            appendFile(filename, 0, node.getNodeId(), reqTime);
            appendFile(filename, 1, node.getNodeId(), enterTime);

            int execTime = (int)expo(csExecTime);
            try {
                System.out.println(node.getNodeId() + "is in critical section");
                Thread.sleep(execTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            long quitTime = new Date().getTime();
            appendFile(filename, -1, node.getNodeId(), quitTime);
            node.csLeave();
            System.out.println(node.getNodeId() + "quits critical section");
            i++;
        }
        node.end();
    }

    double expo(int lambda) {
        double x, z;
        z = Math.random();
        x = - (1 / (double)lambda) * Math.log(z);
        return x;
    }

    void appendFile(String filename, int action, int id, long date) throws IOException {
        String actionStr = action == 0 ? "request" : action == 1 ? "enter  " : "quit   ";
        String content = String.format("%s node %2d %d %s\n",
                actionStr, id, date, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(date));
        Files.write(
                Paths.get(filename),
                content.getBytes(),
                StandardOpenOption.APPEND);
    }

    private static boolean test() throws FileNotFoundException {
        File file = new File(System.getProperty("user.home") + "/launch/record.txt");
        Scanner sc = new Scanner(file);
        int i = 0;
        String node = "";
        long reqTime = Long.MIN_VALUE, enterTime = Long.MIN_VALUE, quitTime = Long.MIN_VALUE;
        while (sc.hasNextLine()) {
            String s = sc.nextLine();
            String[] str = s.trim().split("\\s+");
            if (i % 3 == 0) {
                if (!str[0].equals("request")) { //first one should be request
                    return false;
                }
                node = str[2]; // initial node with request node
                reqTime = Long.parseLong(str[3]);
            } else if (i % 3 == 1) {
                if (!str[0].equals("enter")) { // second one should be enter
                    return false;
                }
                if (!str[2].equals(node)) // if enter node is same node
                    return false;
                enterTime = Long.parseLong(str[3]);
                if (enterTime < reqTime) // if enter time is larger than request time for same node
                    return false;
                if (enterTime < quitTime) // for first entering node
                    return false;
            } else {
                if (!str[0].equals("quit")) { // third one should be quit
                    return false;
                }
                if (!str[2].equals(node)) // if quit node is same node
                    return false;
                quitTime =  Long.parseLong(str[3]);
                if (quitTime < enterTime) // if quit time is larger than enter time for same node
                    return false;
                node = "";
            }
            i++;
        }
        return true;
    }

    public static void main(String[] args) throws IOException {

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
        new Thread(node).start();
        Application application = new Application(node, interReqDelay, csExecTime, reqNum);
        try {
            application.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Test mechanism
        if (nodeId == 0) {
            String filename = System.getProperty("user.home") + "/launch/result.txt";
            File f = new File(filename);
            f.createNewFile();
            String content = test() ? "Implementation is correct!\n" : "Implementation is wrong!\n";
            Files.write(
                    Paths.get(filename),
                    content.getBytes(),
                    StandardOpenOption.APPEND);
        }
    }
}
