/*
 * Copyright 1997-2012 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MaDKit.
 * 
 * MaDKit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MaDKit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MaDKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.kernel;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import madkit.gui.menu.AgentLogLevelMenu;
import madkit.i18n.Words;
import madkit.kernel.Madkit.BooleanOption;
import madkit.kernel.Madkit.LevelOption;
import madkit.kernel.Madkit.Option;

/**
 * This class defines a logger specialized for MaDKit agents.
 * 
 * @author Fabien Michel
 * @version 0.91
 * @since MaDKit 5.0.0.5
 * 
 */
final public class AgentLogger extends Logger {

	/**
	 * Defines the default formatter as : [agent's name] LOG_LEVEL : message
	 */
	final public static Formatter									AGENT_FORMATTER		= new AgentFormatter();
	/**
	 * Defines the default file formatter as : LOG_LEVEL : message
	 */
	final public static Formatter									AGENT_FILE_FORMATTER	= new AgentFormatter() {
																											@Override
																											protected String getHeader(
																													final LogRecord record) {
																												return "";
																											}
																										};
	final static AgentLogger										defaultAgentLogger	= new AgentLogger();

	final static Level												talkLevel				= Level.parse("1100");
	final static private Map<AbstractAgent, AgentLogger>	agentLoggers			= new ConcurrentHashMap<>();	// TODO evaluate foot print
	
	private FileHandler	fh;

	final private AbstractAgent									myAgent;

	private Level														warningLogLevel		= LevelOption.warningLogLevel
																												.getValue(Madkit.defaultConfig);

	final static AgentLogger getLogger(final AbstractAgent agent) {
		AgentLogger al = agentLoggers.get(agent);
		if (al == null) {
			al = new AgentLogger(agent);
			agentLoggers.put(agent, al);
		}
		return al;
	}

	// public static void renameLogger(AbstractAgent agent) {
	// AgentLogger al = agentLoggers.get(agent);
	// if(! al.getName().equals(agent.getName())){
	//
	// }
	// if(al == null){
	// al = new AgentLogger(agent);
	// agentLoggers.put(agent, al);
	// LogManager.getLogManager().addLogger(al);
	// }
	// return al;
	// }

	/**
	 * Returns the log level above which MaDKit warnings are displayed 
	 * for the corresponding agent.
	 * 
	 * @return the warningLogLevel of the corresponding agent
	 */
	public Level getWarningLogLevel() {
		return warningLogLevel;
	}

	/**
	 * Sets the agent's log level above which MaDKit warnings are displayed
	 * 
	 * @param warningLogLevel the log level to set
	 */
	public void setWarningLogLevel(final Level warningLogLevel) {
		if(warningLogLevel == null)
			throw new NullPointerException();
		this.warningLogLevel = warningLogLevel;
		AgentLogLevelMenu.update(myAgent);
	}

	private AgentLogger() {
		super("[UNREGISTERED AGENT]", null);
		myAgent = null;
		setUseParentHandlers(false);
		super.setLevel(LevelOption.agentLogLevel.getValue(Madkit.defaultConfig));
		if (! BooleanOption.noAgentConsoleLog.isActivated(Madkit.defaultConfig)) {
			addHandler(new ConsoleHandler());
		}
	}

	private AgentLogger(final AbstractAgent agent) {
		super("[" + agent.getName() + "]", null);
		myAgent = agent;
		setUseParentHandlers(false);
		final Properties madkitConfig = myAgent.getMadkitConfig();
		final Level l = myAgent.logger == null ? Level.OFF : LevelOption.agentLogLevel.getValue(madkitConfig);
		super.setLevel(l);
		setWarningLogLevel(LevelOption.warningLogLevel.getValue(madkitConfig));
		if (! BooleanOption.noAgentConsoleLog.isActivated(madkitConfig)) {
			ConsoleHandler ch = new ConsoleHandler();
			addHandler(ch);
			ch.setFormatter(AGENT_FORMATTER);
		}
		if (BooleanOption.createLogFiles.isActivated(madkitConfig) && agent.getMadkitKernel() != agent) {
			createLogFile();
		}
	}

