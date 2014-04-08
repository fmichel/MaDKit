/*
 * Copyright 1997-2012 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MaDKit.
 * 
 * MaDKit is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * MaDKit is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MaDKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.hook;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.logging.Level;

import madkit.agr.LocalCommunity;
import madkit.agr.LocalCommunity.Groups;
import madkit.agr.Organization;
import madkit.kernel.JunitMadkit;
import madkit.kernel.Madkit.LevelOption;
import madkit.kernel.Message;
import madkit.message.hook.AgentLifeEvent;
import madkit.message.hook.HookMessage;
import madkit.message.hook.HookMessage.AgentActionEvent;
import madkit.message.hook.MessageEvent;
import madkit.message.hook.OrganizationEvent;
import madkit.testing.util.agent.NormalAA;
import madkit.testing.util.agent.NormalAgent;

import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.14
 * @version 0.9
 * 
 */

public class HookSystemTest extends JunitMadkit {
	
	@Test
	public void createGroupHook() {
		addMadkitArgs(LevelOption.agentLogLevel.toString(), Level.ALL.toString()
//				,LevelOption.kernelLogLevel.toString(),Level.ALL.toString()
				);
		launchTest(new NormalAgent() {
			@Override
			protected void activate() {
						sendMessage(LocalCommunity.NAME, 
								LocalCommunity.Groups.SYSTEM, 
								Organization.GROUP_MANAGER_ROLE, 
								new HookMessage(AgentActionEvent.CREATE_GROUP));
						pause(10);
						createGroup(COMMUNITY, GROUP);
						OrganizationEvent m = (OrganizationEvent) waitNextMessage();
						assertNotNull(m);
						assertEquals(COMMUNITY, m.getSourceAgent().getCommunity());
						assertEquals(GROUP, m.getSourceAgent().getGroup());
						assertEquals(Organization.GROUP_MANAGER_ROLE, m.getSourceAgent().getRole());
					}
				});
		pause(100);
	}

	@Test
	public void releaseHook() {
		addMadkitArgs(LevelOption.agentLogLevel.toString(), Level.ALL.toString()
//				,LevelOption.kernelLogLevel.toString(),Level.ALL.toString()
				);
		launchTest(new NormalAgent() {
			@Override
					protected void activate() {
						sendMessage(LocalCommunity.NAME, 
								LocalCommunity.Groups.SYSTEM, 
								Organization.GROUP_MANAGER_ROLE, 
								new HookMessage(AgentActionEvent.CREATE_GROUP));
						sendMessage(LocalCommunity.NAME, 
								LocalCommunity.Groups.SYSTEM, 
								Organization.GROUP_MANAGER_ROLE, 
								new HookMessage(AgentActionEvent.CREATE_GROUP));
						pause(10);
						createGroup(COMMUNITY, GROUP);
						assertNull(nextMessage());
					}
				});
		pause(100);
	}

	@Test
	public void requestRole() {
		addMadkitArgs(LevelOption.agentLogLevel.toString(), Level.ALL.toString()
//				,LevelOption.kernelLogLevel.toString(),Level.ALL.toString()
				);
		launchTest(new NormalAgent() {
					@Override
					protected void activate() {
						sendMessage(LocalCommunity.NAME, 
								LocalCommunity.Groups.SYSTEM, 
								Organization.GROUP_MANAGER_ROLE, 
								new HookMessage(AgentActionEvent.REQUEST_ROLE));
						pause(10);
						createGroup(COMMUNITY, GROUP);
						requestRole(COMMUNITY, GROUP, ROLE);
						OrganizationEvent m = (OrganizationEvent) waitNextMessage();
						assertNotNull(m);
						assertEquals(AgentActionEvent.REQUEST_ROLE, m.getContent());
						assertEquals(COMMUNITY, m.getSourceAgent().getCommunity());
						assertEquals(GROUP, m.getSourceAgent().getGroup());
						assertEquals(ROLE, m.getSourceAgent().getRole());
					}
				});
		pause(100);
	}

	@Test
	public void leaveRole() {
		addMadkitArgs(LevelOption.agentLogLevel.toString(), Level.ALL.toString()
//				,LevelOption.kernelLogLevel.toString(),Level.ALL.toString()
				);
		launchTest(new NormalAgent() {
			@Override
					protected void activate() {
						sendMessage(LocalCommunity.NAME, 
								LocalCommunity.Groups.SYSTEM, 
								Organization.GROUP_MANAGER_ROLE, 
								new HookMessage(AgentActionEvent.LEAVE_ROLE));
						pause(10);
						createGroup(COMMUNITY, GROUP);
						requestRole(COMMUNITY, GROUP, ROLE);
						leaveRole(COMMUNITY, GROUP, ROLE);
						OrganizationEvent m = (OrganizationEvent) waitNextMessage();
						assertNotNull(m);
						assertEquals(AgentActionEvent.LEAVE_ROLE, m.getContent());
						assertEquals(COMMUNITY, m.getSourceAgent().getCommunity());
						assertEquals(GROUP, m.getSourceAgent().getGroup());
						assertEquals(ROLE, m.getSourceAgent().getRole());
					}
				});
	}

