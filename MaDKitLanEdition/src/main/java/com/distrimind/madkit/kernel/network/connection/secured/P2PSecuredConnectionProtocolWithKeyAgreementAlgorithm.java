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
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;

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
import com.distrimind.madkit.kernel.network.connection.AskConnection;
import com.distrimind.madkit.kernel.network.connection.ConnectionFinished;
import com.distrimind.madkit.kernel.network.connection.ConnectionMessage;
import com.distrimind.madkit.kernel.network.connection.ConnectionProtocol;
import com.distrimind.madkit.kernel.network.connection.IncomprehensiblePublicKey;
import com.distrimind.madkit.kernel.network.connection.TransferedBlockChecker;
import com.distrimind.madkit.kernel.network.connection.UnexpectedMessage;
import com.distrimind.ood.database.DatabaseWrapper;
import com.distrimind.util.Bits;
import com.distrimind.util.crypto.AbstractSecureRandom;
import com.distrimind.util.crypto.KeyAgreement;
import com.distrimind.util.crypto.SymmetricAuthentifiedSignatureCheckerAlgorithm;
import com.distrimind.util.crypto.SymmetricAuthentifiedSignerAlgorithm;
import com.distrimind.util.crypto.SymmetricEncryptionAlgorithm;
import com.distrimind.util.crypto.SymmetricSecretKey;

import gnu.vm.jgnu.security.InvalidAlgorithmParameterException;
import gnu.vm.jgnu.security.InvalidKeyException;
import gnu.vm.jgnu.security.NoSuchAlgorithmException;
import gnu.vm.jgnu.security.NoSuchProviderException;
import gnu.vm.jgnu.security.SignatureException;
import gnu.vm.jgnu.security.spec.InvalidKeySpecException;
import gnu.vm.jgnux.crypto.BadPaddingException;
import gnu.vm.jgnux.crypto.IllegalBlockSizeException;
import gnu.vm.jgnux.crypto.NoSuchPaddingException;
import gnu.vm.jgnux.crypto.ShortBufferException;

/**
 * 
 * 
 * @author Jason Mahdjoub
 * @version 2.1
 * @since MadkitLanEdition 1.7
 */
public class P2PSecuredConnectionProtocolWithKeyAgreementAlgorithm extends ConnectionProtocol<P2PSecuredConnectionProtocolWithKeyAgreementAlgorithm> {
	
	private static final int MATERIAL_KEY_SIZE_BYTES=64;
	Step current_step = Step.NOT_CONNECTED;

	private SymmetricSecretKey secret_key_for_encryption = null;
	private SymmetricSecretKey secret_key_for_signature = null;
	
	protected SymmetricEncryptionAlgorithm symmetricEncryption = null;
	protected KeyAgreement keyAgreementForEncryption=null, keyAgreementForSignature=null;
	protected SymmetricAuthentifiedSignerAlgorithm signer = null;
	protected SymmetricAuthentifiedSignatureCheckerAlgorithm signatureChecker = null;
	final int signature_size_bytes;
	private final SubBlockParser parser;

	
	private final P2PSecuredConnectionProtocolWithKeyAgreementProperties hproperties;
	private final AbstractSecureRandom approvedRandom, approvedRandomForKeys;
	private boolean blockCheckerChanged = true;
	private boolean currentBlockCheckerIsNull = true;
	private byte[] materialKeyForSignature=null, materialKeyForEncryption=null;
	private final PacketCounterForEncryptionAndSignature packetCounter;
	private boolean reinitSymmetricAlgorithm=true;
	private boolean myCounterSent=false;
	private boolean doNotTakeIntoAccountNextState=true;
	private P2PSecuredConnectionProtocolWithKeyAgreementAlgorithm(InetSocketAddress _distant_inet_address,
			InetSocketAddress _local_interface_address, ConnectionProtocol<?> _subProtocol,
			DatabaseWrapper sql_connection, MadkitProperties mkProperties, NetworkProperties _properties, int subProtocolLevel, boolean isServer,
			boolean mustSupportBidirectionnalConnectionInitiative) throws ConnectionException {
		super(_distant_inet_address, _local_interface_address, _subProtocol, sql_connection, _properties,
				subProtocolLevel, isServer, mustSupportBidirectionnalConnectionInitiative);
		hproperties = (P2PSecuredConnectionProtocolWithKeyAgreementProperties) super.connection_protocol_properties;
		hproperties.checkProperties();

		
		
		try {
			approvedRandom=mkProperties.getApprovedSecureRandom();
			approvedRandomForKeys=mkProperties.getApprovedSecureRandomForKeys();
		} catch (NoSuchAlgorithmException | NoSuchProviderException e) {
			throw new ConnectionException(e);
		}
		
		int sigsize=0;
		try {
			
			SymmetricAuthentifiedSignerAlgorithm signerTmp = new SymmetricAuthentifiedSignerAlgorithm(hproperties.symmetricSignatureType.getKeyGenerator(approvedRandomForKeys, hproperties.symmetricEncryptionType.getDefaultKeySizeBits()).generateKey());
			signerTmp.init();
			sigsize = signerTmp.getMacLength();
			
		} catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeyException | InvalidKeySpecException e) {
			throw new ConnectionException(e);
		}
		signature_size_bytes=sigsize;
		this.packetCounter=new PacketCounterForEncryptionAndSignature(approvedRandom, hproperties.enableEncryption, true);
		
