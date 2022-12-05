/*
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details.
 */

package applications;

import core.*;
import report.RumourAppReporter;

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
	/** Size of the pong message */
	public static final String PING_PONG_SIZE = "pongSize";

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
	private int		pongSize=1;
	private Random	rng;

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
		if (s.contains(PING_PONG_SIZE)) {
			this.pongSize = s.getInt(PING_PONG_SIZE);
		}
		if (s.contains(PING_DEST_RANGE)){
			int[] destination = s.getCsvInts(PING_DEST_RANGE,2);
			this.destMin = destination[0];
			this.destMax = destination[1];
		}

		rng = new Random(this.seed);
		super.setAppID(APP_ID);
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
		this.pongSize = a.getPongSize();
		this.pingSize = a.getPingSize();
		this.rng = new Random(this.seed);
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
			double real = (double) msg.getProperty("real");	// Realistic or not (given as decimal)
			double conf = getConfidence(msg, host);	// Confidence of the message received by the sender
			double chatProb = 1; // TODO : probability to talk about rumour (based on schedule)

			double sendingProb = getSendProbability(conf, real, chatProb);

			msg.printHops("r99");

			if (sendingProb >= rng.nextDouble()){
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
	 * @return the confidence of the message
	 */
	public double getConfidence(Message msg, DTNHost host){
		// TODO : return confidence of message based on sender, message, path
		double conf = 1;

		DTNHost from = msg.getFrom();

		if (from.groupId.equalsIgnoreCase(host.groupId)){
			// TODO : Improve confidence if sender and receiver from same group
		}

		return conf;
	}

	/**
	 * @param conf confidence of the message
	 * @param real realistic or not (given as a decimal)
	 * @param chatProb probability to talk about rumour (based on schedule)
	 * @return the confidence of the message
	 */
	public double getSendProbability(double conf, double real, double chatProb){
		// TODO : return probability of sending the message
		double prob = 1;
		return prob;
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
	 * @return the pongSize
	 */
	public int getPongSize() {
		return pongSize;
	}

	/**
	 * @param pongSize the pongSize to set
	 */
	public void setPongSize(int pongSize) {
		this.pongSize = pongSize;
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
