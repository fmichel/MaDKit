package madkit.messages;

import madkit.action.SchedulingAction;

public class SchedulingMessage extends CommandMessage<SchedulingAction> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1433336977900865385L;

	public SchedulingMessage(SchedulingAction schedulerAction, Object... info) {
		super(schedulerAction,info);
	}

}
