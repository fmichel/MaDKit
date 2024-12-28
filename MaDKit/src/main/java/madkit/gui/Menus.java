package madkit.gui;

import java.util.Arrays;
import java.util.Collection;

import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;

import javafx.scene.control.MenuBar;
import madkit.action.Actions;
import madkit.kernel.Agent;

/**
 * Utility class to create menus with default actions for a given agent.
 * 
 * @since MaDKit 6.0
 * @author Fabien Michel
 *
 */
public class Menus {

	private Menus() {
		throw new IllegalAccessError();
	}

	/**
	 * Creates a menu bar for the given agent.
	 * 
	 * @param agent the agent for which to create the menu bar
	 * @return a menu bar containing the default actions for the agent
	 */
	public static MenuBar createMenuBarFor(Agent agent) {
		Collection<? extends Action> actions = Arrays.asList(Actions.createMadkitActionGroupFor(agent),
				Actions.createAgentActionGroupFor(agent));
		return ActionUtils.createMenuBar(actions);
	}

}
