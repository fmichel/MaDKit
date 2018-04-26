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
package com.distrimind.madkit.kernel.network.connection.unsecured;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;

import com.distrimind.madkit.exceptions.BlockParserException;
import com.distrimind.madkit.exceptions.ConnectionException;
import com.distrimind.madkit.exceptions.MessageSerializationException;
import com.distrimind.madkit.kernel.MadkitProperties;
import com.distrimind.madkit.kernel.network.NetworkProperties;
import com.distrimind.madkit.kernel.network.PacketCounter;
import com.distrimind.madkit.kernel.network.SubBlock;
import com.distrimind.madkit.kernel.network.SubBlockInfo;
import com.distrimind.madkit.kernel.network.SubBlockParser;
import com.distrimind.madkit.kernel.network.SystemMessage.Integrity;
import com.distrimind.madkit.kernel.network.connection.AskConnection;
import com.distrimind.madkit.kernel.network.connection.ConnectionFinished;
import com.distrimind.madkit.kernel.network.connection.ConnectionMessage;
import com.distrimind.madkit.kernel.network.connection.ConnectionProtocol;
import com.distrimind.madkit.kernel.network.connection.TransferedBlockChecker;
import com.distrimind.madkit.kernel.network.connection.UnexpectedMessage;
import com.distrimind.madkit.util.OOSUtils;
import com.distrimind.ood.database.DatabaseWrapper;
import com.distrimind.util.crypto.AbstractMessageDigest;
import com.distrimind.util.crypto.MessageDigestType;

/**
 * Represents a connection protocol that check the data validity thanks to a
 * message digest
 * 
 * 
 * @author Jason Mahdjoub
 * @version 1.3
 * @since MadkitLanEdition 1.0
 */
public class CheckSumConnectionProtocol extends ConnectionProtocol<CheckSumConnectionProtocol> {
	private final Parser parser;
	private boolean connected = false;
	protected final AbstractMessageDigest messageDigest;
	protected final MessageDigestType messageDigestType;
	private final NullPacketCounter packetCounter=new NullPacketCounter();

	private CheckSumConnectionProtocol(InetSocketAddress _distant_inet_address,
			InetSocketAddress _local_interface_address, ConnectionProtocol<?> _subProtocol,
			DatabaseWrapper _sql_connection, MadkitProperties mkProperties, NetworkProperties _properties, int subProtocolLevel, boolean isServer,
			boolean mustSupportBidirectionnalConnectionInitiative) throws ConnectionException {
		super(_distant_inet_address, _local_interface_address, _subProtocol, _sql_connection, _properties,
				subProtocolLevel, isServer, mustSupportBidirectionnalConnectionInitiative);
		parser = new Parser();
		CheckSumConnectionProtocolProperties p = (CheckSumConnectionProtocolProperties) super.connection_protocol_properties;
		p.checkProperties();
		messageDigestType = p.messageDigestType;
		try {
			messageDigest = messageDigestType.getMessageDigestInstance();
		} catch (Exception e) {
			throw new ConnectionException(e);
		}
	}

	@Override
	public boolean needsMadkitLanEditionDatabase() {
		return false;
	}

	@Override
	public boolean isCrypted() {
		return false;
	}

	@Override
	protected ConnectionMessage getNextStep(ConnectionMessage _m) {
		if (!connected) {
			if (_m instanceof AskConnection) {
				if (((AskConnection) _m).isYouAreAsking())
					return new AskConnection(false);
				else {
					connected = true;
					return new ConnectionFinished(getDistantInetSocketAddress(), (byte[])null);
				}
			} else if (_m instanceof ConnectionFinished) {
				ConnectionFinished cf = (ConnectionFinished) _m;
				if (!cf.getState().equals(ConnectionProtocol.ConnectionState.CONNECTION_ESTABLISHED)) {
					if (cf.getState().equals(ConnectionProtocol.ConnectionState.CONNECTION_CLOSED)) {
						return new ConnectionFinished(this.getDistantInetSocketAddress(),
								ConnectionClosedReason.CONNECTION_PROPERLY_CLOSED);
					} else {
						return new ConnectionFinished(this.getDistantInetSocketAddress(),
								ConnectionClosedReason.CONNECTION_LOST);
					}
				} else {
					connected = true;
					return new ConnectionFinished(getDistantInetSocketAddress(), (byte[])null);
				}
			} else {
				return new UnexpectedMessage(getDistantInetSocketAddress());
			}

		} else {
			if (_m instanceof ConnectionFinished) {
				ConnectionFinished cf = (ConnectionFinished) _m;
				if (!cf.getState().equals(ConnectionProtocol.ConnectionState.CONNECTION_ESTABLISHED)) {
					if (cf.getState().equals(ConnectionProtocol.ConnectionState.CONNECTION_CLOSED)) {
						return new ConnectionFinished(this.getDistantInetSocketAddress(),
								ConnectionClosedReason.CONNECTION_PROPERLY_CLOSED);
					} else {
						return new ConnectionFinished(this.getDistantInetSocketAddress(),
								ConnectionClosedReason.CONNECTION_LOST);
					}
				}
				return null;
			} else
				return new UnexpectedMessage(getDistantInetSocketAddress());
		}
	}

	@Override
	protected void closeConnection(ConnectionClosedReason _reason) {
		connected = false;
	}

	@Override
	public SubBlockParser getParser() {
		return parser;
	}

