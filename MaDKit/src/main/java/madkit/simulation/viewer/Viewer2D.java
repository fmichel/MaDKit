/**
 * 
 */
package madkit.simulation.viewer;

import javafx.scene.canvas.GraphicsContext;
import madkit.gui.UIProperty;
import madkit.simulation.Viewer;
import madkit.simulation.environment.Environment2D;

/**
 * A viewer that displays a 2D environment. It uses a {@link CanvasDrawerGUI} to draw the
 * environment on a canvas.
 * 
 */
public abstract class Viewer2D extends Viewer {

	private GraphicsContext graphics;

	@UIProperty(category = "Rendering", displayName = "Paint over")
	private boolean paintOver = false;

	@Override
	protected void onActivation() {
		super.onActivation();
		CanvasDrawerGUI gui = new CanvasDrawerGUI(this);
		Environment2D env = getEnvironment();
		gui.setCanvasSize(env.getWidth(), env.getHeight());
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

	@Override
	public void render() {
		if (!paintOver) {
			getGUI().clearCanvas();
		}
	}

	/**
	 * Redefines to benefit from automatic casting.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public CanvasDrawerGUI getGUI() {
		return super.getGUI();
	}

	/**
	 * Sets the art mode. When art mode is enabled, the canvas is not cleared before
	 * rendering.
	 * 
	 * @param paintOver the paintOver to set
	 */
	public void setPaintOver(boolean paintOver) {
		this.paintOver = paintOver;
	}

	/**
	 * Returns the art mode.
	 * 
	 * @return the paintOver
	 */
	public boolean isPaintOver() {
		return paintOver;
	}

}
