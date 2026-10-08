package ridesharing.exception;

public class ProcessingException extends RuntimeException {
  private final String rawMessage;
  private final int taskID;

  public ProcessingException(int taskID, String message) {
    super("processing error (task " + taskID + "): " + message);
    this.rawMessage = message;
    this.taskID = taskID;
  }

  public String getRawMessage() {
    return rawMessage;
  }

  public int getTaskID() {
    return taskID;
  }
}
