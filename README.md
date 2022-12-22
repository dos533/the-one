# The ONE

The Opportunistic Network Environment simulator.

> - For introduction and releases, see [the ONE homepage at GitHub](http://akeranen.github.io/the-one/).
> - For instructions on how to get started, see [the README](https://github.com/akeranen/the-one/wiki/README).
> - The [wiki page](https://github.com/akeranen/the-one/wiki) has the latest information.



# Rumour spreading within scientific institutions

Group 5: Schuler Dominik, Schulte Kilian, Sritharan Suren and Eble Lars


## Running the simulation

Run the simulation using one of the four setting files inside `rumour_settings/`:

- `rumour_settings/fmi_settings1.txt`: Normal Day
- `rumour_settings/fmi_settings1.txt`: Exam
- `rumour_settings/fmi_settings1.txt`: Normal Day
- `rumour_settings/fmi_settings1.txt`: Exam

## Inspecting the reports

The simulation generates two reports:

- `reports/FMI-..._RumourAppReporter.txt`: Generates information about the spread rumours.
- `reports/FMI-..._ScheduleReporter.txt`: Outputs the list of all generated schedules.

## Implementation files

The following files contain custom implementations for our simulation:

- `src/movement/schedule/Schedule.java`: Contains the domain model for a schedule and the implementation for 
  generating schedules for each group.
  
  Each schedule consists of a sequence of `ScheduleSlot`s defining the time, duration and room a node should be in.
  Additionally has an `exitRoom` to go to before the first or after the last slot.
  
  A schedule provides two main methods to be called by the movement class:
  - `getNextRoom(int currentTime)` returns the room the node should go to at the given time. This is either a room corresponding to the currently active `ScheduleSlot`, or the `exitRoom`.
  - `getNextSlotTime(int currentTime)` returns the time the node should move to another room, effectively setting the duration a node should stay in the current room.
  
- `src/movement/schedule/(ScheduleSlot/Lecture).java`: Contain the domain models for a schedule slot and lecture.

- `src/movement/room/RoomBase.java`: Contains the base interface for rooms and implementations for selecting rooms.

  Rooms are grouped into categories with methods like `GetRandom(...)Option()` to return a random room from this category.
  This is used when generating the schedule for each node.
  
- `src/movement/room/(...).java`: Contain the domain models for each specific room types.

  Each room defines a polygon for nodes to move in and one or multiple doors to neighboring rooms.
  
- `src/movement/RoomBasedMovement.java`: Contains the movement implementation to navigate nodes through the defined rooms.

  This uses the schedule of each node to get the current room it should be in and the time it should stay there. When the room
  changes, it routes the node to the next room using BFS on the graph of neighboring rooms.

- `src/routing/RumourRouter.java`: Contains the router implementation for spreading rumours.

- `src/application/RumourApplication.java`: Contains the application implementation for handling rumours
