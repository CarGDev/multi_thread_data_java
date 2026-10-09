package ridesharing.queue;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import ridesharing.exception.QueueException;
import ridesharing.task.Task;

public class TaskQueue {
  private final BlockingQueue<Task> tasks;
  private boolean closed;
  private final Object lock = new Object();

  public TaskQueue(int capacity) {
    this.tasks = new ArrayBlockingQueue<>(capacity);
  }

  public void enqueue(Task t) throws QueueException, InterruptedException {
    synchronized (lock) {
      if (this.closed) {
        throw new QueueException("queue is closed");
      }
      tasks.put(t);
    }
  }

  public Task dequeue() throws QueueException, InterruptedException {
    while (true) {
      Task t = tasks.poll(100, TimeUnit.MILLISECONDS);
      if (t != null) {
        return t;
      }
      if (closed && tasks.isEmpty()) {
        throw new QueueException("queue is closed and empty");
      }
    }
  }

  public Task tryDequeue() {
    return tasks.poll();
  }

  public int size() {
    return tasks.size();
  }

  public boolean isEmpty() {
    return tasks.isEmpty();
  }

  public void close() {
    synchronized (lock) {
      this.closed = true;
    }
  }

  public boolean isClosed() {
    return closed;
  }
}
