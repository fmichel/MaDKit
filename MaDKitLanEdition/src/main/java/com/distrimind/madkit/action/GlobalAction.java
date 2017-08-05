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

package com.distrimind.madkit.action;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ResourceBundle;
import java.util.logging.Level;

import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.distrimind.madkit.gui.SwingUtil;
import com.distrimind.madkit.i18n.ErrorMessages;
import com.distrimind.madkit.i18n.I18nUtilities;
import com.distrimind.madkit.i18n.Words;
import com.distrimind.madkit.kernel.AgentLogger;
import com.distrimind.madkit.kernel.Madkit;
import com.distrimind.madkit.kernel.MadkitClassLoader;

/**
 * Global actions that can be triggered from anywhere during execution.
 * 
 * @author Fabien Michel
 * @author Jason Mahdjoub
 * @since MaDKit 5.0.1
 * @version 0.92
 * 
 */
public class GlobalAction {

	final static private ResourceBundle messages = I18nUtilities.getResourceBundle(GlobalAction.class.getSimpleName());

	/**
	 * An action that Launches the jconsole tool if it is available. It is set to
	 * <code>null</code> if jconsole is unavailable. jconsole is available on
	 * environments containing the oracle JDK.
	 */
	final public static Action JCONSOLE;

	static {
		final String jconsolePath = MadkitClassLoader.findJavaExecutable("jconsole");
		if (jconsolePath == null) {
			JCONSOLE = null;
		} else {
			JCONSOLE = new MDKAbstractAction(new ActionInfo("JCONSOLE", KeyEvent.VK_L, messages)) {

				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					final String pid = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
					try {
						new ProcessBuilder(jconsolePath, pid.substring(0, pid.indexOf('@'))).start();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			};
		}
	}

	/**
	 * An action that enable or disable the debugging mode. When activated, all the
	 * active agent loggers set their level to {@link Level#ALL}. When disabled, all
	 * the loggers are set to {@link Level#INFO}.
	 */
	final public static Action DEBUG = new MDKAbstractAction(new ActionInfo("DEBUG", KeyEvent.VK_D, messages)) {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e == null) { // programmatically triggered
				putValue(Action.SELECTED_KEY, new Boolean(!((Boolean) getValue(Action.SELECTED_KEY)).booleanValue()));
			}
			if (((Boolean) getValue(Action.SELECTED_KEY)).booleanValue()) {
				AgentLogger.setAllLogLevels(Level.ALL);
			} else {
				AgentLogger.setAllLogLevels(Level.INFO);
			}
		}
	};

	/**
	 * An action that create a log file for each agent having a non
	 * <code>null</code> logger.
	 */
	final public static Action LOG_FILES;

	static {
		final ActionInfo actionInfo = new ActionInfo("LOG_FILES", KeyEvent.VK_F, messages);
		actionInfo.setIcon("LOG_LEVEL");
		LOG_FILES = new MDKAbstractAction(actionInfo) {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				AgentLogger.createLogFiles();
			}
		};
	}

	/**
	 * An action that launch the main method of the class which name is obtained
	 * using {@link ActionEvent#getActionCommand()} on the received event, i.e. the
	 * action command of the button.
	 */
	final public static Action LAUNCH_MAIN;

	static {
		final String[] params = null;
		final ActionInfo actionInfo = new ActionInfo("LAUNCH_MAIN", KeyEvent.VK_DOLLAR, messages);
		LAUNCH_MAIN = new MDKAbstractAction(actionInfo) {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					MadkitClassLoader.getLoader().loadClass(e.getActionCommand())
							.getDeclaredMethod("main", String[].class).invoke(null, (Object) params);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
						| NoSuchMethodException | SecurityException | ClassNotFoundException e1) {
					e1.printStackTrace();
				}
			}
		};
		LAUNCH_MAIN.putValue(Action.SMALL_ICON, SwingUtil.MADKIT_LOGO_SMALL);
	}

	/**
	 * Opens a dialog for selecting the jar file to add.
	 */
	final public static Action LOAD_JAR_FILE = new MDKAbstractAction(
			new ActionInfo("LOAD_JAR_FILE", KeyEvent.VK_J, messages)) {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
			chooser.setFileFilter(new FileNameExtensionFilter("Jar file", "jar"));
			int returnVal = chooser.showOpenDialog(null);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				try {
					MadkitClassLoader.loadUrl(chooser.getSelectedFile().toURI().toURL());
				} catch (MalformedURLException e1) {
					e1.printStackTrace();
				}
			}
		}
	};

	/**
	 * Load the jar files which are in the "demos" directory if there is one in the
	 * working directory
	 */
	final public static Action LOAD_LOCAL_DEMOS = new MDKAbstractAction(
			new ActionInfo("LOAD_LOCAL_DEMOS", KeyEvent.VK_S, messages)) {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			File f = new File("demos");
			if (!(f.exists() && f.isDirectory())) {
				for (URL url : MadkitClassLoader.getLoader().getURLs()) {
					System.err.println(url);
					if (url.getFile().endsWith("madkit-" + Madkit.VERSION + ".jar")) {
						try {
							f = new File(Paths.get(url.toURI()).getParent().toString(), "demos");
							break;
						} catch (URISyntaxException ex) {
							ex.printStackTrace();
						}
					}
				}
			}
			if (f.exists() && f.isDirectory()) {
				if (!MadkitClassLoader.loadJarsFromDirectory(f.getAbsolutePath())) {
					JOptionPane.showMessageDialog(null, f.getAbsolutePath() + " : no new resources found",
							getValue(Action.NAME).toString(), JOptionPane.WARNING_MESSAGE);
				}
			} else {
				JOptionPane.showMessageDialog(null,
						f.getAbsolutePath() + " " + Words.DIRECTORY + " " + ErrorMessages.CANT_FIND,
						getValue(Action.NAME).toString(), JOptionPane.WARNING_MESSAGE);
			}
		}
	};

	/**
	 * An action that launches a new MaDKit instance using the configuration file
	 * which name is obtained using {@link ActionEvent#getActionCommand()} on the
	 * received event, i.e. the action command of the button.
	 */
	public static final Action LAUNCH_MDK_CONFIG;

	static {
		final ActionInfo actionInfo = new ActionInfo("LAUNCH_MDK_CONFIG", KeyEvent.VK_DOLLAR, messages);
		LAUNCH_MDK_CONFIG = new MDKAbstractAction(actionInfo) {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				new Madkit("configFiles", e.getActionCommand()
				// ,LevelOption.madkitLogLevel.toString(),"ALL"
				);
			}
		};
		LAUNCH_MAIN.putValue(Action.SMALL_ICON, SwingUtil.MADKIT_LOGO_SMALL);
	}

}
