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
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import gnu.vm.jgnu.security.InvalidAlgorithmParameterException;
import gnu.vm.jgnu.security.InvalidKeyException;
import gnu.vm.jgnu.security.NoSuchAlgorithmException;
import gnu.vm.jgnu.security.NoSuchProviderException;
import gnu.vm.jgnu.security.spec.InvalidKeySpecException;
import gnu.vm.jgnux.crypto.BadPaddingException;
import gnu.vm.jgnux.crypto.IllegalBlockSizeException;
import gnu.vm.jgnux.crypto.NoSuchPaddingException;

import com.distrimind.madkit.database.KeysPairs;
import com.distrimind.madkit.kernel.AbstractGroup;
import com.distrimind.madkit.kernel.KernelAddress;
import com.distrimind.madkit.kernel.MadkitProperties;
import com.distrimind.madkit.kernel.MultiGroup;
import com.distrimind.madkit.kernel.network.NetworkProperties;
import com.distrimind.ood.database.exceptions.DatabaseException;
import com.distrimind.util.crypto.ASymmetricKeyPair;
import com.distrimind.util.crypto.AbstractSecureRandom;
import com.distrimind.util.crypto.P2PASymmetricSecretMessageExchanger;
import com.distrimind.util.crypto.SecureRandomType;

/**
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MadkitLanEdition 1.0
 */
public class AccessProtocol {
	private final AccessData access_data;
	private final AccessProtocolProperties access_protocol_properties;
	private final MadkitProperties properties;

	private AccessState access_state = AccessState.ACCESS_NOT_INITIALIZED;
	private ASymmetricKeyPair myKeyPair = null;
	/*
	 * private PublicKey access_public_key=null; private PublicKey
	 * access_distant_public_key=null;
	 */
	// private PublicKey access_private_key=null;
	/*
	 * private final Cipher access_cipher_rsa_crypt; private final Cipher
	 * access_distant_cipher_rsa_crypt;
	 */
	private P2PASymmetricSecretMessageExchanger cipher;
	// private final int maximum_rsa_message_size;

	private AtomicReference<MultiGroup> groups_access = new AtomicReference<MultiGroup>();
	private boolean other_can_takes_initiative;

	private List<Identifier> identifiers = null;
	private ArrayList<PairOfIdentifiers> accepted_identifiers = null;
	private ArrayList<PairOfIdentifiers> denied_identifiers = null;
	private final InetSocketAddress distant_inet_address;
	// private final InetSocketAddress local_interface_address;
	private KernelAddress kernel_address = null;
	private final AbstractSecureRandom random;
	private LinkedList<AccessMessage> differedAccessMessages = new LinkedList<>();
	private boolean accessFinalizedMessageReceived = false;

	public AccessProtocol(InetSocketAddress _distant_inet_address, InetSocketAddress _local_interface_address,
			/* AccessGroupsNotifier accessGroupsNotifier, */ LoginEventsTrigger loginTrigger,
			MadkitProperties _properties) throws AccessException, NoSuchAlgorithmException, NoSuchProviderException {
		if (_distant_inet_address == null)
			throw new NullPointerException("_distant_inet_address");
		if (_local_interface_address == null)
			throw new NullPointerException("_local_interface_address");
		if (_properties == null)
			throw new NullPointerException("_properties");
		properties = _properties;
		access_data = properties.networkProperties.getAccessData(_distant_inet_address, _local_interface_address);
		if (access_data == null)
			throw new NullPointerException("No Access data was found into the MadkitProperties !");

		access_protocol_properties = properties.networkProperties.getAccessProtocolProperties(_distant_inet_address,
				_local_interface_address);
		if (access_protocol_properties == null)
			throw new NullPointerException("No AccessProtocolProperties was found into the MadkitProperties !");

		// maximum_rsa_message_size=access_protocol_properties.RSALoginKeySize/8-11;
		distant_inet_address = _distant_inet_address;
		cipher = null;
		random = SecureRandomType.DEFAULT.getInstance();
		access_protocol_properties.checkProperties();
		// this.accessGroupsNotifier=accessGroupsNotifier;
		if (access_data instanceof LoginData) {
			((LoginData) access_data).addTrigger(loginTrigger);
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null)
			return false;
		if (o instanceof AccessProtocol)
			return ((AccessProtocol) o).access_data.equals(access_data);
		return false;
	}

	public void setKernelAddress(KernelAddress ka) {
		kernel_address = ka;
	}

