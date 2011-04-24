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

import static java.awt.event.KeyEvent.VK_E;
import static java.awt.event.KeyEvent.VK_F;
import static java.awt.event.KeyEvent.VK_G;
import static java.awt.event.KeyEvent.VK_H;
import static java.awt.event.KeyEvent.VK_I;
import static java.awt.event.KeyEvent.VK_J;
import static java.awt.event.KeyEvent.VK_K;
import static java.awt.event.KeyEvent.VK_N;
import static java.awt.event.KeyEvent.VK_O;
import static java.awt.event.KeyEvent.VK_Q;
import static java.awt.event.KeyEvent.VK_R;
import static java.awt.event.KeyEvent.VK_S;
import static java.awt.event.KeyEvent.VK_T;
import static java.awt.event.KeyEvent.VK_U;
import static java.awt.event.KeyEvent.VK_W;
import static java.awt.event.KeyEvent.VK_Y;
import static java.awt.event.KeyEvent.VK_Z;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.net.MalformedURLException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JSlider;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileNameExtensionFilter;

import madkit.kernel.AbstractAgent;
import madkit.kernel.KernelAddress;
import madkit.kernel.Madkit;
import madkit.kernel.Scheduler;
import madkit.kernel.Scheduler.State;
import madkit.messages.ObjectMessage;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.9
 * @version 0.9
 * 
 */
public enum MadkitActions implements MK_Action {

	MADKIT_EXIT_ACTION(new ImageIcon(MadkitActions.class.getResource("images/madkit/exitMadKit.png")),VK_E),
	MADKIT_LAUNCH_NETWORK(new ImageIcon(MadkitActions.class.getResource("images/madkit/network_local.png")),VK_N),
	MADKIT_STOP_NETWORK(new ImageIcon(MadkitActions.class.getResource("images/madkit/network_local.png")),VK_T),
	MADKIT_ICONIFY_ALL(new ImageIcon(MadkitActions.class.getResource("images/madkit/iconify.png")),VK_U),
	MADKIT_DEICONIFY_ALL(new ImageIcon(MadkitActions.class.getResource("images/madkit/iconify.png")),VK_I),
	MADKIT_RESTART(new ImageIcon(MadkitActions.class.getResource("images/madkit/restart.png")),VK_R),
	MADKIT_CLONE(new ImageIcon(MadkitActions.class.getResource("images/madkit/restart.png")),VK_Y),
	MADKIT_KILL_AGENTS(null,VK_K),
	CONNECT_WEB_REPO(null,VK_W),
	
	MADKIT_LOAD_JAR_FILE(null,VK_J), 
	AGENT_SETUP_GUI(null,VK_J), 
	AGENT_DISPOSE_GUI(null,VK_J), 
	MADKIT_LAUNCH_DEMO(null,VK_J), 
	AGENT_LAUNCH_AGENT(null,VK_J);
	
	final private ImageIcon imageIcon;
	
	final Action getStandardAction(final AbstractAgent guiManager){
		return new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				this.setEnabled(false);
				guiManager.receiveMessage(new GUIMessage(MadkitActions.this, e.getActionCommand()));
				this.setEnabled(true);
			}
		};
	}
	
	public ImageIcon getImageIcon() {
		return imageIcon;
	}

	public int getKeyEvent() {
		return keyEvent;
	}

	final private int keyEvent;
	private static HashMap<KernelAddress,Map<MadkitActions,Action>> globalActions;
	
	private MadkitActions(ImageIcon ii, int keyEvent){
		imageIcon = ii;
		this.keyEvent = keyEvent;
	}
	
	static void registerGlobalActions(GUIManagerAgent guiManager){
		if (globalActions == null)
			globalActions = new HashMap<KernelAddress, Map<MadkitActions, Action>>();
		Map<MadkitActions, Action> actions = new HashMap<MadkitActions, Action>();
		for(MadkitActions mkA : EnumSet.allOf(MadkitActions.class)){
			actions.put(mkA, mkA.buildAction(guiManager));
		}
		globalActions.put(guiManager.getKernelAddress(), actions);
	}
	
	public Action getAction(AbstractAgent agent){
		return globalActions.get(agent.getKernelAddress()).get(this);
	}
	
	private Action buildAction(final GUIManagerAgent agent){
		Action a;
		switch (this) {
			case AGENT_SETUP_GUI:
		case AGENT_DISPOSE_GUI:
			return null;
		case MADKIT_EXIT_ACTION:
		case MADKIT_ICONIFY_ALL:
		case MADKIT_DEICONIFY_ALL:
		case MADKIT_RESTART:
		case CONNECT_WEB_REPO:
		case MADKIT_KILL_AGENTS:
		case MADKIT_LAUNCH_DEMO:
		case MADKIT_CLONE:
			a = getStandardAction(agent); 
			break;
		case MADKIT_LAUNCH_NETWORK:
		case MADKIT_STOP_NETWORK:
			a = getStandardAction(agent); 
			a.putValue(Action.SHORT_DESCRIPTION, a.getValue(Action.SHORT_DESCRIPTION)
					+ agent.getKernelAddress().toString());
			break;
		case MADKIT_LOAD_JAR_FILE:
			a = getLoadJarAction(agent);
			break;
		case AGENT_LAUNCH_AGENT:
			return AgentAction.initAction(this, getLaunchAgentAction(agent));
		default:
			throw new AssertionError(this);
		}
		if(a == null)
			return null;
		return AgentAction.initAction(this, a);
	}


	private Action getLaunchAgentAction(final GUIManagerAgent agent) {
		return new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setEnabled(false);
				agent.receiveMessage(new GUIMessage(MadkitActions.this, e.getActionCommand()));
				setEnabled(true);
			}
		};
	}

	private Action getLoadJarAction(final AbstractAgent agent) {
		return new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
			    JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
			    chooser.setFileFilter(new FileNameExtensionFilter("Jar file", "jar"));
			    int returnVal = chooser.showOpenDialog(null);
			    if(returnVal == JFileChooser.APPROVE_OPTION) {
			   	 try {
			   		 agent.receiveMessage(new GUIMessage(MADKIT_LOAD_JAR_FILE, chooser.getSelectedFile().toURI().toURL()));
					} catch (MalformedURLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
			    }
			}
		};
	}
}

	

