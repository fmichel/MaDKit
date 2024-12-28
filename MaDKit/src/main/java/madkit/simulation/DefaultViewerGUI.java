package madkit.simulation;

import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionGroup;
import org.controlsfx.control.action.ActionUtils;
import org.controlsfx.control.action.ActionUtils.ActionTextBehavior;

import java.awt.event.KeyEvent;
import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import madkit.action.ActionData;
import madkit.action.Actions;
import madkit.gui.FXActionCheck;
import madkit.gui.FXManager;
import madkit.gui.ToolBars;
import madkit.kernel.FxAgentStage;

/**
 * This class provides a default GUI for a viewer agent. It creates a JavaFX stage
 * with a menu bar, a toolbar, a central node and a right node.
 * <p>
 * The central node is created by the {@link #createCentralNode()} method, which has to be
 * implemented by the subclass. 
 * <p>
 * The right node is created by the {@link #createRightNode()} method, which can be
 * overridden to provide a custom right node. By default, it creates a VBox containing
 * the simulation properties using the {@link ParametersSheetFactory}.
 * <p>
 * The toolbar is created by the {@link #createToolBar()} method, which can be overridden
 * to provide a custom toolbar. By default, it creates a toolbar with the viewer agent's actions
 * and the scheduler's actions.
 * <p>
 * The rendering is managed by the {@link #requestRendering()} method, which is called by the
 * viewer agent when it wants to render the simulation state. This method triggers the {@link Viewer#render()}
 * in the JavaFX thread, depending on the state of the GUI such as the synchronous painting mode.
 * <p>
 * The synchronous painting mode can be activated or deactivated by the user. When activated, the rendering
 * is done for each simulation step and blocks the simulation process. When deactivated, the rendering is done without
 * blocking the simulation process, at a rate corresponding to the complexity of the rendering.
 *  
 */
public abstract class DefaultViewerGUI {

	private Viewer viewer;
	private FxAgentStage stage;
	private FXActionCheck renderingOff;
	private FXActionCheck synchroPainting;
	private int renderingInterval = 1;
	private int counter = 2;

	private MenuBar menuBar;
	private BorderPane mainPane;
	private Node centralNode;
	private Scene scene;

	/**
	 * Creates a new DefaultViewerGUI for the given viewer.
	 * @param viewer the viewer agent
	 */
	protected DefaultViewerGUI(Viewer viewer) {
		FXManager.runAndWait(() -> {
			this.viewer = viewer;
			stage = new FxAgentStage(viewer);
			initRenderingActions();

			VBox root = new VBox();
			menuBar = new MenuBar();
			Menu menu = new Menu("rendering");
			menu.getItems().add(ActionUtils.createCheckMenuItem(synchroPainting));
			menu.getItems().add(ActionUtils.createCheckMenuItem(renderingOff));
			menuBar.getMenus().add(menu);
			root.getChildren().add(menuBar);
			root.getChildren().add(createToolBar());

			setMainPane(new BorderPane());

			setCentralNode(createCentralNode());
			getCentralNode().setStyle("-fx-border-style: solid inside;" + "-fx-border-width: 2;" + "-fx-border-insets: 5;"
					+ "-fx-border-radius: 25;" + "-fx-border-color: blue;");

			getMainPane().setCenter(getCentralNode());
			root.getChildren().add(getMainPane());
			BorderPane.setMargin(centralNode, new Insets(20, 20, 20, 20));

			getMainPane().setRight(createRightNode());

			scene = new Scene(root);
			getMainPane().prefHeightProperty().bind(getScene().heightProperty());
			stage.setScene(getScene());
			javafx.geometry.Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
			stage.setWidth(screenBounds.getWidth());
			stage.setHeight(screenBounds.getHeight());
			stage.show();
		});

	}

	/**
	 * Returns the {@link VBox} that will positioned on the right part of the main
	 * pane, which is a {@link BorderPane}. By default, it creates a VBox containing
	 * the simulation properties using the {@link ParametersSheetFactory}.
	 * <p>
	 * This method can be overridden to provide a custom right node.
	 * <p>
	 * Default code is:
	 * 
	 * <pre>
	 * {@code
	 * protected VBox createRightNode() {
	 * 	return ParametersSheetFactory.getVBoxProperties(
	 * 		getViewer().getSimuEngine(), 
	 * 		getViewer().getModel(),
	 * 		getViewer().getEnvironment(), 
	 * 		getViewer().getScheduler(), 
	 * 		getViewer());
	 * }
	 *    </pre>
	 * <p>
	 * It will be positioned on the right of the main pane.
	 *
	 * @return a VBox that will be used as the right node of the main pane
	 */
	protected VBox createRightNode() {
		return ParametersSheetFactory.getVBoxProperties(getViewer().getSimuEngine(), getViewer().getModel(),
				getViewer().getEnvironment(), getViewer().getScheduler(), getViewer());
	}

