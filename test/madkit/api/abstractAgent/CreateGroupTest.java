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
public class CreateGroupTest  extends JunitMadKit{
	
	final GroupIdentifier gi = new GroupIdentifier() {
		@Override
		public boolean allowAgentToTakeRole(String roleName, Object memberCard) {
			return false;
		}
	};

	@Test
	public void createGroup(){
		launchTest(new AbstractAgent(){
			protected void activate() {
			assertEquals(SUCCESS, createGroup(COMMUNITY,GROUP));
			assertTrue(isCommunity(COMMUNITY));
			assertTrue(isGroup(COMMUNITY,GROUP));
			assertEquals(SUCCESS, createGroup(aa(),aa(),true));
			assertEquals(SUCCESS, createGroup(aa(),aa(),false));
			assertEquals(SUCCESS, createGroup(aa(),aa(),true,gi));
			assertEquals(SUCCESS, createGroup(aa(),aa(),false,gi));
		}});
	}

	@Test
	public void createGroupAlreadyDone(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				assertEquals(SUCCESS, createGroup(COMMUNITY,GROUP));
				assertEquals(ALREADY_GROUP, createGroup(COMMUNITY,GROUP));
				assertTrue(isGroup(COMMUNITY,GROUP));
			}
		});
	}
	
	@Test
	public void nullArgs(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				assertFalse(isCommunity(null));
				assertFalse(isGroup(null,null));
				assertEquals(NULL_STRING, createGroup(null,null));
				assertEquals(NULL_STRING, createGroup(null,null,true));
				assertEquals(NULL_STRING, createGroup(null,null,false));
				assertEquals(NULL_STRING, createGroup(null,null,true,null));
				assertEquals(NULL_STRING, createGroup(aa(),null,false,null));
				assertEquals(NULL_STRING, createGroup(null,null,false,null));
				assertEquals(NULL_STRING, createGroup(null,null,false,gi));
			}
		});
	}
	
	@Test
	public void createGroupAndLeaveAndCreate(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				assertFalse(isCommunity(COMMUNITY));
				assertFalse(isGroup(COMMUNITY,GROUP));
				assertEquals(SUCCESS, createGroup(COMMUNITY,GROUP));
				assertTrue(isCommunity(COMMUNITY));
				assertTrue(isGroup(COMMUNITY,GROUP));
				assertEquals(SUCCESS, leaveGroup(COMMUNITY,GROUP));
				assertEquals(SUCCESS, createGroup(COMMUNITY,GROUP));
				assertEquals(SUCCESS, leaveGroup(COMMUNITY,GROUP));
				assertFalse(isCommunity(COMMUNITY));
				assertFalse(isGroup(COMMUNITY,GROUP));
			}
		});
	}
	
	
}
