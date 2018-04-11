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
package com.distrimind.madkit.kernel.network.connection.secured;

import java.io.ByteArrayInputStream;
import java.net.InetSocketAddress;

import gnu.vm.jgnu.security.InvalidAlgorithmParameterException;
import gnu.vm.jgnu.security.InvalidKeyException;
import gnu.vm.jgnu.security.NoSuchAlgorithmException;
import gnu.vm.jgnu.security.NoSuchProviderException;
import gnu.vm.jgnu.security.SignatureException;
import gnu.vm.jgnu.security.spec.InvalidKeySpecException;
import gnu.vm.jgnux.crypto.NoSuchPaddingException;
import gnu.vm.jgnux.crypto.ShortBufferException;

import com.distrimind.madkit.exceptions.BlockParserException;
import com.distrimind.madkit.exceptions.ConnectionException;
import com.distrimind.madkit.kernel.MadkitProperties;
import com.distrimind.madkit.kernel.network.Block;
import com.distrimind.madkit.kernel.network.NetworkProperties;
import com.distrimind.madkit.kernel.network.PacketCounter;
import com.distrimind.madkit.kernel.network.PacketPartHead;
import com.distrimind.madkit.kernel.network.SubBlock;
import com.distrimind.madkit.kernel.network.SubBlockInfo;
import com.distrimind.madkit.kernel.network.SubBlockParser;
import com.distrimind.madkit.kernel.network.connection.ConnectionFinished;
import com.distrimind.madkit.kernel.network.connection.ConnectionMessage;
import com.distrimind.madkit.kernel.network.connection.ConnectionProtocol;
import com.distrimind.madkit.kernel.network.connection.TransferedBlockChecker;
import com.distrimind.madkit.kernel.network.connection.UnexpectedMessage;
import com.distrimind.ood.database.DatabaseWrapper;
import com.distrimind.util.Bits;
import com.distrimind.util.crypto.ASymmetricKeyPair;
import com.distrimind.util.crypto.ASymmetricKeyWrapperType;
import com.distrimind.util.crypto.AbstractSecureRandom;
import com.distrimind.util.crypto.SymmetricAuthentifiedSignatureCheckerAlgorithm;
import com.distrimind.util.crypto.SymmetricAuthentifiedSignatureType;
import com.distrimind.util.crypto.SymmetricAuthentifiedSignerAlgorithm;
import com.distrimind.util.crypto.SymmetricEncryptionAlgorithm;
import com.distrimind.util.crypto.SymmetricEncryptionType;
import com.distrimind.util.crypto.SymmetricSecretKey;
import com.distrimind.util.sizeof.ObjectSizer;

/**
 * Represents a connection protocol used between a client and a server. This
 * class must be used by the server. There is no certificate, so the public key
 * must be known in advance with this protocol.
 * 
 * @author Jason Mahdjoub
 * @version 1.2
 * @since MadkitLanEdition 1.0
 * @see ClientSecuredConnectionProtocolWithKnownPublicKey
 */
