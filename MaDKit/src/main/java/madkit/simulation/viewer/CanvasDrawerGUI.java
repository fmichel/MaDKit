package madkit.simulation.viewer;

import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import madkit.simulation.Viewer;
import madkit.simulation.DefaultViewerGUI;

public class CanvasDrawerGUI extends DefaultViewerGUI {

	Canvas canvas;

	public CanvasDrawerGUI(Viewer viewer) {
		super(viewer);
	}

	@Override
	protected Node createCentralNode() {
		canvas = new Canvas(600, 600);
//		canvas.setStyle("-fx-border-style: solid outside;" + "-fx-border-width: 2;" + "-fx-border-insets: 25;"
//				+ "-fx-border-radius: 5;" + "-fx-border-color: blue;");
		
		canvas.setOnScroll(e -> {
			double factor = e.getDeltaY() > 0 ? 0.1 : -0.1;
			if (canvas.getScaleX() + factor > 0.3) {
				canvas.setScaleX(canvas.getScaleX() + factor);
				canvas.setScaleY(canvas.getScaleY() + factor);
			}
		});
		return canvas;
	}

	public GraphicsContext getGraphics() {
		return canvas.getGraphicsContext2D();
	}

	/**
	 * @return the canvas
	 */
	public Canvas getCanvas() {
		return canvas;
	}

	public void setCanvasSize(int width, int height) {
        canvas.setWidth(width);
        canvas.setHeight(height);
    }


}
