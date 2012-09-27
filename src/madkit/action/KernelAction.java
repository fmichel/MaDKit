/*
 * Copyright 1997-2012 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MaDKit.
 * 
 * MaDKit is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * MaDKit is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MaDKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.action;

import static java.awt.event.KeyEvent.VK_C;
import static java.awt.event.KeyEvent.VK_D;
import static java.awt.event.KeyEvent.VK_DOLLAR;
import static java.awt.event.KeyEvent.VK_N;
import static java.awt.event.KeyEvent.VK_O;
import static java.awt.event.KeyEvent.VK_Q;
import static java.awt.event.KeyEvent.VK_R;
import static java.awt.event.KeyEvent.VK_T;
import static java.awt.event.KeyEvent.VK_W;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.EnumSet;
import java.util.ResourceBundle;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JToolBar;

import madkit.agr.LocalCommunity;
import madkit.agr.LocalCommunity.Groups;
import madkit.agr.Organization;
import madkit.i18n.I18nUtilities;
import madkit.kernel.AbstractAgent;
import madkit.message.KernelMessage;

/**
 * Enum representing kernel actions. This especially could
 * be used to communicate with the kernel in order to
 * trigger kernel's actions.
 * It could be used by any agent to interact with the kernel
 * by creating {@link Action} using {@link #getActionFor(AbstractAgent, Object...)}.
 * 
 * 
 * @author Fabien Michel
 * @since MaDKit 5.0.0.14
 * @version 0.9
 * 
 */
public enum KernelAction {

	/**
	 * Close the kernel 
	 */
	EXIT(VK_Q),
	/**
	 *  Clone the kernel with its initial options
	 */
	COPY(VK_C),
	/**
	 *  Restart the kernel with its initial options
	 */
	RESTART(VK_R),
	/**
	 * Start the network
	 */
	LAUNCH_NETWORK(VK_W),
	/**
	 * Stop the network
	 */
	STOP_NETWORK(VK_T),

	/**
	 * Launch jconsole on the kernel
	 * if available
	 */
	JCONSOLE(VK_N),

	/**
	 * Makes a redirection of the out and err 
	 * to a MaDKit agent.
	 */
	CONSOLE(VK_O),

	/**
	 * Load the jar files which are in the "demos" directory if there is one in to the working directory
	 */
	LOAD_LOCAL_DEMOS(VK_D),
	

	
	//	//Actions that need parameters, i.e. not global
	/**
	 * Launch an agent
	 */
	LAUNCH_AGENT(VK_DOLLAR),
	/**
	 * Launch a MAS configuration
	 */
	LAUNCH_MAS(VK_DOLLAR),
	/**
	 * Kill an agent
	 */
	KILL_AGENT(VK_DOLLAR),
	/**
	 * Connection to www.madkit.net repository
	 */
	CONNECT_WEB_REPO(VK_DOLLAR),
	/**
	 * Load a jar file so that its agent classes are then available
	 */
	LOAD_JAR_FILE(VK_DOLLAR); 


	//	final private MKAction mkAction;

	private ActionInfo actionInfo;
	final private int keyEvent;

	final static private ResourceBundle messages = I18nUtilities.getResourceBundle(KernelAction.class.getSimpleName());

	private KernelAction(int keyEvent){
		this.keyEvent = keyEvent;
	}

	/**
	 * Builds an action that will make the kernel do the
	 * corresponding operation if possible.
	 * 
	 * @param agent the agent that will send the message
	 * to the kernel
	 * @param parameters the info 
	 * @return the new corresponding action 
	 */
	public Action getActionFor(final AbstractAgent agent, final Object... parameters){
		return new MDKAbstractAction(getActionInfo()){
			/**
			 * 
			 */
			private static final long serialVersionUID = -8907472475007112860L;

			@Override
			public void actionPerformed(ActionEvent e) {
				if(agent.isAlive()){
				agent.sendMessage(
						LocalCommunity.NAME, 
						Groups.SYSTEM, 
						Organization.GROUP_MANAGER_ROLE, 
						new KernelMessage(KernelAction.this, parameters));//TODO work with AA but this is probably worthless	
				}
			}
		};
	}

	/**
	 * @return the actionInfo
	 */
	public ActionInfo getActionInfo() {
		if(actionInfo == null)
			actionInfo = new ActionInfo(this,keyEvent,messages);
		return actionInfo;
	}

	/**
	 * Could be used to add all global actions 
	 * to a menu or a toolbar
	 * 
	 * @param menuOrToolBar a {@link JMenu} or a {@link JToolBar} for instance
	 * @param agent the agent that will send the message
	 */
	@SuppressWarnings("incomplete-switch")
	public static void addAllActionsTo(JComponent menuOrToolBar, AbstractAgent agent){
		try {//this bypasses class incompatibility
			final Method add = menuOrToolBar.getClass().getMethod("add", Action.class);
			final Method addSeparator = menuOrToolBar.getClass().getMethod("addSeparator");
			for (KernelAction ka : EnumSet.allOf(KernelAction.class)) {
				if(ka == LAUNCH_AGENT)
					return;
				if((ka == LOAD_LOCAL_DEMOS || ka == RESTART) && ActionInfo.javawsIsOn)
					continue;
				if(ka == JCONSOLE && (findJconsole() == null || ActionInfo.javawsIsOn)){
					continue;
				}
				add.invoke(menuOrToolBar, ka.getActionFor(agent));
				switch (ka) {
				case EXIT:
				case RESTART:
				case STOP_NETWORK:
				case CONSOLE:
					addSeparator.invoke(menuOrToolBar);
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
	
	public static String findJconsole() {//TODO facto
		File javaHome = new File(System.getProperty("java.home"));
		File jconsole = new File(javaHome.getParent(), "bin" + File.separatorChar+ "jconsole");
		if (jconsole.exists())
			return jconsole.getAbsolutePath();
		jconsole = javaHome.getParentFile().getParentFile();
		for (File dir : jconsole.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.getName().startsWith("jdk");
			}
		})) {
			jconsole = new File(dir, "bin" + File.separatorChar + "jconsole");
			if (jconsole.exists()) {
				return jconsole.getAbsolutePath();
			}
		}
		return null;
	}

}
