/*
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details.
 */
package routing;

import core.*;

/**
 * Rumuor message router
 * TODO : Model buffer
 * TODO : Model multiple transfers simultaneously (group behavior)
 * with drop-oldest buffer and only single transferring
 * connections at a time.
 */
public class RumourRouter extends EpidemicRouter {

	/**
	 * Constructor. Creates a new message router based on the settings in
	 * the given Settings object.
	 * @param s The settings object
	 */
	public RumourRouter(Settings s) {
		super(s);
		//TODO: read&use epidemic router specific settings (if any)
	}

	/**
	 * Copy constructor.
	 * @param r The router prototype where setting values are copied from
	 */
	protected RumourRouter(RumourRouter r) {
		super(r);
		//TODO: copy epidemic settings here (if any)
	}

	@Override
	public RumourRouter replicate() {
		return new RumourRouter(this);
	}

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

		if (!isFinalRecipient && outgoing!=null) {
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

//		TODO : Do not ignore repetitive messages
//		if ( hasMessage(m.getId()) || isDeliveredMessage(m) || super.isBlacklistedMessage(m.getId())) {
//			return DENIED_OLD; // already seen this message -> reject it
//		}

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





}
