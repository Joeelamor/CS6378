import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.*;

public class Validator {
  class CriticalSectionEvent implements Comparable<CriticalSectionEvent> {
    long reqTS;
    long enterTS;
    long exitTS;
    int id;

    public CriticalSectionEvent(String reqTS, String enterTS, String exitTS,
                                String id) {
      this.reqTS = Long.parseLong(reqTS);
      this.enterTS = Long.parseLong(enterTS);
      this.exitTS = Long.parseLong(exitTS);
      this.id = Integer.parseInt(id);
    }


    @Override
    public int compareTo(CriticalSectionEvent o) {
      return Long.compare(this.exitTS, o.enterTS);
    }
  }

  List<CriticalSectionEvent> cs;
  Set<String> nodes;

  public Validator() {
    nodes = new HashSet<>();
  }

  public boolean load(String resultFile) throws IOException {
    List<String> result = Files.readAllLines(Paths.get(resultFile));
    if (result.size() % 3 != 0) {
      return false;
    }

    List<CriticalSectionEvent> criticalSectionEvents = new ArrayList<>();

    for (int i = 0; i < result.size(); i += 3) {
      String[] req = result.get(i).trim().split("\\s+");
      String[] enter = result.get(i + 1).trim().split("\\s+");
      String[] quit = result.get(i + 2).trim().split("\\s+");
      if (!req[0].equals("request") || !enter[0].equals("enter") || !quit[0].equals("quit"))
        return false;
      if (!(req[2].equals(enter[2]) && enter[2].equals(quit[2])))
        return false;
      criticalSectionEvents.add(new CriticalSectionEvent(req[3], enter[3],
        quit[3], req[2]));
      nodes.add(req[2]);
    }

    cs = criticalSectionEvents;

    return true;
  }

  long totalCS() {
    return cs.size();
  }

  long startTime() {
    return cs.get(0).reqTS;
  }

  long endTime() {
    return cs.get(cs.size() - 1).exitTS;
  }

  public boolean isSorted() {
    Iterator<CriticalSectionEvent> itr = cs.iterator();
    if (!itr.hasNext())
      return true;
    CriticalSectionEvent pred = itr.next(), curr;
    while (itr.hasNext()) {
      curr = itr.next();
      if (pred.compareTo(curr) > 0)
        return false;
      pred = curr;
    }
    return true;
  }

  public double calculateAverageRequestResponseTime() {
    return cs.stream().mapToLong(e -> e.enterTS - e.reqTS).average().getAsDouble();
  }

  public double calculateAverageSynchronizationDelay() {
    Iterator<CriticalSectionEvent> itr = cs.iterator();
    if (!itr.hasNext())
      return 0;
    List<Long> delays = new ArrayList<>();
    CriticalSectionEvent pred = itr.next(), curr;
    while (itr.hasNext()) {
      curr = itr.next();
      delays.add(curr.enterTS - pred.exitTS);
      pred = curr;
    }
    return delays.stream().mapToDouble(a -> a).average().getAsDouble();
  }

  public void dumpResult(String filename, String averageFile, int totalMessagesCount) throws IOException {
    SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    String now = timeFormat.format(new Date().getTime());
    File f = new File(filename);
    if (!f.exists())
      f.createNewFile();
    else {
      Files.write(f.toPath(), new byte[0], StandardOpenOption.TRUNCATE_EXISTING);
      System.out.println("Result file already exist, truncate with current result.");
    }
    append(filename, String.format("============ Result for %s ============", now));
    append(filename, String.format("Experiment %s", isSorted() ? "finished without error." : "failed."));
    append(filename, String.format("Total number of node: %d", nodes.size()));
    append(filename, String.format("Total number of critical section: %d", totalCS()));
    append(filename, String.format("Experiment start timestamp: %s", timeFormat.format(startTime())));
    append(filename, String.format("Experiment end timestamp:   %s", timeFormat.format(endTime())));
    append(filename, String.format("Average request response time: %.2f ms", calculateAverageRequestResponseTime()));
    append(filename, String.format("Average synchronization delay: %.2f ms", calculateAverageSynchronizationDelay()));
    append(filename, String.format("Average Request throughput: %.2f per minute", ((double) totalCS() * 1000 * 60) / (endTime() - startTime())));
    append(filename, String.format("Average Message complexity: %.2f per critical section", (double) totalMessagesCount / totalCS()));

    File af = new File(averageFile);
    if (!af.exists())
      af.createNewFile();
    append(averageFile, String.format("%.2f, %.2f, %.2f, %.2f",
      (double) totalMessagesCount / totalCS(),
      calculateAverageRequestResponseTime(),
      (double) totalCS() * 1000 * 60 / (endTime() - startTime()),
      calculateAverageSynchronizationDelay()));
  }

  void append(String file, String line) throws IOException {
    if (!line.endsWith("\n"))
      line = line + "\n";
    Files.write(Paths.get(file), line.getBytes(), StandardOpenOption.APPEND);
  }
}
