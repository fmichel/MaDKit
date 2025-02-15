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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * The Class RandomnessTest.
 */
public class RandomnessTest {

	private RandomGenerator prng;

	@BeforeMethod
	public void setUp() {
		prng = RandomGeneratorFactory.of("Random").create();
	}

	@Test
	public void givenObjectWithRandomizedFields_whenRandomizeFields_thenFieldsAreRandomized() {
		// Given
		TestObject testObject = new TestObject();

		// When
		Randomness.randomizeFields(testObject, prng);

		// Then
		assertThat(testObject.randomizedDouble).isBetween(0.0, 1.0);
		assertThat(testObject.randomizedInteger).isBetween(1, 10);
		assertThat(testObject.randomizedFloat).isBetween(0.0f, 1.0f);
		assertThat(testObject.randomizedString).isIn("A", "B", "C");
	}

	private static class TestObject {
		@RandomizedDouble(min = 0.0, max = 1.0)
		private double randomizedDouble = 50;

		@RandomizedInteger(min = 1, max = 10)
		private int randomizedInteger = 50;

		@RandomizedFloat(min = 0.0f, max = 1.0f)
		private float randomizedFloat = 50f;

		@RandomizedString(values = { "A", "B", "C" })
		private String randomizedString = "Z";
	}
}
