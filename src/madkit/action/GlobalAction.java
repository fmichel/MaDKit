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
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.ResourceBundle;
import java.util.logging.Level;

import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import madkit.gui.SwingUtil;
import madkit.i18n.ErrorMessages;
import madkit.i18n.I18nUtilities;
import madkit.i18n.Words;
import madkit.kernel.AgentLogger;
import madkit.kernel.Madkit;
import madkit.kernel.Madkit.Option;
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
	public final static String jconsolePath = MadkitClassLoader.findJavaExecutable("jconsole");

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
								new ProcessBuilder(jconsolePath,pid.substring(0, pid.indexOf('@'))).start();
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
				if(e == null){ // programmatically triggered
					putValue(Action.SELECTED_KEY, ! (boolean) getValue(Action.SELECTED_KEY));
				}
				if ((boolean) getValue(Action.SELECTED_KEY)) {
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
		 * An action that launch the main method of the class
		 * which name is obtained using {@link ActionEvent#getActionCommand()} 
		 * on the received event, i.e. the action command of the button.
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
		}
		
		/**
		 * Opens a dialog for selecting the jar file to add.
		 */
		final public static Action LOAD_JAR_FILE= new MDKAbstractAction(new ActionInfo("LOAD_JAR_FILE", KeyEvent.VK_J, messages)) {
			/**
			 * 
			 */
			private static final long	serialVersionUID	= 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
					JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
					chooser.setFileFilter(new FileNameExtensionFilter("Jar file", "jar"));
					int returnVal = chooser.showOpenDialog(null);
					if(returnVal == JFileChooser.APPROVE_OPTION) {
						try {
							MadkitClassLoader.loadUrl(chooser.getSelectedFile().toURI().toURL());
						} catch (MalformedURLException e1) {
							e1.printStackTrace();
						}
					}
			}
		};
		
		/**
		 * Load the jar files which are in the "demos" directory if there is one in the working directory
		 */
		final public static Action LOAD_LOCAL_DEMOS= new MDKAbstractAction(new ActionInfo("LOAD_LOCAL_DEMOS", KeyEvent.VK_S, messages)) {
			/**
			 * 
			 */
			private static final long	serialVersionUID	= 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				File f = new File("demos");
				if (f.exists() && f.isDirectory()) {
					MadkitClassLoader.loadJarsFromDirectory(f.getAbsolutePath());
				} else {
					JOptionPane.showMessageDialog(null, f.getAbsolutePath()+" "+ Words.DIRECTORY +" "+ ErrorMessages.CANT_FIND , getValue(Action.NAME).toString(), JOptionPane.WARNING_MESSAGE);
			}
			}
		};

		
		/**
		 * An action that launches a new MaDKit instance using the configuration file
		 * which name is obtained using {@link ActionEvent#getActionCommand()} 
		 * on the received event, i.e. the action command of the button.
		 */
		public static final Action LAUNCH_MDK_CONFIG;
		
		static{
			final ActionInfo actionInfo = new ActionInfo("LAUNCH_MDK_CONFIG", KeyEvent.VK_DOLLAR, messages);
			LAUNCH_MDK_CONFIG = new MDKAbstractAction(actionInfo) {
				/**
				 * 
				 */
				private static final long	serialVersionUID	= 1L;

				@SuppressWarnings("unused")
				@Override
				public void actionPerformed(ActionEvent e) {
						new Madkit(Option.configFile.toString(),
								e.getActionCommand()
//								,LevelOption.madkitLogLevel.toString(),"ALL"
								);
				}
			};
			LAUNCH_MAIN.putValue(Action.SMALL_ICON, SwingUtil.MADKIT_LOGO_SMALL);
		}

}
