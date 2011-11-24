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
package madkit.gui.actions;

import static java.awt.event.KeyEvent.VK_D;
import static java.awt.event.KeyEvent.VK_E;
import static java.awt.event.KeyEvent.VK_I;
import static java.awt.event.KeyEvent.VK_J;
import static java.awt.event.KeyEvent.VK_K;
import static java.awt.event.KeyEvent.VK_N;
import static java.awt.event.KeyEvent.VK_R;
import static java.awt.event.KeyEvent.VK_T;
import static java.awt.event.KeyEvent.VK_U;
import static java.awt.event.KeyEvent.VK_W;
import static java.awt.event.KeyEvent.VK_Y;

import java.awt.event.ActionEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import madkit.gui.GUIManagerAgent;
import madkit.gui.GUIMessage;
import madkit.kernel.AbstractAgent;
import madkit.kernel.KernelAddress;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.9
 * @version 0.9
 * 
 */
@SuppressWarnings("serial")
public enum MadkitAction implements MadkitGUIAction {
	

	EXIT(VK_E),
	RESTART(VK_R),
	CLONE(VK_Y),
	LOAD_LOCAL_DEMOS(VK_D),
	LOAD_JAR_FILE(VK_J), 
	LAUNCH_NETWORK(VK_N),
	STOP_NETWORK(VK_T),
	ICONIFY_ALL(VK_U),
	DEICONIFY_ALL(VK_I),
	KILL_AGENTS(VK_K),
	CONNECT_WEB_REPO(VK_W),
	
	AGENT_SETUP_GUI(Integer.MAX_VALUE), 
	AGENT_DISPOSE_GUI(Integer.MAX_VALUE), 
	MADKIT_LAUNCH_SESSION(Integer.MAX_VALUE), 
	MADKIT_KILL_AGENT(Integer.MAX_VALUE), 
	LAUNCH_AGENT(VK_J);
	
	private ImageIcon imageIcon;
	final private int keyEvent;
	final static private String imageDir = "images/madkit/";
	private static HashMap<KernelAddress,Map<MadkitAction,Action>> globalActions;
	
	private MadkitAction(int keyEvent){
		if (keyEvent != Integer.MAX_VALUE) {
			try {
				imageIcon = new ImageIcon(MadkitAction.class.getResource(imageDir + name() + ".png"));
			} catch (NullPointerException e) {
				System.err.println(name() + "----------------------------icon TODO");
			}
		}
		this.keyEvent = keyEvent;
	}
	
	final Action getStandardAction(final AbstractAgent guiManager){
		return new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				this.setEnabled(false);
//				guiManager.receiveMessage(new madkit.gui.GUIMessage(MadkitAction.this, e == null ? name() : e.getActionCommand()));//useful to call global actions
				this.setEnabled(true);
			}
		};
	}
	
	public ImageIcon getImageIcon() {
		return imageIcon;
	}

	public void setImageIcon(ImageIcon icon) {
		imageIcon = icon;
	}

	public int getKeyEvent() {
		return keyEvent;
	}

	public static void registerGlobalActions(GUIManagerAgent guiManager){
		if (globalActions == null)
			globalActions = new HashMap<KernelAddress, Map<MadkitAction, Action>>();
		Map<MadkitAction, Action> actions = new HashMap<MadkitAction, Action>();
		for(MadkitAction mkA : EnumSet.allOf(MadkitAction.class)){
			actions.put(mkA, mkA.buildAction(guiManager));
		}
		globalActions.put(guiManager.getKernelAddress(), actions);
	}
	
	public Action getAction(AbstractAgent agent){
		return globalActions.get(agent.getKernelAddress()).get(this);
	}
	
	public static void addAllActionsTo(JComponent menuOrToolBar, AbstractAgent agent){
		try {//this bypasses class incompatibility
			final Method add = menuOrToolBar.getClass().getMethod("add", Action.class);
			final Method addSeparator = menuOrToolBar.getClass().getMethod("addSeparator");
			for (MadkitAction mkA : EnumSet.allOf(MadkitAction.class)) {
				add.invoke(menuOrToolBar, mkA.getAction(agent));
				switch (mkA) {
				case LOAD_JAR_FILE:
				case STOP_NETWORK:
				case DEICONIFY_ALL:
//				case KILL_AGENTS:
					addSeparator.invoke(menuOrToolBar);
				default:
					break;
				}
				if(mkA == KILL_AGENTS)
					return;
			}
		} catch (InvocationTargetException e) {
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
		} catch (NoSuchMethodException e) {
		}
	}
	
	private Action buildAction(final GUIManagerAgent agent){
		Action a = null;
		switch (this) {
			case AGENT_SETUP_GUI:
		case AGENT_DISPOSE_GUI:
			return null;
		case EXIT:
		case ICONIFY_ALL:
		case DEICONIFY_ALL:
		case RESTART:
		case CONNECT_WEB_REPO:
		case KILL_AGENTS:
		case MADKIT_KILL_AGENT:
		case MADKIT_LAUNCH_SESSION:
		case CLONE:
		case LOAD_LOCAL_DEMOS:
			a = getStandardAction(agent); 
			break;
		case LAUNCH_NETWORK:
		case STOP_NETWORK:
			a = getStandardAction(agent); 
			a = Actions.initAction(this, a);
			a.putValue(Action.SHORT_DESCRIPTION, a.getValue(Action.SHORT_DESCRIPTION)
					+ agent.getKernelAddress().toString());
			return a;
		case LOAD_JAR_FILE:
//			a = getLoadJarAction(agent);
			break;
		case LAUNCH_AGENT:
			return Actions.initAction(this, getLaunchAgentAction(agent));
		default:
			throw new AssertionError(this);
		}
		if(a == null)
			return null;
		return Actions.initAction(this, a);
	}
	
	@Override
	public String toString() {
		return Actions.getDescription(this);
	}

	private Action getLaunchAgentAction(final GUIManagerAgent agent) {
		return new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setEnabled(false);
//				agent.receiveMessage(new GUIMessage(MadkitAction.this, e.getActionCommand()));
				setEnabled(true);
			}
		};
	}
//
//	private Action getLoadJarAction(final AbstractAgent agent) {
//		return new AbstractAction() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//			    JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
//			    chooser.setFileFilter(new FileNameExtensionFilter("Jar file", "jar"));
//			    int returnVal = chooser.showOpenDialog(null);
//			    if(returnVal == JFileChooser.APPROVE_OPTION) {
//			   	 try {
//			   		 agent.receiveMessage(new GUIMessage(LOAD_JAR_FILE, chooser.getSelectedFile().toURI().toURL()));
//					} catch (MalformedURLException e1) {
//						// TODO Auto-generated catch block
//						e1.printStackTrace();
//					}
//			    }
//			}
//		};
//	}
//	
}

	

