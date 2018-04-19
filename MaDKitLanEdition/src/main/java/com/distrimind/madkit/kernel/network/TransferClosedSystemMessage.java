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

import com.distrimind.madkit.exceptions.MessageSerializationException;
import com.distrimind.madkit.kernel.KernelAddress;
import com.distrimind.madkit.kernel.network.TransferAgent.IDTransfer;

/**
 * 
 * @author Jason Mahdjoub
 * @version 1.1
 * @since MadkitLanEdition 1.0
 */
class TransferClosedSystemMessage extends BroadcastableSystemMessage {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7387851356039905328L;

	private IDTransfer idTransfer;
	private boolean lastPass;

	TransferClosedSystemMessage(IDTransfer _idTransferDestination, KernelAddress _kernelAddressDestination,
			IDTransfer idTransfer, boolean lastPass) {
		super(_idTransferDestination, _kernelAddressDestination);
		if (idTransfer == null)
			throw new NullPointerException("idTransfer");
		if (idTransfer.equals(TransferAgent.NullIDTransfer))
			throw new IllegalArgumentException("idTransfer cannot be equals to TransferAgent.NullIDTransfer");
		this.idTransfer = idTransfer;
		this.lastPass = lastPass;
		
	}
	@Override
	public int getInternalSerializedSize() {
		
		return super.getInternalSerializedSize()+idTransfer.getInternalSerializedSize()+1;
	}


	@Override
	public void readAndCheckObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		super.readAndCheckObject(in);
		Object o=in.readObject();
		if (!(o instanceof IDTransfer))
			throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN);
		idTransfer=(IDTransfer)o;
		lastPass=in.readBoolean();
		if (idTransfer.equals(TransferAgent.NullIDTransfer))
			throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN);
	}

	@Override
	public void writeAndCheckObject(ObjectOutputStream oos) throws IOException {
		super.writeAndCheckObject(oos);
		oos.writeObject(idTransfer);
		oos.writeBoolean(lastPass);
	}
	boolean isLastPass() {
		return lastPass;
	}

	IDTransfer getIdTransfer() {
		return idTransfer;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[idTransferDestination=" + getIdTransferDestination()
				+ ", kernelAddressDestination=" + getKernelAddressDestination() + ", idTransfer=" + idTransfer + "]";
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
