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

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.distrimind.madkit.kernel.MadkitProperties;
import com.distrimind.util.crypto.AbstractMessageDigest;
import com.distrimind.util.crypto.AbstractSecureRandom;
import com.distrimind.util.crypto.P2PJPAKESecretMessageExchanger;

import gnu.vm.jgnu.security.DigestException;
import gnu.vm.jgnu.security.NoSuchAlgorithmException;
import gnu.vm.jgnu.security.NoSuchProviderException;

/**
 * Represents properties of a specific connection protocol
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MadkitLanEdition 1.2
 *
 */
public class AccessProtocolWithJPake extends AbstractAccessProtocol {

	private final AccessProtocolWithJPakeProperties access_protocol_properties;
	private AccessState access_state = AccessState.ACCESS_NOT_INITIALIZED;
	private Map<Identifier, P2PJPAKESecretMessageExchanger> jpakes;
	
	private final AbstractMessageDigest messageDigest;
	private byte[] localGeneratedSalt=null, distantGeneratedSalt=null;
	private MadkitProperties mkProperties;
	public AccessProtocolWithJPake(InetSocketAddress _distant_inet_address,
			InetSocketAddress _local_interface_address, LoginEventsTrigger loginTrigger, MadkitProperties _properties)
			throws AccessException {
		super(_distant_inet_address, _local_interface_address, loginTrigger, _properties);
		this.mkProperties=_properties;
		access_protocol_properties = (AccessProtocolWithJPakeProperties)_properties.networkProperties.getAccessProtocolProperties(_distant_inet_address,_local_interface_address);
		if (access_protocol_properties == null)
			throw new NullPointerException("No AccessProtocolProperties was found into the MadkitProperties !");
		
		try
		{
			
			messageDigest=access_protocol_properties.identifierDigestionTypeUsedForAnonymization.getMessageDigestInstance();
		}
		catch(NoSuchProviderException | NoSuchAlgorithmException e)
		{
			throw new AccessException(e);
		}
		
		
	}
	
	private static enum AccessState {
		ACCESS_NOT_INITIALIZED, ACCESS_INITIALIZED, WAITING_FOR_IDENTIFIERS, WAITING_FOR_PASSWORD_VALIDATION_1, WAITING_FOR_PASSWORD_VALIDATION_2, WAITING_FOR_PASSWORD_VALIDATION_3, WAITING_FOR_LOGIN_CONFIRMATION, ACCESS_FINALIZED, WAITING_FOR_NEW_LOGIN_STEP1, WAITING_FOR_NEW_LOGIN_STEP2, WAITING_FOR_NEW_LOGIN_STEP3, WAITING_FOR_NEW_LOGIN_CONFIRMATION,
	}

