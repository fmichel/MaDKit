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
package com.distrimind.madkit.kernel.network;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import com.distrimind.madkit.kernel.AgentAddress;
import com.distrimind.madkit.kernel.network.TransferAgent.IDTransfer;
import com.distrimind.madkit.kernel.network.connection.ConnectionProtocol.ConnectionClosedReason;

/**
 * This class represents a message that asks for a connection/disconnection to
 * the given IP address.
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MadkitKanEdition 1.0
 */
public class AskForConnectionMessage extends ConnectionStatusMessage {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1451440654086443752L;

	final boolean now;
	private Object joinedPiece = null;
	private AgentAddress originalSender = null;
	private boolean candidateToBan = false;
	private transient IDTransfer transferID = null;
	private transient AgentAddress indirectAgentAddress = null;

	public AskForConnectionMessage(Type _type, AbstractIP _address) {
		super(_type, _address);
		now = false;
	}

	void chooseIP(boolean enableIPv6) {
		InetAddress ia = null;
		if (enableIPv6)
			ia = getIP().getInetAddress();
		else
			ia = getIP().getInet4Address();

		if (ia != null)
			choosenIP = new InetSocketAddress(ia, getIP().getPort());
		else
			choosenIP = null;
	}

	AskForConnectionMessage(Type _type, AbstractIP _address, InetSocketAddress choosenIP,
			InetSocketAddress _interface_address) {
		this(_type, _address, choosenIP, _interface_address, false);
	}

	AskForConnectionMessage(Type _type, AbstractIP _address, InetSocketAddress choosenIP,
			InetSocketAddress _interface_address, boolean now) {
		super(_type, _address, choosenIP, _interface_address);
		this.now = now;
	}

	AskForConnectionMessage(ConnectionClosedReason reason, AbstractIP _address, InetSocketAddress choosenIP,
			InetSocketAddress _interface_address, boolean now, boolean candidateToBan) {
		super(ConnectionStatusMessage.Type.DISCONNECT, _address, choosenIP, _interface_address, reason);
		this.now = now;
		this.candidateToBan = candidateToBan;
	}

	AskForConnectionMessage(ConnectionClosedReason reason, AbstractIP _address, InetSocketAddress choosenIP,
			InetSocketAddress _interface_address, boolean now, boolean candidateToBan, IDTransfer transferID,
			AgentAddress indirectAgentAddress) {
		this(reason, _address, choosenIP, _interface_address, now, candidateToBan);
		this.transferID = transferID;
		this.indirectAgentAddress = indirectAgentAddress;
	}

	boolean concernsIndirectConnection() {
		return transferID != null && !transferID.equals(TransferAgent.NullIDTransfer);
	}

	AgentAddress getIndirectAgentAddress() {
		return indirectAgentAddress;
	}

	IDTransfer getIDTransfer() {
		return transferID;
	}

	private AskForConnectionMessage(AskForConnectionMessage m) {
		super(m);
		this.now = m.now;
		this.joinedPiece = m.joinedPiece;
		this.originalSender = m.originalSender;

	}

	void setJoinedPiece(Object joinedPiece, AgentAddress originalSender) {
		this.joinedPiece = joinedPiece;
		this.originalSender = originalSender;
	}

	Object getJoinedPiece() {
		return joinedPiece;
	}

	AgentAddress getOriginalSender() {
		return originalSender;
	}

	@Override
	public AskForConnectionMessage clone() {
		return new AskForConnectionMessage(this);
	}

	boolean getCandidateToBan() {
		return candidateToBan;
	}
}
