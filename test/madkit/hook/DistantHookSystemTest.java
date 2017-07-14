/*
 * Copyright or © or Copr. Fabien Michel, Olivier Gutknecht, Jacques Ferber (1997)

fmichel@lirmm.fr
olg@no-distance.net
ferber@lirmm.fr

This software is a computer program whose purpose is to 
provide a lightweight Java library for designing and simulating Multi-Agent Systems (MAS).

This software is governed by the CeCILL-C license under French law and
abiding by the rules of distribution of free software.  You can  use, 
modify and/ or redistribute the software under the terms of the CeCILL-C
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info". 

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability. 

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or 
data to be ensured and,  more generally, to use and operate it in the 
same conditions as regards security. 

The fact that you are presently reading this means that you have had
knowledge of the CeCILL-C license and that you accept its terms.
 */
package madkit.hook;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.logging.Level;

import org.junit.Ignore;
import org.junit.Test;

import madkit.action.KernelAction;
import madkit.agr.DefaultMaDKitRoles;
import madkit.agr.LocalCommunity;
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

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.14
 * @version 0.9
 * 
 */

@Ignore //TODO
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
								DefaultMaDKitRoles.GROUP_MANAGER_ROLE, 
								new HookMessage(AgentActionEvent.CREATE_GROUP));
						Madkit mk = launchMKNetworkInstance(Level.ALL);
						OrganizationEvent m = (OrganizationEvent) waitNextMessage();
						assertEquals(COMMUNITY, m.getSourceAgent().getCommunity());
						assertEquals(GROUP, m.getSourceAgent().getGroup());
						assertEquals(DefaultMaDKitRoles.GROUP_MANAGER_ROLE, m.getSourceAgent().getRole());
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
								DefaultMaDKitRoles.GROUP_MANAGER_ROLE, 
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
								DefaultMaDKitRoles.GROUP_MANAGER_ROLE, 
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
				getLogger().setLevel(Level.ALL);
						sendMessage(LocalCommunity.NAME, 
								LocalCommunity.Groups.SYSTEM, 
								DefaultMaDKitRoles.GROUP_MANAGER_ROLE, 
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
						assertEquals(DefaultMaDKitRoles.GROUP_MANAGER_ROLE, m.getSourceAgent().getRole());
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
								DefaultMaDKitRoles.GROUP_MANAGER_ROLE, 
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
								DefaultMaDKitRoles.GROUP_MANAGER_ROLE, 
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
