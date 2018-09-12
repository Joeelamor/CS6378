package parser;

import java.io.*;
import java.util.*;
import java.net.*;

public class Parser {
    private String path;
    private int totalNumber;
    private int nodeId;
    private int port;
    private Map<Integer, String[]> connectionList;

    public Parser(String path) {
        this.totalNumber = 0;
        this.nodeId = Integer.MIN_VALUE;
        this.path = path;
        this.connectionList = new HashMap<>();
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

    public void parseFile(String Hostname) throws FileNotFoundException, InvalidNodeNumberFormatException {

        Map<Integer, String[]> serverList = new HashMap<>();
        Map<Integer, List<Integer>> connectionList = new HashMap<>();

        File file = new File(path);
        Scanner sc = new Scanner(file);

        List<String> parsed = new ArrayList<>();

        while (sc.hasNextLine()) {
            char[] line = sc.nextLine().trim().toCharArray();
            if (line.length == 0 || line[0] < '0' || line[0] > '9') {
                continue;
            } else {
                StringBuffer temp = new StringBuffer();
                for (int i = 0; i < line.length; i++) {
                    if (line[i] == '#')
                        break;
                    else
                        temp.append(line[i]);
                }
                if (temp.length() > 0) parsed.add(temp.toString());
            }
        }

        try {
            totalNumber = Integer.parseInt(parsed.get(0).trim());
        } catch (NumberFormatException nef) {
            throw new InvalidNodeNumberFormatException(nef.getMessage());
        }


        for (int i = 1; i < totalNumber + 1; i++) {
            String line = parsed.get(i).trim();
            String[] server = line.split("\\s+");
            if (server.length != 3) {
                System.out.println("The format of node address and port is not correct!");
                System.exit(-1);
            }

            int id = Integer.parseInt(server[0].trim());
            String[] connector = {server[1], server[2]};
            serverList.put(id, connector);
        }

        for (Map.Entry<Integer, String[]> entry : serverList.entrySet()) {
            int identifier = entry.getKey();
            String[] address = entry.getValue();
            if (address[0].equals(Hostname)) {
                nodeId = identifier;
                port = Integer.parseInt(address[1]);
                break;
            }
        }

        if (nodeId == Integer.MIN_VALUE) {
            System.out.println("This machine is not in the configuration file!");
            System.exit(-1);
        }

        for (int i = totalNumber + 1; i < parsed.size(); i++) {
            String line = parsed.get(i).trim();
            String[] linked = line.split("\\s+");
            int id = Integer.parseInt(linked[0]);

            List<Integer> temp = new ArrayList<>();
            for (int j = 1; j < linked.length; j++) {
                temp.add(Integer.parseInt(linked[j]));
            }

            connectionList.put(id, temp);
        }

        for (int id : connectionList.get(nodeId)) {
            this.connectionList.put(id, serverList.get(id));
        }
    }

    public static void main(String[] args) throws UnknownHostException {
        Parser test = new Parser("config.txt");
        String Hostname = InetAddress.getLocalHost().getHostName();
        try {
            test.parseFile(Hostname);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (InvalidNodeNumberFormatException e) {
            System.out.println("The format of totalNumber of nodes is not correct!" + e.getMessage());
        }

    }
}