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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import picocli.CommandLine.Option;

//TODO make this public
/**
 *
 *
 */
class MDKCommandLine {

	private static final String SWITCH = "--";

	public static final String AGENT_LOG_LEVEL = "agentLogLevel";
	public static final String CREATE_LOG_FILES = "createLogFiles";
	public static final String HEADLESS_MODE = "headless";
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

	@Option(names = SWITCH + HEADLESS_MODE, defaultValue = "false", description = "inhibit UI")
	private boolean headless;

	@Option(names = "--start", description = "Automatically start the simulation")
	boolean start = false;

	@Option(names = "--seed", description = "Provides the seed to be used by the kernel or a SimuLauncher. If not set, the kernel will used a random one and SimuLauncher agents 0 as value.")
	int seed = Integer.MIN_VALUE;

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
