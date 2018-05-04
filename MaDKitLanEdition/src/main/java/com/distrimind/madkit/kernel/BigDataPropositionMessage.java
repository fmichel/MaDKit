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
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import com.distrimind.madkit.exceptions.MessageSerializationException;
import com.distrimind.madkit.io.RandomInputStream;
import com.distrimind.madkit.io.RandomOutputStream;
import com.distrimind.madkit.kernel.network.Block;
import com.distrimind.madkit.kernel.network.SystemMessage.Integrity;
import com.distrimind.madkit.kernel.network.RealTimeTransfertStat;
import com.distrimind.madkit.util.SerializationTools;
import com.distrimind.madkit.util.ExternalizableAndSizable;
import com.distrimind.util.crypto.MessageDigestType;

/**
 * Message received when a big data transfer is requested.
 * 
 * By calling the function
 * {@link BigDataPropositionMessage#acceptTransfer(com.distrimind.madkit.io.RandomOutputStream)},
 * the transfer will be able to begin.
 * 
 * By calling the function {@link BigDataPropositionMessage#denyTransfer()}, the
 * transfer will rejected.
 * 
 * @author Jason Mahdjoub
 * @version 1.2
 * @since MadkitLanEdition 1.0
 * 
 * @see AbstractAgent#sendBigDataWithRole(AgentAddress, RandomInputStream, long, long, ExternalizableAndSizable, MessageDigestType, String, boolean)
 * @see BigDataResultMessage
 */
