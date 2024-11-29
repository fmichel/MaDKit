package madkit.kernel;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import madkit.logging.AgentFormatter;

/**
 * This class defines a logger specialized for MaDKit agents.
 * 
 * @author Fabien Michel
 * @version 6
 * @since MaDKit 5.0.0.5
 */
public class AgentLogger extends Logger {

	private static final String LOGFILE_SESSION_SEPARATOR = "--------------------------------------------------------------------------\n";
	private static final String LOGFILE_SESSION_END = " --\n" + LOGFILE_SESSION_SEPARATOR + "\n";

	@SuppressWarnings("static-access")
	static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME
			.ofPattern("yyyy-MM-dd_HH:mm:ss ").withZone(ZoneId.systemDefault());

	public static final Path DEFAULT_LOG_DIRECTORY = FileSystems.getDefault().getPath("Logs");

	/**
	 * Defines the default formatter as : [agent's name] LOG_LEVEL : message
	 */
	public static final Formatter AGENT_FORMATTER = new AgentFormatter();

	/**
	 * Defines the default file formatter as : LOG_LEVEL : message
	 */
	public static final Level TALK = Level.parse("1100");
	/**
	 * Defines the No log logger for simulation purpose.
	 */
	static final AgentLogger NO_LOG_LOGGER = new AgentLogger() {
		@Override
		public final boolean isLoggable(Level level) {
			return false;
		}

		@Override
		public void log(Level level, String msg) {
			// NOSONAR performance purpose
		}

		@Override
		public void log(Level level, Supplier<String> msgSupplier) {
			// NOSONAR performance purpose
		}
	};

	/**
	 * Defines the default file formatter as : LOG_LEVEL : message
	 */
	public static final Formatter AGENT_FILE_FORMATTER = new AgentFormatter() {
		@Override
		protected StringBuilder getHeader(final LogRecord record) {// NOSONAR keeping super name
			return new StringBuilder();
		}
	};

