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

	private static final HashMap<String, Double> sendProb;
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
	 *
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
		boolean isFinalRecipient;
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
//		isFinalRecipient = aMessage.getTo() == this.host;  // NO : This will never happen
//		isFirstDelivery = isFinalRecipient && !isDeliveredMessage(aMessage); // NO : This will never happen
		isFinalRecipient = false;
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
	 * Handle incoming messages when it's being received (accept or no)??
	 *
	 * Checks if router "wants" to start receiving message (i.e. router
	 * isn't transferring, doesn't have the message and has room for it).
	 * @param m The message to check
	 * @return A return code similar to
	 * {@link MessageRouter#receiveMessage(Message, DTNHost)}, i.e.
	 * {@link MessageRouter#RCV_OK} if receiving seems to be OK,
	 * TRY_LATER_BUSY if router is transferring, DENIED_OLD if the router
	 * is already carrying the message or it has been delivered to
	 * this router (as final recipient), or DENIED_NO_SPACE if the message
	 * does not fit into buffer
	 */
	@Override
	protected int checkReceiving(Message m, DTNHost from) {
		if (isTransferring()) {
			return TRY_LATER_BUSY; // only one connection at a time
		}

		if ( hasMessage(m.getId()) || isDeliveredMessage(m) || super.isBlacklistedMessage(m.getId())) {
			return DENIED_OLD; // already seen this message -> reject it
		}

		if (m.getTtl() <= 0 && m.getTo() != getHost()) {
			/* TTL has expired and this host is not the final recipient */
			return DENIED_TTL;
		}

		if (!this.hasEnergy()) {
			return MessageRouter.DENIED_LOW_RESOURCES;
		}

//		TODO : Check policy
//		if (!policy.acceptReceiving(from, getHost(), m)) {
//			return MessageRouter.DENIED_POLICY;
//		}

		/* remove oldest messages but not the ones being sent */
		if (!makeRoomForMessage(m.getSize())) {
			return DENIED_NO_SPACE; // couldn't fit into buffer -> reject
		}

		return RCV_OK;
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
//			System.out.println("Chat prob: " + chatProb);
//		}else{
//			chatProb = 1;
//		}

		return chatProb;
	}






}
