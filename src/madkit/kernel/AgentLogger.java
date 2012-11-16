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
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import madkit.gui.menu.AgentLogLevelMenu;
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
public class AgentLogger extends Logger {

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

	final static Level												talkLevel				= Level
																												.parse("1100");
	final static private Map<AbstractAgent, AgentLogger>	agentLoggers			= new ConcurrentHashMap<AbstractAgent, AgentLogger>();	// TODO evaluate foot print

	final private AbstractAgent									myAgent;

	private Level														warningLogLevel		= LevelOption.warningLogLevel
																												.getValue(Madkit.defaultConfig);

	final static AgentLogger getLogger(final AbstractAgent agent) {
		AgentLogger al = agentLoggers.get(agent);
		if (al == null || !al.getName().equals(agent.getLoggingName())) {
			if (al != null) {
				for (final Handler h : al.getHandlers()) {
					h.close();
				}
			}
			al = new AgentLogger(agent);
			agentLoggers.put(agent, al);
		}
		return al;
	}

	static void removeLogger(final AbstractAgent agent) {
		agentLoggers.remove(agent);
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
		this.warningLogLevel = warningLogLevel;
		AgentLogLevelMenu.update(myAgent);
	}

	private AgentLogger() {
		super("[UNREGISTERED AGENT]", null);
		myAgent = null;
		setUseParentHandlers(false);
		super.setLevel(LevelOption.agentLogLevel.getValue(Madkit.defaultConfig));
		if (!BooleanOption.noAgentConsoleLog.isActivated(Madkit.defaultConfig)) {
			addHandler(new ConsoleHandler());
		}
	}

	private AgentLogger(final AbstractAgent agent) {
		super(agent.getLoggingName(), null);
		myAgent = agent;
		setUseParentHandlers(false);
		final Level l = agent.logger == null ? Level.OFF
				: LevelOption.agentLogLevel.getValue(agent.getMadkitConfig());
		super.setLevel(l);
		setWarningLogLevel(LevelOption.warningLogLevel.getValue(agent
				.getMadkitConfig()));
		if (!BooleanOption.noAgentConsoleLog.isActivated(agent.getMadkitConfig())) {
			ConsoleHandler ch = new ConsoleHandler();
			addHandler(ch);
			ch.setFormatter(AGENT_FORMATTER);
		}
		if (BooleanOption.createLogFiles.isActivated(myAgent.getMadkitConfig())) {
			createLogFile();
		}
	}

	/**
	 * Creates a log file for this logger.
	 * This file will be located in the directory specified by
	 * the MaDKit property {@link Option#logDirectory}
	 */
	public void createLogFile() {
		final String logDir = myAgent.getMadkitConfig().getProperty(
				Option.logDirectory.name());
		new File(logDir).mkdirs();
		final String fileName = logDir + File.separator + getName();
		if (! new File(fileName).exists()) {
			addHandler(getFileHandler(fileName));
		}
	}

	static private FileHandler getFileHandler(final String logFileName){
		final File logFile = new File(logFileName);
		FileHandler fh = null;
		final String logSession = "\n------------------------------------------------------------------\n" +
				"-- Log session for "+logFileName.substring(logFileName.lastIndexOf(File.separator)+1);
		final String logEnd= " --\n------------------------------------------------------------------\n\n";
		final Date date = new Date();
			try {
				FileWriter fw = new FileWriter(logFile, true);
				fw.write(logSession+" started on "+Madkit.dateFormat.format(date)+logEnd);
				fw.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			try {
			fh = new FileHandler(logFileName,true){
				public synchronized void close() throws SecurityException {
					super.close();
					try {
						FileWriter fw = new FileWriter(logFile, true);
						date.setTime(System.currentTimeMillis());
						fw.write(logSession+" closed on  "+Madkit.dateFormat.format(date)+logEnd);
						fw.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			};
			fh.setFormatter(AGENT_FILE_FORMATTER);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return fh;
	}

	@Override
	public synchronized void addHandler(final Handler handler)
			throws SecurityException {
		super.addHandler(handler);
		handler.setLevel(getLevel());
	}

	static void resetLoggers() {
		for (final Logger l : agentLoggers.values()) {
			for (final Handler h : l.getHandlers()) {
				l.removeHandler(h);
				try {
					h.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			// l.setLevel(null);
		}
	}

	/**
	 * Log a TALK message. This uses a special level which could be used
	 * to produce messages that will be rendered as they are, without any
	 * formatting work nor end-of-line character.
	 * <p>
	 * If the logger's level is not {@link Level#OFF} then the given message is forwarded to all the registered output Handler objects.
	 * <p>
	 * If the logger's level is {@link Level#OFF} then the message is only printed to {@link System#err}
	 * 
	 * @param msg The string message
	 */
	public void talk(final String msg) {
		if (getLevel() == Level.OFF)
			System.err.print(msg);
		else
			log(talkLevel, msg);
	}

	/**
	 * Set the log level for the corresponding agent.
	 */
	@Override
	public void setLevel(final Level newLevel) throws SecurityException {
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
		return getName() + " logger: \n\tlevel " + getLevel()
				+ "\n\twarningLogLevel " + getWarningLogLevel();
	}

	/**
	 * @see java.util.logging.Logger#log(java.util.logging.LogRecord)
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

	/**
	 * This call bypasses any settings and always produces severe log
	 * messages which display the stack trace of the throwable if it is
	 * not <code>null</code>
	 * 
	 * @param msg the message to display
	 * @param t the exception raised
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
		log(Level.SEVERE, msg, t);
		setLevel(lvl);
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