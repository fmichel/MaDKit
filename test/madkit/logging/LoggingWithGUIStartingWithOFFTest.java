package madkit.logging;

import static madkit.kernel.AbstractAgent.ReturnCode.AGENT_CRASH;
import static madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static org.junit.Assert.assertEquals;

import java.util.logging.Level;

import madkit.kernel.AbstractAgent;
import madkit.kernel.Agent;
import madkit.kernel.JunitMadkit;
import madkit.kernel.Madkit.LevelOption;

import org.junit.Before;
import org.junit.Test;

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