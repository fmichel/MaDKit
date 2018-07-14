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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import com.distrimind.madkit.kernel.AbstractAgent;
import com.distrimind.madkit.kernel.KernelException;
import com.distrimind.madkit.kernel.Message;
import com.distrimind.madkit.kernel.Scheduler;

/**
 * @author Fabien Michel
 * @author Jason Mahdjoub
 * @since MadkitLanEdition 1.0
 * @version 1.0
 * 
 * 
 */
public class FakeKernelTest {

	AbstractAgent a;

	@Before
	public void setUp() {
		a = new AbstractAgent();
	}

	@Test
	public final void testGetMadkitConfig() {
		assertNotNull(a.getMadkitConfig());
		assertNotNull(a.getMadkitConfig().madkitVersion);
		assertEquals(a.getKernel(), AbstractAgent.FAKE_KERNEL);
	}

	@Test
	public final void testGetKernelAddress() {
		try {
			a.getKernelAddress();
			fail("exception not thrown");
		} catch (KernelException e) {
			assertEquals(a.getKernel(), AbstractAgent.FAKE_KERNEL);

		}

	}

	@Test
	public final void testGetAgentAddressIn() {
		try {
			a.getAgentAddressIn(new Group("t", "t"), "t");
			fail("exception not thrown");
		} catch (KernelException e) {
			assertEquals(a.getKernel(), AbstractAgent.FAKE_KERNEL);

		}

	}

	@Test
	public final void testIsKernelOnline() {
		try {
			a.isKernelOnline();
			fail("exception not thrown");
		} catch (KernelException e) {
			assertEquals(a.getKernel(), AbstractAgent.FAKE_KERNEL);

		}

	}

	@Test
	public final void testCreateGroupAbstractAgentStringStringStringGatekeeperBoolean() {
		try {
			a.createGroup(new Group("t", "t"), null);
			fail("exception not thrown");
		} catch (KernelException e) {
			assertEquals(a.getKernel(), AbstractAgent.FAKE_KERNEL);

		}

	}

	@Test
	public final void testRequestRoleAbstractAgentStringStringStringObject() {
		try {
			a.requestRole(new Group("t", "t"), "t", null);
			fail("exception not thrown");
		} catch (KernelException e) {
			assertEquals(a.getKernel(), AbstractAgent.FAKE_KERNEL);

			assertEquals(a.getKernel(), AbstractAgent.FAKE_KERNEL);
		}

	}

	@Test
	public final void testLeaveGroupAbstractAgentStringString() {
		try {
			a.leaveGroup(new Group("t", "t"));
			fail("exception not thrown");
		} catch (KernelException e) {
			assertEquals(a.getKernel(), AbstractAgent.FAKE_KERNEL);

		}

	}

	@Test
	public final void testLeaveRoleAbstractAgentStringStringString() {
		try {
			a.leaveRole(new Group("t", "t"), "t");
			fail("exception not thrown");
		} catch (KernelException e) {
			assertEquals(a.getKernel(), AbstractAgent.FAKE_KERNEL);

		}

	}

	@Test
	public final void testGetAgentsWithRoleAbstractAgentStringStringStringBoolean() {
		try {
			a.getAgentsWithRole(new Group("t", "t"), "t");
			fail("exception not thrown");
		} catch (KernelException e) {
			assertEquals(a.getKernel(), AbstractAgent.FAKE_KERNEL);

		}

	}

	@Test
	public final void testGetAgentWithRoleAbstractAgentStringStringString() {
		try {
			a.getAgentWithRole(new Group("t", "t"), "t");
			fail("exception not thrown");
		} catch (KernelException e) {
			assertEquals(a.getKernel(), AbstractAgent.FAKE_KERNEL);

		}

	}

	@Test
	public final void testSendMessageAbstractAgentStringStringStringMessageString() {
		try {
			a.sendMessage(new Group("t", "t"), "t", new Message());
			fail("exception not thrown");
		} catch (KernelException e) {
			assertEquals(a.getKernel(), AbstractAgent.FAKE_KERNEL);

		}

	}

