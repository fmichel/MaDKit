/*
 * Copyright 1997-2011 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MadKit.
 * 
 * MadKit is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * MadKit is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with MadKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.gui.menus;

import java.awt.event.KeyEvent;
import java.util.Collection;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;


/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.9
 * @version 0.9
 * 
 */
public class AgentsMenu extends JMenu {

	/**
	 * 
	 */
	private static final long serialVersionUID = -395609483373286462L;
	final private Collection<String> classesToLaunch;
	final private Action launchAction;
	
	public AgentsMenu(final Action action, Collection<String> classesToLaunch) {
		super("Agents");
		this.classesToLaunch = classesToLaunch;
		launchAction = action;
		setMnemonic(KeyEvent.VK_G);
		update();
	}
	
	public void update(){
		removeAll();
		if (classesToLaunch.size() < 20) {
			for (String string : classesToLaunch) {
				addTomenu(launchAction, this, string, false);
			}
		}
		else{
			String pckName = null;
			JMenu subMenu = null;
			for (String string : classesToLaunch) {
				String pck = string.substring(0,string.lastIndexOf('.'));
				if(pck.equals(pckName)){
					addTomenu(launchAction, subMenu, string,true);
				}
				else{
					pckName = pck;
					subMenu = new JMenu(pck);
					add(subMenu);
					addTomenu(launchAction, subMenu, string,true);
				}
			}
		}
	}

	private void addTomenu(Action a, JMenu subMenu, String className, boolean simpleName) {
		JMenuItem name = new JMenuItem(a);
		String displayedName = simpleName ? className.substring(className.lastIndexOf('.')+1, className.length()) : className;
		name.setText(displayedName);
		name.setAccelerator(null);
		name.setActionCommand(className);
		subMenu.add(name);
	}

}
