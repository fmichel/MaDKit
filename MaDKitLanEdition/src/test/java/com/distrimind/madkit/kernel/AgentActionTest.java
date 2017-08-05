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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.distrimind.madkit.action.AgentAction;
import com.distrimind.madkit.kernel.AbstractAgent;
import com.distrimind.madkit.kernel.Agent;
import com.distrimind.madkit.message.EnumMessage;

/**
 * @author Fabien Michel
 * @author Jason Mahdjoub
 * @since MadkitLanEdition 1.0
 * @version 1.0
 * 
 */
public class AgentActionTest extends JunitMadkit {

	@Test
	public void LAUNCH_AGENT() {
		addMadkitArgs("--kernelLogLevel", "ALL");
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				/*
				 * AbstractAgent a = new Agent(); EnumMessage<AgentAction> m = new
				 * EnumMessage<>(AgentAction.LAUNCH_AGENT,a); proceedEnumMessage(m);
				 * assertTrue(a.isAlive()); assertFalse(a.hasGUI());
				 */

				AbstractAgent a = new Agent();
				EnumMessage<AgentAction> m = new EnumMessage<>(AgentAction.LAUNCH_AGENT, a, new Boolean(true));
				proceedEnumMessage(m);
				assertTrue(a.isAlive());
				assertTrue(a.hasGUI());

				/*
				 * a = new Agent(); m = new EnumMessage<>(AgentAction.LAUNCH_AGENT,a,new
				 * Integer(1),new Boolean(true)); proceedEnumMessage(m);
				 * assertTrue(a.isAlive()); assertTrue(a.hasGUI());
				 * 
				 * a = new Agent(); m = new EnumMessage<>(AgentAction.LAUNCH_AGENT,a,new
				 * Integer(0)); proceedEnumMessage(m); assertFalse(a.hasGUI());
				 * assertFalse(a.isAlive());
				 */

			}
		});
	}

	@Test
	public void LAUNCH_AGENT_wrongType() {
		addMadkitArgs("--kernelLogLevel", "ALL");
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				AbstractAgent help = new AbstractAgent();
				launchAgent(help);
				createDefaultCGR(help);
				createDefaultCGR(this);
				AbstractAgent a = new Agent();
				EnumMessage<AgentAction> m = new EnumMessage<>(AgentAction.LAUNCH_AGENT, a, new Object());
				sendMessage(GROUP, ROLE, m);
				proceedEnumMessage(m);
				assertFalse(a.hasGUI());

				a = new Agent();
				m = new EnumMessage<>(AgentAction.LAUNCH_AGENT, new Object(), new Boolean(true));
				proceedEnumMessage(m);
				assertFalse(a.hasGUI());
			}
		});
	}

	@Test
	public void LAUNCH_AGENT_null() {
		addMadkitArgs("--kernelLogLevel", "ALL");
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				AbstractAgent help = new AbstractAgent();
				launchAgent(help);
				createDefaultCGR(help);
				createDefaultCGR(this);
				AbstractAgent a = new Agent();
				EnumMessage<AgentAction> m = new EnumMessage<>(AgentAction.LAUNCH_AGENT, a, null);
				sendMessage(GROUP, ROLE, m);
				proceedEnumMessage(m);
				assertFalse(a.hasGUI());

				a = new Agent();
				m = new EnumMessage<>(AgentAction.LAUNCH_AGENT, null, new Boolean(true));
				proceedEnumMessage(m);
				assertFalse(a.hasGUI());
			}
		});
	}
}
