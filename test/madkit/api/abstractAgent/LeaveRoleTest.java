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
package madkit.api.abstractAgent;

import static madkit.kernel.AbstractAgent.ReturnCode.*;
import static madkit.kernel.AbstractAgent.ReturnCode.NOT_GROUP;
import static madkit.kernel.AbstractAgent.ReturnCode.ROLE_NOT_HANDLED;
import static madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static madkit.kernel.Madkit.Roles.GROUP_MANAGER_ROLE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import madkit.kernel.AbstractAgent;
import madkit.kernel.JunitMadKit;

import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.6
 * @version 0.9
 * 
 */
@SuppressWarnings("serial")
public class LeaveRoleTest  extends JunitMadKit{

	@Test
	public void returnSuccess(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				assertEquals(SUCCESS, createGroup(COMMUNITY,GROUP));
				assertEquals(SUCCESS, requestRole(COMMUNITY,GROUP,ROLE));
				assertEquals(SUCCESS, leaveRole(COMMUNITY,GROUP,ROLE));
				assertTrue(isGroup(COMMUNITY,GROUP));
				assertEquals(SUCCESS, leaveRole(COMMUNITY,GROUP,GROUP_MANAGER_ROLE));
				//leaveGroup by leaving roles
				assertFalse(isCommunity(COMMUNITY));
				assertFalse(isGroup(COMMUNITY,GROUP));
			}});
	}

	@Test
	public void returnNotCgr(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				assertEquals(SUCCESS, createGroup(COMMUNITY,GROUP));
				assertEquals(NOT_COMMUNITY, leaveRole(aa(),GROUP,ROLE));
				assertEquals(NOT_GROUP, leaveRole(COMMUNITY,aa(),ROLE));
				assertEquals(NOT_ROLE, leaveRole(COMMUNITY,GROUP,aa()));
				assertEquals(SUCCESS, launchAgent(new AbstractAgent(){
					@Override
					protected void activate() {
						requestRole(COMMUNITY, GROUP, ROLE);
					}
				}));
				assertEquals(ROLE_NOT_HANDLED, leaveRole(COMMUNITY,GROUP,ROLE));				
				assertEquals(SUCCESS, leaveGroup(COMMUNITY,GROUP));
				assertEquals(NOT_IN_GROUP, leaveRole(COMMUNITY,GROUP,ROLE));				
			}});
	}

	@Test
	public void nullArgs(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				assertEquals(SUCCESS, createGroup(COMMUNITY,GROUP));
				try {
					assertEquals(NOT_COMMUNITY, leaveRole(null,null,null));
					noExceptionFailure();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
				try {
					assertEquals(NOT_GROUP, leaveRole(COMMUNITY,null,null));
					noExceptionFailure();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
				try {
					assertEquals(ROLE_NOT_HANDLED, leaveRole(COMMUNITY,GROUP,null));
					noExceptionFailure();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
				try {
					assertEquals(NOT_COMMUNITY, leaveRole(null,GROUP,null));
					noExceptionFailure();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
				try {
					assertEquals(NOT_COMMUNITY, leaveRole(null,GROUP,ROLE));
					noExceptionFailure();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
				try {
					assertEquals(NOT_COMMUNITY, leaveRole(null,null,ROLE));
					noExceptionFailure();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
			}
		});
	}

}
