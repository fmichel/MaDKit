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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import madkit.agr.Organization;
import madkit.kernel.Madkit.BooleanOption;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.7
 * @version 0.9
 * 
 */
public class RoleTest {

	static Role r;
	static AbstractAgent a;
	static MadkitKernel mk;

	@Before
	public void before() {
		String[] args = { "--" + BooleanOption.desktop, "false", "--platformLogLevel", "ALL" };
		mk = new MadkitKernel(new Madkit(args));
		a = new AbstractAgent();
		a.setKernel(mk);
		mk.createGroup(a, "c", "g", null, false);
		try {
			mk.getGroup("c", "g").put("r", mk.getGroup("c", "g").createRole("r"));
			r = mk.getRole("c", "g", "r");
		} catch (CGRNotAvailable e) {
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
		assertFalse(a.checkAgentAddress(aa));
	}

	/**
	 * Test method for
	 * {@link madkit.kernel.Role#addMember(madkit.kernel.AbstractAgent)}.
	 */
	@Test
	public final void testAddMember() {
		assertTrue(r.addMember(a));
		assertFalse(r.addMember(a));
	}

	/**
	 * Test method for
	 * {@link madkit.kernel.Role#removeMember(madkit.kernel.AbstractAgent)}.
	 */
	@Test
	public final void testRemoveMember() {
		assertTrue(mk.isGroup(null, "c", "g"));
		assertTrue(r.addMember(a));
		assertEquals(AbstractAgent.ReturnCode.SUCCESS, r.removeMember(a));
		assertEquals(AbstractAgent.ReturnCode.SUCCESS, a.leaveGroup("c", "g"));
		assertNull(r.getAgentAddressOf(a));
		assertFalse(mk.isGroup(null, "c", "g"));
	}

	@Test
	public final void testGetAgentAddressInGroup() {
		assertTrue(r.addMember(a));
		assertNotNull(r.getAgentAddressInGroup(a));
		assertEquals(AbstractAgent.ReturnCode.SUCCESS, r.removeMember(a));
		assertEquals(Organization.GROUP_MANAGER_ROLE, r.getAgentAddressInGroup(a).getRole());
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
	public void testCGRNames() {
		assertEquals("c", r.getCommunityName());
		assertEquals("g", r.getGroupName());
		assertEquals("r", r.getRoleName());
	}

}
