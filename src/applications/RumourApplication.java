/*
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details.
 */

package applications;

import core.*;
import movement.MovementModel;
import movement.RoomBasedMovement;
import movement.room.RoomBase;
import report.RumourAppReporter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * Simple rumour application.
 * Application creates rumour at the start of simulations
 * Received rumours are ignored or forwarded based on confidence
 * The corresponding <code>RumourAppReporter</code> class can be used to record
 * information about the application behavior.
 *
 * @see RumourAppReporter
 * @author suren3141
 */
public class RumourApplication extends Application {
	/** Run in passive mode - don't create rumours, only forward them */
	public static final String RUMOUR_PASSIVE = "passive";
	/** Source address range - inclusive lower, exclusive upper */
	public static final String MSG_SRC = "source";
	/** Seed for the app's random number generator */
	public static final String PING_SEED = "seed";
	/** Size of the ping message */
	public static final String MSG_SIZE = "msgSize";
	/** Threshold of receiving the rumour */
	public static final String RUMOUR_RECEIVE_THRESHOLD = "receiveThreshold";
	/** Threshold of sending the rumour */
	public static final String RUMOUR_SEND_THRESHOLD = "sendThreshold";
	/** Realistic nature of rumour */
	public static final String RUMOUR_REAL = "real";
	public static final String RUMOUR_ID = "id";

	/** Application ID */
	public static final String APP_ID = "RumourApplication";

	// Static vars
	private static final HashMap<String, HashMap<String, Double>> trustMap;
	private static final HashMap<String, Integer> groupIdToNum;
	private static final boolean verbose = false;



	// Private vars
    private int rumourId;
	private double lastRumourCreated = -1;
	private boolean passive = false;
	private int		seed = 0;
	private ArrayList<Integer> srcList;
	private int		dest=0;
	private int msgSize =1;
	private Random	rng;

	private double real = 1.0;
	private double recThreshold;
	private double senThreshold;

	private HashMap<String, HashMap<String, HashMap<Integer, Integer>>> msgReceived;

	static {
		groupIdToNum = new HashMap<>();
		groupIdToNum.put("professor", 1);
		groupIdToNum.put("student", 2);
		groupIdToNum.put("cleaner", 3);
		groupIdToNum.put("barista", 4);
		groupIdToNum.put("visitor", 5);


		trustMap = new HashMap<>();
		trustMap.put("professor", new HashMap<>());
		trustMap.put("student", new HashMap<>());
		trustMap.put("cleaner", new HashMap<>());
		trustMap.put("barista", new HashMap<>());
		trustMap.put("visitor", new HashMap<>());

		trustMap.get("professor").put("professor", 0.95);
		trustMap.get("professor").put("student", 0.5);
		trustMap.get("professor").put("cleaner", 0.6);
		trustMap.get("professor").put("barista", 0.6);
		trustMap.get("professor").put("visitor", 0.7);

		trustMap.get("student").put("student", 0.8);
		trustMap.get("student").put("professor", 0.95);
		trustMap.get("student").put("cleaner", 0.6);
		trustMap.get("student").put("barista", 0.6);
		trustMap.get("student").put("visitor", 0.7);

		trustMap.get("cleaner").put("student", 0.5);
		trustMap.get("cleaner").put("professor", 0.8);
		trustMap.get("cleaner").put("cleaner", 0.8);
		trustMap.get("cleaner").put("barista", 0.6);
		trustMap.get("cleaner").put("visitor", 0.7);

		trustMap.get("barista").put("student", 0.5);
		trustMap.get("barista").put("professor", 0.8);
		trustMap.get("barista").put("cleaner", 0.8);
		trustMap.get("barista").put("barista", 0.6);
		trustMap.get("barista").put("visitor", 0.7);

		trustMap.get("visitor").put("student", 0.5);
		trustMap.get("visitor").put("professor", 0.8);
		trustMap.get("visitor").put("cleaner", 0.8);
		trustMap.get("visitor").put("barista", 0.6);
		trustMap.get("visitor").put("visitor", 0.7);
	}

	/**
	 * Creates a new rumour application with the given settings.
	 *
	 * @param s	Settings to use for initializing the application.
	 */
	public RumourApplication(Settings s) {
		if (s.contains(RUMOUR_PASSIVE)){
			this.passive = s.getBoolean(RUMOUR_PASSIVE);
		}
		if (s.contains(PING_SEED)){
			this.seed = s.getInt(PING_SEED);
		}
		if (s.contains(MSG_SIZE)) {
			this.msgSize = s.getInt(MSG_SIZE);
		}
		if (s.contains(RUMOUR_RECEIVE_THRESHOLD)) {
			this.recThreshold = s.getDouble(RUMOUR_RECEIVE_THRESHOLD);
		}
		if (s.contains(RUMOUR_SEND_THRESHOLD)) {
			this.senThreshold = s.getDouble(RUMOUR_SEND_THRESHOLD);
		}
		if (s.contains(MSG_SRC)){
			int[] source = s.getCsvInts(MSG_SRC);
			srcList = new ArrayList<>();
			for (int i : source) srcList.add(i);
		}
		if (s.contains(RUMOUR_REAL)){
			this.real = s.getDouble(RUMOUR_REAL);
		}
		if (s.contains(RUMOUR_ID)){
			this.rumourId = s.getInt(RUMOUR_ID);
		}else{
			/* rumourId is hostAddress */
			this.rumourId = -1;
		}
		rng = new Random(this.seed);
		super.setAppID(APP_ID);

		msgReceived = new HashMap<>();
	}

