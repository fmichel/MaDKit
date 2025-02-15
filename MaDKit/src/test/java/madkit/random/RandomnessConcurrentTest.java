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

package madkit.random;

import org.testng.annotations.Test;

import madkit.kernel.Agent;
import madkit.kernel.MadkitConcurrentTestCase;

/**
 * The Class RandomnessTest.
 */
public class RandomnessConcurrentTest extends MadkitConcurrentTestCase {

	@Test
	public void givenAgentWithRandomizedFields_whenRandomizeFields_thenFieldsAreRandomized() {
		// Then
		runTest(new RandomizedValuesAgent() {
			@Override
			protected void onActivation() {
				threadAssertTrue(getRandomizedDouble() != 50);
				threadAssertTrue(randomizedInteger != 50);
				threadAssertTrue(randomizedFloat != 50);
				threadAssertFalse(randomizedString.equals("Z"));
				resume();
			}
		});
	}
}

/**
 * The Class RandomizedValuesAgent.
 */
class RandomizedValuesAgent extends Agent {

	@RandomizedDouble(min = 0.0, max = 1.0)
	private double randomizedDouble = 50;

	@RandomizedInteger(min = 1, max = 10)
	protected int randomizedInteger = 50;

	@RandomizedFloat(min = 0.0f, max = 1.0f)
	protected float randomizedFloat = 50;

	@RandomizedString(values = { "A", "B", "C" })
	protected String randomizedString = "Z";

	/**
	 * @return the randomizedDouble
	 */
	public double getRandomizedDouble() {
		return randomizedDouble;
	}

	/**
	 * @param randomizedDouble the randomizedDouble to set
	 */
	public void setRandomizedDouble(double randomizedDouble) {
		this.randomizedDouble = randomizedDouble;
	}

}
