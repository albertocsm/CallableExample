import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.testng.Assert;
import org.testng.annotations.Test;

public class ConcurrentTest {

  @Test
  public void test_concurrent_1() throws Exception {

    String[] values = { "1", "2" };

    ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    Set<Future<Integer>> set = new HashSet<Future<Integer>>();

    for (String word : values) {
      Callable<Integer> callable = new CallableExample(word);
      Future<Integer> future = pool.submit(callable);
      set.add(future);
    }
    int sum = 0;
    for (Future<Integer> future : set) {
      sum += future.get();
    }
    System.out.printf("The sum of lengths is %s%n", sum);

    Assert.assertEquals(sum, 2);
  }

  @Test
  public void test_concurrent_2() throws Exception {
    Date startTime = new Date();

    ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    Future<Integer> r1 = pool.submit(getOneCallable());
    Future<Integer> r2 = pool.submit(getTwoCallable());
    pool.execute(getNoValueRunnable());
    pool.shutdown();

    Integer result = r1.get() + r2.get();

    Assert.assertEquals(result.intValue(), 3);

    Date endTime = new Date();
    long time = (endTime.getTime() - startTime.getTime());
    System.out.println("Elapsed time is " + time);
  }

  private Callable<Integer> getOneCallable() {

    return new Callable<Integer>() {
      public Integer call() throws Exception {
        Thread.sleep(TimeUnit.SECONDS.toMillis(1));
        System.out.println("getOneCallable");
        return 1;
      }
    };
  }

  private Callable<Integer> getTwoCallable() {

    return new Callable<Integer>() {
      public Integer call() throws Exception {
        Thread.sleep(TimeUnit.SECONDS.toMillis(1));
        System.out.println("getTwoCallable");
        return 2;
      }
    };
  }

  private Runnable getNoValueRunnable() {

    return new Runnable() {
      @Override
      public void run() {
        try {
          Thread.sleep(TimeUnit.SECONDS.toMillis(10));
          System.out.println("getNoValueRunnable");
        } catch (Throwable e) {
          System.out.println(e.getMessage());
        }
      }
    };

  }

  /*
   * @Test public void test_concurrent_3() throws Exception {
   * 
   * Date startTime = new Date(); FutureTask<Integer> r1 = getOneFutureTask();
   * FutureTask<Integer> r2 = getTwoFutureTask();
   * 
   * Integer result = r1.get() + r2.get(); Date endTime = new Date(); long time
   * = (endTime.getTime() - startTime.getTime()) / 1000L;
   * 
   * System.out.printf("Elapsed time is %s%n", time);
   * Assert.assertEquals(result.intValue(), 3);
   * 
   * }
   * 
   * private FutureTask<Integer> getOneFutureTask() {
   * 
   * return new FutureTask<Integer>(new Callable<Integer>() { public Integer
   * call() throws Exception { Thread.sleep(TimeUnit.SECONDS.toMillis(1));
   * return 1; } }); }
   * 
   * private FutureTask<Integer> getTwoFutureTask() {
   * 
   * return new FutureTask<Integer>(new Callable<Integer>() { public Integer
   * call() throws Exception { Thread.sleep(TimeUnit.SECONDS.toMillis(1));
   * return 2; } }); }
   */
}
