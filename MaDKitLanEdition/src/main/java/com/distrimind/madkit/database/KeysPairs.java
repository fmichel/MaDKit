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
package com.distrimind.madkit.database;

import java.math.BigInteger;
import java.net.InetAddress;
import java.util.HashMap;

import com.distrimind.ood.database.DatabaseRecord;
import com.distrimind.ood.database.SynchronizedTransaction;
import com.distrimind.ood.database.Table;
import com.distrimind.ood.database.TransactionIsolation;
import com.distrimind.ood.database.annotations.Field;
import com.distrimind.ood.database.annotations.NotNull;
import com.distrimind.ood.database.annotations.PrimaryKey;
import com.distrimind.ood.database.exceptions.DatabaseException;
import com.distrimind.util.crypto.ASymmetricAuthentifiedSignatureType;
import com.distrimind.util.crypto.ASymmetricEncryptionType;
import com.distrimind.util.crypto.ASymmetricKeyPair;
import com.distrimind.util.crypto.AbstractSecureRandom;

/**
 * 
 * @author Jason Mahdjoub
 * @version 1.1
 * @since MadkitLanEdition 1.0
 */
public final class KeysPairs extends Table<KeysPairs.Record> {

	protected KeysPairs() throws DatabaseException {
		super();
	}

	public final static class Record extends DatabaseRecord {
		public @NotNull @Field @PrimaryKey long identifier;
		public @Field ASymmetricKeyPair key_pair;
	}

	private BigInteger getBigInteger(byte[] bytes) {
		if (bytes == null || bytes.length == 0)
			return BigInteger.valueOf(0);
		BigInteger res = BigInteger.valueOf(0xFF & bytes[0]);
		for (int i = 1; i < bytes.length; i++)
			res = res.shiftLeft(8).or(BigInteger.valueOf(0xFF & bytes[i]));
		return res;
	}