public final class BigDataPropositionMessage extends Message implements ExternalizableAndSizable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1785811403975318464L;

	protected static final int maxBufferSize = 1024 * 1024;

	protected final transient RandomInputStream inputStream;
	protected transient RandomOutputStream outputStream = null;
	private transient RealTimeTransfertStat stat = null;
	protected long pos;
	protected long length;
	private ExternalizableAndSizable attachedData;
	private byte[] data;
	private boolean isLocal;
	protected int idPacket;
	protected long timeUTC;
	private MessageDigestType messageDigestType;
	private boolean excludedFromEncryption;
	
	@Override
	public int getInternalSerializedSize() {
		return super.getInternalSerializedSizeImpl()+37+(attachedData==null?0:attachedData.getInternalSerializedSize())+(data==null?0:data.length)+(messageDigestType==null?0:messageDigestType.name().length()*2);
	}
	
	@Override
	public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException
	{
		super.readExternal(in);
		pos=in.readLong();
		length=in.readLong();
		Object o=SerializationTools.readExternalizableAndSizable(in, true);
		if (o!=null && !(o instanceof ExternalizableAndSizable))
			throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN);
		attachedData=((ExternalizableAndSizable)o);

		data=SerializationTools.readBytes(in, Block.BLOCK_SIZE_LIMIT, true);
		isLocal=in.readBoolean();
		idPacket=in.readInt();
		timeUTC=in.readLong();
		String s=SerializationTools.readString(in, 1000, true);
		if (s==null)
			messageDigestType=null;
		else 
		{	
			messageDigestType=MessageDigestType.valueOf(s);
			if (messageDigestType==null)
				throw new MessageSerializationException(Integrity.FAIL);
		}
		
		excludedFromEncryption=in.readBoolean();
			
		
	}
	@Override
	public void writeExternal(final ObjectOutput oos) throws IOException{
		super.writeExternal(oos);
		oos.writeLong(pos);
		oos.writeLong(length);
		
		SerializationTools.writeExternalizableAndSizable(oos, attachedData, true);
		
		SerializationTools.writeBytes(oos, data, Block.BLOCK_SIZE_LIMIT, true);
		oos.writeBoolean(isLocal);
		oos.writeInt(idPacket);
		oos.writeLong(timeUTC);
		SerializationTools.writeString(oos, messageDigestType==null?null:messageDigestType.name(), 1000, true);
		
		oos.writeBoolean(excludedFromEncryption);
	}	
	
	
	
	BigDataPropositionMessage(RandomInputStream stream, long pos, long length, ExternalizableAndSizable attachedData, boolean local,
			int maxBufferSize, RealTimeTransfertStat stat, MessageDigestType messageDigestType, boolean excludedFromEncryption) throws IOException {
		if (stream == null)
			throw new NullPointerException("stream");
		if (pos >= stream.length())
			throw new IllegalArgumentException("pos must be lower than stream.length()");
		if (length > stream.length() - pos)
			throw new IllegalArgumentException("length cannot be greater than stream.length()-pos");
		if (maxBufferSize>Block.BLOCK_SIZE_LIMIT)
			throw new IllegalArgumentException();
		
		this.pos = pos;
		this.length = length;
		this.attachedData = attachedData;
		if (local || maxBufferSize < stream.length()) {
			this.inputStream = stream;
			this.data = null;
		} else {
			this.inputStream = null;
			this.data = new byte[(int) stream.length()];
			stream.read(this.data);
		}
		this.isLocal = local;
		this.stat = stat;
		timeUTC = System.currentTimeMillis();
		this.messageDigestType = messageDigestType;
		this.excludedFromEncryption=excludedFromEncryption;
	}

	public boolean bigDataExcludedFromEncryption()
	{
		return excludedFromEncryption;
	}
	
	/**
	 * Gets the user customized data attached to this big data transfer proposition
	 * 
	 * @return the user customized data attached to this big data transfer
	 *         proposition, or null
	 */
	public Serializable getAttachedData() {
		return attachedData;
	}

	/**
	 * Tells if the transfer was done locally with the same MadkitKernel
	 * 
	 * @return true if the transfer was done locally with the same MadkitKernel,
	 *         false else.
	 */
	public boolean isLocal() {
		return isLocal;
	}

	/**
	 * 
	 * @return the start position of the source stream
	 */
	public long getStartStreamPosition() {
		return pos;
	}

	/**
	 * 
	 * @return the length in bytes of the data to transfer.
	 */
	public long getTransferLength() {
		return length;
	}

	/**
	 * Gets statistics in bytes per seconds related to the concerned big data
	 * transfer
	 * 
	 * @return statistics in bytes per seconds related to the concerned big data
	 *         transfer
	 */
	public RealTimeTransfertStat getStatistics() {
		if (stat == null) {
			final AbstractAgent receiver = getReceiver().getAgent();
			stat = new RealTimeTransfertStat(receiver.getMadkitConfig().networkProperties.bigDataStatDurationMean,
					receiver.getMadkitConfig().networkProperties.bigDataStatDurationMean / 10);
		}
		return stat;

	}

	/**
	 * Accept the transfer A message {@link BigDataResultMessage} is sent in return
	 * to the agent asking for the transfer, to inform him of the transfer result
	 * (see {@link BigDataResultMessage.Type}).
	 * 
	 * @param outputStream
	 *            the output stream to use during the transfer
	 * @throws InterruptedException if the current thread is interrupted
	 */
	public void acceptTransfer(final RandomOutputStream outputStream) throws InterruptedException {
		if (outputStream == null)
			throw new NullPointerException("outputStream");
		final AbstractAgent receiver = getReceiver().getAgent();
		this.outputStream = outputStream;
		if (isLocal()) {
			try {

				receiver.scheduleTask(new Task<>(new Callable<Void>() {

					@Override
					public Void call() throws Exception {
						long remaining = length;
						try {
							byte buffer[] = new byte[(int) Math.min(length, (long) maxBufferSize)];
							inputStream.seek(pos);
							outputStream.setLength(length);
							while (remaining > 0) {
								int s = (int) Math.min(buffer.length, remaining);
								inputStream.read(buffer, 0, s);
								outputStream.write(buffer, 0, s);
								remaining -= s;
							}
						} catch (Exception e) {
							sendBidirectionalReply(BigDataResultMessage.Type.BIG_DATA_PARTIALLY_TRANSFERED,
									length - remaining);
							throw e;
						}
						sendBidirectionalReply(BigDataResultMessage.Type.BIG_DATA_TRANSFERED, length);
						return null;
					}
				})).waitTaskFinished();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}

		} else {
			if (data != null) {
				try {
					outputStream.write(data);
				} catch (IOException e) {
					sendBidirectionalReply(BigDataResultMessage.Type.BIG_DATA_PARTIALLY_TRANSFERED, 0);
				}
				sendBidirectionalReply(BigDataResultMessage.Type.BIG_DATA_TRANSFERED, length);
			} else {

				receiver.getKernel().acceptDistantBigDataTransfer(receiver, this);
			}
		}
	}

	/**
	 * 
	 * @return the message digest type used for check the validity of the transfered
	 *         data
	 */
	public MessageDigestType getMessageDigestType() {
		return messageDigestType;
	}

	/**
	 * Reject the transfer A message {@link BigDataResultMessage} is sent in return
	 * to the agent asking for the transfer, to inform him of the transfer result
	 * (see {@link BigDataResultMessage.Type}).
	 * 
	 * @throws InterruptedException if the current thread is interrupted
	 */
	public void denyTransfer() throws InterruptedException {
		final AbstractAgent receiver = getReceiver().getAgent();
		try {
			receiver.scheduleTask(new Task<>(new Callable<Void>() {

				@Override
				public Void call() throws Exception {
					if (receiver.isAlive()) {
						Message m = new BigDataResultMessage(BigDataResultMessage.Type.BIG_DATA_TRANSFER_DENIED, 0,
								idPacket, System.currentTimeMillis() - timeUTC);
						m.setIDFrom(BigDataPropositionMessage.this);
						receiver.sendMessage(getSender(), m);
					}
					return null;
				}
			})).waitTaskFinished();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}

	}

	void connectionLost(long dataTransfered) {
		sendBidirectionalReply(BigDataResultMessage.Type.BIG_DATA_PARTIALLY_TRANSFERED, dataTransfered);
	}

	void dataCorrupted(long dataTransfered) {
		sendBidirectionalReply(BigDataResultMessage.Type.BIG_DATA_CORRUPTED, dataTransfered);
	}

	void transferCompleted(long dataTransfered) {
		sendBidirectionalReply(BigDataResultMessage.Type.BIG_DATA_TRANSFERED, dataTransfered);
	}

	protected void sendBidirectionalReply(final BigDataResultMessage.Type type, final long length) {
		final AbstractAgent receiver = getReceiver().getAgent();

		receiver.scheduleTask(new Task<>(new Callable<Void>() {

			@Override
			public Void call() throws Exception {
				Message m = new BigDataResultMessage(type, length, idPacket, System.currentTimeMillis() - timeUTC);
				m.setIDFrom(BigDataPropositionMessage.this);
				receiver.sendMessage(getSender(), m);
				return null;
			}
		}));

		Message m = new BigDataResultMessage(type, length, idPacket, System.currentTimeMillis() - timeUTC);
		m.setReceiver(getReceiver());
		m.setSender(getSender());
		m.setIDFrom(BigDataPropositionMessage.this);
		receiver.receiveMessage(m);
	}

	RandomInputStream getInputStream() {
		return inputStream;
	}

	void setIDPacket(int idPacket) {
		this.idPacket = idPacket;
	}

	int getIDPacket() {
		return idPacket;
	}

	RandomOutputStream getOutputStream() {
		return outputStream;
	}
	
	
}
