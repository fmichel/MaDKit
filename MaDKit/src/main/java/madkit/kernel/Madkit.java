/*******************************************************************************
 * MaDKit - Multi-agent systems Development Kit 
 * 
 * Copyright (c) 1998-2025 Fabien Michel, Olivier Gutknecht, Jacques Ferber...
 * 
 * This software is a computer program whose purpose is to
 * provide a lightweight Java API for developing and simulating 
 * Multi-Agent Systems (MAS) using an organizational perspective.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.You can use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty and the software's author, the holder of the
 * economic rights, and the successive licensors have only limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading, using, modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean that it is complicated to manipulate, and that also
 * therefore means that it is reserved for developers and experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and, more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 *******************************************************************************/

package madkit.kernel;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Arrays;
import java.util.Calendar;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.configuration2.ConfigurationConverter;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.ParseResult;
import picocli.CommandLine.PropertiesDefaultProvider;

/**
 * The `Madkit` class is the main entry point for the MaDKit platform. It initializes the
 * configuration, logging, and starts the kernel agent.
 * <p>
 * This class uses the PicoCLI library for command-line parsing.
 * </p>
 * 
 * @version 6.0
 * @since MaDKit 1.0
 */
@Command(name = "MaDKit", mixinStandardHelpOptions = true, description = "Lightweight OCMAS platform: Multi-Agent Systems as artificial organizations")
public class Madkit {

	/** The Constant MDK_LOGGER. */
	static final Logger MDK_LOGGER = Logger.getLogger("[MADKIT] ");

	static {
		final ConsoleHandler ch = new ConsoleHandler();
		ch.setFormatter(AgentLogger.AGENT_FORMATTER);
		ch.setLevel(Level.ALL);
		MDK_LOGGER.addHandler(ch);
		MDK_LOGGER.setLevel(Level.SEVERE);
		MDK_LOGGER.setUseParentHandlers(false);
	}

	static final String LAUNCHER = "launcherClass";
	static final String LAUNCHER_CLASS = "lc";

	/** The web. */
	static String WEB;
	/**
	 * The version of the MaDKit platform.
	 */
	static String version = "INIT";

	static String oneFileLauncher;
	static String[] oneFileLauncherArgs;

	@Mixin
	private MDKCommandLine mdkOptions = new MDKCommandLine();

	private KernelConfig config;

	private Logger mdkLogger;

	private static final String CMD_LINE = "CMD_LINE";

	final String[] startingArgs;

	final Class<? extends Madkit> launcherClass;

	private KernelAgent kernelAgent;

	/**
	 * Constructs a `Madkit` instance with the specified command-line arguments.
	 *
	 * @param args the command-line arguments
	 * 
	 */
	public Madkit(String... args) {
		launcherClass = getClass();
		startingArgs = args;
		initConfiguration(args);
		if (parseCommanLine(args)) {
			initLogging();
			mdkLogger.finest(() -> MadkitClassLoader.getLoader().toString());
			mdkLogger.finer(() -> "args: " + Arrays.deepToString(args));
			addMixinToKernelConfig();
			mdkLogger.finest(() -> getConfig().toString());
			start();
		}
	}