	@Override
	public TransferedBlockChecker getTransferedBlockChecker(TransferedBlockChecker subBlockChercker)
			throws ConnectionException {
		return new BlockChecker(subBlockChercker, messageDigestType);
	}

	class Parser extends SubBlockParser {

		@Override
		public SubBlockInfo getSubBlock(SubBlock _block) throws BlockParserException {
			int sizeHead = getSizeHead();
			SubBlock res = new SubBlock(_block.getBytes(), _block.getOffset() + sizeHead, _block.getSize() - sizeHead);
			messageDigest.reset();
			messageDigest.update(res.getBytes(), res.getOffset(), res.getSize());
			byte[] digest = messageDigest.digest();
			for (int i = 0; i < sizeHead; i++) {
				if (digest[i] != _block.getBytes()[i + _block.getOffset()])
					return new SubBlockInfo(res, false, false);
			}
			return new SubBlockInfo(res, true, false);
		}

		@Override
		public SubBlock getParentBlock(SubBlock _block, boolean excludeFromEncryption) throws BlockParserException {
			try {
				int outputSize=getBodyOutputSizeForEncryption(_block.getSize());
				SubBlock res= new SubBlock(_block.getBytes(), _block.getOffset() - getSizeHead(),
						outputSize + getSizeHead());
				
				messageDigest.reset();
				messageDigest.update(res.getBytes(), _block.getOffset(), _block.getSize());
				messageDigest.digest(res.getBytes(), res.getOffset(), getSizeHead());
				return res;
			} catch (Exception e) {
				throw new BlockParserException(e);
			}
		}

		@Override
		public int getSizeHead() {
			return messageDigest.getDigestLength();
		}

		@Override
		public int getBodyOutputSizeForEncryption(int _size) {
			return _size;
		}

		@Override
		public int getBodyOutputSizeForDecryption(int _size) {
			return _size;
		}

		@Override
		public int getMaximumSizeHead() {
			return getSizeHead();
		}

		@Override
		public SubBlockInfo checkEntrantPointToPointTransferedBlock(SubBlock _block) throws BlockParserException {
			int sizeHead = getSizeHead();
			SubBlock res = new SubBlock(_block.getBytes(), _block.getOffset() + sizeHead, _block.getSize() - sizeHead);
			messageDigest.reset();
			messageDigest.update(res.getBytes(), res.getOffset(), res.getSize());
			byte[] digest = messageDigest.digest();
			for (int i = 0; i < sizeHead; i++) {
				if (digest[i] != _block.getBytes()[i + _block.getOffset()])
					return new SubBlockInfo(res, false, false);
			}
			return new SubBlockInfo(res, true, false);
		}

		@Override
		public SubBlock signIfPossibleSortantPointToPointTransferedBlock(SubBlock _block) throws BlockParserException {
			try {
				SubBlock res = new SubBlock(_block.getBytes(), _block.getOffset() - getSizeHead(),
						_block.getSize() + getSizeHead());
				messageDigest.reset();
				messageDigest.update(res.getBytes(), _block.getOffset(), _block.getSize());
				messageDigest.digest(res.getBytes(), res.getOffset(), getSizeHead());
				return res;
			} catch (Exception e) {
				throw new BlockParserException(e);
			}
		}

	}

	private static class BlockChecker extends TransferedBlockChecker {

		/**
		 * 
		 */
		private static final long serialVersionUID = 5028832920052128043L;
		private MessageDigestType messageDigestType;
		private transient AbstractMessageDigest messageDigest = null;

		@Override
		public int getInternalSerializedSize() {
			return OOSUtils.getInternalSize(messageDigestType, 0);
		}

		@Override
		public void readAndCheckObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
			Enum<?> e=OOSUtils.readEnum(in, false);
			if (!(e instanceof MessageDigestType))
				throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN);
			messageDigestType=(MessageDigestType)e;
			try
			{
				messageDigest = messageDigestType.getMessageDigestInstance();
			}
			catch(Exception e2)
			{
				throw new MessageSerializationException(Integrity.FAIL);
			}
			
			
		}

		@Override
		public void writeAndCheckObject(ObjectOutputStream oos) throws IOException {
			OOSUtils.writeEnum(oos, messageDigestType, false);
			
		}
		
		protected BlockChecker(TransferedBlockChecker _subChecker, MessageDigestType messageDigestType)
				throws ConnectionException {
			super(_subChecker, true);
			if (messageDigestType == null)
				throw new NullPointerException("messageDigestType");
			this.messageDigestType = messageDigestType;
			try {
				messageDigest = messageDigestType.getMessageDigestInstance();
			} catch (Exception e) {
				throw new ConnectionException(e);
			}
		}

		

		@Override
		public SubBlockInfo checkSubBlock(SubBlock _block) throws BlockParserException {
			messageDigest.reset();
			
			int dl = messageDigest.getDigestLength();
			SubBlock res = new SubBlock(_block.getBytes(), _block.getOffset() + dl, _block.getSize() - dl);
			messageDigest.update(res.getBytes(), res.getOffset(), res.getSize());
			byte[] digest = messageDigest.digest();
			for (int i = 0; i < dl; i++) {
				if (digest[i] != _block.getBytes()[i + _block.getOffset()])
					return new SubBlockInfo(res, false, false);
			}

			return new SubBlockInfo(res, true, false);
		}


		

	}

	@Override
	public boolean isTransferBlockCheckerChangedImpl() {
		return false;
	}
	@Override
	public PacketCounter getPacketCounter() {
		return packetCounter;
	}

}
