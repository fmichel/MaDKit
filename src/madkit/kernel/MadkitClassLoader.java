/*
 * Copyright 1997-2011 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MadKit.
 * 
 * MadKit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MadKit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with MadKit. If not, see <http://www.gnu.org/licenses/>.
 */

package madkit.kernel;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

import madkit.gui.DemoModel;
import madkit.gui.menu.LaunchAgentsMenu;
import madkit.gui.menu.LaunchSessionMenu;

/**
 * @author Fabien Michel
 * @author Jacques Ferber
 * @since MadKit 4.0
 * @version 5.1
 * 
 */
public class MadkitClassLoader extends URLClassLoader {

	private Collection<String> classesToReload;
	final private Madkit madkit;
	private Set<String> agentClasses = new TreeSet<String>();
	private Set<DemoModel> demos = new HashSet<DemoModel>();
	private Set<URL> scannedURLs = new HashSet<URL>();

	//	Class<? extends AbstractAgent> loadAgentClass(String name) throws ClassNotFoundException{
	//		return (Class<? extends AbstractAgent>) loadClass(name);
	//	}
	
	/**
	 * @param urls
	 * @param parent
	 * @throws ClassNotFoundException 
	 */
	MadkitClassLoader(Madkit m, URL[] urls, ClassLoader parent, Collection<String> toReload){
		super(urls, parent);
		if(toReload != null)
			classesToReload = new HashSet<String>(toReload);
		madkit = m;
	}

	@Override
	protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		Class<?> c;
		if(classesToReload != null && classesToReload.contains(name)){
			c = findLoadedClass(name);
			if(c != null){
				Logger l = madkit.getKernel().logger;
				if (l != null) {
					l.log(Level.FINE, "Already defined " + name + " : NEED NEW MCL");
				}
				MadkitClassLoader mcl = new MadkitClassLoader(madkit,getURLs(),this, classesToReload);
				mcl.scannedURLs = scannedURLs;
				mcl.agentClasses = agentClasses;
				mcl.demos = demos;
				classesToReload.remove(name);
				madkit.setMadkitClassLoader(mcl);
				c = mcl.loadClass(name, resolve);
			}
			else{// Never defined nor reloaded : go for defining
				addUrlAndloadClasses(name);
				classesToReload = null;
				return loadClass(name, resolve);// I should now find it on this next try
			}
		}else{
			c = findLoadedClass(name);
		}
		if(c == null){
			return super.loadClass(name, resolve);
		}
		if(resolve)
			resolveClass(c);
		return c;
	}
	
//	Class<? extends AbstractAgent> loadAgentClass(String name) throws ClassNotFoundException{
//		return (Class<? extends AbstractAgent>) loadClass(name);
//	}



//private URL getclassPathUrl(String name) {
//	URL url = this.getResource("/"+name.replace('.', '/')+".class");
//	if(url != null){//TODO if url is null return warning
//		String packageName = "";
//		if (name.contains(".")) {
//			packageName = name.substring(0, name.lastIndexOf('.')+1);
//		}
//		//		for(String s : packageName.split("\\."))
//		//			System.err.println("\nsplit"+s);
//		int deepness = packageName.split("\\.").length;
//		String urlPath = url.getPath();
//		File resourceDir = new File(urlPath.substring(0, urlPath.lastIndexOf('/')));
//		for (int i = 0; i < deepness; i++) {
//			resourceDir = resourceDir.getParentFile();
//		}
//		try {
//			return resourceDir.toURI().toURL();
//		} catch (MalformedURLException e) {
//		}
//	}
//	return null;
//}


	/**
	 * Asks the MadKit class loader to reload the class 
	 * byte code so that new instances, created
	 * using {@link Class#newInstance()} on a class object obtained with
	 * {@link #getNewestClassVersion(String)}, will reflect
	 * compilation changes during run time.
	 * 
	 * @param name The fully qualified class name of the class
	 * @return <code>true</code> if the class has been successfully 
	 * @throws ClassNotFoundException if the class cannot be found on the class path
	 */
