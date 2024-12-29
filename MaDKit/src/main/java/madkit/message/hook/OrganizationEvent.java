
package madkit.message.hook;

import madkit.kernel.AgentAddress;

/**
 * A message which is sent to agents that have requested a hook on
 * {@link HookMessage.AgentActionEvent#CREATE_GROUP},
 * {@link HookMessage.AgentActionEvent#REQUEST_ROLE},
 * {@link HookMessage.AgentActionEvent#LEAVE_GROUP}, or
 * {@link HookMessage.AgentActionEvent#LEAVE_ROLE}
 * 
 * @author Fabien Michel
 * @since MadKit 5.0.0.19
 * @version 0.9
 * 
 */
public class OrganizationEvent extends CGREvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7030965928934873405L;
	private final AgentAddress source;

	/**
	 * @param agentAction
	 * @param source
	 */
	public OrganizationEvent(AgentActionEvent agentAction, AgentAddress source) {
		super(agentAction);
		this.source = source;
	}

	public AgentAddress getSourceAgent() {
		return source;
	}
}