	/**
	 * Copy-constructor
	 *
	 * @param a Rumour application
	 */
	public RumourApplication(RumourApplication a) {
		super(a);
		this.lastRumourCreated = a.getLastRumourCreated();
		this.passive = a.isPassive();
		this.dest = a.dest;
		this.srcList = (ArrayList<Integer>) a.srcList.clone();
		this.seed = a.getSeed();
		this.msgSize = a.getMsgSize();
		this.rng = new Random(this.seed);
		this.recThreshold = a.recThreshold;
		this.senThreshold = a.senThreshold;
		this.msgReceived = (HashMap<String, HashMap<String, HashMap<Integer, Integer>>>) a.msgReceived.clone();
		this.rumourId = a.rumourId;
		this.real = a.real;
	}

	@Override
	public Application replicate() {
		return new RumourApplication(this);
	}

	private void addReceivedMessage(Message msg, DTNHost host){
		DTNHost from = msg.getLastHop();
		String msgID = msg.getId();
		int val;

		if (!msgReceived.containsKey(msgID)) msgReceived.put(msgID, new HashMap<>());
		if (!msgReceived.get(msgID).containsKey(from.groupId)) msgReceived.get(msgID).put(from.groupId, new HashMap<>());

		val = msgReceived.get(msgID).get(from.groupId).getOrDefault(from.getAddress(), 0);

		msgReceived.get(msgID).get(from.groupId).put(from.getAddress(), val+1);
	}


	private String getLocation(DTNHost host){
		MovementModel movement = host.getMovement();
		RoomBase.RoomType roomCategory = ((RoomBasedMovement) movement).getRoomType();

		return roomCategory.name();
	}


	/**
	 * Handles an incoming message.
	 * If the message is a rumour message ignore of forward
	 *
	 * @param msg	message received by the router
	 * @param host	host to which the application instance is attached
	 */
	@Override
	public Message handle(Message msg, DTNHost host) {
		String type = (String)msg.getProperty("type");
		if (type==null) return msg;

		if (type.equalsIgnoreCase("rumour")){
			super.sendEventToListeners("receivedRumour", msg, host);

			if (verbose) System.out.println("Message received : " + msg.getId() + "->" + host.getAddress() + " : " + getLocation(host));

			addReceivedMessage(msg, host);

			double real = (double) msg.getProperty("real");	// Realistic or not (given as percentage)
			double infectProb = getConfidence(msg, host, real);	// Confidence of the message received by the sender

			if (infectProb >= senThreshold){
				super.sendEventToListeners("sendRumour", msg, host);
				return msg;
			}
			else{
				return null;
			}
		}

		return msg;
	}

	/**
	 * Draws a random host from the range
	 *
	 * @return host
	 */
	private DTNHost randomHost(int min, int max) {
		int addr;
		if (min == max) {
			addr = min;
		}else{
			addr = min + rng.nextInt(max-min);
		}
		World w = SimScenario.getInstance().getWorld();
		return w.getNodeByAddress(addr);
	}


	/**
	 * Sends a msg packet if this is an active application instance.
	 *
	 * @param host to which the application instance is attached
	 */
	@Override
	public void update(DTNHost host) {
		if (this.passive) return;
		double curTime = SimClock.getTime();

		// Host in source range
		boolean hostInRange = srcList.contains(host.getAddress());

		if (this.lastRumourCreated < 0 && hostInRange){
			// Rumour created only in the first few tics
//			String msgId = SimClock.getIntTime() + "-" + host.getAddress();
			if (rumourId == 0) rumourId = groupIdToNum.get(host.groupId);
			if (rumourId<0) rumourId = host.getAddress();

			if (verbose) System.out.println("Message created : " + rumourId);

			String msgId = Integer.toString(rumourId);

			Message m = new Message(host, randomHost(dest, dest), msgId, getMsgSize());

			m.addProperty("type", "rumour");
			m.addProperty("real", this.real);
			m.setAppID(APP_ID);
			host.createNewMessage(m);

			// Call listeners
			super.sendEventToListeners("createdRumour", m, host);

			this.lastRumourCreated = curTime;
		}
	}

	/**
	 * @param msg the message
	 * @param host host to which the application is attached
	 * @param real Realistic or not (given as percentage)
	 * @return the confidence of the message
	 */
	public double getConfidence(Message msg, DTNHost host, double real){
		String msgId = msg.getId();
		String recType = host.groupId;
		HashMap<String, HashMap<Integer, Integer>> count = this.msgReceived.get(msgId);
		double conf = 0;

		for (String groupId : count.keySet()){
			double trust = trustMap.get(recType).get(groupId);
			for (Integer address : count.get(groupId).keySet()){
				if (count.get(groupId).get(address) >= recThreshold){
					conf += trust;
				}
			}
		}

		conf = conf * real;

		return conf;
	}



	/**
	 * @return the lastRumourCreated
	 */
	public double getLastRumourCreated() {
		return lastRumourCreated;
	}

	/**
	 * @return the passive
	 */
	public boolean isPassive() {
		return passive;
	}

	/**
	 * @param passive the passive to set
	 */
	public void setPassive(boolean passive) {
		this.passive = passive;
	}

	/**
	 * @return the seed
	 */
	public int getSeed() {
		return seed;
	}

	/**
	 * @param seed the seed to set
	 */
	public void setSeed(int seed) {
		this.seed = seed;
	}

	/**
	 * @return the msgSize
	 */
	public int getMsgSize() {
		return msgSize;
	}

	/**
	 * @param msgSize the msgSize to set
	 */
	public void setMsgSize(int msgSize) {
		this.msgSize = msgSize;
	}

}
