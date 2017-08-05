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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;

import gnu.vm.jgnu.security.InvalidAlgorithmParameterException;
import gnu.vm.jgnu.security.InvalidKeyException;
import gnu.vm.jgnu.security.NoSuchAlgorithmException;
import gnu.vm.jgnu.security.NoSuchProviderException;
import gnu.vm.jgnu.security.SignatureException;
import gnu.vm.jgnu.security.spec.InvalidKeySpecException;
import gnu.vm.jgnux.crypto.BadPaddingException;
import gnu.vm.jgnux.crypto.IllegalBlockSizeException;
import gnu.vm.jgnux.crypto.NoSuchPaddingException;

import com.distrimind.madkit.database.KeysPairs;
import com.distrimind.madkit.exceptions.BlockParserException;
import com.distrimind.madkit.exceptions.ConnectionException;
import com.distrimind.madkit.kernel.network.NetworkProperties;
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

import com.distrimind.ood.database.exceptions.DatabaseException;
import com.distrimind.util.crypto.ASymmetricKeyPair;
import com.distrimind.util.crypto.ASymmetricPublicKey;
import com.distrimind.util.crypto.ASymmetricSignatureType;
import com.distrimind.util.crypto.AbstractSecureRandom;
import com.distrimind.util.crypto.AbstractSignature;
import com.distrimind.util.crypto.P2PASymmetricEncryptionAlgorithm;
import com.distrimind.util.crypto.SecureRandomType;
import com.distrimind.util.crypto.SymmetricEncryptionAlgorithm;
import com.distrimind.util.crypto.SymmetricSecretKey;

/**
 * {@inheritDoc}
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MadkitLanEdition 1.0
 */
public class P2PSecuredConnectionProtocol extends ConnectionProtocol<P2PSecuredConnectionProtocol> {
	Step current_step = Step.NOT_CONNECTED;

	private ASymmetricKeyPair myKeyPair = null;
	private ASymmetricPublicKey distant_public_key = null;
	private SymmetricSecretKey secret_key = null;
	protected P2PASymmetricEncryptionAlgorithm aSymmetricAlgorithm = null;
	protected SymmetricEncryptionAlgorithm symmetricAlgorithm = null;

	final int signature_size;
	private final SubBlockParser parser;

	private long aSymetricKeySizeExpiration;
	private final P2PSecuredConnectionProtocolProperties hproperties;
	private final AbstractSecureRandom random;
	private boolean blockCheckerChanged = true;
	private boolean currentBlockCheckerIsNull = true;

	private P2PSecuredConnectionProtocol(InetSocketAddress _distant_inet_address,
			InetSocketAddress _local_interface_address, ConnectionProtocol<?> _subProtocol,
			DatabaseWrapper sql_connection, NetworkProperties _properties, int subProtocolLevel, boolean isServer,
			boolean mustSupportBidirectionnalConnectionInitiative) throws ConnectionException {
		super(_distant_inet_address, _local_interface_address, _subProtocol, sql_connection, _properties,
				subProtocolLevel, isServer, mustSupportBidirectionnalConnectionInitiative);
		hproperties = (P2PSecuredConnectionProtocolProperties) super.connection_protocol_properties;

		hproperties.checkProperties();

		signature_size = hproperties.signatureType.getSignatureSizeBytes(hproperties.aSymetricKeySize);

		if (hproperties.aSymmetricKeyExpirationMs < 0)
			aSymetricKeySizeExpiration = hproperties.defaultASymmetricKeyExpirationMs;
		else
			aSymetricKeySizeExpiration = hproperties.aSymmetricKeyExpirationMs;
		try {
			random = SecureRandomType.DEFAULT.getInstance();
		} catch (NoSuchAlgorithmException | NoSuchProviderException e) {
			throw new ConnectionException(e);
		}
		generateSecretKey();
		if (hproperties.enableEncryption)
			parser = new ParserWithEncryption();
		else
			parser = new ParserWithNoEncryption();
	}

	private void setPublicPrivateKeys() throws ConnectionException {
		blockCheckerChanged |= !(myKeyPair == null
				|| (isCrypted() ? current_step.compareTo(Step.WAITING_FIRST_MESSAGE) <= 0
						: current_step.compareTo(Step.WAITING_FIRST_MESSAGE) < 0));
		try {
			aSymmetricAlgorithm = null;
			if (sql_connection != null)
				myKeyPair = ((KeysPairs) sql_connection.getTableInstance(KeysPairs.class)).getKeyPair(
						distant_inet_address.getAddress(), NetworkProperties.connectionProtocolDatabaseUsingCode,
						hproperties.aSymetricEncryptionType, hproperties.aSymetricKeySize, this.random,
						aSymetricKeySizeExpiration, network_properties.maximumNumberOfCryptoKeysForIpsSpectrum);
			else
				myKeyPair = hproperties.aSymetricEncryptionType
						.getKeyPairGenerator(random, hproperties.aSymetricKeySize).generateKeyPair();
		} catch (NoSuchAlgorithmException | DatabaseException e) {
			myKeyPair = null;
			aSymmetricAlgorithm = null;
			throw new ConnectionException(e);
		}

	}

