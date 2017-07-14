/*
 * Copyright or © or Copr. Fabien Michel, Olivier Gutknecht, Jacques Ferber (1997) fmichel@lirmm.fr
 * olg@no-distance.net ferber@lirmm.fr This software is a computer program whose purpose is to
 * provide a lightweight Java library for designing and simulating Multi-Agent Systems (MAS). This
 * software is governed by the CeCILL-C license under French law and abiding by the rules of
 * distribution of free software. You can use, modify and/ or redistribute the software under the
 * terms of the CeCILL-C license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info". As a counterpart to the access to the source code and rights to copy,
 * modify and redistribute granted by the license, users are provided only with a limited warranty
 * and the software's author, the holder of the economic rights, and the successive licensors have
 * only limited liability. In this respect, the user's attention is drawn to the risks associated
 * with loading, using, modifying and/or developing or reproducing the software by the user in light
 * of its specific status of free software, that may mean that it is complicated to manipulate, and
 * that also therefore means that it is reserved for developers and experienced professionals having
 * in-depth computer knowledge. Users are therefore encouraged to load and test the software's
 * suitability as regards their requirements in conditions enabling the security of their systems
 * and/or data to be ensured and, more generally, to use and operate it in the same conditions as
 * regards security. The fact that you are presently reading this means that you have had knowledge
 * of the CeCILL-C license and that you accept its terms.
 */
package madkit.hook;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.logging.Level;

import org.junit.Test;

import madkit.agr.DefaultMaDKitRoles;
import madkit.agr.LocalCommunity;
import madkit.agr.LocalCommunity.Groups;
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

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.14
 * @version 0.9
 */

public class HookSystemTest extends JunitMadkit {

    @Test
    public void createGroupHook() {
	addMadkitArgs(LevelOption.agentLogLevel.toString(), Level.ALL.toString()
	// ,LevelOption.kernelLogLevel.toString(),Level.ALL.toString()
	);
	launchTest(new NormalAgent() {

	    @Override
	    protected void activate() {
		sendMessage(LocalCommunity.NAME, LocalCommunity.Groups.SYSTEM, DefaultMaDKitRoles.GROUP_MANAGER_ROLE, new HookMessage(AgentActionEvent.CREATE_GROUP));
		pause(10);
		createGroup(COMMUNITY, GROUP);
		OrganizationEvent m = (OrganizationEvent) waitNextMessage();
		assertNotNull(m);
		assertEquals(COMMUNITY, m.getSourceAgent().getCommunity());
		assertEquals(GROUP, m.getSourceAgent().getGroup());
		assertEquals(DefaultMaDKitRoles.GROUP_MANAGER_ROLE, m.getSourceAgent().getRole());
	    }
	});
	pause(100);
    }

