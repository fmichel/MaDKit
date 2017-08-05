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
package com.distrimind.madkit.message.hook;

import com.distrimind.madkit.kernel.network.AskForTransferMessage;

/**
 * This hook message is generated when a transfer connection is
 * established/unestablished between two peers and the current peer.
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MadkitLanEdition 1.0
 * @see #getEventType()
 * @see TransferEventType
 */
public class TransferEventMessage extends HookMessage {
	/**
	 * 
	 */
	private static final long serialVersionUID = -272409605979165451L;

	private final TransferEventType eventType;
	private final AskForTransferMessage originalMessage;
	private final int idTransfer;

	public TransferEventMessage(int idTransfer, AskForTransferMessage originalMessage, TransferEventType eventType) {
		super(AgentActionEvent.TRANFER_CONNEXION_EVENT);
		this.originalMessage = originalMessage;
		this.eventType = eventType;
		this.idTransfer = idTransfer;
	}

	public int getIDTransfer() {
		return idTransfer;
	}

	public TransferEventType getEventType() {
		return eventType;
	}

	public AskForTransferMessage getOriginalMessage() {
		return originalMessage;
	}

	@Override
	public String toString() {
		return "TransferEventMessage[" + eventType + ", idTransfer=" + idTransfer + "]";
	}

	/**
	 * Transfer connection event type
	 * 
	 * @author Jason Mahdjoub
	 * @version 1.0
	 * @since MadKitLanEdition 1.0
	 *
	 */
	public static enum TransferEventType {
		TRANSFER_EFFECTIVE, DIRECT_CONNECTION_EFFECTIVE, TRANSFER_DISCONNECTED, TRANSFER_DISCONNECTED_BUT_TRYING_RECONNECTION, CONNECTION_IMPOSSIBLE_BECAUSE_TOO_MUCH_CANDIDATES, CONNECTION_IMPOSSIBLE_BECAUSE_NOT_ENOUGH_CANDIDATES, CONNECTION_IMPOSSIBLE_BECAUSE_TIME_ELAPSED, CONNECTION_IMPOSSIBLE
	}

}
