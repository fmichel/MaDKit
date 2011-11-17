package madkit.kernel;

import static org.junit.Assert.fail;

import org.junit.BeforeClass;
import org.junit.Test;

public class RootKernelTest {

	static FakeKernel fakeKernel;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
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
	public void testReloadClass() {
		try {
			fakeKernel.reloadClass(null, null);
			fail("kernel exepction not thrown");
		} catch (KernelException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
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
			fakeKernel.isGroup(null, null, null);
			fail("kernel exepction not thrown");
		} catch (KernelException e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testIsRoleAbstractAgentStringStringString() {
		try {
			fakeKernel.isRole(null, null, null, null);
			fail("kernel exepction not thrown");
		} catch (KernelException e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testCreateGroupIfAbsentAbstractAgentStringStringStringGroupIdentifierBoolean() {
		try {
			fakeKernel.createGroupIfAbsent(null, null, null, null, null, false);
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
