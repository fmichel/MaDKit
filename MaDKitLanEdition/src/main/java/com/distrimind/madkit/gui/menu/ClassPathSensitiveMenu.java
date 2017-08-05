/*
 * Copyright or © or Copr. Fabien Michel, Olivier Gutknecht, Jacques Ferber (1997)
 * 
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
package com.distrimind.madkit.gui.menu;

import java.util.HashSet;
import java.util.Set;

import javax.swing.JMenu;

import com.distrimind.madkit.kernel.MadkitClassLoader;

/**
 * The super class of MaDKit menu which have to be update when the class path is
 * changed or updated with new classes or files.
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
	private static final long serialVersionUID = -8683241036884772403L;
	final static private Set<ClassPathSensitiveMenu> menus = new HashSet<>();// TODO Map

	/**
	 * Builds a new menu.
	 * 
	 * @param title
	 *            the title to use
	 */
	public ClassPathSensitiveMenu(String title) {
		super(title);
		synchronized (menus) {
			menus.add(this);
		}
	}

	/**
	 * Called by the kernel when the class path is modified. This is for instance
	 * the case when the {@link MadkitClassLoader#loadUrl(java.net.URL)} is used.
	 */
	public static void updateAllMenus() {
		synchronized (menus) {
			for (ClassPathSensitiveMenu menu : menus) {
				menu.update();
			}
		}
	}

	/**
	 * Called by {@link #updateAllMenus()} when the class path is modified. This is
	 * for instance the case when the
	 * {@link MadkitClassLoader#loadUrl(java.net.URL)} is used.
	 */
	public abstract void update();
}
