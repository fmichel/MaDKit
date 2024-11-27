package madkit.simu.template;

import static javafx.scene.paint.Color.BLACK;
import static javafx.scene.paint.Color.WHITE;

import javafx.scene.Node;
import madkit.kernel.Agent;
import madkit.simulation.PropertySheetAgents;
import madkit.simulation.environment.Environment2D;
import madkit.simulation.probe.PropertyProbe;
import madkit.simulation.viewer.Viewer2D;

@PropertySheetAgents(classesToBuildUIWith= {SimulatedAgent.class,Environment2D.class})
public class MyViewer extends Viewer2D{

	PropertyProbe<Double> positionX;
	PropertyProbe<Double> positionY;
	
	@SuppressWarnings("unchecked")
	@Override
	public Environment2D getEnvironment() {
		return super.getEnvironment();
	}

	@Override
	protected void onActivation() {
		super.onActivation();
		positionX = new PropertyProbe<>(getModelGroup(), "simuAgent", "x");
		positionY = new PropertyProbe<>(getModelGroup(), "simuAgent", "y");
		addProbe(positionX);
		addProbe(positionY);
	}
	
	@Override
	protected void render() {
		getGraphics().setFill(WHITE);
		getGraphics().fillRect(0, 0, getEnvironment().getWidth(), getEnvironment().getHeight());
		getGraphics().setFill(BLACK);
		for (Agent a : positionX.getCurrentAgentsList()) {
			getGraphics().fillOval(positionX.getPropertyValue(a), positionY.getPropertyValue(a), 10, 10);
		}
	}

	@Override
	protected Node createCentralNode() {
		return newDefaultCanvas(getEnvironment().getWidth(), getEnvironment().getHeight());
	}

}
