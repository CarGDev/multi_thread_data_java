import java.time.Duration;
import java.time.Instant;
import java.util.List;
import ridesharing.simulation.Simulation;
import ridesharing.system.RideSharingSystem;
import ridesharing.task.Task;

static final int WORKER_COUNT = 8;
static final String RESULTS_FILE = "results.csv";

void main() {
  try {
    Simulation.loadDrivers();
  } catch (RuntimeException e) {
    IO.println("loading drivers: " + e.getMessage());
    System.exit(1);
  }

  List<Task> tasks = null;
  try {
    tasks = Simulation.loadRides();
  } catch (RuntimeException e) {
    IO.println("loading rides: " + e.getMessage());
    System.exit(1);
  }

  RideSharingSystem sys = RideSharingSystem.initialize(WORKER_COUNT);
  try {
    Instant start = Instant.now();
    try {
      sys.run(tasks, RESULTS_FILE);
    } catch (RuntimeException e) {
      IO.println("running: " + e.getMessage());
      sys.close();
      System.exit(1);
    }

    RideSharingSystem.Stats stats = sys.stats();
    IO.println(
        String.format(
            "done in %dms: %d total, %d successful, %d failed (results in %s)",
            Duration.between(start, Instant.now()).toMillis(),
            stats.total,
            stats.success,
            stats.failed,
            RESULTS_FILE));
  } finally {
    sys.close();
  }
}
