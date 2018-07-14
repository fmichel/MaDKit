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

import static org.junit.Assert.fail;

import org.junit.BeforeClass;
import org.junit.Test;

import com.distrimind.madkit.kernel.FakeKernel;
import com.distrimind.madkit.kernel.KernelException;

/**
 * @author Fabien Michel
 * @author Jason Mahdjoub
 * @since MadkitLanEdition 1.0
 * @version 1.0
 * 
 */
public class RootKernelTest {

	static FakeKernel fakeKernel;

	@BeforeClass
	public static void setUpBeforeClass() {
		fakeKernel = new FakeKernel();
	}

	@Test
	public void testGetKernelAddress() {
		try {
			fakeKernel.getKernelAddress();
			fail("kernel exepction not thrown");
		} catch (KernelException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testIsKernelConnected() {
		try {
			fakeKernel.getKernelAddress();
			fail("kernel exepction not thrown");
		} catch (KernelException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testCreateGroupAbstractAgentStringStringStringGroupIdentifierBoolean() {
		try {
			fakeKernel.getKernelAddress();
			fail("kernel exepction not thrown");
		} catch (KernelException e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testRequestRoleAbstractAgentStringStringStringObject() {
		try {
			fakeKernel.getKernelAddress();
			fail("kernel exepction not thrown");
		} catch (KernelException e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testLeaveGroupAbstractAgentStringString() {
		try {
			fakeKernel.getKernelAddress();
			fail("kernel exepction not thrown");
		} catch (KernelException e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testLeaveRoleAbstractAgentStringStringString() {
		try {
			fakeKernel.getKernelAddress();
			fail("kernel exepction not thrown");
		} catch (KernelException e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testGetAgentsWithRoleAbstractAgentStringStringString() {
		try {
			fakeKernel.getKernelAddress();
			fail("kernel exepction not thrown");
		} catch (KernelException e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testGetAgentWithRoleAbstractAgentStringStringString() {
		try {
			fakeKernel.getKernelAddress();
			fail("kernel exepction not thrown");
		} catch (KernelException e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testSendMessageAbstractAgentStringStringStringMessageString() {
		try {
			fakeKernel.getKernelAddress();
			fail("kernel exepction not thrown");
		} catch (KernelException e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testSendMessageAbstractAgentAgentAddressMessageString() {
		try {
			fakeKernel.getKernelAddress();
			fail("kernel exepction not thrown");
		} catch (KernelException e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testSendReplyWithRoleAbstractAgentMessageMessageString() {
		try {
			fakeKernel.getKernelAddress();
			fail("kernel exepction not thrown");
		} catch (KernelException e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testBroadcastMessageWithRoleAbstractAgentStringStringStringMessageString() {
		try {
			fakeKernel.getKernelAddress();
			fail("kernel exepction not thrown");
		} catch (KernelException e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testBroadcastMessageWithRoleAndWaitForRepliesAbstractAgentStringStringStringMessageStringInteger() {
		try {
			fakeKernel.getKernelAddress();
			fail("kernel exepction not thrown");
		} catch (KernelException e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testLaunchAgentBucketWithRolesAbstractAgentStringIntCollectionOfString() {
		try {
			fakeKernel.getKernelAddress();
			fail("kernel exepction not thrown");
		} catch (KernelException e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testLaunchAgentAbstractAgentStringIntBoolean() {
		try {
			fakeKernel.getKernelAddress();
			fail("kernel exepction not thrown");
		} catch (KernelException e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testLaunchAgentAbstractAgentAbstractAgentIntBoolean() {
		try {
			fakeKernel.getKernelAddress();
			fail("kernel exepction not thrown");
		} catch (KernelException e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testKillAgentAbstractAgentAbstractAgentInt() {
		try {
			fakeKernel.getKernelAddress();
			fail("kernel exepction not thrown");
		} catch (KernelException e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testAddOverlooker() {
		try {
			fakeKernel.getKernelAddress();
			fail("kernel exepction not thrown");
		} catch (KernelException e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testRemoveOverlooker() {
		try {
			fakeKernel.getKernelAddress();
			fail("kernel exepction not thrown");
		} catch (KernelException e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testGetNewestClassVersionAbstractAgentString() {
		try {
			fakeKernel.getKernelAddress();
			fail("kernel exepction not thrown");
		} catch (KernelException e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testGetMadkitPropertyAbstractAgentString() {
		try {
			fakeKernel.getKernelAddress();
			fail("kernel exepction not thrown");
		} catch (KernelException e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testSetMadkitPropertyAbstractAgentStringString() {
		try {
			fakeKernel.getKernelAddress();
			fail("kernel exepction not thrown");
		} catch (KernelException e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testIsCommunityAbstractAgentString() {
		try {
			fakeKernel.isCommunity(null, null);
			fail("kernel exepction not thrown");
		} catch (KernelException e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testIsGroupAbstractAgentStringString() {
		try {
			fakeKernel.isGroup(null, null);
			fail("kernel exepction not thrown");
		} catch (KernelException e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testIsRoleAbstractAgentStringStringString() {
		try {
			fakeKernel.isRole(null, null, null);
			fail("kernel exepction not thrown");
		} catch (KernelException e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testCreateGroupIfAbsentAbstractAgentStringStringStringGroupIdentifierBoolean() {
		try {
			fakeKernel.createGroup(null, null, null, false);
			fail("kernel exepction not thrown");
		} catch (KernelException e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testRootKernel() {
		try {
			fakeKernel.getKernelAddress();
			fail("kernel exepction not thrown");
		} catch (KernelException e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testFakeKernelWarningAbstractAgent() {
		try {
			fakeKernel.getKernelAddress();
			fail("kernel exepction not thrown");
		} catch (KernelException e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testFakeKernelWarning() {
		try {
			fakeKernel.getKernelAddress();
			fail("kernel exepction not thrown");
		} catch (KernelException e) {
			e.printStackTrace();
		}

	}

}
