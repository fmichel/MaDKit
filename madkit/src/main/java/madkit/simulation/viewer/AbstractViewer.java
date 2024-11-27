/*******************************************************************************
 * Copyright (c) 2021, MaDKit Team
 *
 * This software is a computer program whose purpose is to
 * provide a lightweight Java API for developing and simulating 
 * Multi-Agent Systems (MAS) using an organizational perspective.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 *******************************************************************************/
package madkit.simulation.viewer;

import java.awt.event.KeyEvent;

import org.controlsfx.control.PropertySheet;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionGroup;
import org.controlsfx.control.action.ActionUtils;
import org.controlsfx.control.action.ActionUtils.ActionTextBehavior;

import static madkit.simulation.DefaultOrganization.SCHEDULER_ROLE;
import static madkit.simulation.DefaultOrganization.VIEWER_ROLE;

import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import madkit.action.ActionData;
import madkit.action.SchedulingAction;
import madkit.kernel.Watcher;
import madkit.gui.fx.FXManager;
import madkit.gui.fx.MDKFXActionCheck;
import madkit.gui.fx.menus.Menus;
import madkit.kernel.AgentFxStage;
import madkit.messages.SchedulingMessage;
import madkit.simulation.ParametersSheetFactory;
import madkit.simulation.PropertySheetAgents;

/**
 * A very basic simulation viewer agent. This class defines a panel for the
 * simulation renderingOff and two modes of renderingOff: Synchronous and
 * asynchronous. The synchronous mode ensures that each simulation frame is
 * displayed. That means that the scheduler will wait the end of the
 * renderingOff activity to proceed to the next activator, waiting for the swing
 * thread to ends. this is not the case with the asynchronous mode so that the
 * whole simulation process goes faster because some simulation states will not
 * be displayed. An <code>observe</code> method is already defined and intended
 * to be called by scheduler agents to trigger the renderingOff. This class
 * could be thus extended to reuse the renderingOff call mechanism which is
 * defined in here.
 * 
 * @author Fabien Michel
 * @since MaDKit 5.4
 */
public abstract class AbstractViewer extends Watcher {
	private MDKFXActionCheck renderingOff;
	private MDKFXActionCheck synchroPainting;
	private int renderingInterval = 1;
	private int counter = 1;

	private AgentFxStage stage;
	private MenuBar menuBar;
	private BorderPane mainPane;
	private Node centralNode;
	private Scene scene;

	@Override
	protected void onActivation() {
		setupGUI();
		setRenderingInterval(1);
		requestRole(getCommunity(), getEngineGroup(), VIEWER_ROLE);
	}

	@Override
	protected void onLiving() {
		waitNextMessage();
	}

	@Override
	protected void onEnding() {
		super.onEnding();
		if (getAgentsWithRole(getCommunity(), getEngineGroup(), VIEWER_ROLE).isEmpty()) {// FIXME
			send(new SchedulingMessage(SchedulingAction.SHUTDOWN), getCommunity(), getEngineGroup(), SCHEDULER_ROLE);
		}
	}

	private void initActions() {
		// initRenderingIntervalComboBox(messages.getString("UPDATE_INTERVAL"));

		AnimationTimer animationTimer = new AnimationTimer() {
			@Override
			public void handle(long now) {
				if (!renderingOff.isSelected() && isAlive()) {
					render();
				}
			}
		};

		renderingOff = new MDKFXActionCheck(new ActionData("DISABLE", KeyEvent.VK_A), null);
		renderingOff.setSelected(false);

		synchroPainting = new MDKFXActionCheck(new ActionData("SYNCHRO_PAINTING", KeyEvent.VK_Z), ae -> {
			if (synchroPainting.isSelected()) {
				animationTimer.stop();
			} else {
				animationTimer.start();
			}
		});

		synchroPainting.setSelected(true);

	}

	public VBox createSimulationPropertiesNode() {
		return ParametersSheetFactory.getVBoxProperties(
				getModel(), 
				getEnvironment(), 
				getScheduler(), 
				getSimuEngine(),
				this);
	}

	/**
	 * Intended to be invoked by a scheduler's activator for triggering the
	 * rendering. This method activates the rendering either synchronously or
	 * asynchronously depending on {@link #isSynchronousPainting()}.
	 */
	@Override
	public void setupGUI() {
		FXManager.runAndWait(() -> {
			stage = new AgentFxStage(this);
			initActions();
			VBox root = new VBox();
			menuBar = new MenuBar();
			Menu menu = new Menu("rendering");
			menu.getItems().add(ActionUtils.createCheckMenuItem(synchroPainting));
			menu.getItems().add(ActionUtils.createCheckMenuItem(renderingOff));
			menuBar.getMenus().add(menu);
			root.getChildren().add(menuBar);
			root.getChildren().add(createToolBar());

			setMainPane(new BorderPane());

//			PropertySheet engineSheet = getSimuEngine().getParametersSheet();
			setCentralNode(createCentralNode());
			getCentralNode().setStyle("-fx-border-style: solid inside;" + "-fx-border-width: 2;" + "-fx-border-insets: 5;"
					+ "-fx-border-radius: 25;" + "-fx-border-color: blue;");

			root.getChildren().add(getMainPane());
			BorderPane.setMargin(centralNode, new Insets(20, 20, 20, 20));

			getMainPane().setCenter(getCentralNode());
			getMainPane().setRight(createSimulationPropertiesNode());
//			getMainPane().setRight(ParametersSheetFactory.getTitledPaneSheet(this));

			scene = new Scene(root);
			getMainPane().prefHeightProperty().bind(getScene().heightProperty());
			stage.setScene(getScene());
			stage.show();
		});
//		stage.setFullScreen(true);
	}