public class ServerSecuredConnectionProtocolWithKnwonPublicKey
		extends ConnectionProtocol<ServerSecuredConnectionProtocolWithKnwonPublicKey> {
	Step current_step = Step.NOT_CONNECTED;

	ASymmetricKeyPair myKeyPairForEncryption;

	protected SymmetricEncryptionType symmetricEncryptionType=null;
	protected SymmetricEncryptionAlgorithm symmetricEncryption = null;
	protected SymmetricAuthentifiedSignerAlgorithm signer = null;
	protected SymmetricAuthentifiedSignatureCheckerAlgorithm signatureChecker=null;
	protected SymmetricSecretKey mySecretKeyForEncryption=null,mySecretKeyForSignature=null;
	protected SymmetricAuthentifiedSignatureType signatureType;
	protected ASymmetricKeyWrapperType keyWrapper=null;
	
	protected int signature_size_bytes;
	protected short secretKeySizeBits;
	private final SubBlockParser parser;

	protected final ServerSecuredProcotolPropertiesWithKnownPublicKey hproperties;
	private final AbstractSecureRandom approvedRandom;
	final int maximumSignatureSize;
	boolean firstMessageReceived = false;
	private boolean needToRefreshTransferBlockChecker = true;
	private final PacketCounterForEncryptionAndSignature packetCounter;
	private boolean reinitSymmetricAlgorithm=true;


	private ServerSecuredConnectionProtocolWithKnwonPublicKey(InetSocketAddress _distant_inet_address,
			InetSocketAddress _local_interface_address, ConnectionProtocol<?> _subProtocol,
			DatabaseWrapper sql_connection, MadkitProperties mkProperties, NetworkProperties _properties, int subProtocolLevel, boolean isServer,
			boolean mustSupportBidirectionnalConnectionInitiative) throws ConnectionException {
		super(_distant_inet_address, _local_interface_address, _subProtocol, sql_connection, _properties,
				subProtocolLevel, isServer, mustSupportBidirectionnalConnectionInitiative);
		hproperties = (ServerSecuredProcotolPropertiesWithKnownPublicKey) super.connection_protocol_properties;

		hproperties.checkProperties();

		myKeyPairForEncryption = null;

		signature_size_bytes = -1;
		this.symmetricEncryption = null;
		this.keyWrapper=null;
		signatureType = null;
		symmetricEncryptionType=null;
		secretKeySizeBits=-1;
		try {
			approvedRandom=mkProperties.getApprovedSecureRandom();

			maximumSignatureSize = hproperties.getMaximumSignatureSizeBits();
		} catch (NoSuchAlgorithmException | NoSuchProviderException e) {
			throw new ConnectionException(e);
		}
		this.packetCounter=new PacketCounterForEncryptionAndSignature(approvedRandom, hproperties.enableEncryption, true);
		if (hproperties.enableEncryption)
			parser = new ParserWithEncryption();
		else
			parser = new ParserWithNoEncryption();

	}

	void initMyKeyPair(int identifier) throws BlockParserException {
		if (myKeyPairForEncryption != null)
			return;
		try {
			myKeyPairForEncryption = hproperties.getKeyPairForEncryption(identifier);
			if (myKeyPairForEncryption == null)
				throw new BlockParserException(
						"Unkonw encryption profile. Impossible to find key pair identified by " + identifier);
			
			signatureType = hproperties.getSignatureType(identifier);
			if (signatureType == null)
				throw new BlockParserException(
						"Unkonw encryption profile. Impossible to find signature identified by " + identifier);
			
			keyWrapper=hproperties.getKeyWrapper(identifier);

			if (keyWrapper == null)
				throw new BlockParserException(
						"Unkonw encryption profile. Impossible to find keyWrapper identified by " + identifier);
			
			symmetricEncryptionType = hproperties.getSymmetricEncryptionType(identifier);
			if (symmetricEncryptionType == null)
				throw new BlockParserException(
						"Unkonw encryption profile. Impossible to find symmetric encryption type identified by "
								+ identifier);
			secretKeySizeBits=hproperties.getSymmetricEncryptionKeySizeBits(identifier);
			
			signature_size_bytes = signatureType.getSignatureSizeInBits()/8;

		} catch (Exception e) {
			if (e instanceof BlockParserException)
				throw (BlockParserException) e;
			else
				throw new BlockParserException(e);
		}
	}
	private void reinitSymmetricAlgorithmIfNecessary() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchProviderException, InvalidKeySpecException
	{
		if (reinitSymmetricAlgorithm)
		{
			reinitSymmetricAlgorithm=false;
			symmetricEncryption=new SymmetricEncryptionAlgorithm(this.approvedRandom, this.mySecretKeyForEncryption, (byte)packetCounter.getMyEncryptionCounter().length);
		}
	}
	boolean isProfileInitialized()
	{
		return firstMessageReceived;
	}

	private void setSecretKeys(AskClientServerConnection askMessage) throws ConnectionException {
		try {
			if (askMessage.getSecretKeyForEncryption()==null && hproperties.enableEncryption)
				throw new ConnectionException("Secret key empty !");
			
			if (hproperties.enableEncryption)
			{
				mySecretKeyForEncryption=keyWrapper.unwrapKey(myKeyPairForEncryption.getASymmetricPrivateKey(), askMessage.getSecretKeyForEncryption());
				symmetricEncryption=new SymmetricEncryptionAlgorithm(approvedRandom, mySecretKeyForEncryption);
			}
			else
				mySecretKeyForEncryption=null;
			
			mySecretKeyForSignature=keyWrapper.unwrapKey(myKeyPairForEncryption.getASymmetricPrivateKey(), askMessage.getSecretKeyForSignature());
			
			signer = new SymmetricAuthentifiedSignerAlgorithm(mySecretKeyForSignature);
			signatureChecker = new SymmetricAuthentifiedSignatureCheckerAlgorithm(mySecretKeyForSignature);
			// this.secret_key=symmetricAlgorithm.getSecretKey();
		} catch (Exception e) {
			resetKeys();
			throw new ConnectionException(e);
		}
	}

	private void resetKeys() {
		mySecretKeyForEncryption = null;
		mySecretKeyForSignature=null;
		signer = null;
		signatureChecker=null;
		symmetricEncryption = null;
	}

	private enum Step {
		NOT_CONNECTED, WAITING_FOR_CONNECTION_CONFIRMATION, CONNECTED,
	}

	@Override
	protected ConnectionMessage getNextStep(ConnectionMessage _m) {
		switch (current_step) {
		case NOT_CONNECTED: {
			
			if (_m instanceof AskClientServerConnection) {
				AskClientServerConnection ask = (AskClientServerConnection) _m;

				try {
					setSecretKeys(ask);
				} catch (ConnectionException e) {
					return new ConnectionFinished(this.getDistantInetSocketAddress(),
							ConnectionClosedReason.CONNECTION_ANOMALY);
				}
				current_step = Step.WAITING_FOR_CONNECTION_CONFIRMATION;
				return new ConnectionFinished(getDistantInetSocketAddress(), packetCounter.getMyEncodedCounters());
			} else if (_m instanceof ConnectionFinished) {
				if (((ConnectionFinished) _m).getState()
						.equals(ConnectionProtocol.ConnectionState.CONNECTION_ESTABLISHED)) {
					return new UnexpectedMessage(this.getDistantInetSocketAddress());
				} else
					return new ConnectionFinished(this.getDistantInetSocketAddress(),
							ConnectionClosedReason.CONNECTION_ANOMALY);
			} else {
				return new UnexpectedMessage(this.getDistantInetSocketAddress());
			}
		}
		case WAITING_FOR_CONNECTION_CONFIRMATION: {
			if (_m instanceof ConnectionFinished && ((ConnectionFinished) _m).getState()
					.equals(ConnectionProtocol.ConnectionState.CONNECTION_ESTABLISHED)) {
				if (!packetCounter.setDistantCounters(((ConnectionFinished) _m).getInitialCounter()))
				{
					current_step=Step.NOT_CONNECTED;
					return new UnexpectedMessage(this.getDistantInetSocketAddress());
				}
				current_step = Step.CONNECTED;
				return null;
			} else if (_m instanceof ConnectionFinished) {
				return new UnexpectedMessage(this.getDistantInetSocketAddress());
			} else {
				return new UnexpectedMessage(this.getDistantInetSocketAddress());
			}

		}
		case CONNECTED: {
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
				else
				{
					if (!packetCounter.setDistantCounters(((ConnectionFinished) _m).getInitialCounter()))
					{
						current_step=Step.NOT_CONNECTED;
						return new UnexpectedMessage(this.getDistantInetSocketAddress());
					}
				}
				return null;
			} else {
				return new UnexpectedMessage(this.getDistantInetSocketAddress());
			}
		}
		}
		return null;
	}

	@Override
	public boolean isCrypted() {
		return hproperties.enableEncryption;
	}

	@Override
	protected void closeConnection(ConnectionClosedReason _reason) {
		if (_reason.equals(ConnectionClosedReason.CONNECTION_ANOMALY)) {
			resetKeys();
		}
		current_step = Step.NOT_CONNECTED;
	}

	private class ParserWithEncryption extends SubBlockParser {

		ParserWithEncryption() {

		}

		@Override
		public int getBodyOutputSizeForEncryption(int size) throws BlockParserException {
			
			try {
				if (current_step==Step.NOT_CONNECTED)
					return size;
				else
				{
					if (getCounterSelector().isActivated())
					{
						reinitSymmetricAlgorithmIfNecessary();
					}
					return symmetricEncryption.getOutputSizeForEncryption(size)+4;
				}

			} catch (Exception e) {
				throw new BlockParserException(e);
			}
		}

		@Override
		public int getBodyOutputSizeForDecryption(int size) throws BlockParserException {
			try {
				switch (current_step) {
				case NOT_CONNECTED:
					return size;
				case WAITING_FOR_CONNECTION_CONFIRMATION:
				case CONNECTED:
					if (getPacketCounter().isDistantActivated())
					{
						reinitSymmetricAlgorithmIfNecessary();
					}
					return symmetricEncryption.getOutputSizeForDecryption(size-4);
				}
			} catch (Exception e) {
				throw new BlockParserException(e);
			}
			throw new IllegalAccessError();
		}

		@Override
		public SubBlockInfo getSubBlock(SubBlock _block) throws BlockParserException {
			switch (current_step) {
			case NOT_CONNECTED: {
				
				
				if (!isProfileInitialized())
				{
					int identifier = Bits.getInt(_block.getBytes(), _block.getOffset());
					initMyKeyPair(identifier);
				}
				SubBlock res = new SubBlock(_block.getBytes(), _block.getOffset() + getSizeHead(),
						getBodyOutputSizeForDecryption(_block.getSize() - getSizeHead()));
				setFirstMessageReceived();
				
				return new SubBlockInfo(res, true, false);
			}
			case WAITING_FOR_CONNECTION_CONFIRMATION:
			case CONNECTED: {
				int off=_block.getOffset() + getSizeHead();
				int offr=_block.getOffset()+_block.getSize();
				boolean excludedFromEncryption=_block.getBytes()[offr-1]==1;
				if (excludedFromEncryption)
				{
					int s=Block.getShortInt(_block.getBytes(), offr-4);
					if (s>Block.BLOCK_SIZE_LIMIT || s>_block.getSize()-getSizeHead()-4  || s<PacketPartHead.getHeadSize(true))
						throw new BlockParserException();
					try{
						
						SubBlock res = new SubBlock(_block.getBytes(), off, s);
						signatureChecker.init(_block.getBytes(), _block.getOffset(),
								signature_size_bytes);
						if (getPacketCounter().isDistantActivated())
						{
							
							signatureChecker.update(packetCounter.getMySignatureCounter());
						}
						signatureChecker.update(_block.getBytes(),
								off, _block.getSize() - getSizeHead());
						boolean check = signatureChecker.verify();
						
					

						return new SubBlockInfo(res, check, !check);
					} catch (Exception e) {

						SubBlock res = new SubBlock(_block.getBytes(), off,
								getBodyOutputSizeForDecryption(_block.getSize() - getSizeHead()));
						return new SubBlockInfo(res, false, true);
					}
				}
				else
				{
					int s=_block.getSize() - getSizeHead()-4;
					
					try (ByteArrayInputStream bais = new ByteArrayInputStream(_block.getBytes(),
							off, s)) {
						
						final byte []tab=new byte[_block.getBytes().length];
						
						ConnectionProtocol.ByteArrayOutputStream os=new ConnectionProtocol.ByteArrayOutputStream(tab, off);
						
						boolean check=true;
						if (!symmetricEncryption.getType().isAuthenticatedAlgorithm())
						{
							signatureChecker.init(_block.getBytes(), _block.getOffset(),
									signature_size_bytes);
							if (getPacketCounter().isDistantActivated())
							{
								
								signatureChecker.update(packetCounter.getMySignatureCounter());
							}
							signatureChecker.update(_block.getBytes(),
									off, _block.getSize() - getSizeHead());
							check = signatureChecker.verify();
						}
						SubBlock res = null;
						if (check)
						{
							if (getPacketCounter().isDistantActivated())
							{
								reinitSymmetricAlgorithmIfNecessary();
								symmetricEncryption.decode(bais, os, packetCounter.getMyEncryptionCounter());
							}
							else
								symmetricEncryption.decode(bais, os);
							res = new SubBlock(tab, off, os.getSize());
						}
						else 
						{
							
							res = new SubBlock(tab, off, symmetricEncryption.getOutputSizeForDecryption(s));
						}
						return new SubBlockInfo(res, check, !check);
					} catch (Exception e) {
						SubBlock res = new SubBlock(_block.getBytes(), _block.getOffset() + getSizeHead(),
								getBodyOutputSizeForDecryption(_block.getSize() - getSizeHead()));
						return new SubBlockInfo(res, false, true);
					}
				}
			}
			}
			throw new BlockParserException("Unexpected exception");
		}

		@Override
		public SubBlock getParentBlock(final SubBlock _block, boolean excludeFromEncryption) throws BlockParserException {
			try {
				final int outputSize = getBodyOutputSizeForEncryption(_block.getSize());
				switch (current_step) {
				case NOT_CONNECTED:
				{
					
					SubBlock res = new SubBlock(_block.getBytes(), _block.getOffset() - getSizeHead(),
							outputSize + getSizeHead());
					
					
					int off=_block.getSize()+_block.getOffset();
					byte[] tab=res.getBytes();
					for (int i=outputSize+_block.getOffset()-1;i>=off;i--)
						tab[i]=0;
					for (int i=res.getOffset();i<_block.getOffset();i++)
						tab[i]=0;
					return res;
				}
				case WAITING_FOR_CONNECTION_CONFIRMATION:
				case CONNECTED: {
					int s=outputSize + getSizeHead();
					if (excludeFromEncryption)
					{
						final SubBlock res = new SubBlock(_block.getBytes(), _block.getOffset() - getSizeHead(),s);
						int off=_block.getSize()+_block.getOffset();
						byte[] tab=res.getBytes();
						for (int i=outputSize+_block.getOffset()-5;i>=off;i--)
							tab[i]=0;
						
						int offr=res.getOffset()+res.getSize();
						tab[offr-1]=1;
						Block.putShortInt(tab, offr-4, _block.getSize());
						signer.init();
						if (getCounterSelector().isActivated())
						{
							signer.update(packetCounter.getOtherSignatureCounter());
						}
						signer.update(res.getBytes(), _block.getOffset(),
								outputSize);
						
						signer.getSignature(res.getBytes(), res.getOffset());

						return res;

					}
					else
					{
						final SubBlock res = new SubBlock(new byte[_block.getBytes().length], _block.getOffset() - getSizeHead(),s);
						
						res.getBytes()[res.getOffset()+res.getSize()-1]=0;
						if (getCounterSelector().isActivated())
						{
							reinitSymmetricAlgorithmIfNecessary();
							symmetricEncryption.encode(_block.getBytes(), _block.getOffset(), _block.getSize(), null, 0, 0, new ConnectionProtocol.ByteArrayOutputStream(res.getBytes(), _block.getOffset()), packetCounter.getOtherEncryptionCounter());
						}
						else
							symmetricEncryption.encode(_block.getBytes(), _block.getOffset(), _block.getSize(), null, 0, 0, new ConnectionProtocol.ByteArrayOutputStream(res.getBytes(), _block.getOffset()));
						
						if (!symmetricEncryption.getType().isAuthenticatedAlgorithm())
						{
							signer.init();
							if (getCounterSelector().isActivated())
							{
								signer.update(packetCounter.getOtherSignatureCounter());
							}
							signer.update(res.getBytes(), _block.getOffset(),
									outputSize);
							
							signer.getSignature(res.getBytes(), res.getOffset());
						}
						
						return res;
					}
				}
				}
			} catch (Exception e) {
				throw new BlockParserException(e);
			}
			throw new IllegalAccessError();
		}

		@Override
		public int getSizeHead() {
			//return signature_size;
			if (isProfileInitialized())
				return signature_size_bytes;
			else
				return ObjectSizer.sizeOf(hproperties.getLastEncryptionProfileIdentifier());
		}

		@Override
		public int getMaximumSizeHead() {
			return maximumSignatureSize;
		}

		@Override
		public SubBlockInfo checkEntrantPointToPointTransferedBlock(SubBlock _block) throws BlockParserException {
			SubBlock res = new SubBlock(_block.getBytes(), _block.getOffset() + getSizeHead(),
					_block.getSize() - getSizeHead());
			switch (current_step) {
			
			case NOT_CONNECTED: {
				
				return new SubBlockInfo(res, true, false);
			}
			case WAITING_FOR_CONNECTION_CONFIRMATION:
			case CONNECTED: {
				try {

					boolean check = signatureChecker.verify(_block.getBytes(), res.getOffset(),
							res.getSize(), _block.getBytes(), _block.getOffset(), signature_size_bytes);
					return new SubBlockInfo(res, check, !check);
				} catch (Exception e) {
					return new SubBlockInfo(res, false, true);
				}
			}
			}
			throw new BlockParserException("Unexpected exception");
		}

		@Override
		public SubBlock signIfPossibleSortantPointToPointTransferedBlock(SubBlock _block) throws BlockParserException {
			try {
				SubBlock res = new SubBlock(_block.getBytes().clone(), _block.getOffset() - getSizeHead(),
						_block.getSize() + getSizeHead());

				switch (current_step) {
				case NOT_CONNECTED:
				{
					return res;
				}
				case WAITING_FOR_CONNECTION_CONFIRMATION:
				case CONNECTED: {
					signer.sign(_block.getBytes(), _block.getOffset(), _block.getSize(), res.getBytes(), res.getOffset(),
							getSizeHead());
					return res;
				}
				}
			} catch (Exception e) {
				throw new BlockParserException(e);
			}
			throw new IllegalAccessError();
		}

	}

	private class ParserWithNoEncryption extends SubBlockParser {
		ParserWithNoEncryption() {

		}

		@Override
		public int getBodyOutputSizeForEncryption(int size) {
			return size;
		}

		@Override
		public SubBlockInfo getSubBlock(SubBlock _block) throws BlockParserException {

			try
			{
			switch (current_step) {
			case NOT_CONNECTED: {
				if (!isProfileInitialized())
				{
					int identifier = Bits.getInt(_block.getBytes(), _block.getOffset());
					initMyKeyPair(identifier);
				}
				SubBlock res = new SubBlock(_block.getBytes(), _block.getOffset() + getSizeHead(),
						getBodyOutputSizeForDecryption(_block.getSize() - getSizeHead()));
				setFirstMessageReceived();				
				return new SubBlockInfo(res, true, false);
			}
			case WAITING_FOR_CONNECTION_CONFIRMATION:
			case CONNECTED: {
				SubBlock res = new SubBlock(_block.getBytes(), _block.getOffset() + getSizeHead(),
						getBodyOutputSizeForDecryption(_block.getSize() - getSizeHead()));
				try {
					signatureChecker.init(_block.getBytes(),
							_block.getOffset(), signature_size_bytes);
					if (getPacketCounter().isDistantActivated())
					{
						
						signatureChecker.update(packetCounter.getMySignatureCounter());
					}
					signatureChecker.update(res.getBytes(), res.getOffset(), _block.getSize() - getSizeHead());
					boolean check = signatureChecker.verify();
					return new SubBlockInfo(res, check, !check);
				} catch (SignatureException | InvalidKeyException | NoSuchAlgorithmException
						| InvalidKeySpecException | ShortBufferException | IllegalStateException e) {
					return new SubBlockInfo(res, false, true);
				}
			}
			}
			}
			catch(Exception e)
			{
				throw new BlockParserException(e);
			}
			throw new BlockParserException("Unexpected exception");
		}

		@Override
		public SubBlock getParentBlock(SubBlock _block, boolean excludeFromEncryption) throws BlockParserException {
			try {
				int output=getBodyOutputSizeForEncryption(_block.getSize());
				SubBlock res = new SubBlock(_block.getBytes(), _block.getOffset() - getSizeHead(),
						output + getSizeHead());
				int off=_block.getSize()+_block.getOffset();
				byte[] tab=res.getBytes();
				for (int i=output+_block.getOffset()-1;i>=off;i--)
					tab[i]=0;
				if (current_step==Step.NOT_CONNECTED)
				{
					for (int i=res.getOffset();i<_block.getOffset();i++)
						tab[i]=0;

					return res;
				}
				signer.init();
				if (getCounterSelector().isActivated())
				{
					signer.update(packetCounter.getOtherSignatureCounter());
				}
				signer.update(_block.getBytes(), _block.getOffset(), output);
				
				signer.getSignature(res.getBytes(), res.getOffset());
				return res;
			} catch (Exception e) {
				throw new BlockParserException(e);
			}

		}

		@Override
		public int getSizeHead() {
			//return signature_size;
			if (isProfileInitialized())
				return signature_size_bytes;
			else
				return ObjectSizer.sizeOf(hproperties.getLastEncryptionProfileIdentifier());
		}

		@Override
		public int getBodyOutputSizeForDecryption(int _size) {
			return _size;
		}

		@Override
		public int getMaximumSizeHead() {
			return maximumSignatureSize;
		}
		
		@Override
		public SubBlockInfo checkEntrantPointToPointTransferedBlock(SubBlock _block) throws BlockParserException {
			SubBlock res = new SubBlock(_block.getBytes(), _block.getOffset() + getSizeHead(),
					_block.getSize() - getSizeHead());
			switch (current_step) {
			
			case NOT_CONNECTED: {
				
				return new SubBlockInfo(res, true, false);
			}
			case WAITING_FOR_CONNECTION_CONFIRMATION:
			case CONNECTED: {
				try {

					boolean check = signatureChecker.verify(_block.getBytes(), res.getOffset(),
							res.getSize(), _block.getBytes(), _block.getOffset(), signature_size_bytes);
					return new SubBlockInfo(res, check, !check);
				} catch (Exception e) {
					return new SubBlockInfo(res, false, true);
				}
			}
			}
			throw new BlockParserException("Unexpected exception");
		}

		@Override
		public SubBlock signIfPossibleSortantPointToPointTransferedBlock(SubBlock _block) throws BlockParserException {
			try {
				SubBlock res = new SubBlock(_block.getBytes().clone(), _block.getOffset() - getSizeHead(),
						_block.getSize() + getSizeHead());
				signer.sign(_block.getBytes(), _block.getOffset(), _block.getSize(), res.getBytes(), res.getOffset(),
						getSizeHead());
				return res;
			} catch (Exception e) {
				throw new BlockParserException(e);
			}
		}		

	}

	@Override
	public SubBlockParser getParser() {
		return parser;
	}

	void setFirstMessageReceived() {
		this.firstMessageReceived = true;
		this.needToRefreshTransferBlockChecker = true;
	}

	@Override
	protected TransferedBlockChecker getTransferedBlockChecker(TransferedBlockChecker subBlockChercker)
			throws ConnectionException {
		try {
			needToRefreshTransferBlockChecker=false;
			return new ConnectionProtocol.NullBlockChecker(subBlockChercker, this.isCrypted(),
					(short) parser.getSizeHead());
		} catch (Exception e) {
			needToRefreshTransferBlockChecker = true;
			throw new ConnectionException(e);
		}
	}

	@Override
	public boolean isTransferBlockCheckerChangedImpl() {
		return needToRefreshTransferBlockChecker;

	}

	/*private static class BlockChecker extends TransferedBlockChecker {
		private final ASymmetricSignatureType signatureType;
		private final int signatureSize;
		private transient AbstractSignature signature;
		private transient ASymmetricPublicKey publicKey;

		protected BlockChecker(TransferedBlockChecker _subChecker, ASymmetricSignatureType signatureType,
				ASymmetricPublicKey publicKey, int signatureSize, boolean isCrypted) throws NoSuchAlgorithmException {
			super(_subChecker, !isCrypted);
			this.signatureType = signatureType;
			this.publicKey = publicKey;
			this.signatureSize = signatureSize;
			initSignature();
		}

		@Override
		public Integrity checkDataIntegrity() {
			if (signatureType == null)
				return Integrity.FAIL;
			if (signature == null)
				return Integrity.FAIL;
			if (publicKey == null)
				return Integrity.FAIL;
			return Integrity.OK;
		}

		private void initSignature() throws NoSuchAlgorithmException {
			this.signature = signatureType.getSignatureInstance();
		}

		private void writeObject(ObjectOutputStream os) throws IOException {
			os.defaultWriteObject();
			byte encodedPK[] = publicKey.encode();
			os.writeInt(encodedPK.length);
			os.write(encodedPK);
		}

		private void readObject(ObjectInputStream is) throws IOException {
			try {
				is.defaultReadObject();
				int len = is.readInt();
				byte encodedPK[] = new byte[len];
				is.read(encodedPK);
				publicKey = ASymmetricPublicKey.decode(encodedPK);
				initSignature();
			} catch (IOException e) {
				throw e;
			} catch (Exception e) {
				throw new IOException(e);
			}
		}

		@Override
		public SubBlockInfo checkSubBlock(SubBlock _block) throws BlockParserException {
			try {
				SubBlock res = new SubBlock(_block.getBytes(), _block.getOffset() + signatureSize,
						_block.getSize() - signatureSize);
				signature.initVerify(publicKey);
				signature.update(res.getBytes(), res.getOffset(), res.getSize());
				boolean check = signature.verify(_block.getBytes(), _block.getOffset(), signatureSize);
				return new SubBlockInfo(res, check, !check);
			} catch (Exception e) {
				throw new BlockParserException(e);
			}
		}

	}*/

	@Override
	public boolean needsMadkitLanEditionDatabase() {
		return false;
	}

	@Override
	public PacketCounter getPacketCounter() {
		return packetCounter;
	}

}
