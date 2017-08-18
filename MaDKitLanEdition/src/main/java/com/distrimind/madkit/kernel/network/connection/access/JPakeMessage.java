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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.bouncycastle.crypto.CryptoException;

import com.distrimind.madkit.kernel.KernelAddress;
import com.distrimind.util.crypto.AbstractMessageDigest;
import com.distrimind.util.crypto.AbstractSecureRandom;
import com.distrimind.util.crypto.P2PJPAKESecretMessageExchanger;

import gnu.vm.jgnu.security.DigestException;
import gnu.vm.jgnu.security.InvalidAlgorithmParameterException;
import gnu.vm.jgnu.security.InvalidKeyException;
import gnu.vm.jgnu.security.NoSuchAlgorithmException;
import gnu.vm.jgnu.security.NoSuchProviderException;
import gnu.vm.jgnu.security.spec.InvalidKeySpecException;
import gnu.vm.jgnux.crypto.BadPaddingException;
import gnu.vm.jgnux.crypto.IllegalBlockSizeException;
import gnu.vm.jgnux.crypto.NoSuchPaddingException;

/**
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MadkitLanEdition 1.2
 */
class JPakeMessage extends AccessMessage{
	/**
	 * 
	 */
	private static final long serialVersionUID = 7717334330741162319L;
	private final boolean identifiersIsEncrypted;
	private final Identifier[] identifiers;
	private final byte[][] jpakeMessages;
	private final short step;
	private final transient short nbAnomalies;
	
	
	
	
	public JPakeMessage(Map<Identifier, P2PJPAKESecretMessageExchanger> jpakes, boolean identifiersIsEncrypted, short nbAnomalies, AbstractSecureRandom random, AbstractMessageDigest messageDigest) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchProviderException, DigestException, IOException {
		super();
		this.identifiersIsEncrypted=identifiersIsEncrypted;
		this.identifiers=new Identifier[jpakes.size()];
		this.jpakeMessages=new byte[jpakes.size()][];
		this.step = 1;
		int i=0;
		for (Map.Entry<Identifier, P2PJPAKESecretMessageExchanger> e : jpakes.entrySet())
		{
			if (identifiersIsEncrypted)
				this.identifiers[i] = new EncryptedIdentifier(e.getKey(), random, messageDigest);
			else
				this.identifiers[i] = e.getKey();
			jpakeMessages[i]=e.getValue().getStep1Message();
			++i;
		}
		this.nbAnomalies=nbAnomalies;
	}
	private JPakeMessage(Map<Identifier, P2PJPAKESecretMessageExchanger> jpakes, boolean identifiersIsEncrypted, short nbAnomalies, AbstractSecureRandom random, AbstractMessageDigest messageDigest, short step, Map<Identifier, byte[]> jakeMessages) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchProviderException, DigestException, IOException {
		super();
		this.identifiersIsEncrypted=identifiersIsEncrypted;
		this.identifiers=new Identifier[jpakes.size()];
		this.jpakeMessages=new byte[jpakes.size()][];
		this.step = step;
		int i=0;
		for (Map.Entry<Identifier, P2PJPAKESecretMessageExchanger> e : jpakes.entrySet())
		{
			if (identifiersIsEncrypted)
				this.identifiers[i] = new EncryptedIdentifier(e.getKey(), random, messageDigest);
			else
				this.identifiers[i] = e.getKey();
			jpakeMessages[i]=jakeMessages.get(e.getKey());
			++i;
		}
		this.nbAnomalies=nbAnomalies;
	}
	@Override
	public short getNbAnomalies() {
		return nbAnomalies;
	}
	
	public byte[][] getJpakeMessages() {
		return jpakeMessages;
	}
	public short getStep() {
		return step;
	}
	public Identifier[] getIdentifiers()
	{
		return identifiers;
	}
	public boolean areIdentifiersEncrypted()
	{
		return identifiersIsEncrypted;
	}
	
	@Override
	public Integrity checkDataIntegrity() {
		if (identifiers==null)
			return Integrity.FAIL_AND_CANDIDATE_TO_BAN;
		if (jpakeMessages==null || jpakeMessages.length!=identifiers.length)
			return Integrity.FAIL_AND_CANDIDATE_TO_BAN;
		for (byte[] b : jpakeMessages)
			if (b==null || b.length==0)
				return Integrity.FAIL_AND_CANDIDATE_TO_BAN;
		if (step<1 || step>3)
			return Integrity.FAIL_AND_CANDIDATE_TO_BAN;
		return Integrity.OK;
	}
	@Override
	public boolean checkDifferedMessages() {
		return false;
	}
	
