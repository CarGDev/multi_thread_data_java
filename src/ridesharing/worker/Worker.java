package ridesharing.worker;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;
import ridesharing.exception.ProcessingException;
import ridesharing.logger.Logger;
import ridesharing.queue.TaskQueue;
import ridesharing.result.Result;
import ridesharing.result.ResultStore;
import ridesharing.task.Task;

public class Worker implements Runnable {
  private final int workerID;
  private final TaskQueue taskQueue;
  private final ResultStore resultStore;
  private final Logger logger;
  private final AtomicBoolean running = new AtomicBoolean(false);

  public Worker(int workerID, TaskQueue taskQueue, ResultStore resultStore, Logger logger) {
    this.workerID = workerID;
    this.taskQueue = taskQueue;
    this.resultStore = resultStore;
    this.logger = logger;
  }

  public Thread start() {
    running.set(true);
    Thread t = new Thread(this, "worker-" + workerID);
    t.start();
    return t;
  }

  @Override
  public void run() {
    logger.logWorkerStart(workerID);
    try {
      while (running.get()) {
        Task t;
        try {
          t = taskQueue.dequeue();
        } catch (InterruptedException err) {
          return;
        }
        processTask(t);
      }
    } finally {
      logger.logWorkerComplete(workerID);
    }
  }

  public void processTask(Task t) {
    logger.logTaskStart(workerID, t.getID());
    Instant startedAt = Instant.now();
    try {
      Result res = t.process();
      res.setWorker(workerID);
      res.setStartedAt(startedAt);
      resultStore.addResult(res);

      if (res.isSuccess()) {
        logger.logTaskComplete(workerID, t.getID());
        return;
      }
      logger.logTaskError(workerID, t.getID(), res.getMessage());

    } catch (RuntimeException e) {
      ProcessingException err = new ProcessingException(t.getID(), e.getMessage());
      logger.logException(workerID, err);

      Result res = new Result(t.getID(), false, err.getRawMessage());
      res.setWorker(workerID);
      res.setStartedAt(startedAt);
      resultStore.addResult(res);
    }
  }

  public void stop() {
    running.set(false);
  }

  public int getID() {
    return workerID;
  }

  public boolean isRunning() {
    return running.get();
  }
}
