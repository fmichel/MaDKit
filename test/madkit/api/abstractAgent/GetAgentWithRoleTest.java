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
public class GetAgentWithRoleTest  extends JunitMadKit{
	
	final AbstractAgent target = new AbstractAgent(){
		protected void activate() {
			assertEquals(SUCCESS, createGroup(COMMUNITY,GROUP));
		}
	};

	@Test
	public void nullArgs(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				assertNull(getAgentWithRole(null,null,null));
				assertNull(getAgentWithRole(COMMUNITY,null,null));
				assertNull(getAgentWithRole(COMMUNITY,GROUP,null));
				assertNull(getAgentWithRole(null,GROUP,ROLE));
				assertNull(getAgentWithRole(null,null,ROLE));
				assertNull(getAgentWithRole(COMMUNITY,null,ROLE));
			}
		});
	}
	
	@Test
	public void returnNull(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				launchAgent(target);
				assertEquals(SUCCESS, target.requestRole(COMMUNITY,GROUP,ROLE));
				assertNull(getAgentWithRole(aa(),GROUP,ROLE));
				assertNull(getAgentWithRole(COMMUNITY,aa(),ROLE));
				assertNull(getAgentWithRole(COMMUNITY,GROUP,aa()));
				assertNotNull(getAgentWithRole(COMMUNITY,GROUP,ROLE));
				assertEquals(SUCCESS, target.leaveRole(COMMUNITY,GROUP,ROLE));
				assertNull(getAgentWithRole(COMMUNITY,GROUP,ROLE));
			}
		});
	}
	
	@Test
	public void returnNotNullOnManagerRole(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				launchAgent(target);
				assertNotNull(getAgentWithRole(COMMUNITY,GROUP,Roles.GROUP_MANAGER_ROLE));
			}
		});
	}
	
	@Test
	public void returnNullOnManagerRole(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				assertEquals(SUCCESS, createGroup(COMMUNITY,GROUP));
				assertNull(getAgentWithRole(COMMUNITY,GROUP,Roles.GROUP_MANAGER_ROLE));
			}
		});
	}
}
