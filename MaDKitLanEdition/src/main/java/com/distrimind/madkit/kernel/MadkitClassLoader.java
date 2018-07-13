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

package com.distrimind.madkit.kernel;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.distrimind.madkit.gui.MASModel;
import com.distrimind.madkit.gui.menu.ClassPathSensitiveMenu;
import com.distrimind.madkit.util.XMLUtilities;

/**
 * The MadkitClassLoader is the class loader used by MaDKit. It enables some
 * specific features such as class hot reloading, jar loading, etc.
 * 
 * @author Fabien Michel
 * @author Jacques Ferber
 * @author Jason Mahdjoub
 * @since MaDKit 4.0
 * @since MadkitLanEdition 1.0
 * @version 6.0
 * 
 */
final public class MadkitClassLoader extends URLClassLoader { // NO_UCD

	private Collection<String> classesToReload;
	// final private Madkit madkit;
	private static Set<String> agentClasses;
	// private static Set<File> mdkFiles;
	private static Set<File> xmlFiles;
	private static Set<String> mains;
	private static Set<MASModel> demos;
	private static Set<URL> scannedURLs;
	private static MadkitClassLoader currentMCL;

	static {
		init();
	}

	public static void init() {
		final URL[] urls;
		/*
		 * if(MadkitProperties.JAVAWS_IS_ON){ final JARDesc[] jars =
		 * JNLPClassLoader.getInstance().getLaunchDesc().getResources().
		 * getEagerOrAllJarDescs(true); urls = new URL[jars.length]; for (int i = 0; i <
		 * jars.length; i++) { // System.err.println(jars[i].getLocation()); urls[i] =
		 * jars[i].getLocation(); } } else{
		 */
		final String[] urlsName = System.getProperty("java.class.path").split(File.pathSeparator);
		urls = new URL[urlsName.length];
		for (int i = 0; i < urlsName.length; i++) {
			try {
				urls[i] = new File(urlsName[i]).toURI().toURL();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			// }
		}
		// final ClassLoader systemCL = MadkitClassLoader.class.getClassLoader();
		// final ClassLoader systemCL = Thread.currentThread().getContextClassLoader();
		// System.err.println(systemCL);
		currentMCL = new MadkitClassLoader(urls, MadkitClassLoader.class.getClassLoader(), null);
	}

	private MadkitClassLoader(URL[] urls, final ClassLoader parent, Collection<String> toReload) {
		super(urls, parent);
		if (toReload != null)
			classesToReload = new HashSet<>(toReload);
		currentMCL = this;
	}

	/**
	 * Returns the last class loader, thus having all the loaded jars on the
	 * classpath.
	 * 
	 * @return the last class loader.
	 */
	public static MadkitClassLoader getLoader() {
		return currentMCL;
	}

	@Override
	protected synchronized Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
		Class<?> c;
		// synchronized (getClassLoadingLock(name)) {
		if (classesToReload != null && classesToReload.contains(name)) {
			c = findLoadedClass(name);
			if (c != null) {
				// Logger l = madkit.getKernel().logger;
				// if (l != null) {
				// l.log(Level.FINE, "Already defined " + name + " : NEED NEW MCL");
				// }
				@SuppressWarnings("resource")
				MadkitClassLoader mcl = new MadkitClassLoader(getURLs(), this, classesToReload);
				classesToReload.remove(name);
				c = mcl.loadClass(name, resolve);
			} else {// Never defined nor reloaded : go for defining
				addUrlAndloadClasses(name);
				// findClass(name);
				classesToReload = null;
				return loadClass(name, resolve);// I should now find it on this next try
			}
		} else {
			c = findLoadedClass(name);
		}
		if (c == null) {
			return super.loadClass(name, resolve);
		}
		if (resolve)
			resolveClass(c);
		// }
		return c;
	}

	/**
	 * Schedule the reloading of the byte code of a class for its next loading. So
	 * new instances, created using {@link Class#newInstance()} on a class object
	 * obtained with {@link #loadClass(String)}, will reflect compilation changes
	 * during run time.
	 * 
	 * In fact, using {@link #loadClass(String)} on the current MDK class loader
	 * obtained with {@link #getLoader()} returns the class object corresponding to
	 * the last compilation of the java code available on the class path.
	 * Especially, this may return a different version than
	 * {@link Class#forName(String)} because {@link Class#forName(String)} uses the
	 * {@link ClassLoader} of the caller's current class which could be different
	 * than the current one (i.e. the one obtained {@link #getLoader()}) if several
	 * reloads have been done.
	 * 
	 * Especially, {@link AbstractAgent#launchAgent(String, int, boolean)} always
	 * uses the newest version of an agent class.
	 * 
	 * @param name
	 *            The fully qualified class name of the class
	 * @throws ClassNotFoundException
	 *             if the class cannot be found on the class path
	 */
	public static void reloadClass(String name) throws ClassNotFoundException {// TODO return false and return code
		// System.err.println(name.replace('.', '/')+".class");
		if (currentMCL.getResource(name.replace('.', '/') + ".class") == null)
			throw new ClassNotFoundException(name);
		if (currentMCL.classesToReload == null) {
			currentMCL.classesToReload = new HashSet<>();
		}
		currentMCL.classesToReload.add(name);
	}

