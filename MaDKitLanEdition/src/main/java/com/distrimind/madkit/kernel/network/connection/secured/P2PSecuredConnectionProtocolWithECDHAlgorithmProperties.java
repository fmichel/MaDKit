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

import com.distrimind.madkit.exceptions.ConnectionException;
import com.distrimind.madkit.kernel.network.connection.ConnectionProtocolProperties;
import com.distrimind.util.crypto.EllipticCurveDiffieHellmanType;
import com.distrimind.util.crypto.SymmetricAuthentifiedSignatureType;
import com.distrimind.util.crypto.SymmetricEncryptionType;

/**
 * 
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MadkitLanEdition 1.2
 */
public class P2PSecuredConnectionProtocolWithECDHAlgorithmProperties extends ConnectionProtocolProperties<P2PSecuredConnectionProtocolWithECDHAlgorithm> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -616754777676015639L;

	public P2PSecuredConnectionProtocolWithECDHAlgorithmProperties() {
		super(P2PSecuredConnectionProtocolWithECDHAlgorithm.class);
	}

	/**
	 * Tells if the connection must be encrypted or not. If not, only signature
	 * packet will be enabled.
	 */
	public boolean enableEncryption = true;

	/**
	 * Elliptic Curve Diffie Hellman Type
	 */
	public EllipticCurveDiffieHellmanType ellipticCurveDiffieHellmanType=EllipticCurveDiffieHellmanType.BC_FIPS_ECDDH_384_AES128;
	
	/**
	 * Symmetric encryption algorithm
	 */
	public SymmetricEncryptionType symmetricEncryptionType = SymmetricEncryptionType.DEFAULT;

	/**
	 * Symmetric signature algorithm
	 */
	public SymmetricAuthentifiedSignatureType symmetricSignatureType=SymmetricAuthentifiedSignatureType.BC_FIPS_HMAC_SHA_512;
	
	/**
	 * symmetric key size in bits
	 */
	public short symmetricKeySizeBits=symmetricEncryptionType.getDefaultKeySizeBits();
	
	/**
	 * Tells if the current peer can receive an ask for connection.
	 */
	public boolean isServer = true;

	void checkProperties() throws ConnectionException {
		if (ellipticCurveDiffieHellmanType==null)
			throw new ConnectionException(new NullPointerException("ellipticCurveDiffieHellmanType"));
		if (symmetricEncryptionType==null)
			throw new ConnectionException(new NullPointerException("symmetricEncryptionType"));
		if (symmetricSignatureType==null)
			throw new ConnectionException(new NullPointerException("symmetricSignatureType"));
	}

	@Override
	protected boolean needsServerSocketImpl() {
		return isServer;
	}

	@Override
	public boolean canTakeConnectionInitiativeImpl() {
		return true;
	}

	@Override
	public boolean supportBidirectionnalConnectionInitiativeImpl() {
		return true;
	}

	@Override
	protected boolean canBeServer() {
		return isServer;
	}

}
