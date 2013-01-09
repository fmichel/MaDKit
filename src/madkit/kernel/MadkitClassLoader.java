/*
 * Copyright 1997-2012 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MaDKit.
 * 
 * MaDKit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MaDKit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MaDKit. If not, see <http://www.gnu.org/licenses/>.
 */

package madkit.kernel;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

import madkit.gui.MASModel;
import madkit.gui.menu.LaunchAgentsMenu;
import madkit.gui.menu.LaunchMAS;
import madkit.kernel.Madkit.BooleanOption;
import madkit.kernel.Madkit.LevelOption;
import madkit.kernel.Madkit.Option;

/**
 * The MadkitClassLoader is the class loader used by MaDKit, enabling 
 * some specific features such as hot class reloading.
 * 
 * @author Fabien Michel
 * @author Jacques Ferber
 * @since MaDKit 4.0
 * @version 5.1
 * 
 */
final public class MadkitClassLoader extends URLClassLoader { // NO_UCD

	private Collection<String>	 classesToReload;
	final private Madkit			madkit;
	private Set<String>			agentClasses	= new TreeSet<String>();
	private Set<MASModel>		demos				= new HashSet<MASModel>();
	private Set<URL>				scannedURLs		= new HashSet<URL>();

	/**
	 * @param urls
	 * @param parent
	 * @throws ClassNotFoundException
	 */
	MadkitClassLoader(final Madkit m, URL[] urls, final ClassLoader parent,
			Collection<String> toReload) {
		super(urls, parent);
		if (toReload != null)
			classesToReload = new HashSet<String>(toReload);
		madkit = m;
	}

	@Override
	protected synchronized Class<?> loadClass(final String name, final boolean resolve)
			throws ClassNotFoundException {
		Class<?> c;
		if (classesToReload != null && classesToReload.contains(name)) {
			c = findLoadedClass(name);
			if (c != null) {
				Logger l = madkit.getKernel().logger;
				if (l != null) {
					l.log(Level.FINE, "Already defined " + name + " : NEED NEW MCL");
				}
				MadkitClassLoader mcl = new MadkitClassLoader(madkit, getURLs(),
						this, classesToReload);
				mcl.scannedURLs = scannedURLs;
				mcl.agentClasses = agentClasses;
				mcl.demos = demos;
				classesToReload.remove(name);
				madkit.setMadkitClassLoader(mcl);
				c = mcl.loadClass(name, resolve);
			}
			else {// Never defined nor reloaded : go for defining
				addUrlAndloadClasses(name);
				classesToReload = null;
				return loadClass(name, resolve);// I should now find it on this next try
			}
		}
		else {
			c = findLoadedClass(name);
		}
		if (c == null) {
			return super.loadClass(name, resolve);
		}
		if (resolve)
			resolveClass(c);
		return c;
	}

	/**
	 * Asks the MaDKit class loader to reload the class
	 * byte code so that new instances, created
	 * using {@link Class#newInstance()} on a class object obtained with {@link #getNewestClassVersion(String)}, will reflect
	 * compilation changes during run time.
	 * 
	 * @param name The fully qualified class name of the class
	 * @return <code>true</code> if the class has been successfully
	 * @throws ClassNotFoundException if the class cannot be found on the class path
	 */
	public boolean reloadClass(String name) throws ClassNotFoundException {// TODO return false and return code
	// System.err.println(name.replace('.', '/')+".class");
		if (getResource(name.replace('.', '/') + ".class") == null)
			throw new ClassNotFoundException(name);
		if (classesToReload == null) {
			classesToReload = new HashSet<String>();
		}
		classesToReload.add(name);
		return true;
	}

	/**
	 * returns the newest version of a class object given its name. If {@link #reloadClass(String)} has been used this
	 * returns the class object corresponding to the last compilation of the java code. Especially, in such a case, this
	 * returns a different version than {@link Class#forName(String)} if the caller using it has not been reloaded at the same time. This is because {@link Class#forName(String)} uses the
	 * {@link ClassLoader} of the current class while this method uses the last class loader which is used by
	 * MaDKit, i.e. the one created for loading classes on which {@link #reloadClass(String)} has been invoked.
	 * Especially, {@link AbstractAgent#launchAgent(String, int, boolean)} always uses the newest version of the agent class.
	 * 
	 * @param className the fully qualified name of the desired class.
	 * @return the newest version of a class object given its name.
	 * @throws ClassNotFoundException
	 * @since MaDKit 5.0.0.8
	 */
	public Class<?> getNewestClassVersion(final String className)
			throws ClassNotFoundException {
		return loadClass(className);
	}

