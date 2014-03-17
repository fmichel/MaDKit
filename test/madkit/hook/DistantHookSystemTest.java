/*
 * Copyright 1997-2013 Fabien Michel, Olivier Gutknecht, Jacques Ferber
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

import java.util.logging.Level;

import madkit.action.KernelAction;
import madkit.agr.LocalCommunity;
import madkit.agr.Organization;
import madkit.kernel.JunitMadkit;
import madkit.kernel.Madkit;
import madkit.kernel.Madkit.BooleanOption;
import madkit.kernel.Madkit.LevelOption;
import madkit.kernel.Message;
import madkit.message.StringMessage;
import madkit.message.hook.HookMessage;
import madkit.message.hook.HookMessage.AgentActionEvent;
import madkit.message.hook.MessageEvent;
import madkit.message.hook.OrganizationEvent;
import madkit.testing.util.agent.LeaveGroupInEndNormalAgent;
import madkit.testing.util.agent.LeaveRoleInEndNormalAgent;
import madkit.testing.util.agent.NormalAgent;
import madkit.testing.util.agent.PongAgent;

import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.14
 * @version 0.9
 * 
 */
@SuppressWarnings("serial")
public class DistantHookSystemTest extends JunitMadkit {
	
//	@BeforeClass
//	
//	public static void init() {
//		pause(2000);
//	}
//	
	@Test
	public void createGroupHook() {
		addMadkitArgs(LevelOption.agentLogLevel.toString(), Level.ALL.toString()
				,LevelOption.kernelLogLevel.toString(), Level.ALL.toString()
				,BooleanOption.network.toString()
				);
		Madkit mdk = launchTest(new NormalAgent() {
			@Override
			protected void activate() {
						sendMessage(LocalCommunity.NAME, 
								LocalCommunity.Groups.SYSTEM, 
								Organization.GROUP_MANAGER_ROLE, 
								new HookMessage(AgentActionEvent.CREATE_GROUP));
						Madkit mk = launchMKNetworkInstance(Level.ALL);
						OrganizationEvent m = (OrganizationEvent) waitNextMessage();
						assertEquals(COMMUNITY, m.getSourceAgent().getCommunity());
						assertEquals(GROUP, m.getSourceAgent().getGroup());
						assertEquals(Organization.GROUP_MANAGER_ROLE, m.getSourceAgent().getRole());
						mk.doAction(KernelAction.EXIT);
					}
				});
		mdk.doAction(KernelAction.EXIT);
		cleanHelperMDKs();
	}
	
//	@Test
//	public void massTest(){
//		for (int i = 0; i < 2; i++) {
//			leaveGroup();
//			broadcastMessage();
//			createGroupHook();
//			leaveRole();
//			requestRole();
//			sendMessage();
//		}
//	}

	@Test
	public void requestRole() {
		addMadkitArgs(LevelOption.agentLogLevel.toString(), Level.ALL.toString()
				,LevelOption.kernelLogLevel.toString(), Level.ALL.toString()
				,BooleanOption.network.toString()
				);
		Madkit mdk = launchTest(new NormalAgent() {
					@Override
					protected void activate() {
						sendMessage(LocalCommunity.NAME, 
								LocalCommunity.Groups.SYSTEM, 
								Organization.GROUP_MANAGER_ROLE, 
								new HookMessage(AgentActionEvent.REQUEST_ROLE));
						Madkit mk = launchMKNetworkInstance(Level.OFF);
						OrganizationEvent m;
						do{
							m = (OrganizationEvent) waitNextMessage(1000);
						}
						while (! m.getSourceAgent().getCommunity().equals(COMMUNITY));
						assertEquals(AgentActionEvent.REQUEST_ROLE, m.getContent());
						assertEquals(COMMUNITY, m.getSourceAgent().getCommunity());
						assertEquals(GROUP, m.getSourceAgent().getGroup());
						assertEquals(ROLE, m.getSourceAgent().getRole());
						mk.doAction(KernelAction.EXIT);
					}
				});
		mdk.doAction(KernelAction.EXIT);
		cleanHelperMDKs();
	}

	@Test
	public void leaveRole() {
		addMadkitArgs(
				LevelOption.agentLogLevel.toString(), Level.ALL.toString()
				,LevelOption.kernelLogLevel.toString(), Level.ALL.toString()
				,LevelOption.networkLogLevel.toString(), Level.ALL.toString()
				,BooleanOption.network.toString()
				);
		Madkit mdk = launchTest(new NormalAgent() {
			@Override
					protected void activate() {
						sendMessage(LocalCommunity.NAME, 
								LocalCommunity.Groups.SYSTEM, 
								Organization.GROUP_MANAGER_ROLE, 
								new HookMessage(AgentActionEvent.LEAVE_ROLE));
						Madkit mk = launchCustomNetworkInstance(Level.FINE,LeaveRoleInEndNormalAgent.class);
						OrganizationEvent m;
						do{
							m = (OrganizationEvent) waitNextMessage(1000);
						}
						while (! m.getSourceAgent().getCommunity().equals(COMMUNITY));
						assertEquals(AgentActionEvent.LEAVE_ROLE, m.getContent());
						assertEquals(COMMUNITY, m.getSourceAgent().getCommunity());
						assertEquals(GROUP, m.getSourceAgent().getGroup());
						assertEquals(ROLE, m.getSourceAgent().getRole());
						mk.doAction(KernelAction.EXIT);
					}
				});
		mdk.doAction(KernelAction.EXIT);
		cleanHelperMDKs();
	}

