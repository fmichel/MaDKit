/*
 * MadKitLanEdition (created by Jason MAHDJOUB (jason.mahdjoub@distri-mind.fr)) Copyright (c)
 * 2015 is a fork of MadKit and MadKitGroupExtension. 
 * 
 * Copyright or © or Copr. Jason Mahdjoub, Fabien Michel, Olivier Gutknecht, Jacques Ferber (1997)
 * 
 * jason.mahdjoub@distri-mind.fr
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

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import com.distrimind.madkit.action.KernelAction;
import com.distrimind.madkit.action.MDKAbstractAction;
import com.distrimind.madkit.agr.LocalCommunity;
import com.distrimind.madkit.agr.Organization;
import com.distrimind.madkit.kernel.AbstractAgent;
import com.distrimind.madkit.kernel.MadkitClassLoader;
import com.distrimind.madkit.message.KernelMessage;

/**
 * This class builds a {@link JMenu} containing all the MDK xml configuration
 * files found on the class path. If checked, a new MaDKit instance will be used
 * for the corresponding configuration.
 * 
 * @author Fabien Michel
 * @author Jason Mahdjoub
 * @since MaDKit 5.0.2
 * @version 0.92
 * 
 */
public class LaunchXMLConfigurations extends ClassPathSensitiveMenu {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3650744981788324553L;
	protected final AbstractAgent myAgent;

	/**
	 * Builds a new menu.
	 * 
	 * @param agent the agent
	 * @param title
	 *            the title to use
	 */
	public LaunchXMLConfigurations(final AbstractAgent agent, final String title) {
		super(title);
		myAgent = agent;
		setMnemonic(KeyEvent.VK_X);
		update();
	}

	@Override
	public void update() {
		removeAll();
		final JCheckBoxMenuItem cbox = new JCheckBoxMenuItem("+ MaDKit instance");
		final Action a = new MDKAbstractAction(KernelAction.LAUNCH_XML.getActionInfo()) {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				if (myAgent.isAlive()) {
					myAgent.sendMessage(LocalCommunity.Groups.SYSTEM, Organization.GROUP_MANAGER_ROLE,
							new KernelMessage(KernelAction.LAUNCH_XML, e.getActionCommand(),
									new Boolean(cbox.getState())));
				}
			}
		};
		for (final File file : MadkitClassLoader.getXMLConfigurations()) {
			JMenuItem name = new JMenuItem(a);
			name.setActionCommand(file.toString());
			name.setText(file.toString());
			add(name);
		}
		add(cbox);
		setVisible(getItemCount() != 1);
	}

}
