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

package madkit.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

import org.testng.annotations.Test;

import madkit.random.RandomizedBoolean;
import madkit.random.RandomizedDouble;
import madkit.random.RandomizedFloat;
import madkit.random.RandomizedInteger;
import madkit.random.RandomizedString;
import madkit.random.Randomness;

/**
 * The Class FieldsValueRandomizerTest.
 */
public class FieldsValueRandomizerTest {

	/**
	 * The Class TestObject.
	 */
	// Given: an object with fields annotated for randomization
	public static class TestObject {
		@RandomizedDouble(min = 1.0, max = 10.0)
		private double doubleField = 100;

		@RandomizedInteger(min = 1, max = 10)
		private int intField = 100;

		@RandomizedBoolean
		private boolean booleanField;

		@RandomizedFloat(min = 1.0f, max = 10.0f)
		private float floatField = 100;

		@RandomizedDouble(min = 1.0, max = 10.0)
		private static double staticDoubleField = 100;

		@RandomizedInteger(min = 1, max = 10)
		private static int staticIntField = 100;

		@RandomizedBoolean
		private static boolean staticBooleanField;

		@RandomizedFloat(min = 1.0f, max = 10.0f)
		private static float staticFloatField = 100;

		@RandomizedString(values = { "apple", "banana", "cherry" })
		private String stringField;

		@RandomizedString(values = { "apple", "banana", "cherry" })
		private static String staticStringField;

		/**
		 * Gets the double field.
		 *
		 * @return the double field
		 */
		public double getDoubleField() {
			return doubleField;
		}

		/**
		 * Gets the int field.
		 *
		 * @return the int field
		 */
		public int getIntField() {
			return intField;
		}

		/**
		 * Checks if is boolean field.
		 *
		 * @return true, if is boolean field
		 */
		public boolean isBooleanField() {
			return booleanField;
		}

		/**
		 * Gets the float field.
		 *
		 * @return the float field
		 */
		public float getFloatField() {
			return floatField;
		}

		/**
		 * Gets the static double field.
		 *
		 * @return the static double field
		 */
		public static double getStaticDoubleField() {
			return staticDoubleField;
		}

		/**
		 * Gets the static int field.
		 *
		 * @return the static int field
		 */
		public static int getStaticIntField() {
			return staticIntField;
		}

		/**
		 * Checks if is static boolean field.
		 *
		 * @return true, if is static boolean field
		 */
		public static boolean isStaticBooleanField() {
			return staticBooleanField;
		}

		/**
		 * Gets the static float field.
		 *
		 * @return the static float field
		 */
		public static float getStaticFloatField() {
			return staticFloatField;
		}

		/**
		 * Gets the string field.
		 *
		 * @return the string field
		 */
		public String getStringField() {
			return stringField;
		}

		/**
		 * Gets the static string field.
		 *
		 * @return the static string field
		 */
		public static String getStaticStringField() {
			return staticStringField;
		}
	}

	/**
	 * The Class SubTestObject.
	 */
	// Given: a subclass that inherits fields annotated for randomization
	public static class SubTestObject extends TestObject {
		@RandomizedDouble(min = 1.0, max = 20.0)
		private double subDoubleField = 200;

		@RandomizedFloat(min = 1.0f, max = 20.0f)
		private float subFloatField = 200;

		/**
		 * Gets the sub double field.
		 *
		 * @return the sub double field
		 */
		public double getSubDoubleField() {
			return subDoubleField;
		}

		/**
		 * Gets the sub float field.
		 *
		 * @return the sub float field
		 */
		public float getSubFloatField() {
			return subFloatField;
		}
	}

	@Test
	public void givenObjectWithAnnotatedFields_whenRandomizeFields_thenFieldsAreRandomized()
			throws IllegalAccessException {
		// Given
		TestObject testObject = new TestObject();
		RandomGenerator prng = RandomGeneratorFactory.of("L64X128MixRandom").create();

		// When: randomizeFields is called
		Randomness.randomizeFields(testObject, prng);

		// Then: the fields should be randomized
		assertThat(testObject.getDoubleField()).isBetween(1.0, 10.0);
		assertThat(testObject.getIntField()).isBetween(1, 10);
		assertThat(testObject.isBooleanField()).isInstanceOf(Boolean.class);
		assertThat(testObject.getFloatField()).isBetween(1.0f, 10.0f);
		assertThat(testObject.getStringField()).isIn("apple", "banana", "cherry");
	}

	@Test
	public void givenClassWithAnnotatedStaticFields_whenRandomizeFields_thenStaticFieldsAreRandomized()
			throws IllegalAccessException {
		// Given
		TestObject testObject = new TestObject();
		RandomGenerator prng = RandomGeneratorFactory.of("L64X128MixRandom").create();

		// When: randomizeFields is called
		Randomness.randomizeFields(testObject, prng);

		// Then: the static fields should be randomized
		assertThat(TestObject.getStaticDoubleField()).isBetween(1.0, 10.0);
		assertThat(TestObject.getStaticIntField()).isBetween(1, 10);
		assertThat(TestObject.isStaticBooleanField()).isInstanceOf(Boolean.class);
		assertThat(TestObject.getStaticFloatField()).isBetween(1.0f, 10.0f);
		assertThat(TestObject.getStaticStringField()).isIn("apple", "banana", "cherry");
	}

	@Test
	public void givenSubclassWithInheritedAnnotatedFields_whenRandomizeFields_thenInheritedFieldsAreRandomized()
			throws IllegalAccessException {
		// Given
		SubTestObject subTestObject = new SubTestObject();
		RandomGenerator prng = RandomGeneratorFactory.of("L64X128MixRandom").create();

		// When: randomizeFields is called
		Randomness.randomizeFields(subTestObject, prng);

		// Then: the inherited fields and subclass fields should be randomized
		assertThat(subTestObject.getDoubleField()).isBetween(1.0, 10.0);
		assertThat(subTestObject.getIntField()).isBetween(1, 10);
		assertThat(subTestObject.isBooleanField()).isInstanceOf(Boolean.class);
		assertThat(subTestObject.getFloatField()).isBetween(1.0f, 10.0f);
		assertThat(subTestObject.getSubDoubleField()).isBetween(1.0, 20.0);
		assertThat(subTestObject.getSubFloatField()).isBetween(1.0f, 20.0f);
		assertThat(subTestObject.getStringField()).isIn("apple", "banana", "cherry");
	}
}
