package madkit.action;

import static madkit.action.AgentAction.KILL_AGENT;
import static madkit.action.AgentAction.LAUNCH_AGENT;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionGroup;
import org.controlsfx.control.action.ActionUtils;

import javafx.collections.ObservableList;
import madkit.kernel.Scheduler;
import madkit.kernel.Agent;

/**
 * 
 * Utility class for creating actions for agents.
 * 
 * 
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
	public static ActionGroup createSchedulerActionGroupFor(Scheduler<?> agent) {
		Collection<Action> actions = new ArrayList<>();
		actions.add(SchedulingAction.RUN.getFxActionFrom(agent));
		actions.add(SchedulingAction.PAUSE.getFxActionFrom(agent));
		return new ActionGroup("Simu", actions);
	}

	/**
	 * Creates an MaDKit actions group for the given agent. The action group includes actions
	 * for various MaDKit operations.
	 * @param agent the agent for which to create the action
	 * @return an action group containing the default MaDKit actions for the agent
	 */
	public static ActionGroup createMadkitActionGroupFor(Agent agent) {
		Collection<Action> actions = new ArrayList<>();
		actions.add(GlobalAction.JCONSOLE.getFXAction());
		actions.add(ActionUtils.ACTION_SEPARATOR);
		actions.add(KernelAction.COPY.newActionFor(agent));
		actions.add(KernelAction.RESTART.newActionFor(agent));
		actions.add(KernelAction.EXIT.newActionFor(agent));
		actions.add(ActionUtils.ACTION_SEPARATOR);
		// actions.add(KernelAction.CONSOLE.newActionFor(agent)); //FIXME
		return new ActionGroup("MaDKit", actions);
	}

	/**
	 * Creates an action group for the given agent. The action group includes actions
	 * for launching and killing the agent.
	 * 
	 * @param agent the agent for which to create the action group
	 * @return an action group containing the default actions for the agent
	 */
	public static ActionGroup createAgentActionGroupFor(Agent agent) {
		Collection<Action> actions = new ArrayList<>();
		try {
			if (agent.getClass().getConstructor((Class<?>[]) null) != null) {
				// actions.add(RELOAD.newActionFor(agent));
				actions.add(LAUNCH_AGENT.getActionFor(agent, agent.getClass().getName(), 0));
			}
		} catch (SecurityException | NoSuchMethodException e) {
		}
		actions.add(KILL_AGENT.getActionFor(agent, agent, 2));
		return new ActionGroup("Agent", actions);
	}

	public static List<Action> createMDKActionsListFor(Agent agent) {
		return List.of(GlobalAction.JCONSOLE.getFXAction(), ActionUtils.ACTION_SEPARATOR,
				KernelAction.COPY.newActionFor(agent), KernelAction.RESTART.newActionFor(agent),
				KernelAction.EXIT.newActionFor(agent));
	}

	public static ObservableList<Action> getMadkitActions(Agent agent) {
		return createMadkitActionGroupFor(agent).getActions();
	}

}
