package ridesharing.task;

import ridesharing.result.Result;

public interface Task {
  Result process();

  int getID();

  TaskStatus getStatus();
}
