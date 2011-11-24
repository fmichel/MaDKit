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
import static java.awt.event.KeyEvent.VK_I;
import static java.awt.event.KeyEvent.VK_J;
import static java.awt.event.KeyEvent.VK_K;
import static java.awt.event.KeyEvent.VK_U;
import static java.awt.event.KeyEvent.VK_W;

import java.awt.event.ActionEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.EnumSet;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import madkit.agr.LocalCommunity;
import madkit.agr.LocalCommunity.Groups;
import madkit.agr.LocalCommunity.Roles;
import madkit.gui.GUIMessage;
import madkit.gui.GUIMessage;
import madkit.kernel.AbstractAgent;
import madkit.kernel.ActionInfo;
import madkit.kernel.KernelAction;
import madkit.kernel.MKAbstractAction;
import madkit.messages.KernelMessage;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.9
 * @version 0.9
 * 
 */
@SuppressWarnings("serial")
public enum GUIManagerAction {


	LOAD_JAR_FILE(VK_J), 
	ICONIFY_ALL(VK_U),
	DEICONIFY_ALL(VK_I),
	KILL_AGENTS(VK_K),
	CONNECT_WEB_REPO(VK_W),

	SETUP_AGENT_GUI(Integer.MAX_VALUE), 
	DISPOSE_AGENT_GUI(Integer.MAX_VALUE), 
	MADKIT_LAUNCH_SESSION(Integer.MAX_VALUE), 
	MADKIT_KILL_AGENT(Integer.MAX_VALUE);

	private ActionInfo actionInfo;
	final private int keyEvent;

	private GUIManagerAction(int keyEvent){
		this.keyEvent = keyEvent;
	}


	public Action getActionFor(final AbstractAgent guiManager, final Object... commandOptions){
		if(actionInfo == null)
			actionInfo = new ActionInfo(this,keyEvent);
		return new MKAbstractAction(actionInfo){
			@Override
			public void actionPerformed(ActionEvent e) {
				guiManager.receiveMessage(new GUIMessage(GUIManagerAction.this, commandOptions));
			}
		};
	}

	public static void addAllActionsTo(JComponent menuOrToolBar, final AbstractAgent guiManager){
		try {//this bypasses class incompatibility
			final Method add = menuOrToolBar.getClass().getMethod("add", Action.class);
			final Method addSeparator = menuOrToolBar.getClass().getMethod("addSeparator");
			for (final GUIManagerAction mkA : EnumSet.allOf(GUIManagerAction.class)) {
				if(mkA == CONNECT_WEB_REPO)
					return;
				Action a = mkA.getActionFor(guiManager);
				if (mkA == LOAD_JAR_FILE) {//TODO move that code in manager
					a = new MKAbstractAction(mkA.actionInfo){
						@Override
						public void actionPerformed(ActionEvent e) {
							guiManager.sendMessage(LocalCommunity.NAME, Groups.SYSTEM, Roles.KERNEL, 
									new KernelMessage(KernelAction.LOAD_JAR_FILE, mkA.getJarUrl()));
							
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
		return MKAbstractAction.enumToMethodName(this);
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



