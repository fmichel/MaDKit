/*
 * Copyright 1997-2013 Fabien Michel, Olivier Gutknecht, Jacques Ferber
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

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ResourceBundle;
import java.util.logging.Level;

import javax.swing.Action;

import madkit.gui.SwingUtil;
import madkit.i18n.I18nUtilities;
import madkit.kernel.AgentLogger;
import madkit.kernel.MadkitClassLoader;

/**
 * Global actions that can be triggered from anywhere during execution.
 * 
 * @author Fabien Michel
 * @since MaDKit 5.0.1
 * @version 0.9
 * 
 */
public class GlobalAction {

	final static private ResourceBundle messages = I18nUtilities.getResourceBundle(GlobalAction.class.getSimpleName());
	
	/**
	 * The path to the jconsole executable if available
	 */
	public static String jconsolePath = MadkitClassLoader.findJavaExecutable("jconsole");

	/**
	 * An action that Launches the jconsole tool if it is available.
	 * jconsole is available on environment containing the oracle JDK.
	 */
	final public static Action JCONSOLE = new MDKAbstractAction(new ActionInfo("JCONSOLE", KeyEvent.VK_L, messages)) {
		private static final long	serialVersionUID	= 1L;
		@Override
		public void actionPerformed(ActionEvent e) {
			if(jconsolePath != null){
				final String pid = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
							try {
								Runtime.getRuntime().exec(jconsolePath+" "+pid.substring(0, pid.indexOf('@')));
							} catch (IOException e1) {
								e1.printStackTrace();
							}
			}
			else{
				System.err.println("jconsole unavailable");
			}
		}
	};
	
	/**
	 * An action that enable or disable the debugging mode. 
	 * When activated, all the active agent loggers set their level to {@link Level#ALL}.
	 * When disabled, all the loggers are set to {@link Level#INFO}.
	 */
	final public static Action DEBUG = new MDKAbstractAction(new ActionInfo("DEBUG", KeyEvent.VK_D, messages)) {
			/**
			 * 
			 */
			private static final long	serialVersionUID	= 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				if (Boolean.valueOf((boolean) getValue(Action.SELECTED_KEY))) {
					AgentLogger.setAllLogLevels(Level.ALL);
				}
				else{
					AgentLogger.setAllLogLevels(Level.INFO);
				}
			}
		};

		/**
		 * An action that create a log file for each agent having a non <code>null</code> logger.
		 */
		final public static Action LOG_FILES;
		
		static{
			final ActionInfo actionInfo = new ActionInfo("LOG_FILES", KeyEvent.VK_F, messages);
			actionInfo.setIcon("LOG_LEVEL");
			LOG_FILES = new MDKAbstractAction(actionInfo) {
				/**
				 * 
				 */
				private static final long	serialVersionUID	= 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					AgentLogger.createLogFiles();
				}
			};
		}
		
		/**
		 * An action that create a log file for each agent having a non <code>null</code> logger.
		 */
		final public static Action LAUNCH_MAIN;
		
		static{
			final String[] params = null; 
			final ActionInfo actionInfo = new ActionInfo("LAUNCH_MAIN", KeyEvent.VK_DOLLAR, messages);
			LAUNCH_MAIN = new MDKAbstractAction(actionInfo) {
				/**
				 * 
				 */
				private static final long	serialVersionUID	= 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						MadkitClassLoader.getLoader().loadClass(e.getActionCommand()).getDeclaredMethod("main", String[].class).invoke(null, (Object) params);
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
							| SecurityException | ClassNotFoundException e1) {
						e1.printStackTrace();
					}
				}
			};
			LAUNCH_MAIN.putValue(Action.SMALL_ICON, SwingUtil.MADKIT_LOGO_SMALL);
			LAUNCH_MAIN.putValue(Action.SMALL_ICON, SwingUtil.MADKIT_LOGO_SMALL);
		}

}