	@Override
	protected void reset()
	{
		super.reset();
		jpakes=new HashMap<>();
		setAcceptedIdentifiers(null);
		setDeniedIdentifiers(null);
	}
	@Override
	public AccessMessage subSetAndGetNextMessage(AccessMessage _m) throws AccessException {
		try {
			if (_m instanceof AccessErrorMessage) {
				access_state = AccessState.ACCESS_NOT_INITIALIZED;
				return null;
			}

			switch (access_state) {
			case ACCESS_NOT_INITIALIZED: {
				reset();
				if (_m instanceof AccessAskInitiliazation) {
					if (access_data instanceof LoginData) {
						access_state = AccessState.ACCESS_INITIALIZED;
						JPakeAccessInitialized res=new JPakeAccessInitialized(((LoginData)access_data).canTakesLoginInitiative(), mkProperties.getApprovedSecureRandomForKeys());
						localGeneratedSalt=res.getGeneratedSalt();
						return res;
						
					} else {
						access_state = AccessState.ACCESS_INITIALIZED;
						JPakeAccessInitialized res=new JPakeAccessInitialized(false, mkProperties.getApprovedSecureRandomForKeys());
						localGeneratedSalt=res.getGeneratedSalt();
						return res;
					}
				} else
					return new AccessErrorMessage(true);

			}
			
			case ACCESS_INITIALIZED: {
				if (_m instanceof JPakeAccessInitialized) {
					JPakeAccessInitialized m=((JPakeAccessInitialized) _m);
					setOtherCanTakesInitiative( m.can_takes_login_initiative);
					distantGeneratedSalt=m.getGeneratedSalt();
					if (access_data instanceof LoginData) {
						LoginData lp = (LoginData) access_data;

						
						if (lp.canTakesLoginInitiative())
							setIdentifiers(lp.getIdentifiersToInitiate());
						else
							setIdentifiers(null);
						if (getIdentifiers() != null && getIdentifiers().size() == 0)
							setIdentifiers(null);
						if (getIdentifiers() != null) {
							access_state = AccessState.WAITING_FOR_IDENTIFIERS;
							return new IdentifiersPropositionMessage(getIdentifiers(), mkProperties.getApprovedSecureRandom(), messageDigest,
									this.access_protocol_properties.encryptIdentifiersBeforeSendingToDistantPeer,
									(short) 0, distantGeneratedSalt);
						} else {
							if (!isOtherCanTakesInitiative()) {
								access_state = AccessState.ACCESS_NOT_INITIALIZED;
								return new AccessAbordedMessage();
							} else {
								access_state = AccessState.WAITING_FOR_IDENTIFIERS;
								return new NullAccessMessage();
							}
						}
					} else {
						access_state = AccessState.ACCESS_FINALIZED;
						return new AccessFinalizedMessage();
					}
				} else {
					access_state = AccessState.ACCESS_NOT_INITIALIZED;
					return new AccessErrorMessage(false);
				}
			}
			case WAITING_FOR_IDENTIFIERS: {
				if (_m instanceof IdentifiersPropositionMessage) {
					if (access_data instanceof LoginData) {
						LoginData lp = (LoginData) access_data;
						if (getIdentifiers() != null) {
							access_state = AccessState.WAITING_FOR_PASSWORD_VALIDATION_1;
							return ((IdentifiersPropositionMessage) _m).getJPakeMessage(lp, jpakes,mkProperties.getApprovedSecureRandom(), mkProperties.getApprovedSecureRandomForKeys(), messageDigest,
									this.access_protocol_properties.encryptIdentifiersBeforeSendingToDistantPeer, distantGeneratedSalt, localGeneratedSalt);
						} else {
							access_state = AccessState.WAITING_FOR_PASSWORD_VALIDATION_1;
							setIdentifiers(new ArrayList<Identifier>());
							return new AccessMessagesList(((IdentifiersPropositionMessage) _m).getIdentifiersPropositionMessageAnswer(lp, mkProperties.getApprovedSecureRandom(), messageDigest,
											this.access_protocol_properties.encryptIdentifiersBeforeSendingToDistantPeer, getIdentifiers(), distantGeneratedSalt,  localGeneratedSalt),
									((IdentifiersPropositionMessage) _m).getJPakeMessage(lp, jpakes,mkProperties.getApprovedSecureRandom(), mkProperties.getApprovedSecureRandomForKeys(), messageDigest,
											this.access_protocol_properties.encryptIdentifiersBeforeSendingToDistantPeer, distantGeneratedSalt, localGeneratedSalt));
							
						}
					} else
						return new AccessErrorMessage(true);
				} else if (_m instanceof NullAccessMessage) {
					return new DoNotSendMessage();
				} else {
					access_state = AccessState.ACCESS_NOT_INITIALIZED;
					return new AccessErrorMessage(false);
				}
			}
			
			case WAITING_FOR_PASSWORD_VALIDATION_1:{
				if (_m instanceof JPakeMessage) {
					LoginData lp = (LoginData) access_data;
					JPakeMessage jpakem = (JPakeMessage) _m;
					if (jpakem.getStep()!=1)
					{
						access_state = AccessState.ACCESS_NOT_INITIALIZED;
						return new AccessErrorMessage(false);
					}
					
					setAcceptedIdentifiers(new ArrayList<PairOfIdentifiers>());
					setDeniedIdentifiers(new ArrayList<PairOfIdentifiers>());
					AccessMessage res=jpakem.getJPakeMessageNewStep((short)2, lp, mkProperties.getApprovedSecureRandom(), messageDigest, getDeniedIdentifiers(), jpakes, distantGeneratedSalt, localGeneratedSalt);
					
					access_state = AccessState.WAITING_FOR_PASSWORD_VALIDATION_2;

					return res;
				} else {
					access_state = AccessState.ACCESS_NOT_INITIALIZED;
					return new AccessErrorMessage(false);
				}
			}
			case WAITING_FOR_PASSWORD_VALIDATION_2:{
				if (_m instanceof JPakeMessage) {
					LoginData lp = (LoginData) access_data;
					JPakeMessage jpakem = (JPakeMessage) _m;
					if (jpakem.getStep()!=2)
					{
						access_state = AccessState.ACCESS_NOT_INITIALIZED;
						return new AccessErrorMessage(false);
					}
					
					AccessMessage res=jpakem.getJPakeMessageNewStep((short)3, lp, mkProperties.getApprovedSecureRandom(), messageDigest, getDeniedIdentifiers(), jpakes, distantGeneratedSalt, localGeneratedSalt);
					
					if (res instanceof AccessErrorMessage)
					{
						access_state = AccessState.ACCESS_NOT_INITIALIZED;
					}
					else
					{
						access_state = AccessState.WAITING_FOR_PASSWORD_VALIDATION_3;
					}

					return res;
				} else {
					access_state = AccessState.ACCESS_NOT_INITIALIZED;
					return new AccessErrorMessage(false);
				}
			}
			case WAITING_FOR_PASSWORD_VALIDATION_3:{
				if (_m instanceof JPakeMessage) {
					LoginData lp = (LoginData) access_data;
					JPakeMessage jpakem = (JPakeMessage) _m;
					if (jpakem.getStep()!=3)
					{
						access_state = AccessState.ACCESS_NOT_INITIALIZED;
						return new AccessErrorMessage(false);
					}
					AccessMessage res=jpakem.receiveLastMessage(lp, messageDigest, getAcceptedIdentifiers(), getDeniedIdentifiers(), jpakes, localGeneratedSalt);
					if (res!=null && res instanceof AccessErrorMessage)
					{
						access_state = AccessState.ACCESS_NOT_INITIALIZED;
					}
					else
					{
						access_state = AccessState.WAITING_FOR_LOGIN_CONFIRMATION;
					}
					return res;
					
				} else {
					access_state = AccessState.ACCESS_NOT_INITIALIZED;
					return new AccessErrorMessage(false);
				}
			}
			case WAITING_FOR_LOGIN_CONFIRMATION: {
				if (_m instanceof LoginConfirmationMessage && access_data instanceof LoginData) {
					ArrayList<PairOfIdentifiers> ai = new ArrayList<>();
					ArrayList<PairOfIdentifiers> denied_identiers = new ArrayList<>();

					for (PairOfIdentifiers id : getAcceptedIdentifiers()) {
						PairOfIdentifiers found_id = null;

						for (Identifier id2 : ((LoginConfirmationMessage) _m).accepted_identifiers) {
							if (id.getLocalIdentifier().equalsCloudIdentifier(id2)) {
								found_id = id;
								break;
							}
						}
						if (found_id != null)
							ai.add(found_id);
						else
							denied_identiers.add(id);
					}
					setAcceptedIdentifiers(ai);

					//setDistantKernelAddress(((LoginConfirmationMessage) _m).kernel_address);
					addLastAcceptedAndDeniedIdentifiers(getAcceptedIdentifiers(), denied_identiers);

					access_state = AccessState.ACCESS_FINALIZED;
					setAcceptedIdentifiers(null);
					setDeniedIdentifiers(null);
					setIdentifiers(null);
					jpakes.clear();
					return new AccessFinalizedMessage();
				} else {
					access_state = AccessState.ACCESS_NOT_INITIALIZED;
					return new AccessErrorMessage(false);
				}

			}
			case ACCESS_FINALIZED: {
				if (_m instanceof IdentifiersPropositionMessage && access_data instanceof LoginData) {
					AccessMessage res=((IdentifiersPropositionMessage) _m).getJPakeMessage((LoginData)access_data, jpakes, mkProperties.getApprovedSecureRandom(), mkProperties.getApprovedSecureRandomForKeys(), messageDigest, this.access_protocol_properties.encryptIdentifiersBeforeSendingToDistantPeer, distantGeneratedSalt, localGeneratedSalt);
					access_state = AccessState.WAITING_FOR_NEW_LOGIN_STEP1;
					return res;
				} else if (_m instanceof AccessFinalizedMessage) {
					updateGroupAccess();
					return null;
				} else if (_m instanceof UnlogMessage) {
					removeAcceptedIdentifiers(((UnlogMessage) _m).identifier_to_unlog);
					updateGroupAccess();
					return null;
				} else {
					return manageDifferableAccessMessage(_m);
				}
			}
			
			
			case WAITING_FOR_NEW_LOGIN_STEP1:{
				if (_m instanceof JPakeMessage) {
					LoginData lp = (LoginData) access_data;
					JPakeMessage jpakem = (JPakeMessage) _m;
					if (jpakem.getStep()!=1)
					{
						access_state = AccessState.ACCESS_NOT_INITIALIZED;
						return new AccessErrorMessage(false);
					}
					
					setAcceptedIdentifiers(new ArrayList<PairOfIdentifiers>());
					setDeniedIdentifiers(new ArrayList<PairOfIdentifiers>());
					AccessMessage res=jpakem.getJPakeMessageNewStep((short)2, lp, mkProperties.getApprovedSecureRandom(), messageDigest, getDeniedIdentifiers(), jpakes, distantGeneratedSalt, localGeneratedSalt);
					
					access_state = AccessState.WAITING_FOR_NEW_LOGIN_STEP2;

					return res;
				} else if (!differrAccessMessage(_m)) {
					setIdentifiers(null);
					setAcceptedIdentifiers(null);
					setDeniedIdentifiers(null);
					jpakes.clear();
					access_state = AccessState.ACCESS_FINALIZED;
					return manageDifferedAccessMessage();
				} else 
					return null;
			}
			case WAITING_FOR_NEW_LOGIN_STEP2:{
				if (_m instanceof JPakeMessage) {
					LoginData lp = (LoginData) access_data;
					JPakeMessage jpakem = (JPakeMessage) _m;
					if (jpakem.getStep()!=2)
					{
						access_state = AccessState.ACCESS_NOT_INITIALIZED;
						return new AccessErrorMessage(false);
					}
					
					AccessMessage res=jpakem.getJPakeMessageNewStep((short)3, lp, mkProperties.getApprovedSecureRandom(), messageDigest, getDeniedIdentifiers(), jpakes, distantGeneratedSalt, localGeneratedSalt);
					
					if (res instanceof AccessErrorMessage)
					{
						access_state = AccessState.ACCESS_NOT_INITIALIZED;
					}
					else
					{
						access_state = AccessState.WAITING_FOR_NEW_LOGIN_STEP3;
					}

					return res;
				} else if (!differrAccessMessage(_m)) {
					setIdentifiers(null);
					setAcceptedIdentifiers(null);
					setDeniedIdentifiers(null);
					jpakes.clear();
					access_state = AccessState.ACCESS_FINALIZED;
					return manageDifferedAccessMessage();
				} else 
					return null;
			}
			case WAITING_FOR_NEW_LOGIN_STEP3:{
				if (_m instanceof JPakeMessage) {
					LoginData lp = (LoginData) access_data;
					JPakeMessage jpakem = (JPakeMessage) _m;
					if (jpakem.getStep()!=3)
					{
						access_state = AccessState.ACCESS_NOT_INITIALIZED;
						return new AccessErrorMessage(false);
					}
					AccessMessage res=jpakem.receiveLastMessage(lp, messageDigest, getAcceptedIdentifiers(), getDeniedIdentifiers(), jpakes, localGeneratedSalt);
					if (res!=null && res instanceof AccessErrorMessage)
					{
						access_state = AccessState.ACCESS_NOT_INITIALIZED;
					}
					else
					{
						access_state = AccessState.WAITING_FOR_NEW_LOGIN_CONFIRMATION;
					}
					return res;
					
				} else if (!differrAccessMessage(_m)) {
					setIdentifiers(null);
					setAcceptedIdentifiers(null);
					setDeniedIdentifiers(null);
					jpakes.clear();
					access_state = AccessState.ACCESS_FINALIZED;
					return manageDifferedAccessMessage();
				} else 
					return null;
			}
			case WAITING_FOR_NEW_LOGIN_CONFIRMATION: {
				if (_m instanceof LoginConfirmationMessage && access_data instanceof LoginData) {
					ArrayList<PairOfIdentifiers> ai = new ArrayList<>();
					ArrayList<PairOfIdentifiers> denied_identiers = new ArrayList<>();

					for (PairOfIdentifiers id : getAcceptedIdentifiers()) {
						PairOfIdentifiers found_id = null;

						for (Identifier id2 : ((LoginConfirmationMessage) _m).accepted_identifiers) {
							if (id.getLocalIdentifier().equalsCloudIdentifier(id2)) {
								found_id = id;
								break;
							}
						}
						if (found_id != null)
							ai.add(found_id);
						else
							denied_identiers.add(id);
					}
					setAcceptedIdentifiers(ai);

					addLastAcceptedAndDeniedIdentifiers(getAcceptedIdentifiers(), denied_identiers);

					access_state = AccessState.ACCESS_FINALIZED;
					setAcceptedIdentifiers(null);
					setDeniedIdentifiers(null);
					setIdentifiers(null);
					jpakes.clear();
					return manageDifferedAccessMessage();
				} else if (!differrAccessMessage(_m)) {
					setIdentifiers(null);
					setAcceptedIdentifiers(null);
					setDeniedIdentifiers(null);
					jpakes.clear();
					access_state = AccessState.ACCESS_FINALIZED;
					return manageDifferedAccessMessage();
				} else 
					return null;

			}
			default:
				throw new IllegalAccessError();

			}
		} catch (Exception e) {
			throw new AccessException(e);
		}
	}
	@Override
	public final boolean isAccessFinalized() {

		return isAccessFinalizedMessage() && access_state.compareTo(AccessState.ACCESS_FINALIZED) >= 0;
	}
	@Override
	protected  AccessMessage manageDifferableAccessMessage(AccessMessage _m) throws AccessException {
		try
		{
			if (_m instanceof NewLocalLoginAddedMessage) {
				if (access_data instanceof LoginData && ((LoginData) access_data).canTakesLoginInitiative()) {
					access_state = AccessState.WAITING_FOR_NEW_LOGIN_STEP1;
					List<Identifier> identifiers = new ArrayList<>();
					identifiers.addAll(((NewLocalLoginAddedMessage) _m).identifiers);
					setIdentifiers(identifiers);
					IdentifiersPropositionMessage m1= new IdentifiersPropositionMessage(identifiers, mkProperties.getApprovedSecureRandom(), messageDigest,
							this.access_protocol_properties.encryptIdentifiersBeforeSendingToDistantPeer, (short) 0, distantGeneratedSalt);
					
					JPakeMessage m2=new JPakeMessage((LoginData)access_data, mkProperties.getApprovedSecureRandom(), mkProperties.getApprovedSecureRandomForKeys(), messageDigest, jpakes, access_protocol_properties.encryptIdentifiersBeforeSendingToDistantPeer, identifiers, distantGeneratedSalt);
					return new AccessMessagesList(m1, m2);
				} else
					return null;
			} else if (_m instanceof NewLocalLoginRemovedMessage) {
				NewLocalLoginRemovedMessage nlrm = (NewLocalLoginRemovedMessage) _m;
				UnlogMessage um = removeAcceptedIdentifiers(nlrm.removed_identifiers);
				if (um.identifier_to_unlog == null || um.identifier_to_unlog.isEmpty())
					return null;
				else {
					updateGroupAccess();
					return um;
				}
			} else {
				access_state = AccessState.ACCESS_NOT_INITIALIZED;
				return new AccessErrorMessage(false);
			}
		}
		catch(Exception e)
		{
			throw new AccessException(e);
		}
	}

	
	static byte[] anonimizeIdentifier(byte[] identifier, AbstractSecureRandom random, AbstractMessageDigest messageDigest, byte[] distantGeneratedSalt) throws DigestException
	{
		if (random==null)
			throw new NullPointerException();
		if (messageDigest==null)
			throw new NullPointerException();
		
		int mds=messageDigest.getDigestLength();
		byte[] ivParameter=new byte[mds];
		random.nextBytes(ivParameter);
		return anonimizeIdentifier(identifier, ivParameter, messageDigest, distantGeneratedSalt);
	}
	