	protected ToolBar createToolBar() {
		ToolBar toolBar = Menus.createToolBarFor(this);
//		toolBar.getItems().addAll(Menus.createMDKActionsListFor(this).toArray());
//		toolBar.getItems().addAll(Menus.createToolBarFor(this).getItems());
		toolBar.getItems().add(ActionUtils.createToggleButton(renderingOff, ActionTextBehavior.HIDE));
		toolBar.getItems().add(ActionUtils.createToggleButton(synchroPainting, ActionTextBehavior.HIDE));
//		toolBar.getItems().addAll(getScheduler().getActionListFor(this));

		toolBar.getItems().addAll(getScheduler().getToolBar().getItems());

		ActionGroup ag = Menus.createAgentActionGroupFor(this);

		for (Action action : ag.getActions()) {
			toolBar.getItems().add(ActionUtils.createButton(action, ActionTextBehavior.HIDE));
		}

		Button b = new Button("RESET");
		b.setOnAction(e -> {
			getScheduler().onStart();
		});
		toolBar.getItems().add(b);
		return toolBar;
	}

	/**
	 * @return the stage
	 */
	public AgentFxStage getStage() {
		return stage;
	}

	public MenuBar getMenuBar() {
		return menuBar;
	}

	public void setMenuBar(MenuBar menuBar) {
		this.menuBar = menuBar;
	}

	/**
	 * @return
	 */
	protected abstract Node createCentralNode();

//	protected void observe() {
//		if (synchroPainting.isSelected() && !renderingOff.isSelected()){// && isAlive()) {
//			if (counter > renderingInterval) {
//				FXManager.runAndWait(this::render);
//				counter = 2;
//			} else {
//				counter++;
//			}
//		}
//	}

	protected void observe() {
		if (synchroPainting.isSelected() && !renderingOff.isSelected()){// && isAlive()) {
				FXManager.runAndWait(this::render);
		}
	}

	@Override
	public void onSimuStartup() {
		observe();
	}

	/**
	 * @return
	 */
	/**
	 * Override this method to do the renderingOff in the agent's panel. This method
	 * is automatically called when the <code>observe</code> method is triggered by
	 * a Scheduler
	 * 
	 * @param graphicsContext
	 */
	protected abstract void render();

	public MDKFXActionCheck getRenderingOff() {
		return renderingOff;
	}

	/**
	 * @return the synchroPainting
	 */
	public MDKFXActionCheck getSynchroPainting() {
		return synchroPainting;
	}

	/**
	 * @return <code>true</code> if the renderingOff activity is activated.
	 */
	public boolean isRendering() {
		return !renderingOff.isSelected();
	}

	/**
	 * Enable or disable the renderingOff activity
	 */
	public void setRenderingOff(boolean activated) {
		renderingOff.setSelected(activated);
	}

	/**
	 * Enable or disable the renderingOff activity
	 */
	public void setSynchroPainting(boolean activated) {
		synchroPainting.setSelected(activated);
	}

	public void setRenderingInterval(int interval) {
		renderingInterval = interval > 0 ? interval : 1;
		// if ((int) comboBox.getSelectedItem() != renderingInterval) {
		// comboBox.setSelectedItem(renderingInterval);
		// }
	}

	/**
	 * @return the mainPane
	 */
	public BorderPane getMainPane() {
		return mainPane;
	}

	/**
	 * @param mainPane the mainPane to set
	 */
	public void setMainPane(BorderPane mainPane) {
		this.mainPane = mainPane;
	}

	/**
	 * @return the centralNode
	 */
	public Node getCentralNode() {
		return centralNode;
	}

	/**
	 * @param centralNode the centralNode to set
	 */
	public void setCentralNode(Node centralNode) {
		this.centralNode = centralNode;
	}

	/**
	 * @return the scene
	 */
	public Scene getScene() {
		return scene;
	}

	private Class<?>[] getObservedClasses() {
		PropertySheetAgents annotation = getClass().getDeclaredAnnotation(PropertySheetAgents.class);
		if (annotation == null) {
			return new Class<?>[0];
		}
		return annotation.classesToBuildUIWith();
	}

}
