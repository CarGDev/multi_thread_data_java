import java.time.Duration;
import java.time.Instant;
import java.util.List;
import javax.annotation.processing.FilerException;
import ridesharing.exception.FileIOException;
import ridesharing.simulation.Simulation;
import ridesharing.system.RideSharingSystem;
import ridesharing.task.Task;

static final int WORKER_COUNT = 8;
static final String RESULTS_FILE = "results.csv";

void main() throws FileIOException {
  try {
    Simulation.loadDrivers();
  } catch (FilerException e) {
    throw new FileIOException("loading drivers: " + e.getMessage());
  }

  List<Task> tasks = null;
  try {
    tasks = Simulation.loadRides();
  } catch (FilerException e) {
    throw new FileIOException("loading rides: " + e.getMessage());
  }

  RideSharingSystem sys = RideSharingSystem.initialize(WORKER_COUNT);
  try {
    Instant start = Instant.now();
    sys.run(tasks, RESULTS_FILE);

    RideSharingSystem.Stats stats = sys.stats();
    System.out.printf(
        "done in %dms: %d total, %d successful, %d failed (results in %s)",
        Duration.between(start, Instant.now()).toMillis(),
        stats.total,
        stats.success,
        stats.failed,
        RESULTS_FILE);
  } finally {
    sys.close();
  }
}
