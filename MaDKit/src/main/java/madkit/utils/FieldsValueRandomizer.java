package madkit.utils;

import java.lang.reflect.Field;
import java.util.random.RandomGenerator;

/**
 * This class provides a method to randomize the values of fields in an object. The fields
 * to be randomized are marked with the annotations {@link RandomizedDouble},
 * {@link RandomizedInteger}, and {@link RandomizedBoolean}. For Agent subclasses, the
 * {@link FieldsValueRandomizer} is automatically called by the kernel when the agent is
 * launched, unless MadkitOptions.randomizeFields is set to false.
 */
public class FieldsValueRandomizer {

	/**
	 * Randomizes the values of the fields of the object. The fields to be randomized are
	 * marked with the annotations {@link RandomizedDouble}, {@link RandomizedInteger}, and
	 * {@link RandomizedBoolean}.
	 * 
	 * @param object the object whose fields are to be randomized
	 * @param prng   the pseudo-random number generator to be used
	 * @throws IllegalAccessException if a field cannot be accessed
	 */
	public static void randomizeFields(Object object, RandomGenerator prng)
			throws IllegalAccessException {
		final Class<?> originType = object.getClass();
		Class<?> currentType = originType;
		while (currentType != Object.class) {
			for (Field field : currentType.getDeclaredFields()) {
				randomizeDouble(object, field, prng);
				randomizeInteger(object, field, prng);
				randomizeBoolean(object, field, prng);
				randomizeFloat(object, field, prng);
			}
			currentType = currentType.getSuperclass();
		}
	}

	private static void randomizeDouble(Object object, Field field, RandomGenerator prng)
			throws IllegalAccessException {
		if (field.isAnnotationPresent(RandomizedDouble.class)) {
			RandomizedDouble annotation = field.getAnnotation(RandomizedDouble.class);
			double minValue = annotation.minValue();
			double maxValue = annotation.maxValue();
			double randomValue = prng.nextDouble(minValue, maxValue);

			field.setAccessible(true);
			field.setDouble(object, randomValue);
		}
	}

	private static void randomizeInteger(Object object, Field field, RandomGenerator prng)
			throws IllegalAccessException {
		if (field.isAnnotationPresent(RandomizedInteger.class)) {
			RandomizedInteger annotation = field.getAnnotation(RandomizedInteger.class);
			int minValue = annotation.minValue();
			int maxValue = annotation.maxValue();
			int randomValue = prng.nextInt(minValue, maxValue);

			field.setAccessible(true);
			field.setInt(object, randomValue);
		}
	}

	private static void randomizeBoolean(Object object, Field field, RandomGenerator prng)
			throws IllegalAccessException {
		if (field.isAnnotationPresent(RandomizedBoolean.class)) {
			field.setAccessible(true);
			field.setBoolean(object, prng.nextBoolean());
		}
	}

	private static void randomizeFloat(Object object, Field field, RandomGenerator prng) throws IllegalAccessException {
		if (field.isAnnotationPresent(RandomizedFloat.class)) {
			RandomizedFloat annotation = field.getAnnotation(RandomizedFloat.class);
			float minValue = annotation.minValue();
			float maxValue = annotation.maxValue();
			float randomValue = prng.nextFloat(minValue, maxValue);

			field.setAccessible(true);
			field.setFloat(object, randomValue);
		}
	}

}
