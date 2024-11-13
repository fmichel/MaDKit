/*******************************************************************************
 * Copyright (c) 1997, 2021, MaDKit Team
 *
 * This software is a computer program whose purpose is to
 * provide a lightweight Java API for developing and simulating 
 * Multi-Agent Systems (MAS) using an organizational perspective
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
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
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 *******************************************************************************/
package madkit.kernel;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.configuration2.ConfigurationConverter;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;

import madkit.reflection.ClassScanner;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.ParseResult;
import picocli.CommandLine.PropertiesDefaultProvider;

/**
 * @author Fabien Michel
 * 
 * @version 6.0
 */
@Command(name = "Madkit", mixinStandardHelpOptions = true, version = "6.0", description = "Lightweight OCMAS platform: Multi-Agent Systems as artificial organizations")
public class Madkit {

	static final Logger MDK_ROOT_LOGGER = Logger.getLogger("[MADKIT] ");

	static final String LAUNCHER = "launcherClass";

	static final String LAUNCHER_CLASS = "lc";

	public static String BUILD_ID;
	public static String WEB;
	public static String VERSION = "INIT";

	static String oneFileLauncher;
	static String[] oneFileLauncherArgs;

	static {
		final ConsoleHandler ch = new ConsoleHandler();
		ch.setFormatter(AgentLogger.AGENT_FORMATTER);
		ch.setLevel(Level.ALL);
		MDK_ROOT_LOGGER.addHandler(ch);
		MDK_ROOT_LOGGER.setLevel(Level.OFF);
		MDK_ROOT_LOGGER.setUseParentHandlers(false);
	}

	@Mixin
	private MDKCommandLine mdkOptions = new MDKCommandLine();

	private KernelConfig config;

	private Logger mdkLogger;

	private final String commandLine = "CMD_LINE";

	final String[] startingArgs;

	Class<? extends Madkit> launcherClass;

	private KernelAgent kernelAgent;

	public Madkit(String... args) {
		startingArgs = args;
		initConfiguration(args);
		if (parseCommanLine(args)) {
			initLogging();
			mdkLogger.finest(() -> "args: " + Arrays.deepToString(args));
			addMixinToKernelConfig();
			mdkLogger.finer(() -> getConfig().toString());
			start();
		}
	}

	private void addMixinToKernelConfig() {
		Class<?> c = getClass();
		do {
			for (Field f : c.getDeclaredFields()) {
				if (f.getAnnotation(Mixin.class) != null) {
					try {
						// NO-SONAR
						f.setAccessible(true);
						config.addPropertiesFromFields(f.get(this));
					} catch (IllegalArgumentException | IllegalAccessException e) {
						e.printStackTrace();
					}
				}
			}
			c = c.getSuperclass();
		} while (c != Object.class);
	}
	
	public void launchAgent(Agent a) {
		kernelAgent.launchAgent(a);
	}

	/**
	 * 
	 */
	private void initLogging() {
		mdkLogger = Logger.getLogger("[MDK] ");
		mdkLogger.setParent(MDK_ROOT_LOGGER);
		mdkLogger.setLevel(mdkOptions.madkitLogLevel);
	}

