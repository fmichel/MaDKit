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
package com.distrimind.madkit.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;

import com.distrimind.madkit.kernel.MadkitClassLoader;

/**
 * 
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MadkitLanEdition 1.7
 */

public class ReflectionTools {
	public static Object invoke(Method m, Object o, Object... args) throws InvocationTargetException {
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

	public static Method getMethod(final Class<?> c, final String method_name, final Class<?>... parameters) {
		return AccessController.doPrivileged(new PrivilegedAction<Method>() {

			@Override
			public Method run() {
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
		});
		
	}

	public static <E> Constructor<E> getConstructor(final Class<E> c, final Class<?>... parameters) {
		return AccessController.doPrivileged(new PrivilegedAction<Constructor<E>>() {

			@Override
			public Constructor<E> run() {
				try {
					Constructor<E> m = c.getDeclaredConstructor(parameters);
					m.setAccessible(true);
					return m;
				} catch (SecurityException | NoSuchMethodException e) {
					System.err.println("Impossible to access to the constructor of the class " + c.getCanonicalName()
							+ ". This is an inner bug of MadKitLanEdition. Please contact the developers. Impossible to continue. See the next error :");
					e.printStackTrace();
					System.exit(-1);
					return null;
				}
			}
		});
		
	}

	public static Class<?> loadClass(String class_name) {
		try {
			return Class.forName(class_name, true, MadkitClassLoader.getSystemClassLoader());
		} catch (SecurityException | ClassNotFoundException e) {
			System.err.println("Impossible to access to the class " + class_name
					+ ". This is an inner bug of MadKitLanEdition. Please contact the developers. Impossible to continue. See the next error :");
			e.printStackTrace();
			System.exit(-1);
			return null;
		}
	}

}
