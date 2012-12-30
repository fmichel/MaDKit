package madkit.kernel;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import madkit.action.AgentAction;
import madkit.kernel.Madkit.LevelOption;
import madkit.message.EnumMessage;

import org.junit.Test;

public class AgentActionTest extends JunitMadkit{

	
	@Test
	public void LAUNCH_AGENT() {
		addMadkitArgs(LevelOption.kernelLogLevel.toString(),"ALL");
		launchTest(new AbstractAgent() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			protected void activate() {
				AbstractAgent a = new Agent();
				EnumMessage<AgentAction> m = new EnumMessage<AgentAction>(AgentAction.LAUNCH_AGENT,a);
				proceedEnumMessage(m);
				assertTrue(a.isAlive());
				assertFalse(a.hasGUI());

				a = new Agent();
				m = new EnumMessage<AgentAction>(AgentAction.LAUNCH_AGENT,a,true);
				proceedEnumMessage(m);
				assertTrue(a.isAlive());
				assertTrue(a.hasGUI());

				a = new Agent();
				m = new EnumMessage<AgentAction>(AgentAction.LAUNCH_AGENT,a,1,true);
				proceedEnumMessage(m);
				assertTrue(a.hasGUI());

				a = new Agent();
				m = new EnumMessage<AgentAction>(AgentAction.LAUNCH_AGENT,a,0);
				proceedEnumMessage(m);
				assertFalse(a.hasGUI());

			}
		});
	}

	@Test
	public void LAUNCH_AGENT_wrongType() {
		addMadkitArgs(LevelOption.kernelLogLevel.toString(),"ALL");
		launchTest(new AbstractAgent() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			protected void activate() {
				AbstractAgent help = new AbstractAgent();
				launchAgent(help);
				createDefaultCGR(help);
				createDefaultCGR(this);
				AbstractAgent a = new Agent();
				EnumMessage<AgentAction> m = new EnumMessage<AgentAction>(AgentAction.LAUNCH_AGENT,a,new Object());
				sendMessage(COMMUNITY, GROUP, ROLE, m);
				proceedEnumMessage(m);
				assertFalse(a.hasGUI());

				a = new Agent();
				m = new EnumMessage<AgentAction>(AgentAction.LAUNCH_AGENT,new Object(),true);
				proceedEnumMessage(m);
				assertFalse(a.hasGUI());
			}
		});
	}

	
	
	@Test
	public void LAUNCH_AGENT_null() {
		addMadkitArgs(LevelOption.kernelLogLevel.toString(),"ALL");
		launchTest(new AbstractAgent() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			protected void activate() {
				AbstractAgent help = new AbstractAgent();
				launchAgent(help);
				createDefaultCGR(help);
				createDefaultCGR(this);
				AbstractAgent a = new Agent();
				EnumMessage<AgentAction> m = new EnumMessage<AgentAction>(AgentAction.LAUNCH_AGENT,a,null);
				sendMessage(COMMUNITY, GROUP, ROLE, m);
				proceedEnumMessage(m);
				assertFalse(a.hasGUI());

				a = new Agent();
				m = new EnumMessage<AgentAction>(AgentAction.LAUNCH_AGENT,null,true);
				proceedEnumMessage(m);
				assertFalse(a.hasGUI());
			}
		});
	}
}
