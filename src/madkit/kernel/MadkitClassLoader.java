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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.logging.Level;

/**
 * @author Fabien Michel since v.3
 * @author Jacques Ferber
 * @since MadKit 4.0
 * @version 5.0
 * 
 */
class MadkitClassLoader extends URLClassLoader {
	
	/**
	 * @param urls
	 * @param parent
	 */
	MadkitClassLoader(URL[] urls, ClassLoader parent) {
		super(urls, parent);
	}
	
	/**
	 * @param madkitClassLoader
	 */
	MadkitClassLoader(MadkitClassLoader madkitClassLoader) {
		super(madkitClassLoader.getURLs(),madkitClassLoader);
	}

//	@Override
//	public Class<?> loadClass(final String name) throws ClassNotFoundException {
//        if(! reloadedClasses.contains(name))
//                return super.loadClass(name);
//        try {
//        	InputStream input = getClass().getResourceAsStream("/"+name.replace('.', File.separatorChar)+".class");
//            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
//            int data = input.read();
//
//            while(data != -1){
//                buffer.write(data);
//                data = input.read();
//            }
//            input.close();
//
//            byte[] classData = buffer.toByteArray();
//
//            Class<?> c = defineClass(name,classData, 0, classData.length);
//            resolveClass(c);
//            reloadedClasses.remove(name);
//            return c;
//
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace(); 
//        }
//
//        return null;
//    }
	
	void reloadClass(Madkit m, String name){//TODO if name is null
		//the new class loader required for this
		MadkitClassLoader mcl = new MadkitClassLoader(this);
		//looking for something to reload
     	InputStream input = getClass().getResourceAsStream("/"+name.replace('.', File.separatorChar)+".class");
      ByteArrayOutputStream buffer = new ByteArrayOutputStream();
      Class<?> c = null;
		try {
			int data = input.read();
			while(data != -1){
				buffer.write(data);
				data = input.read();
			}
			input.close();
			byte[] classData = buffer.toByteArray();
			c = mcl.defineClass(name, classData, 0, classData.length);
		} catch (ClassFormatError e) {
			m.kernelLog("reloading problem", Level.SEVERE, e);
		} catch (IndexOutOfBoundsException e) {
			m.kernelLog("reloading problem", Level.SEVERE, e);
		} catch (SecurityException e) {
			m.kernelLog("reloading problem", Level.SEVERE, e);
		} catch (IOException e) {
			m.kernelLog("reloading problem", Level.SEVERE, e);
		} 
		if(c != null){
			mcl.resolveClass(c);
			try {
				c.newInstance();
				m.setMadkitClassLoader(mcl);
			} catch (InstantiationException e) {
				m.kernelLog("reloading problem", Level.SEVERE, e);
			} catch (IllegalAccessException e) {
				m.kernelLog("reloading problem", Level.SEVERE, e);
			} catch (IllegalAccessError e) {
				throw e;
			}
		}
	}
	
}
