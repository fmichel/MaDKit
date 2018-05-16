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
package com.distrimind.madkit.kernel.network;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.distrimind.madkit.kernel.AbstractGroup;
import com.distrimind.madkit.kernel.JunitMadkit;
import com.distrimind.madkit.kernel.MadkitEventListener;
import com.distrimind.madkit.kernel.MadkitProperties;
import com.distrimind.madkit.kernel.network.connection.access.AccessData;
import com.distrimind.madkit.kernel.network.connection.access.AccessException;
import com.distrimind.madkit.kernel.network.connection.access.Identifier;
import com.distrimind.madkit.kernel.network.connection.access.IdentifierPassword;
import com.distrimind.madkit.kernel.network.connection.access.LoginData;
import com.distrimind.madkit.kernel.network.connection.access.PasswordKey;
import com.distrimind.util.crypto.AbstractSecureRandom;
import com.distrimind.util.crypto.SecureRandomType;
import com.distrimind.util.crypto.SymmetricAuthentifiedSignatureType;
import com.distrimind.util.crypto.SymmetricSecretKey;

import gnu.vm.jgnu.security.NoSuchAlgorithmException;
import gnu.vm.jgnu.security.NoSuchProviderException;

/**
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MadkitLanEdition 1.0
 */
public class AccessDataMKEventListener implements MadkitEventListener {
	private final ArrayList<AccessData> accessData;

	public AccessDataMKEventListener(AccessData... accessData) {
		this.accessData = new ArrayList<>(accessData.length);
		for (AccessData ad : accessData)
			this.accessData.add(ad);
	}

	public AccessDataMKEventListener(ArrayList<AccessData> accessData) {
		this.accessData = accessData;
	}

	@Override
	public void onMadkitPropertiesLoaded(MadkitProperties _properties) {
		if (accessData != null) {
			for (AccessData ad : accessData)
				_properties.networkProperties.addAccessData(ad);

		}

	}

	private static final byte SALT[] = new byte[30];
	public static final int CLOUD_ID_NUMBER = 20;
	private static final CustumCloudIdentifier cloudIdentifiers[];
	static {
		new Random(System.currentTimeMillis()).nextBytes(SALT);
		cloudIdentifiers = new CustumCloudIdentifier[CLOUD_ID_NUMBER];
		for (int i = 0; i < CLOUD_ID_NUMBER; i++) {
			cloudIdentifiers[i] = new CustumCloudIdentifier("cloud" + i, SALT);
		}
	}

