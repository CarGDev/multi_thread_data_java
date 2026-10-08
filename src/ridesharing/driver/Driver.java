package ridesharing.driver;

import java.util.*;
import ridesharing.location.Location;

public class Driver {
  private final int driverID;
  private final String name;
  private int currentLocation;
  private DriverStatus status = DriverStatus.AVAILABLE;

  private static final List<Driver> drivers = new ArrayList<>();
  private static final Map<Integer, Driver> availableDrivers = new HashMap<>();
  private static final Object LOCK = new Object();

  public Driver(int driverID, String name, double latitude, double longitude, String address) {
    this.name = name;
    this.driverID = driverID;
    Location loc = Location.create(latitude, longitude, address);
    this.currentLocation = loc.locationID;
    synchronized (LOCK) {
      drivers.add(this);
      availableDrivers.put(driverID, this);
    }
  }

  public void updateLocation(double latitude, double longitude, String address) {
    Location loc = Location.create(latitude, longitude, address);
    synchronized (LOCK) {
      this.currentLocation = loc.locationID;
    }
  }

  public static Driver acquireNearest(Location pickup) {
    synchronized (LOCK) {
      Driver nearest = null;
      double best = Double.MAX_VALUE;

      for (Driver d : availableDrivers.values()) {
        Location loc = Location.getLocationById(d.currentLocation);
        if (loc == null) {
          continue;
        }
        double dist = loc.distanceTo(pickup);
        if (dist < best) {
          best = dist;
          nearest = d;
        }
      }
      if (nearest == null) {
        return null;
      }

      nearest.status = DriverStatus.BUSY;
      availableDrivers.remove(nearest.driverID);
      return nearest;
    }
  }

  public boolean acceptRide() {
    synchronized (LOCK) {
      if (this.status != DriverStatus.AVAILABLE) {
        return false;
      }
      this.status = DriverStatus.BUSY;
      availableDrivers.remove(this.driverID);
      return true;
    }
  }

  /** Frees the driver where they currently are. */
  public void completeRide() {
    synchronized (LOCK) {
      if (this.status != DriverStatus.BUSY) {
        return;
      }
      this.status = DriverStatus.AVAILABLE;
      availableDrivers.put(driverID, this);
    }
  }

  /** Frees the driver and leaves them at the ride's destination. */
  public void completeRideAt(Location destination) {
    synchronized (LOCK) {
      if (this.status != DriverStatus.BUSY) {
        return;
      }
      this.currentLocation = destination.locationID;
      this.status = DriverStatus.AVAILABLE;
      availableDrivers.put(driverID, this);
    }
  }

  public boolean goOffline() {
    synchronized (LOCK) {
      if (this.status != DriverStatus.AVAILABLE) {
        return false;
      }
      this.status = DriverStatus.OFFLINE;
      availableDrivers.remove(this.driverID);
      return true;
    }
  }

  public void goOnline() {
    synchronized (LOCK) {
      if (this.status == DriverStatus.OFFLINE) {
        this.status = DriverStatus.AVAILABLE;
        availableDrivers.put(this.driverID, this);
      }
    }
  }

  public int getID() {
    return this.driverID;
  }

  public String getName() {
    return this.name;
  }

  public Location getLocation() {
    synchronized (LOCK) {
      return Location.getLocationById(this.currentLocation);
    }
  }

  public DriverStatus getStatus() {
    synchronized (LOCK) {
      return this.status;
    }
  }

  public static List<Driver> getAll() {
    synchronized (LOCK) {
      return new ArrayList<>(drivers);
    }
  }

  public static List<Driver> getAllAvailable() {
    synchronized (LOCK) {
      return new ArrayList<>(availableDrivers.values());
    }
  }
}
