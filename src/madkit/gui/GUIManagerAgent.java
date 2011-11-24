/*
 * Copyright 1997-2011 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MadKit.
 * 
 * MadKit is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * MadKit is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with MadKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.gui;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Window;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;

import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.SwingUtilities;

import madkit.agr.LocalCommunity;
import madkit.agr.LocalCommunity.Groups;
import madkit.agr.LocalCommunity.Roles;
import madkit.gui.actions.GUIManagerAction;
import madkit.gui.actions.MadkitAction;
import madkit.gui.menus.AgentsMenu;
import madkit.gui.menus.DemosMenu;
import madkit.kernel.AAAction;
import madkit.kernel.AbstractAgent;
import madkit.kernel.Agent;
import madkit.kernel.AgentAddress;
import madkit.kernel.KernelAction;
import madkit.kernel.Message;
import madkit.messages.CommandMessage;
import madkit.messages.KernelMessage;

/**
 * The GUI manager agent is responsible for setting and managing
 * agents UI which are created by the default mechanism of MadKit.
 * 
 * @author Fabien Michel
 * @since MadKit 5.0.0.6
 * @version 0.9
 * 
 */
//* By default the kernel always launch this agent. Although, this agent is
//* extremely light weight, it is possible to tell the kernel to not launch it
//* by using the {@link BooleanOption#noGUIManager} option when launching MadKit.
@SuppressWarnings("unchecked")
public class GUIManagerAgent extends Agent  {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5249615481398560277L;

	final private ConcurrentMap<AbstractAgent, JFrame> guis;
	final private List<AgentsMenu> agentsMenus;
	final private List<DemosMenu> demosMenus;
	final private Map<AbstractAgent, JInternalFrame> internalFrames;
	private boolean shuttedDown = false;
	private Desktop desktop;
	private AgentAddress kernelAddress;
	private Set<String> agentClasses;
	private Set<DemoModel> demos;
	private Set<URL> knownUrls;

//	final private static Class<AbstractAgent> supertype;
//	static{//need that because not always on initial class path
//		Class<AbstractAgent> c = null;
//		try {
//			c = (Class<AbstractAgent>) GUIManagerAgent.class.getClassLoader().loadClass("madkit.kernel.AbstractAgent");
//		} catch (ClassNotFoundException e) {
//			e.printStackTrace();//impossible
//		}
//		supertype = c;
//	}

	GUIManagerAgent(boolean asDaemon){
		super(asDaemon);
		guis = new ConcurrentHashMap<AbstractAgent, JFrame>();
		if (asDaemon) {
			internalFrames = Collections.emptyMap();
		}
		else{
			internalFrames = new ConcurrentHashMap<AbstractAgent, JInternalFrame>();
		}
		agentsMenus = new ArrayList<AgentsMenu>(20);
		demosMenus = new ArrayList<DemosMenu>(10);
		demos = new TreeSet<DemoModel>();
		agentClasses = new TreeSet<String>();
		agentClasses.add("madkit.kernel.Agent");
		knownUrls = new HashSet<URL>();
	}

	//	GUIManagerAgent(){
	//		this(true);
	//	}

	@Override
	protected void activate() {//TODO parallelize that
//		GUIToolkit.buildGlobalActions(this);
//		scanClassPathForAgentClasses();
//		setLogLevel(Level.ALL);
		kernelAddress = getAgentWithRole(LocalCommunity.NAME, Groups.SYSTEM, Roles.KERNEL);
		if(kernelAddress == null)//TODO remove that when OK
			throw new AssertionError();
		requestRole(LocalCommunity.NAME, Groups.SYSTEM, Roles.GUI_MANAGER);
		if (! isDaemon()) {//use to detect desktop mode
			desktop = new Desktop(this);
		}
	}

	@Override
	protected void live() {
		while (! shuttedDown) {
			final Message m = waitNextMessage();
			if(m instanceof GUIMessage){
				proceedCommandMessage((GUIMessage) m);
			}
			else if(m instanceof KernelMessage){
				proceedCommandMessage((KernelMessage) m);
			}
//			else if(m instanceof GUIMessage){
//				handleGUIMessage((GUIMessage) m);
//			}
			else{
				if(logger != null)
					logger.warning("I received a message that I do not understand. Discarding "+m);
			}
		}
	}
	
	protected void proceedCommandMessage(GUIMessage cm) {
		super.proceedCommandMessage(cm);
		if(cm.getCode() == GUIManagerAction.SETUP_AGENT_GUI){
			if(isAlive()){
				sendReply(cm, cm);
			}
		}
	}

