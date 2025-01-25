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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceArrayMap;
import madkit.kernel.Activator;

/**
 * 
 * This class provides a way to find a {@link MethodHandle} from a given {@link Class} and
 * a method name. It is especially useful when the method signature is unknown and the
 * method is private or inherited. The {@link MethodHandle} is cached in order to avoid
 * the cost of the reflection API.
 * 
 * @since MaDKit 6.0
 */
public class MethodHandleFinder {

	private MethodHandleFinder() {
		throw new IllegalStateException("Utility class");
	}

	private static Lookup lookup = MethodHandles.lookup();
	private static final Map<Class<?>, Map<String, MethodHandle>> methodsTableAA = new ConcurrentHashMap<>();

	private static Map<String, MethodHandle> getMethodTable(Class<?> type) {
		return methodsTableAA.computeIfAbsent(type, table -> new Reference2ReferenceArrayMap<String, MethodHandle>());
	}

	/**
	 * Returns a {@link MethodHandle} according to a <code>methodName</code>, a given
	 * agentClass and a sample of the arguments which could be passed to it. The purpose of
	 * this method is restricted to a limited number of use cases since
	 * {@link #findMethodHandle(Class, String, Class...)} should be preferred if the exact
	 * signature of the searched method is known. A typical use case of this method is when
	 * the only information available is the arguments which are passed, for instance when
	 * overriding the {@link Activator#execute(Object...)} method and the like in
	 * {@link Activator} subclasses. This also works for private and inherited methods.
	 *
	 * @param agentClass the class from which the search has to be done
	 * @param methodName the name of the method
	 * @param args       a sample of the args which can be passed to the method
	 * @return the found {@link MethodHandle}
	 * @throws NoSuchMethodException  if a matching method cannot be found
	 * @throws IllegalAccessException if the method cannot be accessed, for instance if
	 *                                targeted the module is not opened to the MaDKit
	 *                                madkit.base module
	 */
	public static MethodHandle findMethodHandleFromArgs(Class<?> agentClass, String methodName, Object... args)
			throws NoSuchMethodException, IllegalAccessException {
		Map<String, MethodHandle> methodTable = getMethodTable(agentClass);
		MethodHandle methodHandle = methodTable.get(methodName);
		if (methodHandle == null) {
			Method m;
			if (args.length == 0) {
				m = MethodFinder.getMethodOn(agentClass, methodName);
			} else {
				m = MethodFinder.getMethodOn(agentClass, methodName, args);
			}
			m.setAccessible(true);// NOSONAR
			methodHandle = lookup.unreflect(m);
			methodTable.put(methodName, methodHandle);
		}
		return methodHandle;
	}

	/**
	 * Returns a {@link MethodHandle} according to a <code>methodName</code> and a given
	 * agentClass. This method should be preferred the exact signature of the searched method
	 * is known.
	 * 
	 * @param agentClass the class from which the search has to be done
	 * @param methodName the name of the method
	 * @param paramTypes the types of the arguments of the method as an array of {@link Class}
	 * @return the found {@link MethodHandle}
	 * @throws NoSuchMethodException if a matching method cannot be found
	 */
	public static MethodHandle findMethodHandle(Class<?> agentClass, String methodName, Class<?>... paramTypes)
			throws NoSuchMethodException {
		MethodHandle methodHandle = getMethodTable(agentClass).computeIfAbsent(methodName, name -> {
			try {
				Method m = MethodFinder.getMethodFromTypes(agentClass, name, paramTypes);
				m.setAccessible(true);// NOSONAR
				return lookup.unreflect(m);
			} catch (NoSuchMethodException | IllegalAccessException e) {
				e.printStackTrace();
				return null;
			}
		});
		if (methodHandle == null) {
			throw new NoSuchMethodException("method " + methodName + " not found in " + agentClass);
		}
		return methodHandle;
	}

}
