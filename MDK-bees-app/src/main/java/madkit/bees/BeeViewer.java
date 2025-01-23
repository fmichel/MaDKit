package madkit.bees;

import java.awt.Point;
import java.util.Objects;

import org.controlsfx.control.action.ActionUtils;

import javafx.collections.ObservableList;
import javafx.scene.Node;
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

	@Override
	protected void onActivation() {
		beeProbe = new PropertyProbe<>(getModelGroup(), Bee.BEE_ROLE, "data");
		addProbe(beeProbe);
		queenProbe = new Probe(getModelGroup(), Bee.QUEEN);
		addProbe(queenProbe);
		super.onActivation();
		FXExecutor.runLater(() -> {
			ObservableList<Node> items = getGUI().getToolBar().getItems();
			items.add(ActionUtils.createButton(new AgentMethodAction(this, "launchAQueen")));
			items.add(ActionUtils.createButton(new AgentMethodAction(this, "killAQueen")));
		});
	}

	@Override
	public void render() {
		super.render();
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