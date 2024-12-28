package madkit.gui;

import org.controlsfx.control.action.ActionUtils;
import org.controlsfx.control.action.ActionUtils.ActionTextBehavior;

import javafx.scene.control.ToolBar;
import madkit.action.Actions;
import madkit.kernel.Agent;

public class ToolBars {
	
	private ToolBars() {
		throw new IllegalAccessError();
	}

	/**
	 * Creates a tool bar for the given agent. The tool bar includes actions for the MaDKit kernel
	 * @param agent the agent for which to create the tool bar
	 * @return a tool bar containing the default actions for the agent
	 */
	public static ToolBar createToolBarFor(Agent agent) {
			return ActionUtils.createToolBar(Actions.getMadkitActions(agent), ActionTextBehavior.HIDE);
		}

}
