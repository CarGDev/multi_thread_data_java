package ridesharing.rider;

import java.util.ArrayList;
import java.util.List;
import ridesharing.location.Location;

public class Rider {
  private final int riderID;
  private final String name;
  private int currentLocation;

  private static final List<Rider> riders = new ArrayList<>();
  private static final Object LOCK = new Object();

  public Rider(int riderID, String name, double latitude, double longitude, String address) {
    this.riderID = riderID;
    this.name = name;
    Location loc = Location.create(latitude, longitude, address);
    this.currentLocation = loc.locationID;
    synchronized (LOCK) {
      riders.add(this);
    }
  }

  public static List<Rider> getAll() {
    synchronized (LOCK) {
      return new ArrayList<>(riders);
    }
  }

  public void updateLocation(double latitude, double longitude, String address) {
    Location loc = Location.create(latitude, longitude, address);
    synchronized (LOCK) {
      this.currentLocation = loc.locationID;
    }
  }

  public int getID() {
    return this.riderID;
  }

  public String getName() {
    return this.name;
  }

  public Location getLocation() {
    int id;
    synchronized (LOCK) {
      id = this.currentLocation;
    }
    return Location.getLocationById(id);
  }
}
