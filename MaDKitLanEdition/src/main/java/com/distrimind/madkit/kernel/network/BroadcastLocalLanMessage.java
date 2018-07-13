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

import com.distrimind.madkit.kernel.AbstractGroup;
import com.distrimind.madkit.kernel.AgentAddress;
import com.distrimind.madkit.kernel.Message;
import com.distrimind.madkit.kernel.network.DistantKernelAgent.ReceivedSerializableObject;

/**
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MadkitLanEdition 1.0
 */
public class BroadcastLocalLanMessage extends LocalLanMessage {


	AbstractGroup abstract_group;
	String role;
	ArrayList<AgentAddress> agentAddressesSender;

	BroadcastLocalLanMessage(Message _message, ReceivedSerializableObject originalMessage,
			AbstractGroup _destination_groups, String _destination_role,
			ArrayList<AgentAddress> _agentAddressesSender) {
		super(_message, originalMessage);
		abstract_group = _destination_groups;
		role = _destination_role;
		agentAddressesSender = _agentAddressesSender;
	}

	private BroadcastLocalLanMessage(BroadcastLocalLanMessage This, Message _message,
			ReceivedSerializableObject originalMessage, AbstractGroup _destination_groups, String _destination_role,
			ArrayList<AgentAddress> _agentAddressesSender, MessageLocker locker) {
		super(This, _message, originalMessage, locker);
		abstract_group = _destination_groups;
		role = _destination_role;
		agentAddressesSender = _agentAddressesSender;
	}

	@SuppressWarnings("unused")
	BroadcastLocalLanMessage(Message _message, AbstractGroup _destination_groups, String _destination_role,
							 ArrayList<AgentAddress> _agentAddressesSender) {
		super(_message, null);
		abstract_group = _destination_groups;
		role = _destination_role;
		agentAddressesSender = _agentAddressesSender;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[conversationID=" + getOriginalMessage().getConversationID()
				+ ", readyForInjection=" + readyForInjection + ", abstract_group=" + abstract_group + ", role=" + role
				+ "]";
	}

	/*
	 * private BroadcastLocalLanMessage(MessageLocker _locker, Message _message,
	 * AbstractGroup _destination_groups, String _destination_role,
	 * ArrayList<AgentAddress> _agentAddressesSender) { super(_locker, _message);
	 * abstract_group=_destination_groups; role=_destination_role;
	 * agentAddressesSender=_agentAddressesSender; }
	 */

	public AgentAddress getAgentAddressSenderFromReceiver(AgentAddress _agent_address) {
		for (AgentAddress aa : agentAddressesSender) {
			if (aa.getCommunity().equals(_agent_address.getCommunity())
					&& aa.getGroup().equals(_agent_address.getGroup()))
				return aa;
		}
		return null;
	}

	public AbstractGroup getAbstractGroup() {
		return abstract_group;
	}

	public String getRole() {
		return role;
	}
	/*
	 * BroadcastLocalLanMessage getBroadcast(AbstractGroup _destination_group, int
	 * _id_packet) { BroadcastLocalLanMessage bllm=new
	 * BroadcastLocalLanMessage(this.getMessageLocker(), this.message,
	 * _destination_group, role, agentAddressesSender);
	 * bllm.setIDPacket(_id_packet); return bllm; }
	 */

	public BroadcastLanMessage getBroadcastMessage(AbstractGroup concerned_groups) {
		Message m = getOriginalMessage().clone();
		// MadkitKernelAccess.setReceiver(m, m.getReceiver());
		/*
		 * KernelAddress ka=MadkitKernelAccess.getOrigin(m.getConversationID()); if (ka
		 * instanceof KernelAddressInterfaced)
		 * MadkitKernelAccess.setOrigin(m.getConversationID(),
		 * ((KernelAddressInterfaced)ka).getOriginalKernelAddress());
		 */
		return new BroadcastLanMessage(m, concerned_groups, role, agentAddressesSender);
	}

	@Override
	public BroadcastLocalLanMessage clone() {
		BroadcastLocalLanMessage b = new BroadcastLocalLanMessage(this, this.getOriginalMessage().clone(),
				originalMessage == null ? null : originalMessage.clone(), this.abstract_group, this.role,
				agentAddressesSender, this.getMessageLocker());
		b.readyForInjection = readyForInjection;
		return b;
	}

}
