import java.util.Date;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.testng.Assert;
import org.testng.annotations.Test;

public class ConcurrentTest {

  // nested types
  class TaskException extends RuntimeException {

    int taskId;



    TaskException(int value) {
      this.taskId = value;
    }

  }

  @Test
  public void test_submit_await_and_submit() throws InterruptedException, ExecutionException {

    final ExecutorService pool = Executors.newFixedThreadPool(2);
    final ExecutorCompletionService<Integer> completionService = new ExecutorCompletionService(pool);

    // schedule work
    for (int i = 0; i < 2; i++) {

      completionService.submit(getRepeatableTask(i));
      Thread.sleep(1000);
    }

    // re-schedule work for ever
    while (true) {

      Future<Integer> task;
      Integer taskId = null;
      try {

        // blocks until the first task is completed
        task = completionService.take();
        taskId = task.get();

      } catch (Throwable e) {

        if (e.getCause() instanceof TaskException){
          taskId = ((TaskException)e.getCause()).taskId;
        } else {
          taskId = 9999;
        }
        System.err.println(String.format("Task %s blow up - %s", taskId, e));
      } finally {

        // task finished working.. need to re-schedule it
        System.out.println(String.format("Task %s is going to be re-scheduled", taskId));

        // re-schedules
        completionService.submit(getRepeatableTask(taskId));
      }
    }
  }

  @Test(enabled = false)
  public void test_concurrent_1() throws Exception {

    String[] values = {"1", "2"};

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

  @Test(enabled = false)
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

  // helpers
  private Callable<Integer> getRepeatableTask(int taskId) {
    return () -> {

      float r = new Random().nextFloat();
      long restTime = (long) (r * 10000);

      // want to blow up x% of the times+
      if (r <= 0.10f) {

        throw new TaskException(taskId);
      }

      System.out.println(String.format("Task %s doing its work by sleeping %s", taskId, restTime));
      Thread.sleep(restTime);

      return taskId;
    };
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