	@Test
	public void leaveGroup() {
		addMadkitArgs(LevelOption.agentLogLevel.toString(), Level.ALL.toString()
				,LevelOption.kernelLogLevel.toString(), Level.ALL.toString()
				,BooleanOption.network.toString()
				);
		Madkit mdk = launchTest(new NormalAgent() {
			@Override
					protected void activate() {
				setLogLevel(Level.ALL);
						sendMessage(LocalCommunity.NAME, 
								LocalCommunity.Groups.SYSTEM, 
								Organization.GROUP_MANAGER_ROLE, 
								new HookMessage(AgentActionEvent.LEAVE_GROUP));
						Madkit mk = launchCustomNetworkInstance(Level.FINE,LeaveGroupInEndNormalAgent.class);
						OrganizationEvent m;
						do{
							m = (OrganizationEvent) waitNextMessage();
						}
						while (! m.getSourceAgent().getCommunity().equals(COMMUNITY));
						assertEquals(AgentActionEvent.LEAVE_GROUP, m.getContent());
						assertEquals(COMMUNITY, m.getSourceAgent().getCommunity());
						assertEquals(GROUP, m.getSourceAgent().getGroup());
						assertEquals(Organization.GROUP_MANAGER_ROLE, m.getSourceAgent().getRole());
						mk.doAction(KernelAction.EXIT);
					}
				});
		mdk.doAction(KernelAction.EXIT);
		cleanHelperMDKs();
	}

	@Test
	public void sendMessage() {
		addMadkitArgs(LevelOption.agentLogLevel.toString(), Level.ALL.toString()
				,BooleanOption.network.toString()
				);
		Madkit mdk = launchTest(new NormalAgent() {
					@Override
					protected void activate() {
						createGroup(COMMUNITY, GROUP,true);
						requestRole(COMMUNITY, GROUP, ROLE);
						sendMessage(
								LocalCommunity.NAME, 
								LocalCommunity.Groups.SYSTEM, 
								Organization.GROUP_MANAGER_ROLE, 
								new HookMessage(AgentActionEvent.SEND_MESSAGE));
						Madkit mk = launchCustomNetworkInstance(Level.FINE,PongAgent.class);
						waitNextMessage();
						Message waitNextMessage = null;
						while (waitNextMessage == null || ! (waitNextMessage instanceof MessageEvent)) {
							waitNextMessage = waitNextMessage();
						}
						MessageEvent m = (MessageEvent) waitNextMessage;
						assertNotNull(m);
						assertEquals(AgentActionEvent.SEND_MESSAGE, m.getContent());
						assertEquals(ROLE, m.getMessage().getSender().getRole());
						assertEquals("test", ((StringMessage) m.getMessage()).getContent());
						mk.doAction(KernelAction.EXIT);
					}
				});
		mdk.doAction(KernelAction.EXIT);
	}


	@Test
	public void broadcastMessage() {
		addMadkitArgs(LevelOption.agentLogLevel.toString(), Level.ALL.toString()
				,BooleanOption.network.toString()
				);
		Madkit mdk = launchTest(new NormalAgent() {
					@Override
					protected void activate() {
						createGroup(COMMUNITY, GROUP,true);
						requestRole(COMMUNITY, GROUP, ROLE);
						sendMessage(
								LocalCommunity.NAME, 
								LocalCommunity.Groups.SYSTEM, 
								Organization.GROUP_MANAGER_ROLE, 
								new HookMessage(AgentActionEvent.SEND_MESSAGE));
						Madkit mk = launchCustomNetworkInstance(Level.FINE,PongAgent.class);
						waitNextMessage();
						Message waitNextMessage = null;
						while (waitNextMessage == null || ! (waitNextMessage instanceof MessageEvent)) {
							waitNextMessage = waitNextMessage();
						}
						MessageEvent m = (MessageEvent) waitNextMessage;
						assertNotNull(m);
						assertEquals(AgentActionEvent.SEND_MESSAGE, m.getContent());
						assertEquals(ROLE, m.getMessage().getSender().getRole());
						assertEquals("test", ((StringMessage) m.getMessage()).getContent());
						mk.doAction(KernelAction.EXIT);
					}
				});
		mdk.doAction(KernelAction.EXIT);
	}

}
