/*
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details.
 */

package report;

import applications.RumourApplication;
import core.Application;
import core.ApplicationListener;
import core.DTNHost;
import core.Message;

import java.util.ArrayList;

/**
 * Reporter for the <code>PingApplication</code>. Counts the number of pings
 * and pongs sent and received. Calculates success probabilities.
 *
 * @author teemuk
 */
public class RumourAppReporter extends Report implements ApplicationListener {

//	private int pingsSent=0, pingsReceived=0;
//	private int pongsSent=0, pongsReceived=0;
	private final ArrayList<Message> rumours;
	private final ArrayList<Message> received;

	public RumourAppReporter(){
		super();
		rumours = new ArrayList<Message>();
		received = new ArrayList<>();
	}

	public void gotEvent(String event, Object params, Application app,
			DTNHost host) {

		// Check that the event is sent by correct application type
		if (!(app instanceof RumourApplication)) return;

		if (event.equalsIgnoreCase("createdRumour")) {
			rumours.add((Message) params);
		}

		if (event.equalsIgnoreCase("receivedRumour")) {
			received.add((Message) params);
		}

	}


	@Override
	public void done() {
		write("Rumour : " + getScenarioName() +
				"\nsim_time: " + format(getSimTime()));
//		double pingProb = 0; // ping probability
//		double pongProb = 0; // pong probability
//		double successProb = 0;	// success probability
//
//		if (this.pingsSent > 0) {
//			pingProb = (1.0 * this.pingsReceived) / this.pingsSent;
//		}
//		if (this.pongsSent > 0) {
//			pongProb = (1.0 * this.pongsReceived) / this.pongsSent;
//		}
//		if (this.pingsSent > 0) {
//			successProb = (1.0 * this.pongsReceived) / this.pingsSent;
//		}
//
//		String statsText = "pings sent: " + this.pingsSent +
//			"\npings received: " + this.pingsReceived +
//			"\npongs sent: " + this.pongsSent +
//			"\npongs received: " + this.pongsReceived +
//			"\nping delivery prob: " + format(pingProb) +
//			"\npong delivery prob: " + format(pongProb) +
//			"\nping/pong success prob: " + format(successProb)
//			;

		ArrayList<Integer> hopCount = new ArrayList<>();
		String txt = "Rumours created: " + this.rumours.size() + '\n';
		write(txt);

		txt = rumours.toString();
		write(txt);

		for (Message m: received) hopCount.add(m.getHopCount());
		txt = hopCount.toString();
		write(txt);

		super.done();
	}
}