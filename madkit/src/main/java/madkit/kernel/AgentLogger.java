package madkit.kernel;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.function.Supplier;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import madkit.logging.AgentFormatter;
import picocli.CommandLine.Option;

/**
 * This class defines a logger specialized for MaDKit agents.
 * 
 * @author Fabien Michel
 * @version 6
 * @since MaDKit 5.0.0.5
 */
public class AgentLogger extends Logger {

	/**
	 * Defines the No log logger for simulation purpose.
	 */
	static final AgentLogger NO_LOG_LOGGER = new AgentLogger() {
		@Override
		final public boolean isLoggable(Level level) {
			return false;
		}

		@Override
		public void log(Level level, String msg) {
			return;
		}

		@Override
		public void log(Level level, Supplier<String> msgSupplier) {
			return;
		}
	};

	/**
	 * Defines the default formatter as : [agent's name] LOG_LEVEL : message
	 */
	public static final Formatter AGENT_FORMATTER = new AgentFormatter();

	/**
	 * Defines the default file formatter as : LOG_LEVEL : message
	 */
	public static final Formatter AGENT_FILE_FORMATTER = new AgentFormatter() {
		@Override
		protected StringBuilder getHeader(final LogRecord record) {
			return new StringBuilder();
		}
	};

	/**
	 * Defines the default file formatter as : LOG_LEVEL : message
	 */
	static public final Level TALK = Level.parse("1100");

	AgentLogger(final Agent a) {
		super("[" + a.getName() + "] ", null);
		setParent(Madkit.MDK_ROOT_LOGGER);
		if (a.kernel != null && a.kernel != KernelAgent.deadKernel) {
			setLevel(a.getKernelConfig().getLevel("agentLogLevel"));
			// setLevel(a.getKernelConfiguration().getLevel(logDirectory));
			// if (createLogFiles) {
			// createLogFile();
			// }
		} else {
			setLevel(Level.INFO);
		}
	}

	AgentLogger(final KernelAgent a) {
		super("[" + a.getName() + "] ", null);
		setParent(Madkit.MDK_ROOT_LOGGER);
	}

	AgentLogger() {
		super(null, null);
	}

	@Override
	public String toString() {
		return getName()+' '+getLevel();
	}

//    /**
//     * Prevents this logger to change its level when {@link #setAllLoggersAtLevelAll()}
//     * or {@link #setAllLogLevels(Level)} are used.
//     */
//    public void doNotReactToDebugMode() {
//	debugModeBlackList.add(this);
//    }
//
//    /**
//     * Tells if CGR warnings (Community, Group, Role) are enabled.
//     * 
//     * @see #enableCGRWarnings()
//     * @return <code>true</code> if CGR warnings are enabled for this logger
//     */
//    public boolean isCGRWarningsOn() {
//	return agentCGRWarningsOnAction != null && (boolean) agentCGRWarningsOnAction.getValue(Action.SELECTED_KEY);
//    }
//
//    /**
//     * Enables the logging of {@link Level#WARNING} messages related with failed queries over the artificial
//     * society. For instance, if an agent tries to get agent addresses using
//     * {@link Agent#getAgentsWithRole(String, String, String)} over a CGR location which does not exist then there
//     * will be a warning about that. Since such results could be obtained by agents on purpose, this method provides a
//     * convenient way of enabling these kind of traces as will.
//     * 
//     */
//    public void enableCGRWarnings() {
//	getEnableCGRWarningsAction().putValue(Action.SELECTED_KEY, true);
//    }
//
//    /**
//     * Disables the logging of {@link Level#WARNING} messages related with failed queries over the artificial
//     * society.
//     * @see #enableCGRWarnings()
//     */
//    public void disableCGRWarnings() {
//	if (agentCGRWarningsOnAction != null) {
//	    getEnableCGRWarningsAction().putValue(Action.SELECTED_KEY, false);
//	}
//    }
//
//    /**
//     * @return an {@link Action} for building UI with this feature
//     */
//    public BooleanAction getEnableCGRWarningsAction() {
//	if (agentCGRWarningsOnAction == null) {
//	    agentCGRWarningsOnAction = (BooleanAction) LoggingAction.CGR_WARNINGS.getActionFor(myAgent);
//	}
//	return agentCGRWarningsOnAction;
//    }

