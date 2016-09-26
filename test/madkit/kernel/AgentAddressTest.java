/*
 * Copyright or © or Copr. Fabien Michel, Olivier Gutknecht, Jacques Ferber (1997)

fmichel@lirmm.fr
olg@no-distance.net
ferber@lirmm.fr

This software is a computer program whose purpose is to 
provide a lightweight Java library for designing and simulating Multi-Agent Systems (MAS).

This software is governed by the CeCILL-C license under French law and
abiding by the rules of distribution of free software.  You can  use, 
modify and/ or redistribute the software under the terms of the CeCILL-C
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info". 

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability. 

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or 
data to be ensured and,  more generally, to use and operate it in the 
same conditions as regards security. 

The fact that you are presently reading this means that you have had
knowledge of the CeCILL-C license and that you accept its terms.
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
 * @since MaDKit 5.0.0.9
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
		mk = new MadkitKernel(new Madkit(BooleanOption.desktop.toString(), "false"));
		a = new AbstractAgent();
		a.setKernel(mk);
		mk.createGroup(a, "c", "g", null, false);
		try {
			mk.getGroup("c", "g").put("r", mk.getGroup("c", "g").createRole("r"));
			r = mk.getRole("c", "g", "r");
		} catch (CGRNotAvailable e) {
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
		assertTrue(a.checkAgentAddress(aa));
		assertTrue(aa.isFrom(a.getKernelAddress()));
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
		assertFalse(a.checkAgentAddress(aa));
		assertTrue(aa.isFrom(a.getKernelAddress()));
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
		assertTrue(a.checkAgentAddress(aa));
		assertEquals(AbstractAgent.ReturnCode.SUCCESS, r.removeMember(a));
		assertFalse(a.checkAgentAddress(aa));
	}

	/**
	 * Test method for {@link madkit.kernel.AgentAddress#isFrom()}.
	 */
	@Test
	public void testIsLocal() {
		// AgentAddress other = new AgentAddress(new AbstractAgent(), r,
		// r.getKernelAddress());
		assertTrue(aa.isFrom(a.getKernelAddress()));
		assertEquals(AbstractAgent.ReturnCode.SUCCESS, r.removeMember(a));
		assertTrue(aa.isFrom(a.getKernelAddress()));
	}

}