	AgentLogger(final Agent a) {
		super("[" + a.getName() + "] ", null);
		setParent(Madkit.MDK_ROOT_LOGGER);
		if (a.kernel != null && a.kernel != KernelAgent.deadKernel) {
			setLevel(a.getKernelConfig().getLevel(MDKCommandLine.AGENT_LOG_LEVEL));
			if (a.getKernelConfig().getBoolean(MDKCommandLine.CREATE_LOG_FILES)) {
				createLogFile();
			}
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
		return getName() + ' ' + getLevel();
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
	 * Creates a log file for this logger.
	 * 
	 * @param fileName     May be {@code null}, in which case the creation date is
	 *                     used in combination with {@link #getName()}
	 * @param logDirectory the logDirectory to use. May be {@code null}, in which
	 *                     case the file will be located in a directory named
	 *                     {@value #DEFAULT_LOG_DIRECTORY}
	 * @param append       if <code>true</code>, then bytes will be written to the
	 *                     end of the file rather than the beginning
	 * @param comments     if <code>true</code>, includes comments displaying
	 *                     creation and closing dates
	 * 
	 * @see FileHandler
	 */
	public void createLogFile(String fileName, Path logDirectory, boolean append, boolean comments) {
		addFileHandler(
				Objects.requireNonNullElseGet(fileName, () -> DATE_FORMATTER.format(Instant.now()) + getName()),
				Objects.requireNonNullElse(logDirectory, DEFAULT_LOG_DIRECTORY), append, comments);
	}

	/**
	 * This has the same effect as
	 * {@linkplain #createLogFile(String, Path, boolean, boolean)}
	 * {@code (logDirectory, fileName, append, true)}.
	 * 
	 * @param fileName     May be {@code null}, in which case the creation date is
	 *                     used in combination with {@link #getName()}
	 * @param logDirectory the logDirectory to use. May be {@code null}, in which
	 *                     case the file will be located in a directory named
	 *                     {@value #DEFAULT_LOG_DIRECTORY}
	 * @param append       if <code>true</code>, then bytes will be written to the
	 *                     end of the file rather than the beginning
	 */
	public void createLogFile(String fileName, Path logDirectory, boolean append) {
		createLogFile(fileName, logDirectory, append, true);
	}

	/**
	 * This has the same effect as
	 * {@linkplain #createLogFile(String, Path, boolean, boolean)}
	 * {@code (logDirectory, fileName, true, true)}.
	 * 
	 * @param fileName     May be {@code null}, in which case the creation date is
	 *                     used in combination with {@link #getName()}
	 * @param logDirectory the logDirectory to use. May be {@code null}, in which
	 *                     case the file will be located in a directory named
	 *                     {@value #DEFAULT_LOG_DIRECTORY}
	 */
	public void createLogFile(String fileName, Path logDirectory) {
		createLogFile(fileName, logDirectory, true, true);
	}

	/**
	 * This has the same effect as
	 * {@linkplain #createLogFile(String, Path, boolean, boolean)}
	 * {@code (fileName, null, true, true)}.
	 * 
	 * @param fileName May be {@code null}, in which case the creation date is used
	 *                 in combination with {@link #getName()}
	 */
	public void createLogFile(String fileName) {
		createLogFile(fileName, null, true, true);
	}

	/**
	 * This has the same effect as
	 * {@linkplain #createLogFile(String, Path, boolean, boolean)}
	 * {@code (null, null, true, true)}.
	 * 
	 */
	public void createLogFile() {
		createLogFile(null, null, true, true);
	}

	/**
	 * Adds a new {@link FileHandler} to this logger. This method provides an easy
	 * way of creating a new file handler with an agent formatting and a
	 * corresponding file located in a specified directory.
	 * 
	 * @param fileName the fileName
	 * @param dir      the logDirectory to use
	 * @param append   if <code>true</code>, then bytes will be written to the end
	 *                 of the file rather than the beginning
	 * @param comments if <code>true</code>, includes comments displaying creation
	 *                 and closing dates
	 * 
	 * @see FileHandler
	 */
	private void addFileHandler(String fileName, Path dir, boolean append, boolean comments) {
		try {
			Files.createDirectories(dir);
			Path pathToFile = Paths.get(dir.toString(), fileName);
			if (!append) {
				int i = 1;
				while (pathToFile.toFile().exists()) {
					pathToFile = Paths.get(dir.toString(), fileName + "_" + (++i));
				}
			}

			String logSession = LOGFILE_SESSION_SEPARATOR + "-- Log session for " + getName();
			FileHandler fh = new FileHandler(pathToFile.toString(), append) {
				@Override
				public synchronized void close() {
					if (comments) {
						String closeString = "\n\n" + logSession + " closed on  " + DATE_FORMATTER.format(Instant.now())
								+ LOGFILE_SESSION_END;
						publish(new LogRecord(TALK, closeString));
					}
					super.close();
				}
			};
			fh.setFormatter(AGENT_FILE_FORMATTER);
			addHandler(fh);
			if (comments) {
				final String startComments = logSession + " started on " + DATE_FORMATTER.format(Instant.now())
						+ LOGFILE_SESSION_END;
				fh.publish(new LogRecord(TALK, startComments));
			}
		} catch (SecurityException | IOException e) {
			e.printStackTrace();
		}
	}

	void close() {
		Arrays.stream(getHandlers()).filter(FileHandler.class::isInstance).forEach(Handler::close);
	}

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
	 * @param msg the log message
	 */
	public void talk(final String msg) {// NOSONAR
		if (getLevel() == Level.OFF)
			System.out.print(msg);// NOSONAR
		else
			log(TALK, msg);
	}

	/**
	 * override so that the throwable is printed if not <code>null</code>
	 */
	@Override
	public void log(LogRecord record) {// NOSONAR keeping the name used in super implementation
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
	 * inherited from its parent. Has been overridden for improving performances.
	 *
	 * @param level a message logging level
	 * @return true if the given message level is currently being logged.
	 */
	@Override
	public boolean isLoggable(Level level) {// override for performance: Level.OFF -> performance
		return !(level == Level.OFF || level.intValue() < getLevel().intValue());
	}
}
