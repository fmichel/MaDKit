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
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Policy;
import java.security.ProtectionDomain;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import madkit.i18n.ErrorMessages;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
/**
 * The brand new version of the starter class of MadKit. 
 * <p>
 * <h2>MadKit v.5 new features</h2>
 * <p>
 * <ul>
 * <li>One big change that comes with version 5 is how agents
 * are identified and localized within the artificial society.
 * An agent is no longer binded to a single agent address but 
 * has as many agent addresses as holden positions in the artificial society.
 * see {@link AgentAddress} for more information.</li>
 * <br>
 * <li>With respect to the previous change, a <code><i>withRole</i></code> version
 * of all the messaging methods has been added. 
 * See {@link AbstractAgent#sendMessageWithRole(AgentAddress, Message, String)} for an example
 * of such a method.</li>
 * <br><li>A replying mechanism has been introduced through 
 * <code><i>sendReply</i></code> methods. 
 * It enables the agent with the possibility of replying directly to a given message.
 * Also, it is now possible to get the reply to a message, or to wait for a reply 
 * ( for {@link Agent} subclasses only as they are threaded)
 * See {@link AbstractAgent#sendReply(Message, Message)}
 * for more details.</li>
 * <br><li>Agents now have a <i>formal</i> state during a MadKit session.
 * See the {@link AbstractAgent#getState()} method for detailed information.</li>
 * <br><li>One of the most convenient improvement of v.5 is the logging mechanism which is provided.
 * See the {@link AbstractAgent#logger} attribute for more details.</li>
 * <br><li>Internationalization is being made (fr_fr and en_us for now).</li>
 * <p>

 * @author Fabien Michel
 * @author Jacques Ferber
 * @since MadKit 4.0
 * @version 5.0
 */


final public class Madkit {

	final static Properties defaultConfig = new Properties();
	final static private ResourceBundle resourceBundle;
	final private Properties madkitConfig = new Properties();

