/*
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details.
 */

package applications;

import core.*;
import report.RumourAppReporter;

import java.util.HashMap;
import java.util.Random;

/**
 * Simple ping application to demonstrate the application support. The
 * application can be configured to send pings with a fixed interval or to only
 * answer to pings it receives. When the application receives a ping it sends
 * a pong message in response.
 *
 * The corresponding <code>PingAppReporter</code> class can be used to record
 * information about the application behavior.
 *
 * @see RumourAppReporter
 * @author teemuk
 */
public class RumourApplication extends Application {
	/** Run in passive mode - don't generate pings but respond */
	public static final String PING_PASSIVE = "passive";
	/** Ping generation interval */
	public static final String PING_INTERVAL = "interval";
	/** Ping interval offset - avoids synchronization of ping sending */
	public static final String PING_OFFSET = "offset";
	/** Destination address range - inclusive lower, exclusive upper */
	public static final String PING_DEST_RANGE = "destinationRange";
	/** Seed for the app's random number generator */
	public static final String PING_SEED = "seed";
	/** Size of the ping message */
	public static final String PING_PING_SIZE = "pingSize";
	/** Threshold of believing the rumour (in percentage) */
	public static final String RUMOUR_THRESHOLD = "threshold";

	/** Application ID */
	public static final String APP_ID = "RumourApplication";

	// Private vars
	private double lastRumourCreated = 0;
	private double	interval = 500;
	private boolean passive = false;
	private int		seed = 0;
	private int		destMin=0;
	private int		destMax=1;
	private int		pingSize=1;
	private Random	rng;
	private double threshold;
	private HashMap<String, HashMap<String, HashMap<Integer, Integer>>> msgReceived;


	/**
	 * Creates a new ping application with the given settings.
	 *
	 * @param s	Settings to use for initializing the application.
	 */
	public RumourApplication(Settings s) {
		if (s.contains(PING_PASSIVE)){
			this.passive = s.getBoolean(PING_PASSIVE);
		}
		if (s.contains(PING_INTERVAL)){
			this.interval = s.getDouble(PING_INTERVAL);
		}
		if (s.contains(PING_OFFSET)){
			this.lastRumourCreated = s.getDouble(PING_OFFSET);
		}
		if (s.contains(PING_SEED)){
			this.seed = s.getInt(PING_SEED);
		}
		if (s.contains(PING_PING_SIZE)) {
			this.pingSize = s.getInt(PING_PING_SIZE);
		}
		if (s.contains(RUMOUR_THRESHOLD)) {
			this.threshold = s.getDouble(RUMOUR_THRESHOLD);
		}
		if (s.contains(PING_DEST_RANGE)){
			int[] destination = s.getCsvInts(PING_DEST_RANGE,2);
			this.destMin = destination[0];
			this.destMax = destination[1];
		}

		rng = new Random(this.seed);
		super.setAppID(APP_ID);

		msgReceived = new HashMap<>();
	}

	/**
	 * Copy-constructor
	 *
	 * @param a
	 */
	public RumourApplication(RumourApplication a) {
		super(a);
		this.lastRumourCreated = a.getLastRumourCreated();
		this.interval = a.getInterval();
		this.passive = a.isPassive();
		this.destMax = a.getDestMax();
		this.destMin = a.getDestMin();
		this.seed = a.getSeed();
		this.pingSize = a.getPingSize();
		this.rng = new Random(this.seed);
		this.msgReceived = (HashMap<String, HashMap<String, HashMap<Integer, Integer>>>) a.msgReceived.clone();
	}

	private void addReceivedMessage(Message msg, DTNHost host){
		DTNHost from = msg.getLastHop();
		String msgID = msg.getId();
		int val;

		if (!msgReceived.containsKey(msgID)) msgReceived.put(msgID, new HashMap<>());
		if (!msgReceived.get(msgID).containsKey(from.groupId)) msgReceived.get(msgID).put(from.groupId, new HashMap<>());
		if (msgReceived.get(msgID).get(from.groupId).containsKey(from.getAddress())) {
			val = msgReceived.get(msgID).get(from.groupId).get(from.getAddress()) + 1;
		}else{
			val = 1;
		}
		msgReceived.get(msgID).get(from.groupId).put(from.getAddress(), val);
	}

