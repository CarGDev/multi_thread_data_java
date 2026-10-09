package ridesharing.task;

import java.time.Instant;

public class BaseTask {
  protected int taskID;
  protected TaskStatus status;
  protected Instant createdAt;
  private final Object lock = new Object();

  public BaseTask() {}

  protected void init(int taskID) {
    synchronized (lock) {
      this.taskID = taskID;
      this.createdAt = Instant.now();
    }
  }

  public int getID() {
    return taskID;
  }

  public TaskStatus getStatus() {
    return status;
  }

  protected void setStatus(TaskStatus status) {
    synchronized (lock) {
      this.status = status;
    }
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
