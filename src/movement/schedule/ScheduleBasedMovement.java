/*
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details.
 */
package movement.schedule;

import core.Coord;
import core.DTNHost;
import core.Settings;
import movement.*;

/**
 *
 * This movement model makes use of several other movement models to simulate
 * movement with daily routines. People wake up in the morning, go to work,
 * go shopping or similar activities in the evening and finally go home to
 * sleep.
 *
 * @author Frans Ekman
 */
public class ScheduleBasedMovement extends ExtendedMovementModel {

	private MapRouteMovement routeMovement;
	private Schedule schedule;

	/**
	 * Creates a new instance of ScheduleBasedMovement
	 * @param settings
	 */
	public ScheduleBasedMovement(Settings settings) {
		super(settings);
		routeMovement = new MapRouteMovement(settings);
		setCurrentMovementModel(routeMovement);
	}

	/**
	 * Creates a new instance of ScheduleBasedMovement from a prototype
	 * @param proto
	 */
	public ScheduleBasedMovement(ScheduleBasedMovement proto) {
		super(proto);
		routeMovement = new MapRouteMovement(proto.routeMovement);
		setCurrentMovementModel(routeMovement);
	}

	@Override
	public void setHost(DTNHost host) {
		super.setHost(host);
		this.generateSchedule(); // Can only initialize after the host has been set
	}

	private void generateSchedule() {
		int seed = host.getAddress();
		schedule = Schedule.fromSeed(seed);
		// set initial location on movement model
	}

	@Override
	public boolean newOrders() {
		// always true for map route movement?
		if (routeMovement.isReady()) {

			// check schedule and set next destination


		}
		return true;
	}

	@Override
	public Coord getInitialLocation() {
		return routeMovement.getInitialLocation();
	}

	@Override
	public MovementModel replicate() {
		return new ScheduleBasedMovement(this);
	}
}