	void loadJarsFromPath(final String path) {
		final File demoDir = new File(path);
		if (demoDir.isDirectory()) {
			for (final File f : demoDir.listFiles(new FileFilter() {

				public boolean accept(final File pathname) {
					return pathname.getName().endsWith(".jar");
				}
			})) {
				try {
					addToClasspath(f.toURI().toURL());
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	// Class<? extends AbstractAgent> loadAgentClass(String name) throws ClassNotFoundException{
	// return (Class<? extends AbstractAgent>) loadClass(name);
	// }

	/**
	 * used to reload classes from the target's package, ensuring accessibility
	 * @param name full class's name
	 */
	private void addUrlAndloadClasses(final String name) {
		final URL url = this.getResource(name.replace('.', '/') + ".class");
		if (url != null) {// TODO if url is null return warning
			String packageName = getClassPackageName(name);
			packageName = packageName == null ? "" : packageName+'.';//need this to rebuild
			final String urlPath = url.getPath();
			final File packageDir = new File(urlPath.substring(0, urlPath.lastIndexOf('/')));
			for (final String fileName : packageDir.list()) {
				if (fileName.endsWith(".class")) {
					try {
						findClass(packageName + fileName.substring(0, fileName.length() - 6));
					} catch (ClassNotFoundException e) {
						e.printStackTrace();// TODO log this this should not happen
					}
				}
			}
		}
	}
	
	/**
	 * Returns the package name for this class name. E.g. <code>java.lang.Object</code>
	 * as input gives <code>java.lang</code> as output.
	 * 
	 * @param classFullName the full name of a class
	 * @return the package name or <code>null</code> if no package is defined
	 */
	public static String getClassPackageName(final String classFullName){
		final int index = classFullName.lastIndexOf('.');
		return index > 0 ? classFullName.substring(0, index) : null;
	}
	
	/**
	 * Returns the simple name for a full class name. E.g.  <code>java.lang.Object</code>
	 * as input gives <code>Object</code> as output.
	 * 
	 * @param classFullName the full name of a class
	 * @return the package name or an empty string if no package is defined
	 */
	public static String getClassSimpleName(final String classFullName){
		final int index = classFullName.lastIndexOf('.');
		return index > 0 ? classFullName.substring(index+1, classFullName.length()) : classFullName;
	}
	

	private void scanClassPathForAgentClasses() {
		if (scannedURLs == null) {
			scannedURLs = new HashSet<URL>();
			agentClasses = new TreeSet<String>();
		}
		for (URL dir : getURLs()) {
			if (!scannedURLs.add(dir))
				continue;
			if (dir.getFile().endsWith(".jar")) {
				try {
					JarFile jarFile = ((JarURLConnection) new URL("jar:" + dir
							+ "!/").openConnection()).getJarFile();
					agentClasses.addAll(scanJarFileForLaunchConfig(jarFile));
				} catch (IOException e) {
					madkit.getLogger().log(Level.SEVERE,"web repo conf is not valid", e);
				}
			}
			else {
				agentClasses.addAll(scanFolderForAgentClasses(new File(dir.getFile()), null));
			}
		}
	}

	/**
	 * Adds a directory or a jar file to the class path.
	 * 
	 * @param url the resource to add
	 */
	public void addToClasspath(URL url) {
		addURL(url);
		LaunchAgentsMenu.updateAllMenus();
		LaunchMAS.updateAllMenus();
	}

	/**
	 * Returns all the session configurations available on the class path
	 * 
	 * @return a set of session configurations available on the
	 *         class path
	 */
	public Set<MASModel> getAvailableConfigurations() {
		scanClassPathForAgentClasses();
		if (demos != null) {
			return new HashSet<MASModel>(demos);
		}
		return Collections.emptySet();
	}

	// List<AbstractAgent> createBucket(final String agentClass, int bucketSize){
	//
	// }

	/**
	 * Returns the names of all the available agent classes
	 * 
	 * @return All the agent classes available on the
	 *         class path
	 */
	public Set<String> getAllAgentClasses() {
		scanClassPathForAgentClasses();
		return new TreeSet<String>(agentClasses);
	}

	void addMASConfig(MASModel session) {
		demos.add(session);
	}

	/**
	 * @param jarFile
	 * @return <code>true</code> if the jar contains MDK files
	 */
	private List<String> scanJarFileForLaunchConfig(final JarFile jarFile) {
		Attributes projectInfo = null;
		try {
			projectInfo = jarFile.getManifest().getAttributes(
					"MaDKit-Project-Info");
		} catch (IOException e) {
		}
		if(projectInfo == null)
			return Collections.EMPTY_LIST;
		
		final String mdkArgs = projectInfo.getValue("MaDKit-Args");
		if(mdkArgs != null && ! mdkArgs.trim().isEmpty()){
			MASModel mas = new MASModel(projectInfo.getValue("Project-Name")
					.trim(), mdkArgs.split(" "),
					projectInfo.getValue("Description").trim());
			if (demos == null) {
				demos = new HashSet<MASModel>();
			}
			demos.add(mas);
			Logger l = madkit.getLogger();
			if (l != null) {
				l.finest("found MAS config info " + mas);
			}
		}
		return Arrays.asList(projectInfo.getValue("Agent-Classes").split(","));
	}

	private List<String> scanFolderForAgentClasses(final File file,
			final String pckName) {
		final File[] files = file.listFiles();
		if (files == null)
			return Collections.emptyList();
		final List<String> l = new ArrayList<String>();
		for (File f : files) {
			if (f.isDirectory()) {
				l.addAll(scanFolderForAgentClasses(f, pckName == null ? f.getName()
						: pckName + "." + f.getName()));
			}
			else
				if (f.getName().endsWith(".class")) {
					String className = (pckName == null ? "" : pckName + ".")
							+ f.getName().replace(".class", "");
					if (isAgentClass(className)) {
						l.add(className);
					}
				}
		}
		return l;
	}

	private boolean isAgentClass(final String className) {
		try {
			final Class<?> cl = loadClass(className);
			if (cl != null && AbstractAgent.class.isAssignableFrom(cl)
					&& cl.getConstructor((Class<?>[]) null) != null
					&& (!Modifier.isAbstract(cl.getModifiers()))
					&& Modifier.isPublic(cl.getModifiers())) {
				return true;
			}
		} catch (VerifyError e) {//scala files raise problems
//			e.printStackTrace();//FIXME should be logged
		} catch (ClassNotFoundException e) {//no log here for junit !!
//			e.printStackTrace();
		} catch (NoClassDefFoundError e) {
//			e.printStackTrace(); 
		} catch (SecurityException e) {
//			e.printStackTrace();
		} catch (NoSuchMethodException e) {
//			e.printStackTrace();
		}
		return false;
	}

	/**
	 * @param dir
	 * @param executable the name of the program to look for
	 */
	private static String findJExecutable(File dir, String executable) {
		dir = new File(dir,"bin");
		if(dir.exists()){
		for (File candidate : dir.listFiles()) {
			if(candidate.getName().contains(executable)){
				return candidate.getAbsolutePath();
			}
		}
		}
		return null;
	}

	/**
	 * Find a JDK/JRE program
	 * 
	 * @param executable the name of the Java program to look for. E.g. "jarsigner", without file extension.
	 * @return the path to the executable or <code>null</code> if not found.
	 */
	public static String findJavaExecutable(String executable) {//TODO facto
		File lookupDir = new File(System.getProperty("java.home"));
		String exe = MadkitClassLoader.findJExecutable(lookupDir,executable);
		if(exe != null)// was jdk dir
			return exe;
		lookupDir = lookupDir.getParentFile();
		exe = MadkitClassLoader.findJExecutable(lookupDir,executable);
		if(exe != null)// was jre dir in jdk
			return exe;
		while(lookupDir != null){
			for (File dir : lookupDir.listFiles(new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					if(pathname.isDirectory()){
						final String dirName = pathname.getName();
						return dirName.contains("jdk") || dirName.contains("java");
					}
					return false;
				}
			})) 
			{
				exe = MadkitClassLoader.findJExecutable(dir,executable);
				if(exe != null)
					return exe;
			}
			lookupDir = lookupDir.getParentFile();
		}
		return null;
	}

	/**
	 * This is only used by ant scripts for building MDK jar files.
	 * This will create a file in java.io.tmpdir named agents.classes
	 * containing the agent classes which are on the class path
	 * 
	 * @param args
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void main(String[] args) throws FileNotFoundException, IOException {
		Madkit m = new Madkit(
				LevelOption.madkitLogLevel.toString(),Level.OFF.toString(),
				Option.launchAgents.toString(),AbstractAgent.class.getName(),
				BooleanOption.desktop.toString(),"false"
				);
		Set<String> s = m.getMadkitClassLoader().getAllAgentClasses();
		int size = s.toString().length();
		java.util.Properties p = new java.util.Properties();
		p.setProperty("agents.classes", s.toString().substring(1,size-1).replace(", ", ","));
		final String findJavaExecutable = findJavaExecutable("jarsigner");
		if (findJavaExecutable != null) {
			p.setProperty("jarsigner.path", findJavaExecutable);
		}
		p.store(new FileOutputStream(new File(System.getProperty("java.io.tmpdir")+File.separatorChar+"agentClasses.properties")),System.getProperty("java.class.path"));
//		for (String string : System.getProperty("java.class.path").split(File.pathSeparator)) {
//			System.err.println(string);
//		}
	}

}
