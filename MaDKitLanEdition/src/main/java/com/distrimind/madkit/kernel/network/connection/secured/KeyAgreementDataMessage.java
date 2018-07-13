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

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import com.distrimind.madkit.kernel.network.NetworkProperties;
import com.distrimind.madkit.kernel.network.connection.ConnectionMessage;
import com.distrimind.madkit.util.SerializationTools;

/**

 * @author Jason Mahdjoub
 * @version 1.1
 * @since MadkitLanEdition 1.0
 * @see ServerSecuredConnectionProtocolWithKnwonPublicKey
 */
@SuppressWarnings("ExternalizableWithoutPublicNoArgConstructor")
public class KeyAgreementDataMessage extends ConnectionMessage {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4560100871284266098L;
	
	private byte[] data;
	private byte[] materialKey;
	
	@SuppressWarnings("unused")
	KeyAgreementDataMessage()
	{
		
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		int totalSize=NetworkProperties.GLOBAL_MAX_SHORT_DATA_SIZE;
		data=SerializationTools.readBytes(in, totalSize, false);
		assert data != null;
		materialKey=SerializationTools.readBytes(in, totalSize-data.length, true);
	}


	@Override
	public void writeExternal(ObjectOutput oos) throws IOException {
		int totalSize=NetworkProperties.GLOBAL_MAX_SHORT_DATA_SIZE;
		SerializationTools.writeBytes(oos, data, totalSize, false);
		SerializationTools.writeBytes(oos, materialKey, totalSize-data.length, true);
		
	}
	

	public KeyAgreementDataMessage(byte[] data, byte[] materialKey) {
		super();
		this.data = data;
		this.materialKey=materialKey;
	}


	public byte[] getData() {
		return data;
	}


	public byte[] getMaterialKey()
	{
		return materialKey;
	}

	


	@Override
	public boolean excludedFromEncryption() {
		return false;
	}


	
	
}
