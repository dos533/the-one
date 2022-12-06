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
import java.util.HashMap;

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
	private final HashMap<String, ArrayList<Message>> received;

	private final HashMap<String, HashMap<String, Message>> receivedNoDuplicate;

	public RumourAppReporter(){
		super();
		rumours = new ArrayList<Message>();
		received = new HashMap<>();
		receivedNoDuplicate = new HashMap<>();
	}

	public void addRemoveDuplicate(Message msg){
		String msgId = msg.toString();
		String hops = msg.hopsToString(msg.getHopCount());
		receivedNoDuplicate.get(msgId).put(hops, msg);

		if (msg.getHopCount() > 1){
			String hopsLast = msg.hopsToString(msg.getHopCount()-1);
			receivedNoDuplicate.get(msgId).remove(hopsLast);
		}

	}

	public void gotEvent(String event, Object params, Application app,
			DTNHost host) {

		// Check that the event is sent by correct application type
		if (!(app instanceof RumourApplication)) return;

		if (event.equalsIgnoreCase("createdRumour")) {
			rumours.add((Message) params);
		}

		if (event.equalsIgnoreCase("receivedRumour")) {
			Message msg = (Message) params;
			String msgId = msg.toString();
			if (!received.containsKey(msgId)) received.put(msgId, new ArrayList<>());
			if (!receivedNoDuplicate.containsKey(msgId)) receivedNoDuplicate.put(msgId, new HashMap<>());

			// Add message to received messages
			received.get(msgId).add(msg);
			// Add message and remove duplicates
			addRemoveDuplicate(msg);
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

		HashMap<String, ArrayList<Integer>> hopCount = new HashMap<>();
		String txt;

		txt = "Rumours created: " + this.rumours.size();
		write(txt);

		txt = rumours.toString();
		write(txt);

		for (String msgId : received.keySet()) {
			hopCount.put(msgId, new ArrayList<>());
			for (Message m: received.get(msgId)){
				hopCount.get(msgId).add(m.getHopCount());
			}
		}
		txt = hopCount.toString();
		write(txt);

		for (String msgId : received.keySet()) {
			hopCount.remove(msgId);
			hopCount.put(msgId, new ArrayList<>());
			for (String hops: receivedNoDuplicate.get(msgId).keySet()){
				hopCount.get(msgId).add(receivedNoDuplicate.get(msgId).get(hops).getHopCount());
			}
		}
		txt = hopCount.toString();
		write(txt);

		super.done();
	}
}