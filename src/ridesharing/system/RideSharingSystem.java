package ridesharing.system;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import ridesharing.exception.FileIOException;
import ridesharing.exception.QueueException;
import ridesharing.logger.Logger;
import ridesharing.queue.TaskQueue;
import ridesharing.result.Result;
import ridesharing.result.ResultStore;
import ridesharing.task.Task;
import ridesharing.worker.Worker;

public class RideSharingSystem {
  private static final int QUEUE_CAPACITY = 100;

  public static class Stats {
    public int total;
    public int success;
    public int failed;
  }

  private final TaskQueue taskQueue;
  private final ResultStore resultStore;
  private final Logger logger;
  private final List<Worker> workers = new ArrayList<>();
  private final List<Thread> threads = new ArrayList<>();
  private boolean running;

  private RideSharingSystem(Logger logger) {
    this.logger = logger;
    this.taskQueue = new TaskQueue(QUEUE_CAPACITY, logger);
    this.resultStore = new ResultStore(logger);
  }

  public static RideSharingSystem initialize(int workerCount, Logger log) {
    RideSharingSystem s = new RideSharingSystem(log);
    for (int i = 1; i <= workerCount; i++) {
      s.workers.add(new Worker(i, s.taskQueue, s.resultStore, s.logger));
    }
    return s;
  }

  public void submitTask(Task t) throws InterruptedException {
    try {
      taskQueue.enqueue(t);
    } catch (QueueException e) {
      logger.error(e.getMessage());
      throw e;
    } catch (InterruptedException e) {
      logger.error("submit interrupted");
      throw e;
    }
  }

  /** Enqueues every task and throws on the first error, if any. */
  public void submitTasks(List<Task> tasks) throws InterruptedException {
    for (Task t : tasks) {
      submitTask(t);
    }
  }

  public Stats stats() {
    Stats s = new Stats();
    s.total = resultStore.count();
    s.success = resultStore.countSuccess();
    s.failed = resultStore.countFailed();
    return s;
  }

  /** Starts the workers, processes all tasks, waits for completion and writes the results. */
  public void run(List<Task> tasks, String outPath) {
    logger.info("starting " + workers.size() + " worker(s) for " + tasks.size() + " task(s)");
    startWorkers();
    try {
      submitTasks(tasks);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("interrupted while submitting tasks", e);
    } finally {
      shutdown();
      awaitCompletion();
    }
    writeResults(outPath);
  }

  public void startWorkers() {
    if (running) {
      return;
    }
    running = true;
    for (Worker w : workers) {
      threads.add(w.start());
    }
  }

  /** Closes the queue; workers finish the remaining tasks and exit. */
  public void shutdown() {
    taskQueue.close();
    running = false;
  }

  public void awaitCompletion() {
    for (Thread t : threads) {
      try {
        t.join();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        return;
      }
    }
  }

  public List<Result> getResults() {
    return resultStore.getResults();
  }

  public void writeResults(String path) {
    try {
      resultStore.writeToCSV(Path.of(path));
    } catch (FileIOException e) {
      logger.error(e.getMessage());
      throw e;
    }
  }
}
