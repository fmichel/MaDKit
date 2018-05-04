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

import gnu.vm.jgnu.security.InvalidAlgorithmParameterException;
import gnu.vm.jgnu.security.InvalidKeyException;
import gnu.vm.jgnu.security.NoSuchAlgorithmException;
import gnu.vm.jgnu.security.NoSuchProviderException;
import gnu.vm.jgnu.security.SignatureException;
import gnu.vm.jgnu.security.spec.InvalidKeySpecException;
import gnu.vm.jgnu.security.spec.InvalidParameterSpecException;
import gnu.vm.jgnux.crypto.BadPaddingException;
import gnu.vm.jgnux.crypto.IllegalBlockSizeException;
import gnu.vm.jgnux.crypto.NoSuchPaddingException;
import gnu.vm.jgnux.crypto.ShortBufferException;

import com.distrimind.madkit.exceptions.MessageSerializationException;
import com.distrimind.madkit.kernel.network.connection.AskConnection;
import com.distrimind.madkit.util.SerializationTools;
import com.distrimind.util.crypto.ASymmetricKeyWrapperType;
import com.distrimind.util.crypto.ASymmetricPublicKey;
import com.distrimind.util.crypto.AbstractSecureRandom;
import com.distrimind.util.crypto.SymmetricAuthentifiedSignatureCheckerAlgorithm;
import com.distrimind.util.crypto.SymmetricAuthentifiedSignerAlgorithm;
import com.distrimind.util.crypto.SymmetricSecretKey;

/**
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MadkitLanEdition 1.0
 */
class AskClientServerConnection extends AskConnection {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6607916237726396986L;

	//private final transient byte[] distantPublicKeyForEncryptionEncoded;
	private byte[] secretKeyForEncryption, secretKeyForSignature, signatureOfSecretKeyForEncryption;

	AskClientServerConnection()
	{
		
	}
	
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		secretKeyForEncryption=SerializationTools.readBytes(in, MAX_SECRET_KEY_LENGTH, true);
		secretKeyForSignature=SerializationTools.readBytes(in, MAX_SECRET_KEY_LENGTH, false);
		signatureOfSecretKeyForEncryption=SerializationTools.readBytes(in, MAX_SIGNATURE_LENGTH, true);
		if (secretKeyForEncryption!=null && secretKeyForEncryption.length == 0)
			throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN);
		if (secretKeyForSignature.length == 0)
			throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN);
		if (this.isYouAreAsking())
			throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN);
		if (secretKeyForEncryption!=null && signatureOfSecretKeyForEncryption==null)
			throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN);
	}


	@Override
	public void writeExternal(ObjectOutput oos) throws IOException {
		super.writeExternal(oos);
		SerializationTools.writeBytes(oos, secretKeyForEncryption, MAX_SECRET_KEY_LENGTH, true);
		SerializationTools.writeBytes(oos, secretKeyForSignature, MAX_SECRET_KEY_LENGTH, false);
		SerializationTools.writeBytes(oos, signatureOfSecretKeyForEncryption, MAX_SIGNATURE_LENGTH, true);
		
	}
	
	
	AskClientServerConnection(AbstractSecureRandom random, ASymmetricKeyWrapperType keyWrapper, SymmetricSecretKey encryptionSecretKey,SymmetricSecretKey signatureSecretKey,			
			ASymmetricPublicKey distantPublicKeyForEncryption) throws InvalidKeyException, InvalidAlgorithmParameterException,
			IllegalBlockSizeException, BadPaddingException, IOException, IllegalStateException,
			NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException, NoSuchPaddingException, SignatureException, ShortBufferException {
		super(false);
		if (keyWrapper == null)
			throw new NullPointerException("symmetricAlgo");
		if (encryptionSecretKey == null)
			throw new NullPointerException("encryptionSecretKey");
		if (signatureSecretKey == null)
			throw new NullPointerException("signatureSecretKey");
		if (distantPublicKeyForEncryption == null)
			throw new NullPointerException("distantPublicKeyForEncryption");
		
		this.secretKeyForEncryption=keyWrapper.wrapKey(random, distantPublicKeyForEncryption, encryptionSecretKey);
		this.secretKeyForSignature=keyWrapper.wrapKey(random, distantPublicKeyForEncryption, signatureSecretKey);
		SymmetricAuthentifiedSignerAlgorithm signer=new SymmetricAuthentifiedSignerAlgorithm(signatureSecretKey);
		this.signatureOfSecretKeyForEncryption=signer.sign(secretKeyForEncryption);
		//this.distantPublicKeyForEncryptionEncoded = asymmetricAlgo.encode(distantPublicKeyForEncryption.encode());
	}
	AskClientServerConnection(AbstractSecureRandom random, ASymmetricKeyWrapperType keyWrapper, SymmetricSecretKey signatureSecretKey,			
			ASymmetricPublicKey distantPublicKeyForEncryption) throws InvalidKeyException, InvalidAlgorithmParameterException,
			IllegalBlockSizeException, BadPaddingException, IOException, IllegalStateException,
			NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException, NoSuchPaddingException {
		super(false);
		if (keyWrapper == null)
			throw new NullPointerException("symmetricAlgo");
		if (signatureSecretKey == null)
			throw new NullPointerException("signatureSecretKey");
		if (distantPublicKeyForEncryption == null)
			throw new NullPointerException("distantPublicKeyForEncryption");
		
		this.secretKeyForEncryption=null;
		this.secretKeyForSignature=keyWrapper.wrapKey(random, distantPublicKeyForEncryption, signatureSecretKey);
		this.signatureOfSecretKeyForEncryption=null;
		//this.distantPublicKeyForEncryptionEncoded = asymmetricAlgo.encode(distantPublicKeyForEncryption.encode());
	}

	byte[] getSecretKeyForEncryption() {
		return secretKeyForEncryption;
	}
	byte[] getSecretKeyForSignature() {
		return secretKeyForSignature;
	}
	
	boolean checkSignedSecretKey(SymmetricSecretKey signatureSecretKey)
	{
		if (secretKeyForEncryption!=null)
		{
			
			try {
				SymmetricAuthentifiedSignatureCheckerAlgorithm checker=new SymmetricAuthentifiedSignatureCheckerAlgorithm(signatureSecretKey);
				return checker.verify(this.secretKeyForEncryption, this.signatureOfSecretKeyForEncryption);
			} catch (InvalidKeyException | SignatureException | NoSuchAlgorithmException | InvalidKeySpecException
					| ShortBufferException | IllegalStateException | NoSuchProviderException
					| InvalidAlgorithmParameterException | InvalidParameterSpecException | IOException e) {
				return false;
			}
		}
		else
			return false;
	}

	/*byte[] getEncodedPublicKeyForEncryption() {
		return publicKeyForEncryptionEncoded;
	}
	byte[] getEncodedPublicKeyForSignature() {
		return publicKeyForSignatureEncoded;
	}*/


	
	

	@Override
	public void corrupt() {
		byte[] tmp=secretKeyForEncryption;
		secretKeyForEncryption=secretKeyForSignature;
		secretKeyForSignature=tmp;
	}
	
	

}