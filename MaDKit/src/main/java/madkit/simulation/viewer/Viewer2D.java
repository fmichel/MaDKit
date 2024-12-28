/**
 * 
 */
package madkit.simulation.viewer;

import javafx.scene.canvas.GraphicsContext;
import madkit.simulation.Viewer;
import madkit.simulation.DefaultViewerGUI;
import madkit.simulation.environment.Environment2D;
import madkit.simulation.probe.PropertyProbe;

/**
 * 
 */
public abstract class Viewer2D extends Viewer {

	private GraphicsContext graphics;
	
	@SuppressWarnings("unchecked")
	@Override
	public Environment2D getEnvironment() {
		return super.getEnvironment();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public CanvasDrawerGUI getGUI() {
		return super.getGUI();
	}

	@Override
	protected void onActivation() {
		super.onActivation();
		CanvasDrawerGUI gui = new CanvasDrawerGUI(this);
		gui.setCanvasSize(getEnvironment().getWidth(), getEnvironment().getHeight());
		graphics = gui.getGraphics();
		setGUI(gui);
	}

	/**
	 * Gets the graphics context to draw on the canvas.
	 * @return the graphics context to draw on the canvas.
	 */
	protected GraphicsContext getGraphics() {
		return graphics;
	}
}
