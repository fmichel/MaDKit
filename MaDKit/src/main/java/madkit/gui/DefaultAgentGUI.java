/*******************************************************************************
 * MaDKit - Multi-agent systems Development Kit 
 * 
 * Copyright (c) 1998-2025 Fabien Michel, Olivier Gutknecht, Jacques Ferber...
 * 
 * This software is a computer program whose purpose is to
 * provide a lightweight Java API for developing and simulating 
 * Multi-Agent Systems (MAS) using an organizational perspective.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.You can use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty and the software's author, the holder of the
 * economic rights, and the successive licensors have only limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading, using, modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean that it is complicated to manipulate, and that also
 * therefore means that it is reserved for developers and experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and, more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 *******************************************************************************/

package madkit.gui;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import madkit.kernel.Agent;
import madkit.kernel.FXAgentStage;

/**
 * This class provides a default GUI for an agent using JavaFX. To create a custom GUI for
 * an agent, extend this class and override the methods to customize the different parts
 * of the GUI.
 */
public class DefaultAgentGUI {

	private Agent agent;
	private FXAgentStage stage;
	private Scene scene;
	private BorderPane mainPane;
	private ToolBar toolbar;

	/**
	 * Constructs a default GUI for the specified agent. Override this constructor to
	 * customize the initialization of the GUI.
	 *
	 * @param agent the agent for which the GUI is created
	 */
	public DefaultAgentGUI(Agent agent) {
		this.agent = agent;
		FXExecutor.runAndWait(() -> {
			onInitialize();
			stage = new FXAgentStage(agent);
			mainPane = new BorderPane();
			mainPane.setTop(createTopNode());
			mainPane.setLeft(createLeftNode());
			mainPane.setCenter(createCenterNode());
			mainPane.setRight(createRightNode());
			toolbar = createToolBar();
			mainPane.setBottom(createBottomNode());
			scene = new Scene(mainPane);
			stage.setScene(scene);
			stage.show();
		});
	}

	/**
	 * Called first when the GUI is initialized. Override this method to perform custom
	 * initialization.
	 */
	protected void onInitialize() {
		// to be overridden
	}

	/**
	 * Creates the top node of the GUI. Override this method to customize the top part of the
	 * GUI.
	 *
	 * @return the top node
	 */
	protected Node createTopNode() {
		return new AgentMenuToolbar(getAgent());
	}

	/**
	 * Creates the left node of the GUI. Override this method to customize the left part of
	 * the GUI.
	 *
	 * @return the left node, or null if not used
	 */
	protected Node createLeftNode() {
		return null;
	}

	/**
	 * Creates the right node of the GUI. Override this method to customize the right part of
	 * the GUI.
	 *
	 * @return the right node
	 */
	protected Node createRightNode() {
		return PropertySheetFactory.getTitledPaneSheet(getAgent());
	}

	/**
	 * Creates the bottom node of the GUI. Override this method to customize the bottom part
	 * of the GUI.
	 *
	 * @return the bottom node
	 */
	protected Node createBottomNode() {
		return getToolBar();
	}

	/**
	 * Creates a toolbar. Override this method to customize the toolbar. By default it will be
	 * used for creating the bottom node.
	 * 
	 * @return the toolbar
	 */
	protected ToolBar createToolBar() {
		return ToolBars.createToolBarFor(getAgent());
	}

	/**
	 * Creates the center node of the GUI. Override this method to customize the center part
	 * of the GUI.
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
	 * Returns the agent bounded to this GUI.
	 *
	 * @return the agent
	 */
	public Agent getAgent() {
		return agent;
	}

	/**
	 * Returns the tool bar of the GUI
	 * 
	 * @return the tool bar
	 */
	public ToolBar getToolBar() {
		return toolbar;
	}
}
