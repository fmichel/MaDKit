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
package madkit.sample.chartfx;

import madkit.simulation.EngineAgents;
import madkit.simulation.SimuLauncher;

/**
 * Launcher for the chart-fx agent metric demo.
 *
 * <p>
 * This simulation demonstrates multi-series live plotting with
 * {@link madkit.simulation.viewer.chartfx.XYChartViewer}. It launches {@value #NB_AGENTS}
 * {@link MetricAgent} instances (randomly assigned "producer" or "consumer" roles) whose
 * average energy is plotted by a {@link MetricChartViewer}.
 *
 * <h2>Pacing</h2>
 * <p>
 * The scheduler pause is set to {@value #SCHEDULER_PAUSE_MS} ms so the chart evolves
 * slowly enough to be observed by a human.
 *
 * <h2>Running the demo</h2>
 * 
 * <pre>{@code
 * MetricChartLauncher.main(new String[] { "--start" });
 * }</pre>
 *
 * @see MetricAgent
 * @see MetricChartViewer
 * @see MetricScheduler
 */
@EngineAgents(scheduler = MetricScheduler.class, viewers = { MetricChartViewer.class })
public class MetricChartLauncher extends SimuLauncher {

	/** Number of simulated agents to launch. */
	private static final int NB_AGENTS = 100;

	/**
	 * Scheduler pause in milliseconds between simulation steps.
	 */
	private static final int SCHEDULER_PAUSE_MS = 50;

	/**
	 * Launches the simulated agents. Each agent is a {@link MetricAgent} that randomly takes
	 * a "producer" or "consumer" role with an evolving energy metric.
	 */
	@Override
	protected void onLaunchSimulatedAgents() {
		for (int i = 0; i < NB_AGENTS; i++) {
			launchAgent(new MetricAgent());
		}
	}

	/**
	 * Configures simulation pacing after all agents and viewers are ready.
	 */
	@Override
	public void onSimulationStart() {
		super.onSimulationStart();
		getScheduler().setPause(SCHEDULER_PAUSE_MS);
	}

	/**
	 * Entry point for running the agent metric chart demo.
	 *
	 * @param args command-line arguments passed to MaDKit
	 */
	public static void main(String[] args) {
		executeThisAgent(args);
	}
}
