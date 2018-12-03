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

  public void start(String filename) throws IOException, InterruptedException {
    int i = 0;

    while (i < reqNum) {
      int generateTime = (int) expo(interReqDelay);
      try {
        Thread.sleep(generateTime);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      System.out.println(node.getNodeId() + " trys to enter critical section");
      long reqTime = new Date().getTime();
      node.csEnter();
      long enterTime = new Date().getTime();
      appendFile(filename, 0, node.getNodeId(), reqTime);
      appendFile(filename, 1, node.getNodeId(), enterTime);

      int execTime = (int) expo(csExecTime);
      System.out.println(node.getNodeId() + " enter critical section");
      Thread.sleep(execTime);

      long quitTime = new Date().getTime();
      appendFile(filename, -1, node.getNodeId(), quitTime);
      node.csLeave();
      System.out.println(node.getNodeId() + " quits critical section");
      i++;
    }
    node.end();
  }

  static double expo(int lambda) {
    return -lambda * Math.log(Math.random());
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

    String record = String.format("%s%s-%d-%d-%d-%d.txt",
      System.getProperty("user.home"), "/launch/record",
      totalNumber, interReqDelay, csExecTime, reqNum);
    File f = new File(record);
    if (!f.exists()) {
      f.createNewFile();
    } else {
      Files.write(f.toPath(), new byte[0], StandardOpenOption.TRUNCATE_EXISTING);
    }

    Node node = new Node(connectionList, nodeId, port, totalNumber);
    node.init();
    new Thread(node).start();
    Application application = new Application(node, interReqDelay, csExecTime, reqNum);

    try {
      application.start(record);
    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
    }

    // Test mechanism
    if (nodeId == 0) {
      String result = String.format("%s%s-%d-%d-%d-%d.txt",
        System.getProperty("user.home"), "/launch/result",
        totalNumber, interReqDelay, csExecTime, reqNum);
      String average = String.format("%s%s-%d-%d-%d-%d-average.txt",
        System.getProperty("user.home"), "/launch/result",
        totalNumber, interReqDelay, csExecTime, reqNum);

      Validator v = new Validator();
      v.load(record);
      v.dumpResult(result, average, node.getTotalMessagesCount());
    }
  }
}


