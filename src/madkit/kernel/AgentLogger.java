/*
 * Copyright or © or Copr. Fabien Michel, Olivier Gutknecht, Jacques Ferber (1997)

fmichel@lirmm.fr
olg@no-distance.net
ferber@lirmm.fr

This software is a computer program whose purpose is to 
provide a lightweight Java library for designing and simulating Multi-Agent Systems (MAS).

This software is governed by the CeCILL-C license under French law and
abiding by the rules of distribution of free software.  You can  use, 
modify and/ or redistribute the software under the terms of the CeCILL-C
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info". 

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability. 

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or 
data to be ensured and,  more generally, to use and operate it in the 
same conditions as regards security. 

The fact that you are presently reading this means that you have had
knowledge of the CeCILL-C license and that you accept its terms.
 */
package madkit.kernel;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.swing.Action;
import javax.swing.JOptionPane;

import madkit.action.LoggingAction;
import madkit.gui.menu.AgentLogLevelMenu;
import madkit.i18n.Words;
import madkit.kernel.Madkit.BooleanOption;
import madkit.kernel.Madkit.LevelOption;
import madkit.kernel.Madkit.Option;

/**
 * This class defines a logger specialized for MaDKit agents.
 * 
 * @author Fabien Michel
 * @version 0.92
 * @since MaDKit 5.0.0.5
 */
public final class AgentLogger extends Logger {

    /**
     * Defines the default formatter as : [agent's name] LOG_LEVEL : message
     */
    public static final Formatter AGENT_FORMATTER = new AgentFormatter();
    /**
     * Defines the default file formatter as : LOG_LEVEL : message
     */
    public static final Formatter AGENT_FILE_FORMATTER = new AgentFormatter() {

	@Override
	protected String getHeader(final LogRecord record) {
	    return "";
	}
    };

    static final Level TALK_LEVEL = Level.parse("1100");

    private static Map<AbstractAgent, AgentLogger> agentLoggers = new ConcurrentHashMap<>();

    private final AbstractAgent myAgent;
    private Action agentCGRWarningsOnAction;

    static final AgentLogger getLogger(final AbstractAgent agent) {
	AgentLogger al = agentLoggers.get(agent);
	if (al == null) {
	    al = new AgentLogger(agent);
	    agentLoggers.put(agent, al);
	}
	return al;
    }

    private AgentLogger(final AbstractAgent agent) {
	super("[" + agent.getName() + "]", null);
	myAgent = agent;
	setUseParentHandlers(false);
	final Properties madkitConfig = myAgent.getMadkitConfig();
	setLevel(LevelOption.agentLogLevel.getValue(madkitConfig));
	if (BooleanOption.cgrWarnings.isActivated(madkitConfig)) {
	    enableCGRWarnings();
	}
	if (!BooleanOption.noAgentConsoleLog.isActivated(madkitConfig)) {
	    ConsoleHandler ch = new ConsoleHandler();
	    addHandler(ch);
	    ch.setFormatter(AGENT_FORMATTER);
	}
	if (BooleanOption.createLogFiles.isActivated(madkitConfig) && agent.getMadkitKernel() != agent) {
	    createLogFile();
	}
    }

    /**
     * Set the log level for the corresponding agent. If <code>newLevel</code> is {@link Level#ALL} then
     * {@link #enableCGRWarnings()} is automatically triggered.
     */
    @Override
    public void setLevel(final Level newLevel) {
	super.setLevel(Objects.requireNonNull(newLevel));
	for (Handler h : getHandlers()) {
	    h.setLevel(newLevel);
	}
	if (myAgent == myAgent.getKernel())// a bit dirty, isn't it ?
	    return;
	if (myAgent.hasGUI()) {
	    AgentLogLevelMenu.update(myAgent);// TODO level model
	}
	if (newLevel != Level.OFF) {
	    myAgent.setKernel(myAgent.getKernel().getLoggedKernel());
	    if (newLevel == Level.ALL) {
		enableCGRWarnings();
	    }
	}
	else {
	    myAgent.setKernel(myAgent.getMadkitKernel());
	}
    }

    /**
     * Tells if CGR warnings (Community, Group, Role) are enabled.
     * 
     * @see #activateAgentRequestsWarnings(boolean)
     * @return <code>true</code> if CGR warnings are enabled for this logger
     */
    public boolean isCGRWarningsOn() {
	return agentCGRWarningsOnAction != null && (boolean) agentCGRWarningsOnAction.getValue(Action.SELECTED_KEY);
    }

