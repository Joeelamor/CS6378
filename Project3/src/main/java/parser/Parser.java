package parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

public class Parser {
  private int totalNumber;
  private int nodeId;
  private int port;
  private int interReqDelay;
  private int csExecTime;
  private int reqNum;
  private Map<Integer, String[]> connectionList;

  public Parser() {
    this.totalNumber = 0;
    this.nodeId = Integer.MIN_VALUE;
    this.port = 0;
    this.interReqDelay = 0;
    this.csExecTime = 0;
    this.reqNum = 0;
    this.connectionList = new HashMap<>();
  }


  public int getInterReqDelay() {
    return interReqDelay;
  }

  public int getReqNum() {
    return reqNum;
  }

  public int getCsExecTime() {
    return csExecTime;
  }

  public int getTotalNumber() {
    return totalNumber;
  }

  public int getNodeId() {
    return this.nodeId;
  }

  public int getPort() {
    return this.port;
  }

  public Map<Integer, String[]> getConnectionList() {
    return connectionList;
  }

  @Override
  public String toString() {
    return "Parser{" +
      "totalNumber=" + totalNumber +
      ", nodeId=" + nodeId +
      ", port=" + port +
      ", interReqDelay=" + interReqDelay +
      ", csExecTime=" + csExecTime +
      ", reqNum=" + reqNum +
      ", connectionList=" + connectionList +
      '}';
  }

  public void parseFile(String path, String Hostname) throws FileNotFoundException {

    File file = new File(path);
    Scanner sc = new Scanner(file);

    String[] firstLine = sc.nextLine().split(" ");
    totalNumber = Integer.parseInt(firstLine[0]);
    interReqDelay = Integer.parseInt(firstLine[1]);
    csExecTime = Integer.parseInt(firstLine[2]);
    reqNum = Integer.parseInt(firstLine[3]);

    while (sc.hasNextLine()) {
      String s = sc.nextLine();
      if (s.length() != 0) {
        String[] str = s.trim().split(" ");

        String host = str[1] + ".utdallas.edu";
        if (host.equals(Hostname)) {
          nodeId = Integer.parseInt(str[0]);
          port = Integer.parseInt(str[2]);
        }
        connectionList.put(Integer.parseInt(str[0]), new String[]{host, str[2]});
      }
    }
  }

  public static void main(String[] args) throws UnknownHostException {
    Parser test = new Parser();
    String Hostname = InetAddress.getLocalHost().getHostName();
    try {
      test.parseFile("./config.txt", "dc03.utdallas.edu");
      System.out.println(test.toString());
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }
}