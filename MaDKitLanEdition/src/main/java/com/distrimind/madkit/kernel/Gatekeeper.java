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

package com.distrimind.madkit.kernel;

/**
 * This interface is implemented by objects which are used to verify if an agent
 * is allowed to play a certain role in a group, or if is allowed to create a
 * sub group into a parent group. Objects implementing this interface could be
 * used when creating a Group to secure it.
 * 
 * @author Fabien Michel
 * @author Jason Mahdjoub
 * @since MaDKit 3.0
 * @since MaDKitLanEdition 1.0
 * @version 3.0
 */
// TODO put an example of use
public interface Gatekeeper {

	/**
	 * @param _group
	 *            the group the agent wants to join
	 * @param roleName
	 *            the role the agent wants to play
	 * @param requesterClass
	 *            the requester's class
	 * @param agentNetworkID
	 *            a string identifying uniquely an agent, even over multiple
	 *            connected kernels
	 * @param memberCard
	 *            the access card provided by the agent
	 * @return <code>true</code> if the agent should be allowed to play this role in
	 *         the group, or <code>false</code> otherwise associated with this
	 *         {@link Gatekeeper}
	 */
	boolean allowAgentToTakeRole(final Group _group, final String roleName,
								 final Class<? extends AbstractAgent> requesterClass, final AgentNetworkID agentNetworkID,
								 final Object memberCard);

	/**
	 * @param parent_group
	 *            the parent group
	 * @param sub_group
	 *            the sub group the agent wants to create
	 * @param requesterClass
	 *            the requester's class
	 * @param agentNetworkID
	 *            a string identifying uniquely an agent, even over multiple
	 *            connected kernels
	 * @param memberCard
	 *            the access card provided by the agent
	 * @return <code>true</code> if the agent should be allowed to create a sub
	 *         group into the group, or <code>false</code> otherwise associated with
	 *         this {@link Gatekeeper}
	 */
	boolean allowAgentToCreateSubGroup(final Group parent_group, final Group sub_group,
									   final Class<? extends AbstractAgent> requesterClass, final AgentNetworkID agentNetworkID,
									   final Object memberCard);

}
