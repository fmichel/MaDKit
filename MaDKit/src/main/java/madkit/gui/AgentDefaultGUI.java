
package madkit.gui;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import madkit.kernel.Agent;
import madkit.kernel.FXAgentStage;

/**
 * This class provides a default GUI for an agent using JavaFX. To create a
 * custom GUI for an agent, extend this class and override the methods to
 * customize the different parts of the GUI.
 */
public class AgentDefaultGUI {
	
	private Agent agent;
	private FXAgentStage stage;
	private Scene scene;
	private BorderPane mainPane;

	/**
	 * Constructs a default GUI for the specified agent. Override this constructor
	 * to customize the initialization of the GUI.
	 *
	 * @param agent the agent for which the GUI is created
	 */
	public AgentDefaultGUI(Agent agent) {
		this.agent = agent;
		FXManager.runAndWait(() -> {
			onInitialize();
			stage = new FXAgentStage(agent);
			mainPane = new BorderPane();
			mainPane.setTop(createTopNode());
			mainPane.setLeft(createLeftNode());
			mainPane.setCenter(createCenterNode());
			mainPane.setRight(createRightNode());
			mainPane.setBottom(createBottomNode());
			scene = new Scene(mainPane);
			stage.setScene(scene);
			stage.show();
		});
	}

	/**
	 * Called first when the GUI is initialized. Override this method to perform
	 * custom initialization. 
	 */
	protected void onInitialize() {
		// to be overridden
	}

	/**
	 * Creates the top node of the GUI. Override this method to customize the top
	 * part of the GUI.
	 *
	 * @return the top node
	 */
	protected Node createTopNode() {
		return new AgentMenuToolbar(getAgent());
	}

	/**
	 * Creates the left node of the GUI. Override this method to customize the left
	 * part of the GUI.
	 *
	 * @return the left node, or null if not used
	 */
	protected Node createLeftNode() {
		return null;
	}

	/**
	 * Creates the right node of the GUI. Override this method to customize the
	 * right part of the GUI.
	 *
	 * @return the right node
	 */
	protected Node createRightNode() {
		return PropertySheetFactory.getTitledPaneSheet(getAgent());
	}

	/**
	 * Creates the bottom node of the GUI. Override this method to customize the
	 * bottom part of the GUI.
	 *
	 * @return the bottom node
	 */
	protected Node createBottomNode() {
		return ToolBars.createToolBarFor(getAgent());
	}

	/**
	 * Creates the center node of the GUI. Override this method to customize the
	 * center part of the GUI.
	 *
	 * @return the center node
	 */
	protected Node createCenterNode() {
		return new AgentLogArea(getAgent());
	}

	/**
	 * Returns the stage of the viewer agent.
	 *
	 * @return the stage
	 */
	public FXAgentStage getStage() {
		return stage;
	}

	/**
	 * Returns the main pane of the viewer agent.
	 *
	 * @return the mainPane
	 */
	public BorderPane getMainPane() {
		return mainPane;
	}

	/**
	 * Returns the scene of the viewer agent.
	 *
	 * @return the scene
	 */
	public Scene getScene() {
		return scene;
	}

	/**
	 * Returns the agent associated with this GUI.
	 *
	 * @return the agent
	 */
	public Agent getAgent() {
		return agent;
	}
}