    @Test
    public void releaseHook() {
	addMadkitArgs(LevelOption.agentLogLevel.toString(), Level.ALL.toString()
	// ,LevelOption.kernelLogLevel.toString(),Level.ALL.toString()
	);
	launchTest(new NormalAgent() {

	    @Override
	    protected void activate() {
		sendMessage(LocalCommunity.NAME, LocalCommunity.Groups.SYSTEM, DefaultMaDKitRoles.GROUP_MANAGER_ROLE, new HookMessage(AgentActionEvent.CREATE_GROUP));
		sendMessage(LocalCommunity.NAME, LocalCommunity.Groups.SYSTEM, DefaultMaDKitRoles.GROUP_MANAGER_ROLE, new HookMessage(AgentActionEvent.CREATE_GROUP));
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
	// ,LevelOption.kernelLogLevel.toString(),Level.ALL.toString()
	);
	launchTest(new NormalAgent() {

	    @Override
	    protected void activate() {
		sendMessage(LocalCommunity.NAME, LocalCommunity.Groups.SYSTEM, DefaultMaDKitRoles.GROUP_MANAGER_ROLE, new HookMessage(AgentActionEvent.REQUEST_ROLE));
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
	// ,LevelOption.kernelLogLevel.toString(),Level.ALL.toString()
	);
	launchTest(new NormalAgent() {

	    @Override
	    protected void activate() {
		sendMessage(LocalCommunity.NAME, LocalCommunity.Groups.SYSTEM, DefaultMaDKitRoles.GROUP_MANAGER_ROLE, new HookMessage(AgentActionEvent.LEAVE_ROLE));
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
	// ,LevelOption.kernelLogLevel.toString(),Level.ALL.toString()
	);
	launchTest(new NormalAgent() {

	    @Override
	    protected void activate() {
		sendMessage(LocalCommunity.NAME, LocalCommunity.Groups.SYSTEM, DefaultMaDKitRoles.GROUP_MANAGER_ROLE, new HookMessage(AgentActionEvent.LEAVE_GROUP));
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
	// ,LevelOption.kernelLogLevel.toString(),Level.ALL.toString()
	);
	launchTest(new NormalAgent() {

	    @Override
	    protected void activate() {
		sendMessage(LocalCommunity.NAME, LocalCommunity.Groups.SYSTEM, DefaultMaDKitRoles.GROUP_MANAGER_ROLE, new HookMessage(AgentActionEvent.SEND_MESSAGE));
		pause(10);
		sendMessage(LocalCommunity.NAME, Groups.SYSTEM, DefaultMaDKitRoles.GROUP_MANAGER_ROLE, new Message());
		MessageEvent m = (MessageEvent) waitNextMessage();
		assertNotNull(m);
		assertEquals(AgentActionEvent.SEND_MESSAGE, m.getContent());
		assertEquals(hashCode(), m.getMessage().getSender().hashCode());
		assertEquals(DefaultMaDKitRoles.GROUP_CANDIDATE_ROLE, m.getMessage().getSender().getRole());
	    }
	});
    }

    @Test
    public void broadcastMessage() {
	addMadkitArgs(LevelOption.agentLogLevel.toString(), Level.ALL.toString()
	// ,LevelOption.kernelLogLevel.toString(),Level.ALL.toString()
	);
	launchTest(new NormalAgent() {

	    @Override
	    protected void activate() {
		sendMessage(LocalCommunity.NAME, LocalCommunity.Groups.SYSTEM, DefaultMaDKitRoles.GROUP_MANAGER_ROLE, new HookMessage(AgentActionEvent.BROADCAST_MESSAGE));
		pause(10);
		broadcastMessage(LocalCommunity.NAME, Groups.SYSTEM, DefaultMaDKitRoles.GROUP_MANAGER_ROLE, new Message());
		MessageEvent m = (MessageEvent) waitNextMessage();
		assertNotNull(m);
		assertEquals(AgentActionEvent.BROADCAST_MESSAGE, m.getContent());
		assertEquals(hashCode(), m.getMessage().getSender().hashCode());
		assertEquals(DefaultMaDKitRoles.GROUP_CANDIDATE_ROLE, m.getMessage().getSender().getRole());
	    }
	});
    }

    @Test
    public void agentStarted() {
	addMadkitArgs(LevelOption.agentLogLevel.toString(), Level.ALL.toString(), LevelOption.kernelLogLevel.toString(), Level.OFF.toString());
	launchTest(new NormalAgent() {

	    @Override
	    protected void activate() {
		sendMessage(LocalCommunity.NAME, LocalCommunity.Groups.SYSTEM, DefaultMaDKitRoles.GROUP_MANAGER_ROLE, new HookMessage(AgentActionEvent.AGENT_STARTED));
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
	// ,LevelOption.kernelLogLevel.toString(),Level.ALL.toString()
	);
	launchTest(new NormalAgent() {

	    @Override
	    protected void activate() {
		sendMessage(LocalCommunity.NAME, LocalCommunity.Groups.SYSTEM, DefaultMaDKitRoles.GROUP_MANAGER_ROLE, new HookMessage(AgentActionEvent.AGENT_TERMINATED));
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
