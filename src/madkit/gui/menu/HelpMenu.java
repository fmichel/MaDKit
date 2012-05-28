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
package madkit.gui.menu;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import madkit.gui.AboutFrame;
import madkit.i18n.Words;

/**
 * 
 * @author Fabien Michel
 * @since MadKit 5.0.0.17
 * @version 0.9
 * 
 */
final public class HelpMenu extends JMenu {
	
	final private static String MDKWeb = "http://www.madkit.net/";

	private static final long serialVersionUID = 6177193453649323680L;
	final private static ActionListener about = new ActionListener() {
		@SuppressWarnings("unused")
		@Override
		public void actionPerformed(ActionEvent e) {
			new AboutFrame();
		}
	};
	final private static ActionListener tuto = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				Desktop.getDesktop().browse(new URI(MDKWeb+e.getActionCommand()));
			} catch (IOException e1) {
			} catch (URISyntaxException e1) {
				e1.printStackTrace();
			}
		}
	};

	public HelpMenu(){
		super(Words.HELP.toString());
		setMnemonic(KeyEvent.VK_E);
		JMenuItem mi;
		if (Desktop.isDesktopSupported()) {
			mi = new JMenuItem("API");
			mi.addActionListener(tuto);
			mi.setActionCommand("api");
			add(mi);
			mi = new JMenuItem(Words.TUTORIALS.toString());
			mi.addActionListener(tuto);
			mi.setActionCommand("tutorials");
			add(mi);
			mi = new JMenuItem("Forum");
			mi.addActionListener(tuto);
			mi.setActionCommand("forum");
			add(mi);
		}
		mi = new JMenuItem(Words.ABOUT.toString());
		mi.setMnemonic(KeyEvent.VK_A);
		mi.addActionListener(about);
		add(mi);
	}
}
