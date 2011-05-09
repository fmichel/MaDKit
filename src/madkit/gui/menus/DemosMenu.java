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
import java.util.Set;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import madkit.gui.DemoModel;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.9
 * @version 0.9
 * 
 */
public class DemosMenu extends JMenu {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5176941218011430194L;
	final private Set<DemoModel> demos;
	final private Action action;

	public DemosMenu(Action action, Set<DemoModel> demos) {
		super("Demos");
		this.demos = demos;
		this.action = action;
		setMnemonic(KeyEvent.VK_D);
		update();
	}

	public void update() {
		removeAll();
		for (DemoModel demo : demos) {
			addTomenu(action, this, demo);
		}
//		addTomenu(a, subMenu, className)
	}

	private void addTomenu(Action a, JMenu subMenu, DemoModel demo) {
		JMenuItem name = new JMenuItem(a);
		String displayedName = demo.getName();
		name.setText(displayedName);
		name.setToolTipText(demo.toString());
		name.setAccelerator(null);
		name.setActionCommand(displayedName);
		subMenu.add(name);
	}



}

