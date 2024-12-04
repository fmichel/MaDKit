
package madkit.kernel;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
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
 * The `Madkit` class is the main entry point for the MaDKit platform. It
 * initializes the configuration, logging, and starts the kernel agent.
 * <p>
 * This class uses the PicoCLI library for command-line parsing.
 * </p>
 * 
 * @version 6.0
 * @since MaDKit 1.0
 */
@Command(name = "Madkit", mixinStandardHelpOptions = true, version = "6.0", description = "Lightweight OCMAS platform: Multi-Agent Systems as artificial organizations")
public class Madkit {

	static final Logger MDK_ROOT_LOGGER = Logger.getLogger("[MADKIT] ");

	static {
		final ConsoleHandler ch = new ConsoleHandler();
		ch.setFormatter(AgentLogger.AGENT_FORMATTER);
		ch.setLevel(Level.ALL);
		MDK_ROOT_LOGGER.addHandler(ch);
		MDK_ROOT_LOGGER.setLevel(Level.OFF);
		MDK_ROOT_LOGGER.setUseParentHandlers(false);
	}

	static final String LAUNCHER = "launcherClass";
	static final String LAUNCHER_CLASS = "lc";

	public static String BUILD_ID;
	public static String WEB;
	public static String VERSION = "INIT";

	static String oneFileLauncher;
	static String[] oneFileLauncherArgs;

	@Mixin
	private MDKCommandLine mdkOptions = new MDKCommandLine();

	private KernelConfig config;

	private Logger mdkLogger;

	private final String commandLine = "CMD_LINE";

	final String[] startingArgs;

	final Class<? extends Madkit> launcherClass;

	private KernelAgent kernelAgent;

	/**
	 * Constructs a `Madkit` instance with the specified command-line arguments.
	 *
	 * @param args the command-line arguments
	 */
	public Madkit(String... args) {
		launcherClass = getClass();
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
		mdkLogger.setParent(MDK_ROOT_LOGGER);
		mdkLogger.setLevel(mdkOptions.madkitLogLevel);
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
		config.addProperty(commandLine, args);
		VERSION = config.getString("madkit.version");
		BUILD_ID = config.getString("build.id");
		WEB = config.getString("madkit.web");
	}

	/**
	 * Parses the command-line arguments.
	 *
	 * @param args the command-line arguments
	 * @return <code>true</code> if the arguments were parsed successfully,
	 *         <code>false</code> otherwise
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
				cmd.printVersionHelp(System.out); // NOSONAR
			} else {
				return true;
			}
		} catch (ParameterException e) {
			e.printStackTrace();
			// NO-SONAR
			e.getCommandLine().usage(System.err); // NOSONAR
			// throw e;
		}
		return false;
	}

	/**
	 * The main method for the MaDKit platform.
	 *
	 * @param args the command-line arguments
	 */
	public static void main(String[] args) {
		if (args != null)
			new Madkit(args);
		else
			new Madkit(new String[0]);
		// try {
		// List<Class<? extends Agent>> subclasses =
		// ClassScanner.findSubclasses(Agent.class);
		// for (Class<? extends Agent> subclass : subclasses) {
		// System.out.println(subclass.getName());
		// }
		// } catch (IOException | ClassNotFoundException e) {
		// e.printStackTrace();
		// }

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

	/**
	 * Prints the welcome string for the MaDKit platform.
	 */
	private void printWelcomeString() {
		if (mdkLogger.getLevel() != Level.OFF) {
			// NO-SONAR
			System.out.println("\n\t---------------------------------------" + "\n\t                MaDKit"
					+ "\n\t             version: " + VERSION + "\n\t       MaDKit Team (c) 1997-"
					+ Calendar.getInstance().get(Calendar.YEAR) + "\n\t---------------------------------------\n");
		}
	}

	/**
	 * Returns the class of the one-file launcher.
	 *
	 * @return the class of the one-file launcher, or the launcher class if not set
	 */
	Class<?> getOneFileLauncherClass() {
		if (oneFileLauncher != null)
			try {
				return Class.forName(oneFileLauncher);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		return launcherClass;
	}

	/**
	 * Returns the arguments for the launcher.
	 *
	 * @return the arguments for the launcher
	 */
	String[] getLauncherArgs() {
		if (oneFileLauncherArgs != null)
			return oneFileLauncherArgs;
		String[] strings = config.get(String[].class, "CMD_LINE");
		return strings;
	}

	/**
	 * Returns the kernel configuration.
	 *
	 * @return the kernel configuration
	 */
	public KernelConfig getConfig() {
		return config;
	}

}
