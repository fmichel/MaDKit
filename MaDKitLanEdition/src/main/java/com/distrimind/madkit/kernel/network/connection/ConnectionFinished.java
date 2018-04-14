/*
 * MadKitLanEdition (created by Jason MAHDJOUB (jason.mahdjoub@distri-mind.fr)) Copyright (c)
 * 2015 is a fork of MadKit and MadKitGroupExtension. 
 * 
 * Copyright or © or Copr. Jason Mahdjoub, Fabien Michel, Olivier Gutknecht, Jacques Ferber (1997)
 * 
 * jason.mahdjoub@distri-mind.fr
 * fmichel@lirmm.fr
 * olg@no-distance.net
 * ferber@lirmm.fr
 * 
 * This software is a computer program whose purpose is to
 * provide a lightweight Java library for designing and simulating Multi-Agent Systems (MAS).
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 * 
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package com.distrimind.madkit.kernel.network.connection;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;

import com.distrimind.madkit.kernel.network.connection.ConnectionProtocol.ConnectionClosedReason;
import com.distrimind.madkit.kernel.network.connection.ConnectionProtocol.ConnectionState;

/**
 * Message to tells that the connection protocol was terminated.
 * 
 * @author Jason Mahdjoub
 * @version 1.1
 * @since MadkitLanEdition 1.0
 *
 */
public class ConnectionFinished extends ConnectionMessage {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7511529487957825982L;

	private final InetSocketAddress inet_address;

	private final ConnectionProtocol.ConnectionState state;
	private final byte[] initialCounter;
	private transient final ConnectionClosedReason connection_closed_reason;

	public ConnectionFinished(InetSocketAddress _inet_address, byte[] initialCounter) {
		inet_address = _inet_address;
		state = ConnectionState.CONNECTION_ESTABLISHED;
		connection_closed_reason = null;
		this.initialCounter=initialCounter;
	}

	public ConnectionFinished(InetSocketAddress _inet_address, ConnectionClosedReason _connection_closed_reason) {
		inet_address = _inet_address;

		connection_closed_reason = _connection_closed_reason;
		switch (connection_closed_reason) {
		case CONNECTION_ANOMALY:
		case CONNECTION_LOST:
			state = ConnectionState.CONNECTION_ABORDED;
			break;
		case CONNECTION_PROPERLY_CLOSED:
			state = ConnectionState.CONNECTION_CLOSED;
			break;
		default:
			state = null;
		}
		this.initialCounter=null;
	}

	public ConnectionProtocol.ConnectionState getState() {
		return state;
	}

	public InetSocketAddress getInetSocketAddress() {
		return inet_address;
	}

	public ConnectionClosedReason getConnectionClosedReason() {
		return connection_closed_reason;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Integrity checkDataIntegrity() {
		if (inet_address == null || state == null)
			return Integrity.FAIL_AND_CANDIDATE_TO_BAN;
		if (initialCounter!=null && initialCounter.length>Short.MAX_VALUE)
			return Integrity.FAIL;
		return Integrity.OK;
	}

	@Override
	public boolean excludedFromEncryption() {
		return false;
	}

	public byte[] getInitialCounter()
	{
		return initialCounter;
	}
	
	private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		readAndCheckObject(in);
	}
	private void writeObject(final ObjectOutputStream oos) throws IOException
	{
		writeAndCheckObject(oos);
	}
}
