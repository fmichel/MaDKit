package madkit.messaging;

import java.lang.reflect.InvocationTargetException;

import madkit.kernel.Message;
import madkit.testing.agents.ThreadedTestAgent;
import madkit.testing.agents.behaviors.ActivateCGRBehavior;
import madkit.testing.agents.behaviors.OnLiveReplierBehavior;

/**
 *
 *
 */
public class ForEverReplierAgent extends ThreadedTestAgent implements ActivateCGRBehavior, OnLiveReplierBehavior {

	private Class<?> msgType;

	/**
	 * @param msgType the class of the message to reply to
	 * 
	 */
	public ForEverReplierAgent(Class<? extends madkit.kernel.Message> msgType) {
		this.msgType = msgType;
	}

	/**
	 * 
	 */
	public ForEverReplierAgent() {
		this.msgType = madkit.kernel.Message.class;
	}

	@Override
	public void waitMessageAndReply() {
		Message waitNextMessage = waitNextMessage();
		sleep(100);
		try {
			Message reply = (Message) msgType.getConstructor().newInstance();
			reply(reply, waitNextMessage);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException e) {
			e.printStackTrace();
		}
	}

}