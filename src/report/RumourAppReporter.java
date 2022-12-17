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
import java.util.HashSet;
import java.util.Set;

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

	private final HashMap<String, Set<DTNHost>> receivedHost;

	private final HashMap<String, Set<DTNHost>> infected;

	public RumourAppReporter(){
		super();
		rumours = new ArrayList<>();
		received = new HashMap<>();
		receivedNoDuplicate = new HashMap<>();
		receivedHost = new HashMap<>();
		infected = new HashMap<>();
	}

	public void addReceivedRemoveDuplicate(Message msg){
		String msgId = msg.toString();
		String hops = msg.hopsToString(msg.getHopCount());

		if (!receivedNoDuplicate.containsKey(msgId)) receivedNoDuplicate.put(msgId, new HashMap<>());
		receivedNoDuplicate.get(msgId).put(hops, msg);

		if (msg.getHopCount() > 1){
			String hopsLast = msg.hopsToString(msg.getHopCount()-1);
			receivedNoDuplicate.get(msgId).remove(hopsLast);
		}

	}

	public void addReceived(Message msg){
		String msgId = msg.toString();
		if (!received.containsKey(msgId)) received.put(msgId, new ArrayList<>());

		received.get(msgId).add(msg);
	}

	public void countReceived(Message msg, DTNHost host){
		String msgId = msg.toString();
		if (!receivedHost.containsKey(msgId)) receivedHost.put(msgId, new HashSet<>());
		receivedHost.get(msgId).add(host);
	}

	public void countInfected(Message msg, DTNHost host){
		String msgId = msg.toString();
		if (!infected.containsKey(msgId)) infected.put(msgId, new HashSet<>());
		infected.get(msgId).add(host);
	}

	public void gotEvent(String event, Object params, Application app,
			DTNHost host) {

		// Check that the event is sent by correct application type
		if (!(app instanceof RumourApplication)) return;

		if (event.equalsIgnoreCase("createdRumour")) {
			rumours.add((Message) params);
		}

		if (event.equalsIgnoreCase("receivedRumour")) {
			// Add message to received messages
			addReceived((Message) params);
			// Count hosts who received message
			countReceived((Message) params, host);
			// Add message and remove duplicates
			addReceivedRemoveDuplicate((Message) params);
		}

		if (event.equalsIgnoreCase("sendRumour")) {
			countInfected((Message) params, host);
		}

	}


	@Override
	public void done() {
		write("Rumour : " + getScenarioName() +
				"\nsim_time: " + format(getSimTime()));

		HashMap<String, ArrayList<Integer>> hopCount = new HashMap<>();
		String txt;

		txt = "RumoursCreated: " + this.rumours.size();
		write(txt);

		txt = "RumourList: " + rumours.toString();
		write(txt);

//		for (String msgId : received.keySet()) {
//			hopCount.put(msgId, new ArrayList<>());
//			for (Message m: received.get(msgId)){
//				hopCount.get(msgId).add(m.getHopCount());
//			}
//		}
//		txt = hopCount.toString();
//		write(txt);

		txt = "Received: " + receivedHost.toString();
		write(txt);

		txt = "Infected: " + infected.toString();
		write(txt);

		for (String msgId : received.keySet()) {
			hopCount.remove(msgId);
			hopCount.put(msgId, new ArrayList<>());
			for (String hops: receivedNoDuplicate.get(msgId).keySet()){
				hopCount.get(msgId).add(receivedNoDuplicate.get(msgId).get(hops).getHopCount());
			}
		}
		txt = "HopCount: " + hopCount.toString();
		write(txt);

		super.done();
	}
}
