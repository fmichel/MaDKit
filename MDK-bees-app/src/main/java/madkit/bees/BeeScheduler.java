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
package madkit.bees;

import java.util.Map;

import madkit.grafana.metrics.SimuMetrics;
import madkit.kernel.Scheduler;
import madkit.simulation.scheduler.DateBasedTimer;
import madkit.simulation.scheduler.MethodActivator;

/**
 * The scheduler of the bees simulation.
 */
public class BeeScheduler extends Scheduler<DateBasedTimer> {

	private MethodActivator bees;
	private BeeLauncher launcher;
	private SimuMetrics metrics;

	/**
	 * On activation.
	 */
	@Override
	public void onActivation() {
		super.onActivation();
		bees = new MethodActivator(getModelGroup(), BeeOrganization.BEE, "buzz");
		addActivator(bees);
		addViewersActivator();
		launcher = (BeeLauncher) getLauncher();
		metrics = launcher.getMetrics();
	}

	/**
	 * Defines a simulation step. Executes all activators, advances the timer, and records
	 * population metrics for Grafana visualization.
	 */
	@Override
	public void doSimulationStep() {
		logCurrrentTime();
		getActivators().forEach(a -> a.execute());
		getSimuTimer().addOneTimeUnit();
		recordPopulationMetrics();
	}

	/**
	 * Records the current population counts as metrics for Grafana visualization. Population
	 * probes are owned by the {@link BeeLauncher} (which extends {@code Watcher}), so they
	 * are accessed via {@link #getLauncher()}.
	 */
	private void recordPopulationMetrics() {
		metrics.record("population", bees.size(), Map.of("role", "follower"));
		metrics.record("queens", launcher.getQueensProbe().size(), Map.of("role", "queen"));
	}

}