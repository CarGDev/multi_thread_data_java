import java.time.Duration;
import java.time.Instant;
import java.util.List;
import ridesharing.exception.FileIOException;
import ridesharing.logger.Logger;
import ridesharing.simulation.Simulation;
import ridesharing.system.RideSharingSystem;
import ridesharing.task.Task;

static final int WORKER_COUNT = 8;
static final String RESULTS_FILE = "results.csv";
static final String LOG_FILE = "ridesharing.log";

String formatElapsed(Duration d) {
  long totalMs = d.toMillis();
  long minutes = totalMs / 60_000;
  double seconds = (totalMs % 60_000) / 1000.0;
  if (minutes > 0) {
    return String.format(java.util.Locale.ROOT, "%dm%.3fs", minutes, seconds);
  }
  return String.format(java.util.Locale.ROOT, "%.3fs", seconds);
}

Logger openLogger() {
  try {
    return new Logger(LOG_FILE);
  } catch (FileIOException e) {
    Logger fallback = new Logger();
    fallback.warn("file logging disabled: " + e.getMessage());
    return fallback;
  }
}

void main() {
  boolean failed = false;
  try (Logger logger = openLogger()) {
    try {
      Simulation.loadDrivers(logger);
      List<Task> tasks = Simulation.loadRides(logger);

      RideSharingSystem sys = RideSharingSystem.initialize(WORKER_COUNT, logger);
      Instant start = Instant.now();
      sys.run(tasks, RESULTS_FILE);

      RideSharingSystem.Stats stats = sys.stats();
      String summary =
          String.format(
              "done in %s: %d total, %d successful, %d failed (results in %s)",
              formatElapsed(Duration.between(start, Instant.now())),
              stats.total,
              stats.success,
              stats.failed,
              RESULTS_FILE);
      System.out.println(summary);
    } catch (FileIOException e) {
      logger.error(e.getMessage());
      failed = true;
    }
  }
  if (failed) {
    System.exit(1);
  }
}
