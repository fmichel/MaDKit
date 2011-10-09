/*
 * Copyright 1997-2011 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MadKit.
 * 
 * MadKit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MadKit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with MadKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.kernel;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import madkit.kernel.Madkit.BooleanOption;
import madkit.kernel.Madkit.LevelOption;
import madkit.kernel.Madkit.Option;

/**
 * This class defines a logger specialized for MadKit agents.
 * 
 * @author Fabien Michel
 * @version 0.9
 * @since MadKit 5.0.0.5
 *
 */
public class AgentLogger extends Logger {

	final static AgentLogger defaultAgentLogger = new AgentLogger();
	final static private Map<AbstractAgent,AgentLogger> agentLoggers = new ConcurrentHashMap<AbstractAgent,AgentLogger>();//TODO evaluate foot print
	final static Level talkLevel = Level.parse("1100");
	private Level warningLogLevel = LevelOption.warningLogLevel.getValue(Madkit.defaultConfig);
	final private AbstractAgent myAgent;

	final public static Formatter agentFormatter = new Formatter(){//TODO create Formatter hierarchy
		@Override
		final public String format(final LogRecord record) {
			final Level lvl = record.getLevel();
			if(lvl.equals(talkLevel)){
				return record.getMessage();
			}
			return record.getLoggerName()+" "+lvl.getLocalizedName()+" : "+record.getMessage()+"\n";
		}
	};

	final public static Formatter agentFileFormatter = new Formatter(){
		@Override
		final public String format(final LogRecord record) {
			final Level lvl = record.getLevel();
			if(lvl.equals(talkLevel)){
				return record.getMessage();
			}
			return lvl.getLocalizedName()+" : "+record.getMessage()+"\n";
		}
	};

	//	final static Formatter unregisteredAgentFormatter = new Formatter(){
	//		@Override
	//		final public String format(final LogRecord record) {
	//			final Level lvl = record.getLevel();
	//			if(lvl.equals(talkLevel)){
	//				return record.getMessage();
	//			}
	//			new Exception().printStackTrace();
	//			return record.getSourceMethodName() + lvl.getLocalizedName()+" : "+formatMessage(record)+"\n";
	//		}
	//	};





	static AgentLogger getLogger(AbstractAgent agent) {
		AgentLogger al = agentLoggers.get(agent);
		if(al == null || ! al.getName().equals(agent.getLoggingName())){
			if(al != null){
				for (Handler h : al.getHandlers()) {
					h.close();
				}
			}
			al = new AgentLogger(agent);
			agentLoggers.put(agent, al);
			//			LogManager.getLogManager().addLogger(al);
		}
		return al;
	}

	static void removeLogger(AbstractAgent agent) {
		agentLoggers.remove(agent);
	}

	//	public static void renameLogger(AbstractAgent agent) {
	//		AgentLogger al = agentLoggers.get(agent);
	//		if(! al.getName().equals(agent.getName())){
	//			
	//		}
	//		if(al == null){
	//			al = new AgentLogger(agent);
	//			agentLoggers.put(agent, al);
	//			LogManager.getLogManager().addLogger(al);
	//		}
	//		return al;
	//	}



	/**
	 * @return the warningLogLevel
	 */
	public Level getWarningLogLevel() {
		return warningLogLevel;
	}
	
	@Override
	public ResourceBundle getResourceBundle() {
		return Madkit.getResourceBundle();
	}

	/** 
	 * Sets the agent's log level above which MadKit warnings are displayed
	 * 
	 * @param warningLogLevel the log level to set
	 */
	public void setWarningLogLevel(Level warningLogLevel) {
		this.warningLogLevel = warningLogLevel;
		updateAgentUi();
	}

	private void updateAgentUi() {
		if (myAgent != null) {
			madkit.gui.GUIToolkit.updateAgentUI(myAgent);
		}
	}

	AgentLogger(){
		super("[UNREGISTERED AGENT]", null);
		myAgent = null;
		setUseParentHandlers(false);
		super.setLevel(LevelOption.agentLogLevel.getValue(Madkit.defaultConfig));
		if(! BooleanOption.noAgentConsoleLog.isActivated(Madkit.defaultConfig)){
			addHandler(new ConsoleHandler());
		}
	}

	AgentLogger(AbstractAgent agent){
		super(agent.getLoggingName(), null);
		myAgent = agent;
		setUseParentHandlers(false);
		super.setLevel(LevelOption.agentLogLevel.getValue(agent.getMadkitConfig()));
		setWarningLogLevel(LevelOption.warningLogLevel.getValue(agent.getMadkitConfig()));
		if(! BooleanOption.noAgentConsoleLog.isActivated(agent.getMadkitConfig())){
			ConsoleHandler ch = new ConsoleHandler();
			addHandler(ch);
			ch.setFormatter(agentFormatter);
		}
		if (Boolean.parseBoolean(myAgent.getMadkitProperty(BooleanOption.createLogFiles.name()))) {
			createLogFile();
		}
	}

	/**
	 */
	void createLogFile() {
			addHandler(getFileHandler(myAgent.getMadkitProperty(Option.logDirectory.name())+File.separator+getName()));
	}
	
