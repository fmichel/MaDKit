
package madkit.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

import org.testng.annotations.Test;

import madkit.random.RandomizedBoolean;
import madkit.random.RandomizedDouble;
import madkit.random.RandomizedFloat;
import madkit.random.RandomizedInteger;
import madkit.random.Randomness;

public class FieldsValueRandomizerTest {

	// Given: an object with fields annotated for randomization
	public static class TestObject {
		@RandomizedDouble(minValue = 1.0, maxValue = 10.0)
		private double doubleField = 100;

		@RandomizedInteger(minValue = 1, maxValue = 10)
		private int intField = 100;

		@RandomizedBoolean
		private boolean booleanField;

		@RandomizedFloat(minValue = 1.0f, maxValue = 10.0f)
		private float floatField = 100;

		@RandomizedDouble(minValue = 1.0, maxValue = 10.0)
		private static double staticDoubleField = 100;

		@RandomizedInteger(minValue = 1, maxValue = 10)
		private static int staticIntField = 100;

		@RandomizedBoolean
		private static boolean staticBooleanField;

		@RandomizedFloat(minValue = 1.0f, maxValue = 10.0f)
		private static float staticFloatField = 100;

		public double getDoubleField() {
			return doubleField;
		}

		public int getIntField() {
			return intField;
		}

		public boolean isBooleanField() {
			return booleanField;
		}

		public float getFloatField() {
			return floatField;
		}

		public static double getStaticDoubleField() {
			return staticDoubleField;
		}

		public static int getStaticIntField() {
			return staticIntField;
		}

		public static boolean isStaticBooleanField() {
			return staticBooleanField;
		}

		public static float getStaticFloatField() {
			return staticFloatField;
		}
	}

	// Given: a subclass that inherits fields annotated for randomization
	public static class SubTestObject extends TestObject {
		@RandomizedDouble(minValue = 1.0, maxValue = 20.0)
		private double subDoubleField = 200;

		@RandomizedFloat(minValue = 1.0f, maxValue = 20.0f)
		private float subFloatField = 200;

		public double getSubDoubleField() {
			return subDoubleField;
		}

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
		assertThat(testObject.isBooleanField()).isNotNull();
		assertThat(testObject.getFloatField()).isBetween(1.0f, 10.0f);
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
		assertThat(TestObject.isStaticBooleanField()).isNotNull();
		assertThat(TestObject.getStaticFloatField()).isBetween(1.0f, 10.0f);
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
		assertThat(subTestObject.isBooleanField()).isNotNull();
		assertThat(subTestObject.getFloatField()).isBetween(1.0f, 10.0f);
		assertThat(subTestObject.getSubDoubleField()).isBetween(1.0, 20.0);
		assertThat(subTestObject.getSubFloatField()).isBetween(1.0f, 20.0f);
	}
}
