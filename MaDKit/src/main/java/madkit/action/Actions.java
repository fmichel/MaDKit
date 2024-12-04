package madkit.action;

import java.util.ArrayList;
import java.util.Collection;

import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionGroup;

import madkit.kernel.AbstractScheduler;

/**
 * @author Fabien Michel
 *
 */
public class Actions {

	private Actions() {
		throw new IllegalAccessError();
	}

	/**
	 * Creates an action group for the given scheduler agent. The action group
	 * includes actions for running and pausing the scheduler.
	 * 
	 * @param agent the scheduler agent for which to create the action group
	 * @return an ActionGroup containing the actions for the scheduler
	 */
	public static ActionGroup createSchedulerActionGroupFor(AbstractScheduler<?> agent) {
		Collection<Action> actions = new ArrayList<>();
		actions.add(SchedulingAction.RUN.getFxActionFrom(agent));
		actions.add(SchedulingAction.PAUSE.getFxActionFrom(agent));
		return new ActionGroup("Simu", actions);
	}

}