	/**
	 * Creates a default log file for this logger. This call is equivalent to
	 * <code>addLogFile(null, null, false, true)</code> This file will be located in
	 * the directory specified by the MaDKit property {@link Option#logDirectory},
	 * which is set to "logs" by default.
	 * 
	 * @see #addFileHandler(Path, String, boolean, boolean)
	 */
	public void createLogFile() {
//	addFileHandler(null, null, false, true);
	}

//    /**
//     * Adds a new {@link FileHandler} to this logger. This method provides an easy way of creating a new file handler with
//     * an agent formatting and with a corresponding file located in a specified directory. The related file will be located
//     * in the directory specified by the MaDKit property {@link Option#logDirectory}, which is set to "logs" followed by a
//     * directory named according to the date of the run.
//     * 
//     * @param dir
//     *            the logDirectory to be used may be {@code null}, in which case the file will be located in the directory
//     *            specified by the MaDKit property {@link Option#logDirectory} which is set to "logs" by default.
//     * @param fileName
//     *            may be {@code null}, in which case {@link #getName()} is used
//     * @param append
//     *            if <code>true</code>, then bytes will be written to the end of the file rather than the beginning
//     * @param includeDefaultComment
//     *            if <code>true</code>, includes comments displaying creation and closing dates
//     * @see FileHandler
//     */
//    public void addFileHandler(Path dir, String fileName, boolean append, boolean includeDefaultComment) {
//	if (fileName == null) {
//	    fileName = getName();// NOSONAR argument was null
//	}
//	if (dir == null) {
//	    dir = FileSystems.getDefault().getPath(dir);// NOSONAR
//	}
//	try {
//	    Files.createDirectories(dir);
//	    final Path pathToFile = Paths.get(dir, fileName);
//
//	    final String lineSeparator = "--------------------------------------------------------------------------\n";
//	    final String logSession = lineSeparator + "-- Log session for " + getName();
//	    final String logEnd = " --\n" + lineSeparator + "\n";
//
//	    final FileHandler fh = new FileHandler(pathToFile.toString(), append) {
//
//		@Override
//		public synchronized void close() {
//		    if (includeDefaultComment) {
//			String closeString = "\n\n" + logSession + " closed on  " + Madkit.DATE_FORMATTER.format(Instant.now()) + logEnd;
//			publish(new LogRecord(TALK, closeString));
//		    }
//		    super.close();
//		}
//	    };
//	    fh.setFormatter(AGENT_FILE_FORMATTER);
//	    addHandler(fh);
//	    if (includeDefaultComment) {
//		final String startComments = logSession + " started on " + Madkit.DATE_FORMATTER.format(Instant.now()) + logEnd;
//		fh.publish(new LogRecord(TALK, startComments));
//	    }
//	}
//	catch(SecurityException | IOException e) {
//	    e.printStackTrace();// NOSONAR
//	}
//    }
//
//    final synchronized void close() {
//	for (final Handler h : getHandlers()) {
//	    removeHandler(h);
//	    h.close();
//	}
//	agentLoggers.remove(myAgent);
//    }

	/**
	 * Logs a {@link #TALK} message. This uses a special level which could be used
	 * to produce messages that will be rendered as they are, without any formatting
	 * work nor end-of-line character.
	 * <p>
	 * If the logger's level is not {@link Level#OFF} then the given message is
	 * forwarded to all the registered output Handler objects.
	 * <p>
	 * If the logger's level is {@link Level#OFF} then the message is only printed
	 * to {@link System#out}
	 * 
	 * @param msg The string message
	 */
	public void talk(final String msg) {
		if (getLevel() == Level.OFF)
			System.out.print(msg);// NOSONAR
		else
			log(TALK, msg);
	}

	/**
	 * override so that the throwable is printed if not <code>null</code>
	 */
	@Override
	public void log(final LogRecord record) {
		Throwable t = record.getThrown();
		if (t != null) {
			final StringWriter sw = new StringWriter();
			final PrintWriter pw = new PrintWriter(sw);
			t.printStackTrace(pw);
			pw.close();
			record.setMessage(record.getMessage() + "\n ** " + sw);
		}
		super.log(record);
	}

//    /**
//     * This call bypasses any settings and always produces severe log messages displaying the stack trace of the throwable
//     * if it is not <code>null</code>
//     * 
//     * @param message
//     *            the message to display
//     * @param t
//     *            the related exception if any. It can be <code>null</code>
//     */
//    public void severeLog(final String message, final Throwable t) {
//	// This will also be logged by the kernel at FINEST
//	final AgentLogger kernelLogger = myAgent.getMadkitKernel().logger;
//	if (kernelLogger != null) {
//	    kernelLogger.log(Level.WARNING, t, () -> "log for " + myAgent + "\n" + message);
//	}
//	if (t != null) {
//	    myAgent.filterAgentStackTrace(t);
//	}
//	if (getLevel() == Level.OFF) {
//	    setLevel(Level.SEVERE);
//	}
//	log(Level.SEVERE, message, t);
//    }

	/**
	 * Check if a message of the given level would actually be logged by this
	 * logger. This check is based on the Loggers effective level, which may be
	 * inherited from its parent.
	 * Has been overridden for improving performances.
	 *
	 * @param level a message logging level
	 * @return true if the given message level is currently being logged.
	 */
	@Override
	public boolean isLoggable(Level level) {// override for performance: Level.OFF -> performance
		return !(level == Level.OFF || level.intValue() < getLevel().intValue());
	}
}