	static{
		ResourceBundle rb = null;
		try {
			defaultConfig.load(Madkit.class.getResourceAsStream("madkitKernel.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		resourceBundle = rb;
	}

	private Element madkitXMLConfigFile=null;
	private FileHandler madkitLogFileHandler;
	final private MadkitKernel myKernel;
	private Logger logger;
	private MadkitClassLoader madkitClassLoader;
	String cmdLine;
	String[] args;

	MadkitClassLoader getMadkitClassLoader() {
		return madkitClassLoader;
	}

	private static Madkit currentInstance;


	static Madkit getCurrentInstance(){
		return currentInstance;
	}

	/**
	 * @return the madkitLogFileHandler
	 */
	final FileHandler getMadkitLogFileHandler() {
		return madkitLogFileHandler;
	}

	/**
	 * This main should be used to
	 * launch application booter agents using predefined options.
	 * It can be used in two ways :
	 * <p>
	 * (1) From the command line:
	 * <p>
	 * For instance, assuming that your classpath is already set correctly:
	 * <p>
	 * <tt>>java madkit.kernel.Madkit agentLogLevel INFO --launchAgents
	 * madkit.marketorg.Client,20,true;madkit.marketorg.Broker,10,true;madkit.marketorg.Provider,20,true;</tt>
	 * <p> 
	 * (2) It can be used programmatically anywhere, especially
	 * within main method of agent classes to ease their launch within an IDE.
	 * <p>
	 * Here is an example of how it can be used in this way:
	 * <p>
	 * 
	 * <pre>
	 *	public static void main(String[] args) {
	*	String[] argss = {
	*		LevelOption.agentLogLevel.toString(),"FINE",
	*		Option.launchAgents.toString(),//gets the -- launchAgents string
	*		Client.class.getName()+",true,20;"+
	*		Broker.class.getName()+",true,10;"+
	*		Provider.class.getName()+",false,20"
	*	};
	*	Madkit.main(argss);//launching the application
*	}
	 * </pre>
	 * 
	 * 	 
	 * 
	 * @param args the options which should be used to launch Madkit:
	 * see {@link LevelOption}, {@link BooleanOption} and {@link Option}
	 */
	public static void main(String[] args) {
		new Madkit(args);
	}

	Madkit(String[] argss){
		if(argss != null && argss.length == 1 && argss[0].contains(" ")){//jnlp arg in here
			argss = argss[0].split(" ");
			for (String s : argss) {
				this.cmdLine += " "+s;
			}
		}
		currentInstance = this;
		Policy.setPolicy(getAllPermissionPolicy());
		//installing config
		//		this.args = argss != null ? argss : new String[0];
		this.args = argss;
		madkitConfig.putAll(defaultConfig);
		Properties fromArgs = buildConfigFromArgs(args);
		madkitConfig.putAll(fromArgs);
		if(logger != null)
			logger.finer("command line args : "+fromArgs);
		initMadkitLogging();
		loadJarFileArguments();
		loadConfigFile();
		if(logger != null)
			logger.fine("** OVERRIDING WITH COMMAND LINE ARGUMENTS **");
		loadJarFileArguments();
		loadConfigFile();
		if(logger != null)
			logger.fine("** OVERRIDING WITH COMMAND LINE ARGUMENTS **");
		madkitConfig.putAll(fromArgs);
		//desktop on if no agents at this point
		if(madkitConfig.get(Option.launchAgents.name()).equals("null")){
			if(logger != null)
				logger.fine(Option.launchAgents.name()+" null : Activating desktop");
			BooleanOption.desktop.setProperty(madkitConfig, true);
		}
		createLogDirectory();
		myKernel = new MadkitKernel(this);
		if(logger != null)
			logger.finer("**  MADKIT KERNEL CREATED **");
		logSessionConfig(madkitConfig, Level.FINER);
		printWelcomeString();
		buildMadkitClassLoader();
		logSessionConfig(madkitConfig, Level.FINER);

		this.cmdLine = System.getProperty("java.home")+File.separatorChar+"bin"+File.separatorChar+"java -cp "+System.getProperty("java.class.path")+" madkit.kernel.Madkit ";
		//		for (String s : args) {
		//			System.err.println(s);
		//		}
		startKernel();
	}

	private Policy getAllPermissionPolicy()//TODO super bourrin, mais fait marcher le jnlp pour l'instant...
	{
		Policy policy = new Policy() {

			private PermissionCollection m_permissionCollection;

			@Override
			public PermissionCollection getPermissions(CodeSource p_codesource)
			{
				return getAllPermissionCollection();
			}

			@Override
			public PermissionCollection getPermissions(ProtectionDomain p_domain)
			{
				return getAllPermissionCollection();
			}

			/**
			 * @return an AllPermissionCollection
			 */
			private PermissionCollection getAllPermissionCollection()
			{
				if (m_permissionCollection == null)
				{
					m_permissionCollection = new AllPermission().newPermissionCollection();
					m_permissionCollection.add(new AllPermission());
				}
				return m_permissionCollection;
			}
		};
		return policy;
	}
	/**
	 * 
	 */
	private void createLogDirectory() {
		if (BooleanOption.createLogFiles.isActivated(madkitConfig)) {
			SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
			String logDir = madkitConfig.get(Option.logDirectory.name()) + File.separator+ simpleFormat.format(new Date());
			new File(logDir).mkdirs();
			if(logger != null)
				logger.fine("** CREATE LOG DIRECTORY "+logDir+" **");
			madkitConfig.put(Option.logDirectory.name(), logDir);
		}
	}

	/**
	 * 
	 */
	private void loadJarFileArguments() {
		String [] args = null;
		if(logger != null)
			logger.fine("** LOADING JAR FILE ARGUMENTS **");
		try{
			for (Enumeration<URL> urls = Madkit.class.getClassLoader().getResources("META-INF/MANIFEST.MF");urls.hasMoreElements();) {
				Manifest manifest = new Manifest(urls.nextElement().openStream());
				//				logger.info(manifest.toString());
				//				for (Map.Entry<String, Attributes> e : manifest.getEntries().entrySet()) {
				//					System.err.println("\n"+e.getValue().values());
				//				}
				Attributes projectInfo = manifest.getAttributes("MadKit-Project-Info");
				if(projectInfo != null){
					if(logger != null)
						logger.finest("found project info"+projectInfo);
					args = projectInfo.getValue("MadKit-Args").split(" ");
					Map<String,String> projectInfos = new HashMap<String, String>();
					projectInfos.put("Project-Code-Name",projectInfo.getValue("Project-Code-Name"));
					projectInfos.put("Project-Version",projectInfo.getValue("Project-Version"));
					madkitConfig.putAll(buildConfigFromArgs(args));
					madkitConfig.putAll(projectInfos);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	private void buildMadkitClassLoader() {
		if(logger != null)
			logger.finer("** BUILDING MADKIT CLASS LOADER **");
		final ClassLoader systemCL = getClass().getClassLoader();
		//		madkitClassLoader = new MadkitClassLoader(this, new URL[0], systemCL);
		if(systemCL instanceof URLClassLoader){
			madkitClassLoader = new MadkitClassLoader(this,((URLClassLoader) systemCL).getURLs(),systemCL,null);			
		}
		else{
			madkitClassLoader = new MadkitClassLoader(this, new URL[0],systemCL,null);
		}
		if(logger != null){
			logger.finer("ClassPath is:\n"+madkitClassLoader);
			logger.fine("** MADKIT CLASS LOADER INITIALIZED **");
		}
	}

	private void initMadkitLogging() {
		Level l = LevelOption.madkitLogLevel.getValue(madkitConfig);
		if(l != Level.OFF){
			logger = Logger.getLogger("[*MADKIT*]");
			logger.setUseParentHandlers(false);
			logger.setLevel(l);
			ConsoleHandler cs = new ConsoleHandler();
			cs.setLevel(logger.getLevel());
			cs.setFormatter(AgentLogger.agentFormatter);
			logger.addHandler(cs);
			logger.fine("** LOGGING INITIALIZED **");
		}
	}

	private void loadConfigFile() {
		final String fileName = madkitConfig.getProperty(Option.configFile.name());
		if(fileName.equals("null")){
			return;
		}
		if(logger != null)
			logger.fine("** Loading config file "+fileName+" **");
		InputStream url = getClass().getClassLoader().getResourceAsStream(fileName);
		if(url != null){
			try {
				madkitXMLConfigFile = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(url).getDocumentElement();
				NodeList madkitOptionNodes = madkitXMLConfigFile.getElementsByTagName("madkitOptions");
				for (int i = 0; i < madkitOptionNodes.getLength(); i++) {
					org.w3c.dom.NamedNodeMap options = madkitOptionNodes.item(i).getAttributes();
					for (int j = 0; j < options.getLength(); j++) {
						madkitConfig.put(options.item(j).getNodeName(),options.item(j).getNodeValue());					
					}
				}
				if(logger != null)
					logger.fine("** Config file "+fileName+" successfully loaded **\n");
			} catch (SAXException e) {
				if(logger != null)
					logger.log(Level.WARNING,ErrorMessages.CANT_LOAD+" configuration "+fileName,e);
			} catch (IOException e) {
				if(logger != null)
					logger.log(Level.WARNING,ErrorMessages.CANT_LOAD+" configuration "+fileName,e);
			} catch (ParserConfigurationException e) {
				if(logger != null)
					logger.log(Level.WARNING,ErrorMessages.CANT_LOAD+" configuration "+fileName,e);
			}
		}
		else if(logger != null){
			logger.warning(ErrorMessages.CANT_FIND+" configuration "+fileName);
		}
	}

	/**
	 * 
	 */
	private void startKernel() {
		//starting the kernel agent and waiting the end of its activation
		if(logger != null)
			logger.fine("** LAUNCHING KERNEL AGENT **");
		myKernel.launchAgent(myKernel, myKernel, Integer.MAX_VALUE, false);
	}

	//	private void prepareConfigAgents(){
	//		logger.fine("** INITIALIAZING CONFIG AGENTS **");
	//		final String agentsTolaunch =madkitConfig.getProperty(Madkit.launchAgents);
	//		if(! agentsTolaunch.equals("null")){
	//			final String[] agentsClasses = agentsTolaunch.split(";");
	//			for(final String classNameAndOption : agentsClasses){
	//				final String[] classAndOptions = classNameAndOption.split(",");
	//				final String className = classAndOptions[0].trim();//TODO should test if these classes exist
	//				final boolean withGUI = (classAndOptions.length > 1 ? Boolean.parseBoolean(classAndOptions[1].trim()) : false);
	//				int number = 1;
	//				if(classAndOptions.length > 2) {
	//					number = Integer.parseInt(classAndOptions[2].trim());
	//				}
	//				logger.finer("Launching "+number+ " instance(s) of "+className+" with GUI = "+withGUI);
	//				for (int i = 0; i < number; i++) {
	//					myKernel.receiveMessage(new KernelMessage(MadkitAction.AGENT_LAUNCH_AGENT, className, withGUI));
	//				}
	//			}
	//		}
	//	}

	/**
	 * 
	 */
	private void printWelcomeString() {
		if(!(LevelOption.madkitLogLevel.getValue(madkitConfig) == Level.OFF)){
			System.err.println("\n\t-----------------------------------------------------");
			System.err.println("\n\t\t\t   MadKit");
			System.err.println("\n\t\t   version: "+defaultConfig.getProperty("madkit.version")+"\n\t\t  build: "+defaultConfig.getProperty("build.id"));
			System.err.println("\n\t\tby MadKit Team (c) 1997-"+Calendar.getInstance().get(Calendar.YEAR));
			System.err.println("\n\t-----------------------------------------------------\n");			
		}
	}

	String printFareWellString() {
		if(!(LevelOption.madkitLogLevel.getValue(madkitConfig) == Level.OFF)){
			//				|| Level.parse(madkitConfig.getProperty(Madkit.MadkitLogLevel)).equals(Level.OFF))){
			return("\n\t-----------------------------------------------------")+
					("\n\t   MadKit Kernel "+myKernel.getKernelAddress()+" is shutting down, Bye !")+
					("\n\t-----------------------------------------------------\n");			
		}
		return "";
	}

	//	private void misuseOptionMessage(String option,String value) {
	//		System.err.println("\n\n-------------MadKit WARNING----------------------------\n" +
	//				"Misuse of --"+option+" option\nincorrect value : "+value+
	//		"\n------------------------------------------------------\n");
	//	}

	//	/**
	//	 * @param agentsLogFile the agentsLogFile to set
	//	 */
	//	private final void setAgentsLogFile(FileHandler agentsLogFile) {
	//		this.aaLogFile = agentsLogFile;
	//	}

	//	private boolean isOptionWithDifferentValue(String optionName,String option, String value) {
	//		return option.equals(optionName) && ! madkitConfig.getProperty(optionName).equals(value);
	//	}

	void logSessionConfig(Properties session, Level lvl){
		if(logger != null){
			String message = "MadKit current configuration is\n\n";
			message+="\t--- MadKit regular options ---\n";
			for (String option : defaultConfig.stringPropertyNames()) {
				message+="\t"+String.format("%-" + 30 + "s", option)+session.getProperty(option)+"\n";					
			}
			Set<Object> tmp = new HashSet<Object>(session.keySet());
			tmp.removeAll(defaultConfig.keySet());
			if(tmp.size()>0){
				message+="\n\t--- Additional non MadKit options ---\n";
				for(Object o : tmp)
					message+="\t"+String.format("%-" + 25 + "s", o)+session.get(o)+"\n";
			}
			logger.log(lvl, message);
		}
	}

	Properties buildConfigFromArgs(String[] args) {
		Properties currentMap = new Properties();
		if (args != null) {
			String parameters = "";
			String currentOption = null;
			for (int i = 0; i < args.length; i++) {
				if (args[i].startsWith("--")) {
					currentOption = args[i].substring(2);
					currentMap.put(currentOption, "true");
					if (logger != null)
						logger.finest("found option -- " + currentOption);
					parameters = "";
				} else {
					parameters += args[i] + " ";
					if (i + 1 == args.length || args[i + 1].startsWith("--")) {
						currentMap.put(currentOption, parameters.trim());//TODO bug on "-" use
						if (logger != null)
							logger.finest("found option -- " + currentOption + " -- value -- " + parameters.trim());
					}
				}
			}
			if (logger != null)
				logger.finest("build temp map is " + currentMap);
		}
		return currentMap;
	}


	Logger getLogger() {
		return logger;
	}

	Properties getConfigOption() {
		return madkitConfig;
	}

	//	Logger initLogger(String loggerName, Level lvl, boolean consoleOn, List<FileHandler> fhs, Formatter formatter) {
	//		Logger newLogger = Logger.getLogger(loggerName);
	//		Logger tmpLogger = logger;
	//		if(logger == newLogger){// in case this is the MK logger
	//			tmpLogger = Logger.getLogger("[TMP]",defaultConfig.getProperty("madkit.resourceBundle.file"));
	//			tmpLogger.setUseParentHandlers(false);
	//			tmpLogger.setLevel(logger.getLevel());
	//			for(Handler h : tmpLogger.getHandlers()){
	//				tmpLogger.removeHandler(h);
	//			}
	//			for(Handler h : logger.getHandlers()){
	//				tmpLogger.addHandler(h);
	//			}
	//		}
	//		if(tmpLogger != null){
	//			tmpLogger.finest("Removing all previous handlers of "+newLogger.getName());
	//			tmpLogger.finest(newLogger.getName()+" log level changed from "+newLogger.getLevel()+" to "+lvl);
	//		}
	//		for(Handler h : newLogger.getHandlers())
	//			newLogger.removeHandler(h);
	//		newLogger.setLevel(lvl);
	//		newLogger.setUseParentHandlers(false);
	//		if(consoleOn){
	//			newLogger.addHandler(new ConsoleHandler());
	//			if(tmpLogger != null){
	//				tmpLogger.finest("Console handling is on");
	//			}
	//		}
	//		for (FileHandler fh : fhs) {
	//			if (fh != null) {
	//				if (logger != null)
	//					tmpLogger.finest("Creating a log file for logger "+ newLogger.getName());
	//				fh.setLevel(newLogger.getLevel());
	//				newLogger.addHandler(fh);
	//			}
	//		}
	//		for(Handler h : newLogger.getHandlers()){
	//			h.setLevel(newLogger.getLevel());
	//			if(formatter != null)
	//				h.setFormatter(formatter);
	//		}
	//		if(newLogger.getHandlers().length == 0){
	//			newLogger = null;
	//		}
	//		return newLogger;
	//	}

	//	/**
	//	 * @return the agentsLogFile
	//	 */
	//	FileHandler getAgentsLogFile() {
	//		return aaLogFile;
	//	}

	//	/**
	//	 * @param requester 
	//	 * @param agentClassName
	//	 * @return
	//	 * @throws ClassNotFoundException 
	//	 */
	//	@SuppressWarnings("unchecked")
	//	Class<?> loadClass(AbstractAgent requester, String agentClassName){
	//			return madkitClassLoader.loadClass(agentClassName);
	//	}

	/**
	 * @param className
	 * @return
	 * @throws ClassNotFoundException 
	 */
	//	boolean reloadClass(String name) {
	//		if (name != null) {
	//			return madkitClassLoader.reloadClass(name);
	//		}
	//		return false;
	//	}

	//	boolean checkAndValidateOption(String option, String value){
	//		return checkAndValidateOption(madkitConfig, option, value);
	//	}
	//	/**
	//	 * @param option
	//	 * @param value
	//	 * @return true if something has been changed, false otherwise
	//	 */
	//	@SuppressWarnings("unchecked")
	//	//TODO use enum for option
	//	boolean checkAndValidateOption(Properties session, String option, String value){//TODO check all the options + update on what has to be !!
	//		if(option == null || value == null)
	//			return false; //TODO log error
	//		if(! defaultConfig.containsKey(option)){
	//			if(logger != null){
	//				logger.finer("Adding a non MadKit option: "+option+", value is "+value);
	//			}
	//			session.put(option,value);
	//			return true;
	//		}
	//		if(isOptionWithDifferentValue(agentLogLevel, option, value) || 
	//				isOptionWithDifferentValue(MadkitLogLevel, option, value) || 
	//				isOptionWithDifferentValue(platformLogLevel, option, value) || 
	//				isOptionWithDifferentValue(kernelLogLevel, option, value) || 
	//				isOptionWithDifferentValue(guiLogLevel, option, value) || 
	//				isOptionWithDifferentValue(kernelLogLevel, option, value) || 
	//				isOptionWithDifferentValue(orgLogLevel, option, value) || 
	//				isOptionWithDifferentValue(warningLogLevel, option, value)){
	//			try {
	//				final Level l = Level.parse(value);
	//				session.put(option, l.getName());
	//				return true;
	//			} catch (IllegalArgumentException e1) {
	//				misuseOptionMessage(option,value);
	//			}
	//		}
	//		if(option.equals(Madkit.configFile)){
	//			if(! (value.equals("true") || value.equals("null") || value.equals(""))){
	//				session.put(option, value);
	//				return true;
	//			}
	//		}
	//		if(option.equals(Madkit.launchAgents)){
	//			if(! (value.equals("true") || value.equals("null") || value.equals(""))){
	//				//				if (launchAgentsOptionValidate(value)) {
	//				session.put(option, value);
	//				return true;
	//				//				}
	//			}
	//		}
	//		if(option.equals(Madkit.booterAgentKey)){
	//			if(value.equals("true")){
	//				if(logger != null){
	//					logger.warning("Missing argument for option --"+Madkit.booterAgentKey+ ": Using default agent booter");
	//				}
	//				return false;
	//			}
	//			try {
	//				@SuppressWarnings("unused")
	//				//the following line is only a check !
	//				final Class<? extends AbstractAgent> booterAgent = (Class<? extends AbstractAgent>) madkitClassLoader.loadClass(value);
	//				session.put(option, value);
	//				return true;
	//			} catch (ClassNotFoundException e) {
	//				logSevereException(logger,e, "Cannot find the booter agent class -- "+value+" -- in the classpath");
	//			} catch (ClassCastException e) {
	//				logSevereException(logger,e, value+" is not an AbstractAgent !!");
	//			}
	//			if(logger != null){
	//				logger.warning("Using default agent booter");
	//			}
	//		}
	//		if(isOptionWithDifferentValue(Madkit.agentsLogFile, option, value)){
	//			session.put(option, value);
	//			//			setAgentsLogFile(createFileHandler(value,logger));
	//			return true;
	//		}
	//		if(isOptionWithDifferentValue(Madkit.MadkitLogFile, option, value)){
	//			session.put(option, value);
	//			//			setMadkitLogFileHandler(createFileHandler(value, logger));
	//			return true;
	//		}
	//		if(isOptionWithDifferentValue(Madkit.logDirectory, option, value)){
	//			if(! value.endsWith(File.separator)){
	//				value += File.separator;
	//			}
	//			session.put(option, value);
	//			return true;
	//		}
	//		//parse boolean options with no immediate updates
	//		if(isOptionWithDifferentValue(
	//				Madkit.createLogFiles, option, value)
	//				|| isOptionWithDifferentValue(Madkit.noAgentConsoleLog, option, value)
	//				|| isOptionWithDifferentValue(Madkit.desktop, option, value)
	//				|| isOptionWithDifferentValue(Madkit.autoConnectMadkitWebsite, option, value)
	//				|| isOptionWithDifferentValue(Madkit.loadLocalDemos, option, value)
	//				|| isOptionWithDifferentValue(Madkit.network, option, value)){
	//			value = value.trim().toLowerCase();
	//			if((value.equals("true") || value.equals("false"))){
	//				session.put(option, value);
	//				return true;
	//			}
	//		}
	//		if(isOptionWithDifferentValue(Madkit.noMadkitConsoleLog, option, value)){ //TODO tolowercase always
	//			value = value.trim().toLowerCase();
	//			if((value.equals("true") || value.equals("false"))){
	//				session.put(option, value);
	//				return true;
	//			}
	//		}
	//		return false;
	//	}

	//	/**
	//	 * @param value
	//	 * @return
	//	 */
	//	private boolean launchAgentsOptionValidate(String agentsTolaunch) {
	//		if(! agentsTolaunch.equals("null")){
	//			final String[] agentsClasses = agentsTolaunch.split(";");
	//			for(final String classNameAndOption : agentsClasses){
	//				final String[] classAndOptions = classNameAndOption.split(",");
	//				final String className = classAndOptions[0].trim();//TODO should test if these classes exist
	//				try {
	//					Class<? extends AbstractAgent> c = (Class<? extends AbstractAgent>) getMadkitClassLoader().loadClass(className);
	//					if(classAndOptions.length > 2) {
	//						Integer.parseInt(classAndOptions[2].trim());
	//					}
	//				} catch (ClassNotFoundException e1) {
	//					return false;
	//				} catch (ClassCastException e) {
	//					return false;
	//				} catch (NumberFormatException e) {
	//					return false;
	//				}
	//			}
	//		}
	//		return true;
	//	}


	//	final void kernelLog(String message, Level logLvl, Throwable e) {
	//		logger.log(logLvl, message, e);
	//		if (e != null) {
	//			e.printStackTrace();
	//		}
	//	}

	/**
	 * @return
	 */
	MadkitKernel getKernel() {
		return myKernel;
	}

	/**
	 * @param madkitClassLoader the madkitClassLoader to set
	 */
	final void setMadkitClassLoader(MadkitClassLoader madkitClassLoader) {
		this.madkitClassLoader = madkitClassLoader;
	}

	/**
	 * @return the resourceBundle
	 */
	static ResourceBundle getResourceBundle() {
		return resourceBundle;
	}

	boolean isOptionActivated(String madkitOptionName) {
		return Boolean.parseBoolean(madkitConfig.getProperty(madkitOptionName));
	}

	/**
	 * Option used to activate or disable features on startup.
	 * These options can be used 
	 * from the command line or using the main method of MadKit.
	 * 
	 * <pre>SYNOPSIS</pre>
	 * <code><b>--optionName</b></code> [true|false]
	 * <pre>DESCRIPTION</pre>
	 * If no boolean value is specified, 
	 * the option is considered as set to true.
	 * <pre>EXAMPLES</pre>
	 * <ul>
	 * <li>--optionName false</li> 
	 * <li>--optionName (equivalent to)</li>
	 * <li>--optionName true</li>
	 */
	public static enum BooleanOption implements MadkitOption{
		/**
		 * Starts the desktop mode.
		 * Default value is "false". 
		 */
		desktop,
		/**
		 * Connect to the MadKit repository on startup.
		 * Default value is "false". 
		 */
		autoConnectMadkitWebsite,
		/**
		 * Starts the network on startup.
		 * Default value is "false". 
		 */
		network,
		/**
		 * If activated, MadKit will create a log file for every agent which has 
		 * a log level greater than {@link Level#OFF}.
		 * Default value is "false". 
		 * @see Madkit.Option#logDirectory
		 */
		createLogFiles,
		/**
		 * not functional yet
		 */
		noGUIManager,
		/**
		 * Defines if agent logging should be quiet in the
		 * default console.
		 * Default value is "false". 
		 */
		noAgentConsoleLog,
		/**
		 * Loads all the jar files which are in the demos directory on startup.
		 * Default value is "false". 
		 */
		loadLocalDemos;
		public boolean isActivated(Properties session){
			return Boolean.parseBoolean(session.getProperty(this.name()));
		}

		public void setProperty(Properties session, Boolean activated){
			session.setProperty(name(), activated.toString());
		}

		@Override
		public String toString() {
			return "--"+name();
		}

	}

	/**
	 * MadKit options which are valued with a string representing parameters.
	 * These options could be used from the command line or using the main method of MadKit.
	 * 
	 * @author Fabien Michel
	 * @since MadKit 5.0.0.10
	 * @version 0.9
	 * 
	 */
	public static enum Option implements MadkitOption{
		/**
		 * Used to launch agents at start up.
		 * This option can be used 
		 * from the command line or using the main method of MadKit.
		 * 
		 * <pre>SYNOPSIS</pre>
		 * <code><b>--launchAgents</b></code> AGENT_CLASS_NAME[,GUI][,NB][;OTHERS]
		 * <p>
		 * <ul>
		 * <li><i>AGENT_CLASS_NAME</i>: the agent class to launch</li> 
		 * <li><i>GUI</i> (boolean optional): with a default GUI if <code>true</code></li>
		 * <li><i>NB</i> (integer optional): number of desired instances</li>
		 * </ul>
		 *  
		 * <pre>DESCRIPTION</pre>
		 * The optional parameters could be used to (1) launch several different types
		 * of agents, (2) launch the agents with a default GUI and/or (3) 
		 * specify the number of desired instances of each type.
		 * <pre>DEFAULT VALUE</pre>
		 * Default value is <i>"null"</i>, meaning that no agent has to be launched.
		 * <p>Default values for the optional parameters are
		 * <ul>
		 * <li><i>GUI</i> : <code>false</code></li>
		 * <li><i>NB</i> : 1</li> 
		 * </ul>
		 * 
		 * <pre>EXAMPLES</pre>
		 * <ul>
		 * <li> --launchAgents myPackage.MyAgent</li> 
		 * <li> --launchAgents myPackage.MyAgent,true</li>
		 * <li> --launchAgents myPackage.MyAgent,false,3</li> 
		 * <li> --launchAgents myPackage.MyAgent;other.OtherAgent</li> 
		 * <li> --launchAgents myPackage.MyAgent,true;other.OtherAgent,true</li>
		 * <li> --launchAgents myPackage.MyAgent;other.OtherAgent,true,3;madkit.kernel.Agent</li>
		 * </ul>
		 */
		launchAgents,
		/**
		 * Used to specify the directory wherein the logs should be done 
		 * when the {@link BooleanOption#createLogFiles} is activated.
		 * 
		 * <pre>SYNOPSIS</pre>
		 * <code><b>--logDirectory</b></code> DIRECTORY_NAME
		 * <pre>DESCRIPTION</pre>
		 * Specify the desired directory. It could be an absolute 
		 * or a relative path. At runtime, a log directory named with
		 * the current date (second precision) will be created in the log directory for each MadKit session. 
		 * E.g. /home/neo/madkit_5/logs/2011.06.23.16.23.53
		 * <pre>DEFAULT VALUE</pre>
		 * Default value is <i>"logs"</i>, so that a directory named
		 * "logs" will be created in the application working directory.
		 * 
		 * <pre>EXAMPLES</pre>
		 * <ul>
		 * <li> --logDirectory bin</li> 
		 * <li> --logDirectory /home/neo/madkit_logs</li> 
		 * </ul>
		 * @see BooleanOption#createLogFiles
		 */
		logDirectory,

		/**
		 * TODO
		 */
		configFile;

		@Override
		public String toString() {
			return "--"+name();
		}

	}

	/**
	 * MadKit options valued with a string representing a {@link Level} value.
	 * These options could be used from the command line or using the main method of MadKit.
	 * 
	 * @author Fabien Michel
	 * @since MadKit 5.0.0.10
	 * @version 0.9
	 * 
	 */
	public static enum LevelOption implements MadkitOption{
		/**
		 * Option defining the default agent log level for newly
		 * launched agents. 
		 * Default value is "INFO". This value could be overridden
		 * individually by agents using {@link AbstractAgent#setLogLevel(Level)}. 
		 * <p>Example:
		 * <ul>
		 * <li> --agentLogLevel OFF</li> 
		 * <li> --agentLogLevel ALL</li> 
		 * <li> --agentLogLevel FINE</li> 
		 * </ul>
		 * @see AbstractAgent#logger
		 * @see java.util.logging.Logger
		 * @see AbstractAgent#getMadkitProperty(String)
		 * @see AbstractAgent#setMadkitProperty(String, String)
		 */
		agentLogLevel,
		/**
		 * Only useful for kernel developers
		 */
		kernelLogLevel,
		/**
		 * Only useful for kernel developers
		 */
		guiLogLevel,
		/**
		 * Can be used to make MadKit quiet
		 */
		madkitLogLevel,
		/**
		 * Option defining the default warning log level for newly
		 * launched agents.
		 * Default value is "FINE". This value could be changed
		 * individually by the agents using {@link AgentLogger#setWarningLogLevel(Level)} 
		 * on their personal logger. 
		 * <p>Example:
		 * <ul>
		 * <li> --warningLogLevel OFF</li> 
		 * <li> --warningLogLevel ALL</li> 
		 * <li> --warningLogLevel FINE</li> 
		 * </ul>
		 * @see AbstractAgent#logger
		 * @see java.util.logging.Logger
		 * @see AbstractAgent#getMadkitProperty(String)
		 * @see AbstractAgent#setMadkitProperty(String, String)
		 * @since MadKit 5 
		 */
		warningLogLevel,
		networkLogLevel;

		Level getValue(Properties session){
			try {
				return Level.parse(session.getProperty(name()));
			} catch (IllegalArgumentException e) {
				currentInstance.myKernel.getLogger().severeLog(ErrorMessages.OPTION_MISUSED.toString()+" "+name()+" : "+session.getProperty(name()), e);
			}
			return Level.ALL;
		}

		@Override
		public String toString() {
			return "--"+name();
		}

		void setProperty(Properties session, Level lvl){
			session.put(name(), lvl.toString());
		}
	}

}
