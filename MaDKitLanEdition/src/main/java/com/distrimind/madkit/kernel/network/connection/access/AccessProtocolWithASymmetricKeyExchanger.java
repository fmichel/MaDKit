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
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import com.distrimind.madkit.database.KeysPairs;
import com.distrimind.madkit.kernel.MadkitProperties;
import com.distrimind.madkit.kernel.network.NetworkProperties;
import com.distrimind.ood.database.exceptions.DatabaseException;
import com.distrimind.util.crypto.ASymmetricKeyPair;
import com.distrimind.util.crypto.AbstractSecureRandom;
import com.distrimind.util.crypto.P2PASymmetricSecretMessageExchanger;
import com.distrimind.util.crypto.SecureRandomType;

import gnu.vm.jgnu.security.InvalidAlgorithmParameterException;
import gnu.vm.jgnu.security.InvalidKeyException;
import gnu.vm.jgnu.security.NoSuchAlgorithmException;
import gnu.vm.jgnu.security.NoSuchProviderException;
import gnu.vm.jgnu.security.spec.InvalidKeySpecException;
import gnu.vm.jgnux.crypto.BadPaddingException;
import gnu.vm.jgnux.crypto.IllegalBlockSizeException;
import gnu.vm.jgnux.crypto.NoSuchPaddingException;

/**
 * Represents properties of a specific connection protocol
 * 
 * @author Jason Mahdjoub
 * @version 2.0
 * @since MadkitLanEdition 1.0
 *
 */
public class AccessProtocolWithASymmetricKeyExchanger extends AbstractAccessProtocol {

	private final AccessProtocolWithASymmetricKeyExchangerProperties access_protocol_properties;
	private AccessState access_state = AccessState.ACCESS_NOT_INITIALIZED;
	private ASymmetricKeyPair myKeyPair = null;
	private P2PASymmetricSecretMessageExchanger cipher;
	private final AbstractSecureRandom random;
	
	public AccessProtocolWithASymmetricKeyExchanger(InetSocketAddress _distant_inet_address,
			InetSocketAddress _local_interface_address, LoginEventsTrigger loginTrigger, MadkitProperties _properties)
			throws AccessException {
		super(_distant_inet_address, _local_interface_address, loginTrigger, _properties);
		
		access_protocol_properties = (AccessProtocolWithASymmetricKeyExchangerProperties)_properties.networkProperties.getAccessProtocolProperties(_distant_inet_address,_local_interface_address);
		if (access_protocol_properties == null)
			throw new NullPointerException("No AccessProtocolProperties was found into the MadkitProperties !");
		
		cipher = null;
		try
		{
			random = SecureRandomType.DEFAULT.getInstance();
		}
		catch(NoSuchProviderException | NoSuchAlgorithmException e)
		{
			throw new AccessException(e);
		}
		
	}
	
	private static enum AccessState {
		ACCESS_NOT_INITIALIZED, WAITING_FOR_PUBLIC_KEY, IDENTICAL_PUBLIC_KEY, INVALID_PUBLIC_KEY, ACCESS_INITIALIZED, WAITING_FOR_IDENTIFIERS, WAITING_FOR_PASSWORD, WAITING_FOR_LOGIN_CONFIRMATION, ACCESS_FINALIZED, WAITING_FOR_NEW_LOGIN_PW_FOR_ASKER, WAITING_FOR_NEW_LOGIN_PW_FOR_RECEIVER, WAITING_FOR_NEW_LOGIN_CONFIRMATION_ASKER, WAITING_FOR_NEW_LOGIN_CONFIRMATION_RECEIVER,
	}

	private void initKeyPair() throws NoSuchAlgorithmException, DatabaseException {
		if (myKeyPair == null) {
			if (properties.getDatabaseWrapper() == null)
				myKeyPair = access_protocol_properties.aSymetricEncryptionType
						.getKeyPairGenerator(random, access_protocol_properties.aSymetricKeySize).generateKeyPair();
			else
				myKeyPair = (((KeysPairs) properties.getDatabaseWrapper().getTableInstance(KeysPairs.class)).getKeyPair(
						distant_inet_address.getAddress(), NetworkProperties.accessProtocolDatabaseUsingCode,
						access_protocol_properties.aSymetricEncryptionType, access_protocol_properties.aSymetricKeySize,
						random, access_protocol_properties.aSymmetricKeyExpirationMs,
						properties.networkProperties.maximumNumberOfCryptoKeysForIpsSpectrum));
		}
	}

