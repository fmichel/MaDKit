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
package com.distrimind.madkit.kernel;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.distrimind.madkit.exceptions.MessageSerializationException;
import com.distrimind.madkit.kernel.network.SystemMessage.Integrity;
import com.distrimind.madkit.util.OOSUtils;
import com.distrimind.madkit.util.SerializableAndSizable;

/**
 * Gives the result of a big data transfer
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MadkitLanEdition 1.0
 */
public final class BigDataResultMessage extends Message implements SerializableAndSizable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5848565025339803364L;

	private long transferedData;
	private Type type;
	private int idPacket;
	private long duration;

	@Override
	public int getInternalSerializedSize() {
		return super.getInternalSerializedSizeImpl()+22+(type.name().length()*2);
	}
	
	@Override
	protected void readAndCheckObject(final ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		super.readAndCheckObjectImpl(in);
		
			
		transferedData=in.readLong();
		type=Type.valueOf(OOSUtils.readString(in, 1000, false));
		if (type==null)
			throw new MessageSerializationException(Integrity.FAIL);
		idPacket=in.readInt();
		duration=in.readLong();
		
	}
	@Override
	protected void writeAndCheckObject(final ObjectOutputStream oos) throws IOException{
		super.writeAndCheckObjectImpl(oos);
		oos.writeLong(transferedData);
		OOSUtils.writeString(oos, type.name(), 1000, false);
		oos.writeInt(idPacket);
		oos.writeLong(duration);
	}	
	
	
	BigDataResultMessage(Type type, long transferedData, int idPacket, long duration) {
		if (type == null)
			throw new NullPointerException("type");
		if (type == Type.BIG_DATA_TRANSFER_DENIED && transferedData != 0)
			throw new IllegalArgumentException("transferedData must be equal to 0");
		this.type = type;
		this.transferedData = transferedData;
		this.idPacket = idPacket;
		this.duration = duration;
	}

	/**
	 * Gest the transfer duration in milliseconds
	 * 
	 * @return the transfer duration in milliseconds
	 */
	public long getTransferDuration() {
		return duration;
	}

	/**
	 * Gets the result type
	 * 
	 * @return the result type
	 * @see Type
	 */
	public Type getType() {
		return type;
	}

	/**
	 * Gets the total data transfered in bytes
	 * 
	 * @return the total data transfered in bytes
	 */
	public long getTransferedDataLength() {
		return transferedData;
	}

	int getIDPacket() {
		return idPacket;
	}

	public enum Type {
		/**
		 * The big data was entirely transfered.
		 */
		BIG_DATA_TRANSFERED,
		/**
		 * when a problem occurs during the data writing into the distant peer. The
		 * associated transfered data length is precise and exact.
		 */
		BIG_DATA_PARTIALLY_TRANSFERED,

		/**
		 * when the received data was invalid or if hash tag was invalid.
		 */
		BIG_DATA_CORRUPTED,

		/**
		 * when the connection is lost. The associated transfered data length is
		 * approximate.
		 */
		CONNECTION_LOST,

		/**
		 * The proposed big data transfer is rejected
		 */
		BIG_DATA_TRANSFER_DENIED
	}
	
	public String toString()
	{
		return "BigDataResultMessage[type="+type+", dataTransferedInBytes="+getTransferedDataLength()+", durationInMs="+getTransferDuration()+"]";
	}
}
