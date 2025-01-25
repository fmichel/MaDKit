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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

import madkit.kernel.AgentRuntimeException;

/**
 * Provides various utility methods related to randomness with MaDKit.
 * 
 */
public class Randomness {

	private static final Map<Class<?>, Boolean> typeHasAnnotation = new ConcurrentHashMap<>();

	/**
	 * Randomizes the values of the fields of the object. The fields to be randomized are
	 * marked with the annotations {@link RandomizedDouble}, {@link RandomizedInteger}, and
	 * {@link RandomizedBoolean}. For Agent subclasses, the
	 * {@link #randomizeFields(Object, RandomGenerator)} is automatically called by the kernel
	 * just before the agent is launched, unless MadkitOptions.randomizeFields is set to
	 * false.
	 * 
	 * @param object the object whose fields are to be randomized
	 * @param prng   the pseudo-random number generator to be used
	 * @throws AgentRuntimeException if a field cannot be accessed
	 */
	public static void randomizeFields(Object object, RandomGenerator prng) {
		if (!hasAnyAnnotationInUnderlyingType(object)) {
			return;
		}
		final Class<?> originType = object.getClass();
		Class<?> currentType = originType;
		while (currentType != Object.class) {
			for (Field field : currentType.getDeclaredFields()) {
				try {
					Randomness.randomizeDouble(object, field, prng);
					Randomness.randomizeInteger(object, field, prng);
					Randomness.randomizeBoolean(object, field, prng);
					Randomness.randomizeFloat(object, field, prng);
					Randomness.randomizeString(object, field, prng);
				} catch (IllegalAccessException e) {
					throw new AgentRuntimeException("Cannot access field " + field.getName());
				}
			}
			currentType = currentType.getSuperclass();
		}
	}

	private static void randomizeString(Object object, Field field, RandomGenerator prng) throws IllegalAccessException {
		if (field.isAnnotationPresent(RandomizedString.class)) {
			RandomizedString annotation = field.getAnnotation(RandomizedString.class);
			String[] values = annotation.values();
			String randomValue = values[prng.nextInt(values.length)];
			field.setAccessible(true);
			field.set(object, randomValue);
		}
	}

	/**
	 * Returns the best random generator factory considering the capabilities of current
	 * system and the highest number of state bits.
	 * 
	 * @return the best random generator factory
	 */
	public static RandomGeneratorFactory<RandomGenerator> getBestRandomGeneratorFactory() {
		return RandomGeneratorFactory.all().filter(rgf -> !rgf.name().equals("SecureRandom")) // SecureRandom has
																															// MAX_VALUE stateBits.
				.sorted(Comparator.comparingInt(RandomGeneratorFactory<RandomGenerator>::stateBits).reversed()).findFirst()
				.orElse(RandomGeneratorFactory.of("Random"));
	}

	private static void randomizeDouble(Object object, Field field, RandomGenerator prng) throws IllegalAccessException {
		if (field.isAnnotationPresent(RandomizedDouble.class)) {
			RandomizedDouble annotation = field.getAnnotation(RandomizedDouble.class);
			double minValue = annotation.min();
			double maxValue = annotation.max();
			double randomValue = prng.nextDouble(minValue, maxValue);

			field.setAccessible(true);
			field.setDouble(object, randomValue);
		}
	}

	private static void randomizeInteger(Object object, Field field, RandomGenerator prng)
			throws IllegalAccessException {
		if (field.isAnnotationPresent(RandomizedInteger.class)) {
			RandomizedInteger annotation = field.getAnnotation(RandomizedInteger.class);
			int minValue = annotation.min();
			int maxValue = annotation.max();
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
			float minValue = annotation.min();
			float maxValue = annotation.max();
			float randomValue = prng.nextFloat(minValue, maxValue);

			field.setAccessible(true);
			field.setFloat(object, randomValue);
		}
	}

	private static boolean hasAnyAnnotationInUnderlyingType(Object o) {
		return typeHasAnnotation.computeIfAbsent(o.getClass(), (Class<?> c) -> {
			Class<?> currentType = c;
			while (currentType != Object.class) {
				for (Field field : currentType.getDeclaredFields()) {
					Annotation[] annotations = field.getAnnotations();
					if (annotations.length > 0) {
						return true;
					}
				}
				currentType = currentType.getSuperclass();
			}
			return false;
		});
	}
}