	@Override
	protected void end() {
		if(logger != null)
			logger.finer("Disposing frames");
		//		final Thread t = new Thread(//not necessary just do not do interrupt
		SwingUtilities.invokeLater(
				new Runnable() {
					public void run() {
						disposeAllAgents();
						if (desktop != null) {//TODO swing thread or cleaner shutdown
							desktop.dispose();
						}
					}});
		//		t.start();
		//		try {
		//			t.join();
		//		} catch (InterruptedException e) {
		//			if(logger != null)
		//				logger.finer("interrupted by auto shutdown");
		//		}
	}

//	private void handlePrivateMessage(GUIMessage m) {
//		MadkitAction code = m.getCode();
//		switch (code) {
//		case LAUNCH_NETWORK://forward the request
//		case STOP_NETWORK://forward the request
//			sendMessage(kernelAddress, new KernelMessage(code, (Object) null));
//			break;
//		case ICONIFY_ALL:
//		case DEICONIFY_ALL:
//			iconifyAll(code == MadkitAction.ICONIFY_ALL);
//			break;
//		case CONNECT_WEB_REPO:
//			scanMadkitRepo();
//			break;
//		case LAUNCH_AGENT:
//			launchAgent((String) m.getContent(),0,true);
//			break;
//		case EXIT://forward the shutdown
//			shuttedDown = true;
//			sendMessage(kernelAddress, new KernelMessage(code, (Object) null));
//			return;
//		case LOAD_LOCAL_DEMOS:
//		case RESTART://forward the request
//			sendMessageAndWaitForReply(kernelAddress, new KernelMessage(code, (Object) null),1000);
//			if (code == MadkitAction.LOAD_LOCAL_DEMOS) {
//				scanClassPathForAgentClasses();
//			}
//			break;
//		case CLONE://forward the request
//			sendMessage(kernelAddress, new KernelMessage(MadkitAction.CLONE, m.getContent()));
//			break;
//		case KILL_AGENTS:
//			killAgents();
//			//			sendMessage(kernelAddress, new KernelMessage(code, (Object) null));
//			//			disposeAllAgents();
//			break;
//		case LOAD_JAR_FILE:
//			loadingJarFile((URL) m.getContent());
//			break;
//		case MADKIT_LAUNCH_SESSION:
//			launchDemo((String) m.getContent());
//			break;
//		default:
//			break;
//		}
//	}
	
	private void exit(){
		shuttedDown = true;
	}

//	private void handleGUIMessage(GUIMessage m) {
//		switch (m.getCode()) {
//		case AGENT_SETUP_GUI:
//			setupGUIOf(m);
//			break;
//		case AGENT_DISPOSE_GUI:
//			disposeGUIOf((AbstractAgent) m.getContent());
//			break;
//		case EXIT:
//			shuttedDown = true;
//			//		end();
//			//		sendReply(m, new Message());
//		default:
//			break;
//		}

//	}
	
	private void setupAgentGui(final AbstractAgent agent){
		if(logger != null)
			logger.fine("Setting up GUI for "+agent);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (! shuttedDown && agent.isAlive()) {
					AgentFrame f = new AgentFrame(agent, agent.getName());
					try {
						agent.setupFrame(f);//TODO catch failures because of delegation
					} catch (Exception e) {
						agent.getLogger().severeLog("Cannot initialize frame -> default GUI", e);
						f = new AgentFrame(agent, agent.getName());
					}
					if (desktop != null) {
						JInternalFrame jf = new AgentInternalFrame(f, GUIManagerAgent.this);
						desktop.addInternalFrame(jf);
						internalFrames.put(agent, jf);
						jf.setVisible(true);
					} else {
						f.setLocation(checkLocation(f));
						guis.put(agent, f);
						f.setVisible(true);
					}
				}
	}
		});
	}

	private void launchDemo(final String content) {
		if(logger != null)
			logger.fine("Launching demo "+content);
		for (DemoModel demo : demos) {
			if(demo.getName().equals(content)){
				launchDemo(demo);
			}
		}
	}

	private void launchDemo(DemoModel demo) {
		if(logger != null)
			logger.finer("Launching demo "+demo);
//		sendMessage(kernelAddress, new KernelMessage(MadkitAction.MADKIT_LAUNCH_SESSION, (Object[]) demo.getSessionArgs()));
		//		final String[] agentsClasses = demo.getLaunchAgent().split(";");
		//		for(final String classNameAndOption : agentsClasses){
		//			final String[] classAndOptions = classNameAndOption.split(",");
		//			final String className = classAndOptions[0].trim();//TODO should test if these classes exist
		//			final boolean withGUI = (classAndOptions.length > 1 ? Boolean.parseBoolean(classAndOptions[1].trim()) : false);
		//			int number = 1;
		//			if(classAndOptions.length > 2) {
		//				try {
		//					number = Integer.parseInt(classAndOptions[2].trim());
		//				} catch (NumberFormatException e) {
		//					//TODO log that
		//				}
		//			}
		//			for (int i = 0; i < number; i++) {
		//				launchAgent(className, 0, withGUI);
		//			}
		//		}
	}