	@Test
	public final void testSendMessageAbstractAgentAgentAddressMessageString() {
		try {
			a.sendMessage(null, new Message());
			fail("exception not thrown");
		} catch (KernelException e) {
			assertEquals(a.getKernel(), AbstractAgent.FAKE_KERNEL);

		}

	}

	@Test
	public final void testBroadcastMessageWithRoleAbstractAgentStringStringStringMessageString() {
		try {
			a.broadcastMessage(new Group("t", "t"), "t", new Message());
			fail("exception not thrown");
		} catch (KernelException e) {
			assertEquals(a.getKernel(), AbstractAgent.FAKE_KERNEL);

		}

	}

	@Test
	public final void testBroadcastMessageWithRoleAndWaitForRepliesAbstractAgentStringStringStringMessageStringInteger() {
		try {
			a.broadcastMessageWithRole(new Group("t", "t"), "t", new Message(), null);
			fail("exception not thrown");
		} catch (KernelException e) {
			assertEquals(a.getKernel(), AbstractAgent.FAKE_KERNEL);

		}

	}

	/*
	 * @Test public final void
	 * testLaunchAgentBucketWithRolesAbstractAgentStringIntCollectionOfString() {
	 * 
	 * assertNull(a.launchAgentBucket("t", 20)); }
	 */

	@Test
	public final void testLaunchAgentBucketNoKernel() {
		try {
			a.launchAgentBucket(AbstractAgent.class.getName(), 20);
			fail("exception not thrown");
		} catch (KernelException e) {
			assertEquals(a.getKernel(), AbstractAgent.FAKE_KERNEL);
		}
	}

	@Test
	public final void testLaunchAgentAbstractAgentAbstractAgentIntBoolean() {
		try {
			a.launchAgent(new AbstractAgent(), true);
			fail("exception not thrown");
		} catch (KernelException e) {
			assertEquals(a.getKernel(), AbstractAgent.FAKE_KERNEL);

		}

	}

	@Test
	public final void testKillAgentAbstractAgentAbstractAgentInt() {
		try {
			a.killAgent(new AbstractAgent(), 1);
			fail("exception not thrown");
		} catch (KernelException e) {
			assertEquals(a.getKernel(), AbstractAgent.FAKE_KERNEL);

		}

	}

	@Test
	public final void testAddOverlooker() {
		try {
			new Scheduler().addActivator(null);
			fail("exception not thrown");
		} catch (KernelException e) {
			assertEquals(a.getKernel(), AbstractAgent.FAKE_KERNEL);

		}

	}

	@Test
	public final void testRemoveOverlooker() {
		try {
			new Scheduler().removeActivator(null);
			fail("exception not thrown");
		} catch (KernelException e) {
			assertEquals(a.getKernel(), AbstractAgent.FAKE_KERNEL);

		}

	}

	@Test
	public final void testIsCommunityAbstractAgentString() {
		try {
			a.isCommunity("t");
			fail("exception not thrown");
		} catch (KernelException e) {
			assertEquals(a.getKernel(), AbstractAgent.FAKE_KERNEL);

		}

	}

	@Test
	public final void testIsGroupAbstractAgentStringString() {
		try {
			a.isCreatedGroup(new Group("t", "t"));
			fail("exception not thrown");
		} catch (KernelException e) {
			assertEquals(a.getKernel(), AbstractAgent.FAKE_KERNEL);

		}

	}

	@Test
	public final void testIsRoleAbstractAgentStringStringString() {
		try {
			a.isCreatedRole(new Group("t", "t"), "t");
			fail("exception not thrown");
		} catch (KernelException e) {
			assertEquals(a.getKernel(), AbstractAgent.FAKE_KERNEL);

		}

	}

	@Test
	public final void testCreateGroupIfAbsentAbstractAgentStringStringStringGatekeeperBoolean() {
		try {
			a.createGroup(new Group("t", "t"), null);
			fail("exception not thrown");
		} catch (KernelException e) {
			assertEquals(a.getKernel(), AbstractAgent.FAKE_KERNEL);

		}

	}

}
