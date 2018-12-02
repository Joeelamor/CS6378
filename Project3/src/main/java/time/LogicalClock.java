package time;

import java.util.Comparator;

public interface LogicalClock<FIRST, SECOND> {

  Comparator<? extends FIRST> firstComparator();

  Comparator<? extends SECOND> secondComparator();

  FIRST incrementAndGet();

  FIRST compareIncrementAndGet(FIRST receivedTime);

  FIRST get();


}