	private void initNewKeyPair() throws NoSuchAlgorithmException, DatabaseException {
		if (properties.getDatabaseWrapper() == null)
			myKeyPair = access_protocol_properties.aSymetricEncryptionType
					.getKeyPairGenerator(random, access_protocol_properties.aSymetricKeySize).generateKeyPair();
		else
			myKeyPair = (((KeysPairs) properties.getDatabaseWrapper().getTableInstance(KeysPairs.class)).getNewKeyPair(
					distant_inet_address.getAddress(), NetworkProperties.accessProtocolDatabaseUsingCode,
					access_protocol_properties.aSymetricEncryptionType, access_protocol_properties.aSymetricKeySize,
					random, access_protocol_properties.aSymmetricKeyExpirationMs,
					properties.networkProperties.maximumNumberOfCryptoKeysForIpsSpectrum));
	}
	
	@Override
	public AccessMessage subSetAndGetNextMessage(AccessMessage _m) throws AccessException {
		try {
			if (_m instanceof AccessErrorMessage) {
				if (_m instanceof AccessIdenticalPublicKeys) {

					initNewKeyPair();

					this.cipher = new P2PASymmetricSecretMessageExchanger(
							this.access_protocol_properties.messageDigestType,
							this.access_protocol_properties.passwordHashType, myKeyPair.getASymmetricPublicKey());
					this.cipher.setHashIterationsNumber(this.access_protocol_properties.passwordHashIterations);
				}
				access_state = AccessState.ACCESS_NOT_INITIALIZED;
			}
			switch (access_state) {
			case ACCESS_NOT_INITIALIZED: {
				reset();
				if (_m instanceof AccessAskInitiliazation) {
					if (access_data instanceof LoginData) {
						initKeyPair();
						this.cipher = new P2PASymmetricSecretMessageExchanger(
								this.access_protocol_properties.messageDigestType,
								this.access_protocol_properties.passwordHashType, myKeyPair.getASymmetricPublicKey());
						this.cipher.setHashIterationsNumber(this.access_protocol_properties.passwordHashIterations);
						access_state = AccessState.WAITING_FOR_PUBLIC_KEY;
						return new AccessPublicKeyMessage(this.cipher.getMyPublicKey(),
								this.cipher.getDistantPublicKey(), ((LoginData) access_data).canTakesLoginInitiative());
					} else {
						access_state = AccessState.ACCESS_INITIALIZED;
						return new AccessInitialized(false);
					}
				} else
					return new AccessErrorMessage(true);

			}
			case WAITING_FOR_PUBLIC_KEY: {
				if (_m instanceof AccessPublicKeyMessage) {
					boolean invalid_key = false;
					try {
						AccessPublicKeyMessage m = ((AccessPublicKeyMessage) _m);
						setOtherCanTakesInitiative(m.isOtherCanTakeLoginInitiative());
						this.cipher.setDistantPublicKey(m.getEncodedPublicKey());
						if (this.cipher.getDistantPublicKey().equals(this.cipher.getMyPublicKey())) {
							initNewKeyPair();
							this.cipher = new P2PASymmetricSecretMessageExchanger(
									this.access_protocol_properties.messageDigestType,
									this.access_protocol_properties.passwordHashType,
									myKeyPair.getASymmetricPublicKey());
							this.cipher.setHashIterationsNumber(this.access_protocol_properties.passwordHashIterations);

							access_state = AccessState.IDENTICAL_PUBLIC_KEY;
							return new AccessIdenticalPublicKeys(true);
						} else if (!this.cipher.getDistantPublicKey().getAlgorithmType()
								.equals(this.cipher.getMyPublicKey().getAlgorithmType()))
							invalid_key = true;
						else {
						}
					} catch (Exception e) {
						invalid_key = true;
					}

					if (invalid_key) {
						this.cipher = new P2PASymmetricSecretMessageExchanger(
								this.access_protocol_properties.messageDigestType,
								this.access_protocol_properties.passwordHashType, this.cipher.getMyPublicKey());
						this.cipher.setHashIterationsNumber(this.access_protocol_properties.passwordHashIterations);
						return new AccessInvalidKeyMessage(false);
					} else {
						access_state = AccessState.ACCESS_INITIALIZED;
						return new AccessInitialized(((LoginData) access_data).canTakesLoginInitiative());
					}
				} else if (_m instanceof AccessInvalidKeyMessage) {
					return new AccessPublicKeyMessage(this.cipher.getMyPublicKey(), this.cipher.getDistantPublicKey(),
							((LoginData) access_data).canTakesLoginInitiative());
				} else {
					access_state = AccessState.ACCESS_NOT_INITIALIZED;
					return new AccessErrorMessage(true);
				}

			}

			case IDENTICAL_PUBLIC_KEY: {
				if (_m instanceof AccessIdenticalPublicKeys) {
					access_state = AccessState.WAITING_FOR_PUBLIC_KEY;
					return new AccessPublicKeyMessage(this.cipher.getMyPublicKey(), this.cipher.getDistantPublicKey(),
							((LoginData) access_data).canTakesLoginInitiative());
				} else {
					access_state = AccessState.ACCESS_NOT_INITIALIZED;
					return new AccessErrorMessage(false);
				}
			}
			case ACCESS_INITIALIZED: {
				if (_m instanceof AccessInitialized) {
					setOtherCanTakesInitiative( ((AccessInitialized) _m).can_takes_login_initiative);

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
							return new IdentifiersPropositionMessage(getIdentifiers(), cipher,
									this.access_protocol_properties.encryptIdentifiersBeforeSendingToDistantPeer,
									(short) 0);
							/*
							 * if (!other_can_takes_initiative) {
							 * access_state=AccessState.WAITING_FOR_IDENTIFIERS;
							 * //access_state=AccessState.WAITING_FOR_LOGIN_CONFIRMATION; return new
							 * IdentifiersPropositionMessage(lp.getIdentifiersAndPasswords(),
							 * access_cipher_rsa_crypt, maximum_rsa_message_size); //return new
							 * IdPwMessage(lp.getIdentifiersAndPasswords(), access_cipher_rsa_crypt,
							 * maximum_rsa_message_size); } else {
							 * access_state=AccessState.WAITING_FOR_IDENTIFIERS; return new
							 * IdentifiersPropositionMessage(lp.getIdentifiersAndPasswords(),
							 * access_cipher_rsa_crypt, maximum_rsa_message_size); }
							 */
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
				} else if (_m instanceof AccessInvalidKeyMessage) {
					return new AccessPublicKeyMessage(this.cipher.getMyPublicKey(), this.cipher.getDistantPublicKey(),
							((LoginData) access_data).canTakesLoginInitiative());
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
							access_state = AccessState.WAITING_FOR_PASSWORD;
							return ((IdentifiersPropositionMessage) _m).getIdPwMessage(lp, cipher,
									this.access_protocol_properties.encryptIdentifiersBeforeSendingToDistantPeer);
						} else {
							access_state = AccessState.WAITING_FOR_PASSWORD;
							IdentifiersPropositionMessage res = ((IdentifiersPropositionMessage) _m)
									.getIdentifiersPropositionMessageAnswer(lp, cipher,
											this.access_protocol_properties.encryptIdentifiersBeforeSendingToDistantPeer);
							return res;
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
			case WAITING_FOR_PASSWORD: {
				if (_m instanceof IdPwMessage) {
					LoginData lp = (LoginData) access_data;
					IdPwMessage idpw = (IdPwMessage) _m;
					setAcceptedIdentifiers(new ArrayList<PairOfIdentifiers>());
					setDeniedIdentifiers(new ArrayList<PairOfIdentifiers>());
					ArrayList<IdentifierPassword> idpws = new ArrayList<>();
					short nbAnomalies = idpw.getAcceptedIdentifiers(lp, cipher, getAcceptedIdentifiers(),
							getDeniedIdentifiers(), idpws);
					access_state = AccessState.WAITING_FOR_LOGIN_CONFIRMATION;

					if (lp.canTakesLoginInitiative()) {
						return new LoginConfirmationMessage(getAcceptedIdentifiers(), getDeniedIdentifiers(), getKernelAddress(),
								nbAnomalies, false);
					} else {
						return new IdPwMessage(idpws, cipher,
								this.access_protocol_properties.encryptIdentifiersBeforeSendingToDistantPeer,
								nbAnomalies);
					}

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

					setDistantKernelAddress(((LoginConfirmationMessage) _m).kernel_address);
					addLastAcceptedAndDeniedIdentifiers(getAcceptedIdentifiers(), denied_identiers);

					access_state = AccessState.ACCESS_FINALIZED;
					LoginData lp = (LoginData) access_data;
					AccessMessage am = new LoginConfirmationMessage(getAcceptedIdentifiers(), getDeniedIdentifiers(),
							getKernelAddress(), (short) 0, false);
					setAcceptedIdentifiers(null);
					setDeniedIdentifiers(null);
					setIdentifiers(null);
					// accessGroupsNotifier.notifyNewAccessChangements();
					if (!lp.canTakesLoginInitiative())
						return am;
					else
						return new AccessFinalizedMessage();
				} else {
					access_state = AccessState.ACCESS_NOT_INITIALIZED;
					return new AccessErrorMessage(false);
				}

			}
			case ACCESS_FINALIZED: {
				if (_m instanceof IdentifiersPropositionMessage && access_data instanceof LoginData) {
					IdPwMessage res = ((IdentifiersPropositionMessage) _m).getIdPwMessage((LoginData) access_data,
							cipher, this.access_protocol_properties.encryptIdentifiersBeforeSendingToDistantPeer);
					access_state = AccessState.WAITING_FOR_NEW_LOGIN_PW_FOR_RECEIVER;
					return res;
				} else if (_m instanceof AccessFinalizedMessage) {
					updateGroupAccess();
					if (access_data instanceof LoginData) {
						if (!((LoginData) access_data).canTakesLoginInitiative())
							return new AccessFinalizedMessage();
						else {
							return null;
						}
					} else
						return null;
				} else if (_m instanceof UnlogMessage) {
					removeAcceptedIdentifiers(((UnlogMessage) _m).identifier_to_unlog);
					updateGroupAccess();
					return null;
				} else {
					return manageDifferableAccessMessage(_m);
				}
			}
			case WAITING_FOR_NEW_LOGIN_PW_FOR_ASKER: {
				if (_m instanceof IdPwMessage) {
					LoginData lp = (LoginData) access_data;
					IdPwMessage ipm = (IdPwMessage) _m;
					setAcceptedIdentifiers(new ArrayList<PairOfIdentifiers>());
					setDeniedIdentifiers(new ArrayList<PairOfIdentifiers>());
					ArrayList<IdentifierPassword> idpws = new ArrayList<>();
					short nbAno = ipm.getAcceptedIdentifiers(lp, cipher, getAcceptedIdentifiers(), getDeniedIdentifiers(),
							idpws);
					access_state = AccessState.WAITING_FOR_NEW_LOGIN_CONFIRMATION_ASKER;

					return new IdPwMessage(idpws, cipher,
							this.access_protocol_properties.encryptIdentifiersBeforeSendingToDistantPeer, nbAno);
				}
				/*
				 * else if (_m instanceof IdentifiersPropositionMessage && access_data
				 * instanceof LoginData) { return
				 * ((IdentifiersPropositionMessage)_m).getIdPwMessage((LoginData)access_data,
				 * cipher,
				 * this.access_protocol_properties.encryptIdentifiersBeforeSendingToDistantPeer)
				 * ; }
				 */
				else if (!differrAccessMessage(_m)) {
					setIdentifiers(null);
					access_state = AccessState.ACCESS_FINALIZED;
					return manageDifferedAccessMessage();
				} else
					return null;

			}
			case WAITING_FOR_NEW_LOGIN_PW_FOR_RECEIVER: {
				if (_m instanceof IdPwMessage) {
					LoginData lp = (LoginData) access_data;
					IdPwMessage ipm = (IdPwMessage) _m;
					setAcceptedIdentifiers(new ArrayList<PairOfIdentifiers>());
					setDeniedIdentifiers(new ArrayList<PairOfIdentifiers>());
					short nbAno = ipm.getAcceptedIdentifiers(lp, cipher, getAcceptedIdentifiers(), getDeniedIdentifiers(),
							new ArrayList<IdentifierPassword>());
					access_state = AccessState.WAITING_FOR_NEW_LOGIN_CONFIRMATION_RECEIVER;
					return new LoginConfirmationMessage(getAcceptedIdentifiers(), getDeniedIdentifiers(), getKernelAddress(), nbAno,
							false);
				} else if (!differrAccessMessage(_m)) {
					setIdentifiers(null);
					access_state = AccessState.ACCESS_FINALIZED;
					return manageDifferedAccessMessage();
				} else
					return null;

			}
			case WAITING_FOR_NEW_LOGIN_CONFIRMATION_ASKER: {
				if (_m instanceof LoginConfirmationMessage) {
					LoginConfirmationMessage lcm = (LoginConfirmationMessage) _m;
					ArrayList<PairOfIdentifiers> ai = new ArrayList<>();
					for (PairOfIdentifiers id : getAcceptedIdentifiers()) {
						boolean found = false;
						for (Identifier id2 : lcm.accepted_identifiers) {
							if (id.getDistantIdentifier().equalsCloudIdentifier(id2)) {
								found = true;
								break;
							}
						}
						if (found)
							ai.add(id);
						else
							getDeniedIdentifiers().add(id);
					}
					setAcceptedIdentifiers(ai);
					addLastAcceptedAndDeniedIdentifiers(ai, getDeniedIdentifiers());
					AccessMessage am = new LoginConfirmationMessage(getAcceptedIdentifiers(), getDeniedIdentifiers(),
							getKernelAddress(), (short) 0, true);

					setIdentifiers(null);
					setAcceptedIdentifiers(null);
					setDeniedIdentifiers(null);
					updateGroupAccess();
					// accessGroupsNotifier.notifyNewAccessChangements();
					access_state = AccessState.ACCESS_FINALIZED;
					return am;
				} else if (!differrAccessMessage(_m)) {
					setIdentifiers(null);
					setAcceptedIdentifiers(null);
					setDeniedIdentifiers(null);
					access_state = AccessState.ACCESS_FINALIZED;
					return manageDifferedAccessMessage();
				} else
					return null;

			}
			case WAITING_FOR_NEW_LOGIN_CONFIRMATION_RECEIVER: {
				if (_m instanceof LoginConfirmationMessage) {
					LoginConfirmationMessage lcm = (LoginConfirmationMessage) _m;
					ArrayList<PairOfIdentifiers> ai = new ArrayList<>();
					for (PairOfIdentifiers id : getAcceptedIdentifiers()) {
						boolean found = false;
						for (Identifier id2 : lcm.accepted_identifiers) {
							if (id.getDistantIdentifier().equalsCloudIdentifier(id2)) {
								found = true;
								break;
							}
						}
						if (found)
							ai.add(id);
						else
							getDeniedIdentifiers().add(id);
					}
					setAcceptedIdentifiers(ai);
					addLastAcceptedAndDeniedIdentifiers(ai, getDeniedIdentifiers());

					setIdentifiers(null);
					setAcceptedIdentifiers(null);
					setDeniedIdentifiers(null);
					updateGroupAccess();

					// accessGroupsNotifier.notifyNewAccessChangements();
					access_state = AccessState.ACCESS_FINALIZED;
					return manageDifferedAccessMessage();
				} else if (!differrAccessMessage(_m)) {
					setIdentifiers(null);
					setAcceptedIdentifiers(null);
					setDeniedIdentifiers(null);
					access_state = AccessState.ACCESS_FINALIZED;
					return manageDifferedAccessMessage();
				} else
					return null;
			}
			default:
				return null;

			}
		} catch (DatabaseException | NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException
				| InvalidAlgorithmParameterException | InvalidKeySpecException | IOException | IllegalAccessException
				| IllegalBlockSizeException | BadPaddingException | NoSuchProviderException e) {
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
					access_state = AccessState.WAITING_FOR_NEW_LOGIN_PW_FOR_ASKER;
					List<Identifier> identifiers = new ArrayList<>();
					identifiers.addAll(((NewLocalLoginAddedMessage) _m).identifiers);
					setIdentifiers(identifiers);
					return new IdentifiersPropositionMessage(identifiers, cipher,
							this.access_protocol_properties.encryptIdentifiersBeforeSendingToDistantPeer, (short) 0);
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
		catch(InvalidKeyException | IOException | AccessException | IllegalBlockSizeException | BadPaddingException |
				NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException |
				InvalidAlgorithmParameterException | NoSuchProviderException e)
		{
			throw new AccessException(e);
		}
	}

}