	private void setNewPublicPrivateKeys() throws ConnectionException {
		blockCheckerChanged |= !(myKeyPair == null
				|| (isCrypted() ? current_step.compareTo(Step.WAITING_FIRST_MESSAGE) <= 0
						: current_step.compareTo(Step.WAITING_FIRST_MESSAGE) < 0));
		try {
			aSymmetricAlgorithm = null;
			if (sql_connection != null)
				myKeyPair = (((KeysPairs) sql_connection.getTableInstance(KeysPairs.class)).getNewKeyPair(
						distant_inet_address.getAddress(), NetworkProperties.connectionProtocolDatabaseUsingCode,
						hproperties.aSymetricEncryptionType, hproperties.aSymetricKeySize, random,
						aSymetricKeySizeExpiration, network_properties.maximumNumberOfCryptoKeysForIpsSpectrum));
			else
				myKeyPair = hproperties.aSymetricEncryptionType
						.getKeyPairGenerator(random, hproperties.aSymetricKeySize).generateKeyPair();
		} catch (NoSuchAlgorithmException | DatabaseException e) {
			myKeyPair = null;
			aSymmetricAlgorithm = null;
			throw new ConnectionException(e);
		}
	}

	private void checkAsymetricAlgorithm() throws ConnectionException {
		try {
			if (myKeyPair != null && distant_public_key != null) {
				if (aSymmetricAlgorithm == null) {
					aSymmetricAlgorithm = new P2PASymmetricEncryptionAlgorithm(myKeyPair, distant_public_key);
				}
			} else {
				aSymmetricAlgorithm = null;
			}
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeySpecException
				| NoSuchProviderException e) {
			throw new ConnectionException(e);
		}
	}

	private void setDistantPublicKey(ASymmetricPublicKey _key) {
		aSymmetricAlgorithm = null;
		distant_public_key = _key;
	}

	private void resetPublicPrivateKeys() {
		blockCheckerChanged = true;
		myKeyPair = null;
		distant_public_key = null;
		secret_key = null;
	}

	private enum Step {
		NOT_CONNECTED, WAITING_FOR_PUBLIC_KEY, WAITING_FOR_SECRET_KEY, WAITING_FIRST_MESSAGE, WAITING_FOR_CONNECTION_CONFIRMATION, CONNECTED,
	}

