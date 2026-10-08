package ridesharing.logger;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import ridesharing.exception.FileIOException;

public class Logger implements AutoCloseable {
  private final Object lock = new Object();
  private PrintWriter file;

  public Logger() {}

  public Logger(String path) {
    try {
      this.file = new PrintWriter(new FileWriter(path, true));
    } catch (IOException e) {
      throw new FileIOException(e.getMessage());
    }
  }

  @Override
  public void close() {
    synchronized (lock) {
      if (file == null) {
        return;
      }
      file.close();
      if (file.checkError()) {
        file = null;
        throw new FileIOException("failed to close log file");
      }
      file = null;
    }
  }

  private void log(String level, String message) {
    synchronized (lock) {
      String line =
          OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
              + " ["
              + level
              + "] "
              + message;
      System.out.println(line);
      if (file != null) {
        file.println(line);
        file.flush();
      }
    }
  }

  public void info(String message) {
    log("INFO", message);
  }

  public void warn(String message) {
    log("WARN", message);
  }

  public void error(String message) {
    log("ERROR", message);
  }

  public void logWorkerStart(int workerID) {
    info("worker " + workerID + " started");
  }

  public void logWorkerComplete(int workerID) {
    info("worker " + workerID + " completed");
  }

  public void logTaskStart(int workerID, int taskID) {
    info("worker " + workerID + " started task " + taskID);
  }

  public void logTaskComplete(int workerID, int taskID) {
    info("worker " + workerID + " completed task " + taskID);
  }

  public void logTaskError(int workerID, int taskID, String message) {
    error("worker " + workerID + " task " + taskID + " failed: " + message);
  }

  public void logException(int workerID, Throwable err) {
    error("worker " + workerID + ": " + err);
  }
}