	/**
	 * Handles an incoming message. If the message is a ping message replies
	 * with a pong message. Generates events for ping and pong messages.
	 *
	 * @param msg	message received by the router
	 * @param host	host to which the application instance is attached
	 */
	@Override
	public Message handle(Message msg, DTNHost host) {
		String type = (String)msg.getProperty("type");
		if (type==null) return msg; // Not a ping/pong message

		if (type.equalsIgnoreCase("rumour")){
			super.sendEventToListeners("receivedRumour", msg, host);

			addReceivedMessage(msg, host);

			double real = (double) msg.getProperty("real");	// Realistic or not (given as percentage)
			double infectProb = getConfidence(msg, host, real);	// Confidence of the message received by the sender

			if (host.getAddress()==99){
				System.out.print(host.getAddress());
				System.out.println(host.getMessageCollection());
				System.out.println(this.msgReceived.toString());
			}

//			msg.printHops("r99");

			if (infectProb >= threshold){
				return msg;
			}
			else{
				return null;
			}

		}

		return msg;
	}

	/**
	 * Draws a random host from the destination range
	 *
	 * @return host
	 */
	private DTNHost randomHost() {
		int destaddr = 0;
		if (destMax == destMin) {
			destaddr = destMin;
		}
		destaddr = destMin + rng.nextInt(destMax - destMin);
		World w = SimScenario.getInstance().getWorld();
		return w.getNodeByAddress(destaddr);
	}

	@Override
	public Application replicate() {
		return new RumourApplication(this);
	}

	/**
	 * Sends a ping packet if this is an active application instance.
	 *
	 * @param host to which the application instance is attached
	 */
	@Override
	public void update(DTNHost host) {
		if (this.passive) return;
		double curTime = SimClock.getTime();

		if (curTime - this.lastRumourCreated >= this.interval && curTime < 100){
			// Rumour created only in the first few tics
			// TODO : Change randomHost to fixed unreachable host (0, 0)
			// TODO : Change initial confience based on type of rumour
			Message m = new Message(host, randomHost(),
					SimClock.getIntTime() + "-" + host.getAddress(),
					getPingSize());
			m.addProperty("type", "rumour");
			m.addProperty("real", 1.0);
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
		// TODO : return confidence of message based on sender, message, path

		double conf = 1;

//		DTNHost from = msg.getLastHop();
//
//		if (from.groupId.equalsIgnoreCase(host.groupId)){
//			// TODO : Improve confidence if sender and receiver from same group
//		}

		return conf;
	}

	/**
	 * @return the lastRumourCreated
	 */
	public double getLastRumourCreated() {
		return lastRumourCreated;
	}

	/**
	 * @param lastRumourCreated the lastRumourCreated to set
	 */
	public void setLastRumourCreated(double lastRumourCreated) {
		this.lastRumourCreated = lastRumourCreated;
	}

	/**
	 * @return the interval
	 */
	public double getInterval() {
		return interval;
	}

	/**
	 * @param interval the interval to set
	 */
	public void setInterval(double interval) {
		this.interval = interval;
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
	 * @return the destMin
	 */
	public int getDestMin() {
		return destMin;
	}

	/**
	 * @param destMin the destMin to set
	 */
	public void setDestMin(int destMin) {
		this.destMin = destMin;
	}

	/**
	 * @return the destMax
	 */
	public int getDestMax() {
		return destMax;
	}

	/**
	 * @param destMax the destMax to set
	 */
	public void setDestMax(int destMax) {
		this.destMax = destMax;
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
	 * @return the pingSize
	 */
	public int getPingSize() {
		return pingSize;
	}

	/**
	 * @param pingSize the pingSize to set
	 */
	public void setPingSize(int pingSize) {
		this.pingSize = pingSize;
	}

}
