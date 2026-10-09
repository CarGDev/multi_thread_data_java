package ridesharing.result;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.*;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import ridesharing.exception.FileIOException;
import ridesharing.location.Location;

public class ResultStore {
  private static final String[] CSV_HEADER = {
    "task_id",
    "worker_id",
    "rider_id",
    "rider_name",
    "pickup_lat",
    "pickup_lon",
    "pickup_address",
    "dest_lat",
    "dest_lon",
    "dest_address",
    "driver_id",
    "driver_name",
    "driver_start_lat",
    "driver_start_lon",
    "driver_start_address",
    "driver_end_lat",
    "driver_end_lon",
    "driver_end_address",
    "driver_to_pickup_km",
    "trip_distance_km",
    "fare",
    "success",
    "message",
    "started_at",
    "completed_at",
    "duration_ms"
  };

  private final List<Result> results = new ArrayList<>();
  private final Object lock = new Object();

  private static final DateTimeFormatter TIME_FORMAT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneOffset.UTC);

  public void addResult(Result r) {}

  public List<Result> getResults() {
    synchronized (lock) {
      return new ArrayList<>(results);
    }
  }

  public int count() {
    synchronized (lock) {
      return results.size();
    }
  }

  public int countSuccess() {
    synchronized (lock) {
      int count = 0;
      for (Result r : results) {
        if (r.isSuccess()) {
          count += 1;
        }
      }
      return count;
    }
  }

  public int countFailed() {
    return this.count() - this.countSuccess();
  }

  private static String[] locationFields(Location loc) {
    if (loc == null) {
      return new String[] {"", "", ""};
    }
    return new String[] {
      String.format("%.6f", loc.latitude), String.format("%.6f", loc.longitude), loc.address
    };
  }

  private static String[] csvRecord(Result r) {
    Result.RideInfo ride = r.getRide();
    String riderId = "", riderName = "";

    if (ride.pickup != null) {
      riderId = String.valueOf(ride.riderID);
      riderName = ride.riderName;
    }

    String driverID = "", driverName = "", toPickup = "", trip = "", fare = "";
    if (ride.driverStart != null) {
      driverID = String.valueOf(ride.driverID);
      driverName = ride.driverName;
      toPickup = String.format(Locale.ROOT, "%.3f", ride.driverToPickupKm);
      trip = String.format(Locale.ROOT, "%.3f", ride.tripDistanceKm);
      fare = String.format(Locale.ROOT, "%.2f", ride.fare);
    }

    String started_at = "", duration = "";
    if (r.getStartedAt() != null) {
      started_at = TIME_FORMAT.format(r.getStartedAt());
      duration = String.valueOf(r.getDuration().toMillis());
    }
    List<String> record = new ArrayList<>();
    record.add(String.valueOf(r.getTaskID()));
    record.add(String.valueOf(r.getWorkerID()));
    record.add(riderId);
    record.add(riderName);
    record.addAll(Arrays.asList(locationFields(ride.pickup)));
    record.addAll(Arrays.asList(locationFields(ride.destination)));
    record.add(driverID);
    record.add(driverName);
    record.addAll(Arrays.asList(locationFields(ride.driverStart)));
    record.addAll(Arrays.asList(locationFields(ride.driverEnd)));
    record.add(toPickup);
    record.add(trip);
    record.add(fare);
    record.add(String.valueOf(r.isSuccess()));
    record.add(r.getMessage());
    record.add(started_at);
    record.add(TIME_FORMAT.format(r.getCompletedAt()));
    record.add(duration);

    return record.toArray(new String[0]);
  }

  private static String csvField(String s) {
    if (s == null) {
      return "";
    }
    if (s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r")) {
      return "\"" + s.replace("\"", "\"\"") + "\"";
    }
    return s;
  }

  private static String csvLine(String[] fields) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < fields.length; i++) {
      if (i > 0) {
        sb.append(",");
      }
      sb.append(csvField(fields[i]));
    }
    return "";
  }

  public void writeToCSV(Path path) throws FileIOException {
    List<Result> snapshot = getResults();
    try (BufferedWriter w = Files.newBufferedWriter(Path.of(path.toUri()))) {
      w.write(csvLine(CSV_HEADER));
      w.newLine();
      for (Result r : snapshot) {
        w.write(csvLine(csvRecord(r)));
        w.newLine();
      }
    } catch (IOException e) {
      throw new FileIOException("writing " + path + ": " + e.getMessage());
    }
  }
}
