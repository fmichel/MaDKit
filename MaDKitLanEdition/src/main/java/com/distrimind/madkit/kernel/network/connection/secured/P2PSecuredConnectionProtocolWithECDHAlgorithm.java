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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;

import com.distrimind.madkit.exceptions.BlockParserException;
import com.distrimind.madkit.exceptions.ConnectionException;
import com.distrimind.madkit.kernel.MadkitProperties;
import com.distrimind.madkit.kernel.network.Block;
import com.distrimind.madkit.kernel.network.NetworkProperties;
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
import com.distrimind.util.crypto.AbstractSecureRandom;
import com.distrimind.util.crypto.EllipticCurveDiffieHellmanAlgorithm;
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
 * @version 1.1
 * @since MadkitLanEdition 1.2
 */
public class P2PSecuredConnectionProtocolWithECDHAlgorithm extends ConnectionProtocol<P2PSecuredConnectionProtocolWithECDHAlgorithm> {
	Step current_step = Step.NOT_CONNECTED;

	private SymmetricSecretKey secret_key_for_encryption = null;
	private SymmetricSecretKey secret_key_for_signature = null;
	
	protected SymmetricEncryptionAlgorithm symmetricAlgorithm = null;
	protected EllipticCurveDiffieHellmanAlgorithm ellipticCurveDiffieHellmanAlgorithmForEncryption=null;
	protected EllipticCurveDiffieHellmanAlgorithm ellipticCurveDiffieHellmanAlgorithmForSignature=null;
	protected SymmetricAuthentifiedSignerAlgorithm signerAlgorithm = null;
	protected SymmetricAuthentifiedSignatureCheckerAlgorithm signatureCheckerAlgorithm = null;
	final int signature_size;
	private final SubBlockParser parser;

	
	private final P2PSecuredConnectionProtocolWithECDHAlgorithmProperties hproperties;
	private final AbstractSecureRandom approvedRandom, approvedRandomForKeys;
	private boolean blockCheckerChanged = true;
	private boolean currentBlockCheckerIsNull = true;
	private byte[] materialKey=null;

	private P2PSecuredConnectionProtocolWithECDHAlgorithm(InetSocketAddress _distant_inet_address,
			InetSocketAddress _local_interface_address, ConnectionProtocol<?> _subProtocol,
			DatabaseWrapper sql_connection, MadkitProperties mkProperties, NetworkProperties _properties, int subProtocolLevel, boolean isServer,
			boolean mustSupportBidirectionnalConnectionInitiative) throws ConnectionException {
		super(_distant_inet_address, _local_interface_address, _subProtocol, sql_connection, _properties,
				subProtocolLevel, isServer, mustSupportBidirectionnalConnectionInitiative);
		hproperties = (P2PSecuredConnectionProtocolWithECDHAlgorithmProperties) super.connection_protocol_properties;
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
		signature_size=sigsize;
		
		
		if (hproperties.enableEncryption)
			parser = new ParserWithEncryption();
		else
			parser = new ParserWithNoEncryption();
	}

	private void checkSymmetricAlgorithm() throws ConnectionException {
		try {
			if (secret_key_for_encryption != null && secret_key_for_signature!=null) {
				if (symmetricAlgorithm == null) {
					symmetricAlgorithm = new SymmetricEncryptionAlgorithm(approvedRandom, secret_key_for_encryption);
				}
				if (signerAlgorithm==null || signatureCheckerAlgorithm==null)
				{
					signerAlgorithm=new SymmetricAuthentifiedSignerAlgorithm(secret_key_for_signature);
					signatureCheckerAlgorithm=new SymmetricAuthentifiedSignatureCheckerAlgorithm(secret_key_for_signature);
					blockCheckerChanged=true;
				}
			} else {
				symmetricAlgorithm = null;
				signerAlgorithm=null;
				signatureCheckerAlgorithm=null;
			}
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeySpecException
				| NoSuchProviderException | InvalidAlgorithmParameterException e) {
			throw new ConnectionException(e);
		}
	}

