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

import static madkit.kernel.AbstractAgent.ReturnCode.*;
import static madkit.kernel.AbstractAgent.ReturnCode.NOT_COMMUNITY;
import static madkit.kernel.AbstractAgent.ReturnCode.NOT_GROUP;
import static madkit.kernel.AbstractAgent.ReturnCode.NOT_IN_GROUP;
import static madkit.kernel.AbstractAgent.ReturnCode.NOT_ROLE;
import static madkit.kernel.AbstractAgent.ReturnCode.NO_RECIPIENT_FOUND;
import static madkit.kernel.AbstractAgent.ReturnCode.ROLE_NOT_HANDLED;
import static madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import madkit.agr.Organization;
import madkit.kernel.AbstractAgent;
import madkit.kernel.AgentAddress;
import madkit.kernel.JunitMadKit;
import madkit.kernel.Message;

import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.7
 * @version 0.9
 * 
 */
@SuppressWarnings("serial")
public class SendMessageWithCGRTest  extends JunitMadKit{

	final AbstractAgent target = new AbstractAgent(){
		protected void activate() {
			assertEquals(SUCCESS, createGroup(COMMUNITY,GROUP));
			assertEquals(SUCCESS, requestRole(COMMUNITY,GROUP,ROLE));
		}
	};

	@Test
	public void returnSuccess(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				assertEquals(SUCCESS,launchAgent(target));
				assertEquals(SUCCESS, requestRole(COMMUNITY,GROUP,ROLE));
				
				//Without role
				assertEquals(SUCCESS, sendMessage(COMMUNITY,GROUP,ROLE, new Message()));
				Message m = target.nextMessage();
				assertNotNull(m);
				assertEquals(ROLE, m.getReceiver().getRole());

				//With role
				assertEquals(SUCCESS, sendMessageWithRole(COMMUNITY,GROUP,ROLE, new Message(),ROLE));
				m = target.nextMessage();
				assertNotNull(m);
				assertEquals(ROLE, m.getReceiver().getRole());
			}});
	}

	@Test
	public void returnSuccessOnCandidateRole(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				assertEquals(SUCCESS,launchAgent(target));

				//Without role
				assertEquals(SUCCESS, sendMessage(COMMUNITY,GROUP,Organization.GROUP_MANAGER_ROLE, new Message()));
				Message m = target.nextMessage();
				assertNotNull(m);
				assertEquals(Organization.GROUP_MANAGER_ROLE, m.getReceiver().getRole());
				assertEquals(Organization.GROUP_CANDIDATE_ROLE, m.getSender().getRole());

				//With role
				assertEquals(SUCCESS, sendMessageWithRole(COMMUNITY,GROUP,Organization.GROUP_MANAGER_ROLE, new Message(), Organization.GROUP_CANDIDATE_ROLE));
				m = target.nextMessage();
				assertNotNull(m);
				assertEquals(Organization.GROUP_MANAGER_ROLE, m.getReceiver().getRole());
				assertEquals(Organization.GROUP_CANDIDATE_ROLE, m.getSender().getRole());
			}});
	}

	@Test
	public void returnInvalidAA(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				assertEquals(SUCCESS,launchAgent(target));
				assertEquals(SUCCESS, requestRole(COMMUNITY,GROUP,ROLE));
				AgentAddress aa = getAgentWithRole(COMMUNITY, GROUP, ROLE);
				assertEquals(SUCCESS, target.leaveRole(COMMUNITY, GROUP, ROLE));
				assertEquals(INVALID_AA, sendMessage(aa, new Message()));
				//With role
				assertEquals(INVALID_AA, sendMessageWithRole(aa, new Message(), ROLE));
			}});
	}

	@Test
	public void returnNotInGroup(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				assertEquals(SUCCESS,launchAgent(target));
				assertEquals(NOT_IN_GROUP, sendMessage(COMMUNITY,GROUP,ROLE, new Message()));
				//With role
				assertEquals(NOT_IN_GROUP, sendMessageWithRole(COMMUNITY,GROUP,ROLE, new Message(), ROLE));
			}});
	}
	
	@Test
	public void returnNotCGR(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				assertEquals(SUCCESS,launchAgent(target));
				assertEquals(NOT_COMMUNITY, sendMessage(aa(),GROUP,ROLE, new Message()));
				assertEquals(NOT_GROUP, sendMessage(COMMUNITY,aa(),ROLE, new Message()));
				assertEquals(NOT_ROLE, sendMessage(COMMUNITY,GROUP,aa(), new Message()));

				//With role
				assertEquals(NOT_COMMUNITY, sendMessageWithRole(aa(),GROUP,ROLE, new Message(), ROLE));
				assertEquals(NOT_GROUP, sendMessageWithRole(COMMUNITY,aa(),ROLE, new Message(), ROLE));
				assertEquals(NOT_ROLE, sendMessageWithRole(COMMUNITY,GROUP,aa(), new Message(), ROLE));
			}});
	}
	
	@Test
	public void returnRoleNotHandled(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				assertEquals(SUCCESS,launchAgent(target));
				assertEquals(SUCCESS, requestRole(COMMUNITY,GROUP,ROLE));
				assertEquals(ROLE_NOT_HANDLED, sendMessageWithRole(COMMUNITY,GROUP,ROLE, new Message(), aa()));
			}});
	}
	
	@Test
	public void returnNoRecipientFound(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				assertEquals(SUCCESS,launchAgent(target));
				assertEquals(SUCCESS, requestRole(COMMUNITY,GROUP,ROLE));
				assertEquals(SUCCESS, target.leaveRole(COMMUNITY,GROUP,ROLE));
				assertEquals(NO_RECIPIENT_FOUND, sendMessage(COMMUNITY,GROUP,ROLE, new Message()));
				assertEquals(NO_RECIPIENT_FOUND, sendMessageWithRole(COMMUNITY,GROUP,ROLE, new Message(),ROLE));
			}});
	}
	
	@Test
	public void nullCommunity(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				assertEquals(SUCCESS,launchAgent(target));
				try {
					sendMessage(null, aa(), aa(), new Message());
					noExceptionFailure();
				} catch (NullPointerException e) {
					throw e;
				}
			}
		},AGENT_CRASH);
	}
	
	@Test
	public void nullGroup(){
		launchTest(new AbstractAgent(){
				protected void activate() {
					assertEquals(SUCCESS,launchAgent(target));
					try {
						sendMessage(COMMUNITY, null, aa(), new Message());
						noExceptionFailure();
					} catch (NullPointerException e) {
						throw e;
					}
			}
		},AGENT_CRASH);
	}
	
	@Test
	public void nullRole(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				assertEquals(SUCCESS,launchAgent(target));
				try {
					sendMessage(COMMUNITY, GROUP, null, new Message());
					noExceptionFailure();
				} catch (NullPointerException e) {
					throw e;
				}
			}
		},AGENT_CRASH);
	}
	
	@Test
	public void nullMessage(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				assertEquals(SUCCESS,launchAgent(target));
				assertEquals(SUCCESS,requestRole(COMMUNITY,GROUP,ROLE));
				try {
					sendMessage(COMMUNITY, GROUP, ROLE, null);
					noExceptionFailure();
				} catch (NullPointerException e) {
					throw e;
				}
			}
		},AGENT_CRASH);
	}
	


}
