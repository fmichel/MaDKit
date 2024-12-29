/**
 * 
 */
package madkit.simulation.viewer;

import javafx.scene.canvas.GraphicsContext;
import madkit.simulation.Viewer;
import madkit.simulation.environment.Environment2D;

/**
 * A viewer that displays a 2D environment. It uses a {@link CanvasDrawerGUI} to
 * draw the environment on a canvas.
 * 
 */
public abstract class Viewer2D extends Viewer {

	private GraphicsContext graphics;

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
	 * 
	 * @return the graphics context to draw on the canvas.
	 */
	protected GraphicsContext getGraphics() {
		return graphics;
	}

	/**
	 * Redefines to benefit from automatic casting.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Environment2D getEnvironment() {
		return super.getEnvironment();
	}

	/**
	 * Redefines to benefit from automatic casting.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public CanvasDrawerGUI getGUI() {
		return super.getGUI();
	}
}
