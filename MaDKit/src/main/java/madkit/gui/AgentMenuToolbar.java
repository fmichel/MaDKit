package madkit.gui;

import javafx.scene.control.MenuBar;
import javafx.scene.layout.VBox;
import madkit.kernel.Agent;

public class AgentMenuToolbar extends VBox{

	public AgentMenuToolbar(Agent agent) {
		MenuBar menuBar = Menus.createMenuBarFor(agent);
		getChildren().add(menuBar);
	}
}