	void startNewSession() {
		MDK_LOGGER.config(() -> "Starting new MaDKit session with " + Arrays.deepToString(startingArgs));
		try {
			Thread.ofVirtual().start(() -> {
				Method c = getLauncherClassMainMethod();
				try {
					c.invoke(null, (Object) startingArgs);
				} catch (IllegalAccessException | InvocationTargetException e) {
					MDK_LOGGER.severe(() -> "Cannot restart MaDKit: " + e);
				}
			}).join();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	/**
	 * Initializes the configuration for the MaDKit platform.
	 *
	 * @param args the command-line arguments
	 */
	private void initConfiguration(String[] args) {
		Parameters params = new Parameters();
		FileBasedConfigurationBuilder<KernelConfig> builder = new FileBasedConfigurationBuilder<>(KernelConfig.class)
				.configure(params.properties().setURL(Madkit.class.getResource("madkit.properties")));
		try {
			config = builder.getConfiguration();
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
		config.addProperty(CMD_LINE, args);
		version = getVersionUsingJarFIleName("LOCAL-BUILD");
		WEB = config.getString("madkit.web");
	}

	/**
	 * Parses the command-line arguments.
	 *
	 * @param args the command-line arguments
	 * @return <code>true</code> if the arguments were parsed successfully, <code>false</code>
	 *         otherwise
	 */
	private boolean parseCommanLine(String[] args) {
		CommandLine cmd = new CommandLine(this);
		cmd.registerConverter(Level.class, Level::parse);
		cmd.setDefaultValueProvider(new PropertiesDefaultProvider(ConfigurationConverter.getProperties(config)));
		try {
			ParseResult result = cmd.parseArgs(args);
			if (result.isUsageHelpRequested()) {
				// NO-SONAR
				cmd.usage(System.out);// NOSONAR
			} else if (result.isVersionHelpRequested()) {
				cmd.printVersionHelp(System.out); // NOSONAR
			} else {
				return true;
			}
		} catch (ParameterException e) {
			e.printStackTrace();
			e.getCommandLine().usage(System.err); // NOSONAR
		}
		return false;
	}

	/**
	 * Adds mixin properties to the kernel configuration.
	 */
	private void addMixinToKernelConfig() {
		Class<?> c = getClass();
		do {
			for (Field f : c.getDeclaredFields()) {
				if (f.getAnnotation(Mixin.class) != null) {
					try {
						f.setAccessible(true); // NOSONAR
						config.addPropertiesFromFields(f.get(this));
					} catch (IllegalArgumentException | IllegalAccessException e) {
						e.printStackTrace();
					}
				}
			}
			c = c.getSuperclass();
		} while (c != Object.class);
	}

	/**
	 * Launches the specified agent.
	 *
	 * @param a the agent to launch
	 */
	public void launchAgent(Agent a) {
		kernelAgent.launchAgent(a);
	}

	/**
	 * Returns the main method of the launcher class.
	 *
	 * @return the main method of the launcher class, or null if not found
	 */
	Method getLauncherClassMainMethod() {
		try {
			return launcherClass.getDeclaredMethod("main", String[].class);
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Initializes logging for the MaDKit platform.
	 */
	private void initLogging() {
		mdkLogger = Logger.getLogger("[MDK] ");
		mdkLogger.setParent(MDK_LOGGER);
		mdkLogger.setLevel(mdkOptions.madkitLogLevel);
	}

	/**
	 * Starts the MaDKit platform.
	 */
	private void start() {
		printWelcomeString();
		kernelAgent = new KernelAgent(this);
		kernelAgent.launchAgent(kernelAgent, Integer.MAX_VALUE);
		mdkLogger.fine("** Kernel launched **");
	}

	private void printWelcomeString() {
		if (mdkLogger.getLevel() != Level.OFF) {
			String welcome = "---------------------------------------" + "\n                MaDKit"
					+ "\n             version: " + version + "\n       MaDKit Team (c) 1997-"
					+ Calendar.getInstance().get(Calendar.YEAR) + "\n---------------------------------------\n";
			System.out.println(welcome.indent(8));// NOSONAR
		}
	}

	Class<?> getOneFileLauncherClass() {
		if (oneFileLauncher != null) {
			try {
				return Class.forName(oneFileLauncher);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		return launcherClass;
	}

	/**
	 * Returns the arguments for the launcher.
	 *
	 * @return the arguments for the launcher
	 */
	String[] getLauncherArgs() {
		if (oneFileLauncherArgs != null) {
			return oneFileLauncherArgs;
		}
		return config.get(String[].class, CMD_LINE);
	}

	/**
	 * Returns the kernel configuration.
	 *
	 * @return the kernel configuration
	 */
	public KernelConfig getConfig() {
		return config;
	}

	/**
	 * The main method for the MaDKit platform.
	 *
	 * @param args the command-line arguments
	 */
	public static void main(String[] args) {
		if (args != null) {
			new Madkit(args);
		} else {
			new Madkit(new String[0]);
		}
	}

	/**
	 * Returns the version of the MaDKit platform using the JAR file name.
	 * 
	 * @param defaultValue the default value to return if the property is not found
	 * @return the value of the property if found, otherwise the default value
	 */
	private static String getVersionUsingJarFIleName(String defaultValue) {
		String classFile = Madkit.class.getName().replace('.', '/') + ".class";
		URL classUrl = Madkit.class.getClassLoader().getResource(classFile);
		if (classUrl != null) {
			String url = classUrl.toString();
			if (url.startsWith("jar:")) {
				String jarPath = url.substring(4, url.indexOf("!"));
				return jarPath.substring(jarPath.lastIndexOf("madkit-") + 7, jarPath.lastIndexOf('.'));
			}
		}
		return defaultValue;
	}
}
