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

import static org.junit.Assert.*;
import static org.junit.Assert.assertNull;
import madkit.agr.LocalCommunity;
import madkit.agr.Organization;
import madkit.kernel.AbstractAgent;
import madkit.kernel.JunitMadkit;

import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.3
 * @version 0.9
 * 
 */
@SuppressWarnings("all")
public class HasRoleTest extends JunitMadkit {

	@Test
	public void hasRoleTest() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				assertFalse(hasRole(COMMUNITY, GROUP, ROLE));
				assertEquals(ReturnCode.SUCCESS, createGroup(COMMUNITY, GROUP));
				assertTrue(hasRole(COMMUNITY, GROUP, Organization.GROUP_MANAGER_ROLE));
				assertEquals(ReturnCode.SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
				assertTrue(hasRole(COMMUNITY, GROUP, ROLE));
				assertEquals(ReturnCode.SUCCESS, leaveRole(COMMUNITY, GROUP, ROLE));
				assertFalse(hasRole(COMMUNITY, GROUP, ROLE));
			}
		});
	}
}
