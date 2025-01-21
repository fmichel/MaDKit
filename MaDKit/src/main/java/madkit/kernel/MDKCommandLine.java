package madkit.kernel;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import picocli.CommandLine.Option;

//TODO make this public
/**
 * @author Fabien Michel
 *
 */
class MDKCommandLine {

	private static final String SWITCH = "--";

	public static final String AGENT_LOG_LEVEL = "agentLogLevel";
	public static final String CREATE_LOG_FILES = "createLogFiles";
	public static final String HEADLESS = "headless";
	public static final String NO_RANDOM = "noRandomizedFields";

	//////////////////// OPTIONS

	@Option(names = { "-d", "--debug" }, description = "activate the debug mode")
	boolean debug;

	@Option(names = { "--desktop" }, negatable = true, description = "activate the desktop mode")
	boolean desktop;

	/**
	 * No log {@link #getLogger()} is not used, there is no memory footprint at all,
	 * which could be crucial when working with thousands of abstract agents in
	 * simulation mode.
	 *
	 */
	@Option(names = { "--noLog" }, description = "inhibit logging for optimizing simulations")
	boolean noLog;

	@Option(names = SWITCH + NO_RANDOM, description = "inhibit the randomization of annotated fields")
	boolean noRandomizedFields = false;

	@Option(names = { "-la",
			"--agents" }, arity = "1", fallbackValue = "madkit.kernel.Madkit", description = "launch agents on startup")
	List<String> agents = Collections.emptyList();

	@Option(names = SWITCH
			+ AGENT_LOG_LEVEL, defaultValue = "INFO", description = "agents initial log level (default: ${DEFAULT-VALUE})")
	Level agentLogLevel;

	@Option(names = "--kernelLogLevel", defaultValue = "OFF", description = "Kernel log level (default: ${DEFAULT-VALUE})")
	Level kernelLogLevel;

	@Option(names = "--madkitLogLevel", defaultValue = "INFO", description = "MaDKit log level (default: ${DEFAULT-VALUE})")
	Level madkitLogLevel;

	@Option(names = SWITCH + CREATE_LOG_FILES, description = "creates log files for each agent")
	boolean createLogFiles;

	@Option(names = "--logDirectory", description = "specifies the directory wherein log files are cretaed")
	private String logDirectory;

	@Option(names = "--scheduler",
			// fallbackValue = "madkit.kernel.Scheduler",
			description = "specifies the class that should be used as Scheduler")
	private String scheduler;

	@Option(names = { "-v", "--viewers" }, arity = "1", description = "specifies the viewer classes in simulation mode")
	List<String> viewers = Collections.emptyList();

	@Option(names = "--environment", description = "specifies the class that should be used as SimuEnvironment")
	private String environment;

	@Option(names = "--model", description = "specifies the class that should be used as Model")
	private String model;

	@Option(names = SWITCH + HEADLESS, defaultValue = "false", description = "inhibit UI")
	private boolean headless;

	@Option(names = "--start", description = "Automatically start the simulation")
	boolean start = false;

	@Option(names = "-D", fallbackValue = "") // allow -Dkey
	void setProperty(Map<String, String> props) {
		props.forEach(System::setProperty);
	}

	void feedConfiguration(KernelConfig config) {
		Arrays.stream(getClass().getDeclaredFields()).forEach(f -> {
			try {
				config.setProperty(f.getName(), f.get(this));
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		});
	}

}
