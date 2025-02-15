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
package madkit.simulation.viewer;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionGroup;
import org.controlsfx.control.action.ActionUtils;
import org.controlsfx.control.action.ActionUtils.ActionTextBehavior;

import javafx.animation.AnimationTimer;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import madkit.action.ActionData;
import madkit.action.Actions;
import madkit.gui.DefaultAgentGUI;
import madkit.gui.FXActionCheck;
import madkit.gui.FXExecutor;
import madkit.gui.PropertySheetFactory;
import madkit.gui.ToolBars;
import madkit.kernel.Agent;
import madkit.kernel.Probe;
import madkit.simulation.Viewer;

/**
 * This class provides a default GUI for a viewer agent. It creates a JavaFX stage with a
 * menu bar, a toolbar, a central node and a right node.
 * <p>
 * The central node is created by the {@link #createCenterNode()} method, which has to be
 * implemented by the subclass.
 * <p>
 * The right node is created by the {@link #createRightNode()} method, which can be
 * overridden to provide a custom right node. By default, it creates a VBox containing the
 * simulation properties using the {@link PropertySheetFactory}.
 * <p>
 * The toolbar is created by the {@link #createToolBar()} method, which can be overridden
 * to provide a custom toolbar. By default, it creates a toolbar with the viewer agent's
 * actions and the scheduler's actions.
 * <p>
 * The rendering is managed by the {@link #requestRendering()} method, which is called by
 * the viewer agent when it wants to render the simulation state. This method triggers the
 * {@link Viewer#render()} in the JavaFX thread, depending on the state of the GUI such as
 * the synchronous painting mode.
 * <p>
 * The synchronous painting mode can be activated or deactivated by the user. When
 * activated, the rendering is done for each simulation step and blocks the simulation
 * process. When deactivated, the rendering is done without blocking the simulation
 * process, at a rate corresponding to the complexity of the rendering.
 * 
 */
public abstract class ViewerDefaultGUI extends DefaultAgentGUI {

	private FXActionCheck renderingOff;
	private FXActionCheck synchroPainting;
	private int renderingInterval = 1;
	private int counter = 2;

	/**
	 * Creates a new ViewerDefaultGUI for the given viewer.
	 * 
	 * @param viewer the viewer agent
	 */
	protected ViewerDefaultGUI(Viewer viewer) {
		super(viewer);
		FXExecutor.runAndWait(() -> {
			Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
			getStage().setWidth(screenBounds.getWidth());
			getStage().setHeight(screenBounds.getHeight());
			ToolBar tb = (ToolBar) getMainPane().getBottom();
			tb.getItems().get(8).requestFocus();
		});
	}

	/**
	 * Redefines to initialize the default rendering actions.
	 */
	@Override
	protected void onInitialize() {
		AnimationTimer animationTimer = new AnimationTimer() {
			@Override
			public void handle(long now) {
				if (getViewer().isAlive()) {
					getViewer().render();
				}
			}
		};

		renderingOff = new FXActionCheck(new ActionData("DISABLE", KeyEvent.VK_A), _ -> {
			if (renderingOff.isSelected()) {
				animationTimer.stop();
			} else {
				checkAnimationTimer(animationTimer);
			}
		});
		renderingOff.setSelected(false);

		synchroPainting = new FXActionCheck(new ActionData("SYNCHRO_PAINTING", KeyEvent.VK_Z), _ -> {
			checkAnimationTimer(animationTimer);
		});
		synchroPainting.setSelected(true);
	}

	/**
	 * @param animationTimer
	 */
	private void checkAnimationTimer(AnimationTimer animationTimer) {
		if (synchroPainting.isSelected()) {
			animationTimer.stop();
		} else {
			animationTimer.start();
		}
	}

	/**
	 * Creates the top node of the viewer agent. By default, it creates a menu bar with the
	 * rendering actions and returns a VBox containing the menu bar.
	 *
	 * @return a VBox containing the menu bar
	 */
	@Override
	protected Node createTopNode() {
		VBox vb = new VBox();
		MenuBar menuBar = new MenuBar();
		Menu menu = new Menu("Rendering");
		menu.getItems().add(ActionUtils.createCheckMenuItem(synchroPainting));
		menu.getItems().add(ActionUtils.createCheckMenuItem(renderingOff));
		menuBar.getMenus().add(menu);
		vb.getChildren().add(menuBar);
		return vb;
	}

