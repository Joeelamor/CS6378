package time;

public class ScalarClock implements Comparable<ScalarClock> {
    private Integer id;
    private Integer time;

    public ScalarClock(int id, Integer time) {
        this.id = id;
        this.time = time;
    }

    public synchronized Integer incrementAndGet() {
        time += 1;
        return time;
    }

    public synchronized Integer compareIncrementAndGet(Integer receivedTime) {
        int maxTime = Math.max(receivedTime, time);
        time = maxTime + 1;
        return time;
    }

    public Integer get() {
        return time;
    }

    @Override
    public int compareTo(ScalarClock o) {
        int ret = this.time.compareTo(o.get());
        if (ret != 0) {
            return ret;
        }
        return this.id.compareTo(o.id);
    }
}
