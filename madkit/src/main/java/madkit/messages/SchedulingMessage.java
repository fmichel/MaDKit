
package madkit.messages;

import madkit.action.SchedulingAction;

/**
 * This message class could be used to interact with a Scheduler agent.
 * Here is an example :
 * 
 * <pre>
 * sendMessage(schedulerAddress, new SchedulingMessage(SchedulingAction.PAUSE));
 * </pre>
 * 
 * 
 * @author Fabien Michel
 * @since MaDKit 5.0.0.14
 * @version 0.9
 * 
 */
public class SchedulingMessage extends EnumMessage<SchedulingAction> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1433336977900865385L;

	public SchedulingMessage(SchedulingAction schedulerAction, Object... info) {
		super(schedulerAction, info);
	}

}
