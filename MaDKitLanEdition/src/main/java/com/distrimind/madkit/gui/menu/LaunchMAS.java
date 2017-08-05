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

import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import com.distrimind.madkit.action.KernelAction;
import com.distrimind.madkit.gui.MASModel;
import com.distrimind.madkit.gui.SwingUtil;
import com.distrimind.madkit.i18n.Words;
import com.distrimind.madkit.kernel.AbstractAgent;
import com.distrimind.madkit.kernel.MadkitClassLoader;

/**
 * This class builds a {@link JMenu} containing all the MAS which have been
 * found on the class path, so that they can be individually launched according
 * to their configuration.
 * 
 * @author Fabien Michel
 * @since MaDKit 5.0.0.16
 * @version 0.9
 * 
 */
public class LaunchMAS extends ClassPathSensitiveMenu {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3716696545105934911L;
	final private AbstractAgent myAgent;

	/**
	 * Builds a new menu.
	 * 
	 * @param agent
	 *            the agent according to which this menu should be created, i.e. the
	 *            agent that will be responsible of the launch.
	 */
	public LaunchMAS(final AbstractAgent agent) {
		super(Words.MAS.toString());
		setMnemonic(KeyEvent.VK_S);
		myAgent = agent;
		update();
	}

	private void addTomenu(Action a, JMenu subMenu, MASModel demo) {
		JMenuItem name = new JMenuItem(a);
		String displayedName = demo.getName();
		name.setText(displayedName);
		name.setIcon(SwingUtil.MADKIT_LOGO_SMALL);
		name.setToolTipText(demo.toString());
		name.setAccelerator(null);
		name.setActionCommand(displayedName);
		subMenu.add(name);
	}

	public void update() {
		if (!myAgent.isAlive())
			return;
		removeAll();
		for (final MASModel dm : MadkitClassLoader.getAvailableConfigurations()) {
			addTomenu(KernelAction.LAUNCH_MAS.getActionFor(myAgent, dm), this, dm);
		}
		setVisible(getItemCount() != 0);
	}

}
