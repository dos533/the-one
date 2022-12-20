/*
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details.
 */
package interfaces;

import core.NetworkInterface;
import core.Settings;

/**
 * A simple Network Interface that provides a constant bit-rate service, where
 * one transmission can be on at a time.
 */
public class RumourInterface extends SimpleBroadcastInterface {

	/**
	 * Reads the interface settings from the Settings file
	 */
	public RumourInterface(Settings s)	{
		super(s);

//		this.transmitRange = getTransmitRangeGroup();
//		this.transmitSpeed = getTransmitSpeedGroup();
	}

    /**
     * Create transmitRange based on group
	 * @return transmit range
	 */
	public double getTransmitRangeGroup() {
		// TODO : Create Transmit range based on attribute and groupId
		String groupId = this.host.groupId;
		double transmitRange = this.transmitRange;
		return transmitRange;
	}

	/**
	 * Create transmitSpeed based on group
	 * @return transmitSpeed
	 */
	public int getTransmitSpeedGroup() {
		// TODO : Create transmitSpeed based on attribute and groupId
		String groupId = this.host.groupId;
		int transmitSpeed = this.transmitSpeed;
		return transmitSpeed;
	}

	/**
	 * Copy constructor
	 * @param ni the copied network interface object
	 */
	public RumourInterface(RumourInterface ni) {
		super(ni);
	}

	public NetworkInterface replicate()	{
		return new RumourInterface(this);
	}

	/**
	 * Returns a string representation of the object.
	 * @return a string representation of the object.
	 */
	public String toString() {
		return "RumourInterface " + super.toString();
	}

}
