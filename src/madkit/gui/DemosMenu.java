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
package madkit.gui;

import java.awt.event.KeyEvent;
import java.util.Set;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.9
 * @version 0.9
 * 
 */
class DemosMenu extends JMenu {

	private GUIManagerAgent guiManager;

	DemosMenu(GUIManagerAgent guiManagerAgent) {
		super("Demos");
		guiManager = guiManagerAgent;
		setMnemonic(KeyEvent.VK_D);
		update();
	}

	void update() {
		Action a = MadkitActions.MADKIT_LAUNCH_DEMO.getAction(guiManager);
		removeAll();
		for (DemoModel string : guiManager.getDemos()) {
			addTomenu(a, this, string);
		}
//		addTomenu(a, subMenu, className)
	}

	private void addTomenu(Action a, JMenu subMenu, DemoModel className) {
		JMenuItem name = new JMenuItem(a);
		String displayedName = className.getName();
		name.setText(displayedName);
		name.setToolTipText(className.getDescription());
		name.setAccelerator(null);
		name.setActionCommand(displayedName);
		subMenu.add(name);
	}



}