	private static final CustumPassword paswordIdentifiers[];
	static {
		paswordIdentifiers = new CustumPassword[cloudIdentifiers.length];
		AbstractSecureRandom random;
		try {
			random = SecureRandomType.DEFAULT.getSingleton(null);
			for (int i = 0; i < paswordIdentifiers.length; i++) {
				String pw = "pw" + i;
				SymmetricSecretKey sk=null;
				if (random.nextBoolean())
					sk=SymmetricAuthentifiedSignatureType.BC_FIPS_HMAC_SHA2_512.getKeyGenerator(random).generateKey();
				paswordIdentifiers[i] = new CustumPassword(pw, SALT, sk);
			}
		} catch (NoSuchAlgorithmException | NoSuchProviderException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
	}

	public static CustumHostIdentifier getCustumHostIdentifier(int hostNumber) {
		return new CustumHostIdentifier("host" + hostNumber);
	}

	public static ArrayList<IdentifierPassword> getServerLogins(CustumHostIdentifier host) {
		ArrayList<IdentifierPassword> res = new ArrayList<>(cloudIdentifiers.length);
		for (int i = 0; i < cloudIdentifiers.length; i++) {
			res.add(new IdentifierPassword(new Identifier(cloudIdentifiers[i], host), paswordIdentifiers[i]));
		}
		return res;
	}

	public static ArrayList<IdentifierPassword> getClientOrPeerToPeerLogins(CustumHostIdentifier host, int... indexes) {
		ArrayList<IdentifierPassword> res = new ArrayList<>(indexes.length);
		for (int i : indexes) {
			res.add(new IdentifierPassword(new Identifier(cloudIdentifiers[i], host), paswordIdentifiers[i]));
		}
		return res;
	}

	public static Identifier getIdentifier(CustumHostIdentifier host, int index) {
		return new Identifier(cloudIdentifiers[index], host);
	}

	public static IdentifierPassword getIdentifierPassword(CustumHostIdentifier host, int index) {
		return new IdentifierPassword(new Identifier(cloudIdentifiers[index], host), paswordIdentifiers[index]);
	}

	public static AccessData getDefaultAccessData(final AbstractGroup defaultGroupAccess) {
		return new AccessData() {

			@Override
			public AbstractGroup getDefaultGroupsAccess() {
				return defaultGroupAccess;
			}

			@Override
			public boolean equals(Object _o) {
				if (_o == null)
					return false;
				return this.getClass() == _o.getClass();
			}
		};
	}

	public static LoginData getDefaultLoginData(final ArrayList<IdentifierPassword> identifersAndPasswords,
			final AbstractGroup defaultGroupAccess, final AbstractGroup groupAccess,
			final boolean canTakeLoginInitiative, final Runnable invalidPassord) {
		return new LoginData() {

			@Override
			public AbstractGroup getDefaultGroupsAccess() {
				return defaultGroupAccess;
			}

			@Override
			public boolean equals(Object _o) {
				if (_o == null)
					return false;
				return this.getClass() == _o.getClass();
			}

			@Override
			public void parseIdentifiers(IdentifierParser _notifier) throws AccessException {
				for (IdentifierPassword idpw : identifersAndPasswords)
					_notifier.newIdentifier(idpw.getIdentifier());

			}

			@Override
			public Identifier localiseIdentifier(Identifier _identifier) {
				for (IdentifierPassword idpw : identifersAndPasswords) {
					if (idpw.getIdentifier().getCloudIdentifier().equals(_identifier.getCloudIdentifier()))
						return idpw.getIdentifier();
				}
				return null;
			}

			@Override
			public void invalidPassword(Identifier _identifier) {
				if (invalidPassord != null)
					invalidPassord.run();
			}

			@Override
			public PasswordKey getPassword(Identifier _identifier) {
				for (IdentifierPassword idpw : identifersAndPasswords) {
					if (idpw.getIdentifier().getCloudIdentifier().equals(_identifier.getCloudIdentifier()))
						return idpw.getPassword();
				}
				return null;
			}

			@Override
			public List<Identifier> getIdentifiersToInitiate() {
				ArrayList<Identifier> list = new ArrayList<>(identifersAndPasswords.size());
				for (IdentifierPassword idpw : identifersAndPasswords) {
					list.add(idpw.getIdentifier());
				}
				return list;
			}

			@Override
			public AbstractGroup getGroupsAccess(Identifier _id) {
				return groupAccess;
			}

			@Override
			public boolean canTakesLoginInitiative() {
				return canTakeLoginInitiative;
			}

		};
	}

	public static ArrayList<AccessDataMKEventListener> getAccessDataMKEventListenerForPeerToPeerConnections(
			final boolean canTakeLoginInitiative, final Runnable invalidPassord, CustumHostIdentifier hostIdentifier,
			int... loginIndexes) {
		ArrayList<AccessDataMKEventListener> res = new ArrayList<>();

		AccessData ad1 = getDefaultAccessData(JunitMadkit.DEFAULT_NETWORK_GROUP_FOR_ACCESS_DATA);
		AccessData ad2 = getDefaultLoginData(getClientOrPeerToPeerLogins(hostIdentifier, loginIndexes),
				JunitMadkit.DEFAULT_NETWORK_GROUP_FOR_ACCESS_DATA, JunitMadkit.NETWORK_GROUP_FOR_LOGIN_DATA,
				canTakeLoginInitiative, invalidPassord);

		res.add(new AccessDataMKEventListener(ad1));
		res.add(new AccessDataMKEventListener(ad1, ad2));
		res.add(new AccessDataMKEventListener(ad2));
		return res;
	}

	public static ArrayList<AccessDataMKEventListener> getAccessDataMKEventListenerForServerConnections(
			final boolean canTakeLoginInitiative, final Runnable invalidPassord, CustumHostIdentifier hostIdentifier,
			int... loginIndexes) {
		return getAccessDataMKEventListenerForPeerToPeerConnections(canTakeLoginInitiative, invalidPassord,
				hostIdentifier, loginIndexes);
	}

	public static ArrayList<AccessDataMKEventListener> getAccessDataMKEventListenerForClientConnections(
			final boolean canTakeLoginInitiative, final Runnable invalidPassord, CustumHostIdentifier hostIdentifier,
			int... loginIndexes) {
		return getAccessDataMKEventListenerForPeerToPeerConnections(canTakeLoginInitiative, invalidPassord,
				hostIdentifier, loginIndexes);
	}
}