	static private FileHandler getFileHandler(final String logFileName){
		FileHandler fh = null;
		final SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss",Locale.getDefault());//TODO i18n + formatting
			try {
				FileWriter fw = new FileWriter(new File(logFileName), true);
				fw.write("\n----------------------------------------------------------------------------\n" +
						"-- Log session for "+logFileName.substring(logFileName.lastIndexOf(File.separator)+1)
						+" started on "+simpleFormat.format(new Date(System.currentTimeMillis()))+
				" --\n----------------------------------------------------------------------------\n\n");
				fw.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			try {
			fh = new FileHandler(logFileName,true){
				public synchronized void close() throws SecurityException {
//					setFormatter(new Formatter() {
//						@Override
//						public String format(LogRecord record) {
//							return "\n----------------------------------------------------------------------------\n-- Log session for "+logFileName.substring(logFileName.lastIndexOf(File.separator)+1)+" closed on "+simpleFormat.format(new Date(record.getMillis()))+" --\n----------------------------------------------------------------------------\n\n";
//						}
//					});
//					publish(new LogRecord(Level.ALL, ""));
//					flush();
//					JOptionPane.showConfirmDialog(null, "c");
					super.close();
					FileWriter fw2;
					try {
						fw2 = new FileWriter(new File(logFileName), true);
						fw2.write("\n----------------------------------------------------------------------------\n" +
						"-- Log session for "+logFileName.substring(logFileName.lastIndexOf(File.separator)+1)
						+" closed on "+simpleFormat.format(new Date(System.currentTimeMillis()))+
						" --\n----------------------------------------------------------------------------\n\n");
						fw2.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				};
			};
			fh.setFormatter(agentFileFormatter);
//			fh.setFormatter(new Formatter() {
//				@Override
//				public String format(LogRecord record) {
//					//TODO good format
////				DateFormat df = DateFormat.getDateInstance(DateFormat.FULL, Locale.getDefault());
////				final Date date = new Date(record.getMillis());
//					return "\n----------------------------------------------------------------------------\n-- Log session for "+logFileName.substring(logFileName.lastIndexOf(File.separator)+1)+" started on "+simpleFormat.format(new Date(record.getMillis()))+" --\n----------------------------------------------------------------------------\n\n";
//				}
//			});
//			fh.publish(new LogRecord(Level.ALL, null));
//			fh.flush();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return fh;
	}


	@Override
	public synchronized void addHandler(Handler handler) throws SecurityException {
		super.addHandler(handler);
		handler.setLevel(getLevel());
	}
	
	static void resetLoggers(){
		for (Logger l : agentLoggers.values()) {
			for(Handler h : l.getHandlers()){
				l.removeHandler(h);
				try {
					h.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
//			l.setLevel(null);
		}
	}

	/**
	 * Log a TALK message. This uses a special level which could be used 
	 * to produce messages that will be rendered as they are, without any 
	 * formatting work nor end-of-line character.
	 * <p>
	 * If the logger's level is not {@link Level#OFF} 
	 * then the given message is forwarded to all the
	 * registered output Handler objects.
	 * <p>
	 * If the logger's level is {@link Level#OFF} 
	 * then the message is only printed to {@link System#err}
	 * @param   msg	The string message 
	 */
	public void talk(String msg){
		if(getLevel() == Level.OFF)
			System.err.print(msg);
		else
			log(talkLevel,msg);
	}

	//	/**
	//	 * @see java.util.logging.Logger#log(java.util.logging.Level, java.lang.String)
	//	 */
	//	@Override
	//	public void log(Level level, String msg) {
	//		if(level == talkLevel)
	//			
	//		super.log(level, msg);
	//	}

	//	void addGUIHandlerFor(AbstractAgent agent){
	//		if(agent.getGUIComponent() != null && agent.getGUIComponent() instanceof AgentGUIModel){
	//			OutputStream os = ((AgentGUIModel) agent.getGUIComponent()).getOutputStream();
	//			if(os != null){
	//				Handler h = new StreamHandler(os, AgentLogger.agentFileFormatter){
	//					@Override
	//					public synchronized void publish(LogRecord record) {
	//						super.publish(record);
	//						flush();
	//					}
	//				};
	//				h.setLevel(getLevel());
	//				addHandler(h);
	//			}
	//		}
	//	}

	/**
	 * @see java.util.logging.Logger#setLevel(java.util.logging.Level)
	 */
	@Override
	public void setLevel(Level newLevel) throws SecurityException {
		super.setLevel(newLevel);
		for(Handler h : getHandlers()){
			h.setLevel(newLevel);
		}
		updateAgentUi();//TODO level model
	}

	@Override
	public String toString() {
		return "Agent Logger"+ getName();
	}

	/**
	 * @see java.util.logging.Logger#log(java.util.logging.LogRecord)
	 */
	@Override
	public void log(final LogRecord record) {
		Throwable t = record.getThrown();
		if(t != null){
			final StringWriter sw = new StringWriter();
			final PrintWriter pw = new PrintWriter(sw);
			t.printStackTrace(pw);
			pw.close();
			record.setMessage(record.getMessage()+"\n ** "+sw);
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
	public void severeLog(String msg, Throwable t) {
		myAgent.getKernel().getMadkitKernel().getLogger().log(Level.FINEST,"log for "+myAgent+"\n"+msg,t);
		myAgent.setAgentStackTrace(t);
		final Level l = getLevel();
		if (l == Level.OFF) {
			setLevel(Level.ALL);
			log(Level.SEVERE, msg, t);
			setLevel(l);
		}
		else{
			log(Level.SEVERE, msg, t);
		}
	}
	
}