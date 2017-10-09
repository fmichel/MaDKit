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

import com.distrimind.madkit.kernel.MadkitEventListener;
import com.distrimind.madkit.kernel.MadkitProperties;
import com.distrimind.madkit.kernel.network.connection.ConnectionProtocolProperties;
import com.distrimind.madkit.kernel.network.connection.secured.ClientSecuredProtocolPropertiesWithKnownPublicKey;
import com.distrimind.madkit.kernel.network.connection.secured.P2PSecuredConnectionProtocolWithECDHAlgorithmProperties;
import com.distrimind.madkit.kernel.network.connection.secured.ServerSecuredProcotolPropertiesWithKnownPublicKey;
import com.distrimind.madkit.kernel.network.connection.unsecured.CheckSumConnectionProtocolProperties;
import com.distrimind.madkit.kernel.network.connection.unsecured.UnsecuredConnectionProtocolProperties;
import com.distrimind.util.crypto.ASymmetricEncryptionType;
import com.distrimind.util.crypto.ASymmetricKeyPair;
import com.distrimind.util.crypto.ASymmetricKeyWrapperType;
import com.distrimind.util.crypto.SecureRandomType;
import com.distrimind.util.crypto.SymmetricEncryptionType;

import gnu.vm.jgnu.security.InvalidAlgorithmParameterException;
import gnu.vm.jgnu.security.NoSuchAlgorithmException;
import gnu.vm.jgnu.security.NoSuchProviderException;

/**
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MadkitLanEdition 1.0
 */
public class ConnectionsProtocolsMKEventListener implements MadkitEventListener {
	private final ArrayList<ConnectionProtocolProperties<?>> connectionProtocolsProperties;

	public ConnectionsProtocolsMKEventListener(ConnectionProtocolProperties<?>... connectionProtocolsProperties) {
		this.connectionProtocolsProperties = new ArrayList<>(connectionProtocolsProperties.length);
		for (ConnectionProtocolProperties<?> cpp : connectionProtocolsProperties)
			this.connectionProtocolsProperties.add(cpp);
	}

	public ConnectionsProtocolsMKEventListener(
			ArrayList<ConnectionProtocolProperties<?>> connectionProtocolsProperties) {
		this.connectionProtocolsProperties = connectionProtocolsProperties;
	}

	public void add(ConnectionProtocolProperties<?> cpp) {
		if (cpp == null)
			throw new NullPointerException();
		this.connectionProtocolsProperties.add(cpp);
	}

	public ArrayList<ConnectionProtocolProperties<?>> getConnectionProtocolProperties() {
		return connectionProtocolsProperties;
	}

	@Override
	public void onMadkitPropertiesLoaded(MadkitProperties _properties) {
		if (this.connectionProtocolsProperties != null) {
			for (ConnectionProtocolProperties<?> cpp : connectionProtocolsProperties) {
				_properties.networkProperties.addConnectionProtocol(cpp);
			}
		}

	}

	public static ArrayList<ConnectionsProtocolsMKEventListener> getConnectionsProtocolsMKEventListenerForPeerToPeerConnections(
			boolean isServer) {
		ArrayList<ConnectionsProtocolsMKEventListener> res = new ArrayList<>();
		P2PSecuredConnectionProtocolWithECDHAlgorithmProperties p2p_ecdh = new P2PSecuredConnectionProtocolWithECDHAlgorithmProperties();
		p2p_ecdh.isServer = isServer;
		res.add(new ConnectionsProtocolsMKEventListener(p2p_ecdh));
		UnsecuredConnectionProtocolProperties ucpp = new UnsecuredConnectionProtocolProperties();
		ucpp.isServer = isServer;
		p2p_ecdh = new P2PSecuredConnectionProtocolWithECDHAlgorithmProperties();
		p2p_ecdh.isServer = isServer;
		ucpp.subProtocolProperties = p2p_ecdh;
		res.add(new ConnectionsProtocolsMKEventListener(ucpp));
		p2p_ecdh = new P2PSecuredConnectionProtocolWithECDHAlgorithmProperties();
		p2p_ecdh.isServer = isServer;
		ucpp = new UnsecuredConnectionProtocolProperties();
		ucpp.isServer = isServer;
		p2p_ecdh.subProtocolProperties = ucpp;
		res.add(new ConnectionsProtocolsMKEventListener(p2p_ecdh));
		
		
		return res;
	}

	private static ASymmetricKeyPair keyPairForEncryption = null;
	private static ASymmetricKeyPair keyPairForSignature = null;
	private static short keyPairSize = 1024;
	private static int encryptionProfileIdentifier = -1;

