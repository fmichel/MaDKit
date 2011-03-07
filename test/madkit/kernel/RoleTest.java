/**
 * 
 */
package madkit.kernel;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author fab
 *
 */
public class RoleTest {

	/**
	 * Test method for {@link madkit.kernel.Role#getMyGroup()}.
	 */
	@Test
	public final void testGetMyGroup() {
		Role r = new Role("test", "test");
		r.getMyGroup();
	}

	/**
	 * Test method for {@link madkit.kernel.Role#getPlayers()}.
	 */
	@Test
	public final void testGetPlayers() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link madkit.kernel.Role#getCommunityName()}.
	 */
	@Test
	public final void testGetCommunityName() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link madkit.kernel.Role#addMember(madkit.kernel.AbstractAgent)}.
	 */
	@Test
	public final void testAddMember() {
		fail("Not yet implemented"); // TODO
	}

}
