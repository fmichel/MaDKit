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
package com.distrimind.madkit.concurrent;

import static com.distrimind.madkit.kernel.JunitMadkit.GROUP;
import static com.distrimind.madkit.kernel.JunitMadkit.ROLE;
import static com.distrimind.madkit.kernel.JunitMadkit.testFails;

import com.distrimind.madkit.kernel.Agent;
import com.distrimind.madkit.kernel.AgentAddress;
import com.distrimind.madkit.kernel.Group;
import com.distrimind.madkit.kernel.JunitMadkit;

/**
 * @author Fabien Michel
 * @author Jason Mahdjoub
 * @since MadkitLanEdition 1.0
 * @version 1.0
 *
 */
public class ConcurrentTestAgent extends Agent {

	private boolean playing = false;
	int cycles = 100000;

	@Override
	protected void liveCycle() {
		// setLogLevel(Level.INFO);
		// getLogger().setWarningLogLevel(Level.OFF);

		if (Math.random() < .5) {
			if (createGroup(GROUP) == ReturnCode.SUCCESS) {
				final ReturnCode requestRole = requestRole(GROUP, ROLE);
				if (requestRole != ReturnCode.SUCCESS) {
					System.err
							.println(" req failed " + requestRole + " " + this + " " + getAgentAddressIn(GROUP, ROLE));
					failed(requestRole);
					this.killAgent(this);
				}
				playing = true;
			} else {
				requestRole(GROUP, ROLE);
			}
		} else {
			final AgentAddress agentAddressIn = getAgentAddressIn(GROUP, ROLE);
			if (playing) {
				// if(agentAddressIn != null){ //TODO
				if (Math.random() < .5) {
					// System.err.println(getAgentAddressIn(COMMUNITY, GROUP, ROLE));
					final ReturnCode leaveRole = leaveRole(GROUP, ROLE);
					if (leaveRole != ReturnCode.SUCCESS) {
						System.err.println(this.toString() + checkAgentAddress(agentAddressIn));
						System.err.println(agentAddressIn + " leave role failed " + leaveRole + " " + this + " "
								+ getAgentAddressIn(GROUP, ROLE));
						failed(leaveRole);
						// System.exit(0);
						this.killAgent(this);
					}
					playing = false;
				} else {
					// System.err.println(getAgentAddressIn(COMMUNITY, GROUP, ROLE));
					final ReturnCode leaveGroup = leaveGroup(GROUP);
					if (leaveGroup != ReturnCode.SUCCESS) {
						System.err.println(checkAgentAddress(agentAddressIn));
						System.err.println(agentAddressIn + " leave group failed " + leaveGroup + " " + this + " "
								+ getAgentAddressIn(GROUP, ROLE));
						failed(leaveGroup);
						// System.exit(0);
						this.killAgent(this);
					}
					playing = false;
				}
			}
		}
		if (cycles-- == 0)
			this.killAgent(this);
	}

	/**
	 * @param code
	 * 
	 */
	private void failed(ReturnCode code) {
		final AgentAddress agentAddressIn = getAgentAddressIn(GROUP, ROLE);
		testFails(new Exception(
				code.toString() + (agentAddressIn == null ? "not having the role !" : agentAddressIn.toString())));
	}
}

class ConcurrentTestAgentBis extends ConcurrentTestAgent {
	@Override
	protected void liveCycle() {

		Group groupName = new Group(JunitMadkit.C, JunitMadkit.G + ((int) (Math.random() * 10)));
		if (Math.random() < .5) {
			if (createGroup(groupName) == ReturnCode.SUCCESS) {
				if (requestRole(groupName, ROLE) != ReturnCode.SUCCESS) {
					testFails(new Exception());
					this.killAgent(this);
				}
			} else {
				requestRole(groupName, ROLE);
			}
		} else {
			if (Math.random() < .5) {
				leaveRole(groupName, ROLE);
			} else {
				leaveGroup(groupName);
			}
		}
		if (cycles-- == 0)
			this.killAgent(this);

	}

}