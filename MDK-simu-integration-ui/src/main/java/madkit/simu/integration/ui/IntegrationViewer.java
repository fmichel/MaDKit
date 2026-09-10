package madkit.simu.integration.ui;

import static javafx.scene.paint.Color.DARKORANGE;

import madkit.kernel.AgentAddress;
import madkit.simulation.viewer.Viewer2D;

/**
 * Minimal JavaFX viewer that renders one marker for every shared integration agent.
 *
 * <p>The viewer deliberately obtains agents through the MaDKit organization instead of
 * exposing simulation state through the environment.</p>
 */
public class IntegrationViewer extends Viewer2D {

	@Override
	protected void onActivation() {
		super.onActivation();
	}

	@Override
	public void render() {
		super.render();
		getGraphics().setFill(DARKORANGE);
		int index = 0;
		for (AgentAddress ignored : getAgentsWithRole(getCommunity(), getModelGroup(), "simuAgent")) {
			int x = 20 + (index % 10) * 45;
			int y = 20 + (index / 10) * 45;
			getGraphics().fillOval(x, y, 18, 18);
			index++;
		}
	}
}
