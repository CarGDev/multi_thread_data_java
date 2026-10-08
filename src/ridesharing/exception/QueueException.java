package ridesharing.exception;

public class QueueException extends RuntimeException {
  private final String rawMessage;

  public QueueException(String message) {
    super("queue error: " + message);
    this.rawMessage = message;
  }

  public String getRawMessage() {
    return rawMessage;
  }
}
