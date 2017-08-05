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

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import com.distrimind.madkit.kernel.AbstractGroup;
import com.distrimind.util.crypto.P2PASymmetricSecretMessageExchanger;

/**
 * Represents data enabling to identify each peer, and its right access.
 * 
 * @author Jason Mahdjoub
 * @since MadkitLanEdition 1.0
 * @version 1.0
 * @see AccessData
 */
public abstract class LoginData extends AccessData {

	private final AtomicReference<LoginEventsTrigger[]> login_triggers = new AtomicReference<>(null);

	/**
	 * Returns for one identifier the group(s) for which it is enabled to access
	 * 
	 * @param _id
	 *            the identifier of the distant user
	 * @return the authorized group(s)
	 * @throws AccessException
	 *             if a problem occurs
	 */
	public abstract AbstractGroup getGroupsAccess(Identifier _id) throws AccessException;

	/**
	 * Parse all identifiers and call
	 * {@link IdentifierParser#newIdentifier(Identifier)} function for each
	 * identifier parsed.
	 * 
	 * @param notifier
	 *            the notifier
	 * @throws AccessException
	 *             if a problem occurs
	 * @see IdentifierParser
	 */
	public abstract void parseIdentifiers(IdentifierParser notifier) throws AccessException;

	/**
	 * Get the identifier corresponding to the given encrypted identifier
	 * 
	 * @param encryptedIdentifier
	 *            the encrypted identifier
	 * @param cipher
	 *            the cipher which will enable to check the parsed identifiers with
	 *            the given encrypted identifier
	 * @return the corresponding clear identifier
	 * @throws AccessException
	 *             if a problem occurs
	 */
	public Identifier getIdentifier(final EncryptedIdentifier encryptedIdentifier,
			final P2PASymmetricSecretMessageExchanger cipher) throws AccessException {
		final AtomicReference<Identifier> res = new AtomicReference<>(null);

		parseIdentifiers(new IdentifierParser() {

			@Override
			public boolean newIdentifier(Identifier _identifier) throws AccessException {
				try {
					if (encryptedIdentifier.getCloudIdentifier() instanceof EncryptedCloudIdentifier
							&& ((EncryptedCloudIdentifier) encryptedIdentifier.getCloudIdentifier())
									.verifyWithLocalCloudIdentifier(_identifier.getCloudIdentifier(), cipher)) {
						res.set(new Identifier(_identifier.getCloudIdentifier(),
								encryptedIdentifier.getHostIdentifier()));
						return false;
					} else
						return true;
				} catch (Exception e) {
					throw new AccessException(e);
				}
			}
		});

		return res.get();
	}

	/**
	 * Parse the identifiers corresponding to the cloud identifier itself
	 * corresponding to the given encrypted identifier
	 * 
	 * @param parser
	 *            the parser
	 * @param encryptedIdentifier
	 *            the encrypted identifier
	 * @param cipher
	 *            the cipher which will enable to check the parsed identifiers with
	 *            the given encrypted identifier
	 * @return the corresponding clear identifier
	 * @throws AccessException
	 *             if a problem occurs
	 * @see IdentifierParser
	 */
	public void parseHostIdentifiers(final IdentifierParser parser, final EncryptedIdentifier encryptedIdentifier,
			final P2PASymmetricSecretMessageExchanger cipher) throws AccessException {
		parseIdentifiers(new IdentifierParser() {

			@Override
			public boolean newIdentifier(Identifier _identifier) throws AccessException {
				try {
					if (encryptedIdentifier.getEncryptedCloudIdentifier()
							.verifyWithLocalCloudIdentifier(_identifier.getCloudIdentifier(), cipher)) {
						return parser.newIdentifier(_identifier);
					} else
						return true;
				} catch (Exception e) {
					throw new AccessException(e);
				}
			}
		});
	}

	/**
	 * Gets a list of possible identifiers candidate to be used for connection
	 * initiative with the distant peer. All identifiers must not be returned. This
	 * function differs from the function
	 * {@link #parseIdentifiers(IdentifierParser)} which parse all identifiers.
	 * 
	 * @return the list of possible identifiers to initiate
	 * @see #canTakesLoginInitiative()
	 */
	public abstract List<Identifier> getIdentifiersToInitiate() throws AccessException;

