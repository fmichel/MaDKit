
package madkit.simu.template;

import static javafx.scene.paint.Color.BLACK;
import static javafx.scene.paint.Color.WHITE;

import madkit.kernel.Agent;
import madkit.simulation.PropertyProbe;
import madkit.simulation.environment.Environment2D;
import madkit.simulation.viewer.Viewer2D;

/**
 * The `MyViewer` class extends the `Viewer2D` class and is responsible for
 * rendering the simulation. It uses property probes to get the positions of
 * agents and draws them on the screen.
 */
public class MyViewer extends Viewer2D {

	// Property probe to get the X position of agents
	PropertyProbe<Double> positionX;
	// Property probe to get the Y position of agents
	PropertyProbe<Double> positionY;

	/**
	 * This method is called when the viewer is activated. It initializes the
	 * property probes for the X and Y positions of agents and adds them to the
	 * viewer.
	 * <p>
	 * Call the superclass's onActivation method on the last line so that the
	 * building of the GUI is done after the probes are added to the viewer, thus
	 * enabling the automatic update of the GUI with the static bean properties of the classes
	 * of the agents referenced by the probes
	 * 
	 */
	@Override
	protected void onActivation() {
		// Initialize the property probe for the X position of agents
		positionX = new PropertyProbe<>(getModelGroup(), "simuAgent", "x");
		// Initialize the property probe for the Y position of agents
		positionY = new PropertyProbe<>(getModelGroup(), "simuAgent", "y");
		// Add the X position probe to the viewer
		addProbe(positionX);
		// Add the Y position probe to the viewer
		addProbe(positionY);
		// Call the superclass's onActivation method
		super.onActivation();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Environment2D getEnvironment() {
		return (Environment2D) super.getEnvironment();
	}

	/**
	 * This method is called to render the simulation. It clears the screen and
	 * draws the agents as black ovals at their respective positions.
	 */
	@Override
	public void render() {
		getGraphics().setFill(WHITE);
		getGraphics().fillRect(0, 0, getEnvironment().getWidth(), getEnvironment().getHeight());
		getGraphics().setFill(BLACK);
		for (Agent a : positionX.getAgents()) {
			getGraphics().fillOval(positionX.getPropertyValue(a), positionY.getPropertyValue(a), 10, 10);
		}
	}
}
