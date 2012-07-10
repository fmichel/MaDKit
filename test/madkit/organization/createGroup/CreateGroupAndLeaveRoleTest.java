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
package madkit.organization.createGroup;

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
 * @since MadKit 5.0.0.10
 * @version 0.9
 * 
 */
@SuppressWarnings("serial")
public class CreateGroupAndLeaveRoleTest extends JunitMadkit {

	@Test
	public void testCreateGroupAndLeaveRole() {
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

				assertEquals(SUCCESS, leaveRole(COMMUNITY, GROUP, Organization.GROUP_MANAGER_ROLE));

				assertFalse(isRole(COMMUNITY, GROUP, Organization.GROUP_MANAGER_ROLE));
				assertFalse(isGroup(COMMUNITY, GROUP));
				assertFalse(isCommunity(COMMUNITY));

				// Rerun to test with another agent inside the group

				AbstractAgent testAgent = new AbstractAgent();
				launchAgent(testAgent);

				assertEquals(SUCCESS, createGroup(COMMUNITY, GROUP, true, null));
				assertEquals(ROLE_ALREADY_HANDLED, testAgent.requestRole(COMMUNITY, GROUP, Organization.GROUP_MANAGER_ROLE));
				assertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
				assertEquals(SUCCESS, testAgent.requestRole(COMMUNITY, GROUP, ROLE));
				assertEquals(SUCCESS, leaveRole(COMMUNITY, GROUP, Organization.GROUP_MANAGER_ROLE));
				assertTrue(isCommunity(COMMUNITY));
				assertTrue(isGroup(COMMUNITY, GROUP));
			}
		});
	}
}
