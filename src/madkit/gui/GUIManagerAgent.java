package madkit.gui;

import static madkit.kernel.Madkit.Roles.KERNEL_ROLE;
import static madkit.kernel.Madkit.Roles.LOCAL_COMMUNITY;
import static madkit.kernel.Madkit.Roles.SYSTEM_GROUP;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowListener;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
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

import javax.swing.Box;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.WindowConstants;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import madkit.kernel.AbstractAgent;
import madkit.kernel.Agent;
import madkit.kernel.AgentAddress;
import madkit.kernel.KernelMessage;
import madkit.kernel.Madkit;
import madkit.kernel.Message;

@SuppressWarnings("unchecked")
public class GUIManagerAgent extends Agent  {

	final private ConcurrentMap<AbstractAgent, JFrame> guis;
	final private List<AgentsMenu> agentsMenus;
	final private List<DemosMenu> demosMenus;
	private Map<AbstractAgent, JInternalFrame> internalFrames;
	private boolean shuttedDown = false;
	private Desktop desktop;
	private AgentAddress kernelAddress;
	private Set<String> agentClasses;
	private Set<DemoModel> demos;
	private Set<URL> knownUrls;
	final private static Class<AbstractAgent> supertype;

	static{
		Class<AbstractAgent> c = null;
		try {
			c = (Class<AbstractAgent>) GUIManagerAgent.class.getClassLoader().loadClass("madkit.kernel.AbstractAgent");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();//impossible
		}
		supertype = c;
	}

	public GUIManagerAgent(boolean asDaemon){
		super(asDaemon);
//		setLogLevel(Level.ALL);//TODO
		guis = new ConcurrentHashMap<AbstractAgent, JFrame>();
		if (asDaemon) {
			internalFrames = Collections.emptyMap();
		}
		agentsMenus = new ArrayList<AgentsMenu>(20);
		demosMenus = new ArrayList<DemosMenu>(10);
		demos = new HashSet<DemoModel>();
		agentClasses = new TreeSet<String>();
		agentClasses.add("madkit.kernel.Agent");
		knownUrls = new HashSet<URL>();
	}

	public GUIManagerAgent(){
		this(true);
	}

	@Override
	protected void activate() {
		MKToolkit.buildGlobalActions(this);
		kernelAddress = getAgentWithRole(LOCAL_COMMUNITY, SYSTEM_GROUP, KERNEL_ROLE);
		if(kernelAddress == null)//TODO remove that
			throw new AssertionError();
		requestRole(Madkit.Roles.LOCAL_COMMUNITY, Madkit.Roles.SYSTEM_GROUP, Madkit.Roles.GUI_MANAGER_ROLE);
		if (internalFrames == null) {//use to detect desktop mode
			desktop = new Desktop(this);
			internalFrames = new ConcurrentHashMap<AbstractAgent, JInternalFrame>();
		}
	}

	@Override
	protected void live() {
		scanForAgentClasses();//here so that kernel does not wait
		while (! shuttedDown) {
			Message m = waitNextMessage();
			if(m.getSender() == null){
				handlePrivateMessage((GUIMessage) m);
			}
			else if(m instanceof GUIMessage){
				handleGUIMessage((GUIMessage) m);
			}
			else{
				if(logger != null)
					logger.warning("I received a message that I do not understang. Discarding "+m);
			}
		}
	}

	@Override
	protected void end() {
		if (desktop != null) {//TODO swing thread or cleaner shutdown
			desktop.dispose();
		}
		closeAllFrames();
	}

	private void handlePrivateMessage(GUIMessage m) {
		MadkitActions code = m.getCode();
		switch (code) {
		case MADKIT_EXIT_ACTION://forward the shutdown
			sendMessage(kernelAddress, new KernelMessage(MadkitActions.MADKIT_EXIT_ACTION, (Object) null));
			break;
		case MADKIT_LAUNCH_NETWORK://forward the request
		case MADKIT_STOP_NETWORK://forward the request
			sendMessage(kernelAddress, new KernelMessage(code, (Object) null));
			break;
		case MADKIT_ICONIFY_ALL:
		case MADKIT_DEICONIFY_ALL:
			iconifyAll(code == MadkitActions.MADKIT_ICONIFY_ALL);
			break;
		case CONNECT_WEB_REPO:
			scanMadkitRepo();
			break;
		case AGENT_LAUNCH_AGENT:
			launchAgent((String) m.getContent(),0,true);
			break;
		case MADKIT_KILL_AGENTS:
			sendMessage(kernelAddress, new KernelMessage(MadkitActions.MADKIT_KILL_AGENTS, (Object) null));
			break;
		case MADKIT_LOAD_JAR_FILE:
			loadingJarFile((URL) m.getContent());
			break;
		case MADKIT_LAUNCH_DEMO:
			launchDemo((String) m.getContent());
			break;
		case MADKIT_RESTART://forward the request
			sendMessage(kernelAddress, new KernelMessage(MadkitActions.MADKIT_RESTART, (Object) null));
			break;
		case MADKIT_CLONE://forward the request
			sendMessage(kernelAddress, new KernelMessage(MadkitActions.MADKIT_CLONE, m.getContent()));
			break;
		default:
			break;
		}
	}