//	private void loadingJarFile(URL url) {
//		Message m = sendMessageAndWaitForReply(kernelAddress, new KernelMessage(KernelAction.LOAD_JAR_FILE, url), 1000);
//		if(logger != null)
//			logger.info(url+" loaded\n\n");
//		scanClassPathForAgentClasses();
//	}
//
	private void iconifyAll(boolean iconify) {
		final int code = iconify ? JFrame.ICONIFIED : JFrame.NORMAL;
		for (final JFrame f : guis.values()){
			f.setExtendedState(code);
		}
		for (final JInternalFrame jf : internalFrames.values()) {
			try {
				jf.setIcon(iconify);
			} catch (PropertyVetoException e) {
				e.printStackTrace();
			}
		}
	}

	private void iconifyAll() {
		iconifyAll(true);
	}
	
	private void deiconifyAll(){
		iconifyAll(false);
	}

//	private void setupGUIOf(final GUIMessage m) {
//		final AbstractAgent agent = (AbstractAgent) m.getContent();
//		if(logger != null)
//			logger.fine("Setting up GUI for "+agent);
//		SwingUtilities.invokeLater(new Runnable() {
//			public void run() {
//				if (! shuttedDown && agent.isAlive()) {
//					AgentFrame f = new AgentFrame(agent, agent.getName());
//					try {
//						agent.setupFrame(f);//TODO catch failures because of delegation
//					} catch (Exception e) {
//						agent.getLogger().severeLog("Cannot initialize frame -> default GUI", e);
//						f = new AgentFrame(agent, agent.getName());
//					}
//					if (desktop != null) {
//						JInternalFrame jf = new AgentInternalFrame(f, GUIManagerAgent.this);
//						desktop.addInternalFrame(jf);
//						internalFrames.put(agent, jf);
//						jf.setVisible(true);
//					} else {
//						f.setLocation(checkLocation(f));
//						guis.put(agent, f);
//						f.setVisible(true);
//					}
//				}
//				if (isAlive()) {
//					sendReply(m, new Message());
//				}
//			}
//		});

		//TODO choose one !!
		//		try {
		//			SwingUtilities.invokeAndWait(new Runnable() {
		//				public void run() {
		//					AgentFrame f = new AgentFrame(agent, agent.getName());
		//					agent.setupFrame(f);//TODO catch failures because of delegation
		//					if (desktop != null) {
		//						JInternalFrame jf = new AgentInternalFrame(f, GUIManagerAgent.this);
		//						desktop.addInternalFrame(jf);
		//						internalFrames.put(agent, jf);
		//						jf.setVisible(true);
		//					} else {
		//						f.setLocation(checkLocation(f));
		//						guis.put(agent, f);
		//						f.setVisible(true);
		//					}
		//					sendReply(m, new Message());
		//				}
		//			});
		//		} catch (InterruptedException e) {
		//			e.printStackTrace();
		//			Thread.currentThread().interrupt();
		//		} catch (InvocationTargetException e) {
		//			e.printStackTrace();
		//		}
