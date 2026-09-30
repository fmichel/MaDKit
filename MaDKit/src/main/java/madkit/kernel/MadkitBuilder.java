/*******************************************************************************
 * MaDKit - Multi-agent systems Development Kit
 *******************************************************************************/
package madkit.kernel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

import madkit.simulation.SimuEnvironment;
import madkit.simulation.SimuModel;
import madkit.simulation.Viewer;

/**
 * Fluent, type-safe configuration for launching a {@link Madkit} instance.
 *
 * <pre>
 * Madkit.builder()
 *     .agentLogLevel(Level.FINE)
 *     .headless(true)
 *     .launchAgent(MyAgent.class)
 *     .build();
 * </pre>
 *
 * <p>The builder starts MaDKit when {@link #build()} is called, just like the
 * existing command-line constructor. The original {@code Madkit(String...)}
 * constructor remains available for CLI and properties-file launches.</p>
 */
public class MadkitBuilder {

	private Level agentLogLevel = Level.INFO;
	private Level kernelLogLevel = Level.OFF;
	private Level madkitLogLevel = Level.INFO;
	private boolean debug;
	private boolean desktop;
	private boolean headless;
	private boolean network;
	private boolean noLog;
	private boolean noRandomizedFields;
	private boolean createLogFiles;
	private boolean autoStart;
	private int seed = Integer.MIN_VALUE;
	private String logDirectory;
	private final List<String> agents = new ArrayList<>();
	private final List<String> viewers = new ArrayList<>();
	private String scheduler;
	private String environment;
	private String model;

	public MadkitBuilder agentLogLevel(Level level) {
		agentLogLevel = Objects.requireNonNull(level, "level");
		return this;
	}

	public MadkitBuilder kernelLogLevel(Level level) {
		kernelLogLevel = Objects.requireNonNull(level, "level");
		return this;
	}

	public MadkitBuilder madkitLogLevel(Level level) {
		madkitLogLevel = Objects.requireNonNull(level, "level");
		return this;
	}

	public MadkitBuilder debug(boolean enabled) {
		debug = enabled;
		return this;
	}

	public MadkitBuilder desktop(boolean enabled) {
		desktop = enabled;
		return this;
	}

	public MadkitBuilder headless(boolean enabled) {
		headless = enabled;
		return this;
	}

	public MadkitBuilder network(boolean enabled) {
		network = enabled;
		return this;
	}

	public MadkitBuilder noLog(boolean disabled) {
		noLog = disabled;
		return this;
	}

	public MadkitBuilder noRandomizedFields(boolean disabled) {
		noRandomizedFields = disabled;
		return this;
	}

	public MadkitBuilder createLogFiles(boolean enabled) {
		createLogFiles = enabled;
		return this;
	}

	public MadkitBuilder autoStart(boolean enabled) {
		autoStart = enabled;
		return this;
	}

	public MadkitBuilder seed(int value) {
		seed = value;
		return this;
	}

	public MadkitBuilder logDirectory(String directory) {
		logDirectory = directory;
		return this;
	}

	public MadkitBuilder launchAgent(String className) {
		agents.add(Objects.requireNonNull(className, "className"));
		return this;
	}

	public MadkitBuilder launchAgent(Class<? extends Agent> agentClass) {
		return launchAgent(Objects.requireNonNull(agentClass, "agentClass").getName());
	}

	public MadkitBuilder viewer(String className) {
		viewers.add(Objects.requireNonNull(className, "className"));
		return this;
	}

	public MadkitBuilder viewer(Class<? extends Viewer> viewerClass) {
		return viewer(Objects.requireNonNull(viewerClass, "viewerClass").getName());
	}

	public MadkitBuilder scheduler(Class<? extends Scheduler<?>> schedulerClass) {
		scheduler = Objects.requireNonNull(schedulerClass, "schedulerClass").getName();
		return this;
	}

	public MadkitBuilder environment(Class<? extends SimuEnvironment> environmentClass) {
		environment = Objects.requireNonNull(environmentClass, "environmentClass").getName();
		return this;
	}

	public MadkitBuilder model(Class<? extends SimuModel> modelClass) {
		model = Objects.requireNonNull(modelClass, "modelClass").getName();
		return this;
	}

	/**
	 * Creates and starts MaDKit with this configuration.
	 *
	 * @return the started MaDKit instance
	 */
	public Madkit build() {
		return new Madkit(this);
	}

	KernelConfig toKernelConfig() {
		KernelConfig config = new KernelConfig();
		config.setProperty(MDKCommandLine.AGENT_LOG_LEVEL, agentLogLevel);
		config.setProperty(MDKCommandLine.KERNEL_LOG_LEVEL, kernelLogLevel);
		config.setProperty(MDKCommandLine.MADKIT_LOG_LEVEL, madkitLogLevel);
		config.setProperty(MDKCommandLine.DEBUG, debug);
		config.setProperty(MDKCommandLine.DESKTOP, desktop);
		config.setProperty(MDKCommandLine.HEADLESS_MODE, headless);
		config.setProperty(MDKCommandLine.NETWORK, network);
		config.setProperty(MDKCommandLine.NO_LOG, noLog);
		config.setProperty(MDKCommandLine.NO_RANDOM, noRandomizedFields);
		config.setProperty(MDKCommandLine.CREATE_LOG_FILES, createLogFiles);
		config.setProperty(MDKCommandLine.START, autoStart);
		config.setProperty(MDKCommandLine.SEED, seed);
		config.setProperty(MDKCommandLine.AGENTS, new ArrayList<>(agents));
		config.setProperty(MDKCommandLine.VIEWERS, new ArrayList<>(viewers));
		setIfPresent(config, MDKCommandLine.LOG_DIRECTORY, logDirectory);
		setIfPresent(config, MDKCommandLine.SCHEDULER, scheduler);
		setIfPresent(config, MDKCommandLine.ENVIRONMENT, environment);
		setIfPresent(config, MDKCommandLine.MODEL, model);
		return config;
	}

	private static void setIfPresent(KernelConfig config, String key, String value) {
		if (value != null) {
			config.setProperty(key, value);
		}
	}
}