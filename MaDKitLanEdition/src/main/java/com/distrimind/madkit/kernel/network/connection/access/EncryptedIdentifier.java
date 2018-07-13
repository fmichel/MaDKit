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

package com.distrimind.madkit.kernel.network.connection.access;

import java.io.IOException;

import gnu.vm.jgnu.security.DigestException;
import gnu.vm.jgnu.security.InvalidAlgorithmParameterException;
import gnu.vm.jgnu.security.InvalidKeyException;
import gnu.vm.jgnu.security.NoSuchAlgorithmException;
import gnu.vm.jgnu.security.NoSuchProviderException;
import gnu.vm.jgnu.security.spec.InvalidKeySpecException;
import gnu.vm.jgnux.crypto.BadPaddingException;
import gnu.vm.jgnux.crypto.IllegalBlockSizeException;
import gnu.vm.jgnux.crypto.NoSuchPaddingException;
import gnu.vm.jgnux.crypto.ShortBufferException;

import com.distrimind.util.crypto.AbstractMessageDigest;
import com.distrimind.util.crypto.AbstractSecureRandom;
import com.distrimind.util.crypto.P2PASymmetricSecretMessageExchanger;

/**
 * Represent an identifier encrypted
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MadKitLanEdition 1.0
 * @see Identifier
 */
@SuppressWarnings("ExternalizableWithoutPublicNoArgConstructor")
public class EncryptedIdentifier extends Identifier {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1396620582869308278L;

	@SuppressWarnings("unused")
	EncryptedIdentifier()
	{
		
	}
	
	/*EncryptedIdentifier(EncryptedCloudIdentifier _cloud_identifier, HostIdentifier _host_identifier) {
		super(_cloud_identifier, _host_identifier);
	}

	EncryptedIdentifier(CloudIdentifier _cloud_identifier, HostIdentifier _host_identifier,
			P2PASymmetricSecretMessageExchanger cipher) throws InvalidKeyException, IOException,
			IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, InvalidKeySpecException,
			NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchProviderException, IllegalStateException, ShortBufferException {
		super(new EncryptedCloudIdentifier(_cloud_identifier, cipher), _host_identifier);

	}*/

	EncryptedIdentifier(Identifier identifier, AbstractSecureRandom random, AbstractMessageDigest messageDigest, byte[] distantGeneratedSalt)
			throws
			DigestException {
		super(new EncryptedCloudIdentifier(identifier.getCloudIdentifier(), random, messageDigest, distantGeneratedSalt), identifier.getHostIdentifier());
	}

	EncryptedIdentifier(Identifier identifier, P2PASymmetricSecretMessageExchanger cipher)
			throws InvalidKeyException, IOException, IllegalBlockSizeException, BadPaddingException,
			NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException,
			InvalidAlgorithmParameterException, NoSuchProviderException, IllegalStateException, ShortBufferException {
		super(new EncryptedCloudIdentifier(identifier.getCloudIdentifier(), cipher), identifier.getHostIdentifier());
	}

	/**
	 * Tells if the given identifier corresponds to the current encrypted
	 * identifier, considering the given cipher.
	 * 
	 * @param originalIdentifier
	 *            the original cloud identifier
	 * @param cipher
	 *            the cipher
	 * @return true if the given cloud identifier corresponds to the current
	 *         encrypted cloud identifier, considering the given cipher.
	 * @throws InvalidKeyException
	 *             if a problem occurs
	 * @throws IllegalAccessException
	 *             if a problem occurs
	 * @throws IOException
	 *             if a problem occurs
	 * @throws BadPaddingException if a problem occurs
	 * @throws IllegalBlockSizeException if a problem occurs
	 * @throws InvalidKeySpecException if a problem occurs
	 * @throws NoSuchAlgorithmException if a problem occurs
	 * @throws NoSuchProviderException if a problem occurs
	 */
	public boolean verifyWithLocalIdentifier(Identifier originalIdentifier, P2PASymmetricSecretMessageExchanger cipher)
			throws InvalidKeyException, IllegalAccessException, IOException, IllegalBlockSizeException,
			BadPaddingException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException {

		if (originalIdentifier == null)
			throw new NullPointerException("originalIdentifier");
		if (cipher == null)
			throw new NullPointerException("cipher");
		if (!originalIdentifier.equalsHostIdentifier(this))
			return false;
		return ((EncryptedCloudIdentifier) this.getCloudIdentifier())
				.verifyWithLocalCloudIdentifier(originalIdentifier.getCloudIdentifier(), cipher);
	}

	public EncryptedCloudIdentifier getEncryptedCloudIdentifier() {
		return ((EncryptedCloudIdentifier) this.getCloudIdentifier());
	}
}