	/**
	 * Creates a log file for this logger.
	 * This file will be located in the directory specified by
	 * the MaDKit property {@link Option#logDirectory}, 
	 * which is set to "logs" by default.
	 */
	public void createLogFile() {
		if (fh == null) {
			final String logDir = myAgent.getMadkitConfig().getProperty(Option.logDirectory.name());
			new File(logDir).mkdirs();
			final String logFileName = logDir + File.separator + getName();
			final File logFile = new File(logFileName);
			final String lineSeparator = "----------------------------------------------------------------------\n";
			final String logSession = lineSeparator + "-- Log session for "
					+ logFileName.substring(logFileName.lastIndexOf(File.separator) + 1);
			final String logEnd = " --\n"+lineSeparator+"\n";
			final Date date = new Date();
			try (FileWriter fw = new FileWriter(logFile, true)) {
				fw.write(logSession + " started on " + Madkit.dateFormat.format(date) + logEnd);
				fh = new FileHandler(logFileName, true) {
					public synchronized void close() throws SecurityException {
						super.close();
						try (FileWriter fw2 = new FileWriter(logFile, true)) {
							date.setTime(System.currentTimeMillis());
							fw2.write("\n\n"+logSession + " closed on  " + Madkit.dateFormat.format(date) + logEnd);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				};
				fh.setFormatter(AGENT_FILE_FORMATTER);
			} catch (SecurityException | IOException  e) {
				e.printStackTrace();
			}
			addHandler(fh);
		}
	}

	final synchronized void close(){
		for (final Handler h : getHandlers()) {
			removeHandler(h);
				h.close();
		}
		agentLoggers.remove(myAgent);
	}

	@Override
	public synchronized void addHandler(final Handler handler)
			throws SecurityException {
		super.addHandler(handler);
		handler.setLevel(getLevel());
	}
	

	static void resetLoggers() {
		for (final AgentLogger l : agentLoggers.values()) {
				l.close();
		}
	}

	/**
	 * Log a TALK message. This uses a special level which could be used
	 * to produce messages that will be rendered as they are, without any
	 * formatting work nor end-of-line character.
	 * <p>
	 * If the logger's level is not {@link Level#OFF} then the given message is forwarded to all the registered output Handler objects.
	 * <p>
	 * If the logger's level is {@link Level#OFF} then the message is only printed to {@link System#out}
	 * 
	 * @param msg The string message
	 */
	public void talk(final String msg) {
		if (getLevel() == Level.OFF)
			System.out.print(msg);
		else
			log(talkLevel, msg);
	}

	/**
	 * Set the log level for the corresponding agent.
	 */
	@Override
	public void setLevel(final Level newLevel) throws SecurityException {
		if(newLevel == null)
			throw new NullPointerException();
		super.setLevel(newLevel);
		for (Handler h : getHandlers()) {
			h.setLevel(newLevel);
		}
		if (myAgent.hasGUI()) {
			AgentLogLevelMenu.update(myAgent);
			// updateAgentUi();//TODO level model
		}
	}

	@Override
	public String toString() {
		return getName() + " logger: \n\tlevel " + getLevel() + "\n\twarningLogLevel " + getWarningLogLevel();
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
	 * This call bypasses any settings and always produces severe log
	 * messages which display the stack trace of the throwable if it is
	 * not <code>null</code>
	 * 
	 * @param msg the message to display
	 * @param t the related exception if any. It can be <code>null</code>
	 */
	public void severeLog(final String msg, final Throwable t) {
		// This will also be logged by the kernel at FINEST
		final Logger l = myAgent.getMadkitKernel().logger;
		if (l != null) {
			l.log(Level.FINEST, "log for " + myAgent + "\n" + msg, t);
		}
		if (t != null) {
			myAgent.setAgentStackTrace(t);
		}
		final Level lvl = getLevel();
		if (lvl == Level.OFF) {
			setLevel(Level.ALL);
		}
		if (t != null) {
			log(Level.SEVERE, msg, t);
		}
		else{
			log(Level.SEVERE,msg);
		}
		setLevel(lvl);
	}

	/**
	 * This call bypasses any settings and always produces severe log
	 * messages.
	 * 
	 * @param msg the message to display
	 */
	public void severeLog(final String msg) {
			severeLog(msg, null);
		}

	/**
	 * Set all the agents' loggers to the specified level
	 * 
	 * @param level the new level
	 */
	public static void setAllLogLevels(final Level level) {
		for (AbstractAgent loggedAgent : agentLoggers.keySet()) {
			if (loggedAgent != loggedAgent.getMadkitKernel()) {
				loggedAgent.setLogLevel(level);
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
		try{
			AbstractAgent a = new ArrayList<>(agentLoggers.keySet()).get(0);
			a.setMadkitProperty(BooleanOption.createLogFiles.name(), "true");
			JOptionPane.showMessageDialog(null, Words.DIRECTORY+" "+new File(a.getMadkitProperty(Option.logDirectory)).getAbsolutePath() + " "
					+ Words.CREATED, "OK", JOptionPane.INFORMATION_MESSAGE);
			for (AgentLogger logger : agentLoggers.values()) {
				logger.createLogFile();
			}
		}
		catch(IndexOutOfBoundsException e){
			JOptionPane.showMessageDialog(null, "No active agents yet", Words.FAILED.toString(), JOptionPane.WARNING_MESSAGE);
		}
	}
}

class AgentFormatter extends Formatter {

	@Override
	public String format(final LogRecord record) {
		final Level lvl = record.getLevel();
		if (lvl.equals(AgentLogger.talkLevel)) {
			return record.getMessage();
		}
		return getHeader(record) + lvl.getLocalizedName() + " : "
				+ record.getMessage() + "\n";
	}

	protected String getHeader(final LogRecord record) {
		return record.getLoggerName() + " ";
	}

}