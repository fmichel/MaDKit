/*
 * Copyright 1997-2011 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MadKit.
 * 
 * MadKit is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * MadKit is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with MadKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.kernel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import madkit.kernel.Madkit.BooleanOption;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.7
 * @version 0.9
 * 
 */
public class RoleTest {
	
	static Role r;
	static AbstractAgent a;
	static MadkitKernel mk;
	
	@Before
	public void before(){
		String[] args = {"--"+BooleanOption.desktop,"false",
				"--platformLogLevel","ALL"
				};
		mk = new MadkitKernel(new Madkit(args));
		a = new AbstractAgent();
		a.setKernel(mk);
		mk.createGroup(a, "c", "g", null, null, false);
		try {
			mk.getGroup("c", "g").put("r",mk.getGroup("c", "g").createRole("r"));
			r = mk.getRole("c", "g", "r");
		} catch (CGRNotAvailable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Test method for {@link madkit.kernel.Role#getMyGroup()}.
	 */
	@Test
	public final void testGetMyGroup() {
		r.getMyGroup();
	}

	/**
	 * Test method for {@link madkit.kernel.Role#getPlayers()}.
	 */
	@Test
	public final void testGetPlayers() {
		assertEquals(0, r.getPlayers().size());
		assertTrue(r.addMember(a));
		assertEquals(1, r.getPlayers().size());
		assertEquals(AbstractAgent.ReturnCode.SUCCESS, r.removeMember(a));
		assertEquals(0, r.getPlayers().size());
	}

	/**
	 * Test method for {@link madkit.kernel.Role#getCommunityName()}.
	 */
	@Test
	public final void testGetAgentAddressOf() {
		assertTrue(r.addMember(a));
		AgentAddress aa = r.getAgentAddressOf(a);
		assertNotNull(aa);
		assertEquals(AbstractAgent.ReturnCode.SUCCESS, r.removeMember(a));
		assertNull(r.getAgentAddressOf(a));
		assertFalse(aa.exists());
	}

	/**
	 * Test method for {@link madkit.kernel.Role#addMember(madkit.kernel.AbstractAgent)}.
	 */
	@Test
	public final void testAddMember() {
		assertTrue(r.addMember(a));
		assertFalse(r.addMember(a));
	}
	
	/**
	 * Test method for {@link madkit.kernel.Role#removeMember(madkit.kernel.AbstractAgent)}.
	 */
	@Test
	public final void testRemoveMember() {
		assertTrue(mk.isGroup(null,"c", "g"));
		assertTrue(r.addMember(a));
		assertEquals(AbstractAgent.ReturnCode.SUCCESS, r.removeMember(a));
		assertEquals(AbstractAgent.ReturnCode.SUCCESS, a.leaveGroup("c", "g"));
		assertNull(r.getAgentAddressOf(a));
		assertFalse(mk.isGroup(null,"c", "g"));
	}
	
	@Test
	public final void testGetAgentAddressInGroup() {
		assertTrue(r.addMember(a));
		assertNotNull(r.getAgentAddressInGroup(a));
		assertEquals(AbstractAgent.ReturnCode.SUCCESS, r.removeMember(a));
		assertEquals(Madkit.Roles.GROUP_MANAGER_ROLE, r.getAgentAddressInGroup(a).getRole());
		assertEquals(AbstractAgent.ReturnCode.SUCCESS, a.leaveGroup("c", "g"));
		assertNull(r.getAgentAddressOf(a));
	}

	@Test
	public final void testGetAgentAddresses() {
		assertTrue(r.addMember(a));
		AbstractAgent b = new AbstractAgent();
		b.setKernel(mk);
		assertTrue(r.addMember(b));
		assertEquals(2, r.buildAndGetAddresses().size());
		assertEquals(AbstractAgent.ReturnCode.SUCCESS, a.leaveGroup("c", "g"));
		assertNull(r.getAgentAddressOf(a));
		assertEquals(1, r.buildAndGetAddresses().size());
		assertEquals(AbstractAgent.ReturnCode.SUCCESS, b.leaveGroup("c", "g"));
		assertEquals(0, r.buildAndGetAddresses().size());
		assertNull(r.getAgentAddressOf(b));
	}
	
	@Test
	public void testCGRNames(){
		assertEquals("c", r.getCommunityName());
		assertEquals("g", r.getGroupName());
		assertEquals("r", r.getRoleName());
	}

}
