/*
 * MadKitLanEdition (created by Jason MAHDJOUB (jason.mahdjoub@distri-mind.fr)) Copyright (c)
 * 2015 is a fork of MadKit and MadKitGroupExtension. 
 * 
 * Copyright or © or Copr. Jason Mahdjoub, Fabien Michel, Olivier Gutknecht, Jacques Ferber (1997)
 * 
 * jason.mahdjoub@distri-mind.fr
 * fmichel@lirmm.fr
 * olg@no-distance.net
 * ferber@lirmm.fr
 * 
 * This software is a computer program whose purpose is to
 * provide a lightweight Java library for designing and simulating Multi-Agent Systems (MAS).
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 * 
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package com.distrimind.madkit.kernel.network.connection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.distrimind.madkit.kernel.AbstractAgent;
import com.distrimind.madkit.kernel.Agent;
import com.distrimind.madkit.message.hook.HookMessage;
import com.distrimind.madkit.message.hook.NetworkEventMessage;

/**
 * Gives access to Madkit Kernel methods
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 */
class MadkitKernelAccess {

	static Agent getMadkitKernel(AbstractAgent _requester) {
		try {
			return (Agent) invoke(m_get_madkit_kernel, _requester);
		} catch (InvocationTargetException e) {
			System.err.println("Unexpected error :");
			e.printStackTrace();
			System.exit(-1);
			return null;
		}
	}

	static void informHooks(AbstractAgent _requester, NetworkEventMessage hook_message) {
		try {
			invoke(m_inform_hooks, getMadkitKernel(_requester), hook_message);
		} catch (InvocationTargetException e) {
			System.err.println("Unexpected error :");
			e.printStackTrace();
			System.exit(-1);
		}
	}

	private static final String package_name;
	private static final Class<?> c_madkit_kernel;
	private static final Method m_get_madkit_kernel;
	private static final Method m_inform_hooks;

	static {
		package_name = AbstractAgent.class.getPackage().getName();
		c_madkit_kernel = loadClass(package_name + ".MadkitKernel");
		m_get_madkit_kernel = getMethod(AbstractAgent.class, "getMadkitKernel");
		m_inform_hooks = getMethod(c_madkit_kernel, "informHooks", HookMessage.class);
	}

	private static Object invoke(Method m, Object o, Object... args) throws InvocationTargetException {
		try {
			return m.invoke(o, args);
		} catch (IllegalAccessException | IllegalArgumentException e) {
			System.err.println("Impossible to access to the function " + m.getName() + " of the class "
					+ m.getDeclaringClass()
					+ ". This is an inner bug of MadKitLanEdition. Please contact the developers. Impossible to continue. See the next error :");
			e.printStackTrace();
			System.exit(-1);
			return null;
		}
	}

	private static Method getMethod(Class<?> c, String method_name, Class<?>... parameters) {
		try {
			Method m = c.getDeclaredMethod(method_name, parameters);
			m.setAccessible(true);
			return m;
		} catch (SecurityException | NoSuchMethodException e) {
			System.err.println("Impossible to access to the function " + method_name + " of the class "
					+ c.getCanonicalName()
					+ ". This is an inner bug of MadKitLanEdition. Please contact the developers. Impossible to continue. See the next error :");
			e.printStackTrace();
			System.exit(-1);
			return null;
		}
	}

	private static Class<?> loadClass(String class_name) {
		try {
			return Class.forName(class_name);
		} catch (SecurityException | ClassNotFoundException e) {
			System.err.println("Impossible to access to the class " + class_name
					+ ". This is an inner bug of MadKitLanEdition. Please contact the developers. Impossible to continue. See the next error :");
			e.printStackTrace();
			System.exit(-1);
			return null;
		}
	}

}
