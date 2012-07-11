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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.logging.Level;

import madkit.action.AgentAction;
import madkit.agr.LocalCommunity;
import madkit.agr.LocalCommunity.Groups;
import madkit.agr.Organization;
import madkit.kernel.AbstractAgent;
import madkit.kernel.Agent;
import madkit.kernel.JunitMadkit;
import madkit.kernel.Madkit.LevelOption;
import madkit.kernel.Message;
import madkit.message.AgentHookMessage;

import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.14
 * @version 0.9
 * 
 */
@SuppressWarnings("serial")
public class HookSystemTest extends JunitMadkit {
	
	@Test
	public void createGroupHook() {
		addMadkitArgs(LevelOption.agentLogLevel.toString(), Level.ALL.toString()
//				,LevelOption.kernelLogLevel.toString(),Level.ALL.toString()
				);
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				launchAgent(new Agent(){
					@Override
					protected void activate() {
						sendMessage(LocalCommunity.NAME, 
								LocalCommunity.Groups.SYSTEM, 
								Organization.GROUP_MANAGER_ROLE, 
								new AgentHookMessage(AgentAction.CREATE_GROUP));
						pause(10);
						createGroup(COMMUNITY, GROUP);
						Message m = waitNextMessage();
						assertNotNull(m);
						System.err.println(m);
					}
				},0);
			}
		});
		pause(100);
	}

	@Test
	public void releaseHook() {
		addMadkitArgs(LevelOption.agentLogLevel.toString(), Level.ALL.toString()
//				,LevelOption.kernelLogLevel.toString(),Level.ALL.toString()
				);
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				launchAgent(new Agent(){
					@Override
					protected void activate() {
						sendMessage(LocalCommunity.NAME, 
								LocalCommunity.Groups.SYSTEM, 
								Organization.GROUP_MANAGER_ROLE, 
								new AgentHookMessage(AgentAction.CREATE_GROUP));
						pause(10);
						sendMessage(LocalCommunity.NAME, 
								LocalCommunity.Groups.SYSTEM, 
								Organization.GROUP_MANAGER_ROLE, 
								new AgentHookMessage(AgentAction.CREATE_GROUP));
						createGroup(COMMUNITY, GROUP);
						assertNull(nextMessage());
					}
				},0);
			}
		});
		pause(100);
	}

	@Test
	public void requestRole() {
		addMadkitArgs(LevelOption.agentLogLevel.toString(), Level.ALL.toString()
//				,LevelOption.kernelLogLevel.toString(),Level.ALL.toString()
				);
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				launchAgent(new Agent(){
					@Override
					protected void activate() {
						sendMessage(LocalCommunity.NAME, 
								LocalCommunity.Groups.SYSTEM, 
								Organization.GROUP_MANAGER_ROLE, 
								new AgentHookMessage(AgentAction.REQUEST_ROLE));
						pause(10);
						createGroup(COMMUNITY, GROUP);
						requestRole(COMMUNITY, GROUP, ROLE);
						Message m = waitNextMessage();
						assertNotNull(m);
						System.err.println(m);
					}
				},0);
			}
		});
		pause(100);
	}

	@Test
	public void leaveRole() {
		addMadkitArgs(LevelOption.agentLogLevel.toString(), Level.ALL.toString()
//				,LevelOption.kernelLogLevel.toString(),Level.ALL.toString()
				);
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				launchAgent(new Agent(){
					@Override
					protected void activate() {
						sendMessage(LocalCommunity.NAME, 
								LocalCommunity.Groups.SYSTEM, 
								Organization.GROUP_MANAGER_ROLE, 
								new AgentHookMessage(AgentAction.LEAVE_ROLE));
						pause(10);
						createGroup(COMMUNITY, GROUP);
						requestRole(COMMUNITY, GROUP, ROLE);
						leaveRole(COMMUNITY, GROUP, ROLE);
						Message m = waitNextMessage();
						assertNotNull(m);
						System.err.println(m);
					}
				},0);
			}
		});
		pause(100);
	}

	@Test
	public void leaveGroup() {
		addMadkitArgs(LevelOption.agentLogLevel.toString(), Level.ALL.toString()
//				,LevelOption.kernelLogLevel.toString(),Level.ALL.toString()
				);
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				launchAgent(new Agent(){
					@Override
					protected void activate() {
						sendMessage(LocalCommunity.NAME, 
								LocalCommunity.Groups.SYSTEM, 
								Organization.GROUP_MANAGER_ROLE, 
								new AgentHookMessage(AgentAction.LEAVE_GROUP));
						pause(10);
						createGroup(COMMUNITY, GROUP);
						leaveGroup(COMMUNITY, GROUP);
						Message m = waitNextMessage();
						assertNotNull(m);
						System.err.println(m);
					}
				},0);
			}
		});
		pause(100);
	}

	@Test
	public void sendMessage() {
		addMadkitArgs(LevelOption.agentLogLevel.toString(), Level.ALL.toString()
//				,LevelOption.kernelLogLevel.toString(),Level.ALL.toString()
				);
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				launchAgent(new Agent(){
					@Override
					protected void activate() {
						sendMessage(
								LocalCommunity.NAME, 
								LocalCommunity.Groups.SYSTEM, 
								Organization.GROUP_MANAGER_ROLE, 
								new AgentHookMessage(AgentAction.SEND_MESSAGE));
						pause(10);
						sendMessage(LocalCommunity.NAME, Groups.SYSTEM, Organization.GROUP_MANAGER_ROLE, new Message());
						Message m = waitNextMessage();
						assertNotNull(m);
						System.err.println(m);
					}
				},0);
			}
		});
		pause(100);
	}


	@Test
	public void broadcastMessage() {
		addMadkitArgs(LevelOption.agentLogLevel.toString(), Level.ALL.toString()
//				,LevelOption.kernelLogLevel.toString(),Level.ALL.toString()
				);
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				launchAgent(new Agent(){
					@Override
					protected void activate() {
						sendMessage(
								LocalCommunity.NAME, 
								LocalCommunity.Groups.SYSTEM, 
								Organization.GROUP_MANAGER_ROLE, 
								new AgentHookMessage(AgentAction.BROADCAST_MESSAGE));
						pause(10);
						broadcastMessage(LocalCommunity.NAME, Groups.SYSTEM, Organization.GROUP_MANAGER_ROLE, new Message());
						Message m = waitNextMessage();
						assertNotNull(m);
						System.err.println(m);
					}
				},0);
			}
		});
		pause(100);
	}

}
