package ridesharing.task;

import java.time.Instant;

public class BaseTask {
  protected int taskID;
  protected TaskStatus status = TaskStatus.PENDING;
  protected Instant createdAt;
  private final Object lock = new Object();

  public BaseTask() {}

  protected void init(int taskID) {
    synchronized (lock) {
      this.taskID = taskID;
      this.status = TaskStatus.PENDING;
      this.createdAt = Instant.now();
    }
  }

  public int getID() {
    return taskID;
  }

  public TaskStatus getStatus() {
    synchronized (lock) {
      return status;
    }
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
