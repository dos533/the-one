/*
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details.
 */

package report;

import applications.RumourApplication;
import core.*;
import movement.RoomBasedMovement;
import movement.schedule.Schedule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Reporter for the <code>Schedule</code>. Outputs all schedules.
 *
 * @author schultek
 */
public class ScheduleReporter extends Report implements MovementListener {

	private final HashMap<DTNHost, Schedule> schedules;

	public ScheduleReporter(){
		super();
		schedules = new HashMap<>();
	}


	@Override
	public void newDestination(DTNHost host, Coord destination, double speed) {

	}

	@Override
	public void initialLocation(DTNHost host, Coord location) {
		if (host.getMovement() instanceof  RoomBasedMovement) {
			RoomBasedMovement movement = (RoomBasedMovement) host.getMovement();
			Schedule schedule = movement.getSchedule();
			schedules.put(host, schedule);
		}
	}


	@Override
	public void done() {
		for (DTNHost host : schedules.keySet()) {
			write(host.getAddress() + ":"+host.groupId+":"+schedules.get(host).toString());
		}
		super.done();
	}

}
