package madkit.simu.template;

import static javafx.scene.paint.Color.BLACK;
import static javafx.scene.paint.Color.WHITE;

import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import madkit.kernel.Agent;
import madkit.simulation.Viewer;
import madkit.simulation.PropertySheetAgents;
import madkit.simulation.environment.Environment2D;
import madkit.simulation.probe.PropertyProbe;
import madkit.simulation.viewer.CanvasDrawerGUI;
import madkit.simulation.viewer.Viewer2D;

@PropertySheetAgents(classesToBuildUIWith= {SimulatedAgent.class,Environment2D.class})
public class MyViewer extends Viewer2D{

	PropertyProbe<Double> positionX;
	PropertyProbe<Double> positionY;
	
	@Override
	protected void onActivation() {
		super.onActivation();
		positionX = new PropertyProbe<>(getModelGroup(), "simuAgent", "x");
		positionY = new PropertyProbe<>(getModelGroup(), "simuAgent", "y");
		addProbe(positionX);
		addProbe(positionY);
	}
	
	@Override
	public void render() {
		getGraphics().setFill(WHITE);
		getGraphics().fillRect(0, 0, getEnvironment().getWidth(), getEnvironment().getHeight());
		getGraphics().setFill(BLACK);
		getGraphics().setStroke(Color.BLACK);
		getGraphics().strokeLine(0, 0, getEnvironment().getWidth(), getEnvironment().getHeight());
		for (Agent a : positionX.getCurrentAgentsList()) {
			getGraphics().fillOval(positionX.getPropertyValue(a), positionY.getPropertyValue(a), 10, 10);
		}
	}
}
