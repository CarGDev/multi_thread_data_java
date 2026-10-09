package ridesharing.task;

import java.util.concurrent.ThreadLocalRandom;
import ridesharing.driver.Driver;
import ridesharing.exception.ProcessingException;
import ridesharing.location.Location;
import ridesharing.logger.Logger;
import ridesharing.result.Result;
import ridesharing.rider.Rider;

public class RideRequest extends BaseTask implements Task {
  private static final double BASE_FARE = 2.5;
  private static final double FARE_PER_KM = 1.2;
  private static final String NO_DRIVER_TEXT = "no driver available";
  private static final int MIN_WORK_MS = 50;
  private static final int MAX_WORK_MS = 200;

  private final Rider rider;
  private final Location pickup;
  private final Location destination;
  private final Logger logger;

  public RideRequest(
      int taskID, Rider rider, Location pickup, Location destination, Logger logger) {
    this.rider = rider;
    this.pickup = pickup;
    this.destination = destination;
    this.logger = logger;
    init(taskID);
  }

  private Result.RideInfo baseRideInfo() {
    Result.RideInfo info = new Result.RideInfo();
    info.riderID = rider.getID();
    info.riderName = rider.getName();
    info.pickup = pickup;
    info.destination = destination;
    return info;
  }

  @Override
  public Result process() {
    setStatus(TaskStatus.PROCESSING);
    Driver driver = findDriver();
    if (driver == null) {
      setStatus(TaskStatus.FAILED);
      logger.warn("task " + taskID + ": " + NO_DRIVER_TEXT);
      Result result = new Result(taskID, false, NO_DRIVER_TEXT);
      result.setRide(baseRideInfo());
      return result;
    }
    try {
      Location driverStart = driver.getLocation();
      Thread.sleep(MIN_WORK_MS + ThreadLocalRandom.current().nextInt(MAX_WORK_MS - MIN_WORK_MS));

      double fare = calculateFare();
      setStatus(TaskStatus.COMPLETED);

      Result res =
          new Result(
              taskID,
              true,
              String.format(
                  "rider %s assigned to driver %s, fare %.2f",
                  rider.getName(), driver.getName(), fare));

      Result.RideInfo info = baseRideInfo();
      info.driverID = driver.getID();
      info.driverName = driver.getName();
      info.driverStart = driverStart;
      info.driverEnd = destination;
      info.driverToPickupKm = driverStart.distanceTo(pickup);
      info.tripDistanceKm = tripDistanceKm();
      info.fare = fare;
      res.setRide(info);
      return res;
    } catch (InterruptedException error) {
      Thread.currentThread().interrupt();
      setStatus(TaskStatus.FAILED);
      throw new ProcessingException(taskID, "interrupted");
    } finally {
      driver.completeRideAt(destination);
    }
  }

  public Driver findDriver() {
    return Driver.acquireNearest(pickup);
  }

  public double tripDistanceKm() {
    return pickup.distanceTo(destination);
  }

  public double calculateFare() {
    return BASE_FARE + FARE_PER_KM * tripDistanceKm();
  }

  public Rider getRider() {
    return rider;
  }

  public Location getPickup() {
    return pickup;
  }

  public Location getDestination() {
    return destination;
  }
}