	/**
	 * Creates the central node of the viewer agent. This method has to be implemented by the subclass.
	 */
	protected abstract Node createCentralNode();

	/**
	 * Creates a toolbar for the viewer agent.
	 *
	 * @return a ToolBar with the viewer agent's actions
	 */
	protected ToolBar createToolBar() {
		ToolBar toolBar = ToolBars.createToolBarFor(viewer);
		toolBar.getItems().add(ActionUtils.createToggleButton(renderingOff, ActionTextBehavior.HIDE));
		toolBar.getItems().add(ActionUtils.createToggleButton(synchroPainting, ActionTextBehavior.HIDE));

		toolBar.getItems().addAll(viewer.getScheduler().getToolBar().getItems());

		ActionGroup ag = Actions.createAgentActionGroupFor(viewer);
		ag.getActions().removeLast();
		for (Action action : ag.getActions()) {
			toolBar.getItems().add(ActionUtils.createButton(action, ActionTextBehavior.HIDE));
		}
//		Button b = new Button("RESET"); //TODO add a reset button
//		b.setOnAction(e -> viewer.getScheduler().onStart());
//		toolBar.getItems().add(b);
		return toolBar;
	}

	/**
	 * Returns the stage of the viewer agent.
	 * 
	 * @return the stage
	 */
	public FxAgentStage getStage() {
		return stage;
	}

	/**
	 * Returns the menu bar of the viewer agent.
	 * 
	 * @return the menu bar
	 */
	public MenuBar getMenuBar() {
		return menuBar;
	}

	/**
	 * Sets the menu bar of the viewer agent.
	 * 
	 * @param menuBar the menuBar to set
	 */
	public void setMenuBar(MenuBar menuBar) {
		this.menuBar = menuBar;
	}

	/**
	 * Returns the rendering off action.
	 * @return the renderingOff action
	 */
	public FXActionCheck getRenderingOffAction() {
		return renderingOff;
	}

	/**
	 * Returns the synchronous painting action.
	 * 
	 * @return the synchroPainting
	 */
	public FXActionCheck getSynchroPaintingAction() {
		return synchroPainting;
	}

	/**
	 * Returns if the rendering is activated.
	 * 
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
	 * Enable or disable the synchronous painting
	 * 
	 * @param activated if true, the synchronous painting is activated
	 */
	public void setSynchroPainting(boolean activated) {
		synchroPainting.setSelected(activated);
	}

	void setRenderingInterval(int interval) {
		renderingInterval = interval > 0 ? interval : 1;
		// if ((int) comboBox.getSelectedItem() != renderingInterval) {
		// comboBox.setSelectedItem(renderingInterval);
		// }
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
	 * Sets the main pane of the viewer agent.
	 * 
	 * @param mainPane the mainPane to set
	 */
	public void setMainPane(BorderPane mainPane) {
		this.mainPane = mainPane;
	}

	/**
	 * Returns the central node of the viewer
	 * 
	 * @return the centralNode
	 */
	public Node getCentralNode() {
		return centralNode;
	}

	/**
	 * Sets the central node of the viewer
	 * 
	 * @param centralNode the centralNode to set
	 */
	public void setCentralNode(Node centralNode) {
		this.centralNode = centralNode;
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
	 * Requests the rendering of the simulation state. The rendering is done in the JavaFX thread
	 * depending on the state of the GUI such as the synchronous painting mode.
	 *  
	 */
	public void requestRendering() {
		if (synchroPainting.isSelected() && !renderingOff.isSelected()) {// && isAlive()) {
			if (counter > renderingInterval) {
				FXManager.runAndWait(viewer::render);
				counter = 2;
			} else {
				counter++;
			}
		}
	}

	private void initRenderingActions() {
		// initRenderingIntervalComboBox(messages.getString("UPDATE_INTERVAL"));

		AnimationTimer animationTimer = new AnimationTimer() {
			@Override
			public void handle(long now) {
				if (!renderingOff.isSelected() && viewer.isAlive()) {
					viewer.render();
				}
			}
		};

		renderingOff = new FXActionCheck(new ActionData("DISABLE", KeyEvent.VK_A), null);
		renderingOff.setSelected(false);

		synchroPainting = new FXActionCheck(new ActionData("SYNCHRO_PAINTING", KeyEvent.VK_Z), ae -> {
			if (synchroPainting.isSelected()) {
				animationTimer.stop();
			} else {
				animationTimer.start();
			}
		});

		synchroPainting.setSelected(true);

	}

	/**
	 * @return the viewer
	 */
	public Viewer getViewer() {
		return viewer;
	}

}
