package conn;

import time.ScalarClock;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Receiver implements Runnable {
    private ObjectInputStream inputStream;
    private ConcurrentLinkedQueue<Message> queue;
    private ScalarClock time;

    public Receiver(ObjectInputStream inputStream, ConcurrentLinkedQueue<Message> queue, ScalarClock time) {
        this.inputStream = inputStream;
        this.queue = queue;
        this.time = time;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Message message = (Message) this.inputStream.readObject();
                queue.offer(message);
                time.compareIncrementAndGet(message.getTimestamp());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
