
package madkit.message.hook;

import madkit.kernel.AgentAddress;
import madkit.kernel.Message;

/**
 * A message which is sent to agents that have requested a hook on {@link HookMessage.AgentActionEvent#AGENT_STARTED} or
 * {@link HookMessage.AgentActionEvent#AGENT_TERMINATED}
 * 
 * @author Fabien Michel
 * @since MadKit 5.0.0.21
 * @version 0.9
 * 
 */
public class MessageEvent extends CGREvent {

    /**
     * 
     */
    private static final long serialVersionUID = 7792908692169144580L;
    private final Message message;

    public MessageEvent(final AgentActionEvent agentAction, final Message m) {
	super(agentAction);
	message = m;
    }

    /**
     * @return the exchanged message
     */
    public Message getMessage() {
	return message;
    }

    @Override
    public AgentAddress getSourceAgent() {
	return message.getSender();
    }

    @Override
    public String toString() {
	return super.toString() + "\n\tdetails : ------> " + message;
    }

}