	@Test
	public void leaveGroup() {
		addMadkitArgs(LevelOption.agentLogLevel.toString(), Level.ALL.toString()
//				,LevelOption.kernelLogLevel.toString(),Level.ALL.toString()
				);
		launchTest(new NormalAgent() {
			@Override
					protected void activate() {
						sendMessage(LocalCommunity.NAME, 
								LocalCommunity.Groups.SYSTEM, 
								Organization.GROUP_MANAGER_ROLE, 
								new HookMessage(AgentActionEvent.LEAVE_GROUP));
						pause(10);
						createGroup(COMMUNITY, GROUP);
						leaveGroup(COMMUNITY, GROUP);
						OrganizationEvent m = (OrganizationEvent) waitNextMessage();
						assertNotNull(m);
						assertEquals(AgentActionEvent.LEAVE_GROUP, m.getContent());
						assertEquals(COMMUNITY, m.getSourceAgent().getCommunity());
						assertEquals(GROUP, m.getSourceAgent().getGroup());
						assertNull(m.getSourceAgent().getRole());
					}
				});
	}

	@Test
	public void sendMessage() {
		addMadkitArgs(LevelOption.agentLogLevel.toString(), Level.ALL.toString()
//				,LevelOption.kernelLogLevel.toString(),Level.ALL.toString()
				);
		launchTest(new NormalAgent() {
					@Override
					protected void activate() {
						sendMessage(
								LocalCommunity.NAME, 
								LocalCommunity.Groups.SYSTEM, 
								Organization.GROUP_MANAGER_ROLE, 
								new HookMessage(AgentActionEvent.SEND_MESSAGE));
						pause(10);
						sendMessage(LocalCommunity.NAME, Groups.SYSTEM, Organization.GROUP_MANAGER_ROLE, new Message());
						MessageEvent m = (MessageEvent) waitNextMessage();
						assertNotNull(m);
						assertEquals(AgentActionEvent.SEND_MESSAGE, m.getContent());
						assertEquals(hashCode(), m.getMessage().getSender().hashCode());
						assertEquals(Organization.GROUP_CANDIDATE_ROLE, m.getMessage().getSender().getRole());
					}
				});
	}


	@Test
	public void broadcastMessage() {
		addMadkitArgs(LevelOption.agentLogLevel.toString(), Level.ALL.toString()
//				,LevelOption.kernelLogLevel.toString(),Level.ALL.toString()
				);
		launchTest(new NormalAgent() {
					@Override
					protected void activate() {
						sendMessage(
								LocalCommunity.NAME, 
								LocalCommunity.Groups.SYSTEM, 
								Organization.GROUP_MANAGER_ROLE, 
								new HookMessage(AgentActionEvent.BROADCAST_MESSAGE));
						pause(10);
						broadcastMessage(LocalCommunity.NAME, Groups.SYSTEM, Organization.GROUP_MANAGER_ROLE, new Message());
						MessageEvent m = (MessageEvent) waitNextMessage();
						assertNotNull(m);
						assertEquals(AgentActionEvent.BROADCAST_MESSAGE, m.getContent());
						assertEquals(hashCode(), m.getMessage().getSender().hashCode());
						assertEquals(Organization.GROUP_CANDIDATE_ROLE, m.getMessage().getSender().getRole());
					}
				});
	}


	@Test
	public void agentStarted() {
		addMadkitArgs(LevelOption.agentLogLevel.toString(), Level.ALL.toString()
//				,LevelOption.kernelLogLevel.toString(),Level.ALL.toString()
				);
		launchTest(new NormalAgent() {
					@Override
					protected void activate() {
						sendMessage(
								LocalCommunity.NAME, 
								LocalCommunity.Groups.SYSTEM, 
								Organization.GROUP_MANAGER_ROLE, 
								new HookMessage(AgentActionEvent.AGENT_STARTED));
						pause(10);
						NormalAA a;
						launchAgent(a = new NormalAA());
						AgentLifeEvent m = (AgentLifeEvent) waitNextMessage();
						assertNotNull(m);
						assertEquals(AgentActionEvent.AGENT_STARTED, m.getContent());
						killAgent(a);
						launchAgent(new NormalAgent());
						m = (AgentLifeEvent) waitNextMessage();
						assertNotNull(m);
						assertEquals(AgentActionEvent.AGENT_STARTED, m.getContent());
					}
				});
	}

	@Test
	public void agentTerminated() {
		addMadkitArgs(LevelOption.agentLogLevel.toString(), Level.ALL.toString()
//				,LevelOption.kernelLogLevel.toString(),Level.ALL.toString()
				);
		launchTest(new NormalAgent() {
					@Override
					protected void activate() {
						sendMessage(
								LocalCommunity.NAME, 
								LocalCommunity.Groups.SYSTEM, 
								Organization.GROUP_MANAGER_ROLE, 
								new HookMessage(AgentActionEvent.AGENT_TERMINATED));
						pause(10);
						NormalAA a;
						launchAgent(a = new NormalAA());
						killAgent(a);
						AgentLifeEvent m = (AgentLifeEvent) waitNextMessage();
						assertNotNull(m);
						assertEquals(AgentActionEvent.AGENT_TERMINATED, m.getContent());
						NormalAgent b;
						launchAgent(b = new NormalAgent());
						killAgent(b);
						m = (AgentLifeEvent) waitNextMessage();
						assertNotNull(m);
						assertEquals(AgentActionEvent.AGENT_TERMINATED, m.getContent());
					}
				});
	}

}
