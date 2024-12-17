
package madkit.message.hook;

import madkit.messages.ObjectMessage;

/**
 * This message could be used to request a kernel's hook on agent actions so
 * that the sender will be kept informed when an agent performed particular
 * action.
 * 
 * @author Fabien Michel
 * @since MaDKit 5.0.0.14
 * @version 0.91
 * @see AgentActionEvent
 * 
 */
public class HookMessage extends ObjectMessage<HookMessage.AgentActionEvent> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3008390114345525272L;

	/**
	 * This message should be used to request or release a hook on an agent action.
	 * The message should be sent to the kernel (which is the manager of the SYSTEM
	 * group in the local community), here is an example :
	 * 
	 * <pre>
	 * <code>
	 * sendMessage(
	 * 	LocalCommunity.NAME,
	 * 	LocalCommunity.Groups.SYSTEM, 
	 * 	Organization.GROUP_MANAGER_ROLE,
	 * 	new HookMessage(AgentActionEvent.REQUEST_ROLE));
	 * </code>
	 * </pre>
	 * 
	 * In this example, the sender will be informed by the kernel of all successful
	 * requestRole operation made by the agents. This information will be
	 * transmitted using a subclass of HookMessage depending on the nature of the
	 * event. That is, {@link OrganizationEvent}, {@link MessageEvent} or
	 * {@link AgentLifeEvent} messages will be sent by the kernel according to the
	 * type of the hook which has been requested.
	 * <p>
	 * To give up the hook, just send to the kernel another message built with the
	 * same action and it will remove the sender from the subscriber list.
	 * 
	 * @param hookType the action event type to monitor
	 * @see AgentActionEvent
	 */
	public HookMessage(HookMessage.AgentActionEvent hookType) {
		super(hookType);
	}

	/**
	 * Enumeration representing agent actions that could be monitored using hooks.
	 *
	 */
	public enum AgentActionEvent {
		CREATE_GROUP, REQUEST_ROLE, LEAVE_GROUP, LEAVE_ROLE, SEND_MESSAGE, BROADCAST_MESSAGE, AGENT_STARTED,
		AGENT_TERMINATED
	}
}
