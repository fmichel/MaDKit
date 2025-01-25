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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import madkit.kernel.Agent;

/**
 * This class is responsible for scanning the classpath and finding all the subclasses of
 * a given class.
 */
class ClassScanner {

	/**
	 * Find subclasses.
	 *
	 * @param <T> the generic type
	 * @param superClass the super class
	 * @return the list< class<? extends t>>
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ClassNotFoundException the class not found exception
	 */
	public static <T> List<Class<? extends T>> findSubclasses(Class<T> superClass)
			throws IOException, ClassNotFoundException {
		List<Class<? extends T>> subclasses = new ArrayList<>();
		Set<String> classNames = getClassNames();
		classNames.removeIf(c -> c.contains("javafx") || c.contains("it.uni") || c.contains("org.")
				|| c.contains("picocli") || c.contains("com."));
		System.err.println(classNames);

		for (String className : classNames) {
			try {
				Class<?> clazz = Class.forName(className, false, ClassScanner.class.getClassLoader());
				if (superClass.isAssignableFrom(clazz) && !clazz.equals(superClass)
						&& !Modifier.isAbstract(clazz.getModifiers())) {
					subclasses.add((Class<? extends T>) clazz);
				}
			} catch (ClassNotFoundException | NoClassDefFoundError e) {
				e.printStackTrace();
			}
		}

		return subclasses;
	}

	private static Set<String> getClassNames() throws IOException {
		Set<String> classNames = new HashSet<>();
		String classpath = System.getProperty("jdk.module.path");
		String[] paths = classpath.split(File.pathSeparator);

		for (String path : paths) {
			File file = new File(path);
			if (file.isDirectory()) {
				classNames.addAll(findClassesInDirectory(file, ""));
			} else if (file.getName().endsWith(".jar")) {
				classNames.addAll(findClassesInJar(file));
			}
		}

		return classNames;
	}

	private static Set<String> findClassesInDirectory(File directory, String packageName) {
		Set<String> classNames = new HashSet<>();
		File[] files = directory.listFiles();

		if (files != null) {
			for (File file : files) {
				if (file.isDirectory()) {
					classNames.addAll(findClassesInDirectory(file, packageName + file.getName() + "."));
				} else if (file.getName().endsWith(".class")) {
					String className = packageName + file.getName().substring(0, file.getName().length() - 6);
					classNames.add(className);
				}
			}
		}

		return classNames;
	}

	private static Set<String> findClassesInJar(File jarFile) throws IOException {
		Set<String> classNames = new HashSet<>();
		try (java.util.jar.JarFile jar = new java.util.jar.JarFile(jarFile)) {
			Enumeration<java.util.jar.JarEntry> entries = jar.entries();
			while (entries.hasMoreElements()) {
				java.util.jar.JarEntry entry = entries.nextElement();
				if (entry.getName().endsWith(".class")) {
					String className = entry.getName().replace('/', '.').substring(0, entry.getName().length() - 6);
					classNames.add(className);
				}
			}
		}
		return classNames;
	}

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		try {
			List<Class<? extends Agent>> subclasses = findSubclasses(Agent.class);
			for (Class<? extends Agent> subclass : subclasses) {
				System.out.println(subclass.getName());
			}
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}
