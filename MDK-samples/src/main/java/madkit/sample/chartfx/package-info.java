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

/**
 * Chart-fx live-plotting demonstrations for MaDKit simulations.
 *
 * <p>This package contains sample simulations that showcase the
 * {@link madkit.chartfx} wrapper layer for high-performance chart-fx
 * based live plotting. Each demo uses a paced scheduler so plot evolution
 * is observable in real time.
 *
 * <h2>Demo A — Role Population Chart</h2>
 * <p>Demonstrates {@link madkit.chartfx.ProbeXYChartViewer} by monitoring
 * organizational role populations over simulation time. This is the chart-fx
 * counterpart of
 * {@link madkit.simulation.viewer.RolesPopulationLineChartDrawer}.
 * <ul>
 *   <li>{@link madkit.sample.chartfx.PopulationAgent} — simulated agent
 *       that plays and switches roles</li>
 *   <li>{@link madkit.sample.chartfx.PopulationChartViewer} — probe-driven
 *       chart-fx viewer</li>
 *   <li>{@link madkit.sample.chartfx.PopulationScheduler} — tick-based
 *       scheduler with viewer activation</li>
 *   <li>{@link madkit.sample.chartfx.PopulationChartLauncher} — simulation
 *       launcher with pacing configured</li>
 * </ul>
 *
 * <h2>Demo B — Agent Metric Chart</h2>
 * <p>Demonstrates {@link madkit.chartfx.XYChartViewer} with multi-series
 * custom agent metrics plotted live over simulation time.
 * <ul>
 *   <li>{@link madkit.sample.chartfx.MetricAgent} — simulated agent with
 *       an evolving energy metric</li>
 *   <li>{@link madkit.sample.chartfx.MetricChartViewer} — XY chart viewer
 *       plotting average metrics per role</li>
 *   <li>{@link madkit.sample.chartfx.MetricChartLauncher} — simulation
 *       launcher for the metric demo</li>
 * </ul>
 *
 * <p>All demo classes are implemented as <b>separate top-level classes</b>.
 * No inner classes are used for agents, viewers, or launchers.
 *
 * @see madkit.chartfx
 * @see madkit.chartfx.ProbeXYChartViewer
 * @see madkit.chartfx.XYChartViewer
 */
package madkit.sample.chartfx;
