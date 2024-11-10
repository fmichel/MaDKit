
package madkit.message.hook;

import madkit.kernel.Agent;

/**
 * A message which is sent to agents that have requested a hook on {@link HookMessage.AgentActionEvent#AGENT_STARTED} or
 * {@link HookMessage.AgentActionEvent#AGENT_TERMINATED}
 * 
 * @author Fabien Michel
 * @since MadKit 5.0.0.19
 * @version 0.9
 * 
 */
public class AgentLifeEvent extends HookMessage {

    /**
     * 
     */
    private static final long serialVersionUID = -5737575514238878308L;
    private final Agent source;

    public AgentLifeEvent(AgentActionEvent agentAction, Agent agent) {
	super(agentAction);
	source = agent;
    }

    /**
     * The agent which is related to this event
     * 
     * @return the source agent of the event
     */
    public Agent getSource() {
	return source;
    }

    /**
     * Return the ID of the agent, i.e. its hashCode value
     * 
     * @return the ID of the agent
     */
    String getSourceAgentID() {
	return "" + source.hashCode();
    }

    @Override
    public String toString() {
	return super.toString() + " from " + source;
    }

}