	public AccessMessage getJPakeMessageNewStep(short newStep, LoginData lp, AbstractSecureRandom random, AbstractMessageDigest messageDigest, Collection<PairOfIdentifiers> deniedIdentifiers,
			Map<Identifier, P2PJPAKESecretMessageExchanger> jpakes)
			throws AccessException, InvalidKeyException, IllegalAccessException, IOException, IllegalBlockSizeException,
			BadPaddingException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchProviderException, DigestException {

		int nbAno=0;
		if (step!=newStep-1)
		{
			nbAno+=jpakes.size();
			jpakes.clear();
			return new AccessErrorMessage(true);
		}

		Map<Identifier, byte[]> jpkms=new HashMap<>();
		
		for (int i = 0; i < identifiers.length; i++) {
			Identifier id = identifiers[i];
			Identifier decodedID = null;
			if (identifiersIsEncrypted)
				decodedID = lp.getIdentifier((EncryptedIdentifier) id, messageDigest);
			else
				decodedID = id;
			if (decodedID != null) {
				Identifier localID = lp.localiseIdentifier(decodedID);
				if (localID != null) {
					P2PJPAKESecretMessageExchanger jpake=jpakes.get(localID);
					if (jpake!=null)
					{
						try
						{
							byte[] jpakeMessage=null;
							if (newStep==2)
								jpakeMessage=jpake.receiveStep1AndGetStep2Message(this.jpakeMessages[i]);
							else if (newStep==3)
								jpakeMessage=jpake.receiveStep2AndGetStep3Message(this.jpakeMessages[i]);
							jpkms.put(localID, jpakeMessage);
						}
						catch(IOException | CryptoException | ClassNotFoundException e)
						{
							deniedIdentifiers.add(new PairOfIdentifiers(localID, decodedID));
							jpakes.remove(localID);
							++nbAno;
						}
					}
					else
					{
						deniedIdentifiers.add(new PairOfIdentifiers(localID, decodedID));
						++nbAno;
					}
				}
			}
		}
		
		return new JPakeMessage(jpakes, identifiersIsEncrypted, nbAno > Short.MAX_VALUE ? Short.MAX_VALUE : (short)nbAno, random, messageDigest, newStep, jpkms);
	}
	
	public AccessMessage receiveLastMessage(LoginData lp, AbstractMessageDigest messageDigest, Collection<PairOfIdentifiers> acceptedIdentifiers, Collection<PairOfIdentifiers> deniedIdentifiers,
			Map<Identifier, P2PJPAKESecretMessageExchanger> jpakes, KernelAddress kernelAddress)
			throws AccessException, InvalidKeyException, IllegalAccessException, IOException, IllegalBlockSizeException,
			BadPaddingException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchProviderException, DigestException {

		
		int nbAno=0;
		if (step!=3)
		{
			nbAno+=jpakes.size();
			jpakes.clear();
			return new AccessErrorMessage(true);
		}

		for (int i = 0; i < identifiers.length; i++) {
			Identifier id = identifiers[i];
			Identifier decodedID = null;
			if (identifiersIsEncrypted)
				decodedID = lp.getIdentifier((EncryptedIdentifier) id, messageDigest);
			else
				decodedID = id;
			if (decodedID != null) {
				Identifier localID = lp.localiseIdentifier(decodedID);
				if (localID != null) {
					P2PJPAKESecretMessageExchanger jpake=jpakes.get(localID);
					if (jpake!=null)
					{
						try
						{
							jpake.receiveStep3(this.jpakeMessages[i]);
							if (jpake.isPassworkOrKeyValid())
							{
								acceptedIdentifiers.add(new PairOfIdentifiers(localID, decodedID));
							}
							else
								deniedIdentifiers.add(new PairOfIdentifiers(localID, decodedID));	
						}
						catch(IOException | CryptoException | ClassNotFoundException e)
						{
							deniedIdentifiers.add(new PairOfIdentifiers(localID, decodedID));
							jpakes.remove(localID);
							++nbAno;
						}
					}
					else
					{
						deniedIdentifiers.add(new PairOfIdentifiers(localID, decodedID));
						++nbAno;
					}
				}
			}
		}
		return new LoginConfirmationMessage(acceptedIdentifiers, deniedIdentifiers, kernelAddress, nbAno > Short.MAX_VALUE ? Short.MAX_VALUE : (short)nbAno, false);
	}
	
	
}
