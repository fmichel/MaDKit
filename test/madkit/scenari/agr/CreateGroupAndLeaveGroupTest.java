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
package madkit.scenari.agr;

import static madkit.kernel.AbstractAgent.ReturnCode.ALREADY_GROUP;
import static madkit.kernel.AbstractAgent.ReturnCode.NOT_COMMUNITY;
import static madkit.kernel.AbstractAgent.ReturnCode.NOT_GROUP;
import static madkit.kernel.AbstractAgent.ReturnCode.ROLE_ALREADY_HANDLED;
import static madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import madkit.agr.Organization;
import madkit.kernel.AbstractAgent;
import madkit.kernel.JunitMadkit;

import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.10
 * @version 0.9
 * 
 */

public class CreateGroupAndLeaveGroupTest extends JunitMadkit {

	@Test
	public void testCreateGroup() {
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				assertEquals(SUCCESS, createGroup(COMMUNITY, GROUP, true, null));
				assertEquals(ALREADY_GROUP, createGroup(COMMUNITY, GROUP, true, null));
				assertEquals(false, createGroupIfAbsent(COMMUNITY, GROUP, true, null));

				assertTrue(isGroup(COMMUNITY, GROUP));
				assertTrue(isRole(COMMUNITY, GROUP, Organization.GROUP_MANAGER_ROLE));
				assertTrue(isCommunity(COMMUNITY));

				assertEquals(NOT_COMMUNITY, leaveGroup(COMMUNITY + "d", GROUP));
				assertEquals(NOT_GROUP, leaveGroup(COMMUNITY, GROUP + "d"));

				// Manager role cannot be requested
				assertEquals(ROLE_ALREADY_HANDLED, requestRole(COMMUNITY, GROUP, Organization.GROUP_MANAGER_ROLE));
				assertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));

				assertEquals(SUCCESS, leaveGroup(COMMUNITY, GROUP));
				assertFalse(isCommunity(COMMUNITY));
				assertFalse(isGroup(COMMUNITY, GROUP));
				assertFalse(isRole(COMMUNITY, GROUP, Organization.GROUP_MANAGER_ROLE));

				// Rerun to test with another agent inside the group
				AbstractAgent testAgent = new AbstractAgent();
				launchAgent(testAgent);
				assertEquals(SUCCESS, createGroup(COMMUNITY, GROUP, true, null));
				assertEquals(ROLE_ALREADY_HANDLED, testAgent.requestRole(COMMUNITY, GROUP, Organization.GROUP_MANAGER_ROLE));
				assertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
				assertEquals(SUCCESS, testAgent.requestRole(COMMUNITY, GROUP, ROLE));
				assertEquals(SUCCESS, leaveGroup(COMMUNITY, GROUP));
				assertTrue(isCommunity(COMMUNITY));
				assertTrue(isGroup(COMMUNITY, GROUP));
				// a new agent should have been selected
				// assertTrue(isRole(COMMUNITY, GROUP,Madkit.GROUP_MANAGER_ROLE));
			}
		});
	}

}
