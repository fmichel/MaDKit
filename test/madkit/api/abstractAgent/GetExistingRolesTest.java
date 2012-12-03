/*
 * Copyright 1997-2012 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MaDKit.
 * 
 * MaDKit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MaDKit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MaDKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.api.abstractAgent;

import static madkit.kernel.AbstractAgent.ReturnCode.ALREADY_GROUP;
import static madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;
import madkit.agr.LocalCommunity;
import madkit.agr.Organization;
import madkit.kernel.AbstractAgent;
import madkit.kernel.AbstractAgent.ReturnCode;
import madkit.kernel.Gatekeeper;
import madkit.kernel.JunitMadkit;

import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.20
 * @version 0.9
 * 
 */
@SuppressWarnings("all")
public class GetExistingRolesTest extends JunitMadkit {

	@Test
	public void onlyLocal() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				System.err.println(getExistingRoles(LocalCommunity.NAME,LocalCommunity.Groups.SYSTEM));
				assertEquals(1, getExistingRoles(LocalCommunity.NAME,LocalCommunity.Groups.SYSTEM).size());
				assertEquals(Organization.GROUP_MANAGER_ROLE, getExistingRoles(LocalCommunity.NAME,LocalCommunity.Groups.SYSTEM).first());
			}
		});
	}
	
	@Test
	public void notFound(){
		launchTest(new AbstractAgent() {
			protected void activate() {
				assertNull(getExistingRoles(aa(),aa()));
			}
		});
	}

	@Test
	public void createNewAndLeave() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				createGroup(COMMUNITY, GROUP);
				assertEquals(1, getExistingRoles(COMMUNITY,GROUP).size());
				assertEquals(Organization.GROUP_MANAGER_ROLE, getExistingRoles(COMMUNITY,GROUP).first());
				requestRole(COMMUNITY, GROUP, ROLE);
				assertEquals(2, getExistingRoles(COMMUNITY,GROUP).size());
				assertEquals(ROLE, getExistingRoles(COMMUNITY,GROUP).first());
			}
		});
	}

}
