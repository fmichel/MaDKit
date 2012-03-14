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
import static org.junit.Assert.assertTrue;
import madkit.kernel.Madkit.BooleanOption;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.9
 * @version 0.9
 * 
 */
public class AgentAddressTest {

	static Role r;
	static AbstractAgent a;
	static MadkitKernel mk;
	static AgentAddress aa;

	@Before
	public void before() {
		String[] args = { BooleanOption.desktop.toString(), "false" };
		mk = new MadkitKernel(new Madkit(args));
		a = new AbstractAgent();
		a.setKernel(mk);
		mk.createGroup(a, "c", "g", null, null, false);
		try {
			mk.getGroup("c", "g").put("r", mk.getGroup("c", "g").createRole("r"));
			r = mk.getRole("c", "g", "r");
		} catch (CGRNotAvailable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertTrue(r.addMember(a));
		aa = r.getAgentAddressOf(a);
		assertNotNull(aa);
	}

	/**
	 * Test method for
	 * {@link madkit.kernel.AgentAddress#AgentAddress(madkit.kernel.AbstractAgent, madkit.kernel.Role, madkit.kernel.KernelAddress)}
	 * .
	 */
	@Test
	public void testAgentAddress() {
		assertTrue(aa.exists());
		assertTrue(aa.isLocal());
	}

	/**
	 * Test method for {@link madkit.kernel.AgentAddress#getAgent()}.
	 */
	@Test
	public void testGetAgent() {
		assertEquals(a, aa.getAgent());
	}

	/**
	 * Test method for
	 * {@link madkit.kernel.AgentAddress#setRoleObject(madkit.kernel.Role)}.
	 */
	@Test
	public void testCGRNames() {
		assertEquals("c", aa.getCommunity());
		assertEquals("g", aa.getGroup());
		assertEquals("r", aa.getRole());
		assertEquals(AbstractAgent.ReturnCode.SUCCESS, r.removeMember(a));
		assertFalse(aa.exists());
		assertTrue(aa.isLocal());
		assertEquals("c", aa.getCommunity());
		assertEquals("g", aa.getGroup());
		assertEquals("r", aa.getRole());
	}

	/**
	 * Test method for {@link madkit.kernel.AgentAddress#getRoleObject()}.
	 */
	@Test
	public void testGetRoleObject() {
		assertEquals(r, aa.getRoleObject());
	}

	/**
	 * Test method for {@link madkit.kernel.AgentAddress#getKernelAddress()}.
	 */
	@Test
	public void testGetKernelAddress() {
		assertEquals(aa.getKernelAddress(), mk.getKernelAddress());
	}

	/**
	 * Test method for {@link madkit.kernel.AgentAddress#toString()}.
	 */
	@Test
	public void testToString() {
		System.err.println(aa);
		assertTrue(aa.toString().contains("@(c,g,r)"));
	}

	/**
	 * Test method for {@link madkit.kernel.AgentAddress#getAgentCode()}.
	 */
	@Test
	public void testHashCode() {
		assertEquals(a.hashCode(), aa.hashCode());
	}

	/**
	 * Test method for
	 * {@link madkit.kernel.AgentAddress#equals(java.lang.Object)}.
	 */
	@Test
	public void testEqualsObject() {
		AgentAddress other = new AgentAddress(new AbstractAgent(), r, r.getKernelAddress());
		assertFalse(other.equals(aa));
		other = new AgentAddress(a, r, r.getKernelAddress());
		System.err.println(aa.toString()+aa.getKernelAddress());
		System.err.println(other.toString()+other.getKernelAddress());
		assertTrue(other.equals(aa));
	}

	/**
	 * Test method for {@link madkit.kernel.AgentAddress#exists()}.
	 */
	@Test
	public void testExists() {
		assertTrue(aa.exists());
		assertEquals(AbstractAgent.ReturnCode.SUCCESS, r.removeMember(a));
		assertFalse(aa.exists());
	}

	/**
	 * Test method for {@link madkit.kernel.AgentAddress#isLocal()}.
	 */
	@Test
	public void testIsLocal() {
		// AgentAddress other = new AgentAddress(new AbstractAgent(), r,
		// r.getKernelAddress());
		assertTrue(aa.isLocal());
		assertEquals(AbstractAgent.ReturnCode.SUCCESS, r.removeMember(a));
		assertTrue(aa.isLocal());
	}

}
