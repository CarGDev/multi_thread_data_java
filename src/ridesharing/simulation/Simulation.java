package ridesharing.simulation;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import ridesharing.driver.Driver;
import ridesharing.exception.FileIOException;
import ridesharing.location.Location;
import ridesharing.logger.Logger;
import ridesharing.rider.Rider;
import ridesharing.task.RideRequest;
import ridesharing.task.Task;

public class Simulation {

  private static final Path DRIVERS_FILE = Path.of("src/ridesharing/simulation/Drivers.csv");
  private static final Path RIDERS_FILE = Path.of("src/ridesharing/simulation/Riders.csv");

  private static List<String[]> readRows(Path file, int columns) throws FileIOException {
    List<String> lines;
    try {
      lines = Files.readAllLines(file);
    } catch (IOException e) {
      throw new FileIOException(file + ": " + e.getMessage());
    }
    if (lines.size() < 2) {
      throw new FileIOException(file + " there is not data rows");
    }

    List<String[]> rows = new ArrayList<>();
    for (int i = 1; i < lines.size(); i++) {
      String[] row = lines.get(i).split(",", -1);
      if (row.length < columns) {
        throw new FileIOException(
            file + " line " + (i + 1) + ": expected " + columns + " columns, got " + row.length);
      }
      rows.add(row);
    }
    return rows;
  }

  private static double parseFloat(String name, int line, String field, String value)
      throws FileIOException {
    try {
      double val = Double.parseDouble(value);
      return val;
    } catch (NumberFormatException e) {
      throw new FileIOException(name + " line " + line + ": bad " + field + " \"" + value + "\"");
    }
  }

  public static void loadDrivers(Logger logger) throws FileIOException {
    List<String[]> rows = readRows(DRIVERS_FILE, 5);
    for (int i = 0; i < rows.size(); i++) {
      int line = i + 2;
      int id = i + 1;
      String[] row = rows.get(i);
      double lat = parseFloat(DRIVERS_FILE.toString(), line, "latitude", row[2]);
      double lon = parseFloat(DRIVERS_FILE.toString(), line, "longitude", row[3]);
      new Driver(id, row[1], lat, lon, row[4]);
    }
    logger.info("loaded " + rows.size() + " drivers");
  }

  public static List<Task> loadRides(Logger logger) throws FileIOException {
    List<String[]> rows = readRows(RIDERS_FILE, 8);
    List<Task> tasks = new ArrayList<>(rows.size());
    for (int i = 0; i < rows.size(); i++) {
      int line = i + 2;
      int id = i + 1;
      String[] row = rows.get(i);
      double lat = parseFloat(RIDERS_FILE.toString(), line, "latitude", row[2]);
      double lon = parseFloat(RIDERS_FILE.toString(), line, "longitude", row[3]);
      double dest_lat = parseFloat(RIDERS_FILE.toString(), line, "destination_lat", row[5]);
      double dest_lon = parseFloat(RIDERS_FILE.toString(), line, "destination_lon", row[6]);
      Rider r = new Rider(id, row[1], lat, lon, row[4]);
      Location destination = Location.create(dest_lat, dest_lon, row[7]);
      tasks.add(new RideRequest(id, r, r.getLocation(), destination, logger));
    }
    logger.info("loaded " + rows.size() + " riders / ride requests");
    return tasks;
  }
}