	private void launchDemo(String content) {
		for (DemoModel demo : demos) {
			if(demo.getName().equals(content)){
				launchDemo(demo);
			}
		}
	}

	private void launchDemo(DemoModel demo) {
		if(logger != null)
			logger.finer("Launching demo "+demo);
		final String[] agentsClasses = demo.getLaunchAgent().split(";");
		for(final String classNameAndOption : agentsClasses){
			final String[] classAndOptions = classNameAndOption.split(",");
			final String className = classAndOptions[0].trim();//TODO should test if these classes exist
			final boolean withGUI = (classAndOptions.length > 1 ? Boolean.parseBoolean(classAndOptions[1].trim()) : false);
			int number = 1;
			if(classAndOptions.length > 2) {
				try {
					number = Integer.parseInt(classAndOptions[2].trim());
				} catch (NumberFormatException e) {
					//TODO log that
				}
			}
			for (int i = 0; i < number; i++) {
				launchAgent(className, 0, withGUI);
			}
		}
	}

private void loadingJarFile(URL url) {
	sendMessageAndWaitForReply(kernelAddress, new KernelMessage(MadkitActions.MADKIT_LOAD_JAR_FILE, url), 1000);
	scanForAgentClasses();
}

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

private void closeAllFrames() {
	shuttedDown = true;
	if(logger != null)
		logger.finer("Disposing frames");
	for (final JFrame f : guis.values()) {
		f.dispose();
	}
	for (final JInternalFrame jf : internalFrames.values()) {
		jf.dispose();
	}
	guis.clear();
	internalFrames.clear();
}

private void handleGUIMessage(GUIMessage m) {
	switch (m.getCode()) {
	case AGENT_SETUP_GUI:
		if(logger != null)
			logger.fine("Setting up GUI of"+m.getContent());
		setupGUIOf((AbstractAgent) m.getContent());
		sendReply(m, new Message());
		break;
	case AGENT_DISPOSE_GUI:
		disposeGUIOf((AbstractAgent) m.getContent());
		break;
	case MADKIT_EXIT_ACTION:
		closeAllFrames();
		sendReply(m, new Message());
	default:
		break;
	}

}

private void setupGUIOf(final AbstractAgent agent) {
	JFrame f = new JFrame(agent.getName());

	f.setJMenuBar(createMenuBarFor(agent));

	f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	f.addWindowListener(new WindowAdapter() {
		public void windowClosed(java.awt.event.WindowEvent e) {
			if (agent.getState() != State.TERMINATED) {
				agent.killAgent(agent);
				//					}
			}
		}
	}); 
	f.setSize(400,300);
	f.setLocationRelativeTo(null);

	agent.setupFrame(f);//TODO catch failures because of delegation

	if (desktop != null) {
		JInternalFrame jf = frame2internalFrame(f);
		checkLocation(jf);
		desktop.addInternalFrame(jf);
		internalFrames.put(agent, jf);
		jf.setVisible(true);
	}
	else{
		checkLocation(f);
		guis.put(agent, f);
		f.setVisible(true);
	}

}

private JMenuBar createMenuBarFor(AbstractAgent agent) {
	JMenuBar menuBar = new JMenuBar();
	menuBar.add(new MadkitMenu(agent));
	menuBar.add(madkit.gui.MKToolkit.createLaunchingMenu(agent));
	menuBar.add(madkit.gui.MKToolkit.createLogLevelMenu(agent));
	menuBar.add(Box.createHorizontalGlue());
	menuBar.add(new AgentStatusPanel(agent));
	return menuBar;
}

private void disposeGUIOf(AbstractAgent agent) {
	final JFrame f = guis.remove(agent);
	if(f != null){
		MKToolkit.agentUIListeners.remove(agent);
		f.dispose();
	}
	final JInternalFrame jf = internalFrames.remove(agent);
	if(jf != null){
		MKToolkit.agentUIListeners.remove(agent);
		jf.dispose();
	}
}

protected void checkLocation(Container c) {
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
	dim.width-=20;
	boolean notGood = true;
	Point location = c.getLocation();
	location.x = location.x > 0 ? location.x : 0;
	location.y = location.y > 0 ? location.y : 0;
	location.x = location.x < dim.width ? location.x : location.x % dim.width;
	location.y = location.y < dim.height ? location.y : location.y % dim.height;
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
	c.setLocation(location);
}

@SuppressWarnings("unchecked")
void scanForAgentClasses() {
	boolean changed = false;
	for(URL dir : getMadkitClassLoader().getURLs()){
		if(knownUrls.add(dir))
			changed = true;
		else
			continue;
		if(logger != null)
			logger.finer("Scanning dir : "+dir);
		if (dir.toString().endsWith(".jar")) {
			try {
				JarFile jarFile = ((JarURLConnection) new URL("jar:"+dir+"!/").openConnection()).getJarFile();
				scanJarFileForLaunchConfig(jarFile);
				agentClasses.addAll(scanJarFileForAgentClasses(jarFile));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else{
//			File f = ((URLConnection) dir.openConnection()).getFile();
			agentClasses.addAll(scanFolderForAgentClasses(new File(dir.getFile()), null));
		}
	}
	if (changed) {
		updateMenus();
	}
}

private void scanJarFileForLaunchConfig(JarFile jarFile) {
	String[] args = null;
	Attributes projectInfo = null;
	try {
		projectInfo = jarFile.getManifest().getAttributes("MadKit-Project-Info");
	} catch (IOException e) {
		e.printStackTrace();
	}
	if(projectInfo != null){
		logger.finest("found project info"+projectInfo);
		args = projectInfo.getValue("MadKit-Args").split(" ");
		String launchAgents = null;
		for (int i = 0; i< args.length ; i++) {
			if(args[i].equals("--"+Madkit.launchAgents)){
				launchAgents = args[i+1];
				break;
			}
		}
		DemoModel demo = new DemoModel(projectInfo.getValue("Project-Name").trim(),launchAgents,projectInfo.getValue("Description").trim());
		demos.add(demo);
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
	sendMessageAndWaitForReply(kernelAddress, new KernelMessage(MadkitActions.CONNECT_WEB_REPO, (Object) null), 1000);
	if (getMadkitClassLoader().getURLs().length != urls.length) {//more than before ?
		scanForAgentClasses();
	}
}

private void updateMenus() {
	for (DemosMenu menus : demosMenus) {
		menus.update();
	}
	for (AgentsMenu menus : agentsMenus) {
		menus.update();
	}
}

private boolean isAgentClass(String className) {
	try {
		Class<?> cl = getMadkitClassLoader().loadClass(className);
		return supertype.isAssignableFrom(cl) && ! Modifier.isAbstract(cl.getModifiers()) && supertype.isAssignableFrom(cl) && cl.getConstructor((Class<?>[])null) != null;
	} catch (ClassNotFoundException e) {
		e.printStackTrace();
	} catch (SecurityException e) {
		e.printStackTrace();
	} catch (NoSuchMethodException e) {//No default constructor
	} catch (NoClassDefFoundError e) {
		// TODO: the jar file is not on the MK path (IDE JUnit for instance)
	}
	return false;
}

private String fileNameToClassName(String file, String classPathRoot){
	if(classPathRoot != null)
		file = file.replace(classPathRoot, "");
	return file.substring(0, file.length()-6).replace(File.separatorChar, '.');
}

private List<String> scanFolderForAgentClasses(File file, String pckName) {
	File[] files = file.listFiles();
	if(files == null)
		return Collections.emptyList();
	List<String> l = new ArrayList<String>();
	for(File f : files){
		if(f.isDirectory()){
			String pck = pckName == null ? f.getName() : pckName+"."+f.getName();
			if(! isKernelDirectory(pck)){
				l.addAll(scanFolderForAgentClasses(f,pck));
			}
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

private boolean isKernelDirectory(String name) {
	if(name == null)
		return false;
	return name.equals("madkit.kernel") || name.equals("madkit.gui") || name.equals("madkit.messages") || name.equals("madkit.simulation");
}

private JInternalFrame frame2internalFrame(final JFrame f){
	JInternalFrame jf = new JInternalFrame(f.getTitle(),true,true,true,true);
	jf.setSize(f.getSize());
	jf.setLocation(f.getLocation());
	jf.setContentPane(f.getContentPane());
	jf.setJMenuBar(f.getJMenuBar());
	jf.addInternalFrameListener(new InternalFrameAdapter() {

		@Override
		public void internalFrameOpened(InternalFrameEvent e) {
			// TODO Auto-generated method stub

		}

		//			@Override
		//			public void internalFrameIconified(InternalFrameEvent e) {
		//				// TODO Auto-generated method stub
		//				
		//			}
		//			
		//			@Override
		//			public void internalFrameDeiconified(InternalFrameEvent e) {
		//				// TODO Auto-generated method stub
		//				
		//			}

		@Override
		public void internalFrameDeactivated(InternalFrameEvent e) {
			// TODO Auto-generated method stub

		}

		@Override
		public void internalFrameClosing(InternalFrameEvent e) {
		}

		@Override
		public void internalFrameClosed(InternalFrameEvent e) {
			for (WindowListener wl : f.getWindowListeners()) {
				wl.windowClosed(null);
			}
		}

		@Override
		public void internalFrameActivated(InternalFrameEvent e) {
			// TODO Auto-generated method stub

		}
	});
	return jf;
}

Set<String> getLoadedClasses() {
	return agentClasses;
}

JMenu createAgentsMenu() {
	AgentsMenu m = new AgentsMenu(this);
	agentsMenus.add(m);
	return m;
}

JMenu createDemosMenu() {
	DemosMenu m = new DemosMenu(this);
	demosMenus.add(m);
	return m;
}

Set<DemoModel> getDemos() {
	return demos;
}

}
