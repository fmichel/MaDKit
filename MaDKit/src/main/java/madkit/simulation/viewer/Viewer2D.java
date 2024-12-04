package madkit.simulation.viewer;

import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

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
 * @version 6
 */
public abstract class Viewer2D extends AbstractViewer {

	private GraphicsContext graphics;

	private Canvas canvas;

	/**
	 * @return the graphics
	 */
	public GraphicsContext getGraphics() {
		return graphics;
	}

	/**
	 * Should return the rendering node for this viewer. Called by super.setupGUI
	 */
	@Override
	protected Node createCentralNode() {
		return newDefaultCanvas(400, 400);
	}

	protected Canvas newDefaultCanvas(int width, int height) {
		Canvas canvas = new Canvas(width, height);
//		canvas.setStyle("-fx-border-style: solid outside;" + "-fx-border-width: 2;" + "-fx-border-insets: 25;"
//				+ "-fx-border-radius: 5;" + "-fx-border-color: blue;");

		canvas.setOnScroll(e -> {
			double factor = e.getDeltaY() > 0 ? 0.1 : -0.1;
			if (canvas.getScaleX() + factor > 0.3) {
				canvas.setScaleX(canvas.getScaleX() + factor);
				canvas.setScaleY(canvas.getScaleY() + factor);
			}
		});
		setGraphics(canvas.getGraphicsContext2D());
		return canvas;
	}

	/**
	 * @param graphics the graphics to set
	 */
	private void setGraphics(GraphicsContext graphics) {
		this.graphics = graphics;
	}

	/**
	 * @return the canvas
	 */
	public Canvas getCanvas() {
		return canvas;
	}

//	
//	@SuppressWarnings("unchecked")
//	@Override
//	public Environment2D getEnvironment() {
//		return super.getEnvironment();
//	}

}