    /**
     * Enables or disables the logging of {@link Level#WARNING} messages related with failed queries over the artificial
     * society. For instance, if an agent tries to get agent addresses using
     * {@link AbstractAgent#getAgentsWithRole(String, String, String)} over a CGR location which does not exist then there
     * will be a warning about that. Since such results could be obtained by agents on purpose, this method provides a
     * convenient way of disabling/enabling these kind of traces.
     * 
     * @param agentRequestsWarningsOn
     *            if <code>true</code>, CGR warnings are enabled, disabled otherwise
     */
    public void enableCGRWarnings() {
	getEnableCGRWarningsAction().putValue(Action.SELECTED_KEY, true);
    }

    public void disableCGRWarnings() {
	if (agentCGRWarningsOnAction != null) {
	    getEnableCGRWarningsAction().putValue(Action.SELECTED_KEY, false);
	}
    }

    /**
     * @return an {@link Action} for building UI with this feature
     */
    public Action getEnableCGRWarningsAction() {
	if (agentCGRWarningsOnAction == null) {
	    agentCGRWarningsOnAction = LoggingAction.CGR_WARNINGS.getActionFor(myAgent);
	}
	return agentCGRWarningsOnAction;
    }

    /**
     * Creates a default log file for this logger. This call is equivalent to
     * <code>addLogFile(null, null, false, true)</code> This file will be located in the directory specified by the MaDKit
     * property {@link Option#logDirectory}, which is set to "logs" by default.
     * 
     * @see #addFileHandler(Path, String, boolean, boolean)
     */
    public void createLogFile() {
	addFileHandler(null, null, false, true);
    }

    /**
     * Adds a new {@link FileHandler} to this logger. This method provides an easy way of creating a new file handler with
     * an agent formatting and with a corresponding file located in a specified directory. The related file will be located
     * in the directory specified by the MaDKit property {@link Option#logDirectory}, which is set to "logs" followed by a
     * directory named according to the date of the run.
     * 
     * @param logDirectory
     *            the logDirectory to be used may be {@code null}, in which case the file will be located in the directory
     *            specified by the MaDKit property {@link Option#logDirectory} which is set to "logs" by default.
     * @param fileName
     *            may be {@code null}, in which case {@link #getName()} is used
     * @param append
     *            if <code>true</code>, then bytes will be written to the end of the file rather than the beginning
     * @param includeDefaultComment
     *            if <code>true</code>, includes comments displaying creation and closing dates
     * @see FileHandler
     */
    public void addFileHandler(Path logDirectory, String fileName, boolean append, boolean includeDefaultComment) {
	if (fileName == null) {
	    fileName = getName();// NOSONAR argument was null
	}
	if (logDirectory == null) {
	    logDirectory = FileSystems.getDefault().getPath(myAgent.getMadkitConfig().getProperty(Option.logDirectory.name()));// NOSONAR
	}
	try {
	    Files.createDirectories(logDirectory);
	    final Path pathToFile = Paths.get(logDirectory.toString(), fileName);

	    final String lineSeparator = "--------------------------------------------------------------------------\n";
	    final String logSession = lineSeparator + "-- Log session for " + getName();
	    final String logEnd = " --\n" + lineSeparator + "\n";

	    final FileHandler fh = new FileHandler(pathToFile.toString(), append) {

		@Override
		public synchronized void close() {
		    if (includeDefaultComment) {
			String closeString = "\n\n" + logSession + " closed on  " + Madkit.DATE_FORMATTER.format(Instant.now()) + logEnd;
			publish(new LogRecord(TALK_LEVEL, closeString));
		    }
		    super.close();
		}
	    };
	    fh.setFormatter(AGENT_FILE_FORMATTER);
	    addHandler(fh);
	    if (includeDefaultComment) {
		final String startComments = logSession + " started on " + Madkit.DATE_FORMATTER.format(Instant.now()) + logEnd;
		fh.publish(new LogRecord(TALK_LEVEL, startComments));
	    }
	}
	catch(SecurityException | IOException e) {
	    e.printStackTrace();// NOSONAR
	}
    }

    final synchronized void close() {
	for (final Handler h : getHandlers()) {
	    removeHandler(h);
	    h.close();
	}
	agentLoggers.remove(myAgent);
    }

    @Override
    public synchronized void addHandler(final Handler handler) {
	super.addHandler(handler);
	handler.setLevel(getLevel());
    }

