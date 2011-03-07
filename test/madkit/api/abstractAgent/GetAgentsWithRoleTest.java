/*
 * Copyright 1997-2011 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MadKit.
 * 
 * MadKit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MadKit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with MadKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.api.abstractAgent;

import madkit.kernel.AbstractAgent;
import madkit.kernel.GroupIdentifier;
import madkit.kernel.JunitMadKit;
import madkit.kernel.Madkit.Roles;
import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Test;
import static madkit.kernel.AbstractAgent.ReturnCode.*;

import test.util.JUnitBooterAgent;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.7
 * @version 0.9
 * 
 */
public class GetAgentsWithRoleTest  extends JunitMadKit{

	final AbstractAgent target = new AbstractAgent(){
		protected void activate() {
			assertEquals(SUCCESS, createGroup(COMMUNITY,GROUP));
		}
	};

	@Test
	public void nullArgs(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				assertNull(getAgentsWithRole(null,null,null));
				assertNull(getAgentsWithRole(COMMUNITY,null,null));
				assertNull(getAgentsWithRole(COMMUNITY,GROUP,null));
				assertNull(getAgentsWithRole(null,GROUP,ROLE));
				assertNull(getAgentsWithRole(null,null,ROLE));
				assertNull(getAgentsWithRole(COMMUNITY,null,ROLE));
			}
		});
	}

	@Test
	public void returnNull(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				launchAgent(target);
				assertEquals(SUCCESS, target.requestRole(COMMUNITY,GROUP,ROLE));
				assertNull(getAgentsWithRole(aa(),GROUP,ROLE));
				assertNull(getAgentsWithRole(COMMUNITY,aa(),ROLE));
				assertNull(getAgentsWithRole(COMMUNITY,GROUP,aa()));
				assertNotNull(getAgentsWithRole(COMMUNITY,GROUP,ROLE));
				assertEquals(SUCCESS, target.leaveRole(COMMUNITY,GROUP,ROLE));
				assertNull(getAgentsWithRole(COMMUNITY,GROUP,ROLE));
			}
		});
	}

	@Test
	public void returnSuccess(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				assertNull(getAgentsWithRole(COMMUNITY,GROUP,ROLE));
				launchAgent(target);
				assertEquals(SUCCESS, target.requestRole(COMMUNITY,GROUP,ROLE));
				assertNotNull(getAgentsWithRole(COMMUNITY,GROUP,ROLE));
				assertEquals(1, getAgentsWithRole(COMMUNITY,GROUP,ROLE).size());
				launchAgent(new AbstractAgent(){
					protected void activate() {
						assertEquals(SUCCESS, requestRole(COMMUNITY,GROUP,ROLE));
					}
				});
				assertNotNull(getAgentsWithRole(COMMUNITY,GROUP,ROLE));
				assertEquals(2, getAgentsWithRole(COMMUNITY,GROUP,ROLE).size());
			}
		});
	}

	@Test
	public void returnNotNullOnManagerRole(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				launchAgent(target);
				assertNotNull(getAgentsWithRole(COMMUNITY,GROUP,Roles.GROUP_MANAGER_ROLE));
				assertEquals(1, getAgentsWithRole(COMMUNITY,GROUP,Roles.GROUP_MANAGER_ROLE).size());
			}
		});
	}

	@Test
	public void returnNullOnManagerRole(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				assertEquals(SUCCESS, createGroup(COMMUNITY,GROUP));
				assertNull(getAgentsWithRole(COMMUNITY,GROUP,Roles.GROUP_MANAGER_ROLE));
			}
		});
	}
}
