package madkit.messages;

import madkit.gui.actions.SchedulerAction;

public class SchedulingMessage extends CommandMessage<SchedulerAction> {

	public SchedulingMessage(SchedulerAction schedulerAction, Object[] info) {
		super(schedulerAction,info);
	}

}
