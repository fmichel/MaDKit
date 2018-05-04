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
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collection;

import com.distrimind.madkit.exceptions.MessageSerializationException;
import com.distrimind.madkit.kernel.network.NetworkProperties;
import com.distrimind.madkit.util.SerializationTools;


/**
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MadkitLanEdition 1.0
 */
class LoginConfirmationMessage extends AccessMessage {

	/**
	 * 
	 */
	private static final long serialVersionUID = -296815844676424978L;

	public ArrayList<Identifier> accepted_identifiers;
	public ArrayList<Identifier> denied_identifiers;
	private transient short nbAnomalies;
	private boolean checkDifferedMessages;

	LoginConfirmationMessage()
	{
		
	}
	
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		int size=in.readInt();
		int totalSize=4;
		int globalSize=NetworkProperties.GLOBAL_MAX_SHORT_DATA_SIZE;
		if (size<0 || totalSize+size*4>globalSize)
			throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN);
		accepted_identifiers=new ArrayList<>(size);
		for (int i=0;i<size;i++)
		{
			Object o=SerializationTools.readExternalizableAndSizable(in, false);
			if (!(o instanceof Identifier))
				throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN);
			Identifier id=(Identifier)o;
			totalSize+=id.getInternalSerializedSize();
			if (totalSize>globalSize)
				throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN);
			accepted_identifiers.add(id);
		}
		size=in.readInt();
		totalSize+=4;
		if (size<0 || totalSize+size*4>globalSize)
			throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN);
		denied_identifiers=new ArrayList<>(size);
		for (int i=0;i<size;i++)
		{
			Object o=SerializationTools.readExternalizableAndSizable(in, false);
			if (!(o instanceof Identifier))
				throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN);
			Identifier id=(Identifier)o;
			totalSize+=id.getInternalSerializedSize();
			if (totalSize>globalSize)
				throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN);
			denied_identifiers.add(id);
		}
		checkDifferedMessages=in.readBoolean();
	}


	@Override
	public void writeExternal(ObjectOutput oos) throws IOException {
		oos.writeInt(accepted_identifiers.size()); 
		for (Identifier id : accepted_identifiers)
			SerializationTools.writeExternalizableAndSizable(oos, id, false);

		oos.writeInt(denied_identifiers.size()); 
		for (Identifier id : denied_identifiers)
			SerializationTools.writeExternalizableAndSizable(oos, id, false);
			
		oos.writeBoolean(checkDifferedMessages);
		
	}
	
	public LoginConfirmationMessage(Collection<PairOfIdentifiers> _accepted_identifiers,
			Collection<PairOfIdentifiers> _denied_identifiers, short nbAnomalies,
			boolean checkDifferedMessages) {
		if (_accepted_identifiers == null)
			throw new NullPointerException("_accepted_identifiers");
		if (_denied_identifiers == null)
			throw new NullPointerException("_denied_identifiers");
		accepted_identifiers = new ArrayList<>();
		for (PairOfIdentifiers poi : _accepted_identifiers) {
			if (poi.getLocalIdentifier() == null)
				throw new NullPointerException();
			accepted_identifiers.add(poi.getLocalIdentifier());
		}
		denied_identifiers = new ArrayList<>();
		for (PairOfIdentifiers poi : _accepted_identifiers) {
			if (poi.getLocalIdentifier() == null)
				throw new NullPointerException();
			denied_identifiers.add(poi.getLocalIdentifier());
		}
		this.nbAnomalies = nbAnomalies;
		this.checkDifferedMessages = checkDifferedMessages;
	}

	@Override
	public short getNbAnomalies() {
		return nbAnomalies;
	}

	

	@Override
	public boolean checkDifferedMessages() {
		return checkDifferedMessages;
	}

}
