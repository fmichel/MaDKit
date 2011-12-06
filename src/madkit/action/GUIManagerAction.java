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
package madkit.action;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.EnumSet;
import java.util.ResourceBundle;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import madkit.agr.LocalCommunity;
import madkit.agr.LocalCommunity.Groups;
import madkit.agr.Organization;
import madkit.i18n.I18nUtilities;
import madkit.kernel.AbstractAgent;
import madkit.kernel.AgentAddress;
import madkit.kernel.Message;
import madkit.message.GUIMessage;
import madkit.message.KernelMessage;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.14
 * @version 0.9
 * 
 */
@SuppressWarnings("serial")
public enum GUIManagerAction {


	LOAD_JAR_FILE(KeyEvent.VK_J), 
	ICONIFY_ALL(KeyEvent.VK_U),
	DEICONIFY_ALL(KeyEvent.VK_I),
	KILL_AGENTS(KeyEvent.VK_A),
	//	CONNECT_WEB_REPO(VK_W),

	SETUP_AGENT_GUI(KeyEvent.VK_DOLLAR), 
	LOG_LEVEL(KeyEvent.VK_DOLLAR),
	WARNING_LOG_LEVEL(KeyEvent.VK_DOLLAR),
	DISPOSE_AGENT_GUI(KeyEvent.VK_DOLLAR);

	private ActionInfo actionInfo;
	final private int keyEvent;

	final static private ResourceBundle messages = I18nUtilities.getResourceBundle(GUIManagerAction.class.getSimpleName());

	private GUIManagerAction(int keyEvent){
		this.keyEvent = keyEvent;
	}


	public Action getActionFor(final AbstractAgent agent, final Object... commandOptions){
		return new MKAbstractAction(getActionInfo()){
			@Override
			public void actionPerformed(ActionEvent e) {
				if (agent.isAlive()) {
					final Message m = new GUIMessage(GUIManagerAction.this, commandOptions);
					final AgentAddress guiManager = agent.getAgentWithRole(LocalCommunity.NAME, Groups.GUI, Organization.GROUP_MANAGER_ROLE);
					if(guiManager != null){
						agent.sendMessage(guiManager, m);
					}
					else{//this is the gui manager itself
						agent.receiveMessage(m);
					}
				}
			}};
	}

	/**
	 * @return the actionInfo
	 */
	public ActionInfo getActionInfo() {
		if(actionInfo == null)
			actionInfo = new ActionInfo(this,keyEvent,messages);
		return actionInfo;
	}

	public static void addAllActionsTo(JComponent menuOrToolBar, final AbstractAgent agent){
		try {//this bypasses class incompatibility
			final Method add = menuOrToolBar.getClass().getMethod("add", Action.class);
			final Method addSeparator = menuOrToolBar.getClass().getMethod("addSeparator");
			for (final GUIManagerAction mkA : EnumSet.allOf(GUIManagerAction.class)) {
				if(mkA == SETUP_AGENT_GUI)
					return;
				Action a = mkA.getActionFor(agent);
				if (mkA == LOAD_JAR_FILE) {//TODO move that code in manager
					a = new MKAbstractAction(mkA.actionInfo){
						@Override
						public void actionPerformed(ActionEvent e) {
							if (agent.isAlive()) {
								agent.sendMessage(LocalCommunity.NAME, Groups.SYSTEM, Organization.GROUP_MANAGER_ROLE, new KernelMessage(
										KernelAction.LOAD_JAR_FILE, mkA.getJarUrl()));
							}
						}
					};
				}
				add.invoke(menuOrToolBar, a);
				switch (mkA) {
				case LOAD_JAR_FILE:
					addSeparator.invoke(menuOrToolBar);
				case DEICONIFY_ALL:
					//				case KILL_AGENTS:
				default:
					break;
				}
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

	@Override
	public String toString() {
		return ActionInfo.enumToMethodName(this);
	}

	private URL getJarUrl() {
		JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
		chooser.setFileFilter(new FileNameExtensionFilter("Jar file", "jar"));
		int returnVal = chooser.showOpenDialog(null);
		if(returnVal == JFileChooser.APPROVE_OPTION) {
			try {
				return chooser.getSelectedFile().toURI().toURL();
			} catch (MalformedURLException e1) {
				e1.printStackTrace();
			}
		}
		return null;
	}

}



