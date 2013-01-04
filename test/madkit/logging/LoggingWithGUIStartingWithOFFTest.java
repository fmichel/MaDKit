package madkit.logging;

import static madkit.kernel.AbstractAgent.ReturnCode.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

import java.util.logging.Level;

import org.junit.Before;
import org.junit.Test;

import madkit.kernel.AbstractAgent;
import madkit.kernel.Agent;
import madkit.kernel.AgentAddress;
import madkit.kernel.JunitMadkit;
import madkit.kernel.Madkit.LevelOption;
import madkit.kernel.Message;
import madkit.message.StringMessage;

public class LoggingWithGUIStartingWithOFFTest extends JunitMadkit {

	@Before
	public void init(){
		addMadkitArgs(LevelOption.agentLogLevel.toString(),Level.OFF.toString());
	}
	@Test
	public void setNameTest() {
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				assertEquals(SUCCESS , launchAgent(new Agent() {
					protected void activate() {
						setName("Test");
					}
				}, true));
			}
		});
	}

	@Test
	public void setLogLevelInLife() {
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				assertEquals(SUCCESS , launchAgent(new Agent() {
					protected void activate() {
						setLogLevel(Level.ALL);
					}
				}, true));
			}
		});
	}

	@Test
	public void setLogLevelNullInLife() {
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				assertEquals(AGENT_CRASH, launchAgent(new Agent() {
					protected void activate() {
						setLogLevel(null);
					}
				}, true));
			}
		});
	}

	@Test
	public void setLogWarningLevelNullInLife() {
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				assertEquals(AGENT_CRASH, launchAgent(new Agent() {
					protected void activate() {
						getLogger().setWarningLogLevel(null);
					}
				}, true));
			}
		});
	}

	@Test
	public void setLogWarningLevelInLife() {
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				assertEquals(SUCCESS , launchAgent(new Agent() {
					protected void activate() {
						getLogger().setWarningLogLevel(Level.ALL);
					}
				}, true));
			}
		});
	}
}