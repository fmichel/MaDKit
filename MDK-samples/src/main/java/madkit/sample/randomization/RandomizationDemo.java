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
package madkit.sample.randomization;

import madkit.kernel.Agent;

/**
 * Launches 3 {@link RandomizedAgent} instances to demonstrate that each agent receives
 * different randomized field values.
 * <p>
 * Because field randomization happens independently for each agent instance using the
 * kernel's PRNG, running this demo will produce distinct values for every agent — even
 * though they share the same class definition.
 *
 * @see RandomizedAgent
 * @see madkit.random.Randomness#randomizeFields(Object, java.util.random.RandomGenerator)
 */
public class RandomizationDemo extends Agent {

	/** The number of {@link RandomizedAgent} instances to launch. */
	private static final int AGENT_COUNT = 3;

	/**
	 * Launches {@value #AGENT_COUNT} {@link RandomizedAgent} instances and logs each launch.
	 */
	@Override
	protected void onActivation() {
		for (int i = 1; i <= AGENT_COUNT; i++) {
			final int index = i;
			launchAgent(new RandomizedAgent());
			getLogger().info(() -> "Launched RandomizedAgent #" + index);
		}
	}

	/**
	 * Launches a single instance of {@link RandomizationDemo}.
	 *
	 * @param args MaDKit command-line options
	 */
	public static void main(String[] args) {
		executeThisAgent(args);
	}
}
