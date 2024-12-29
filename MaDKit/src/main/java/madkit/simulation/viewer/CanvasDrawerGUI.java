package madkit.simulation.viewer;

import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import madkit.simulation.Viewer;

/**
 * The `CanvasDrawerGUI` class extends the `ViewerDefaultGUI` class and is
 * responsible for rendering the simulation. It uses a canvas to draw the
 * simulation.
 */
public class CanvasDrawerGUI extends ViewerDefaultGUI {

	Canvas canvas;

	/**
	 * Creates a new CanvasDrawerGUI with the specified viewer.
	 * 
	 * @param viewer the viewer associated with this GUI.
	 */
	public CanvasDrawerGUI(Viewer viewer) {
		super(viewer);
	}

	/**
	 * Creates the central node of the GUI. It returns a canvas.
	 */
	@Override
	protected Node createCenterNode() {
		canvas = new Canvas(600, 600);
		canvas.setOnScroll(e -> {
			double deltaY = e.getDeltaY();
			if (deltaY != 0) {
				double factor = deltaY > 0 ? 0.1 : -0.1;
				if (canvas.getScaleX() + factor > 0.2) {
					canvas.setScaleX(canvas.getScaleX() + factor);
					canvas.setScaleY(canvas.getScaleY() + factor);
				}
			}
		});
		return canvas;
	}

	/**
	 * Gets the graphics context of the canvas.
	 * 
	 * @return the graphics context of the canvas.
	 */
	public GraphicsContext getGraphics() {
		return canvas.getGraphicsContext2D();
	}

	/**
	 * Gets the canvas
	 * 
	 * @return the canvas
	 */
	public Canvas getCanvas() {
		return canvas;
	}

	/**
	 * Sets the size of the canvas.
	 * 
	 * @param width  the width of the canvas.
	 * @param height the height of the canvas.
	 */
	public void setCanvasSize(int width, int height) {
		canvas.setWidth(width);
		canvas.setHeight(height);
	}

}
