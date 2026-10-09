package ridesharing.task;

public enum TaskStatus {
  PENDING("pending"),
  PROCESSING("processing"),
  COMPLETED("completed"),
  FAILED("failed");

  private final String label;

  TaskStatus(String label) {
    this.label = label;
  }

  @Override
  public String toString() {
    return label;
  }
}
