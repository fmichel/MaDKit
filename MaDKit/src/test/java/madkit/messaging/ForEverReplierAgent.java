package madkit.messaging;

import java.lang.reflect.InvocationTargetException;

import madkit.kernel.Message;
import madkit.test.agents.ThreadedTestAgent;
import madkit.test.agents.behaviors.ActivateCGR;
import madkit.test.agents.behaviors.LiveReplier;

/**
 *
 *
 */
public class ForEverReplierAgent extends ThreadedTestAgent implements ActivateCGR, LiveReplier {

	private Class<? extends Message> msgType;

	/**
	 * @param msgType the class of the message to reply to
	 * 
	 */
	public ForEverReplierAgent(Class<? extends Message> msgType) {
		this.msgType = msgType;
	}

	/**
	 * 
	 */
	public ForEverReplierAgent() {
		this(Message.class);
	}

	@Override
	public Message createNewMessage() {
		try {
			return msgType.getConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		return null;
	}

}
