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
package madkit.sample.chartfx;

import java.util.logging.Level;

import madkit.simulation.scheduler.MethodActivator;
import madkit.simulation.scheduler.TickBasedScheduler;

/**
 * A tick-based scheduler for the chart-fx population and metric demos.
 *
 * <p>
 * This scheduler activates simulated agents (calling their {@code doIt()} method) and
 * then activates viewers (calling their {@code display()} method) at each simulation
 * step. It follows the standard MaDKit scheduling pattern demonstrated in the simulation
 * template.
 *
 * <p>
 * Pacing is configured externally by the launcher using {@link #setPause(int)}, keeping
 * timing control out of the chart code.
 *
 * @see PopulationChartLauncher
 * @see MetricChartLauncher
 * @see TickBasedScheduler
 */
public class MetricScheduler extends TickBasedScheduler {

	private MethodActivator producers;
	private MethodActivator consumers;
	private MethodActivator viewers;

	/**
	 * Activates this scheduler and sets up the activators for agents and viewers.
	 *
	 * <p>
	 * Two agent activators target the model group: one for {@value PopulationAgent#WORKER}
	 * agents and one for {@value PopulationAgent#MANAGER} agents, both invoking their
	 * {@code doIt()} method. The viewer activator uses the standard
	 * {@link #addViewersActivator()} helper.
	 */
	@Override
	protected void onActivation() {
		super.onActivation();
		getLogger().setLevel(Level.ALL);
		producers = new MethodActivator(getModelGroup(), MetricAgent.PRODUCER, "doIt");
		addActivator(producers);
		consumers = new MethodActivator(getModelGroup(), MetricAgent.CONSUMER, "doIt");
		addActivator(consumers);
		viewers = addViewersActivator();
	}

	/**
	 * Performs one simulation step: executes agent activators, then viewer activators, then
	 * advances the simulation time.
	 */
	@Override
	public void doSimulationStep() {
		producers.execute();
		consumers.execute();
		viewers.execute();
		super.doSimulationStep();
	}
}
