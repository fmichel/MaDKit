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

package madkit.reflection;

import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceArrayMap;
import madkit.kernel.Activator;

/**
 * Utilities for finding method references in Agent classes. Provides methods to find and
 * retrieve methods from agent classes, including private and inherited methods.
 * 
 * @since 6.0
 */
public class MethodFinder {

	// Map to store primitive types and their corresponding wrapper classes
	static final Map<Class<?>, Class<?>> primitiveTypes = new Reference2ReferenceArrayMap<>();

	static {
		primitiveTypes.put(int.class, Integer.class);
		primitiveTypes.put(boolean.class, Boolean.class);
		primitiveTypes.put(byte.class, Byte.class);
		primitiveTypes.put(char.class, Character.class);
		primitiveTypes.put(float.class, Float.class);
		primitiveTypes.put(void.class, Void.class);
		primitiveTypes.put(short.class, Short.class);
		primitiveTypes.put(double.class, Double.class);
		primitiveTypes.put(long.class, Long.class);
	}

	private MethodFinder() {
	}

	/**
	 * Class -> <signature -> Method>
	 */
	private static final Map<Class<?>, Map<String, Method>> methodsTableAA = new ConcurrentHashMap<>();

	/**
	 * Returns the agent's method named <code>methodName</code> considering a given
	 * agentClass. This also works for private and inherited methods.
	 *
	 * @param agentClass     the class wherein the search has to be started
	 * @param methodName     the name of the method
	 * @param parameterTypes the parameter types of the targeted method
	 * @return the agent's method named <code>methodName</code>
	 * @throws NoSuchMethodException if the method cannot be found
	 */
	public static Method getMethodFromTypes(Class<?> agentClass, String methodName, Class<?>... parameterTypes)
			throws NoSuchMethodException {
		final Method a = getMethodTable(agentClass).computeIfAbsent(
				ReflectionUtils.getSignature(methodName, parameterTypes),
				m -> findAvailableMethod(agentClass, methodName, parameterTypes));
		if (a == null) {
			throw new NoSuchMethodException("Cannot find method **" + methodName + "** on " + agentClass);
		}
		return a;
	}

	// Finds an available method in the given class or its super classes
	private static Method findAvailableMethod(Class<?> agentClass, String name2, Class<?>[] types) {
		Method m = findMethodIn(name2, agentClass.getMethods(), types);
		if (m == null) {
			while (agentClass != Object.class) {
				m = findMethodIn(name2, agentClass.getDeclaredMethods(), types);
				if (m != null) {
					try {
						m.setAccessible(true);
					} catch (InaccessibleObjectException e) {
						e.printStackTrace();
					}
					return m;
				}
				agentClass = agentClass.getSuperclass();
			}
		}
		return m;
	}

	/**
	 * Find a method by searching all methods and testing parameters types.
	 *
	 * @param methodName the name of the method
	 * @param methods    the array of methods to search in
	 * @param parameters the parameter types to match
	 * @return the found method or null if not found
	 */
	static Method findMethodIn(String methodName, Method[] methods, Class<?>[] parameters) {
		return Arrays.stream(methods)
				.filter(m -> m.getName().equals(methodName)
						&& checkTypesCompatibility(convertPrimitiveToObjectTypes(m.getParameterTypes()), parameters))
				.findAny().orElse(null);
	}

	// Checks if the method types are compatible with the given parameter types
	static boolean checkTypesCompatibility(Class<?>[] methodTypes, Class<?>[] parametersTypes) {
		if (parametersTypes.length == methodTypes.length) {
			for (int i = 0; i < methodTypes.length; i++) {
				if (parametersTypes[i] != null && !methodTypes[i].isAssignableFrom(parametersTypes[i])) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	// Retrieves the method table for the given class
	private static Map<String, Method> getMethodTable(Class<?> agentClass) {
		return methodsTableAA.computeIfAbsent(agentClass, _ -> new Reference2ReferenceArrayMap<String, Method>());
	}

	// Converts primitive types to their corresponding wrapper classes
	static Class<?>[] convertPrimitiveToObjectTypes(final Class<?>[] parameters) {
		for (int i = 0; i < parameters.length; i++) {
			final Class<?> paramCl = parameters[i];
			if (paramCl != null && paramCl.isPrimitive()) {
				parameters[i] = primitiveTypes.get(paramCl);
			}
		}
		return parameters;
	}

	/**
	 * Returns the agent's method named <code>methodName</code> considering a given agentClass
	 * and a sample of the arguments which could be passed to it. The purpose of this method
	 * is restricted to a limited number of use cases since
	 * {@link #getMethodFromTypes(Class, String, Class...)} should be preferred if the exact
	 * signature of the searched method is known. A typical use case of this method is when
	 * the only information available is the arguments which are passed, for instance when
	 * overriding the {@link Activator#execute(Object...)} method and the like in
	 * {@link Activator} subclasses. This also works for private and inherited methods.
	 *
	 * @param agentClass the class wherein the search has to be made
	 * @param methodName the name of the method
	 * @param argsSample a sample of the args which can be passed to the method
	 * @return the agent's method named <code>methodName</code>
	 * @throws NoSuchMethodException if a matching method cannot be found
	 */
	public static Method getMethodOn(Class<?> agentClass, String methodName, Object... argsSample)
			throws NoSuchMethodException {
		return getMethodFromTypes(agentClass, methodName, ReflectionUtils.convertArgToTypes(argsSample));
	}

}
