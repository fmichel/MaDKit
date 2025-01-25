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

import org.controlsfx.control.action.Action;

import madkit.action.AgentAction;
import madkit.kernel.Agent;

/**
 * Utilities for finding method references in Agent classes.
 * 
 * @since 6.0
 */
public class ReflectionUtils {
	private ReflectionUtils() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * Converts the name of an enum object to a Java standardized method name. For instance,
	 * using this on {@link AgentAction#LAUNCH_AGENT} will return <code>launchAgent</code>. It
	 * is especially used by {@link AgentAction} to create {@link Action}s that reflexively
	 * call the methods of an agent or send a message to it. See
	 * {@link Agent#handleRequestActionMessage(madkit.action.RequestActionMessage)}
	 * 
	 * @param <E> enum type
	 * @param e   the enum object to convert
	 * @return a string having a Java standardized method name form.
	 */
	public static <E extends Enum<E>> String enumToMethodName(E e) {
		final String[] tab = e.name().split("_");
		StringBuilder methodName = new StringBuilder(tab[0].toLowerCase());
		for (int i = 1; i < tab.length; i++) {
			final String s = tab[i];
			methodName.append(s.charAt(0) + s.substring(1).toLowerCase());
		}
		return methodName.toString();
	}

	/**
	 * Converts a signature to its string representation. This is used by {@link MethodFinder}
	 * to store the signature of methods in a map.
	 * 
	 * @param methodName the name of the method
	 * @param types      the types of the arguments of the method
	 * @return a string representation of the signature
	 */
	public static String getSignature(String methodName, Class<?>... types) {
		if (types != null) {
			MethodFinder.convertPrimitiveToObjectTypes(types);
			StringBuilder methodSignature = new StringBuilder(methodName);
			for (Class<?> c : types) {
				methodSignature.append(c.getName());
			}
			return methodSignature.toString().intern();
		}
		return methodName.intern();
	}

	/**
	 * Converts the primitive types in the array to their corresponding wrapper classes.
	 * 
	 * @param parameters the parameters to convert
	 * @return the converted parameters array
	 */
	public static Class<?>[] convertArgToTypes(Object[] parameters) {
		Class<?>[] paramClasses = new Class<?>[parameters.length];
		for (int i = 0; i < paramClasses.length; i++) {
			if (parameters[i] != null) {
				paramClasses[i] = parameters[i].getClass();
			}
		}
		return paramClasses;
	}

}
