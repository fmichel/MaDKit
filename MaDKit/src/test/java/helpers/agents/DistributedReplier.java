package helpers.agents;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;

import madkit.kernel.Message;
import madkit.test.agents.ThreadedTestAgent;
import madkit.test.agents.behaviors.ActivateDistributedCGR;
import madkit.test.agents.behaviors.LiveReplier;

public class DistributedReplier extends ThreadedTestAgent implements ActivateDistributedCGR, LiveReplier {

	private Class<? extends Message> msgType;

	/**
	 * @param msgType the class of the message to reply to
	 * 
	 */
	public DistributedReplier(Class<? extends Message> msgType) {
		this.msgType = msgType;
	}

	@Override
	protected void onActivation() {
		super.onActivation();
		getLogger().setLevel(Level.ALL);
	}

	@Override
	protected void onEnd() {
		super.onEnd();
	}

	/**
	 * 
	 */
	public DistributedReplier() {
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
