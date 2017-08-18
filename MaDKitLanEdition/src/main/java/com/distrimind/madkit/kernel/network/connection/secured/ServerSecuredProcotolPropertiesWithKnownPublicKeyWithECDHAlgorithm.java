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

import java.util.HashMap;
import java.util.Map;

import com.distrimind.madkit.exceptions.ConnectionException;
import com.distrimind.madkit.kernel.network.connection.ConnectionProtocolProperties;
import com.distrimind.util.crypto.ASymmetricEncryptionType;
import com.distrimind.util.crypto.ASymmetricKeyPair;
import com.distrimind.util.crypto.ASymmetricSignatureType;
import com.distrimind.util.crypto.AbstractSecureRandom;
import com.distrimind.util.crypto.SymmetricEncryptionType;

import gnu.vm.jgnu.security.NoSuchAlgorithmException;

/**
 * {@inheritDoc}
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MadkitLanEdition 1.0
 */
public class ServerSecuredProcotolPropertiesWithKnownPublicKeyWithECDHAlgorithm
		extends ConnectionProtocolProperties<ServerSecuredConnectionProtocolWithKnwonPublicKeyWithECDHAlgorithm> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4979144000199527880L;

	public ServerSecuredProcotolPropertiesWithKnownPublicKeyWithECDHAlgorithm() {
		super(ServerSecuredConnectionProtocolWithKnwonPublicKeyWithECDHAlgorithm.class);
	}

	/**
	 * Generate and add an encryption profile with a new key pair, etc.
	 * 
	 * @param random
	 *            a secured random number generator
	 * @param as_type
	 *            tge asymmetric encryption type
	 * @param s_type
	 *            the symmetric encryption type (if null, use default encryption
	 *            type)
	 * @return the encryption profile identifier
	 * @throws NoSuchAlgorithmException
	 */
	public int generateAndAddEncryptionProfile(AbstractSecureRandom random, ASymmetricEncryptionType as_type,
			SymmetricEncryptionType s_type) throws NoSuchAlgorithmException {
		return addEncryptionProfile(as_type.getKeyPairGenerator(random).generateKeyPair(), as_type.getKeyPairGenerator(random).generateKeyPair(), s_type);
	}

	/**
	 * Generate and add an encryption profile with a new key pair, etc.
	 * 
	 * @param random
	 *            a secured random number generator
	 * @param as_type
	 *            tge asymmetric encryption type
	 * @param expirationTimeUTC
	 *            the UTC expiration time of the key pair
	 * @param asymmetricKeySizeBits
	 *            the asymmetric key size in bits
	 * @param signatureType
	 *            the signature type (if null, use default signature type)
	 * @param s_type
	 *            the symmetric encryption type (if null, use default encryption
	 *            type)
	 * @param symmetricKeySizeBits
	 *            the symmetric key size in bits
	 * @return the encryption profile identifier
	 * @throws NoSuchAlgorithmException
	 */
	public int generateAndAddEncryptionProfile(AbstractSecureRandom random, ASymmetricEncryptionType as_type,
			long expirationTimeUTC, short asymmetricKeySizeBits, ASymmetricSignatureType signatureType,
			SymmetricEncryptionType s_type, short symmetricKeySizeBits) throws NoSuchAlgorithmException {
		return addEncryptionProfile(
				as_type.getKeyPairGenerator(random, asymmetricKeySizeBits, expirationTimeUTC).generateKeyPair(),
				as_type.getKeyPairGenerator(random, asymmetricKeySizeBits, expirationTimeUTC).generateKeyPair(),
				signatureType, s_type, symmetricKeySizeBits);
	}

	/**
	 * Add an encryption profile with a new key pair, etc.
	 * 
	 * @param keyPairForEncryption
	 *            the key pair for encryption
	 * @param keyPairForSignature
	 *            the key pair for signature
	 * @param symmetricEncryptionType
	 *            the symmetric encryption type (if null, use default encryption
	 *            type)
	 * @return the encryption profile identifier
	 */
	public int addEncryptionProfile(ASymmetricKeyPair keyPairForEncryption,ASymmetricKeyPair keyPairForSignature, SymmetricEncryptionType symmetricEncryptionType) {
		return this.addEncryptionProfile(keyPairForEncryption, keyPairForSignature, null, symmetricEncryptionType,
				symmetricEncryptionType == null ? (short) -1 : symmetricEncryptionType.getDefaultKeySizeBits());
	}

	/**
	 * Add an encryption profile with a new key pair, etc.
	 * 
	 * @param keyPairForEncryption
	 *            the key pair for encryption
	 * @param keyPairForSignature
	 *            the key pair for signature
	 * @param signatureType
	 *            the signature type (if null, use default signature type)
	 * @param symmetricEncryptionType
	 *            the symmetric encryption type (if null, use default encryption
	 *            type)
	 * @param symmetricKeySizeBits
	 *            the symmetric key size in bits
	 * @return the encryption profile identifier
	 */
	public int addEncryptionProfile(ASymmetricKeyPair keyPairForEncryption,ASymmetricKeyPair keyPairForSignature, ASymmetricSignatureType signatureType,
			SymmetricEncryptionType symmetricEncryptionType, short symmetricKeySizeBits) {
		if (keyPairForEncryption == null)
			throw new NullPointerException("keyPairForEncryption");
		if (keyPairForSignature == null)
			throw new NullPointerException("keyPairForSignature");
		keyPairsForEncryption.put(new Integer(generateNewKeyPairIdentifier()), keyPairForEncryption);
		keyPairsForSignature.put(new Integer(generateNewKeyPairIdentifier()), keyPairForSignature);
		if (signatureType == null)
			signatures.put(new Integer(lastIdentifier), keyPairForSignature.getAlgorithmType().getDefaultSignatureAlgorithm());
		else
			signatures.put(new Integer(lastIdentifier), signatureType);
		if (symmetricEncryptionType == null) {
			symmetricEncryptionType = SymmetricEncryptionType.DEFAULT;
			symmetricKeySizeBits = symmetricEncryptionType.getDefaultKeySizeBits();
		}
		symmetricEncryptionTypes.put(new Integer(lastIdentifier), symmetricEncryptionType);
		symmetricEncryptionKeySizeBits.put(new Integer(lastIdentifier), new Short(symmetricKeySizeBits));
		return lastIdentifier;
	}

	/**
	 * Gets the key pair used for encryption and attached to this connection protocol and the given profile
	 * identifier
	 * 
	 * @param profileIdentifier
	 *            the profile identifier
	 * @return the key pair attached to this connection protocol and the given
	 *         profile identifier
	 */
	public ASymmetricKeyPair getKeyPairForEncryption(int profileIdentifier) {
		return keyPairsForEncryption.get(new Integer(profileIdentifier));
	}
	/**
	 * Gets the key pair used for the message signature and attached to this connection protocol and the given profile
	 * identifier
	 * 
	 * @param profileIdentifier
	 *            the profile identifier
	 * @return the key pair attached to this connection protocol and the given
	 *         profile identifier
	 */
	public ASymmetricKeyPair getKeyPairForSignature(int profileIdentifier) {
		return keyPairsForSignature.get(new Integer(profileIdentifier));
	}

	/**
	 * Gets the signature type attached to this connection protocol and the given
	 * profile identifier
	 * 
	 * @param profileIdentifier
	 *            the profile identifier
	 * @return the signature type attached to this connection protocol and the given
	 *         profile identifier
	 */
	public ASymmetricSignatureType getSignatureType(int profileIdentifier) {
		return signatures.get(new Integer(profileIdentifier));
	}

	public int getMaximumSignatureSizeBits() {
		int res = -1;
		for (Map.Entry<Integer, ASymmetricKeyPair> e : keyPairsForSignature.entrySet()) {
			res = Math.max(res, signatures.get(e.getKey()).getSignatureSizeBits(e.getValue().getKeySize()));
		}
		return res;
	}

	/**
	 * Gets the symmetric encryption type attached to this connection protocol and
	 * the given profile identifier
	 * 
	 * @param profileIdentifier
	 *            the profile identifier
	 * @return the symmetric encryption type attached to this connection protocol
	 *         and the given profile identifier
	 */
	public SymmetricEncryptionType getSymmetricEncryptionType(int profileIdentifier) {
		return symmetricEncryptionTypes.get(new Integer(profileIdentifier));
	}

	/**
	 * Gets the symmetric encryption key size in bits attached to this connection
	 * protocol and the given profile identifier
	 * 
	 * @param profileIdentifier
	 *            the profile identifier
	 * @return the symmetric encryption key size in bits attached to this connection
	 *         protocol and the given profile identifier
	 */
	public short getSymmetricEncryptionKeySizeBits(int profileIdentifier) {
		return symmetricEncryptionKeySizeBits.get(new Integer(profileIdentifier)).shortValue();
	}

	/**
	 * Gets the default key pair (for encryption) attached to this connection protocol and its
	 * default profile
	 * 
	 * @return the default key pair attached to this connection protocol and its
	 *         default profile
	 */
	public ASymmetricKeyPair getDefaultKeyPairForEncryption() {
		return keyPairsForEncryption.get(new Integer(lastIdentifier));
	}
	/**
	 * Gets the default key pair (for signature) attached to this connection protocol and its
	 * default profile
	 * 
	 * @return the default key pair attached to this connection protocol and its
	 *         default profile
	 */
	public ASymmetricKeyPair getDefaultKeyPairForSignature() {
		return keyPairsForSignature.get(new Integer(lastIdentifier));
	}

	/**
	 * Gets the default signature type attached to this connection protocol and its
	 * default profile
	 * 
	 * @return the default signature type attached to this connection protocol and
	 *         its default profile
	 */
	public ASymmetricSignatureType getDefaultSignatureType() {
		return signatures.get(new Integer(lastIdentifier));
	}

	/**
	 * Gets the default symmetric encryption type type attached to this connection
	 * protocol and its default profile
	 * 
	 * @return the default symmetric encryption type attached to this connection
	 *         protocol and its default profile
	 */
	public SymmetricEncryptionType getDefaultSymmetricEncryptionType() {
		return symmetricEncryptionTypes.get(new Integer(lastIdentifier));
	}

	/**
	 * Gets the default symmetric encryption key size in bits attached to this
	 * connection protocol and its default profile
	 * 
	 * @return the default symmetric encryption key size in bits attached to this
	 *         connection protocol and its default profile
	 */
	public short getDefaultSymmetricEncryptionKeySizeBits() {
		return symmetricEncryptionKeySizeBits.get(new Integer(lastIdentifier)).shortValue();
	}

	/**
	 * Gets the last encryption profile identifier
	 * 
	 * @return the last encryption profile identifier
	 */
	public int getLastEncryptionProfileIdentifier() {
		return lastIdentifier;
	}

	/*
	 * public Map<Integer, ASymmetricPublicKey> getPublicKeys() { Map<Integer,
	 * ASymmetricPublicKey> res=new HashMap<>(); for (Map.Entry<Integer,
	 * ASymmetricKeyPair> e : keyPairs.entrySet()) { res.put(e.getKey(),
	 * e.getValue().getASymmetricPublicKey()); } return res; }
	 * 
	 * public Map<Integer, SignatureType> getSignatures() { Map<Integer,
	 * SignatureType> res=new HashMap<>(); for (Map.Entry<Integer, SignatureType> e
	 * : signatures.entrySet()) { res.put(e.getKey(), e.getValue()); } return res; }
	 */

	/**
	 * Tells if the connection must be encrypted or not. If not, only signature
	 * packet will be enabled.
	 */
	public boolean enableEncryption = true;

	/**
	 * The used key pair
	 */
	// ASymmetricKeyPair keyPair=null;

	/**
	 * The used key pairs for encryption
	 */
	private Map<Integer, ASymmetricKeyPair> keyPairsForEncryption = new HashMap<>();

	/**
	 * The used key pairs for signature
	 */
	private Map<Integer, ASymmetricKeyPair> keyPairsForSignature = new HashMap<>();

	/**
	 * The used signatures
	 */
	private Map<Integer, ASymmetricSignatureType> signatures = new HashMap<>();

	private int lastIdentifier = 0;

	private int generateNewKeyPairIdentifier() {
		return ++lastIdentifier;
	}

	/**
	 * The minimum asymetric cipher RSA Key size
	 */
	public final int minASymetricKeySize = 1024;

	/**
	 * Symmetric encryption algorithm
	 */
	private Map<Integer, SymmetricEncryptionType> symmetricEncryptionTypes = new HashMap<>();

	/**
	 * Symmetric encryption key sizes bits
	 */
	private Map<Integer, Short> symmetricEncryptionKeySizeBits = new HashMap<>();

	/**
	 * Default duration of a public key before being regenerated. Must be greater or
	 * equal than 0.
	 */
	public final long defaultASymmetricKeyExpirationMs = 15552000000l;

	/**
	 * The duration of a public key before being regenerated. Must be greater or
	 * equal than 0.
	 */
	public long aSymmetricKeyExpirationMs = defaultASymmetricKeyExpirationMs;

	private boolean checkKeyPairs(Map<Integer, ASymmetricKeyPair> keyPairs) throws ConnectionException
	{
		if (keyPairs == null)
			throw new ConnectionException("The key pairs must defined");
		if (keyPairs.isEmpty())
			throw new ConnectionException("The key pairs must defined");
		boolean valid = false;
		for (Map.Entry<Integer, ASymmetricKeyPair> e : keyPairs.entrySet()) {
			if (e.getValue() == null)
				throw new NullPointerException();
			if (e.getValue().getTimeExpirationUTC() > System.currentTimeMillis()) {
				valid = true;
			}
			int tmp = e.getValue().getKeySize();
			while (tmp != 1) {
				if (tmp % 2 == 0)
					tmp = tmp / 2;
				else
					throw new ConnectionException("The RSA key size have a size of " + e.getValue().getKeySize()
							+ ". This number must correspond to this schema : _rsa_key_size=2^x.");
			}
			if (signatures.get(e.getKey()) == null)
				throw new NullPointerException("No signature found for identifier " + e.getKey());
			if (symmetricEncryptionTypes.get(e.getKey()) == null)
				throw new NullPointerException("No symmetric encryption type found for identifier " + e.getKey());
			if (symmetricEncryptionKeySizeBits.get(e.getKey()) == null)
				throw new NullPointerException(
						"No symmetric encryption key size bits found for identifier " + e.getKey());
		}
		if (keyPairs.get(new Integer(this.lastIdentifier)).getKeySize() < minASymetricKeySize)
			throw new ConnectionException("_rsa_key_size must be greater or equal than " + minASymetricKeySize
					+ " . Moreover, this number must correspond to this schema : _rsa_key_size=2^x.");
		return valid;
	}
	
	void checkProperties() throws ConnectionException {
		boolean valid=true;
		valid|=checkKeyPairs(keyPairsForEncryption);
		valid|=checkKeyPairs(keyPairsForSignature);
		if (!valid) {
			throw new ConnectionException("All given public keys has expired");
		}

	}

	@Override
	protected boolean needsServerSocketImpl() {
		return true;
	}

	@Override
	public boolean canTakeConnectionInitiativeImpl() {
		return false;
	}

	@Override
	public boolean supportBidirectionnalConnectionInitiativeImpl() {
		return false;
	}

	@Override
	protected boolean canBeServer() {
		return true;
	}

}
