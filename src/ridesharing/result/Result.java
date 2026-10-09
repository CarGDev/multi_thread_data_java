package ridesharing.result;

import java.time.Duration;
import java.time.Instant;
import ridesharing.location.Location;

public class Result {
  public static class RideInfo {
    public int riderID;
    public String riderName;
    public Location pickup;
    public Location destination;
    public int driverID;
    public String driverName;
    public Location driverStart;
    public Location driverEnd;
    public double driverToPickupKm;
    public double tripDistanceKm;
    public double fare;
  }

  private final int taskID;
  private int workerID;
  private final boolean success;
  private final String message;
  private RideInfo ride = new RideInfo();
  private Instant startedAt;
  private final Instant completedAt;

  public Result(int taskID, boolean success, String message) {
    this.taskID = taskID;
    this.success = success;
    this.message = message;
    this.completedAt = Instant.now();
  }

  public int getTaskID() {
    return taskID;
  }

  public boolean isSuccess() {
    return success;
  }

  public String getMessage() {
    return message;
  }

  public Instant getCompletedAt() {
    return completedAt;
  }

  public void setWorker(int workerID) {
    this.workerID = workerID;
  }

  public int getWorkerID() {
    return this.workerID;
  }

  public void setStartedAt(Instant t) {
    this.startedAt = t;
  }

  public Instant getStartedAt() {
    return this.startedAt;
  }

  public Duration getDuration() {
    if (startedAt == null) {
      return Duration.ZERO;
    }
    return Duration.between(startedAt, completedAt);
  }

  public void setRide(RideInfo info) {
    this.ride = info;
  }

  public RideInfo getRide() {
    return this.ride;
  }
}
