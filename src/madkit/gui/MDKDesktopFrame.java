/*
 * Copyright 2013 Fabien Michel
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
package madkit.gui;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JToolBar;

import madkit.gui.menu.HelpMenu;
import madkit.gui.menu.LaunchAgentsMenu;
import madkit.gui.menu.LaunchMAS;
import madkit.gui.menu.LaunchMDKConfigurations;
import madkit.gui.menu.LaunchMain;
import madkit.gui.menu.LaunchXMLConfigurations;
import madkit.gui.menu.MadkitMenu;
import madkit.gui.toolbar.MadkitToolBar;
import madkit.kernel.AbstractAgent;
import madkit.kernel.Madkit;

/**
 * A class that could be overridden to define a new 
 * desktop frame for MaDKit
 * 
 * @author Fabien Michel
 * @since MadKit 5.0.0.22
 * @version 0.9
 * 
 */
public class MDKDesktopFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5136102562032184534L;
	final private JDesktopPane desktopPane;

	public MDKDesktopFrame() {
		super("MaDKit " + Madkit.VERSION + " Desktop ");		
		setPreferredSize(new Dimension(800, 600));
		desktopPane = new JDesktopPane();
		desktopPane.setBackground(Color.BLACK);
		add(desktopPane);
		setIconImage(SwingUtil.MADKIT_LOGO.getImage());
	}

	/**
	 * Builds tool bar for the desktop frame. By default it builds a classic MaDKit menu bar
	 * 
	 * @return the menu bar to use in the desktop frame
	 */
	public JMenuBar getMenuBar(final AbstractAgent guiManager) {
		final JMenuBar menuBar = new JMenuBar();
		menuBar.add(new MadkitMenu(guiManager));
		menuBar.add(new LaunchAgentsMenu(guiManager));
		menuBar.add(new LaunchMAS(guiManager));
		menuBar.add(new LaunchMain("Main"));
		menuBar.add(new LaunchMDKConfigurations("Configuration"));
		menuBar.add(new LaunchXMLConfigurations(guiManager, "XML"));
		menuBar.add(new HelpMenu());
		menuBar.add(Box.createHorizontalGlue());
		menuBar.add(new AgentStatusPanel(guiManager));
		return menuBar;
	}

	/**
	 * Builds tool bar for the desktop frame. By default it builds a classic MaDKit tool bar
	 * 
	 * @return the tool bar to use in the desktop frame
	 */
	public JToolBar getToolBar(final AbstractAgent guiManager) {
		return new MadkitToolBar(guiManager);
	}

	/**
	 * @return the desktopPane
	 */
	final JDesktopPane getDesktopPane() {
		return desktopPane;
	}

}