	/**
	 * Loads all the jars present in a directory
	 * 
	 * @param directoryPath
	 *            directory's path
	 * @return <code>true</code> if at least one new jar has been loaded
	 */
	public static boolean loadJarsFromDirectory(final String directoryPath) {
		final File demoDir = new File(directoryPath);
		boolean hasLoadSomething = false;
		if (demoDir.isDirectory()) {
			for (final File f : Objects.requireNonNull(demoDir.listFiles())) {
				if (f.getName().endsWith(".jar")) {
					try {
						if (loadUrl(f.toURI().toURL()))
							hasLoadSomething = true;
					} catch (MalformedURLException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return hasLoadSomething;
	}

	// Class<? extends AbstractAgent> loadAgentClass(String name) throws
	// ClassNotFoundException{
	// return (Class<? extends AbstractAgent>) loadClass(name);
	// }

	/**
	 * used to reload classes from the target's package, ensuring accessibility
	 * 
	 * @param name
	 *            full class's name
	 */
	private void addUrlAndloadClasses(final String name) {
		if (name.startsWith("madkit.kernel."))
			return;
		final URL url = this.getResource(name.replace('.', '/') + ".class");
		if (url != null && url.getProtocol().equals("file")) {
			String packageName = getClassPackageName(name);
			packageName = packageName == null ? "" : packageName + '.';// need this to rebuild
			final String urlPath = url.getPath();
			final File packageDir = new File(urlPath.substring(0, urlPath.lastIndexOf('/')));
			for (final String fileName : Objects.requireNonNull(packageDir.list())) {
				if (fileName.endsWith(".class")) {
					try {
						final String className = packageName + fileName.substring(0, fileName.length() - 6);
						if (findLoadedClass(className) == null) {// because it could be already loaded by loading
																	// another class that depends on it
							findClass(className);
						}
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					} catch (ClassCircularityError e) {// FIXME just a reminder
						// e.printStackTrace();
					}
				}
			}
		}
	}

	/**
	 * Returns the package name for this class name. E.g.
	 * <code>java.lang.Object</code> as input gives <code>java.lang</code> as
	 * output.
	 * 
	 * @param classFullName
	 *            the full name of a class
	 * @return the package name or <code>null</code> if no package is defined
	 */
	public static String getClassPackageName(final String classFullName) {
		final int index = classFullName.lastIndexOf('.');
		return index > 0 ? classFullName.substring(0, index) : null;
	}

	/**
	 * Returns the simple name for a full class name. E.g.
	 * <code>java.lang.Object</code> as input gives <code>Object</code> as output.
	 * 
	 * @param classFullName
	 *            the full name of a class
	 * @return the simple name of a class name
	 */
	public static String getClassSimpleName(final String classFullName) {
		final int index = classFullName.lastIndexOf('.');
		return index > 0 ? classFullName.substring(index + 1, classFullName.length()) : classFullName;
	}

	private static void scanClassPathForAgentClasses() {
		if (scannedURLs == null) {
			scannedURLs = new HashSet<>();
			agentClasses = new TreeSet<>();
			demos = new HashSet<>();
			// mdkFiles = new HashSet<>();
			xmlFiles = new HashSet<>();
			mains = new HashSet<>();
		}
		for (URL dir : getLoader().getURLs()) {

			if (!scannedURLs.add(dir))
				continue;

			if (dir.getFile().endsWith(".jar")) {

				try (JarFile jarFile = ((JarURLConnection) new URL("jar:" + dir + "!/").openConnection())
						.getJarFile()) {
					scanJarFileForLaunchConfig(jarFile);
					// agentClasses.addAll(scanJarFileForLaunchConfig(jarFile));
				} catch (IOException e) {
					// madkit.getLogger().log(Level.SEVERE, "web repo conf is not valid", e);
					e.printStackTrace();
				}
			} else {
				scanFolderForAgentClasses(new File(dir.getFile()), null, dir.getPath());
			}
		}
	}

	/**
	 * 
	 * Adds a directory or a jar file to the class path.
	 * 
	 * @param url
	 *            the resource to add
	 * @return <code>true</code> if this url was not already loaded
	 */
	public static boolean loadUrl(URL url) {
		final int size = getLoader().getURLs().length;// TODO could check if present
		getLoader().addURL(url);
		if (size != getLoader().getURLs().length) {// truly loaded
			System.setProperty("java.class.path",
					System.getProperty("java.class.path") + File.pathSeparator + url.getPath());
			ClassPathSensitiveMenu.updateAllMenus();
			return true;
		}
		return false;
	}

	/**
	 * Returns all the session configurations available on the class path
	 * 
	 * @return a set of session configurations available on the class path
	 */
	public static Set<MASModel> getAvailableConfigurations() {
		scanClassPathForAgentClasses();
		return new HashSet<>(demos);
	}

	// List<AbstractAgent> createBucket(final String agentClass, int bucketSize){
	//
	// }

	/**
	 * Returns the names of all the available agent classes
	 * 
	 * @return All the agent classes available on the class path
	 */
	public static Set<String> getAllAgentClasses() {
		scanClassPathForAgentClasses();
		return new TreeSet<>(agentClasses);
	}

	/*
	 * Returns the names of all the mdk properties files available
	 * 
	 * @return All the mdk files available on the class path
	 */
	/*
	 * public static Set<File> getMDKFiles() { scanClassPathForAgentClasses();
	 * return new TreeSet<>(mdkFiles); }
	 */

	/**
	 * Returns the names of all the xml configuration files available
	 * 
	 * @return All the xml configuration file available on the class path
	 */
	public static Set<File> getXMLConfigurations() {
		scanClassPathForAgentClasses();
		return new TreeSet<>(xmlFiles);
	}

	/**
	 * @return all the agent classes having a <code>main</code> method.
	 */
	public static Set<String> getAgentsWithMain() {
		scanClassPathForAgentClasses();
		return new TreeSet<>(mains);
	}

	/*void addMASConfig(MASModel session) {
		demos.add(session);
	}*/

	private static void scanJarFileForLaunchConfig(final JarFile jarFile) {
		Attributes projectInfo = null;
		try {
			projectInfo = jarFile.getManifest().getAttributes("MaDKit-Project-Info");
		} catch (Exception e) {
			// not a valid MDK jar file
		}
		if (projectInfo != null) {
			// Logger l = madkit.getLogger();
			String mdkArgs = projectInfo.getValue("MaDKit-Args");// TODO MDK files
			if (check(mdkArgs)) {
				final String projectName = projectInfo.getValue("Project-Name").trim();
				final String projectDescription = projectInfo.getValue("Description").trim();
				MASModel mas = new MASModel(projectName, mdkArgs.trim().split("\\s+"), projectDescription);
				demos.add(mas);
				// if (l != null) {
				// l.finest("found MAS info " + mas);
				// }
			}
			/*
			 * mdkArgs = projectInfo.getValue("MDK-Files");// recycling mdkArgs if
			 * (check(mdkArgs)) { for (String configFile : mdkArgs.split(",")) {
			 * mdkFiles.add(new File(configFile)); // if (l != null) { //
			 * l.finest("found MAS config info " + mas); // } } }
			 */
			mdkArgs = projectInfo.getValue("Main-Classes");// recycling
			if (check(mdkArgs)) {
				mains.addAll(Arrays.asList(mdkArgs.split(",")));
			}
			mdkArgs = projectInfo.getValue("XML-Files");// recycling
			if (check(mdkArgs)) {
				for (String f : mdkArgs.split(",")) {
					xmlFiles.add(new File(f));
				}
			}
			agentClasses.addAll(Arrays.asList(projectInfo.getValue("Agent-Classes").split(",")));
		}
	}

	private static boolean check(String args) {
		return args != null && !args.trim().isEmpty();
	}
	//private static final String excludeForderToScan="MaDKitLanEditionDemos/bin/main/com/distrimind/madkitdemos".replaceAll("/", File.separator);
	
	
	private static void scanFolderForAgentClasses(final File file, final String pckName, String currentUrlPath) {
		final File[] files = file.listFiles();
		if (files != null) {
			for (File f : files) {
				final String fileName = f.getName();
				if (f.isDirectory()) {
					
					scanFolderForAgentClasses(f, pckName == null ? fileName : pckName + "." + fileName, currentUrlPath);
				} else {
					if (fileName.endsWith(".class")) {
						String className = (pckName == null ? "" : pckName + ".") + fileName.replace(".class", "");
						if (isAgentClass(className)) {
							agentClasses.add(className);
						}
					}
					/*
					 * else if (fileName.endsWith(".mdk")) { mdkFiles.add(new
					 * File(f.getPath().substring(currentUrlPath.length()))); }
					 */
					else if (fileName.endsWith(".xml")) {
						final File xmlFile = new File(f.getPath().substring(currentUrlPath.length()));
						try {
							// System.err.println(xmlFile);
							Document dom = XMLUtilities.getDOM(xmlFile);
							if (dom != null && dom.getDocumentElement().getNodeName().equals(XMLUtilities.MDK)) {
								xmlFiles.add(xmlFile);
							}
						} catch (SAXException | IOException | ParserConfigurationException e) {
							// e.printStackTrace();
							// FIXME should be logged
						}
					}
				}
			}
		}
	}

	private static boolean isAgentClass(final String className) {
		try {
			final Class<?> cl = getLoader().loadClass(className);
			if (cl != null && AbstractAgent.class.isAssignableFrom(cl) && cl.getConstructor((Class<?>[]) null) != null
					&& (!Modifier.isAbstract(cl.getModifiers())) && Modifier.isPublic(cl.getModifiers())) {
				try {
					cl.getDeclaredMethod("main", String[].class);
					mains.add(className);// if previous line succeeded
				} catch (NoSuchMethodException ignored) {
				}
				return true;
			}
		} catch (VerifyError | ClassNotFoundException | NoClassDefFoundError | SecurityException
				| NoSuchMethodException e) {// FIXME just a reminder
		}
		return false;
	}

	private static String findJExecutable(File dir, String executable) {
		dir = new File(dir, "bin");
		if (dir.exists()) {
			for (File candidate : Objects.requireNonNull(dir.listFiles())) {
				if (candidate.getName().contains(executable)) {
					return candidate.getAbsolutePath();
				}
			}
		}
		return null;
	}

	/**
	 * Find a JDK/JRE program
	 * 
	 * @param executable
	 *            the name of the Java program to look for. E.g. "jarsigner",
	 *            without file extension.
	 * @return the path to the executable or <code>null</code> if not found.
	 */
	public static String findJavaExecutable(String executable) {
		File lookupDir = new File(System.getProperty("java.home"));
		String exe = MadkitClassLoader.findJExecutable(lookupDir, executable);
		if (exe != null)// was jdk dir
			return exe;
		lookupDir = lookupDir.getParentFile();
		exe = MadkitClassLoader.findJExecutable(lookupDir, executable);
		if (exe != null)// was jre dir in jdk
			return exe;
		while (lookupDir != null) {
			for (final File dir : Objects.requireNonNull(lookupDir.listFiles(new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					if (pathname.isDirectory()) {
						final String dirName = pathname.getName();
						return dirName.contains("jdk") || dirName.contains("java");
					}
					return false;
				}
			}))) {
				exe = MadkitClassLoader.findJExecutable(dir, executable);
				if (exe != null)
					return exe;
			}
			lookupDir = lookupDir.getParentFile();
		}
		return null;
	}

	/*
	 * This is only used by ant scripts for building MDK jar files. This will create
	 * a file in java.io.tmpdir named agents.classes containing the agent classes
	 * which are on the class path and other information
	 * 
	 * @param args
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	/*
	 * public static void main(String[] args) throws FileNotFoundException,
	 * IOException { final java.util.Properties p = new java.util.Properties();
	 * p.setProperty("agents.classes",
	 * normalizeResult(MadkitClassLoader.getAllAgentClasses()));
	 * //p.setProperty("mdk.files",
	 * normalizeResult(MadkitClassLoader.getMDKFiles())); p.setProperty("xml.files",
	 * normalizeResult(MadkitClassLoader.getXMLConfigurations()));
	 * p.setProperty("main.classes",
	 * normalizeResult(MadkitClassLoader.getAgentsWithMain())); final String
	 * findJavaExecutable = findJavaExecutable("jarsigner"); if (findJavaExecutable
	 * != null) { p.setProperty("jarsigner.path", findJavaExecutable); } try(final
	 * FileOutputStream out = new FileOutputStream(new
	 * File(System.getProperty("java.io.tmpdir")+File.separatorChar+
	 * "agentClasses.properties"))){
	 * p.store(out,MadkitClassLoader.getLoader().toString()); } }
	 */

	/**
	 * format the toString of a collection: Remove brackets and space
	 * 
	 * 
	 * @return the parsed string
	 */
	/*
	 * private static String normalizeResult(final Set<?> set) { final String s =
	 * set.toString(); return s.substring(1,s.length()-1).replace(", ", ","); }
	 */

	@Override
	public String toString() {
		return "MCL CP : " + Arrays.deepToString(getURLs())
		/* +"\nmains="+getAgentsWithMain() */;// TODO check why this error occurs
	}

}
