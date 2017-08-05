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

import java.net.InetAddress;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.distrimind.madkit.kernel.network.InetAddressFilters;
import com.distrimind.madkit.util.XMLObjectParser;
import com.distrimind.madkit.util.XMLUtilities;
import com.distrimind.util.crypto.ASymmetricEncryptionType;
import com.distrimind.util.crypto.MessageDigestType;
import com.distrimind.util.crypto.PasswordHashType;
import com.distrimind.util.properties.XMLProperties;

/**
 * Represents properties of a specific connection protocol
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MadkitLanEdition
 *
 */
public class AccessProtocolProperties extends XMLProperties {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5967436161679203461L;

	/**
	 * Allowed and forbidden lan
	 */
	public InetAddressFilters filters;

	/**
	 * The asymetric cipher key size used for
	 * {@link PeerToPeerASymmetricSecretMessageExchanger}
	 */
	public short aSymetricKeySize = 4096;

	/**
	 * The minimum asymetric cipher RSA Key size used for
	 * {@link PeerToPeerASymmetricSecretMessageExchanger}
	 */
	public final int minASymetricKeySize = 1024;

	/**
	 * Asymmetric encryption algorithm used for
	 * {@link PeerToPeerASymmetricSecretMessageExchanger}
	 */
	public ASymmetricEncryptionType aSymetricEncryptionType = ASymmetricEncryptionType.DEFAULT;

	/**
	 * Message digest type used for {@link P2PASymmetricSecretMessageExchanger}
	 */
	public MessageDigestType messageDigestType = MessageDigestType.SHA_512;

	/**
	 * PasswordDigestType used for {@link P2PASymmetricSecretMessageExchanger}
	 */
	public PasswordHashType passwordHashType = PasswordHashType.DEFAULT;

	/**
	 * Password hash iterations
	 */
	public int passwordHashIterations = 100000;

	/**
	 * Default duration of a public key before being regenerated. Must be greater or
	 * equal than 0.
	 */
	public final long defaultASymmetricKeyExpirationMs = 15552000000l;

	/**
	 * The duration of a public key before being regenerated. Must be greater or
	 * equal than 0.
	 */
	public long aSymmetricKeyExpirationMs = defaultASymmetricKeyExpirationMs;

	/**
	 * Tells if the identifiers must be encrypted before being sent to the distant
	 * peer. When it is possible and if it does not decrease security level, we
	 * recommend to set this boolean to false. Else set it to true. Notice that if
	 * this boolean is set to true, all identifiers of the distant peer will be
	 * encrypted and compared with the local encrypted given login. So the login
	 * process will be slowest.
	 */
	public boolean encryptIdentifiersBeforeSendingToDistantPeer = true;

	void checkProperties() throws AccessException {
		if (aSymetricKeySize < minASymetricKeySize)
			throw new AccessException("aSymetricKeySize value must be greter than " + minASymetricKeySize);
		int tmp = aSymetricKeySize;
		while (tmp != 1) {
			if (tmp % 2 == 0)
				tmp = tmp / 2;
			else
				throw new AccessException("The RSA key size have a size of " + aSymetricKeySize
						+ ". This number must correspond to this schema : _rsa_key_size=2^x.");
		}

	}

	public AccessProtocolProperties() {
		super(new XMLObjectParser());
	}

	/**
	 * Tells if the filter accept the connection with the given parameters
	 * 
	 * @param _distant_inet_address
	 *            the distant inet address
	 * @param _local_port
	 *            the local port
	 * @return true if the filter accept the connection with the given parameters
	 */
	public boolean isConcernedBy(InetAddress _distant_inet_address, int _local_port) {
		if (filters == null)
			return true;
		else
			return filters.isConcernedBy(_distant_inet_address, _local_port);
	}

	@Override
	public Node getRootNode(Document _document) {
		for (int i = 0; i < _document.getChildNodes().getLength(); i++) {
			Node n = _document.getChildNodes().item(i);
			if (n.getNodeName().equals(XMLUtilities.MDK))
				return n;
		}
		return null;
	}

	@Override
	public Node createOrGetRootNode(Document _document) {
		Node res = getRootNode(_document);
		if (res == null) {
			res = _document.createElement(XMLUtilities.MDK);
			_document.appendChild(res);
		}
		return res;
	}

}
