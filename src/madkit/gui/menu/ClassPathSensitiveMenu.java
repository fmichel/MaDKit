/*
 * Copyright 2013 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MaDKit.
 * 
 * MaDKit is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * MaDKit is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * MaDKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.gui.menu;

import java.util.HashSet;
import java.util.Set;

import javax.swing.JMenu;

import madkit.kernel.MadkitClassLoader;

/**
 * The super class of MaDKit menu which have to be update when
 * the class path is changed or updated with new classes or files.
 * 
 * @author Fabien Michel
 * @since MadKit 5.0.2
 * @version 0.9
 * 
 */
public abstract class ClassPathSensitiveMenu extends JMenu {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= -8683241036884772403L;
	final static private Set<ClassPathSensitiveMenu> menus = new HashSet<>();//TODO Map 

	/**
	 * Builds a new menu.
	 * 
	 * @param title the title to use
	 */
	public ClassPathSensitiveMenu(String title) {
		super(title);
		synchronized (menus) {
			menus.add(this);
		}
	}

	/**
	 * Called by the kernel when the class path is modified.
	 * This is for instance the case when the 
	 * {@link MadkitClassLoader#loadUrl(java.net.URL)}
	 * is used.
	 */
	public static void updateAllMenus() {
		synchronized (menus) {
			for (ClassPathSensitiveMenu menu : menus) {
				menu.update();
			}
		}
	}
	
	/**
	 * Called by {@link #updateAllMenus()} when the class path is modified.
	 * This is for instance the case when the 
	 * {@link MadkitClassLoader#loadUrl(java.net.URL)}
	 * is used.
	 */
	public abstract void update();
}
