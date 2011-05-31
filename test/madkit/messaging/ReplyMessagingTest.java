/**
 * 
 */
package madkit.messaging;

import static madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import madkit.kernel.Agent;
import madkit.kernel.Message;
import madkit.messages.ObjectMessage;
import test.util.JUnitBooterAgent;
/**
 * @author fab
 *
 */
public class ReplyMessagingTest extends JUnitBooterAgent{



	/**
	 * 
	 */
	private static final long serialVersionUID = -8508534747164373794L;
	Agent other = new Agent(){/**
		 * 
		 */
		private static final long serialVersionUID = 5810843908315356964L;

	protected void live() {
		requestRole("public", "system", "other",null);
		Message m = waitNextMessage();
		sendReply(m, new ObjectMessage<String>("reply1"));
		sendReply(m, new ObjectMessage<String>("hello"));
		sendReply(m, new ObjectMessage<String>("hello2"));
		m = waitNextMessage();
		sendReply(m, new ObjectMessage<String>("reply2"));
		sendReply(m, new ObjectMessage<String>("hello3"));
		sendReply(m, new ObjectMessage<String>("hello4"));
	}
	};
	Agent other2 = new Agent(){/**
		 * 
		 */
		private static final long serialVersionUID = -7267003305386748535L;

	protected void live() {
		pause(100);
		requestRole("public", "system", "other",null);
		while(true){
			pause(1000);
		}
	}
	};

	
	@Override
	public void activate() {
		super.activate();
		ReturnCode code;
		code = testAgent.createGroup("public", "system", false,null);
		assertEquals(SUCCESS,code);
		code = testAgent.requestRole("public", "system", "site",null);
		assertEquals(SUCCESS,code);
		assertEquals(SUCCESS,launchAgent(other));
		assertEquals(SUCCESS,launchAgent(other2));
		
		pause(400);
		
		ObjectMessage<String> m = (ObjectMessage<String>) other2.sendMessageAndWaitForReply(other2.getAgentWithRole("public", "system", "other"), new Message());
		assertEquals("reply1",m.getContent());
		assertTrue("hello".equals(((ObjectMessage<String>) other2.waitNextMessage()).getContent()));
		assertEquals("hello2",((ObjectMessage<String>) other2.waitNextMessage()).getContent());
		m = (ObjectMessage<String>) other2.sendMessageAndWaitForReply("public", "system", "other", new Message());
		assertEquals("reply2",m.getContent());
		assertEquals("hello3",((ObjectMessage<String>) other2.waitNextMessage()).getContent());
		assertEquals("hello4",((ObjectMessage<String>) other2.waitNextMessage()).getContent());

		//		Message m = fakeAgent.sendMessage("public", "system", "other", new Message());
		//		assertEquals(NO_RECIPIENT_FOUND,code);

	}

}
