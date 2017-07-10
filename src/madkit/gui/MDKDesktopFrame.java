/*
 * Copyright or © or Copr. Fabien Michel, Olivier Gutknecht, Jacques Ferber (1997)

fmichel@lirmm.fr
olg@no-distance.net
ferber@lirmm.fr

This software is a computer program whose purpose is to 
provide a lightweight Java library for designing and simulating Multi-Agent Systems (MAS).

This software is governed by the CeCILL-C license under French law and
abiding by the rules of distribution of free software.  You can  use, 
modify and/ or redistribute the software under the terms of the CeCILL-C
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info". 

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability. 

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or 
data to be ensured and,  more generally, to use and operate it in the 
same conditions as regards security. 

The fact that you are presently reading this means that you have had
knowledge of the CeCILL-C license and that you accept its terms.
 */
package madkit.gui;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JToolBar;

import madkit.gui.menu.DisplayMenu;
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
public class MDKDesktopFrame extends JFrame implements PrintableFrame{

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
		final String key = getClass().getName() + "_BGC";
//		setBackground(new Color(SwingUtil.UI_PREFERENCES.getInt(key, Color.BLACK.getRGB())));
//		addPropertyChangeListener(new PropertyChangeListener() {
//			@Override
//			public void propertyChange(PropertyChangeEvent evt) {
//				if(evt.getPropertyName().equals("background")){
//					SwingUtil.UI_PREFERENCES.putInt(key, ((Color) evt.getNewValue()).getRGB());
//				}
//			}
//		});
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
		menuBar.add(new DisplayMenu(this));
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
	
	@Override
	public void setBackground(Color bgColor) {
		super.setBackground(bgColor);
		if (desktopPane != null) {
			desktopPane.setBackground(bgColor);
		}
	}

}
