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
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

import madkit.kernel.gui.AgentGUIModel;

/**
 * This class defines a logger specialized for MadKit agents.
 * 
 * @author Fabien Michel
 * @version 0.9
 * @since MadKit 5.0.0.5
 *
 */
public class AgentLogger extends Logger {

	final static String madkitMessageBundleFile = "madkitMessageBundle";
	final static Level talkLevel = Level.parse("1100");
	

	final static Formatter agentFormatter = new Formatter(){//TODO create Formatter hierarchy
		@Override
		final public String format(final LogRecord record) {
			final Level lvl = record.getLevel();
			if(lvl.equals(talkLevel)){
				return record.getMessage();
			}
			return record.getLoggerName()+" "+lvl.getLocalizedName()+" : "+formatMessage(record)+"\n";
		}
	};

	final static Formatter agentFileFormatter = new Formatter(){
		@Override
		final public String format(final LogRecord record) {
			final Level lvl = record.getLevel();
			if(lvl.equals(talkLevel)){
				return record.getMessage();
			}
			return lvl.getLocalizedName()+" : "+formatMessage(record)+"\n";
		}
	};

	private Level warningLogLevel = Level.INFO;

	final static AgentLogger getDefaultAgentLogger(){
		return new AgentLogger("MK Agent");
	}
	
	/**
	 * @return the warningLogLevel
	 */
	public Level getWarningLogLevel() {
		return warningLogLevel;
	}

	/**
	 * @param warningLogLevel the warningLogLevel to set
	 */
	public void setWarningLogLevel(Level warningLogLevel) {
		this.warningLogLevel = warningLogLevel;
	}

	/**
	 * @param simpleName
	 */
	AgentLogger(String simpleName) {
		super(simpleName, madkitMessageBundleFile);
		setLevel(Level.INFO);
	}

	/**
	 * Log a TALK message. This uses a special level which could be used 
	 * to produce messages that will be rendered as they are, without any 
	 * formatting work nor end-of-line character.
	 * <p>
	 * If the logger is currently not <code>null</code>, 
	 * then the given message is forwarded to all the
	 * registered output Handler objects.
	 * <p>
	 * @param   msg	The string message (or a key in the message catalog)
	 */
	public void talk(String msg){
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

	void addGUIHandlerFor(AbstractAgent agent){
		if(agent.getGUIComponent() != null && agent.getGUIComponent() instanceof AgentGUIModel){
			OutputStream os = ((AgentGUIModel) agent.getGUIComponent()).getOutputStream();
			if(os != null){
				Handler h = new StreamHandler(os, AgentLogger.agentFileFormatter){
					@Override
					public synchronized void publish(LogRecord record) {
						super.publish(record);
						flush();
					}
				};
				h.setLevel(getLevel());
				addHandler(h);
			}
		}
	}

	/**
	 * @see java.util.logging.Logger#setLevel(java.util.logging.Level)
	 */
	@Override
	public void setLevel(Level newLevel) throws SecurityException {
		super.setLevel(newLevel);//TODO if OFF set agent's logger to null
		for(Handler h : getHandlers()){
			h.setLevel(newLevel);
		}
	}

	/**
	 * @param oldLogger
	 * @param autoLogDir 
	 */
	private void initFromOldLogger(Logger oldLogger, String autoLogDir) {
		for(Handler h : oldLogger.getHandlers()){
			if(h instanceof FileHandler){
				FileHandler fh = (FileHandler) h;
				fh.flush();
				fh.close();
				File fhFile = new File(autoLogDir+oldLogger.getName());
				if (! new File(autoLogDir+getName()).exists())
					fhFile.renameTo(new File(autoLogDir+getName()));
				try {
					addHandler(Utils.createFileHandler(autoLogDir+getName(), this));
				} catch (SecurityException e) {
					e.printStackTrace();
				}
			}
			else{
				addHandler(h);
			}
		}
	}

	/**
	 * @param abstractAgent
	 * @param oldLogger
	 * @param consoleOn
	 * @param autoLogDir
	 * @param agentsLogFile
	 */
	void init(AbstractAgent abstractAgent, Logger oldLogger, boolean consoleOn, String autoLogDir, String agentsLogFile) {
		if(oldLogger != null){
			initFromOldLogger(oldLogger,autoLogDir);
		}
		else{
			setUseParentHandlers(false);
			if(consoleOn){
				addHandler(new ConsoleHandler());
			}
			if (autoLogDir != null) {
				addHandler(Utils.getFileHandler(autoLogDir+getName()));
			}			
			for(Handler h : getHandlers()){
				if (h instanceof FileHandler)
					h.setFormatter(agentFileFormatter);
				else
					h.setFormatter(agentFormatter);
			}
		}
		setLevel(getLevel());
	}

	/**
	 * @see java.util.logging.Logger#log(java.util.logging.LogRecord)
	 */
	@Override
	public void log(final LogRecord record) {
		Throwable t = record.getThrown();
		if(t != null){
			//			if(record.getLevel() == Level.WARNING && getLevel().intValue() > warningLogLevel.intValue()){
			//				return;
			//			}
			final StringWriter sw = new StringWriter();
			final PrintWriter pw = new PrintWriter(sw);
			t.printStackTrace(pw);
			pw.close();
			record.setMessage("\n ** "+sw);
		}
		super.log(record);
	}
}