		if (hproperties.enableEncryption)
			parser = new ParserWithEncryption();
		else
			parser = new ParserWithNoEncryption();
	}

	private void checkSymmetricSignatureAlgorithm() throws ConnectionException {
		try {
			if (secret_key_for_signature!=null) {
				if (signer==null || signatureChecker==null)
				{
					signer=new SymmetricAuthentifiedSignerAlgorithm(secret_key_for_signature);
					signatureChecker=new SymmetricAuthentifiedSignatureCheckerAlgorithm(secret_key_for_signature);
					blockCheckerChanged=true;
				}
			} else {
				symmetricEncryption = null;
				signer=null;
				signatureChecker=null;
			}
		} catch (NoSuchAlgorithmException  
				| NoSuchProviderException  e) {
			throw new ConnectionException(e);
		}
	}
	private void checkSymmetricEncryptionAlgorithm() throws ConnectionException {
		try {
			if (secret_key_for_encryption != null) {
				if (symmetricEncryption == null) {
					symmetricEncryption = new SymmetricEncryptionAlgorithm(approvedRandom, secret_key_for_encryption);
				}
			} else {
				symmetricEncryption = null;

			}
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeySpecException
				| NoSuchProviderException | InvalidAlgorithmParameterException e) {
			throw new ConnectionException(e);
		}
	}

	private void reset() {
		blockCheckerChanged = true;
		symmetricEncryption=null;
		secret_key_for_encryption = null;
		secret_key_for_signature=null;
		keyAgreementForEncryption=null;
		keyAgreementForSignature=null;
		signer=null;
		signatureChecker=null;
		
	}

	private enum Step {
		NOT_CONNECTED, WAITING_FOR_SIGNATURE_DATA, WAITING_FOR_ENCRYPTION_DATA, WAITING_FOR_CONNECTION_CONFIRMATION, CONNECTED,
	}

	private void initKeyAgreementAlgorithm() throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException, InvalidAlgorithmParameterException
	{
		if (hproperties.enableEncryption && materialKeyForEncryption==null)
			throw new InternalError();
		if (materialKeyForSignature==null)
			throw new InternalError();
		if (this.isCurrentServerAskingConnection())
		{
			if (hproperties.enableEncryption)
				this.keyAgreementForEncryption=hproperties.keyAgreementType.getKeyAgreementServer(this.approvedRandomForKeys, hproperties.symmetricEncryptionType, hproperties.symmetricKeySizeBits, materialKeyForEncryption);
			else
				this.keyAgreementForEncryption=null;
			this.keyAgreementForSignature=hproperties.keyAgreementType.getKeyAgreementServer(this.approvedRandomForKeys, hproperties.symmetricSignatureType, hproperties.symmetricKeySizeBits, materialKeyForSignature);
		}
		else
		{
			if (hproperties.enableEncryption)
				this.keyAgreementForEncryption=hproperties.keyAgreementType.getKeyAgreementClient(this.approvedRandomForKeys, hproperties.symmetricEncryptionType, hproperties.symmetricKeySizeBits, materialKeyForEncryption);
			else
				this.keyAgreementForEncryption=null;
			this.keyAgreementForSignature=hproperties.keyAgreementType.getKeyAgreementClient(this.approvedRandomForKeys, hproperties.symmetricSignatureType, hproperties.symmetricKeySizeBits, materialKeyForSignature);
			
		}
	}
	
	private void reinitSymmetricAlgorithmIfNecessary() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchProviderException, InvalidKeySpecException
	{
		if (reinitSymmetricAlgorithm)
		{
			reinitSymmetricAlgorithm=false;
			symmetricEncryption=new SymmetricEncryptionAlgorithm(this.approvedRandom, this.secret_key_for_encryption, (byte)packetCounter.getMyEncryptionCounter().length);
		}
	}

	@Override
	protected ConnectionMessage getNextStep(ConnectionMessage _m) throws ConnectionException {
		switch (current_step) {
		case NOT_CONNECTED: {
			if (_m instanceof AskConnection) {
				AskConnection ask = (AskConnection) _m;
				
				current_step = Step.WAITING_FOR_SIGNATURE_DATA;
				if (ask.isYouAreAsking()) {
					return new AskConnection(false);
				} else {
					try {
						
						materialKeyForSignature=new byte[MATERIAL_KEY_SIZE_BYTES];
						approvedRandom.nextBytes(materialKeyForSignature);
						
						byte [] material=null;
						if (hproperties.enableEncryption)
						{
							materialKeyForEncryption=new byte[MATERIAL_KEY_SIZE_BYTES];
							approvedRandom.nextBytes(materialKeyForEncryption);
							material=Bits.concateEncodingWithShortSizedTabs(materialKeyForSignature, materialKeyForEncryption);
						}
						else
							material=materialKeyForSignature;
						initKeyAgreementAlgorithm();
						return new KeyAgreementDataMessage(keyAgreementForSignature.getDataToSend(), material);
						
					} catch (Exception e) {
						throw new ConnectionException(e);
					}
				}
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
		case WAITING_FOR_SIGNATURE_DATA:{
			if (_m instanceof KeyAgreementDataMessage)
			{
				
					
				try
				{
					KeyAgreementDataMessage kadm=(KeyAgreementDataMessage)_m;
					if (isCurrentServerAskingConnection())
					{
						
						
						try
						{
							byte[] material=kadm.getMaterialKey();
							if (hproperties.enableEncryption)
							{
								if (material==null || material.length!=MATERIAL_KEY_SIZE_BYTES*2+2)
									return new ConnectionFinished(distant_inet_address, ConnectionClosedReason.CONNECTION_ANOMALY);
								byte[][] t=Bits.separateEncodingsWithShortSizedTabs(material);
								materialKeyForSignature=t[0];
								materialKeyForEncryption=t[1];
								if (materialKeyForEncryption==null || materialKeyForEncryption.length!=MATERIAL_KEY_SIZE_BYTES
										|| materialKeyForSignature==null || materialKeyForSignature.length!=MATERIAL_KEY_SIZE_BYTES)
									return new ConnectionFinished(distant_inet_address, ConnectionClosedReason.CONNECTION_ANOMALY);
							}
							else
							{
								if (material==null || material.length!=MATERIAL_KEY_SIZE_BYTES)
									return new ConnectionFinished(distant_inet_address, ConnectionClosedReason.CONNECTION_ANOMALY);
								materialKeyForSignature=material;
								
								if (materialKeyForSignature==null || materialKeyForSignature.length!=MATERIAL_KEY_SIZE_BYTES)
									return new ConnectionFinished(distant_inet_address, ConnectionClosedReason.CONNECTION_ANOMALY);
							}
							initKeyAgreementAlgorithm();
						}
						catch(Exception e)
						{
							reset();
							current_step=Step.NOT_CONNECTED;
							return new IncomprehensiblePublicKey();
						}
					}
					if (keyAgreementForSignature.hasFinishedReceiption())
					{
						reset();
						current_step=Step.NOT_CONNECTED;
						return new ConnectionFinished(distant_inet_address, ConnectionClosedReason.CONNECTION_ANOMALY);
					}
					keyAgreementForSignature.receiveData(kadm.getData());
					byte data[]=null;
					if (!keyAgreementForSignature.hasFinishedSend())
						data=keyAgreementForSignature.getDataToSend();
					doNotTakeIntoAccountNextState=false;
					if (keyAgreementForSignature.hasFinishedReceiption())
					{
						doNotTakeIntoAccountNextState=true;
						if (hproperties.enableEncryption)
							current_step=Step.WAITING_FOR_ENCRYPTION_DATA;
						else
							current_step=Step.WAITING_FOR_CONNECTION_CONFIRMATION;
						secret_key_for_signature=keyAgreementForSignature.getDerivedKey();
						checkSymmetricSignatureAlgorithm();
					}
						
					
					if (data!=null)
					{
						return new KeyAgreementDataMessage(data, null);
					}
					else
					{
						doNotTakeIntoAccountNextState=false;
						
						if (hproperties.enableEncryption)
						{
							current_step=Step.WAITING_FOR_ENCRYPTION_DATA;
							data=keyAgreementForEncryption.getDataToSend();
							return new KeyAgreementDataMessage(data, null);
						}
						else
						{
							current_step=Step.WAITING_FOR_CONNECTION_CONFIRMATION;
							myCounterSent=true;
							return new ConnectionFinished(getDistantInetSocketAddress(), packetCounter.getMyEncodedCounters());
						}
					}
				}				
				catch(Exception e)
				{
					reset();
					current_step=Step.NOT_CONNECTED;
					return new IncomprehensiblePublicKey();
				}
			} else {
				return new UnexpectedMessage(this.getDistantInetSocketAddress());
			}
		}
		case WAITING_FOR_ENCRYPTION_DATA:{
			if (_m instanceof KeyAgreementDataMessage)
			{
				if (!keyAgreementForSignature.isAgreementProcessValid() || (keyAgreementForEncryption!=null && keyAgreementForEncryption.hasFinishedReceiption()))
				{
					reset();
					current_step=Step.NOT_CONNECTED;
					return new ConnectionFinished(distant_inet_address, ConnectionClosedReason.CONNECTION_ANOMALY);
				}
				try
				{
					KeyAgreementDataMessage kadm=(KeyAgreementDataMessage)_m;
					keyAgreementForEncryption.receiveData(kadm.getData());
					byte data[]=null;
					if (!keyAgreementForEncryption.hasFinishedSend())
						data=keyAgreementForEncryption.getDataToSend();
					doNotTakeIntoAccountNextState=false;
					if (keyAgreementForEncryption.hasFinishedReceiption())
					{
						doNotTakeIntoAccountNextState=true;
						current_step=Step.WAITING_FOR_CONNECTION_CONFIRMATION;
						secret_key_for_encryption=keyAgreementForEncryption.getDerivedKey();
						checkSymmetricEncryptionAlgorithm();
					}
						
					if (data!=null)
					{
						return new KeyAgreementDataMessage(data, null);
					}
					else
					{
						doNotTakeIntoAccountNextState=false;
						myCounterSent=true;
						return new ConnectionFinished(getDistantInetSocketAddress(), packetCounter.getMyEncodedCounters());
					}
				}				
				catch(Exception e)
				{
					reset();
					current_step=Step.NOT_CONNECTED;
					return new IncomprehensiblePublicKey();
				}
			} else {
				return new UnexpectedMessage(this.getDistantInetSocketAddress());
			}
		}
		case WAITING_FOR_CONNECTION_CONFIRMATION:{
			doNotTakeIntoAccountNextState=false;
			if (_m instanceof ConnectionFinished)
			{
				if (!keyAgreementForSignature.isAgreementProcessValid() || (keyAgreementForEncryption!=null && !keyAgreementForEncryption.hasFinishedReceiption()))
				{
					reset();
					current_step=Step.NOT_CONNECTED;
					return new ConnectionFinished(distant_inet_address, ConnectionClosedReason.CONNECTION_ANOMALY);
				}
				ConnectionFinished cf=((ConnectionFinished) _m);
				if (cf.getState()==ConnectionProtocol.ConnectionState.CONNECTION_ESTABLISHED)
				{
					if (!packetCounter.setDistantCounters(cf.getInitialCounter()))
					{
						current_step=Step.NOT_CONNECTED;
						return new UnexpectedMessage(this.getDistantInetSocketAddress());
					}
					current_step=Step.CONNECTED;
					if (!myCounterSent)
					{
						myCounterSent=true;
						return new ConnectionFinished(getDistantInetSocketAddress(), packetCounter.getMyEncodedCounters());
					}
					else
						return null;
				} else {
					reset();
					current_step=Step.NOT_CONNECTED;
					return new ConnectionFinished(this.getDistantInetSocketAddress(),
							ConnectionClosedReason.CONNECTION_ANOMALY);
				}
				
					
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
					if (!packetCounter.setDistantCounters(cf.getInitialCounter()))
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
		if (_reason.equals(ConnectionClosedReason.CONNECTION_ANOMALY))
			reset();
		current_step = Step.NOT_CONNECTED;
	}

	private class ParserWithEncryption extends SubBlockParser {
		public ParserWithEncryption() {

		}

		@Override
		public int getBodyOutputSizeForEncryption(int size) throws BlockParserException {
			try {
				switch (current_step) {
				case NOT_CONNECTED:
				case WAITING_FOR_SIGNATURE_DATA:
				case WAITING_FOR_ENCRYPTION_DATA:
					if (doNotTakeIntoAccountNextState)
						return size;
					else
						return size+4;
				case WAITING_FOR_CONNECTION_CONFIRMATION: {
					if (doNotTakeIntoAccountNextState)
						return size+4;
					else
					{
						if (packetCounter.isDistantActivated())
						{
							reinitSymmetricAlgorithmIfNecessary();
						}
						return symmetricEncryption.getOutputSizeForEncryption(size)+4;
					}
				}
				case CONNECTED:
				{
					if (packetCounter.isDistantActivated())
					{
						reinitSymmetricAlgorithmIfNecessary();
					}
					return symmetricEncryption.getOutputSizeForEncryption(size)+4;
				}
				}
			} catch (Exception e) {
				throw new BlockParserException(e);
			}
			return size;

		}
		
		private SymmetricEncryptionAlgorithm maxAlgo=null;
		@Override
		public int getMaximumBodyOutputSizeForEncryption(int size) throws BlockParserException {
			try {
				if (maxAlgo==null)
				{
					maxAlgo=new SymmetricEncryptionAlgorithm(approvedRandom, hproperties.symmetricEncryptionType.getKeyGenerator(approvedRandom, hproperties.symmetricKeySizeBits).generateKey());
				}
				return maxAlgo.getOutputSizeForEncryption(size)+4;
			} catch (Exception e) {
				throw new BlockParserException(e);
			}
		}

		@Override
		public int getBodyOutputSizeForDecryption(int size) throws BlockParserException {
			try {
				switch (current_step) {
				case NOT_CONNECTED:
				case WAITING_FOR_SIGNATURE_DATA:
					return size;
				case WAITING_FOR_ENCRYPTION_DATA:
					return size-4;
				case WAITING_FOR_CONNECTION_CONFIRMATION:
				case CONNECTED:
				{
					if (getPacketCounter().isLocalActivated())
					{
						reinitSymmetricAlgorithmIfNecessary();
					}
					return symmetricEncryption.getOutputSizeForDecryption(size-4);
				}
				}
			} catch (Exception e) {
				throw new BlockParserException(e);
			}
			return size;

		}

		@Override
		public SubBlockInfo getSubBlock(SubBlock _block) throws BlockParserException {

			switch (current_step) {
			case NOT_CONNECTED:
			case WAITING_FOR_SIGNATURE_DATA:
				return getSubBlockWithNoEncryption(_block);
			case WAITING_FOR_ENCRYPTION_DATA:
				return getSubBlockWithEncryption(_block, false);
			case WAITING_FOR_CONNECTION_CONFIRMATION:
			case CONNECTED: {
				return getSubBlockWithEncryption(_block, true);
			}

			}
			throw new BlockParserException("Unexpected exception");
		}

		public SubBlockInfo getSubBlockWithNoEncryption(SubBlock _block) throws BlockParserException {
			return new SubBlockInfo(new SubBlock(_block.getBytes(), _block.getOffset() + getSizeHead(),
					getBodyOutputSizeForDecryption(_block.getSize() - getSizeHead())), true, false);
		}

		public SubBlockInfo getSubBlockWithEncryption(SubBlock _block, boolean enabledEncryption) throws BlockParserException {
			int off=_block.getOffset() + getSizeHead();
			int offr=_block.getOffset()+_block.getSize();
			boolean excludedFromEncryption=_block.getBytes()[offr-1]==1;
			if (excludedFromEncryption || !enabledEncryption)
			{
				int s=Block.getShortInt(_block.getBytes(), offr-4);
				if (s>Block.BLOCK_SIZE_LIMIT || s>_block.getSize()-getSizeHead()-4 || s<PacketPartHead.getHeadSize(false))
					throw new BlockParserException("s="+s);
				try{
					
					

					SubBlock res = new SubBlock(_block.getBytes(), off, s);
					signatureChecker.init(_block.getBytes(), _block.getOffset(),
							signature_size_bytes);
					if (getPacketCounter().isLocalActivated())
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
						if (getPacketCounter().isLocalActivated())
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
						if (getPacketCounter().isLocalActivated())
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

		public SubBlock getParentBlockWithEncryption(final SubBlock _block, boolean excludeFromEncryption) throws InvalidKeyException, SignatureException, NoSuchAlgorithmException, InvalidKeySpecException, ShortBufferException, BlockParserException, InvalidAlgorithmParameterException, IllegalStateException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException, IOException, NoSuchPaddingException
		{
			final int outputSize = getBodyOutputSizeForEncryption(_block.getSize());

			int s=outputSize + getSizeHead();
			
			if (excludeFromEncryption)
			{
				final SubBlock res = new SubBlock(_block.getBytes(), _block.getOffset() - getSizeHead(),s);
				int off=_block.getSize()+_block.getOffset();
				byte[] tab=res.getBytes();
				Arrays.fill(tab, off, outputSize+_block.getOffset()-4, (byte)0);
				
				int offr=res.getOffset()+res.getSize();
				tab[offr-1]=1;
				Block.putShortInt(tab, offr-4, _block.getSize());
				signer.init();
				if (packetCounter.isDistantActivated())
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
				
				if (packetCounter.isDistantActivated())
				{
					reinitSymmetricAlgorithmIfNecessary();
					symmetricEncryption.encode(_block.getBytes(), _block.getOffset(), _block.getSize(), null, 0, 0, new ConnectionProtocol.ByteArrayOutputStream(res.getBytes(), _block.getOffset()), packetCounter.getOtherEncryptionCounter());
				}
				else
					symmetricEncryption.encode(_block.getBytes(), _block.getOffset(), _block.getSize(), null, 0, 0, new ConnectionProtocol.ByteArrayOutputStream(res.getBytes(), _block.getOffset()));
				
				if (!symmetricEncryption.getType().isAuthenticatedAlgorithm())
				{
					signer.init();
					if (packetCounter.isDistantActivated())
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
		
		
		@Override
		public SubBlock getParentBlock(SubBlock _block, boolean excludeFromEncryption) throws BlockParserException {
			try {
				switch (current_step) {
				case NOT_CONNECTED:
				case WAITING_FOR_SIGNATURE_DATA:
					return getParentBlockWithNoTreatments(_block);
				case WAITING_FOR_ENCRYPTION_DATA:
					if (doNotTakeIntoAccountNextState)
						return getParentBlockWithNoTreatments(_block);
					else
						return getParentBlockWithEncryption(_block, true);
				case WAITING_FOR_CONNECTION_CONFIRMATION: {
					if (doNotTakeIntoAccountNextState)
					{
						if (!hproperties.enableEncryption)
							return getParentBlockWithNoTreatments(_block);
						else
							return getParentBlockWithEncryption(_block, true);
					}
					else
					{
						return getParentBlockWithEncryption(_block, excludeFromEncryption);
					}
				}
				case CONNECTED: {
					return getParentBlockWithEncryption(_block, excludeFromEncryption);
				}
				}

			} catch (Exception e) {
				throw new BlockParserException(e);
			}
			throw new BlockParserException("Unexpected exception");

		}
		

		@Override
		public int getSizeHead() {
			return P2PSecuredConnectionProtocolWithKeyAgreementAlgorithm.this.signature_size_bytes;
		}

		@Override
		public int getMaximumSizeHead() {
			return getSizeHead();
		}

		public SubBlockInfo checkEntrantPointToPointTransferedBlockWithNoEncryptin(SubBlock _block) throws BlockParserException {
			return new SubBlockInfo(new SubBlock(_block.getBytes(), _block.getOffset() + getSizeHead(),
					_block.getSize() - getSizeHead()), true, false);
		}

		public SubBlockInfo checkEntrantPointToPointTransferedBlockWithEncryption(SubBlock _block) throws BlockParserException {
			SubBlock res = new SubBlock(_block.getBytes(), _block.getOffset() + getSizeHead(),
					_block.getSize() - getSizeHead());
			try {
				boolean check = signatureChecker
						.verify(_block.getBytes(), res.getOffset(), res.getSize(), _block.getBytes(),
								_block.getOffset(), signature_size_bytes);

				return new SubBlockInfo(res, check, !check);
			} catch (Exception e) {
				return new SubBlockInfo(res, false, true);
			}

		}
		
		@Override
		public SubBlockInfo checkEntrantPointToPointTransferedBlock(SubBlock _block) throws BlockParserException {
			switch (current_step) {
			case NOT_CONNECTED:
			case WAITING_FOR_SIGNATURE_DATA:
				return checkEntrantPointToPointTransferedBlockWithNoEncryptin(_block);
			case WAITING_FOR_ENCRYPTION_DATA:
			case WAITING_FOR_CONNECTION_CONFIRMATION:
			case CONNECTED: {
				return checkEntrantPointToPointTransferedBlockWithEncryption(_block);
			}

			}
			throw new BlockParserException("Unexpected exception");
		}
		private SubBlock signIfPossibleSortantPointToPointTransferedBlockWithNoEncryption(SubBlock _block)
		{
			SubBlock res= new SubBlock(_block.getBytes(), _block.getOffset() - getSizeHead(),
					_block.getSize() + getSizeHead());
			byte[] tab=res.getBytes();
			for (int i=res.getOffset();i<_block.getOffset();i++)
				tab[i]=0;
			return res;

		}
		private SubBlock signIfPossibleSortantPointToPointTransferedBlockWithEncryption(SubBlock _block) throws InvalidKeyException, SignatureException, NoSuchAlgorithmException, InvalidKeySpecException, ShortBufferException, InvalidAlgorithmParameterException, IllegalStateException, IOException
		{
			SubBlock res = new SubBlock(_block.getBytes(), _block.getOffset() - getSizeHead(),
					_block.getSize() + getSizeHead());

			signer.sign(_block.getBytes(), _block.getOffset(), _block.getSize(),
					res.getBytes(), res.getOffset(), signature_size_bytes);
			return res;			
		}
		@Override
		public SubBlock signIfPossibleSortantPointToPointTransferedBlock(SubBlock _block) throws BlockParserException {
			try {
				switch (current_step) {
				case NOT_CONNECTED:
				case WAITING_FOR_SIGNATURE_DATA:
					return signIfPossibleSortantPointToPointTransferedBlockWithNoEncryption(_block);
				case WAITING_FOR_ENCRYPTION_DATA:
				{
					if (isCurrentServerAskingConnection())
						return signIfPossibleSortantPointToPointTransferedBlockWithNoEncryption(_block);
					else
						return signIfPossibleSortantPointToPointTransferedBlockWithEncryption(_block);
				}
				case WAITING_FOR_CONNECTION_CONFIRMATION: 
				case CONNECTED: {
					return signIfPossibleSortantPointToPointTransferedBlockWithEncryption(_block);
				}
				}

			} catch (Exception e) {
				throw new BlockParserException(e);
			}
			throw new BlockParserException("Unexpected exception");
		}

	}

	private class ParserWithNoEncryption extends ParserWithEncryption {
		public ParserWithNoEncryption() {

		}

		@Override
		public int getBodyOutputSizeForEncryption(int size) {
			return size;
		}

		@Override
		public int getMaximumBodyOutputSizeForEncryption(int size) throws BlockParserException {
			return size;
		}

		@Override
		public int getSizeHead() {
			return P2PSecuredConnectionProtocolWithKeyAgreementAlgorithm.this.signature_size_bytes;
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
		public SubBlockInfo getSubBlockWithEncryption(SubBlock _block, boolean enabledEncryption) throws BlockParserException {
			try {
				SubBlock res=new SubBlock(_block.getBytes(), _block.getOffset() + getSizeHead(),
						getBodyOutputSizeForDecryption(_block.getSize() - getSizeHead()));
				signatureChecker.init(_block.getBytes(),
						_block.getOffset(), signature_size_bytes);
				if (getPacketCounter().isLocalActivated())
				{
					
					signatureChecker.update(packetCounter.getMySignatureCounter());
				}
				signatureChecker.update(res.getBytes(), res.getOffset(), res.getSize());
				boolean check = signatureChecker.verify();
				

				return new SubBlockInfo(res, check, !check);
			} catch (Exception e) {
				SubBlock res = new SubBlock(_block.getBytes(), _block.getOffset() + getSizeHead(),
						getBodyOutputSizeForDecryption(_block.getSize() - getSizeHead()));
				return new SubBlockInfo(res, false, true);
			}
		}

		@Override
		public SubBlock getParentBlockWithEncryption(SubBlock _block, boolean excludeFromEncryption) throws InvalidKeyException, SignatureException, NoSuchAlgorithmException, InvalidKeySpecException, ShortBufferException, BlockParserException, InvalidAlgorithmParameterException, IllegalStateException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException, IOException
		{
			int outputSize=getBodyOutputSizeForEncryption(_block.getSize());
			SubBlock res= new SubBlock(_block.getBytes(), _block.getOffset() - getSizeHead(),
					outputSize + getSizeHead());
			int off=_block.getSize()+_block.getOffset();
			byte[] tab=res.getBytes();
			Arrays.fill(tab, off, outputSize+_block.getOffset(), (byte)0);
			

			signer.init();
			if (packetCounter.isDistantActivated())
			{
				signer.update(packetCounter.getOtherSignatureCounter());
			}
			signer.update(_block.getBytes(), _block.getOffset(), _block.getSize());
			
			signer.getSignature(res.getBytes(), res.getOffset());

			return res;
			
		}
		
		
	}

	@Override
	public SubBlockParser getParser() {
		return parser;
	}

	@Override
	protected TransferedBlockChecker getTransferedBlockChecker(TransferedBlockChecker subBlockChercker)
			throws ConnectionException {
		
		try {
			blockCheckerChanged = false;
			return new ConnectionProtocol.NullBlockChecker(subBlockChercker, this.isCrypted(), (short) parser.getSizeHead());
			
			/*if (secret_key_for_signature == null || current_step.compareTo(Step.WAITING_FOR_CONNECTION_CONFIRMATION) <= 0) {
				currentBlockCheckerIsNull = true;
				return new ConnectionProtocol.NullBlockChecker(subBlockChercker, this.isCrypted(), (short) parser.getSizeHead());
			} else {
				currentBlockCheckerIsNull = false;
				return new BlockChecker(subBlockChercker, this.hproperties.signatureType,
						secret_key_for_signature, this.signature_size, this.isCrypted());
			}*/
		} catch (Exception e) {
			blockCheckerChanged = true;
			throw new ConnectionException(e);
		}
	}

	/*private static class BlockChecker extends TransferedBlockChecker {
		private final SymmetricSignatureType signatureType;
		private final int signatureSize;
		private transient SymmetricSignatureCheckerAlgorithm signatureChecker;
		private transient SymmetricSecretKey secretKey;

		protected BlockChecker(TransferedBlockChecker _subChecker, SymmetricSignatureType signatureType,
				SymmetricSecretKey secretKey, int signatureSize, boolean isCrypted) throws NoSuchAlgorithmException {
			super(_subChecker, !isCrypted);
			this.signatureType = signatureType;
			this.secretKey = secretKey;
			this.signatureSize = signatureSize;
			initSignature();
		}

		@Override
		public Integrity checkDataIntegrity() {
			if (signatureType == null)
				return Integrity.FAIL;
			if (signatureChecker == null)
				return Integrity.FAIL;
			if (secretKey == null)
				return Integrity.FAIL;
			return Integrity.OK;
		}

		private void initSignature() throws NoSuchAlgorithmException {
			this.signatureChecker = new SymmetricSignatureCheckerAlgorithm(secretKey);
		}

		private void writeObject(ObjectOutputStream os) throws IOException {
			os.defaultWriteObject();
			byte encodedPK[] = secretKey.encode();
			os.writeInt(encodedPK.length);
			os.write(encodedPK);
		}

		private void readObject(ObjectInputStream is) throws IOException {
			try {
				is.defaultReadObject();
				int len = is.readInt();
				byte encodedPK[] = new byte[len];
				is.read(encodedPK);
				secretKey = SymmetricSecretKey.decode(encodedPK);
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
				signatureChecker.init();
				signatureChecker.update(res.getBytes(), res.getOffset(), res.getSize());
				boolean check = signatureChecker.verify(_block.getBytes(), _block.getOffset(), signatureSize);
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
	public boolean isTransferBlockCheckerChangedImpl() {
		if (secret_key_for_encryption == null || secret_key_for_signature==null || current_step.compareTo(Step.WAITING_FOR_CONNECTION_CONFIRMATION) <= 0) {
			return !currentBlockCheckerIsNull || blockCheckerChanged;
		} else
			return currentBlockCheckerIsNull || blockCheckerChanged;

	}

	@Override
	public PacketCounter getPacketCounter() {
		return packetCounter;
	}
	
	

}