	/**
	 * Ask if the current peer can ask for login, or it must wait to be asked for
	 * login.
	 * 
	 * @return true if the current peer can ask for login itself
	 * @see #getIdentifiersToInitiate()
	 */
	public abstract boolean canTakesLoginInitiative();

	/**
	 * This class is used to parse identifiers
	 * 
	 * @author Jason Mahdjoub
	 * @see LoginData#parseIdentifiers(IdentifierParser)
	 */
	public static abstract class IdentifierParser {
		/**
		 * 
		 * @param identifier
		 *            the new identifier parsed
		 * @return true if the identifier parsing can continue
		 */
		public abstract boolean newIdentifier(Identifier identifier) throws AccessException;
	}

	/**
	 * According an identifier, returns a password
	 * 
	 * @param identifier
	 *            the identifier
	 * @return the password corresponding to the given identifier
	 */
	public abstract PasswordKey getPassword(Identifier identifier);

	/**
	 * Inform that a bad password has been given to the identifier given as
	 * parameter
	 * 
	 * @param identifier
	 *            the identifier
	 */
	public abstract void invalidPassword(Identifier identifier);

	/**
	 * Transform the given identifier to a local identifier
	 * 
	 * @param _identifier
	 * @return an identifier transformed to be understood locally
	 */
	public abstract Identifier localiseIdentifier(Identifier _identifier);

	private boolean containsTrigger(LoginEventsTrigger logts[], LoginEventsTrigger _trigger) {
		if (logts == null)
			return false;
		for (LoginEventsTrigger lt : logts) {
			if (lt == _trigger)
				return true;
		}
		return false;
	}

	void addTrigger(LoginEventsTrigger _trigger) {
		if (_trigger == null)
			return;
		synchronized (this) {
			LoginEventsTrigger logts[] = login_triggers.get();
			if (containsTrigger(logts, _trigger))
				return;
			LoginEventsTrigger new_logts[] = null;
			if (logts == null)
				new_logts = new LoginEventsTrigger[1];
			else {
				new_logts = new LoginEventsTrigger[logts.length + 1];
				System.arraycopy(logts, 0, new_logts, 0, logts.length);
			}
			new_logts[new_logts.length - 1] = _trigger;
			login_triggers.set(new_logts);
		}
	}

	void removeTrigger(LoginEventsTrigger _trigger) {
		if (_trigger == null)
			return;
		synchronized (this) {
			LoginEventsTrigger logts[] = login_triggers.get();
			if (!containsTrigger(logts, _trigger))
				return;
			LoginEventsTrigger new_logts[] = null;
			if (logts == null)
				return;
			else {
				new_logts = new LoginEventsTrigger[logts.length - 1];
				int i = 0;
				for (LoginEventsTrigger lt : logts) {
					if (lt != _trigger)
						new_logts[i++] = lt;
				}
			}
			login_triggers.set(new_logts);
		}
	}

	/**
	 * This function must be called when an identifier has been added
	 * 
	 * @param _identifier
	 *            the added identifier
	 */
	public final void newIdentifierAddedEvent(Identifier _identifier) {
		LoginEventsTrigger logts[] = login_triggers.get();
		if (logts != null) {
			for (LoginEventsTrigger lt : logts)
				lt.addingIdentifier(_identifier);
		}
	}

	/**
	 * This function must be called when identifiers have been added
	 * 
	 * @param _identifiers
	 *            the identifiers
	 */
	public final void newIdentifiersAddedEvent(Collection<Identifier> _identifiers) {
		LoginEventsTrigger logts[] = login_triggers.get();
		if (logts != null) {
			for (LoginEventsTrigger lt : logts)
				lt.addingIdentifiers(_identifiers);
		}
	}

	/**
	 * This function must be called when an identifier has been removed
	 * 
	 * @param _identifier
	 *            the identifier
	 */
	public final void newIdentifierRemovedEvent(Identifier _identifier) {
		LoginEventsTrigger logts[] = login_triggers.get();
		if (logts != null) {
			for (LoginEventsTrigger lt : logts)
				lt.removingIdentifier(_identifier);
		}
	}

	/**
	 * This function must be called when identifiers have been removed
	 * 
	 * @param _identifiers
	 */
	public final void newIdentifiersRemovedEvent(Collection<Identifier> _identifiers) {
		LoginEventsTrigger logts[] = login_triggers.get();
		if (logts != null) {
			for (LoginEventsTrigger lt : logts)
				lt.removingIdentifiers(_identifiers);
		}
	}
}
