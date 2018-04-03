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

import java.net.InetSocketAddress;

import com.distrimind.madkit.exceptions.BlockParserException;
import com.distrimind.madkit.exceptions.ConnectionException;
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
import com.distrimind.ood.database.DatabaseWrapper;

/**
 * Represents a connection protocol that do not have any security management and
 * any cryptographic algorithm.
 * 
 * @author Jason Mahdjoub
 * @version 1.2
 * @since MadkitLanEdition 1.0
 */
public class UnsecuredConnectionProtocol extends ConnectionProtocol<UnsecuredConnectionProtocol> {
	private final Parser parser;
	private boolean connected = false;
	private final NullPacketCounter packetCounter=new NullPacketCounter();

	private UnsecuredConnectionProtocol(InetSocketAddress _distant_inet_address,
			InetSocketAddress _local_interface_address, ConnectionProtocol<?> _subProtocol,
			DatabaseWrapper _sql_connection, MadkitProperties mkProperties, NetworkProperties _properties, int subProtocolLevel, boolean isServer,
			boolean mustSupportBidirectionnalConnectionInitiative) throws ConnectionException {
		super(_distant_inet_address, _local_interface_address, _subProtocol, _sql_connection, _properties,
				subProtocolLevel, isServer, mustSupportBidirectionnalConnectionInitiative);
		parser = new Parser();
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
			} else {
				return new UnexpectedMessage(getDistantInetSocketAddress());
			}
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
	public TransferedBlockChecker getTransferedBlockChecker(TransferedBlockChecker subBlockChercker) {
		return new BlockChecker(subBlockChercker);
	}

	static class Parser extends SubBlockParser {

		@Override
		public SubBlockInfo getSubBlock(SubBlock _block) throws BlockParserException {
			return new SubBlockInfo(new SubBlock(_block.getBytes(), _block.getOffset() + getSizeHead(),
					getBodyOutputSizeForDecryption(_block.getSize() - getSizeHead())), true, false);
		}

		@Override
		public SubBlock getParentBlock(SubBlock _block, boolean excludeFromEncryption) throws BlockParserException {
			return getParentBlockWithNoTreatments(_block);
		}

		@Override
		public int getSizeHead() {
			return 0;
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
			return new SubBlockInfo(_block, true, false);
		}

		@Override
		public SubBlock signIfPossibleSortantPointToPointTransferedBlock(SubBlock _block) throws BlockParserException {
			return _block;
		}

	}

	private static class BlockChecker extends TransferedBlockChecker {

		protected BlockChecker(TransferedBlockChecker _subChecker) {
			super(_subChecker, true);
		}

		/**
		 * 
		 */
		private static final long serialVersionUID = -6848501726603093585L;

		@Override
		public SubBlockInfo checkSubBlock(SubBlock _block) throws BlockParserException {
			return new SubBlockInfo(_block, true, false);
		}

		@Override
		public Integrity checkDataIntegrity() {
			return Integrity.OK;
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