public boolean reloadClass(String name) throws ClassNotFoundException{//TODO return false and return code
//	System.err.println(name.replace('.', '/')+".class");
	if(getResource(name.replace('.', '/')+".class") == null)
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
 * returns a different version than {@link Class#forName(String)} 
 * if the caller using it has not been reloaded at the same time. This is because {@link Class#forName(String)} 
 * uses the {@link ClassLoader} of the current class while this method uses the last class loader which is used by
 * MadKit, i.e. the one created for loading classes on which {@link #reloadClass(String)} has been invoked.
 * Especially, {@link AbstractAgent#launchAgent(String, int, boolean)} always uses the newest version of the agent class.
 * 
 * @param className the fully qualified name of the desired class.
 * @return the newest version of a class object given its name.
 * @throws ClassNotFoundException 
 * @since MadKit 5.0.0.8
 */
public Class<?> getNewestClassVersion(final String className) throws ClassNotFoundException {
	return loadClass(className);
}



void loadJarsFromPath(String path){
	File demoDir = new File(path);
	if(demoDir.isDirectory()){
		for (File f : demoDir.listFiles(new FileFilter() {
			public boolean accept(File pathname) {
				return pathname.getName().endsWith(".jar");
			}
		})) {
			try {
				addURL(f.toURI().toURL());
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
}
}

//	Class<? extends AbstractAgent> loadAgentClass(String name) throws ClassNotFoundException{
//		return (Class<? extends AbstractAgent>) loadClass(name);
//	}

private void addUrlAndloadClasses(String name) {
	URL url = this.getResource("/"+name.replace('.', '/')+".class");
	if(url != null){//TODO if url is null return warning
		String packageName = "";
		if (name.contains(".")) {
			packageName = name.substring(0, name.lastIndexOf('.')+1);
		}
		//		for(String s : packageName.split("\\."))
		int deepness = packageName.split("\\.").length;
		String urlPath = url.getPath();
		File packageDir = new File(urlPath.substring(0, urlPath.lastIndexOf('/')));
		File cpDir = new File(urlPath.substring(0, urlPath.lastIndexOf('/')));
		for (int i = 0; i < deepness; i++) {
			cpDir = cpDir.getParentFile();
		}
		for(String fileName : packageDir.list()){
			if(fileName.endsWith(".class")){
//				System.err.println("\nt"+this+" trying to define "+fileName+" time stamp "+new File(packageDir+"/"+fileName).lastModified());
				try {
					findClass(packageName+fileName.substring(0, fileName.length()-6));
				} catch (ClassNotFoundException e) {
					e.printStackTrace();//TODO log this this should not happen
				}
			}
		}
	}
}

private void scanClassPathForAgentClasses() {
	if(scannedURLs == null){
		scannedURLs = new HashSet<URL>();
		agentClasses = new TreeSet<String>();
	}
	for(URL dir : getURLs()){
		if(! scannedURLs.add(dir))
			continue;
		if(dir.toString().contains("rsrc")){
			if(dir.toString().contains("jar:rsrc:")){//TODO externalize 
				File f = new File(madkit.getConfigOption().getProperty("Project-Code-Name")+"-"+madkit.getConfigOption().getProperty("Project-Version")+".jar");
				if (f.exists()) {
					try {
						dir = new URL(f.toURI().toURL().toString());
					} catch (MalformedURLException e) {
						e.printStackTrace();
					}
				}
			}
			else{//this is the "." dir : not interested !
				continue;
			}
		}
		if (dir.toString().endsWith(".jar")) {
			try {
				JarFile jarFile = ((JarURLConnection) new URL("jar:"+dir+"!/").openConnection()).getJarFile();
				scanJarFileForLaunchConfig(jarFile);
				agentClasses.addAll(scanJarFileForAgentClasses(jarFile));
			} catch (IOException e) {
//				getLogger().severeLog("web repo conf is not valid", e);
			}
		}
		else{
			agentClasses.addAll(scanFolderForAgentClasses(new File(dir.getFile()), null));
		}
	}
}

@Override
public String toString() {
	ClassLoader parent = getParent();
	String cp =super.toString()+" : ";
	String tab="\t";
	while(parent != null){
		cp+=tab+parent.getClass();
		tab+=tab;
		parent = parent.getParent();
	}
	for (URL url : getURLs()) {
		cp+="\n"+url;
	}
	return cp;
}

/**
 * Adds a directory or a jar file to the class path.
 * @param url the resource to add
 */
public void addToClasspath(URL url) {
	addURL(url);
	LaunchAgentsMenu.updateAllMenus();
	LaunchSessionMenu.updateAllMenus();
}

/**
 * Returns all the session configurations available on the class path
 * @return a set of session configurations available on the 
 * class path
 */
public Set<DemoModel> getAvailableConfigurations(){
	scanClassPathForAgentClasses();
	if(demos != null){
		return new HashSet<DemoModel>(demos);
	}
	return Collections.emptySet();
}

/**
 * Returns the names of all the available agent classes
 * @return All the agent classes available on the 
 * class path
 */
public Set<String> getAllAgentClasses(){
	scanClassPathForAgentClasses();
		return new TreeSet<String>(agentClasses);
}







private void scanJarFileForLaunchConfig(JarFile jarFile) {
	Attributes projectInfo = null;
	try {
		projectInfo = jarFile.getManifest().getAttributes("MadKit-Project-Info");
	} catch (IOException e) {
		return;
	}
	if(projectInfo != null){
		DemoModel demo = new DemoModel(projectInfo.getValue("Project-Name").trim(),
				projectInfo.getValue("MadKit-Args").split(" "),
				projectInfo.getValue("Description").trim());
		if(demos == null){
			demos = new HashSet<DemoModel>();
		}
		demos.add(demo);
//		if (logger != null) {
//			logger.finest("found demo config info " + demo);
		}
	}

private List<String> scanJarFileForAgentClasses(JarFile jarFile) {
	List<String> l = new ArrayList<String>(50);
	for (Enumeration<JarEntry> e = jarFile.entries(); e.hasMoreElements();){
		JarEntry entry = e.nextElement();
		if (! entry.isDirectory() && entry.getName().endsWith(".class")) {
			String className = fileNameToClassName(entry.getName(), null);
			if (isAgentClass(className)) {
				l.add(fileNameToClassName(entry.getName(), null));
			}
		}
	}
	return l;
}

private List<String> scanFolderForAgentClasses(final File file, final String pckName) {
	final File[] files = file.listFiles();
	if(files == null)
		return Collections.emptyList();
	final List<String> l = new ArrayList<String>();
	for(File f : files){
		if(f.isDirectory()){
			//				String pck = pckName == null ? f.getName() : pckName+"."+f.getName();
			//				if(! isKernelDirectory(pck)){
			l.addAll(scanFolderForAgentClasses(f, pckName == null ? f.getName() : pckName+"."+f.getName()));
			//				}
		}
		else if(f.getName().endsWith(".class")){
			String className = pckName+"."+f.getName().replace(".class", "");
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
		if(cl != null && AbstractAgent.class.isAssignableFrom(cl) && cl.getConstructor((Class<?>[]) null) != null && (! Modifier.isAbstract(cl.getModifiers())) && Modifier.isPublic(cl.getModifiers())){
//			for (final Constructor<?> c : cl.getConstructors()) {
//				if (c.getParameterTypes().length == 0)
//					return true;
//			}
			return true;
		}
	} catch (ClassNotFoundException e) {
		e.printStackTrace();
	} catch (NoClassDefFoundError e) {
		// TODO: the jar file is not on the MK path (IDE JUnit for instance)
		//		} catch (IllegalAccessException e) {
		//			//not public
	} catch (SecurityException e) {
	} catch (NoSuchMethodException e) {
	}
		return false;
	}

private String fileNameToClassName(String file, String classPathRoot){
	if(classPathRoot != null)
		file = file.replace(classPathRoot, "");
	return file.substring(0, file.length()-6).replace(File.separatorChar, '.');
}

}
