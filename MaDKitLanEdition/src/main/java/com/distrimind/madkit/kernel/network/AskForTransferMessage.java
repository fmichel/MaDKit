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
import com.distrimind.madkit.kernel.KernelAddress;
import com.distrimind.madkit.kernel.Message;
import com.distrimind.madkit.kernel.network.TransferAgent.IDTransfer;
import com.distrimind.madkit.util.ExternalizableAndSizable;

/**
 * This class represents a message that asks for a transfer/disconnection
 * between two given inet socket addresses, or two given kernel addresses. A
 * transfer represents a connection between two peers, with one or more peers
 * between them. The current Madkit kernel represent an intermediate peer, whole
 * only role is to transfer data between two peers. If a secure connection is
 * requested by the final peers, information will be secured, and intermediate
 * peers will not be able to read transfered data.
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MadkitKanEdition 1.0
 */
public class AskForTransferMessage extends Message {

	private final Type type;
	private InetSocketAddress inetSocketAddress1;
	private InetSocketAddress inetSocketAddress2;
	private KernelAddress kernelAddress1;
	private KernelAddress kernelAddress2;
	private ExternalizableAndSizable attachedData;

	/**
	 * Ask for a transfer connection between two inet socket addresses
	 * 
	 * @param type
	 *            the transfer type
	 * @param inetSocketAddress1
	 *            the IP and the port of one peer
	 * @param inetSocketAddress2
	 *            the IP and the port of another peer
	 * @param attachedData
	 *            attached data used to add more information to each peer. Notice
	 *            that this attached data will be transmitted without encryption to
	 *            each intermediate peer.
	 */
	public AskForTransferMessage(Type type, InetSocketAddress inetSocketAddress1, InetSocketAddress inetSocketAddress2,
			ExternalizableAndSizable attachedData) {

		if (type == null)
			throw new NullPointerException("type");
		if (inetSocketAddress1 == null)
			throw new NullPointerException("socketAddress1");
		if (inetSocketAddress2 == null)
			throw new NullPointerException("socketAddress2");
		if (inetSocketAddress1.equals(inetSocketAddress2))
			throw new IllegalArgumentException("socketAddress1 cannot be equals to socketAddress2");
		this.type = type;
		this.inetSocketAddress1 = inetSocketAddress1;
		this.inetSocketAddress2 = inetSocketAddress2;
		this.kernelAddress1 = null;
		this.kernelAddress2 = null;
		this.attachedData = attachedData;
	}

	private AskForTransferMessage(AskForTransferMessage m) {
		super(m);
		this.type = m.type;
		this.inetSocketAddress1 = m.inetSocketAddress1;
		this.inetSocketAddress2 = m.inetSocketAddress2;
		this.kernelAddress1 = m.kernelAddress1;
		this.kernelAddress2 = m.kernelAddress2;
		this.attachedData = m.attachedData;
	}

	@Override
	public String toString() {
		if (inetSocketAddress1 == null)
			return "transfer connection between " + kernelAddress1 + " and " + kernelAddress2;
		else
			return "transfer connection between " + inetSocketAddress1 + " and " + inetSocketAddress2;
	}

	public Type getType() {
		return type;
	}

	public InetSocketAddress getInetSocketAddress1() {
		return inetSocketAddress1;
	}

	public InetSocketAddress getInetSocketAddress2() {
		return inetSocketAddress2;
	}

	public KernelAddress getKernelAddress1() {
		return kernelAddress1;
	}

	public KernelAddress getKernelAddress2() {
		return kernelAddress2;
	}

	boolean needsKernelAddress() {
		return kernelAddress1 == null || kernelAddress2 == null;
	}

	boolean needsInetSocketAddress() {
		return inetSocketAddress1 == null || inetSocketAddress2 == null;
	}

	void setInetSocketAddress1(InetSocketAddress _inetSocketAddress1) {
		inetSocketAddress1 = _inetSocketAddress1;
	}

	void setInetSocketAddress2(InetSocketAddress _inetSocketAddress2) {
		inetSocketAddress2 = _inetSocketAddress2;
	}

