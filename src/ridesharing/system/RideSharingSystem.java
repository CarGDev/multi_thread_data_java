package ridesharing.system;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import ridesharing.exception.FileIOException;
import ridesharing.logger.Logger;
import ridesharing.queue.TaskQueue;
import ridesharing.result.Result;
import ridesharing.result.ResultStore;
import ridesharing.task.Task;
import ridesharing.worker.Worker;

public class RideSharingSystem {
  private static final int QUEUE_CAPACITY = 100;
  private static final String LOG_FILE_PATH = "ridesharing.log";

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
    this.taskQueue = new TaskQueue(QUEUE_CAPACITY);
    this.resultStore = new ResultStore();
  }

  public static RideSharingSystem initialize(int workerCount) {
    Logger log;
    try {
      log = new Logger(LOG_FILE_PATH);
    } catch (FileIOException e) {
      log = new Logger();
      log.warn("file logging disabled: " + e.getMessage());
    }

    RideSharingSystem s = new RideSharingSystem(log);
    for (int i = 1; i <= workerCount; i++) {
      s.workers.add(new Worker(i, s.taskQueue, s.resultStore, s.logger));
    }
    return s;
  }

  public void submitTask(Task t) throws InterruptedException {
    try {
      taskQueue.enqueue(t);
    } catch (RuntimeException | InterruptedException e) {
      logger.error(e.getMessage());
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
    startWorkers();
    try {
      submitTasks(tasks);
    } catch (InterruptedException e) {
        throw new RuntimeException(e);
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

  /** Releases the log file; call it after awaitCompletion. */
  public void close() {
    logger.close();
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
    } catch (RuntimeException e) {
      logger.error(e.getMessage());
      throw e;
    }
  }
}
