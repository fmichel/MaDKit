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
package com.distrimind.madkit.roles.manager;

import static org.junit.Assert.assertNotNull;

import java.util.logging.Level;

import org.junit.Test;

import com.distrimind.madkit.agr.Organization;
import com.distrimind.madkit.kernel.AbstractAgent;
import com.distrimind.madkit.kernel.Agent;
import com.distrimind.madkit.kernel.AgentNetworkID;
import com.distrimind.madkit.kernel.Gatekeeper;
import com.distrimind.madkit.kernel.Group;
import com.distrimind.madkit.kernel.JunitMadkit;
import com.distrimind.madkit.kernel.Message;
import com.distrimind.madkit.testing.util.agent.NormalAgent;

/**
 * @author Fabien Michel
 * @author Jason Mahdjoub
 * @since MaDKit 5.0.0.10
 * @since MadkitLanEdition 1.0
 * @version 1.0
 * 
 */
public class CandidateTest extends JunitMadkit {

	@Test
	public void createGroupHook() {
		addMadkitArgs("--agentLogLevel", Level.ALL.toString()
		// ,LevelOption.kernelLogLevel.toString(),Level.ALL.toString()
		);
		launchTest(new NormalAgent() {
			@Override
			protected void activate() throws InterruptedException {
				launchAgent(new Agent() {
					@Override
					protected void activate() {
						createGroup(GROUP, new Gatekeeper() {

							@Override
							public boolean allowAgentToTakeRole(Group _group, String _roleName,
									final Class<? extends AbstractAgent> requesterClass, AgentNetworkID _agentNetworkID,
									Object _memberCard) {
								return false;
							}

							@Override
							public boolean allowAgentToCreateSubGroup(Group _parent_group, Group _sub_group,
									final Class<? extends AbstractAgent> requesterClass, AgentNetworkID _agentNetworkID,
									Object _memberCard) {
								return false;
							}
						});
					}

					@Override
					protected void liveCycle() throws InterruptedException {
						setLogLevel(Level.ALL);
						sendReply(waitNextMessage(), new Message());
						this.killAgent(this);
					}
				});
				sendMessage(GROUP, Organization.GROUP_MANAGER_ROLE, new Message());
				pause(10);
				Message m = waitNextMessage();
				assertNotNull(m);
				System.err.println(m);
			}
		});
		pause(null, 100);
	}

}