	protected long getIdentifier(InetAddress address, byte usingTypeId, ASymmetricEncryptionType typeEncryption, ASymmetricAuthentifiedSignatureType typeSignature, short keysize,
			short maximumNumberOfKeysForIpSpectrum) {
		int inet_id = (getBigInteger(address.getAddress()).mod(BigInteger.valueOf(maximumNumberOfKeysForIpSpectrum))
				.intValue() + maximumNumberOfKeysForIpSpectrum * usingTypeId);

		long res = ((long) inet_id) | (((long) ((typeEncryption==null?(typeSignature.ordinal()+ASymmetricEncryptionType.values().length):typeEncryption.ordinal()) & 0xFFFF)) << 32) | (((long) keysize) << 48);
		return res;
	}
	public ASymmetricKeyPair getKeyPair(final InetAddress _inet_address, final byte usingType,
			final ASymmetricEncryptionType algorithm, final short _key_size, final AbstractSecureRandom random,
			final long expiration, final short maximumNumberOfKeysForIpSpectrum) throws DatabaseException {
		return getKeyPair(_inet_address, usingType, algorithm, null, _key_size, random, expiration, maximumNumberOfKeysForIpSpectrum);
	}
	public ASymmetricKeyPair getKeyPair(final InetAddress _inet_address, final byte usingType,
			final ASymmetricAuthentifiedSignatureType algorithm, final short _key_size, final AbstractSecureRandom random,
			final long expiration, final short maximumNumberOfKeysForIpSpectrum) throws DatabaseException {
		return getKeyPair(_inet_address, usingType, null, algorithm, _key_size, random, expiration, maximumNumberOfKeysForIpSpectrum);
	}
	private ASymmetricKeyPair getKeyPair(final InetAddress _inet_address, final byte usingType,
			final ASymmetricEncryptionType algorithmForEncryption, final ASymmetricAuthentifiedSignatureType algorithmForSignature, final short _key_size, final AbstractSecureRandom random,
			final long expiration, final short maximumNumberOfKeysForIpSpectrum) throws DatabaseException {
		try {
			return this.getDatabaseWrapper()
					.runSynchronizedTransaction(new SynchronizedTransaction<ASymmetricKeyPair>() {

						@Override
						public ASymmetricKeyPair run() throws Exception {
							HashMap<String, Object> map = new HashMap<>();
							map.put("identifier", new Long(getIdentifier(_inet_address, usingType, algorithmForEncryption, algorithmForSignature, _key_size,
									maximumNumberOfKeysForIpSpectrum)));
							Record r = getRecord(map);
							if (r == null) {
								ASymmetricKeyPair kp=null;
								if (algorithmForEncryption==null)
									kp = algorithmForSignature
										.getKeyPairGenerator(random, _key_size, System.currentTimeMillis() + expiration)
										.generateKeyPair();
								else
									kp = algorithmForEncryption
									.getKeyPairGenerator(random, _key_size, System.currentTimeMillis() + expiration)
									.generateKeyPair();
									
								map.put("key_pair", kp);
								// map.put("date_key", new Long(System.currentTimeMillis()));
								addRecord(map);
								return kp;
							} else if (r.key_pair.getTimeExpirationUTC() < System.currentTimeMillis()) {
								ASymmetricKeyPair kp=null;
								if (algorithmForEncryption==null)
									kp = algorithmForSignature
										.getKeyPairGenerator(random, _key_size, System.currentTimeMillis() + expiration)
										.generateKeyPair();
								else
									kp = algorithmForEncryption
										.getKeyPairGenerator(random, _key_size, System.currentTimeMillis() + expiration)
										.generateKeyPair();

								map = new HashMap<>();
								map.put("key_pair", kp);
								// map.put("date_key", new Long(System.currentTimeMillis()));

								updateRecord(r, map);
								return kp;
							} else {
								return r.key_pair;
							}

						}

						@Override
						public TransactionIsolation getTransactionIsolation() {
							return TransactionIsolation.TRANSACTION_REPEATABLE_READ;
						}

						@Override
						public boolean doesWriteData() {
							return true;
						}

						@Override
						public void initOrReset() throws Exception {
							
						}
					});
		} catch (Exception e) {
			throw DatabaseException.getDatabaseException(e);
		}

	}
	public ASymmetricKeyPair getNewKeyPair(InetAddress _inet_address, byte usingType,
			final ASymmetricEncryptionType algorithm, final short _key_size, final AbstractSecureRandom random,
			final long expiration, short maximumNumberOfKeysForIpSpectrum) throws DatabaseException {
		return getNewKeyPair(_inet_address, usingType, algorithm, null, _key_size, random, expiration, maximumNumberOfKeysForIpSpectrum);
	}
	public ASymmetricKeyPair getNewKeyPair(InetAddress _inet_address, byte usingType,
			final ASymmetricAuthentifiedSignatureType algorithm, final short _key_size, final AbstractSecureRandom random,
			final long expiration, short maximumNumberOfKeysForIpSpectrum) throws DatabaseException {
		return getNewKeyPair(_inet_address, usingType, null, algorithm, _key_size, random, expiration, maximumNumberOfKeysForIpSpectrum);
	}	
	private ASymmetricKeyPair getNewKeyPair(InetAddress _inet_address, byte usingType,
			final ASymmetricEncryptionType algorithmForEncryption, final ASymmetricAuthentifiedSignatureType algorithmForSignature, final short _key_size, final AbstractSecureRandom random,
			final long expiration, short maximumNumberOfKeysForIpSpectrum) throws DatabaseException {
		final HashMap<String, Object> map = new HashMap<>();
		map.put("identifier", new Long(
				getIdentifier(_inet_address, usingType, algorithmForEncryption, algorithmForSignature, _key_size, maximumNumberOfKeysForIpSpectrum)));

		try {
			return this.getDatabaseWrapper()
					.runSynchronizedTransaction(new SynchronizedTransaction<ASymmetricKeyPair>() {

						@Override
						public ASymmetricKeyPair run() throws Exception {
							Record r = getRecord(map);
							ASymmetricKeyPair kp=null;
							if (algorithmForEncryption==null)
								kp = algorithmForSignature
									.getKeyPairGenerator(random, _key_size, System.currentTimeMillis() + expiration)
									.generateKeyPair();
							else
								kp = algorithmForEncryption
								.getKeyPairGenerator(random, _key_size, System.currentTimeMillis() + expiration)
								.generateKeyPair();
								
							map.put("key_pair", kp);
							
							// map.put("date_key", new Long(System.currentTimeMillis()));
							if (r == null) {
								addRecord(map);
							} else {
								updateRecord(r, map);
							}
							return kp;

						}

						@Override
						public TransactionIsolation getTransactionIsolation() {
							return TransactionIsolation.TRANSACTION_REPEATABLE_READ;
						}

						@Override
						public boolean doesWriteData() {
							return true;
						}

						@Override
						public void initOrReset() throws Exception {
							
						}
					});
		} catch (Exception e) {
			throw DatabaseException.getDatabaseException(e);
		}

	}

}
