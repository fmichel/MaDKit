
package madkit.message.hook;

import madkit.kernel.AgentAddress;

/**
 * Root class of messages which are sent to agents that have requested hooks to
 * the kernel
 * 
 * @author Fabien Michel
 * @since MadKit 5.0.0.19
 * @version 0.9
 * 
 */
public abstract class CGREvent extends HookMessage {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6722477889792374461L;

	CGREvent(AgentActionEvent event) {
		super(event);
	}

	/**
	 * Returns the address of the agent that did the request.
	 * 
	 * @return the name of the agent that triggers the event
	 */
	public abstract AgentAddress getSourceAgent();

	@Override
	public String toString() {
		return super.toString() + " from " + getSourceAgent();
	}

}