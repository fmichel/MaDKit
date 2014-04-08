package madkit.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;

import madkit.kernel.AbstractAgent;
import madkit.kernel.JunitMadkit;
import madkit.kernel.Madkit.LevelOption;
import madkit.kernel.MadkitClassLoader;

import org.junit.Test;


public class KernelActionTest extends JunitMadkit{

	@Test
	public final void test() {//TODO no test here...
		launchTest(new AbstractAgent(){
			@Override
			protected void activate() {
				try {
					MadkitClassLoader.loadUrl(new File("test/MaDKit-ping-pong-2.0.agents.jar").toURI().toURL());
				} catch (Exception e) {
					e.printStackTrace();
					fail();
				}
				pause(100);
			}
		});
	}
	
	@Test
	public void debugModeTest() {
		mkArgs.clear();
//		addMadkitArgs(LevelOption.agentLogLevel.toString(), Level.ALL.toString());
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				assertEquals("INFO", getMadkitProperty(LevelOption.agentLogLevel.name()));
				GlobalAction.DEBUG.actionPerformed(null);
				assertEquals("ALL", getMadkitProperty(LevelOption.agentLogLevel.name()));
				GlobalAction.DEBUG.actionPerformed(null);
				assertEquals("INFO", getMadkitProperty(LevelOption.agentLogLevel.name()));
			}
		});
	}

}
