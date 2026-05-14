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

import madkit.simulation.SimuAgent;

/**
 * A simulated agent with an evolving energy metric for the chart-fx metric chart demo.
 *
 * <p>
 * Each agent has an {@code energy} value that fluctuates at each simulation step. Agents
 * are assigned either the {@value #PRODUCER} or {@value #CONSUMER} role, with producers
 * tending to gain energy and consumers tending to lose it (with random noise).
 *
 * <p>
 * The {@code energy} field is probed by {@link MetricChartViewer} via a
 * {@link madkit.simulation.PropertyProbe} to compute per-role average energy values for
 * live plotting.
 *
 * @see MetricChartViewer
 * @see MetricChartLauncher
 */
public class MetricAgent extends SimuAgent {

	/** Role name for producer agents. */
	static final String PRODUCER = "producer";

	/** Role name for consumer agents. */
	static final String CONSUMER = "consumer";

	/** The energy metric, probed by {@link MetricChartViewer}. */
	private double energy;

	private String role;

	/**
	 * Activates this agent with a random role and initial energy.
	 *
	 * <p>
	 * Producers start with energy around 100, consumers around 50.
	 */
	@Override
	protected void onActivation() {
		role = prng().nextBoolean() ? PRODUCER : CONSUMER;
		playRole(role);
		energy = role.equals(PRODUCER) ? 80 + prng().nextDouble() * 40 : 30 + prng().nextDouble() * 40;
	}

	/**
	 * Performs one simulation step, evolving the energy metric.
	 *
	 * <p>
	 * Producers gain a small random amount of energy, consumers lose a small random amount.
	 * Both have random noise applied. Energy is clamped to the range [0, 200].
	 */
	@SuppressWarnings("unused")
	private void doIt() {
		double delta = prng().nextGaussian() * 3;
		if (role.equals(PRODUCER)) {
			energy += 0.5 + delta;
		} else {
			energy -= 0.5 - delta;
		}
		energy = Math.max(0, Math.min(200, energy));
	}

	/**
	 * Returns the current energy of this agent.
	 *
	 * @return the energy value
	 */
	public double getEnergy() {
		return energy;
	}
}
