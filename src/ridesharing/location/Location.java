package ridesharing.location;

import java.util.*;

public class Location {
  private static final double EARTH_RADIUS_KM = 6371.0;

  public final int locationID;
  public final double latitude;
  public final double longitude;
  public final String address;

  private static final List<Location> locations = new ArrayList<>();
  private static final Object LOCK = new Object();

  private Location(int id, double latitude, double longitude, String address) {
    this.locationID = id;
    this.latitude = latitude;
    this.longitude = longitude;
    this.address = address;
  }

  public static Location create(double lat, double lon, String address) {
    synchronized (LOCK) {
      int id = locations.size() + 1;
      Location loc = new Location(id, lat, lon, address);
      locations.add(loc);
      return loc;
    }
  }

  private static double toRad(double deg) {
    return deg * Math.PI / 180;
  }

  public int getLocationID() {
    return this.locationID;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Location other)) {
      return false;
    }
    return latitude == other.latitude && longitude == other.longitude;
  }

  @Override
  public int hashCode() {
    return Objects.hash(latitude, longitude);
  }

  @Override
  public String toString() {
    return String.format("%s (%.5f, %.5f)", address, latitude, longitude);
  }

  public static Location getLocationById(int id) {
    synchronized (LOCK) {
      if (id < 1 || id > locations.size()) {
        return null;
      }
      return locations.get(id - 1);
    }
  }

  public double distanceTo(Location destination) {
    double dLat = toRad(destination.latitude - latitude);
    double dLon = toRad(destination.longitude - longitude);
    double a =
        Math.sin(dLat / 2) * Math.sin(dLat / 2)
            + Math.cos(toRad(latitude))
                * Math.cos(toRad(destination.latitude))
                * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);

    return EARTH_RADIUS_KM * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  }
}