	private static enum AccessState {
		ACCESS_NOT_INITIALIZED, WAITING_FOR_PUBLIC_KEY, IDENTICAL_PUBLIC_KEY, INVALID_PUBLIC_KEY, ACCESS_INITIALIZED, WAITING_FOR_IDENTIFIERS, WAITING_FOR_PASSWORD, WAITING_FOR_LOGIN_CONFIRMATION, ACCESS_FINALIZED, WAITING_FOR_NEW_LOGIN_PW_FOR_ASKER, WAITING_FOR_NEW_LOGIN_PW_FOR_RECEIVER, WAITING_FOR_NEW_LOGIN_CONFIRMATION_ASKER, WAITING_FOR_NEW_LOGIN_CONFIRMATION_RECEIVER,
	}

	public AccessMessage setAndGetNextMessage(AccessMessage _m) throws AccessException {
		if (_m instanceof AccessFinalizedMessage)
			accessFinalizedMessageReceived = true;
		return subSetAndGetNextMessage(_m);
		/*
		 * if (_m instanceof LocalLogingAccessMessage) { if
		 * (this.access_state.equals(AccessState.ACCESS_FINALIZED)) return
		 * subSetAndGetNextMessage(_m); else {
		 * addNewLocalLoginEvent((LocalLogingAccessMessage)_m); return null; } } else {
		 * AccessMessage am=subSetAndGetNextMessage(_m); if (am==null &&
		 * this.access_state.equals(AccessState.ACCESS_FINALIZED)) {
		 * LocalLogingAccessMessage l=popLocalLogingEvent(); if (l!=null) return
		 * subSetAndGetNextMessage(l); else return null; } else return am; }
		 */

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
				groups_access.set(null);
				identifiers = null;
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
						other_can_takes_initiative = m.isOtherCanTakeLoginInitiative();
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
					other_can_takes_initiative = ((AccessInitialized) _m).can_takes_login_initiative;

					if (access_data instanceof LoginData) {
						LoginData lp = (LoginData) access_data;

						if (lp.canTakesLoginInitiative())
							identifiers = lp.getIdentifiersToInitiate();
						else
							identifiers = null;
						if (identifiers != null && identifiers.size() == 0)
							identifiers = null;
						if (identifiers != null) {
							access_state = AccessState.WAITING_FOR_IDENTIFIERS;
							return new IdentifiersPropositionMessage(identifiers, cipher,
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
							if (!other_can_takes_initiative) {
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
						if (identifiers != null) {
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
					accepted_identifiers = new ArrayList<>();
					denied_identifiers = new ArrayList<>();
					ArrayList<IdentifierPassword> idpws = new ArrayList<>();
					short nbAnomalies = idpw.getAcceptedIdentifiers(lp, cipher, accepted_identifiers,
							denied_identifiers, idpws);
					access_state = AccessState.WAITING_FOR_LOGIN_CONFIRMATION;

					if (lp.canTakesLoginInitiative()) {
						return new LoginConfirmationMessage(accepted_identifiers, denied_identifiers, kernel_address,
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

					for (PairOfIdentifiers id : accepted_identifiers) {
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
					accepted_identifiers = ai;
					// LoginData lp=(LoginData)access_data;

					distant_kernel_address = ((LoginConfirmationMessage) _m).kernel_address;
					addLastAcceptedAndDeniedIdentifiers(accepted_identifiers, denied_identiers);

					access_state = AccessState.ACCESS_FINALIZED;
					LoginData lp = (LoginData) access_data;
					AccessMessage am = new LoginConfirmationMessage(accepted_identifiers, denied_identifiers,
							kernel_address, (short) 0, false);
					accepted_identifiers = null;
					denied_identifiers = null;
					identifiers = null;
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
					/*
					 * if (access_data instanceof LoginData) { return null; } else {
					 * updateGroupAccess(); return null; }
					 */
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
					accepted_identifiers = new ArrayList<>();
					denied_identifiers = new ArrayList<>();
					ArrayList<IdentifierPassword> idpws = new ArrayList<>();
					short nbAno = ipm.getAcceptedIdentifiers(lp, cipher, accepted_identifiers, denied_identifiers,
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
					identifiers = null;
					access_state = AccessState.ACCESS_FINALIZED;
					return manageDifferedAccessMessage();
				} else
					return null;

			}
			case WAITING_FOR_NEW_LOGIN_PW_FOR_RECEIVER: {
				if (_m instanceof IdPwMessage) {
					LoginData lp = (LoginData) access_data;
					IdPwMessage ipm = (IdPwMessage) _m;
					accepted_identifiers = new ArrayList<>();
					denied_identifiers = new ArrayList<>();
					short nbAno = ipm.getAcceptedIdentifiers(lp, cipher, accepted_identifiers, denied_identifiers,
							new ArrayList<IdentifierPassword>());
					access_state = AccessState.WAITING_FOR_NEW_LOGIN_CONFIRMATION_RECEIVER;
					return new LoginConfirmationMessage(accepted_identifiers, denied_identifiers, kernel_address, nbAno,
							false);
				} else if (!differrAccessMessage(_m)) {
					identifiers = null;
					access_state = AccessState.ACCESS_FINALIZED;
					return manageDifferedAccessMessage();
				} else
					return null;

			}
			case WAITING_FOR_NEW_LOGIN_CONFIRMATION_ASKER: {
				if (_m instanceof LoginConfirmationMessage) {
					LoginConfirmationMessage lcm = (LoginConfirmationMessage) _m;
					ArrayList<PairOfIdentifiers> ai = new ArrayList<>();
					for (PairOfIdentifiers id : accepted_identifiers) {
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
							denied_identifiers.add(id);
					}
					accepted_identifiers = ai;
					addLastAcceptedAndDeniedIdentifiers(ai, denied_identifiers);
					AccessMessage am = new LoginConfirmationMessage(accepted_identifiers, denied_identifiers,
							kernel_address, (short) 0, true);

					identifiers = null;
					accepted_identifiers = null;
					denied_identifiers = null;
					updateGroupAccess();
					// accessGroupsNotifier.notifyNewAccessChangements();
					access_state = AccessState.ACCESS_FINALIZED;
					return am;
				} else if (!differrAccessMessage(_m)) {
					identifiers = null;
					accepted_identifiers = null;
					denied_identifiers = null;
					access_state = AccessState.ACCESS_FINALIZED;
					return manageDifferedAccessMessage();
				} else
					return null;

			}
			case WAITING_FOR_NEW_LOGIN_CONFIRMATION_RECEIVER: {
				if (_m instanceof LoginConfirmationMessage) {
					LoginConfirmationMessage lcm = (LoginConfirmationMessage) _m;
					ArrayList<PairOfIdentifiers> ai = new ArrayList<>();
					for (PairOfIdentifiers id : accepted_identifiers) {
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
							denied_identifiers.add(id);
					}
					accepted_identifiers = ai;
					addLastAcceptedAndDeniedIdentifiers(ai, denied_identifiers);

					identifiers = null;
					accepted_identifiers = null;
					denied_identifiers = null;
					updateGroupAccess();

					// accessGroupsNotifier.notifyNewAccessChangements();
					access_state = AccessState.ACCESS_FINALIZED;
					return manageDifferedAccessMessage();
				} else if (!differrAccessMessage(_m)) {
					identifiers = null;
					accepted_identifiers = null;
					denied_identifiers = null;
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

	private boolean differrAccessMessage(AccessMessage m) {
		if (m != null && ((m instanceof NewLocalLoginAddedMessage) || (m instanceof NewLocalLoginRemovedMessage))) {

			differedAccessMessages.offer(m);
			return true;
		}
		return false;
	}

	public AccessMessage manageDifferedAccessMessage()
			throws AccessException, NoSuchAlgorithmException, InvalidKeySpecException {
		try {
			while (differedAccessMessages.size() != 0) {
				AccessMessage res = manageDifferableAccessMessage(differedAccessMessages.poll());
				if (res != null)
					return res;
			}

			return null;
		} catch (InvalidKeyException | IOException | IllegalBlockSizeException | BadPaddingException
				| NoSuchPaddingException | InvalidAlgorithmParameterException | NoSuchProviderException e) {
			throw new AccessException(e);
		}

	}

	private AccessMessage manageDifferableAccessMessage(AccessMessage _m)
			throws InvalidKeyException, IOException, AccessException, IllegalBlockSizeException, BadPaddingException,
			NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException,
			InvalidAlgorithmParameterException, NoSuchProviderException {
		if (_m instanceof NewLocalLoginAddedMessage) {
			if (access_data instanceof LoginData && ((LoginData) access_data).canTakesLoginInitiative()) {
				access_state = AccessState.WAITING_FOR_NEW_LOGIN_PW_FOR_ASKER;
				identifiers = new ArrayList<>();
				identifiers.addAll(((NewLocalLoginAddedMessage) _m).identifiers);
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

	/*
	 * private final LinkedList<LocalLogingAccessMessage>
	 * new_login_events_waiting=new LinkedList<>();
	 * 
	 * private void addNewLocalLoginEvent(LocalLogingAccessMessage loginEvent) {
	 * new_login_events_waiting.add(loginEvent); }
	 * 
	 * private LocalLogingAccessMessage popLocalLogingEvent() { if
	 * (new_login_events_waiting.size()==0) return null;
	 * 
	 * return new_login_events_waiting.removeFirst(); }
	 */

	private KernelAddress distant_kernel_address;
	private ArrayList<PairOfIdentifiers> last_accepted_identifiers = new ArrayList<PairOfIdentifiers>();
	private ArrayList<PairOfIdentifiers> all_accepted_identifiers = new ArrayList<PairOfIdentifiers>();
	private ArrayList<PairOfIdentifiers> last_denied_identifiers_from_other = new ArrayList<>();
	private ArrayList<PairOfIdentifiers> last_unlogged_identifiers = new ArrayList<>();

	public KernelAddress getDistantKernelAddress() {
		return distant_kernel_address;
	}

	private void addLastAcceptedAndDeniedIdentifiers(ArrayList<PairOfIdentifiers> _accepted_identifiers,
			ArrayList<PairOfIdentifiers> _denied_identifiers) {
		last_accepted_identifiers.addAll(_accepted_identifiers);
		all_accepted_identifiers.addAll(_accepted_identifiers);
		last_denied_identifiers_from_other.addAll(_denied_identifiers);
	}

	private UnlogMessage removeAcceptedIdentifiers(ArrayList<Identifier> _identifiers) {

		ArrayList<PairOfIdentifiers> toRemove = new ArrayList<PairOfIdentifiers>(_identifiers.size());
		ArrayList<Identifier> res = new ArrayList<>(_identifiers.size());
		for (Identifier id : _identifiers) {
			for (PairOfIdentifiers id2 : all_accepted_identifiers) {
				if (id.equals(id2.getLocalIdentifier())) {
					toRemove.add(id2);
					res.add(id2.getDistantIdentifier());
					break;
				}
			}
		}
		all_accepted_identifiers.removeAll(toRemove);
		last_accepted_identifiers.removeAll(toRemove);
		last_unlogged_identifiers.addAll(toRemove);
		return new UnlogMessage(res);
	}

	private void updateGroupAccess() throws AccessException {

		AbstractGroup defaultGroup = access_data.getDefaultGroupsAccess();
		MultiGroup mg = null;
		if (defaultGroup == null)
			mg = new MultiGroup();
		else
			mg = new MultiGroup(defaultGroup);

		if (access_data instanceof LoginData) {
			LoginData lp = (LoginData) access_data;

			for (PairOfIdentifiers id : all_accepted_identifiers)
				mg.addGroup(lp.getGroupsAccess(id.getLocalIdentifier()));

		}
		groups_access.set(mg);

		notifyAccessGroupChangements = true;
	}

	public MultiGroup getGroupsAccess() {
		return groups_access.get();
	}

	// private final AccessGroupsNotifier accessGroupsNotifier;
	private boolean notifyAccessGroupChangements = false;

	public boolean isNotifyAccessGroupChangements() {
		boolean res = notifyAccessGroupChangements;
		notifyAccessGroupChangements = false;
		return res;
	}

	public ArrayList<PairOfIdentifiers> getLastDeniedIdentifiers() {
		ArrayList<PairOfIdentifiers> res = last_denied_identifiers_from_other;
		last_denied_identifiers_from_other = new ArrayList<>();
		return res;
	}

	public ArrayList<PairOfIdentifiers> getLastUnloggedIdentifiers() {
		ArrayList<PairOfIdentifiers> res = last_unlogged_identifiers;
		last_unlogged_identifiers = new ArrayList<>();
		return res;
	}

	public ArrayList<PairOfIdentifiers> getLastAcceptedIdentifiers() {
		ArrayList<PairOfIdentifiers> res = last_accepted_identifiers;
		last_accepted_identifiers = new ArrayList<>();
		return res;
	}

	@SuppressWarnings("unchecked")
	public ArrayList<PairOfIdentifiers> getAllAcceptedIdentifiers() {
		return (ArrayList<PairOfIdentifiers>) all_accepted_identifiers.clone();
	}

	public final boolean isAccessFinalized() {

		return accessFinalizedMessageReceived && access_state.compareTo(AccessState.ACCESS_FINALIZED) >= 0;
	}

}