	private static byte[] anonimizeIdentifier(byte[] identifier, byte[] ivParameter, AbstractMessageDigest messageDigest, byte[] generatedSalt) throws DigestException
	{
		if (identifier==null)
			throw new NullPointerException();
		if (identifier.length==0)
			throw new IllegalArgumentException();
		if (messageDigest==null)
			throw new NullPointerException();
		if (generatedSalt==null)
			throw new NullPointerException();
		if (generatedSalt.length==0)
			throw new IllegalArgumentException();
		
		byte[] res=new byte[identifier.length+generatedSalt.length];
		System.arraycopy(generatedSalt, 0, res, 0, generatedSalt.length);
		System.arraycopy(identifier, 0, res, generatedSalt.length, identifier.length);
		identifier=res;
		
		final int mds=messageDigest.getDigestLength();
		if (ivParameter.length<mds)
			throw new IllegalArgumentException("Invalid IvParameter size");
		int index=0;
		res=new byte[(identifier.length/mds+(identifier.length%mds>0?1:0))*mds+mds];
		System.arraycopy(ivParameter, 0, res, 0, mds);
		do
		{
			messageDigest.reset();
			//ivParameter xor identifier
			int s=Math.min(mds+index, identifier.length);
			for (int i=index;i<s;i++)
				messageDigest.update((byte)(identifier[i]^res[i]));
			
			int s2=mds+index;
			for (int i=s;i<s2;i++)
				messageDigest.update(res[i]);
			
			messageDigest.digest(res, index+mds, mds);
			
			index+=mds;
		}while(index<identifier.length);
		return res;
	}

	static boolean compareAnonymizedIdentifier(byte[] identifier, byte[] anonymizedIdentifier, AbstractMessageDigest messageDigest, byte[] localGeneratedSalt) throws DigestException
	{
		if (anonymizedIdentifier==null || anonymizedIdentifier.length<messageDigest.getDigestLength()*2)
			return false;
		byte[] expectedAnonymizedIdentifier=anonimizeIdentifier(identifier, anonymizedIdentifier, messageDigest, localGeneratedSalt);
		return Arrays.equals(expectedAnonymizedIdentifier, anonymizedIdentifier);
	}
	
	
}