    static void resetLoggers() {
	for (final AgentLogger l : agentLoggers.values()) {
	    l.close();
	}
    }

    /**
     * Logs a {@link #TALK_LEVEL} message. This uses a special level which could be used to produce messages that will be
     * rendered as they are, without any formatting work nor end-of-line character.
     * <p>
     * If the logger's level is not {@link Level#OFF} then the given message is forwarded to all the registered output
     * Handler objects.
     * <p>
     * If the logger's level is {@link Level#OFF} then the message is only printed to {@link System#out}
     * 
     * @param msg
     *            The string message
     */
    public void talk(final String msg) {
	if (getLevel() == Level.OFF)
	    System.out.print(msg);// NOSONAR
	else
	    log(TALK_LEVEL, msg);
    }

    @Override
    public String toString() {
	return getName() + " logger: \n\tlevel " + getLevel() + "\n\tcgrWarnings " + isCGRWarningsOn();
    }

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

    /**
     * This call bypasses any settings and always produces severe log messages displaying the stack trace of the throwable
     * if it is not <code>null</code>
     * 
     * @param message
     *            the message to display
     * @param t
     *            the related exception if any. It can be <code>null</code>
     */
    public void severeLog(final String message, final Throwable t) {
	// This will also be logged by the kernel at FINEST
	final AgentLogger kernelLogger = myAgent.getMadkitKernel().logger;
	if (kernelLogger != null) {
	    kernelLogger.log(Level.WARNING, t, () -> "log for " + myAgent + "\n" + message);
	}
	if (t != null) {
	    myAgent.filterAgentStackTrace(t);
	}
	if (getLevel() == Level.OFF) {
	    setLevel(Level.SEVERE);
	}
	log(Level.SEVERE, message, t);
    }

    /**
     * This call bypasses any settings and always produces severe log messages whatever the logger's current level.
     * 
     * @param msg
     *            the message to display
     */
    public void severeLog(final String msg) {
	severeLog(msg, null);
    }

    /**
     * Set all the agents' loggers to the specified level
     * 
     * @param level
     *            the new level
     */
    public static void setAllLogLevels(final Level level) {
	for (AbstractAgent loggedAgent : agentLoggers.keySet()) {
	    if (loggedAgent != loggedAgent.getMadkitKernel()) {
		loggedAgent.getLogger().setLevel(level);
	    }
	    else
		loggedAgent.setMadkitProperty(LevelOption.agentLogLevel.name(), level.toString());
	}
    }

    /**
     * Create a log file for each agent having a non <code>null</code> logger.
     * 
     * @see AgentLogger#createLogFile()
     */
    public static void createLogFiles() {
	try {
	    AbstractAgent a = new ArrayList<>(agentLoggers.keySet()).get(0);
	    a.setMadkitProperty(BooleanOption.createLogFiles.name(), "true");
	    JOptionPane.showMessageDialog(null, Words.DIRECTORY + " " + new File(a.getMadkitProperty(Option.logDirectory)).getAbsolutePath() + " " + Words.CREATED, "OK",
		    JOptionPane.INFORMATION_MESSAGE);
	    for (AgentLogger logger : agentLoggers.values()) {
		logger.createLogFile();
	    }
	}
	catch(IndexOutOfBoundsException e) {
	    JOptionPane.showMessageDialog(null, "No active agents yet", Words.FAILED.toString(), JOptionPane.WARNING_MESSAGE);
	}
    }

    /**
     * now useless.
     * 
     * @deprecated as of MaDKit 5.2, replaced by {@link AgentLogger#enableCGRWarnings()}
     */
    @Deprecated
    public Level getWarningLogLevel() {
	return Level.ALL;
    }

    /**
     * now useless.
     * 
     * @deprecated as of MaDKit 5.2, replaced by {@link AgentLogger#enableCGRWarnings()}
     */
    @Deprecated
    public void setWarningLogLevel(final Level warningLogLevel) {
	if (warningLogLevel != Level.OFF) {
	    enableCGRWarnings();
	}
    }
}

class AgentFormatter extends Formatter {

    @Override
    public String format(final LogRecord record) {
	final Level lvl = record.getLevel();
	if (lvl.equals(AgentLogger.TALK_LEVEL)) {
	    return record.getMessage();
	}
	return getHeader(record) + lvl.getLocalizedName() + " : " + record.getMessage() + "\n";
    }

    protected String getHeader(final LogRecord record) {
	return record.getLoggerName() + " ";
    }

}