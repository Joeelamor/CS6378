import conn.Message;
import conn.Sender;

import java.util.Random;

public class Application{
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

    public void start() {
        Random random = new Random();
        int i = 0;
        while (i < reqNum) {
            int generateTime = random.nextInt(interReqDelay);
            try {
                Thread.sleep(generateTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(node.getNodeId() + "is trying to enter critical section");
            //write
            node.csEnter();
            //write enter cs
            int execTime = random.nextInt(csExecTime);
            try {
                System.out.println(node.getNodeId() + "is in critical section");
                Thread.sleep(execTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            node.csLeave();
            System.out.println(node.getNodeId() + "quits critical section");
            i++;
        }
    }
}
