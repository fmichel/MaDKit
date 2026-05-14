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
import madkit.random.RandomizedBoolean;
import madkit.random.RandomizedDouble;
import madkit.random.RandomizedFloat;
import madkit.random.RandomizedInteger;
import madkit.random.RandomizedString;

/**
 * Demonstrates annotation-driven field randomization in a MaDKit agent.
 * <p>
 * Each field annotated with a {@code Randomized*} annotation is automatically assigned a
 * random value by the MaDKit kernel just before {@link #onActivation()} is called. The
 * initial values set in the field declarations (e.g. {@code -1}, {@code false},
 * {@code "None"}) are replaced with random values within the specified constraints.
 * <p>
 * Run this class directly to see the randomized values in the log output.
 *
 * @see madkit.random.Randomness#randomizeFields(Object, java.util.random.RandomGenerator)
 * @see RandomizationDemo
 */
public class RandomizedAgent extends Agent {

	/** Random speed in the range [0.0, 100.0). */
	@RandomizedDouble(min = 0.0, max = 100.0)
	private double speed = -1;

	/** Random strength in the range [1, 10). */
	@RandomizedInteger(min = 1, max = 10)
	private int strength = -1;

	/** Random boolean value. */
	@RandomizedBoolean
	private boolean active = false;

	/** Random accuracy in the range [0.0f, 1.0f). */
	@RandomizedFloat(min = 0.0f, max = 1.0f)
	private float accuracy = -1f;

	/** Random character class chosen from {"Warrior", "Mage", "Rogue"}. */
	@RandomizedString(values = { "Warrior", "Mage", "Rogue" })
	private String characterClass = "None";

	/**
	 * Called after field randomization. Logs all randomized values so that the effect of
	 * the annotations can be observed.
	 */
	@Override
	protected void onActivation() {
		getLogger().info(() -> "speed         = " + speed);
		getLogger().info(() -> "strength      = " + strength);
		getLogger().info(() -> "active        = " + active);
		getLogger().info(() -> "accuracy      = " + accuracy);
		getLogger().info(() -> "characterClass = " + characterClass);
	}

	/**
	 * Launches a single instance of {@link RandomizedAgent}.
	 *
	 * @param args MaDKit command-line options (e.g. {@code --agentLogLevel FINE})
	 */
	public static void main(String[] args) {
		executeThisAgent(args);
	}
}
