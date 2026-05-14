/*******************************************************************************
 * MaDKit - Multi-agent systems Development Kit 
 * 
 * Copyright (c) 1998-2026 Fabien Michel, Olivier Gutknecht, Jacques Ferber...
 * 
 * This software is a computer program whose purpose is to
 * provide a lightweight Java API for developing and simulating 
 * Multi-Agent Systems (MAS) using an organizational perspective.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.You can use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty and the software's author, the holder of the
 * economic rights, and the successive licensors have only limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading, using, modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean that it is complicated to manipulate, and that also
 * therefore means that it is reserved for developers and experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and, more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 *******************************************************************************/
package madkit.chartfx;

import java.util.List;

import io.fair_acc.chartfx.Chart;
import io.fair_acc.chartfx.plugins.ChartPlugin;
import io.fair_acc.chartfx.plugins.DataPointTooltip;
import io.fair_acc.chartfx.plugins.EditAxis;
import io.fair_acc.chartfx.plugins.Zoomer;
import javafx.scene.Node;
import madkit.gui.FXExecutor;
import madkit.simulation.SimuOrganization;
import madkit.simulation.Viewer;
import madkit.simulation.viewer.ViewerDefaultGUI;

/**
 * Abstract base class for embedding a chart-fx {@link Chart} inside a MaDKit
 * {@link Viewer}.
 *
 * <p>
 * This class handles the lifecycle wiring between MaDKit's viewer mechanism and
 * chart-fx's own rendering pipeline. Subclasses only need to implement
 * {@link #createChart()} to supply the concrete {@link Chart} instance; default
 * interactive plugins (zoom, tooltip, axis editing) are installed automatically and can
 * be customised via {@link #getDefaultPlugins()}.
 *
 * <p>
 * Because chart-fx manages its own {@link javafx.animation.AnimationTimer}, the
 * {@link #render()} method is a no-op by default — there is no need for synchronous
 * painting. Subclasses may override {@link #render()} if they need to perform additional
 * drawing on top of the chart.
 *
 * @see XYChartViewer
 * @see Viewer
 * @see <a href="https://github.com/fair-acc/chart-fx">chart-fx on GitHub</a>
 */
public abstract class ChartFxViewer extends Viewer {

	private Chart chart;

	/**
	 * Creates the chart-fx {@link Chart} instance to be displayed in this viewer.
	 *
	 * <p>
	 * This method is called once during {@link #onActivation()}, on the JavaFX Application
	 * Thread (inside the {@link ViewerDefaultGUI} constructor). Subclasses must return a
	 * fully-configured, non-null chart instance.
	 *
	 * @return the chart-fx chart, never {@code null}
	 */
	protected abstract Chart createChart();

	/**
	 * Returns the list of chart-fx plugins to install on the chart after creation.
	 *
	 * <p>
	 * The default implementation returns a list containing a {@link Zoomer}, a
	 * {@link DataPointTooltip}, and an {@link EditAxis}. Subclasses may override this method
	 * to customise or extend the default set of plugins.
	 *
	 * @return a list of {@link ChartPlugin} instances, never {@code null}
	 * @see Chart#getPlugins()
	 */
	protected List<ChartPlugin> getDefaultPlugins() {
		return List.of(new Zoomer(), new DataPointTooltip(), new EditAxis());
	}

	/**
	 * Returns the chart-fx {@link Chart} instance managed by this viewer.
	 *
	 * <p>
	 * This method returns {@code null} before {@link #onActivation()} has completed. After
	 * activation, it returns the chart created by {@link #createChart()}.
	 *
	 * @return the chart-fx chart, or {@code null} if not yet activated
	 */
	public Chart getChart() {
		return chart;
	}

	/**
	 * Activates this viewer agent, creating the chart and its GUI.
	 *
	 * <p>
	 * The activation flow is:
	 * <ol>
	 * <li>Call {@code super.onActivation()} — requests the
	 * {@link SimuOrganization#VIEWER_ROLE}</li>
	 * <li>Create the {@link ViewerDefaultGUI} whose
	 * {@link ViewerDefaultGUI#createCenterNode() createCenterNode()} invokes
	 * {@link #createChart()} and installs {@link #getDefaultPlugins() default plugins}</li>
	 * <li>Show the stage</li>
	 * <li>Disable synchronous painting (chart-fx self-renders)</li>
	 * </ol>
	 */
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
		FXExecutor.runAndWait(() -> {
			getGUI().getStage().show();
		});
//		getGUI().getSynchroPaintingAction().setSelected(false);
	}

	/**
	 * No-op by default — chart-fx manages its own rendering cycle via an internal
	 * {@link javafx.animation.AnimationTimer}.
	 *
	 * <p>
	 * Subclasses may override this method to perform additional custom drawing on top of the
	 * chart during each simulation display cycle.
	 */
	@Override
	public void render() {
		// No-op: chart-fx self-renders on its own animation timer.
	}
}
