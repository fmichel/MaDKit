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


import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import com.distrimind.madkit.exceptions.ConnectionException;
import com.distrimind.madkit.exceptions.MessageSerializationException;
import com.distrimind.madkit.kernel.network.connection.AskConnection;
import com.distrimind.madkit.kernel.network.connection.ConnectionMessage;
import com.distrimind.madkit.util.SerializationTools;
import com.distrimind.util.crypto.ASymmetricAuthentifiedSignatureCheckerAlgorithm;
import com.distrimind.util.crypto.ASymmetricAuthentifiedSignerAlgorithm;
import com.distrimind.util.crypto.ASymmetricPrivateKey;
import com.distrimind.util.crypto.ASymmetricPublicKey;
import com.distrimind.util.crypto.Key;


/**
 * 
 * @author Jason Mahdjoub
 * @version 1.1
 * @since MadkitLanEdition 1.0
 */
@SuppressWarnings("ExternalizableWithoutPublicNoArgConstructor")
class PublicKeyMessage extends ConnectionMessage {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3516261115139207424L;

	private transient ASymmetricPublicKey public_key_for_encryption = null;
	private byte[] public_key_for_encryption_bytes;
	private byte[] signedPublicKey;
	private transient byte[] public_key_bytes_distant_for_encryption;
	private transient ASymmetricPublicKey public_key_for_signature = null;
	private byte[] public_key_for_signature_bytes;
	private transient byte[] public_key_bytes_distant_for_signature;
	
	@SuppressWarnings("unused")
	PublicKeyMessage()
	{
		
	}
	
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		public_key_for_encryption_bytes=SerializationTools.readBytes(in, SerializationTools.MAX_KEY_SIZE, false);
		signedPublicKey=SerializationTools.readBytes(in, AskConnection.MAX_SIGNATURE_LENGTH, false);
		public_key_for_signature_bytes=SerializationTools.readBytes(in, AskConnection.MAX_SIGNATURE_LENGTH, false);
		try
		{
			public_key_for_encryption = (ASymmetricPublicKey) Key.decode(public_key_for_encryption_bytes);

			assert public_key_for_signature_bytes != null;
			public_key_for_signature = (ASymmetricPublicKey) Key.decode(public_key_for_signature_bytes);
			ASymmetricAuthentifiedSignatureCheckerAlgorithm checker=new ASymmetricAuthentifiedSignatureCheckerAlgorithm(public_key_for_signature);
			if (!checker.verify(public_key_for_encryption_bytes, signedPublicKey))
				throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN);
			
		} catch (Exception e) {
			if (e instanceof MessageSerializationException)
				throw (MessageSerializationException)e;
			throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN);
		}
	}

	@Override
	public void writeExternal(ObjectOutput oos) throws IOException {
		SerializationTools.writeBytes(oos, public_key_for_encryption_bytes, SerializationTools.MAX_KEY_SIZE, false);
		SerializationTools.writeBytes(oos, signedPublicKey, AskConnection.MAX_SIGNATURE_LENGTH, false);
		SerializationTools.writeBytes(oos, public_key_for_signature_bytes, AskConnection.MAX_SIGNATURE_LENGTH, false);
		
	}
	
	public PublicKeyMessage(ASymmetricPublicKey _public_key_for_encryption, ASymmetricPublicKey _public_key_distant_for_encryption, ASymmetricPublicKey _public_key_for_signature, ASymmetricPublicKey _public_key_distant_for_signature, ASymmetricPrivateKey privateKeyForSignature ) throws ConnectionException {
		try
		{
			public_key_for_encryption = _public_key_for_encryption;
			public_key_for_encryption_bytes = _public_key_for_encryption.encode();
			public_key_bytes_distant_for_encryption = _public_key_distant_for_encryption == null ? null : _public_key_distant_for_encryption.encode();
			public_key_for_signature = _public_key_for_signature;
			public_key_for_signature_bytes = _public_key_for_signature.encode();
			public_key_bytes_distant_for_signature = _public_key_distant_for_signature == null ? null : _public_key_distant_for_signature.encode();
			ASymmetricAuthentifiedSignerAlgorithm signer=new ASymmetricAuthentifiedSignerAlgorithm(privateKeyForSignature);
			signedPublicKey=signer.sign(public_key_for_encryption_bytes);
		}
		catch(Exception e)
		{
			throw new ConnectionException(e);
		}
	}

	
	
	public ASymmetricPublicKey getPublicKeyForEncryption() {
		return public_key_for_encryption;
	}
	public ASymmetricPublicKey getPublicKeyForSignature() {
		return public_key_for_signature;
	}

	

	@Override
	public void corrupt() {
		if (public_key_bytes_distant_for_encryption != null)
			public_key_for_encryption_bytes = public_key_bytes_distant_for_encryption;
		if (public_key_bytes_distant_for_signature != null)
			public_key_for_signature_bytes = public_key_bytes_distant_for_signature;
	}

	@Override
	public boolean excludedFromEncryption() {
		return false;
	}



}