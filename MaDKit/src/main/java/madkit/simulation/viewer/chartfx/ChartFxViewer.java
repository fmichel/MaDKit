package madkit.simulation.viewer.chartfx;

import java.util.List;

import io.fair_acc.chartfx.Chart;
import io.fair_acc.chartfx.plugins.ChartPlugin;
import io.fair_acc.chartfx.plugins.DataPointTooltip;
import io.fair_acc.chartfx.plugins.EditAxis;
import io.fair_acc.chartfx.plugins.Zoomer;
import javafx.scene.Node;
import madkit.gui.FXExecutor;
import madkit.simulation.Viewer;
import madkit.simulation.viewer.ViewerDefaultGUI;

/** Base viewer embedding a self-rendering chart-fx {@link Chart}. */
public abstract class ChartFxViewer extends Viewer {
	private Chart chart;

	/** Creates the configured chart displayed by this viewer. */
	protected abstract Chart createChart();

	/** Returns the interactive plugins installed on the created chart. */
	protected List<ChartPlugin> getDefaultPlugins() {
		return List.of(new Zoomer(), new DataPointTooltip(), new EditAxis());
	}

	/** Returns the chart, or {@code null} before activation. */
	public Chart getChart() {
		return chart;
	}

	/** Activates the engine viewer and creates its JavaFX chart UI. */
	@Override
	protected void onActivation() {
		super.onActivation();
		setGUI(new ViewerDefaultGUI(this) {
			@Override
			protected Node createCenterNode() {
				chart = createChart();
				chart.getPlugins().addAll(getDefaultPlugins());
				return chart;
			}
		});
		FXExecutor.runAndWait(() -> getGUI().getStage().show());
	}

	/** No-op because chart-fx manages rendering through its animation timer. */
	@Override
	public void render() {
	}
}
