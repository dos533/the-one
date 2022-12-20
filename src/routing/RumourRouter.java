/*
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details.
 */
package routing;

import core.*;
import movement.MovementModel;
import movement.RoomBasedMovement;
import movement.room.RoomBase;

import java.util.HashMap;
import java.util.Random;

/**
 * Rumuor message router
 * TODO : Model buffer
 * TODO : Model multiple transfers simultaneously (group behavior)
 * with drop-oldest buffer and only single transferring
 * connections at a time.
 */
public class RumourRouter extends ActiveRouter {

	/** Static variables */
	private static final HashMap<String, Double> sendProb;
	private static final boolean verbose = false;

	static {
		sendProb = new HashMap<>();
		sendProb.put("LunchOptions", .9);
		sendProb.put("LectureRooms", .3);
		sendProb.put("GatheringRooms", .95);
		sendProb.put("Wings", .9);
		sendProb.put("EntranceAndExitOptions", 0.0);
	}

	private Random rng;

	/**
	 * Constructor. Creates a new message router based on the settings in
	 * the given Settings object.
	 * @param s The settings object
	 */
	public RumourRouter(Settings s) {
		super(s);
		rng = new Random();
		//TODO: read&use epidemic router specific settings (if any)
	}

	/**
	 * Copy constructor.
	 * @param r The router prototype where setting values are copied from
	 */
	protected RumourRouter(RumourRouter r) {
		super(r);
		rng = new Random();
		//TODO: copy epidemic settings here (if any)
	}

	@Override
	public RumourRouter replicate() {
		return new RumourRouter(this);
	}

	@Override
	public void update() {
		super.update();
		if (isTransferring() || !canStartTransfer()) {
			return; // transferring, don't try other connections yet
		}

		double chatProb = getSendProbability();
		if (rng.nextDouble() <= chatProb) {
			// Try first the messages that can be delivered to final recipient
			if (exchangeDeliverableMessages() != null) {
				return; // started a transfer, don't try others (yet)
			}

			// then try any/all message to any/all connection
			this.tryAllMessagesToAllConnections();
		}
	}


	/**
	 * How to handle incoming messages after it has been received
	 * This method should be called (on the receiving host) after a message
	 * was successfully transferred. The transferred message is put to the
	 * message buffer unless this host is the final recipient of the message.
	 * @param id Id of the transferred message
	 * @param from Host the message was from (previous hop)
	 * @return The message that this host received
	 */
	@Override
	public Message messageTransferred(String id, DTNHost from) {
		Message incoming = removeFromIncomingBuffer(id, from);
		boolean isFirstDelivery; // is this first delivered instance of the msg


		if (incoming == null) {
			throw new SimError("No message with ID " + id + " in the incoming "+
					"buffer of " + this.getHost());
		}

		incoming.setReceiveTime(SimClock.getTime());

		// Pass the message to the application (if any) and get outgoing message
		Message outgoing = incoming;
		for (Application app : getApplications(incoming.getAppID())) {
			// Note that the order of applications is significant
			// since the next one gets the output of the previous.
			outgoing = app.handle(outgoing, this.getHost());
			if (outgoing == null) break; // Some app wanted to drop the message
		}

		Message aMessage = (outgoing==null)?(incoming):(outgoing);
		// If the application re-targets the message (changes 'to')
		// then the message is not considered as 'delivered' to this host.
//		isFirstDelivery = isFinalRecipient && !isDeliveredMessage(aMessage); // NO : This will never happen
		isFirstDelivery = false;

		if (outgoing!=null) {
			// not the final recipient and app doesn't want to drop the message
			// -> put to buffer
			addToMessages(aMessage, false);
		}
//		} else if (isFirstDelivery) {
//			this.deliveredMessages.put(id, aMessage);
//		} else if (outgoing == null) {
//			// Blacklist messages that an app wants to drop.
//			// Otherwise the peer will just try to send it back again.
//			this.blacklistedMessages.put(id, null);
//		}

		for (MessageListener ml : this.getmListeners()) {
			ml.messageTransferred(aMessage, from, this.getHost(), isFirstDelivery);
		}

		return aMessage;
	}

	/**
	 * @return the probability of sending the message based on schedule
	 */
	public double getSendProbability(){
		double chatProb;
		MovementModel movement = this.getHost().getMovement();
//		if (movement instanceof RoomBasedMovement){
			RoomBase.RoomType roomCategory = ((RoomBasedMovement) movement).getRoomType();
			String cat = RoomBase.getRoomCategory(roomCategory);
			chatProb = sendProb.get(cat);

			if (verbose) System.out.println("Chat prob: " + chatProb);
//		}else{
//			chatProb = 1;
//		}

		return chatProb;
	}






}
