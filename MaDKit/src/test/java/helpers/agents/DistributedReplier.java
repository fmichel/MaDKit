package helpers.agents;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;

import madkit.kernel.Message;
import madkit.testing.agents.ThreadedTestAgent;
import madkit.testing.agents.behaviors.ActivateDistributedCGRBehavior;
import madkit.testing.agents.behaviors.OnLiveReplierBehavior;

public class DistributedReplier extends ThreadedTestAgent
		implements ActivateDistributedCGRBehavior, OnLiveReplierBehavior {

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