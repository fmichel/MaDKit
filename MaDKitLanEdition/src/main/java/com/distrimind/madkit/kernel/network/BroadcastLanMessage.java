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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import com.distrimind.madkit.exceptions.MessageSerializationException;
import com.distrimind.madkit.kernel.AbstractGroup;
import com.distrimind.madkit.kernel.AgentAddress;
import com.distrimind.madkit.kernel.Group;
import com.distrimind.madkit.kernel.Message;
import com.distrimind.madkit.util.SerializationTools;

/**
 * 
 * @author Jason Mahdjoub
 * @version 1.1
 * @since MadkitLanEdition 1.0
 */
final class BroadcastLanMessage extends LanMessage {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1688077541837766083L;

	public AbstractGroup abstract_group;
	public String role;
	public ArrayList<AgentAddress> agentAddressesSender;

	@Override
	public void readAndCheckObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		
		super.readAndCheckObject(in);
		int globalSize=NetworkProperties.GLOBAL_MAX_SHORT_DATA_SIZE;
		int totalSize=0;
		Object o=in.readObject();
		if (!(o instanceof AbstractGroup))
			throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN);
		abstract_group=(AbstractGroup)o;
		totalSize+=abstract_group.getInternalSerializedSize();
		role=SerializationTools.readString(in, Group.MAX_ROLE_NAME_LENGTH, false);
		totalSize+=SerializationTools.getInternalSize(role, Group.MAX_ROLE_NAME_LENGTH);
		if (totalSize>globalSize)
			throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN);
		int size=in.readInt();
		totalSize+=4;
		if (size<=0 || totalSize+size*4>globalSize)
			throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN);
		agentAddressesSender=new ArrayList<>(size);
		for (int i=0;i<size;i++)
		{
			o=in.readObject();
			if (!(o instanceof AgentAddress))
				throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN);
			AgentAddress aa=(AgentAddress)o;
			totalSize+=aa.getInternalSerializedSize();
			if (totalSize>globalSize)
				throw new MessageSerializationException(Integrity.FAIL_AND_CANDIDATE_TO_BAN);

			agentAddressesSender.add(aa);
			
		}
	}

	@Override
	public void writeAndCheckObject(ObjectOutputStream oos) throws IOException {
		super.writeAndCheckObject(oos);
		oos.writeObject(abstract_group);
		SerializationTools.writeString(oos, role, Group.MAX_ROLE_NAME_LENGTH, false);
		oos.writeInt(agentAddressesSender.size());
		for (AgentAddress aa : agentAddressesSender)
			oos.writeObject(aa);
	}

	
	BroadcastLanMessage(Message _message, AbstractGroup _abstract_group, String _role,
			ArrayList<AgentAddress> _agentAddressesSender) {
		super(_message);
		if (_abstract_group == null)
			throw new NullPointerException("_abstract_group");
		if (_role == null)
			throw new NullPointerException("_role");
		if (_agentAddressesSender == null)
			throw new NullPointerException("_agentAddressesSender");
		if (_agentAddressesSender.isEmpty())
			throw new IllegalArgumentException("_agentAddressesSender cannot be empty !");
		for (AgentAddress aa : _agentAddressesSender) {
			if (aa == null)
				throw new NullPointerException("null element present on _agentAddressesSender !");
		}
		abstract_group = _abstract_group;
		role = _role;
		agentAddressesSender = _agentAddressesSender;
	}

	

	void setAccetedGroups(AbstractGroup groups) {
		abstract_group = groups;
	}

	@Override
	public boolean excludedFromEncryption() {
		return message.excludedFromEncryption();
	}
	
	private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		readAndCheckObject(in);
	}
	private void writeObject(final ObjectOutputStream oos) throws IOException
	{
		writeAndCheckObject(oos);
	}

	

}
