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
	
	static public ActionGroup createSchedulerActionGroupFor(AbstractScheduler<?> agent) {
		Collection<Action> actions = new ArrayList<>();
		actions.add(SchedulingAction.RUN.getFxActionFrom(agent));
		actions.add(SchedulingAction.PAUSE.getFxActionFrom(agent));
		return new ActionGroup("Simu", actions);
	}

	

}
