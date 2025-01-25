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
package madkit.bees;

import java.awt.Point;
import java.util.Objects;

import org.controlsfx.control.action.ActionUtils;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Label;
import madkit.action.AgentMethodAction;
import madkit.gui.FXExecutor;
import madkit.gui.UIProperty;
import madkit.kernel.Probe;
import madkit.simulation.PropertyProbe;
import madkit.simulation.viewer.CanvasDrawerGUI;
import madkit.simulation.viewer.Viewer2D;

/**
 * A viewer that displays the bees in a 2D environment. It extends the {@link Viewer2D}
 * class that uses a {@link CanvasDrawerGUI} to draw the environment on a canvas.
 * 
 */
public class BeeViewer extends Viewer2D {

	private PropertyProbe<BeeData> beeProbe;
	private Probe queenProbe;

	@UIProperty(category = "Rendering", displayName = "Display trails")
	private boolean trailMode = true;

	/** The number of bees. */
	SimpleIntegerProperty numberOfBees = new SimpleIntegerProperty(0);

	/**
	 * On activation.
	 */
	@Override
	protected void onActivation() {
		beeProbe = new PropertyProbe<>(getModelGroup(), BeeOrganization.BEE, "data");
		addProbe(beeProbe);
		queenProbe = new Probe(getModelGroup(), BeeOrganization.QUEEN);
		addProbe(queenProbe);
		super.onActivation();
		FXExecutor.runLater(() -> {
			ObservableList<Node> items = getGUI().getToolBar().getItems();
			items.add(ActionUtils.createButton(new AgentMethodAction(this, "launchAQueen")));
			items.add(ActionUtils.createButton(new AgentMethodAction(this, "killAQueen")));
			Label label = new Label();
			label.textProperty().bind(numberOfBees.asString("Number of bees: %d"));
			items.add(label);
		});
	}

	/**
	 * Render.
	 */
	@Override
	public void render() {
		super.render();
		numberOfBees.set(beeProbe.size());
		beeProbe.streamValues().filter(Objects::nonNull).forEach(data -> {
			getGraphics().setStroke(data.getBeeColor());
			Point p = data.getCurrentPosition();
			if (trailMode) {
				Point p1 = data.getPreviousPosition();
				getGraphics().strokeLine(p1.x, p1.y, p.x, p.y);
			} else {
				getGraphics().strokeLine(p.x, p.y, p.x, p.y);
			}
		});
	}

	/**
	 * Launches a queen bee.
	 */
	public void launchAQueen() {
		getLogger().info("Launching a queen");
		launchAgent(new QueenBee());
	}

	/**
	 * Kills a queen bee.
	 */
	public void killAQueen() {
		getLogger().info("Killing a queen");
		queenProbe.getAgents().stream().findFirst().ifPresent(this::killAgent);
	}

	/**
	 * If on, the viewer will display the trails of the bees.
	 * 
	 * @return the trailMode
	 */
	public boolean isTrailMode() {
		return trailMode;
	}

	/**
	 * If set to true, the viewer will display the trails of the bees.
	 * 
	 * @param trailMode the trailMode to set
	 */
	public void setTrailMode(boolean trailMode) {
		this.trailMode = trailMode;
	}

}