	private void generateSecretKey() throws ConnectionException {
		try {
			secret_key = hproperties.symmetricEncryptionType.getKeyGenerator(random, hproperties.SymmetricKeySizeBits)
					.generateKey();
			byte seed[] = new byte[64];
			random.nextBytes(seed);
			symmetricAlgorithm = new SymmetricEncryptionAlgorithm(secret_key, SecureRandomType.DEFAULT, seed);
		} catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException
				| InvalidAlgorithmParameterException | NoSuchProviderException | InvalidKeySpecException e) {
			secret_key = null;
			symmetricAlgorithm = null;
			throw new ConnectionException(e);
		}
	}

	private byte[] encodeSecretKey() throws ConnectionException {
		try {
			return symmetricAlgorithm.encodeKey(aSymmetricAlgorithm);
		} catch (InvalidKeyException | InvalidAlgorithmParameterException | IOException | IllegalBlockSizeException
				| BadPaddingException | IllegalStateException | NoSuchAlgorithmException | InvalidKeySpecException
				| NoSuchProviderException e) {
			throw new ConnectionException(e);
		}

	}

	private void decodeSecretKey(byte[] _secret_key) throws ConnectionException {
		try {
			byte seed[] = new byte[64];
			random.nextBytes(seed);
			symmetricAlgorithm = SymmetricEncryptionAlgorithm.getInstance(SecureRandomType.DEFAULT, seed, _secret_key,
					aSymmetricAlgorithm);
			secret_key = symmetricAlgorithm.getSecretKey();
		} catch (InvalidKeyException | InvalidAlgorithmParameterException | IOException | NoSuchAlgorithmException
				| NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | NoSuchProviderException
				| IllegalArgumentException | InvalidKeySpecException e) {
			throw new ConnectionException(e);
		}
	}

	@Override
	protected ConnectionMessage getNextStep(ConnectionMessage _m) throws ConnectionException {
		switch (current_step) {
		case NOT_CONNECTED: {
			if (_m instanceof AskConnection) {
				AskConnection ask = (AskConnection) _m;
				setPublicPrivateKeys();
				if (ask.isYouAreAsking()) {
					current_step = Step.WAITING_FOR_PUBLIC_KEY;
					return new AskConnection(false);
				} else {
					current_step = Step.WAITING_FOR_PUBLIC_KEY;
					return new PublicKeyMessage(myKeyPair.getASymmetricPublicKey(), distant_public_key);
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
		case WAITING_FOR_PUBLIC_KEY: {
			if (_m instanceof PublicKeyMessage) {
				if (((PublicKeyMessage) _m).getPublicKey().equals(myKeyPair.getASymmetricPublicKey())) {
					current_step = Step.WAITING_FOR_PUBLIC_KEY;
					setNewPublicPrivateKeys();
					return new SimilarPublicKeysError();
				}
				setDistantPublicKey(((PublicKeyMessage) _m).getPublicKey());
				checkAsymetricAlgorithm();
				if (isCurrentServerAskingConnection()) {
					current_step = Step.WAITING_FOR_SECRET_KEY;
					return new PublicKeyMessage(myKeyPair.getASymmetricPublicKey(), distant_public_key);
				} else {
					// generateSecretKey();
					current_step = Step.WAITING_FIRST_MESSAGE;
					byte[] encoded_secret_key = encodeSecretKey();
					return new SecretKeyMessage(encoded_secret_key);
				}
			} else if (_m instanceof SimilarPublicKeysError) {
				current_step = Step.WAITING_FOR_PUBLIC_KEY;
				setNewPublicPrivateKeys();
				return new PublicKeyMessage(myKeyPair.getASymmetricPublicKey(), distant_public_key);
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
		case WAITING_FOR_SECRET_KEY: {
			if (_m instanceof SecretKeyMessage) {
				try {
					decodeSecretKey(((SecretKeyMessage) _m).secret_key);
				} catch (ConnectionException e) {
					return new IncomprehensibleSecretKey();
				}
				current_step = Step.WAITING_FIRST_MESSAGE;
				return new FirstMessage();
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
		case WAITING_FIRST_MESSAGE: {
			if (_m instanceof IncomprehensibleSecretKey) {
				generateSecretKey();
				current_step = Step.WAITING_FIRST_MESSAGE;
				byte[] encoded_secret_key = encodeSecretKey();
				return new SecretKeyMessage(encoded_secret_key);
			} else if (_m instanceof FirstMessage) {
				current_step = Step.WAITING_FOR_CONNECTION_CONFIRMATION;
				if (!isCurrentServerAskingConnection()) {
					return new FirstMessage();
				} else {
					return new ConnectionFinished(getDistantInetSocketAddress());
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
		case WAITING_FOR_CONNECTION_CONFIRMATION: {
			if (_m instanceof ConnectionFinished && ((ConnectionFinished) _m).getState()
					.equals(ConnectionProtocol.ConnectionState.CONNECTION_ESTABLISHED)) {
				current_step = Step.CONNECTED;
				if (!isCurrentServerAskingConnection())
					return new ConnectionFinished(getDistantInetSocketAddress());
				else
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
		return true;
	}

	@Override
	protected void closeConnection(ConnectionClosedReason _reason) {
		if (_reason.equals(ConnectionClosedReason.CONNECTION_ANOMALY))
			resetPublicPrivateKeys();
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
				case WAITING_FOR_PUBLIC_KEY:
				case WAITING_FOR_SECRET_KEY:
				case WAITING_FIRST_MESSAGE: {
					return size;
				}
				case WAITING_FOR_CONNECTION_CONFIRMATION:
				case CONNECTED:
					return symmetricAlgorithm.getOutputSizeForEncryption(size);
				}
			} catch (InvalidKeyException | InvalidAlgorithmParameterException | NoSuchAlgorithmException
					| InvalidKeySpecException | NoSuchProviderException e) {
				throw new BlockParserException(e);
			}
			return size;

		}

		@Override
		public int getBodyOutputSizeForDecryption(int size) throws BlockParserException {
			try {
				switch (current_step) {
				case NOT_CONNECTED:
				case WAITING_FOR_PUBLIC_KEY:
				case WAITING_FOR_SECRET_KEY: {
					return size;
				}
				case WAITING_FIRST_MESSAGE: {
					if (isCurrentServerAskingConnection())
						return symmetricAlgorithm.getOutputSizeForDecryption(size);
					else
						return size;
				}
				case WAITING_FOR_CONNECTION_CONFIRMATION:
				case CONNECTED:
					return symmetricAlgorithm.getOutputSizeForDecryption(size);

				}
			} catch (InvalidKeyException | InvalidAlgorithmParameterException | NoSuchAlgorithmException
					| InvalidKeySpecException | NoSuchProviderException e) {
				throw new BlockParserException(e);
			}
			return size;

		}

		@Override
		public SubBlockInfo getSubBlock(SubBlock _block) throws BlockParserException {

			switch (current_step) {
			case NOT_CONNECTED:
			case WAITING_FOR_PUBLIC_KEY:
			case WAITING_FOR_SECRET_KEY: {

				return getSubBlockWithNoEncryption(_block);
			}
			case WAITING_FIRST_MESSAGE:
				if (isCurrentServerAskingConnection()) {
					return getSubBlockWithEncryption(_block);
				} else {
					return getSubBlockWithNoEncryption(_block);
				}

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
			try (ByteArrayInputStream bais = new ByteArrayInputStream(_block.getBytes(),
					_block.getOffset() + getSizeHead(), _block.getSize() - getSizeHead())) {
				byte[] tmp = P2PSecuredConnectionProtocol.this.symmetricAlgorithm.decode(bais);

				if (tmp.length > getBodyOutputSizeForDecryption(_block.getSize() - getSizeHead()))
					throw new BlockParserException("Invalid block size for decoding.");

				SubBlock res = new SubBlock(new byte[_block.getBytes().length], _block.getOffset() + getSizeHead(),
						tmp.length);

				boolean check = P2PSecuredConnectionProtocol.this.aSymmetricAlgorithm.getSignatureCheckerAlgorithm()
						.verify(_block.getBytes(), res.getOffset(), _block.getSize() - getSizeHead(), _block.getBytes(),
								_block.getOffset(), P2PSecuredConnectionProtocol.this.signature_size);

				System.arraycopy(tmp, 0, res.getBytes(), res.getOffset(), tmp.length);
				return new SubBlockInfo(res, check, !check);
			} catch (SignatureException | IOException | InvalidKeyException | InvalidAlgorithmParameterException
					| IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException
					| InvalidKeySpecException | NoSuchProviderException e) {
				SubBlock res = new SubBlock(_block.getBytes(), _block.getOffset() + getSizeHead(),
						getBodyOutputSizeForDecryption(_block.getSize() - getSizeHead()));
				return new SubBlockInfo(res, false, true);
			}

		}

		@Override
		public SubBlock getParentBlock(SubBlock _block) throws BlockParserException {
			try {
				switch (current_step) {
				case NOT_CONNECTED:
				case WAITING_FOR_PUBLIC_KEY:
				case WAITING_FOR_SECRET_KEY:
				case WAITING_FIRST_MESSAGE: {
					return getParentBlockWithNoTreatments(_block);
				}
				case WAITING_FOR_CONNECTION_CONFIRMATION:
				case CONNECTED: {

					int outputSize = getBodyOutputSizeForEncryption(_block.getSize());
					SubBlock res = new SubBlock(new byte[_block.getBytes().length], _block.getOffset() - getSizeHead(),
							outputSize + getSizeHead());

					byte[] tmp = P2PSecuredConnectionProtocol.this.symmetricAlgorithm.encode(_block.getBytes(),
							_block.getOffset(), _block.getSize());
					if (outputSize != tmp.length)
						throw new BlockParserException("Invalid block size for encoding (expected=" + outputSize
								+ ", found=" + tmp.length + ").");
					System.arraycopy(tmp, 0, res.getBytes(), _block.getOffset(), tmp.length);
					P2PSecuredConnectionProtocol.this.aSymmetricAlgorithm.getSignerAlgorithm().sign(tmp, 0, tmp.length,
							res.getBytes(), res.getOffset(), P2PSecuredConnectionProtocol.this.signature_size);
					return res;
				}
				}

			} catch (SignatureException | InvalidKeyException | InvalidAlgorithmParameterException | IOException
					| IllegalBlockSizeException | BadPaddingException | IllegalStateException | NoSuchAlgorithmException
					| InvalidKeySpecException | NoSuchProviderException e) {
				throw new BlockParserException(e);
			}
			throw new BlockParserException("Unexpected exception");

		}

		@Override
		public int getSizeHead() {
			return P2PSecuredConnectionProtocol.this.signature_size;
		}

		@Override
		public int getMaximumSizeHead() {
			return getSizeHead();
		}

	}

	private class ParserWithNoEncryption extends SubBlockParser {
		public ParserWithNoEncryption() {

		}

		@Override
		public int getBodyOutputSizeForEncryption(int size) {
			return size;
		}

		@Override
		public SubBlockInfo getSubBlock(SubBlock _block) throws BlockParserException {

			switch (current_step) {
			case NOT_CONNECTED:
			case WAITING_FOR_PUBLIC_KEY:
			case WAITING_FOR_SECRET_KEY: {
				return new SubBlockInfo(new SubBlock(_block.getBytes(), _block.getOffset() + getSizeHead(),
						getBodyOutputSizeForDecryption(_block.getSize() - getSizeHead())), true, false);
			}
			case WAITING_FIRST_MESSAGE:
			case WAITING_FOR_CONNECTION_CONFIRMATION:
			case CONNECTED: {
				SubBlock res = new SubBlock(_block.getBytes(), _block.getOffset() + getSizeHead(),
						getBodyOutputSizeForDecryption(_block.getSize() - getSizeHead()));
				try {
					boolean check = P2PSecuredConnectionProtocol.this.aSymmetricAlgorithm.getSignatureCheckerAlgorithm()
							.verify(res.getBytes(), res.getOffset(), res.getSize(), _block.getBytes(),
									_block.getOffset(), P2PSecuredConnectionProtocol.this.signature_size);

					return new SubBlockInfo(res, check, !check);
				} catch (SignatureException | InvalidKeyException | NoSuchAlgorithmException
						| InvalidKeySpecException e) {
					return new SubBlockInfo(res, false, true);
				}
			}

			}
			throw new BlockParserException("Unexpected exception");
		}

		@Override
		public SubBlock getParentBlock(SubBlock _block) throws BlockParserException {
			try {

				switch (current_step) {
				case NOT_CONNECTED:
				case WAITING_FOR_PUBLIC_KEY:
				case WAITING_FOR_SECRET_KEY: {
					return getParentBlockWithNoTreatments(_block);
				}
				case WAITING_FIRST_MESSAGE:
				case WAITING_FOR_CONNECTION_CONFIRMATION:
				case CONNECTED: {
					SubBlock res = getParentBlockWithNoTreatments(_block);
					P2PSecuredConnectionProtocol.this.aSymmetricAlgorithm.getSignerAlgorithm().sign(_block.getBytes(),
							_block.getOffset(), _block.getSize(), res.getBytes(), res.getOffset(),
							P2PSecuredConnectionProtocol.this.signature_size);
					return res;
				}
				}

			} catch (SignatureException | InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException e) {
				throw new BlockParserException(e);
			}
			throw new BlockParserException("Unexpected exception");

		}

		@Override
		public int getSizeHead() {
			return P2PSecuredConnectionProtocol.this.signature_size;
		}

		@Override
		public int getBodyOutputSizeForDecryption(int _size) {
			return _size;
		}

		@Override
		public int getMaximumSizeHead() {
			return getSizeHead();
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
			if (myKeyPair == null || (isCrypted() ? current_step.compareTo(Step.WAITING_FIRST_MESSAGE) <= 0
					: current_step.compareTo(Step.WAITING_FIRST_MESSAGE) < 0)) {
				currentBlockCheckerIsNull = true;
				return new ConnectionProtocol.NullBlockChecker(subBlockChercker, this.isCrypted(),
						(short) parser.getSizeHead());
			} else {
				currentBlockCheckerIsNull = false;
				return new BlockChecker(subBlockChercker, this.hproperties.signatureType,
						this.myKeyPair.getASymmetricPublicKey(), this.signature_size, this.isCrypted());
			}
		} catch (Exception e) {
			blockCheckerChanged = true;
			throw new ConnectionException(e);
		}
	}

	private static class BlockChecker extends TransferedBlockChecker {
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

	}

	@Override
	public boolean needsMadkitLanEditionDatabase() {
		return true;
	}

	@Override
	public boolean isTransferBlockCheckerChangedImpl() {
		if (myKeyPair == null || (isCrypted() ? current_step.compareTo(Step.WAITING_FIRST_MESSAGE) <= 0
				: current_step.compareTo(Step.WAITING_FIRST_MESSAGE) < 0)) {
			return !currentBlockCheckerIsNull || blockCheckerChanged;
		} else
			return currentBlockCheckerIsNull || blockCheckerChanged;

	}

}
