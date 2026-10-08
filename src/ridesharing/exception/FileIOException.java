package ridesharing.exception;

public class FileIOException extends RuntimeException {
  private final String rawMessage;

  public FileIOException(String message) {
    super("file I/O error: " + message);
    this.rawMessage = message;
  }

  public String getRawMessage() {
    return rawMessage;
  }
}
