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
package com.distrimind.madkit.message.hook;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.distrimind.madkit.kernel.AbstractGroup;
import com.distrimind.madkit.kernel.Group;
import com.distrimind.madkit.kernel.KernelAddress;
import com.distrimind.madkit.message.hook.HookMessage.AgentActionEvent;

/**
 * Notification message about accessible groups from the network or toward the
 * network (in/out), concerning one specific kernel address.
 * 
 * 
 * @author Jason Mahdjoub
 * 
 * @version 1.0
 * @since MadKitLanEdition 1.0
 * 
 * @see AgentActionEvent#ACCESSIBLE_LAN_GROUPS_GIVEN_BY_DISTANT_PEER
 * @see AgentActionEvent#ACCESSIBLE_LAN_GROUPS_GIVEN_TO_DISTANT_PEER
 */
public class NetworkGroupsAccessEvent extends HookMessage {



	private final List<Group> effective_accessible_groups;
	private final AbstractGroup general_accessible_groups;
	private KernelAddress concerned_kernel_address_interfaced;

	private static Group[] toArray(Collection<Group> _accessible_groups) {
		Group res[] = new Group[_accessible_groups.size()];
		int index = 0;
		for (Group g : _accessible_groups) {
			res[index++] = g;
		}
		return res;
	}

	public NetworkGroupsAccessEvent(AgentActionEvent action, AbstractGroup general_accessible_groups,
			Collection<Group> _accessible_groups, KernelAddress _concerned_kernel_address_interfaced) {
		this(action, general_accessible_groups, toArray(_accessible_groups), _concerned_kernel_address_interfaced);
	}

	public NetworkGroupsAccessEvent(AgentActionEvent action, AbstractGroup general_accessible_groups,
			Group[] _accessible_groups, KernelAddress _concerned_kernel_address_interfaced) {
		super(action);
		if (action == null)
			throw new NullPointerException("action");
		if (!action.equals(AgentActionEvent.ACCESSIBLE_LAN_GROUPS_GIVEN_BY_DISTANT_PEER)
				&& !action.equals(AgentActionEvent.ACCESSIBLE_LAN_GROUPS_GIVEN_TO_DISTANT_PEER))
			throw new IllegalArgumentException(
					"action must be equal to ACCESSIBLE_LAN_GROUPS_GIVEN_BY_DISTANT_PEER or ACCESSIBLE_LAN_GROUPS_GIVEN_TO_DISTANT_PEER");
		if (general_accessible_groups == null)
			throw new NullPointerException("general_accessible_groups");

		effective_accessible_groups = Collections.unmodifiableList(Arrays.asList(_accessible_groups));
		concerned_kernel_address_interfaced = _concerned_kernel_address_interfaced;
		this.general_accessible_groups = general_accessible_groups;
	}

	@Override
	public String toString() {
		return "NetworkGroupsAccessEvent[action=" + getContent() + ", concerned_kernel_address_interfaced="
				+ concerned_kernel_address_interfaced + ", general_accessible_groups=" + general_accessible_groups
				+ "]";
	}

	public List<Group> getRequestedAccessibleGroups() {
		return effective_accessible_groups;
	}

	public AbstractGroup getGeneralAcceptedGroups() {
		return general_accessible_groups;
	}

	public KernelAddress getConcernedKernelAddress() {
		return concerned_kernel_address_interfaced;
	}

	/**
	 * Tells if the current instance represents authorized groups to distant peer,
	 * and from local MadKit kernel.
	 * 
	 * @return true if the current instance represents authorized groups to distant
	 *         peer, and from local MadKit kernel.
	 */
	public boolean concernsAuthorizedGroupsToDistantPeer() {
		return getContent().equals(AgentActionEvent.ACCESSIBLE_LAN_GROUPS_GIVEN_TO_DISTANT_PEER);
	}

	/**
	 * Tells if the current instance represents authorized groups from distant peer
	 * to the local agents
	 * 
	 * @return true if the current instance represents authorized groups from
	 *         distant peer to the local agents
	 */
	public boolean concernsAuthorizedGroupsFromDistantPeer() {
		return getContent().equals(AgentActionEvent.ACCESSIBLE_LAN_GROUPS_GIVEN_BY_DISTANT_PEER);
	}
}