	/**
	 * @param args
	 * @throws ConfigurationException
	 */
	private void initConfiguration(String[] args) {
		Parameters params = new Parameters();
		FileBasedConfigurationBuilder<KernelConfig> builder = new FileBasedConfigurationBuilder<>(
				KernelConfig.class)
						.configure(params.properties().setURL(Madkit.class.getResource("madkit.properties")));
		try {
			config = builder.getConfiguration();
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
		config.addProperty(commandLine, args);
		launcherClass = getClass();
		VERSION = config.getString("madkit.version");
		BUILD_ID = config.getString("build.id");
		WEB = config.getString("madkit.web");
	}

	/**
	 * @param args
	 * @return <code>true</code> if everything is OK
	 */
	private boolean parseCommanLine(String[] args) {
		CommandLine cmd = new CommandLine(this);
		cmd.registerConverter(Level.class, Level::parse);
		cmd.setDefaultValueProvider(new PropertiesDefaultProvider(ConfigurationConverter.getProperties(config)));
		try {
			ParseResult result = cmd.parseArgs(args);
			if (result.isUsageHelpRequested()) {
				// NO-SONAR
				cmd.usage(System.out);
			} else if (result.isVersionHelpRequested()) {
				// NO-SONAR
				cmd.printVersionHelp(System.out);
			} else {
				return true;
			}
		} catch (ParameterException e) {
//			e.printStackTrace();
			// NO-SONAR
			e.getCommandLine().usage(System.err);
//			throw e;
		}
		return false;
	}

	public static void main(String[] args) {
		if(args != null)
			new Madkit(args);
		else
			new Madkit(new String[0]);
      try {
         List<Class<? extends Agent>> subclasses = ClassScanner.findSubclasses(Agent.class);
         for (Class<? extends Agent> subclass : subclasses) {
             System.out.println(subclass.getName());
         }
     } catch (IOException | ClassNotFoundException e) {
         e.printStackTrace();
     }

	}

	private void start() {
		printWelcomeString();
		kernelAgent = new KernelAgent(this);
		kernelAgent.launchAgent(kernelAgent, Integer.MAX_VALUE);
		mdkLogger.fine("** Kernel launched **");
	}

	private void printWelcomeString() {
		if (mdkLogger.getLevel() != Level.OFF) {
			// NO-SONAR
			System.out.println("\n\t---------------------------------------" + "\n\t                MaDKit"
					+ "\n\t             version: " + VERSION + "\n\t       MaDKit Team (c) 1997-"
					+ Calendar.getInstance().get(Calendar.YEAR) + "\n\t---------------------------------------\n");
		}
	}

	Class<?> getLauncherClass() {
		if (oneFileLauncher != null)
			try {
				return Class.forName(oneFileLauncher);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		return launcherClass;
	}

	String[] getLauncherArgs() {
		if (oneFileLauncherArgs != null)
			return oneFileLauncherArgs;
		String[] strings = config.get(String[].class, "CMD_LINE");
		return strings;
	}

	/**
	 * @return
	 */
	public KernelConfig getConfig() {
		return config;
	}

//	public void launchAgent(Agent a, int timeout) {
//		kernelAgent.receiveMessage(new KernelMessage(KernelAction.LAUNCH_AGENT, a, timeout));
//	}

}

//class MadkitLogLevels {
//	@Option(names = "--agentLogLevel", defaultValue = "INFO", description = "agents initial log level (default: ${DEFAULT-VALUE})")
//	Level agentDefaultLogLevel;// = Level.INFO;
//
//	@Option(names = "--kernelLogLevel", defaultValue = "OFF", description = "Kernel log level (default: ${DEFAULT-VALUE})")
//	Level kernelLogLevel;
//
//	@Option(names = "--madkitLogLevel", defaultValue = "FINE", description = "MaDKit log level (default: ${DEFAULT-VALUE})")
//	Level madkitLogLevel;
//
//	@Override
//	public String toString() {
//		return "MadkitLogLevels : [agentDefaultLogLevel=" + agentDefaultLogLevel + ", kernelLogLevel=" + kernelLogLevel
//				+ ", madkitLogLevel=" + madkitLogLevel + "]";
//	}
//
////	@Override
////	public String toString() {
////		return "[madkitLogLevel -> "+madkitLogLevel+"[agentDefaultLogLevel -> "+agentDefaultLogLevel+", kernelLogLevel -> "+kernelLogLevel+", ";
////	}
//
//}
//
//class MadkitOptions {
//	@Option(names = { "-d", "--debug" }, description = "activate the debug mode")
//	boolean debug;
//
//	@Option(names = { "--desktop" }, negatable = true, description = "activate the desktop mode")
//	boolean desktop;
//
//	@Option(names = { "-la",
//			"--launchAgents" }, arity = "1..3", fallbackValue = "madkit.kernel.Madkit", description = "launch agents on startup")
//	List<String> agentsToBeLaunched;
//}