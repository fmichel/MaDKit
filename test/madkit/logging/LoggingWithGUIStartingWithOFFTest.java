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
			/**
			 * 
			 */
			private static final long	serialVersionUID	= -5533331605897442684L;

			@Override
			protected void activate() {
				assertEquals(SUCCESS , launchAgent(new Agent() {
					/**
					 * 
					 */
					private static final long	serialVersionUID	= -6872413530032541154L;

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
			/**
			 * 
			 */
			private static final long	serialVersionUID	= 2348286458650201697L;

			@Override
			protected void activate() {
				assertEquals(SUCCESS , launchAgent(new Agent() {
					/**
					 * 
					 */
					private static final long	serialVersionUID	= -2255594108124349098L;

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
			/**
			 * 
			 */
			private static final long	serialVersionUID	= 8883387648700217528L;

			@Override
			protected void activate() {
				assertEquals(AGENT_CRASH, launchAgent(new Agent() {
					/**
					 * 
					 */
					private static final long	serialVersionUID	= 5427250289865506263L;

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
			/**
			 * 
			 */
			private static final long	serialVersionUID	= -5513234138027253368L;

			@Override
			protected void activate() {
				assertEquals(AGENT_CRASH, launchAgent(new Agent() {
					/**
					 * 
					 */
					private static final long	serialVersionUID	= -5666892212944754895L;

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
			/**
			 * 
			 */
			private static final long	serialVersionUID	= -1729302363521846469L;

			@Override
			protected void activate() {
				assertEquals(SUCCESS , launchAgent(new Agent() {
					/**
					 * 
					 */
					private static final long	serialVersionUID	= 3368927756521472637L;

					protected void activate() {
						getLogger().setWarningLogLevel(Level.ALL);
					}
				}, true));
			}
		});
	}
}