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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Fabien Michel
 * @author Jacques Ferber
 * @since MadKit 4.0
 * @version 5.1
 * 
 */
final class MadkitClassLoader extends URLClassLoader {

	private Collection<String> classesToReload;
	final private Madkit madkit;

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
				classesToReload.remove(name);
				madkit.setMadkitClassLoader(mcl);
				c = mcl.loadClass(name, resolve);
			}
			else{// Never defined nor reload : go for defining
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


boolean reloadClass(String name) throws ClassNotFoundException{//TODO return false and return code
//	System.err.println(name.replace('.', '/')+".class");
	if(name == null || getResource(name.replace('.', '/')+".class") == null)
		throw new ClassNotFoundException(name);
	if (classesToReload == null) {
		classesToReload = new HashSet<String>();
	}
	classesToReload.add(name);
	return true;
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

protected void addJar(URL url) {
	addURL(url);
}

}
