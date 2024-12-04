package madkit.gui.fx.menus;

import static madkit.action.AgentAction.KILL_AGENT;
import static madkit.action.AgentAction.LAUNCH_AGENT;
import static madkit.action.AgentAction.RELOAD;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionGroup;
import org.controlsfx.control.action.ActionUtils;
import org.controlsfx.control.action.ActionUtils.ActionTextBehavior;

import javafx.collections.ObservableList;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ToolBar;
import madkit.action.GlobalAction;
import madkit.action.KernelAction;
import madkit.kernel.Agent;

/**
 * @author Fabien Michel
 *
 */
public class Menus {

	static public ActionGroup createMadkitActionGroupFor(Agent agent) {
		Collection<Action> actions = new ArrayList<>();
		actions.add(GlobalAction.JCONSOLE.getFXAction());
		actions.add(ActionUtils.ACTION_SEPARATOR);
		actions.add(KernelAction.COPY.newActionFor(agent));
		actions.add(KernelAction.RESTART.newActionFor(agent));
		actions.add(KernelAction.EXIT.newActionFor(agent));
		actions.add(ActionUtils.ACTION_SEPARATOR);
//		actions.add(KernelAction.CONSOLE.newActionFor(agent)); //FIXME
		return new ActionGroup("MaDKit", actions);
	}

	static public List<Action> createMDKActionsListFor(Agent agent) {
		return List.of(GlobalAction.JCONSOLE.getFXAction(), ActionUtils.ACTION_SEPARATOR,
				KernelAction.COPY.newActionFor(agent), KernelAction.RESTART.newActionFor(agent),
				KernelAction.EXIT.newActionFor(agent));
	}

	static public ActionGroup createAgentActionGroupFor(Agent agent) {
		Collection<Action> actions = new ArrayList<>();
		try {
			if (agent.getClass().getConstructor((Class<?>[]) null) != null) {
				actions.add(RELOAD.newActionFor(agent));
				actions.add(LAUNCH_AGENT.newActionFor(agent, agent.getClass().getName(), 0));
			}
		} catch (SecurityException | NoSuchMethodException e) {
		}
		actions.add(KILL_AGENT.newActionFor(agent, agent, 2));
		return new ActionGroup("Agent", actions);
	}

	static public MenuBar createMenuBarFor(Agent agent) {
		Collection<? extends Action> actions = Arrays.asList(createMadkitActionGroupFor(agent),
				createAgentActionGroupFor(agent));
		return ActionUtils.createMenuBar(actions);
	}

	static public ToolBar createToolBarFor(Agent agent) {
//		Collection<? extends Action> actions = Arrays.asList(createMadkitActionGroupFor(agent),
//				createAgentActionGroupFor(agent));
		return ActionUtils.createToolBar(getMadkitActions(agent), ActionTextBehavior.HIDE);
	}

	static public ObservableList<Action> getMadkitActions(Agent agent) {
		return createMadkitActionGroupFor(agent).getActions();
	}

}
