/**
 * 
 */
package madkit.gui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static madkit.kernel.AbstractAgent.ReturnCode.*;

import madkit.kernel.Agent;
import test.util.JUnitBooterAgent;
/**
 * @author fab
 *
 */
@SuppressWarnings("serial")
public class GUITest extends JUnitBooterAgent{



	Agent other = new Agent(){protected void live() {
		requestRole("public", "system", "other",null);
		waitNextMessage();
	}
	};
	Agent other2 = new Agent(){protected void activate() {
		pause(100);
		requestRole("public", "system", "other",null);
		
		for (int i = 0; i < 10; i++) {
			//assert does not work in anonymous class
			//		ObjectMessage<String> m = (ObjectMessage<String>) sendMessageAndWaitForReply(getAgentWithRole("public", "system", "other"), new Message());
			//		assertEquals("reply1",m.getContent());
			//		assertTrue("hello".equals(((ObjectMessage<String>) waitNextMessage()).getContent()));
			//		assertEquals("hello2",((ObjectMessage<String>) waitNextMessage()).getContent());
			//		m = (ObjectMessage<String>) sendMessageAndWaitForReply("public", "system", "other", new Message());
			//		assertEquals("reply2",m.getContent());
			//		assertEquals("hello3",((ObjectMessage<String>) waitNextMessage()).getContent());
			//		assertEquals("hello4",((ObjectMessage<String>) waitNextMessage()).getContent());
			pause(1000);
			if (logger != null)
				logger.info("living");
		}
	}
	};
	/* (non-Javadoc)
	 * @see madkit.kernel.AbstractMadkitBooter#activate()
	 */
	@Override
	public void activate() {
		testAgent = launchAgent("madkit.kernel.AbstractAgent",true);
		assertNotNull(testAgent);
		ReturnCode code;
		code = testAgent.createGroup("public", "system", false,null);
		assertEquals(SUCCESS,code);
		code = testAgent.requestRole("public", "system", "site",null);
		assertEquals(SUCCESS,code);
		assertEquals(SUCCESS,testAgent.launchAgent(other,true));
		assertEquals(SUCCESS,launchAgent(other2,true));// will end automatically when launch finished
		
//		while(getAgentState(other2) != 4)
//			pause(10);
	}

}