	void setKernelAddress1(KernelAddress _kernelAddress1) {
		kernelAddress1 = _kernelAddress1;
	}

	void setKernelAddress2(KernelAddress _kernelAddress2) {
		kernelAddress2 = _kernelAddress2;
	}

	/**
	 * Ask for a transfer connection between two kernel addresses
	 * 
	 * @param type
	 *            the transfer type
	 * @param kernelAddress1
	 *            the kernel address of one peer
	 * @param kernelAddress2
	 *            the kernel address of another peer
	 * @param attachedData
	 *            attached data used to add more information to each peer
	 */
	public AskForTransferMessage(Type type, KernelAddress kernelAddress1, KernelAddress kernelAddress2,
			ExternalizableAndSizable attachedData) {
		if (type == null)
			throw new NullPointerException("type");
		if (kernelAddress1 == null)
			throw new NullPointerException("kernelAddress1");
		if (kernelAddress2 == null)
			throw new NullPointerException("kernelAddress2");
		if (kernelAddress1.equals(kernelAddress2))
			throw new IllegalArgumentException("socketAddress1 cannot be equals to socketAddress2");
		this.type = type;
		this.inetSocketAddress1 = null;
		this.inetSocketAddress2 = null;
		this.kernelAddress1 = kernelAddress1;
		this.kernelAddress2 = kernelAddress2;
		this.attachedData = attachedData;
	}

	public ExternalizableAndSizable getAttachedData() {
		return attachedData;
	}

	@Override
	public Message clone() {
		return new AskForTransferMessage(this);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (o == this)
			return true;
		if (o.getClass() == AskForTransferMessage.class) {
			AskForTransferMessage m = (AskForTransferMessage) o;
			if (!type.equals(m.type))
				return false;
			return isConcernedBy(m);
		}
		return false;
	}

	boolean isConcernedBy(AskForTransferMessage m) {
		int correspondance = 0;
		if (inetSocketAddress1 != null) {
			if (inetSocketAddress1.equals(m.inetSocketAddress1))
				correspondance = 1;
			else if (inetSocketAddress1.equals(m.inetSocketAddress2))
				correspondance = 2;
			else
				return false;

			if (correspondance == 1) {
				if (!inetSocketAddress2.equals(m.inetSocketAddress2))
					return false;
			} else {
				if (!inetSocketAddress2.equals(m.inetSocketAddress1))
					return false;
			}
		} else if (m.inetSocketAddress1 != null)
			return false;

		if (kernelAddress1 != null) {
			if (correspondance == 0) {
				if (kernelAddress1.equals(m.kernelAddress1))
					correspondance = 1;
				else if (kernelAddress1.equals(m.kernelAddress2))
					correspondance = 2;
				else
					return false;
			} else {
				if (correspondance == 1) {
					if (!kernelAddress1.equals(m.kernelAddress1))
						return false;
				} else {
					if (!kernelAddress1.equals(m.kernelAddress2))
						return false;
				}
			}
			if (correspondance == 1) {
				if (!kernelAddress2.equals(m.kernelAddress2))
					return false;
			} else {
				if (!kernelAddress2.equals(m.kernelAddress1))
					return false;
			}
		} else if (m.kernelAddress1 != null)
			return false;

		if (inetSocketAddress2 != null && !inetSocketAddress2.equals(m.inetSocketAddress2))
			return false;
		if (kernelAddress1 != null && !kernelAddress1.equals(m.kernelAddress1))
			return false;
		if (kernelAddress2 != null && !kernelAddress2.equals(m.kernelAddress2))
			return false;

		return true;
	}

	/**
	 * Transfer type
	 * 
	 * @author Jason Mahdjoub
	 *
	 */
	public static enum Type {
		/**
		 * Ask for a transfer connection between the two given peers with the current
		 * peer as intermediate peer.
		 */
		TRANSFER,
		/**
		 * Ask for a direct connection between the two given peers. If it is not
		 * possible, ask for a transfer connection between the two given peers with the
		 * current peer as intermediate peer.
		 */
		TRY_DIRECT_CONNECTION_FIRST_OR_TRANSFER,
		/**
		 * Disconnect the transfer connexion between two given peers.
		 */
		DISCONNECT
	}