	/**
	 * Creates the bottom node.
	 *
	 * @return the node
	 */
	@Override
	protected Node createBottomNode() {
		ToolBar toolBar = getToolBar();
		toolBar.getItems().get(8).requestFocus();
		return toolBar;
	}

	/**
	 * Returns the {@link VBox} that will positioned on the right part of the main pane, which
	 * is a {@link BorderPane}. By default, it creates a VBox containing the simulation
	 * properties using the {@link PropertySheetFactory}.
	 * <p>
	 * This method can be overridden to provide a custom right node.
	 * <p>
	 * Default code is:
	 * 
	 * <pre>
	 * {@code
	 * protected VBox createRightNode() {
	 * 	return PropertySheetFactory.getVBoxProperties(getViewer().getSimuEngine(), getViewer().getModel(),
	 * 			getViewer().getEnvironment(), getViewer().getScheduler(), getViewer());
	 * }
	 * }
	 *    </pre>
	 * <p>
	 * It will be positioned on the right of the main pane.
	 *
	 * @return a VBox that will be used as the right node of the main pane
	 */
	@Override
	protected Node createRightNode() {
		List<Object> l = new ArrayList<>();
		l.add(getViewer().getLauncher());
		l.add(getViewer().getModel());
		l.add(getViewer().getEnvironment());
		l.add(getViewer().getScheduler());
		l.add(getViewer());
		l.addAll(getSimulatedAgentClasses());
		return PropertySheetFactory.getVBoxProperties(l.toArray(new Object[0]));
	}

	private Set<Class<? extends Agent>> getSimulatedAgentClasses() {
		Set<Class<? extends Agent>> classes = new HashSet<>();
		for (Probe probe : getViewer().getProbes()) {
			for (Agent a : probe.getAgents()) {
				classes.add(a.getClass());
			}
		}
		return classes;
	}

	/**
	 * Creates the central node of the viewer agent. This method has to be implemented by the
	 * subclass.
	 * 
	 * @return the central node of the main pane
	 */
	@Override
	protected abstract Node createCenterNode();

	/**
	 * Creates a toolbar for the viewer agent.
	 *
	 * @return a ToolBar with the viewer agent's actions
	 */
	@Override
	protected ToolBar createToolBar() {
		ToolBar toolBar = ToolBars.createToolBarFor(getViewer());
		toolBar.getItems().add(ActionUtils.createToggleButton(renderingOff, ActionTextBehavior.HIDE));
		toolBar.getItems().add(ActionUtils.createToggleButton(synchroPainting, ActionTextBehavior.HIDE));

		toolBar.getItems().addAll(getViewer().getScheduler().getToolBar().getItems());

		ActionGroup ag = Actions.createAgentActionGroupFor(getViewer());
		ag.getActions().removeLast();
		for (Action action : ag.getActions()) {
			toolBar.getItems().add(ActionUtils.createButton(action, ActionTextBehavior.HIDE));
		}
		Button b = new Button("On Simulation Start");
		b.setOnAction(_ -> getViewer().getLauncher().onSimulationStart());
		b.setTooltip(new Tooltip("trigger the OnSimulationStart process"));
		toolBar.getItems().add(b);
		return toolBar;
	}

	/**
	 * Requests the rendering of the simulation state. The rendering is done in the JavaFX
	 * thread depending on the state of the GUI such as the synchronous painting mode.
	 * 
	 */
	public void requestRendering() {
		if (synchroPainting.isSelected() && !renderingOff.isSelected()) {// && isAlive()) {
			if (counter > renderingInterval) {
				FXExecutor.runAndWait(getViewer()::render);
				counter = 2;
			} else {
				counter++;
			}
		}
	}

	/**
	 * Returns the rendering off action.
	 * 
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
	 * 
	 * @param activated if <code>true</code>, the renderingOff activity is activated
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

	/**
	 * Sets the rendering interval.
	 *
	 * @param interval the new rendering interval
	 */
	void setRenderingInterval(int interval) {
		renderingInterval = interval > 0 ? interval : 1;
		// if ((int) comboBox.getSelectedItem() != renderingInterval) {
		// comboBox.setSelectedItem(renderingInterval);
		// }
	}

	/**
	 * Returns the viewer agent associated with this GUI.
	 * 
	 * @return the viewer agent
	 */
	public Viewer getViewer() {
		return (Viewer) getAgent();
	}

}
