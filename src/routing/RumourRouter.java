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




}
