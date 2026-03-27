/*******************************************************************************
 * MaDKit - Multi-agent systems Development Kit 
 * 
 * Copyright (c) 1998-2025 Fabien Michel, Olivier Gutknecht, Jacques Ferber...
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

import io.fair_acc.dataset.spi.DoubleDataSet;
import madkit.kernel.Probe;

/**
 * A convenience {@link XYChartViewer} subclass that monitors organizational
 * role populations through {@link Probe} objects, plotting their sizes over
 * simulation time using chart-fx.
 *
 * <p>This class is the chart-fx counterpart of
 * {@link madkit.simulation.viewer.RolesPopulationLineChartDrawer}. It provides
 * the same probe-driven monitoring pattern — register probes for specific
 * group/role pairs, and the viewer automatically plots population counts at
 * each simulation step — while benefiting from chart-fx's non-blocking data
 * path and sliding-window eviction.
 *
 * <h2>Key differences from {@code RolesPopulationLineChartDrawer}</h2>
 * <ul>
 *   <li><b>Non-blocking updates:</b> Data is appended via
 *       {@link XYChartViewer#addData(Object, double, double)}, which does
 *       <b>not</b> use {@link madkit.gui.FXExecutor#runAndWait(Runnable)}.
 *       Chart-fx picks up new data on its next rendering pulse.</li>
 *   <li><b>Sliding-window eviction:</b> Old data points are trimmed
 *       automatically instead of clearing all series at once.</li>
 *   <li><b>Numeric X axis:</b> Simulation time is plotted as a {@code double}
 *       value on a numeric axis, not as a category string.</li>
 * </ul>
 *
 * <h2>Usage example</h2>
 * <pre>{@code
 * public class MyPopulationViewer extends ProbeXYChartViewer {
 *
 *     @Override
 *     protected void onActivation() {
 *         super.onActivation();
 *         addProbeDataSet(getModelGroup(), "worker");
 *         addProbeDataSet(getModelGroup(), "manager");
 *     }
 *
 *     @Override
 *     protected String getChartTitle() {
 *         return "Role Populations";
 *     }
 * }
 * }</pre>
 *
 * <p><b>Thread safety:</b> The {@link #display()} method is called from the
 * simulation thread by the scheduler. All data updates go through the
 * non-blocking {@link XYChartViewer#addData(Object, double, double)} path.
 * No synchronous JavaFX handoff is involved.
 *
 * @see XYChartViewer
 * @see madkit.simulation.viewer.RolesPopulationLineChartDrawer
 * @see Probe
 * @see <a href="https://github.com/fair-acc/chart-fx">chart-fx on GitHub</a>
 */
public abstract class ProbeXYChartViewer extends XYChartViewer<Probe> {

	/**
	 * Creates and registers a {@link Probe} for the specified group and role,
	 * and adds a corresponding {@link DoubleDataSet} to the chart.
	 *
	 * <p>This is the chart-fx equivalent of
	 * {@link madkit.simulation.viewer.RolesPopulationLineChartDrawer#addRoleToMonitoring(String, String)}.
	 * The probe is registered with this viewer's community (obtained from
	 * {@link #getCommunity()}) and added via {@link #addProbe(Probe)}. A
	 * dataset named after the role is created and keyed by the probe.
	 *
	 * <p>This method should be called during {@link #onActivation()} after
	 * {@code super.onActivation()}.
	 *
	 * @param group the organization group to monitor
	 * @param role  the organization role to monitor within the group
	 * @return the newly created and registered {@link Probe}
	 * @see #display()
	 * @see Probe
	 */
	protected Probe addProbeDataSet(String group, String role) {
		Probe probe = new Probe(getCommunity(), group, role);
		addProbe(probe);
		addDataSet(probe, role);
		return probe;
	}

	/**
	 * Updates all probe-based datasets with the current population counts.
	 *
	 * <p>For each registered probe, this method appends a data point at
	 * {@code (simulationTime, probe.size())} using the non-blocking
	 * {@link XYChartViewer#addData(Object, double, double)} method. It then
	 * calls {@code super.display()} to trigger rendering.
	 *
	 * <p>The simulation time is obtained from
	 * {@link #getSimuTimer()}{@code .getCurrentTime()} and converted to
	 * {@code double}. This assumes a tick-based timer where the current time
	 * is a {@link Number} (e.g., {@link java.math.BigDecimal}).
	 *
	 * <p><b>Thread safety:</b> This method is called from the simulation
	 * thread. No {@link madkit.gui.FXExecutor#runAndWait(Runnable)} call is
	 * involved — chart-fx picks up the new data on its next rendering pulse.
	 */
	@Override
	public void display() {
		double time = ((Number) getSimuTimer().getCurrentTime()).doubleValue();
		getProbes().forEach(probe -> addData(probe, time, probe.size()));
		super.display();
	}

	/**
	 * Returns {@code "Population"} as the default Y-axis label.
	 *
	 * <p>This is appropriate for probe-based population monitoring. Subclasses
	 * may override this method to provide a different label.
	 *
	 * @return the Y-axis label, never {@code null}
	 */
	@Override
	protected String getYAxisLabel() {
		return "Population";
	}
}