	private void reset() {
		blockCheckerChanged = true;
		symmetricAlgorithm=null;
		secret_key_for_encryption = null;
		secret_key_for_signature=null;
		ellipticCurveDiffieHellmanAlgorithmForEncryption=null;
		ellipticCurveDiffieHellmanAlgorithmForSignature=null;
		signerAlgorithm=null;
		signatureCheckerAlgorithm=null;
		
	}

	private enum Step {
		NOT_CONNECTED, WAITING_FOR_DATA, WAITING_FOR_CONNECTION_CONFIRMATION, CONNECTED,
	}

	private void initECDHAlgorithm()
	{
		this.ellipticCurveDiffieHellmanAlgorithmForEncryption=hproperties.ellipticCurveDiffieHellmanType.getInstance(approvedRandomForKeys);
		this.ellipticCurveDiffieHellmanAlgorithmForSignature=hproperties.ellipticCurveDiffieHellmanType.getInstance(approvedRandomForKeys);
	}
	


	@Override
	protected ConnectionMessage getNextStep(ConnectionMessage _m) throws ConnectionException {
		switch (current_step) {
		case NOT_CONNECTED: {
			if (_m instanceof AskConnection) {
				AskConnection ask = (AskConnection) _m;
				initECDHAlgorithm();
				current_step = Step.WAITING_FOR_DATA;
				if (ask.isYouAreAsking()) {
					return new AskConnection(false);
				} else {
					try {
						ellipticCurveDiffieHellmanAlgorithmForEncryption.generateAndSetKeyPair();
						ellipticCurveDiffieHellmanAlgorithmForSignature.generateAndSetKeyPair();
						materialKey=new byte[64];
						approvedRandom.nextBytes(materialKey);
						return new ECDHDataMessage(ellipticCurveDiffieHellmanAlgorithmForEncryption.getEncodedPublicKey(), ellipticCurveDiffieHellmanAlgorithmForSignature.getEncodedPublicKey(), materialKey);
					} catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchProviderException | InvalidAlgorithmParameterException e) {
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
		case WAITING_FOR_DATA:{
			if (_m instanceof ECDHDataMessage)
			{
				if (isCurrentServerAskingConnection())
				{
					byte dataForEncryption[], dataForSignature[];
					try {
						ellipticCurveDiffieHellmanAlgorithmForEncryption.generateAndSetKeyPair();
						ellipticCurveDiffieHellmanAlgorithmForSignature.generateAndSetKeyPair();
						
						dataForEncryption = ellipticCurveDiffieHellmanAlgorithmForEncryption.getEncodedPublicKey();
						dataForSignature = ellipticCurveDiffieHellmanAlgorithmForSignature.getEncodedPublicKey();
					} catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchProviderException | InvalidAlgorithmParameterException e) {
						throw new ConnectionException(e);
					}
					try
					{
						materialKey=((ECDHDataMessage) _m).getMaterialKey();
						if (materialKey==null)
							return new ConnectionFinished(distant_inet_address, ConnectionClosedReason.CONNECTION_ANOMALY);
						ellipticCurveDiffieHellmanAlgorithmForEncryption.setDistantPublicKey(((ECDHDataMessage) _m).getDataForEncryption(), hproperties.symmetricEncryptionType, hproperties.symmetricKeySizeBits, materialKey);
						ellipticCurveDiffieHellmanAlgorithmForSignature.setDistantPublicKey(((ECDHDataMessage) _m).getDataForSignature(), hproperties.symmetricSignatureType, hproperties.symmetricKeySizeBits, materialKey);
					}
					catch(Exception e)
					{
						reset();
						current_step=Step.NOT_CONNECTED;
						return new IncomprehensiblePublicKey();
					}
					secret_key_for_encryption=ellipticCurveDiffieHellmanAlgorithmForEncryption.getDerivedKey();
					secret_key_for_signature=ellipticCurveDiffieHellmanAlgorithmForSignature.getDerivedKey();
					checkSymmetricAlgorithm();
					current_step=Step.WAITING_FOR_CONNECTION_CONFIRMATION;
					return new ECDHDataMessage(dataForEncryption, dataForSignature, null);
				}
				else
				{
					try
					{
						ellipticCurveDiffieHellmanAlgorithmForEncryption.setDistantPublicKey(((ECDHDataMessage) _m).getDataForEncryption(), hproperties.symmetricEncryptionType, hproperties.symmetricKeySizeBits, materialKey);
						ellipticCurveDiffieHellmanAlgorithmForSignature.setDistantPublicKey(((ECDHDataMessage) _m).getDataForSignature(), hproperties.symmetricSignatureType, hproperties.symmetricKeySizeBits, materialKey);
					}
					catch(Exception e)
					{
						reset();
						current_step=Step.NOT_CONNECTED;
						return new IncomprehensiblePublicKey();
					}
					secret_key_for_encryption=ellipticCurveDiffieHellmanAlgorithmForEncryption.getDerivedKey();
					secret_key_for_signature=ellipticCurveDiffieHellmanAlgorithmForSignature.getDerivedKey();
					checkSymmetricAlgorithm();
					current_step=Step.WAITING_FOR_CONNECTION_CONFIRMATION;
					return new ConnectionFinished(getDistantInetSocketAddress());
				}
			} else {
				return new UnexpectedMessage(this.getDistantInetSocketAddress());
			}
		}
		case WAITING_FOR_CONNECTION_CONFIRMATION:{
			if (_m instanceof ConnectionFinished)
			{
				if (((ConnectionFinished) _m).getState()==ConnectionProtocol.ConnectionState.CONNECTION_ESTABLISHED)
				{
					
					current_step=Step.CONNECTED;
					if (isCurrentServerAskingConnection())
					{
						return new ConnectionFinished(getDistantInetSocketAddress());
					}
					else
					{
						return null;
					}
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
				case WAITING_FOR_DATA:
					return size;
				case WAITING_FOR_CONNECTION_CONFIRMATION: {
					if (isCurrentServerAskingConnection())
						return size;
					else
						return symmetricAlgorithm.getOutputSizeForEncryption(size)+1;
				}
				case CONNECTED:
					return symmetricAlgorithm.getOutputSizeForEncryption(size)+1;
				}
			} catch (Exception e) {
				throw new BlockParserException(e);
			}
			return size;

		}

		@Override
		public int getBodyOutputSizeForDecryption(int size) throws BlockParserException {
			try {
				switch (current_step) {
				case NOT_CONNECTED:
				case WAITING_FOR_DATA:
					return size;
				case WAITING_FOR_CONNECTION_CONFIRMATION:
				case CONNECTED:
					return symmetricAlgorithm.getOutputSizeForDecryption(size-1);

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
			case WAITING_FOR_DATA:
				return getSubBlockWithNoEncryption(_block);
			case WAITING_FOR_CONNECTION_CONFIRMATION:
			case CONNECTED: {
				return getSubBlockWithEncryption(_block);
			}

			}
			throw new BlockParserException("Unexpected exception");
		}

		public SubBlockInfo getSubBlockWithNoEncryption(SubBlock _block) throws BlockParserException {
			return new SubBlockInfo(new SubBlock(_block.getBytes(), _block.getOffset() + getSizeHead(),
					getBodyOutputSizeForDecryption(_block.getSize() - getSizeHead())), true, false);
		}

		public SubBlockInfo getSubBlockWithEncryption(SubBlock _block) throws BlockParserException {
			int off=_block.getOffset() + getSizeHead();
			boolean excludedFromEncryption=_block.getBytes()[off]==1;
			if (excludedFromEncryption)
			{
				int s=Block.getBlockSize(_block.getBytes(), off+1);
				if (s>Block.BLOCK_SIZE_LIMIT)
					throw new BlockParserException();
				
				try  {
					
					SubBlock res = new SubBlock(new byte[_block.getBytes().length], _block.getOffset() + getSizeHead(),
							s);
	
					boolean check = signatureCheckerAlgorithm
							.verify(_block.getBytes(), res.getOffset(), _block.getSize() - getSizeHead(), _block.getBytes(),
									_block.getOffset(), signature_size);
	
					System.arraycopy(_block.getBytes(), off+4, res.getBytes(), res.getOffset(), s);
					return new SubBlockInfo(res, check, !check);
				} catch (Exception e) {
					SubBlock res = new SubBlock(_block.getBytes(), _block.getOffset() + getSizeHead(),
							getBodyOutputSizeForDecryption(_block.getSize() - getSizeHead()));
					return new SubBlockInfo(res, false, true);
				}
			}
			else
			{
				try (ByteArrayInputStream bais = new ByteArrayInputStream(_block.getBytes(),
						_block.getOffset() + getSizeHead()+1, _block.getSize() - getSizeHead()-1)) {
					byte[] tmp = P2PSecuredConnectionProtocolWithECDHAlgorithm.this.symmetricAlgorithm.decode(bais);
	
					if (tmp.length > getBodyOutputSizeForDecryption(_block.getSize() - getSizeHead()))
						throw new BlockParserException("Invalid block size for decoding.");
	
					SubBlock res = new SubBlock(new byte[_block.getBytes().length], _block.getOffset() + getSizeHead(),
							tmp.length);
	
					boolean check = signatureCheckerAlgorithm
							.verify(_block.getBytes(), res.getOffset(), _block.getSize() - getSizeHead(), _block.getBytes(),
									_block.getOffset(), signature_size);
	
					System.arraycopy(tmp, 0, res.getBytes(), res.getOffset(), tmp.length);
					return new SubBlockInfo(res, check, !check);
				} catch (Exception e) {
					SubBlock res = new SubBlock(_block.getBytes(), _block.getOffset() + getSizeHead(),
							getBodyOutputSizeForDecryption(_block.getSize() - getSizeHead()));
					return new SubBlockInfo(res, false, true);
				}
			}
		}

		public SubBlock getParentBlockWithEncryption(SubBlock _block, boolean excludeFromEncryption) throws InvalidKeyException, SignatureException, NoSuchAlgorithmException, InvalidKeySpecException, ShortBufferException, BlockParserException, InvalidAlgorithmParameterException, IllegalStateException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException, IOException
		{
			int outputSize = getBodyOutputSizeForEncryption(_block.getSize());
			SubBlock res = new SubBlock(new byte[_block.getBytes().length], _block.getOffset() - getSizeHead(),
					outputSize + getSizeHead());
			if (excludeFromEncryption)
			{
				res.getBytes()[_block.getOffset()]=1;
				Block.putShortInt(res.getBytes(), _block.getOffset()+1, _block.getSize());
				System.arraycopy(_block.getBytes(), _block.getOffset(), res.getBytes(), _block.getOffset()+4, _block.getSize());
				signerAlgorithm.sign(res.getBytes(), _block.getOffset(), outputSize, res.getBytes(), res.getOffset(), signature_size);
			}
			else
			{
				byte []tmp=null;
				try(ByteArrayOutputStream baos=new ByteArrayOutputStream(outputSize))
				{
					baos.write(0);
					P2PSecuredConnectionProtocolWithECDHAlgorithm.this.symmetricAlgorithm.encode(_block.getBytes(),
						_block.getOffset(), _block.getSize(), baos);
					baos.flush();
					tmp=baos.toByteArray();
				}
				if (outputSize != tmp.length)
					throw new BlockParserException("Invalid block size for encoding (expected=" + outputSize
							+ ", found=" + tmp.length + ").");
				System.arraycopy(tmp, 0, res.getBytes(), _block.getOffset(), tmp.length);
				signerAlgorithm.sign(tmp, 0, tmp.length,
						res.getBytes(), res.getOffset(), signature_size);
			}
			return res;
			
		}
		
		
		@Override
		public SubBlock getParentBlock(SubBlock _block, boolean excludeFromEncryption) throws BlockParserException {
			try {
				switch (current_step) {
				case NOT_CONNECTED:
				case WAITING_FOR_DATA:
					return getParentBlockWithNoTreatments(_block);
				case WAITING_FOR_CONNECTION_CONFIRMATION: {
					if (isCurrentServerAskingConnection())
						return getParentBlockWithNoTreatments(_block);
					else
						return getParentBlockWithEncryption(_block, excludeFromEncryption);
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
			return P2PSecuredConnectionProtocolWithECDHAlgorithm.this.signature_size;
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
				boolean check = signatureCheckerAlgorithm
						.verify(_block.getBytes(), res.getOffset(), res.getSize(), _block.getBytes(),
								_block.getOffset(), signature_size);

				return new SubBlockInfo(res, check, !check);
			} catch (Exception e) {
				return new SubBlockInfo(res, false, true);
			}

		}
		
		@Override
		public SubBlockInfo checkEntrantPointToPointTransferedBlock(SubBlock _block) throws BlockParserException {
			switch (current_step) {
			case NOT_CONNECTED:
			case WAITING_FOR_DATA:
				return checkEntrantPointToPointTransferedBlockWithNoEncryptin(_block);
			case WAITING_FOR_CONNECTION_CONFIRMATION:
			case CONNECTED: {
				return checkEntrantPointToPointTransferedBlockWithEncryption(_block);
			}

			}
			throw new BlockParserException("Unexpected exception");
		}
		private SubBlock signIfPossibleSortantPointToPointTransferedBlockWithNoEncryption(SubBlock _block)
		{
			return new SubBlock(_block.getBytes().clone(), _block.getOffset() - getSizeHead(),
					_block.getSize() + getSizeHead());
		}
		private SubBlock signIfPossibleSortantPointToPointTransferedBlockWithEncryption(SubBlock _block) throws InvalidKeyException, SignatureException, NoSuchAlgorithmException, InvalidKeySpecException, ShortBufferException, InvalidAlgorithmParameterException, IllegalStateException, IOException
		{
			SubBlock res = new SubBlock(_block.getBytes().clone(), _block.getOffset() - getSizeHead(),
					_block.getSize() + getSizeHead());

			signerAlgorithm.sign(_block.getBytes(), _block.getOffset(), _block.getSize(),
					res.getBytes(), res.getOffset(), signature_size);
			return res;			
		}
		@Override
		public SubBlock signIfPossibleSortantPointToPointTransferedBlock(SubBlock _block) throws BlockParserException {
			try {
				switch (current_step) {
				case NOT_CONNECTED:
				case WAITING_FOR_DATA:
					return signIfPossibleSortantPointToPointTransferedBlockWithNoEncryption(_block);
				case WAITING_FOR_CONNECTION_CONFIRMATION: {
					if (isCurrentServerAskingConnection())
						return signIfPossibleSortantPointToPointTransferedBlockWithNoEncryption(_block);
					else
						return signIfPossibleSortantPointToPointTransferedBlockWithEncryption(_block);
				}
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
		public int getSizeHead() {
			return P2PSecuredConnectionProtocolWithECDHAlgorithm.this.signature_size;
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
		public SubBlockInfo getSubBlockWithEncryption(SubBlock _block) throws BlockParserException {
			try {
			
				boolean check = signatureCheckerAlgorithm
						.verify(_block.getBytes(), _block.getOffset() + getSizeHead(), _block.getSize() - getSizeHead(), _block.getBytes(),
								_block.getOffset(), signature_size);

				return new SubBlockInfo(new SubBlock(_block.getBytes(), _block.getOffset() + getSizeHead(),
						getBodyOutputSizeForDecryption(_block.getSize() - getSizeHead())), check, !check);
			} catch (Exception e) {
				SubBlock res = new SubBlock(_block.getBytes(), _block.getOffset() + getSizeHead(),
						getBodyOutputSizeForDecryption(_block.getSize() - getSizeHead()));
				return new SubBlockInfo(res, false, true);
			}
		}

		@Override
		public SubBlock getParentBlockWithEncryption(SubBlock _block, boolean excludeFromEncryption) throws InvalidKeyException, SignatureException, NoSuchAlgorithmException, InvalidKeySpecException, ShortBufferException, BlockParserException, InvalidAlgorithmParameterException, IllegalStateException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException, IOException
		{
			int outputSize = getBodyOutputSizeForEncryption(_block.getSize());
			SubBlock res = new SubBlock(_block.getBytes(), _block.getOffset() - getSizeHead(),
					outputSize + getSizeHead());

			signerAlgorithm.sign(_block.getBytes(), _block.getOffset(), _block.getSize(),
					res.getBytes(), res.getOffset(), signature_size);
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

}