	public static ASymmetricKeyPair getKeyPairForEncryption() throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
		if (keyPairForEncryption == null)
			keyPairForEncryption = ASymmetricEncryptionType.DEFAULT
					.getKeyPairGenerator(SecureRandomType.DEFAULT.getSingleton(null), keyPairSize).generateKeyPair();
		return keyPairForEncryption;
	}
	public static ASymmetricKeyPair getKeyPairForSignature() throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
		if (keyPairForSignature == null)
			keyPairForSignature = ASymmetricEncryptionType.DEFAULT
					.getKeyPairGenerator(SecureRandomType.DEFAULT.getSingleton(null), keyPairSize).generateKeyPair();
		return keyPairForSignature;
	}

	public static ArrayList<ConnectionsProtocolsMKEventListener> getConnectionsProtocolsMKEventListenerForServerConnection(
			boolean includeP2PConnectionPossibilityForClients)
			throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
		ArrayList<ConnectionsProtocolsMKEventListener> res = new ArrayList<>();
		res.add(new ConnectionsProtocolsMKEventListener(new UnsecuredConnectionProtocolProperties()));

		ServerSecuredProcotolPropertiesWithKnownPublicKey s = new ServerSecuredProcotolPropertiesWithKnownPublicKey();

		encryptionProfileIdentifier = s.addEncryptionProfile(getKeyPairForSignature(), SymmetricEncryptionType.DEFAULT, ASymmetricKeyWrapperType.DEFAULT);
		if (includeP2PConnectionPossibilityForClients) {
			P2PSecuredConnectionProtocolWithECDHAlgorithmProperties p2p = new P2PSecuredConnectionProtocolWithECDHAlgorithmProperties();
			p2p.isServer = false;
			res.add(new ConnectionsProtocolsMKEventListener(s, p2p));
		} else
			res.add(new ConnectionsProtocolsMKEventListener(s));

		ConnectionProtocolProperties<?> cpp = new UnsecuredConnectionProtocolProperties();
		s = new ServerSecuredProcotolPropertiesWithKnownPublicKey();
		encryptionProfileIdentifier = s.addEncryptionProfile(getKeyPairForSignature(), SymmetricEncryptionType.DEFAULT, ASymmetricKeyWrapperType.DEFAULT);
		cpp.subProtocolProperties = s;
		if (includeP2PConnectionPossibilityForClients) {
			ConnectionProtocolProperties<?> cpp2 = new UnsecuredConnectionProtocolProperties();
			P2PSecuredConnectionProtocolWithECDHAlgorithmProperties p2p = new P2PSecuredConnectionProtocolWithECDHAlgorithmProperties();
			cpp2.subProtocolProperties = p2p;
			res.add(new ConnectionsProtocolsMKEventListener(cpp, cpp2));
		} else
			res.add(new ConnectionsProtocolsMKEventListener(cpp));

		cpp = new CheckSumConnectionProtocolProperties();
		cpp.subProtocolProperties = new UnsecuredConnectionProtocolProperties();
		res.add(new ConnectionsProtocolsMKEventListener(cpp));
		return res;
	}

	public static ArrayList<ConnectionsProtocolsMKEventListener> getConnectionsProtocolsMKEventListenerForClientConnection(
			boolean includeP2PConnectionPossibilityForClients)
			throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
		ArrayList<ConnectionsProtocolsMKEventListener> res = new ArrayList<>();
		UnsecuredConnectionProtocolProperties u = new UnsecuredConnectionProtocolProperties();
		u.isServer = false;
		res.add(new ConnectionsProtocolsMKEventListener(u));

		ClientSecuredProtocolPropertiesWithKnownPublicKey c = new ClientSecuredProtocolPropertiesWithKnownPublicKey();
		c.setEncryptionProfile(encryptionProfileIdentifier, getKeyPairForSignature().getASymmetricPublicKey(), SymmetricEncryptionType.DEFAULT,ASymmetricKeyWrapperType.DEFAULT);
		if (includeP2PConnectionPossibilityForClients) {
			P2PSecuredConnectionProtocolWithECDHAlgorithmProperties p2p = new P2PSecuredConnectionProtocolWithECDHAlgorithmProperties();
			p2p.isServer = false;
			res.add(new ConnectionsProtocolsMKEventListener(c, p2p));
		} else
			res.add(new ConnectionsProtocolsMKEventListener(c));

		ConnectionProtocolProperties<?> cpp = u = new UnsecuredConnectionProtocolProperties();
		u.isServer = false;
		c = new ClientSecuredProtocolPropertiesWithKnownPublicKey();
		c.setEncryptionProfile(encryptionProfileIdentifier, getKeyPairForSignature().getASymmetricPublicKey(), SymmetricEncryptionType.DEFAULT,ASymmetricKeyWrapperType.DEFAULT);
		cpp.subProtocolProperties = c;
		if (includeP2PConnectionPossibilityForClients) {
			UnsecuredConnectionProtocolProperties cpp2 = new UnsecuredConnectionProtocolProperties();
			cpp2.isServer = false;
			P2PSecuredConnectionProtocolWithECDHAlgorithmProperties p2p = new P2PSecuredConnectionProtocolWithECDHAlgorithmProperties();
			p2p.isServer = false;
			cpp2.subProtocolProperties = p2p;
			res.add(new ConnectionsProtocolsMKEventListener(cpp, cpp2));
		} else {

			res.add(new ConnectionsProtocolsMKEventListener(cpp));
		}

		u = new UnsecuredConnectionProtocolProperties();
		u.isServer = false;
		CheckSumConnectionProtocolProperties cs = new CheckSumConnectionProtocolProperties();
		cs.subProtocolProperties = u;
		cs.isServer = false;
		res.add(new ConnectionsProtocolsMKEventListener(cs));
		return res;
	}
}
