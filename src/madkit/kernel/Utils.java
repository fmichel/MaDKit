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
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import madkit.i18n.Words;

/**
 * This class is designed to help logging and i18n in MadKit
 * @author Fabien Michel
 * @since MadKit 5
 * @version 0.9
 *
 */
final class Utils {

//	final static ResourceBundle messages = ResourceBundle.getBundle(Madkit.defaultConfig.getProperty("madkit.resourceBundle.file"));

//	static String getI18N(final String message){
//		return messages.getString(message);
//	}

//	static URL getFileURLResource(String fileName){
//		URL url = GUIToolkit.class.getResource("/"+fileName);
//		//looking in the file system
//		if(url == null){
//			File f = new File(fileName);
//			if(f.exists())
//				try {
//					return f.toURI().toURL();
//				} catch (MalformedURLException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//		}
//		return url;
//	}
	
//	@SuppressWarnings("unchecked")
//	static Class<? extends AbstractAgent> getAgentClass(Object requester, String AgentClassName){
//		try {
//			return (Class<? extends AbstractAgent>) requester.getClass().getClassLoader().loadClass(AgentClassName);
//		} catch (ClassNotFoundException e) {
//			e.printStackTrace();
//			return null;
//		}
//						
//	}
	
//	static FileHandler getFileHandler(final String logFileName){
//		FileHandler fh = null;
//		try {
//			fh = new FileHandler(logFileName);
//			fh.setFormatter(new Formatter() {
//				@Override
//				public String format(LogRecord record) {
//					//TODO good format
////				DateFormat df = DateFormat.getDateInstance(DateFormat.FULL, Locale.getDefault());
////				final Date date = new Date(record.getMillis());
//					final SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss",Locale.getDefault());//TODO i18n + formatting
//					return "\n----------------------------------------------------------------------------\n-- Log session for "+logFileName.substring(logFileName.lastIndexOf(File.separator)+1)+" started on "+simpleFormat.format(new Date(record.getMillis()))+" --\n----------------------------------------------------------------------------\n\n";
//				}
//			});
//			fh.publish(new LogRecord(Level.ALL, null));
//			fh.flush();
//		} catch (SecurityException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return fh;
//	}

//	static FileHandler createFileHandler(final String logFileNameAndOptions,final Logger tmpLogger){
////		System.err.println("file is "+logFileNameAndOptions);
//		if(logFileNameAndOptions.equals("null"))
//			return null;
//		FileHandler fh = null;
//		boolean append = false;
//		String[] parameters = logFileNameAndOptions.split(";");
//		final String pattern = parameters[0];
//		try{
//			if(tmpLogger != null)
//				tmpLogger.finer("Log file is "+pattern);
//			append = Boolean.parseBoolean(parameters[1]);
//		}
//		catch(ArrayIndexOutOfBoundsException e){ }
//		if(tmpLogger != null)
//			tmpLogger.finer("append option is "+append);
//		try {
////			final boolean firstLine = //TODO if it does not exist no need for \n
//			fh = new FileHandler(pattern,append);
//			fh.setFormatter(new Formatter() {
//				@Override
//				public String format(LogRecord record) {
//					//TODO good format
////					DateFormat df = DateFormat.getDateInstance(DateFormat.FULL, Locale.getDefault());
////					final Date date = new Date(record.getMillis());
//					final SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss",Locale.getDefault());//TODO i18n + formatting
//					return "\n----------------------------------------------------------------------------\n-- Log session for "+pattern.substring(pattern.lastIndexOf(File.separator)+1)+" started on "+simpleFormat.format(new Date(record.getMillis()))+" --\n----------------------------------------------------------------------------\n\n";
//				}
//			});
//			fh.publish(new LogRecord(Level.ALL, null));
//			fh.flush();
//			fh.setFormatter(AgentLogger.agentFileFormatter);
//		} catch (SecurityException e) {
//			logWarningException(tmpLogger, e, "Permission denied !");
//		} catch (IOException e) {
//			logWarningException(tmpLogger, e, "Error accessing file system !");
//		}
//		return fh;
//	}

	static void logWarningException(final Logger logger,final  Exception e,final String message, final Level logLevelForTrace){
		logException(logger, e, message, Level.WARNING, logLevelForTrace);
	}

	static void logWarningException(final Logger logger, final Exception e,final String message){
		logException(logger, e, message, Level.WARNING, Level.FINEST);
	}

	static void logSevereException(final Logger logger, final Throwable e,final String message){
		if(logger == null){//TODO think about that
			System.err.println(message);
			e.printStackTrace();
			return;
		}
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			if (e.getCause()!=null) {
				e.getCause().printStackTrace(pw);
			}
			pw.close();
			logger.log(Level.SEVERE,message+"\n"+sw);
	}

	static void logException(final Logger logger, final Exception e,final String message,final Level logLevel, final Level logLevelForTrace){
		if(logger != null){
			if(logger.getLevel().intValue() <= logLevelForTrace.intValue()){
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				e.printStackTrace(pw);
				pw.close();
				logger.log(logLevel,message+"\n"+sw);
			}
			else{
				logger.log(logLevel, message+": "+e.toString());
			}
		}
	}

	static String getCGRString(final String community){
		return getCGRString(community,null,null);
	}
	
	static String getCGRString(final String community, final String group){
		return getCGRString(community,group,null);
	}

	static String getCGRString(final String community, final String group, final String role){
		if(role != null)
			return Words.ROLE+" <"+community+","+group+","+role+"> ";
		if(group != null)
			return Words.GROUP+" <"+community+","+group+"> ";
		return Words.COMMUNITY+" <"+community+"> ";
	}
	
//	public static void main(String[] args) {
//		System.err.println(getCGRString("aa", null, null));
//	}

}
