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

import static madkit.kernel.AbstractAgent.ReturnCode.INVALID_AA;
import static madkit.kernel.AbstractAgent.ReturnCode.INVALID_ARG;
import static madkit.kernel.AbstractAgent.ReturnCode.NOT_IN_GROUP;
import static madkit.kernel.AbstractAgent.ReturnCode.ROLE_NOT_HANDLED;
import static madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static madkit.kernel.Madkit.Roles.GROUP_CANDIDATE_ROLE;
import static madkit.kernel.Madkit.Roles.GROUP_MANAGER_ROLE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.*;
import madkit.kernel.AbstractAgent;
import madkit.kernel.Agent;
import madkit.kernel.AgentAddress;
import madkit.kernel.JunitMadKit;
import madkit.kernel.Message;
import madkit.messages.ObjectMessage;

import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.8
 * @version 0.9
 * 
 */
@SuppressWarnings("all")
public class sendMessageAndWaitForReplyWithAATest  extends JunitMadKit{

	final Agent target = new Agent(){
		AgentAddress aa;
		protected void activate() {
			assertEquals(SUCCESS, requestRole(COMMUNITY,GROUP,ROLE));
			aa = getAgentWithRole(COMMUNITY, GROUP, ROLE);
			assertNotNull(aa);
			assertEquals(SUCCESS, sendMessage(aa, new Message()));
			assertEquals(SUCCESS, sendMessage(aa, new Message()));			
		}
		protected void live() {
			waitNextMessage();//waiting the start signal
			sendReply(waitNextMessage(), new ObjectMessage<String>("reply"));
			assertEquals(SUCCESS, sendMessage(aa, new Message()));
			assertEquals(SUCCESS, sendMessage(aa, new Message()));			
			sendReply(waitNextMessage(), new ObjectMessage<String>("reply2"));
		}
	};
	
	final Agent target2 = new Agent(){
		protected void activate() {
			assertEquals(SUCCESS, createGroup(COMMUNITY,GROUP));
		}
		protected void live() {
			sendReply(waitNextMessage(), new ObjectMessage<String>("reply"));
			sendReply(waitNextMessage(), new ObjectMessage<String>("reply2"));
		}
	};
	

	@Test
	public void returnSuccess(){
		launchTest(new Agent(){
			protected void activate() {
				assertEquals(SUCCESS, createGroup(COMMUNITY,GROUP));
				assertEquals(SUCCESS, requestRole(COMMUNITY,GROUP,ROLE));
				assertEquals(SUCCESS,launchAgent(target));
				
				assertFalse(this.isMessageBoxEmpty());
				AgentAddress aa = getAgentWithRole(COMMUNITY, GROUP, ROLE);
				assertNotNull(aa);
				
				//time out but gives the start signal
				assertNull(sendMessageAndWaitForReply(aa, new Message(),100));
				
				//Without role
				Message m = sendMessageAndWaitForReply(aa, new Message());
				assertNotNull(m);
				assertEquals("reply", ((ObjectMessage<String>)m).getContent());
				assertEquals(ROLE, m.getReceiver().getRole());

				assertFalse(this.isMessageBoxEmpty());

				//With role
				m = sendMessageAndWaitForReply(aa, new Message());
				assertNotNull(m);
				assertEquals("reply2", ((ObjectMessage<String>)m).getContent());
				assertEquals(ROLE, m.getReceiver().getRole());
				
				assertNotNull(nextMessage());
				assertNotNull(nextMessage());
				assertNotNull(nextMessage());
				assertNotNull(nextMessage());
				assertNull(nextMessage());
				pause(100);
				assertEquals(INVALID_AA, sendMessage(aa, new Message()));//the target has gone: AgentAddress no longer valid
				assertNull(sendMessageAndWaitForReply(aa, new Message()));//the target has gone: AgentAddress no longer valid
			}});
	}

	@Test
	public void returnSuccessOnCandidateRole(){
		launchTest(new Agent(){
			protected void activate() {
				assertEquals(SUCCESS,launchAgent(target2));

				//Without role
				AgentAddress aa = getAgentWithRole(COMMUNITY, GROUP, GROUP_MANAGER_ROLE);
				assertNotNull(aa);
				Message m = sendMessageAndWaitForReply(aa, new Message());
				assertNotNull(m);
				assertEquals("reply", ((ObjectMessage<String>)m).getContent());
				assertEquals(GROUP_CANDIDATE_ROLE, m.getReceiver().getRole());
				assertEquals(GROUP_MANAGER_ROLE, m.getSender().getRole());
				
				//With role
				m = sendMessageWithRoleAndWaitForReply(aa, new Message(), GROUP_CANDIDATE_ROLE);
				assertNotNull(m);
				assertEquals("reply2", ((ObjectMessage<String>)m).getContent());
				assertEquals(GROUP_CANDIDATE_ROLE, m.getReceiver().getRole());
				assertEquals(GROUP_MANAGER_ROLE, m.getSender().getRole());
			}});
	}

	@Test
	public void returnInvalidAA(){
		launchTest(new Agent(){
			protected void activate() {
				assertEquals(SUCCESS, createGroup(COMMUNITY,GROUP));
				assertEquals(SUCCESS, requestRole(COMMUNITY,GROUP,ROLE));
				assertEquals(SUCCESS,launchAgent(target));
				AgentAddress aa = getAgentWithRole(COMMUNITY, GROUP, ROLE);
				assertEquals(SUCCESS, target.leaveRole(COMMUNITY, GROUP, ROLE));
				assertNull(sendMessageAndWaitForReply(aa, new Message()));//INVALID_AA warning
				assertEquals(INVALID_AA, sendMessage(aa, new Message()));

				//With role
				assertNull(sendMessageWithRoleAndWaitForReply(aa, new Message(), ROLE));//INVALID_AA warning
			}});
	}

	@Test
	public void returnBadCGR(){
		launchTest(new Agent(){
			protected void activate() {
				assertEquals(SUCCESS, createGroup(COMMUNITY,GROUP));
				assertEquals(SUCCESS, requestRole(COMMUNITY,GROUP,ROLE));
				assertEquals(SUCCESS,launchAgent(target));

				AgentAddress aa = getAgentWithRole(COMMUNITY, GROUP, ROLE);
				assertNull(sendMessageWithRoleAndWaitForReply(aa, new Message(),aa()));//not role warning
				assertEquals(SUCCESS, leaveGroup(COMMUNITY,GROUP));
				assertNull(sendMessageAndWaitForReply(aa, new Message()));//not in group warning

			}});
	}
	
	

	@Test
	public void nullArgs(){
		launchTest(new Agent(){
			protected void activate() {
				assertEquals(SUCCESS, createGroup(COMMUNITY,GROUP));
				assertEquals(SUCCESS, requestRole(COMMUNITY,GROUP,ROLE));
				assertEquals(SUCCESS,launchAgent(target));
				AgentAddress aa = getAgentWithRole(COMMUNITY, GROUP, ROLE);
				assertNull(sendMessageAndWaitForReply(null, null));
				assertNull(sendMessageAndWaitForReply(aa, null));
				assertNull(sendMessageAndWaitForReply(null, new Message()));
			}
		});
	}

}