//	}

	private void disposeAgentGui(AbstractAgent agent) {//event dispatch thread ?
		closeFrame(guis.remove(agent));
		closeFrame(internalFrames.remove(agent));
		GUIToolkit.agentUIListeners.remove(agent);
		//making the javaws jvm quits
		if(isDaemon() && guis.isEmpty() && System.getProperty("javawebstart.version") != null)
			System.exit(0);
	}

	private void closeFrame(final Container frame){
		if(frame != null && frame.isVisible() && frame.isShowing()){
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					if (frame instanceof JFrame) {
						((Window) frame).dispose();
					} else {
						((JInternalFrame) frame).dispose();
					}
				}
			});
		}
	}

	Point checkLocation(Container c) {
		Dimension dim;
		List<Container> l; 
		if(c instanceof JInternalFrame){
			dim = desktop.getSize();
			l = new ArrayList<Container>(internalFrames.values());
		}
		else{
			dim = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
			l = new ArrayList<Container>(guis.values());
		}
		//	dim.setSize(dim.width, dim.height-25);
		Dimension size = c.getSize();
		if(size.width > dim.width)
			size.width = dim.width;
		if(size.height > dim.height)
			size.height = dim.height;
		c.setSize(size);
		dim.width-=20;
		boolean notGood = true;
		Point location = c.getLocation();
		location.x = location.x > 0 ? location.x : 0;
		location.y = location.y > 0 ? location.y : 0;
		location.x = location.x <= dim.width ? location.x : location.x % dim.width;
		location.y = location.y <= dim.height ? location.y : location.y % dim.height;
		while(notGood){
			notGood = false;
			for (Container cs : l) {
				if(location.equals(cs.getLocation())){
					notGood = true;
					location.x += 20;
					location.x %= dim.width;
					location.y += 20;
					location.y %= dim.height;
				}
			}
		}
		return location;
	}

	private void scanClassPathForAgentClasses() {
		boolean changed = false;
		for(URL dir : getMadkitClassLoader().getURLs()){
			if(knownUrls.add(dir))
				changed = true;
			else
				continue;
			if(dir.toString().contains("rsrc")){
				if(dir.toString().contains("jar:rsrc:")){//TODO externalize 
					File f = new File(getMadkitProperty("Project-Code-Name")+"-"+getMadkitProperty("Project-Version")+".jar");
					if (f.exists()) {
						try {
							dir = new URL(f.toURI().toURL().toString());
						} catch (MalformedURLException e) {
							e.printStackTrace();
						}
					}
				}
				else{//this is the "." dir : not interested !
					continue;
				}
			}
			if(logger != null)
				logger.finer("Scanning dir : "+dir);
			if (dir.toString().endsWith(".jar")) {
				try {
					JarFile jarFile = ((JarURLConnection) new URL("jar:"+dir+"!/").openConnection()).getJarFile();
					scanJarFileForLaunchConfig(jarFile);
					agentClasses.addAll(scanJarFileForAgentClasses(jarFile));
				} catch (IOException e) {
					getLogger().severeLog("web repo conf is not valid", e);
				}
			}
			else{
				agentClasses.addAll(scanFolderForAgentClasses(new File(dir.getFile()), null));
			}
		}
		if (changed) {
			updateDemosMenu();
			updateAgentsMenus();
		}
	}

	private void scanJarFileForLaunchConfig(JarFile jarFile) {
		Attributes projectInfo = null;
		try {
			projectInfo = jarFile.getManifest().getAttributes("MadKit-Project-Info");
		} catch (IOException e) {
			return;
		}
		if(projectInfo != null){
			DemoModel demo = new DemoModel(projectInfo.getValue("Project-Name").trim(),
					projectInfo.getValue("MadKit-Args").split(" "),
					projectInfo.getValue("Description").trim());
			demos.add(demo);
			if (logger != null) {
				logger.finest("found demo config info " + demo);
			}
		}
	}

	private List<String> scanJarFileForAgentClasses(JarFile jarFile) {
		List<String> l = new ArrayList<String>(50);
		for (Enumeration<JarEntry> e = jarFile.entries(); e.hasMoreElements();){
			JarEntry entry = e.nextElement();
			if (! entry.isDirectory() && entry.getName().endsWith(".class")) {
				String className = fileNameToClassName(entry.getName(), null);
				if (isAgentClass(className)) {
					l.add(fileNameToClassName(entry.getName(), null));
				}
			}
		}
		return l;
	}

	private void scanMadkitRepo() {
		URL[] urls = getMadkitClassLoader().getURLs();
//		sendMessageAndWaitForReply(kernelAddress, new KernelMessage(MadkitAction.CONNECT_WEB_REPO, (Object) null), 1000);
		if (getMadkitClassLoader().getURLs().length != urls.length) {//more than before ?
			scanClassPathForAgentClasses();
		}
	}

	private void updateAgentsMenus() {
		for (AgentsMenu menus : agentsMenus) {
			menus.update();
		}
	}

	private void updateDemosMenu() {
		for (DemosMenu menus : demosMenus) {
			menus.update();
		}
	}

	private boolean isAgentClass(final String className) {
		try {
			final Class<?> cl = getMadkitClassLoader().loadClass(className);
			if(cl != null && AbstractAgent.class.isAssignableFrom(cl) && cl.getConstructor((Class<?>[]) null) != null && (! Modifier.isAbstract(cl.getModifiers())) && Modifier.isPublic(cl.getModifiers())){
//				for (final Constructor<?> c : cl.getConstructors()) {
//					if (c.getParameterTypes().length == 0)
//						return true;
//				}
				return true;
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (NoClassDefFoundError e) {
			// TODO: the jar file is not on the MK path (IDE JUnit for instance)
			//		} catch (IllegalAccessException e) {
			//			//not public
		} catch (SecurityException e) {
		} catch (NoSuchMethodException e) {
		}
			return false;
		}

		private String fileNameToClassName(String file, String classPathRoot){
			if(classPathRoot != null)
				file = file.replace(classPathRoot, "");
			return file.substring(0, file.length()-6).replace(File.separatorChar, '.');
		}

		private List<String> scanFolderForAgentClasses(final File file, final String pckName) {
			if(logger != null)
				logger.finest("Scanning dir :"+file.getName());
			final File[] files = file.listFiles();
			if(files == null)
				return Collections.emptyList();
			final List<String> l = new ArrayList<String>();
			for(File f : files){
				if(f.isDirectory()){
					//				String pck = pckName == null ? f.getName() : pckName+"."+f.getName();
					//				if(! isKernelDirectory(pck)){
					l.addAll(scanFolderForAgentClasses(f, pckName == null ? f.getName() : pckName+"."+f.getName()));
					//				}
				}
				else if(f.getName().endsWith(".class")){
					String className = pckName+"."+f.getName().replace(".class", "");
					if (isAgentClass(className)) {
						l.add(className);
					}
				}
			}
			return l;
		}

		//	private boolean isKernelDirectory(String name) {
		//		if(name == null)
		//			return false;
		//		return name.equals("madkit.kernel") || name.equals("madkit.gui") || name.equals("madkit.messages") || name.equals("madkit.simulation");
		//	}

		Set<String> getLoadedClasses() {
			return agentClasses;
		}

		JMenu createAgentsMenu() {
			LaunchAgentsMenu am = new LaunchAgentsMenu(this);
			AgentsMenu m = new AgentsMenu(MadkitAction.LAUNCH_AGENT.getAction(this),agentClasses);
			agentsMenus.add(m);
			return am;
		}

		JMenu createDemosMenu() {
			DemosMenu m = new DemosMenu(MadkitAction.MADKIT_LAUNCH_SESSION.getAction(this),demos);
			demosMenus.add(m);
			return m;
		}

		Set<DemoModel> getDemos() {
			return demos;
		}

		/**
		 * Kills all the agents that have a GUI
		 */
		private void disposeAllAgents() {
			for (final JFrame f : guis.values()) {
				if (f.isVisible() && f.isShowing()) {
					f.dispose();
				}
			}
			for (final JInternalFrame jf : internalFrames.values()) {
				if (jf.isVisible() && jf.isShowing()) {
					jf.dispose();
				}
			}
			guis.clear();
			internalFrames.clear();
		}

		/**
		 * Kills all the agents that have a GUI
		 */
		private void killAgents() {
			for (final AbstractAgent a : guis.keySet()) {
				AgentFrame.killAgent(a, 0);//TODO should be something else
			}
			for (final AbstractAgent a : internalFrames.keySet()) {
				AgentFrame.killAgent(a, 0);
			}
		}

	}

	//class FrameLauncher implements Runnable{
	//	
	//	private final GUIManagerAgent guiManager;
	//	private AbstractAgent agent;
	//
	//	public FrameLauncher(GUIManagerAgent manager) {
	//		guiManager = manager;
	//	}
	//	
	//	public void run() {
	//	AgentFrame f = new AgentFrame(agent, agent.getName());
	//	agent.setupFrame(f);//TODO catch failures because of delegation
	//	if (manager.getDesktop() != null) {
	//		JInternalFrame jf = new AgentInternalFrame(f, manager);
	//		desktop.addInternalFrame(jf);
	//		internalFrames.put(agent, jf);
	//		jf.setVisible(true);
	//	} else {
	//		f.setLocation(checkLocation(f));
	//		guis.put(agent, f);
	//		f.setVisible(true);
	//	}
	//}