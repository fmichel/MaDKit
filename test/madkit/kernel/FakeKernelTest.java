package madkit.kernel;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class FakeKernelTest {

	AbstractAgent a;

	@Before
	public void setUp() throws Exception {
		a = new AbstractAgent();
	}

	@Test
	public final void testGetMadkitConfig() {
		assertNotNull(a.getMadkitConfig());
		assertNotNull(a.getMadkitProperty("madkit.version"));
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
			a.getAgentAddressIn("t","t","t");
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
			a.createGroup("t", "t", false, null);
			fail("exception not thrown");
		} catch (KernelException e) {
			assertEquals(a.getKernel(), AbstractAgent.FAKE_KERNEL);

		}

	}

	@Test
	public final void testRequestRoleAbstractAgentStringStringStringObject() {
		try {
			a.requestRole("t", "t", "t", null);
			fail("exception not thrown");
		} catch (KernelException e) {
			assertEquals(a.getKernel(), AbstractAgent.FAKE_KERNEL);

			assertEquals(a.getKernel(), AbstractAgent.FAKE_KERNEL);
		}

	}

	@Test
	public final void testLeaveGroupAbstractAgentStringString() {
		try {
			a.leaveGroup("t", "t");
			fail("exception not thrown");
		} catch (KernelException e) {
			assertEquals(a.getKernel(), AbstractAgent.FAKE_KERNEL);

		}

	}

	@Test
	public final void testLeaveRoleAbstractAgentStringStringString() {
		try {
			a.leaveRole("t", "t", "t");
			fail("exception not thrown");
		} catch (KernelException e) {
			assertEquals(a.getKernel(), AbstractAgent.FAKE_KERNEL);

		}

	}

	@Test
	public final void testGetAgentsWithRoleAbstractAgentStringStringStringBoolean() {
		try {
			a.getAgentsWithRole("t", "t", "t");
			fail("exception not thrown");
		} catch (KernelException e) {
			assertEquals(a.getKernel(), AbstractAgent.FAKE_KERNEL);

		}

	}

	@Test
	public final void testGetAgentWithRoleAbstractAgentStringStringString() {
		try {
			a.getAgentWithRole("t", "t", "t");
			fail("exception not thrown");
		} catch (KernelException e) {
			assertEquals(a.getKernel(), AbstractAgent.FAKE_KERNEL);

		}

	}

	@Test
	public final void testSendMessageAbstractAgentStringStringStringMessageString() {
		try {
			a.sendMessage("t", "t", "t", new Message());
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
			a.broadcastMessage("t", "t", "t", new Message());
			fail("exception not thrown");
		} catch (KernelException e) {
			assertEquals(a.getKernel(), AbstractAgent.FAKE_KERNEL);

		}

	}

	@Test
	public final void testBroadcastMessageWithRoleAndWaitForRepliesAbstractAgentStringStringStringMessageStringInteger() {
		try {
			a.broadcastMessageWithRole("t", "t", "t", new Message(), null);
			fail("exception not thrown");
		} catch (KernelException e) {
			assertEquals(a.getKernel(), AbstractAgent.FAKE_KERNEL);

		}

	}

	@Test
	public final void testLaunchAgentBucketWithRolesAbstractAgentStringIntCollectionOfString() {
			assertNull(a.launchAgentBucket("t", 20));
	}

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
			a.isGroup("t", "t");
			fail("exception not thrown");
		} catch (KernelException e) {
			assertEquals(a.getKernel(), AbstractAgent.FAKE_KERNEL);

		}

	}

	@Test
	public final void testIsRoleAbstractAgentStringStringString() {
		try {
			a.isRole("t", "t", "t");
			fail("exception not thrown");
		} catch (KernelException e) {
			assertEquals(a.getKernel(), AbstractAgent.FAKE_KERNEL);

		}

	}

	@Test
	public final void testCreateGroupIfAbsentAbstractAgentStringStringStringGatekeeperBoolean() {
		try {
			a.createGroupIfAbsent("t", "t", true, null);
			fail("exception not thrown");
		} catch (KernelException e) {
			assertEquals(a.getKernel(), AbstractAgent.FAKE_KERNEL);

		}

	}

}
