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
package com.distrimind.madkit.testing.util.agent;

import org.junit.Assert;

import com.distrimind.madkit.kernel.AgentAddress;
import com.distrimind.madkit.kernel.AgentFakeThread;
import com.distrimind.madkit.kernel.Group;
import com.distrimind.madkit.kernel.Message;

import static org.junit.Assert.*;

/**
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MadkitLanEdition 1.0
 */
public abstract class AgentAddressAgentTester extends AgentFakeThread {

	public boolean testAgentAddressReceiver(Message _message, Group group, String role) {
		try {
			AgentAddress local = getAgentAddressIn(group, role);
			assertTrue(local.isFrom(getKernelAddress()));
			final AgentAddress receiver = _message.getReceiver();
            assertEquals(receiver, local);
			assertTrue(receiver.isFrom(getKernelAddress()));
			Assert.assertTrue(checkAgentAddress(_message.getReceiver()));
			return true;
		} catch (Throwable e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean testAgentAddressSender(Message _message, Group group, String role) {
		try {
			assertFalse(_message.getSender().isFrom(getKernelAddress()));

			AgentAddress distant = null;
			for (AgentAddress aa : getAgentsWithRole(group, role)) {
				if (aa.equals(_message.getSender()))
					distant = aa;
			}
			Assert.assertNotNull(distant);
			assertFalse(_message.getSender().isFrom(getKernelAddress()));
			Assert.assertTrue(checkAgentAddress(_message.getSender()));
			return true;
		} catch (Throwable e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean testTraveledAgentAddress(AgentAddress aa, boolean returnToSender, Group group, String role) {
		try {
			Assert.assertTrue(checkAgentAddress(aa));
			if (returnToSender) {
				Assert.assertEquals(getAgentAddressIn(group, role), aa);
				assertTrue(aa.isFrom(getKernelAddress()));
			} else {
				AgentAddress distant = null;
				for (AgentAddress aa2 : getAgentsWithRole(group, role)) {
					if (aa2.equals(aa))
						distant = aa;
				}
				Assert.assertNotNull(distant);
				assertFalse(aa.isFrom(getKernelAddress()));
			}
			return true;
		} catch (Throwable e) {
			e.printStackTrace();
			return false;
		}

	}
}