	class CandidateForTransfer {
		protected final AgentAddress agentSocket;
		protected final KernelAddress kernelAddress;
		protected final InetSocketAddress inetAddress;
		protected final int numberOfIntermediatePeers;
		protected final ConnectionInfoSystemMessage connectionInfo;

		CandidateForTransfer(AgentAddress agentSocket, KernelAddress kernelAddress, InetSocketAddress inetAddress,
				int numberOfIntermediatePeers, ConnectionInfoSystemMessage connectionInfo) {
			if (agentSocket == null)
				throw new NullPointerException("agentSocket");
			if (kernelAddress == null)
				throw new NullPointerException("kernelAddress");
			this.agentSocket = agentSocket;
			this.kernelAddress = kernelAddress;
			this.inetAddress = inetAddress;
			this.numberOfIntermediatePeers = numberOfIntermediatePeers;
			this.connectionInfo = connectionInfo;
		}

		AskForTransferMessage getOriginalMessage() {
			return AskForTransferMessage.this;
		}

		AgentAddress getAgentAddress() {
			return agentSocket;
		}

		KernelAddress getKernelAddress() {
			return kernelAddress;
		}

		boolean isConcernedBy(AskForTransferMessage m) {
			return AskForTransferMessage.this.equals(m);
		}

		int getNumberOfIntermediatePeers() {
			return numberOfIntermediatePeers;
		}

		InetSocketAddress getInetAddress() {
			return inetAddress;
		}

		InetSocketAddress getInetSocketAddress(InetAddress commingFrom) {
			if (connectionInfo == null) {
				return null;
			} else {
				return connectionInfo.getInetSocketAddress(commingFrom, inetAddress.getAddress());
			}
		}

		@Override
		public String toString() {
			return "InitiateTransferConnection[agentSocket=" + agentSocket + ", kernelAddress=" + kernelAddress
					+ ", inetAddress=" + inetAddress + ", numberOfIntermediatePeers=" + numberOfIntermediatePeers + "]";
		}

	}

	CandidateForTransfer getCandidate(AgentAddress agentSocket, KernelAddress kernelAddress,
			InetSocketAddress inetAddress, int numberOfIntermediatePeers, ConnectionInfoSystemMessage connectionInfo) {
		return new CandidateForTransfer(agentSocket, kernelAddress, inetAddress, numberOfIntermediatePeers,
				connectionInfo);
	}

	InitiateTransferConnection getIntiateConnectionMessage(AgentAddress agentSocket, KernelAddress kernelAddress,
			IDTransfer idTransfer, int numberOfIntermediatePeers, boolean youAskConnection) {
		return new InitiateTransferConnection(agentSocket, kernelAddress, idTransfer, numberOfIntermediatePeers,
				youAskConnection);
	}

	class InitiateTransferConnection extends CandidateForTransfer {
		private final IDTransfer idTransfer;

		private final boolean youAskConnection;

		InitiateTransferConnection(AgentAddress agentSocket, KernelAddress kernelAddress, IDTransfer idTransfer,
				int numberOfIntermediatePeers, boolean youAskConnection) {
			super(agentSocket, kernelAddress, null, numberOfIntermediatePeers, null);
			if (idTransfer == null)
				throw new NullPointerException("idTransfer");
			this.idTransfer = idTransfer;

			this.youAskConnection = youAskConnection;

		}

		IDTransfer getIdTransfer() {
			return idTransfer;
		}

		boolean isYouAskConnection() {
			return youAskConnection;
		}

		@Override
		public String toString() {
			return "InitiateTransferConnection[agentSocket=" + agentSocket + ", kernelAddress=" + kernelAddress
					+ ", inetAddress=" + inetAddress + ", numberOfIntermediatePeers=" + numberOfIntermediatePeers
					+ ", idTransfer=" + idTransfer + "]";
		}

	}

}
