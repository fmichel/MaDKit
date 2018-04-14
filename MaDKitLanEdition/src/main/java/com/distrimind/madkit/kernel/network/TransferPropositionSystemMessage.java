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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import com.distrimind.madkit.kernel.KernelAddress;
import com.distrimind.madkit.kernel.network.TransferAgent.IDTransfer;

/**
 * 
 * @author Jason Mahdjoub
 * @version 1.1
 * @since MadkitLanEdition 1.0
 */
class TransferPropositionSystemMessage extends BroadcastableSystemMessage {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5392984162781863410L;

	private final KernelAddress kernelAddressToConnect;
	private final Serializable attachedData;
	private final IDTransfer idTransfer;
	private final int numberOfIntermediatePeers;
	private boolean finalTestResult = true;
	private final boolean youAskConnection;

	TransferPropositionSystemMessage(IDTransfer idTransferDestinationUsedForBroadcast, IDTransfer idTransfer,
			KernelAddress kernelAddressToConnect, KernelAddress kernelAddressDestination, int numberOfIntermediatePeers,
			Serializable attachedData, boolean youAskConnection) {
		super(idTransferDestinationUsedForBroadcast, kernelAddressDestination);
		if (idTransfer == null)
			throw new NullPointerException("idTransfer");
		if (idTransfer.equals(TransferAgent.NullIDTransfer))
			throw new IllegalArgumentException("idTransfer cannot be equals to TransferAgent.NullIDTransfer");
		if (kernelAddressToConnect == null)
			throw new NullPointerException("kernelAddressToConnect");

		this.idTransfer = idTransfer;
		this.kernelAddressToConnect = kernelAddressToConnect;
		this.attachedData = attachedData;
		this.numberOfIntermediatePeers = numberOfIntermediatePeers;
		this.youAskConnection = youAskConnection;
	}

	void addIntermediateTestResult(boolean intermediateTestResult) {
		this.finalTestResult &= intermediateTestResult;
	}

	boolean getFinalTestResult() {
		return finalTestResult;
	}

	int getNumberOfIntermediatePeers() {
		return numberOfIntermediatePeers;
	}

	IDTransfer getIdTransfer() {
		return idTransfer;
	}

	KernelAddress getKernelAddressToConnect() {
		return kernelAddressToConnect;
	}

	Serializable getAttachedDataForConnection() {
		return attachedData;
	}

	@Override
	public Integrity checkDataIntegrity() {
		Integrity i = super.checkDataIntegrity();
		if (i != Integrity.OK)
			return i;
		if (kernelAddressToConnect == null)
			return Integrity.FAIL;
		i = kernelAddressToConnect.checkDataIntegrity();
		if (i != Integrity.OK)
			return i;
		if (idTransfer == null)
			return Integrity.FAIL;
		i = idTransfer.checkDataIntegrity();
		if (i != Integrity.OK)
			return i;
		if (idTransfer.equals(TransferAgent.NullIDTransfer))
			return Integrity.FAIL;
		if (numberOfIntermediatePeers < 0)
			return Integrity.FAIL;

		return Integrity.OK;
	}

	boolean isYouAskConnection() {
		return youAskConnection;
	}

	@Override
	public boolean excludedFromEncryption() {
		return false;
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
