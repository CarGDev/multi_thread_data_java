package ridesharing.driver;

public enum DriverStatus {
  AVAILABLE("available"),
  BUSY("busy"),
  OFFLINE("offline");

  private final String label;

  DriverStatus(String label) {
    this.label = label;
  }

  public String toString() {
    return label;
  }
}
