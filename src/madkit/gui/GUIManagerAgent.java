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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenuBar;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import madkit.action.GUIManagerAction;
import madkit.agr.LocalCommunity;
import madkit.agr.LocalCommunity.Groups;
import madkit.agr.LocalCommunity.Roles;
import madkit.gui.menus.MadkitMenu;
import madkit.gui.toolbars.MadkitToolBar;
import madkit.kernel.AbstractAgent;
import madkit.kernel.Agent;
import madkit.kernel.Message;
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
public class GUIManagerAgent extends Agent  {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5249615481398560277L;

	final private ConcurrentMap<AbstractAgent, JFrame> guis;
	//	final private List<AgentsMenu> agentsMenus;
	//	final private List<DemosMenu> demosMenus;
	//	final private Map<AbstractAgent, JInternalFrame> internalFrames;
	private boolean shuttedDown = false;
	//	private AgentAddress kernelAddress;

	private JDesktopPane desktopPane;

	private JFrame myFrame;

	GUIManagerAgent(boolean asDaemon){
		super(asDaemon);
		guis = new ConcurrentHashMap<AbstractAgent, JFrame>();
		//		if (asDaemon) {
		//			internalFrames = Collections.emptyMap();
		//		}
		//		else{
		//			internalFrames = new ConcurrentHashMap<AbstractAgent, JInternalFrame>();
		//		}
		//		agentsMenus = new ArrayList<AgentsMenu>(20);
		//		demosMenus = new ArrayList<DemosMenu>(10);
		//		demos = new TreeSet<DemoModel>();
		//		agentClasses = new TreeSet<String>();
		//		agentClasses.add("madkit.kernel.Agent");
		//		knownUrls = new HashSet<URL>();
	}

	//	GUIManagerAgent(){
	//		this(true);
	//	}

	@Override
	protected void activate() {//TODO parallelize that
		//		GUIToolkit.buildGlobalActions(this);
		//		scanClassPathForAgentClasses();
		//		setLogLevel(Level.ALL);
		//		kernelAddress = getAgentWithRole(LocalCommunity.NAME, Groups.SYSTEM, Roles.KERNEL);
		requestRole(LocalCommunity.NAME, Groups.SYSTEM, Roles.GUI_MANAGER);
		if (! isDaemon()) {//use to detect desktop mode
			//			desktop = new Desktop(this);
			buildUI();
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
		if(isAlive() && cm.getCode() == GUIManagerAction.SETUP_AGENT_GUI){
			setupAgentGui((AbstractAgent) cm.getContent()[0]);
			sendReply(cm, cm);
		}
		else{
			super.proceedCommandMessage(cm);
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
						killAgents();
						if (desktopPane != null) {//TODO swing thread or cleaner shutdown
							//							desktop.dispose();
							myFrame.dispose();
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

	@SuppressWarnings("unused")
	private void exit(){
		shuttedDown = true;
	}

	private void setupAgentGui(final AbstractAgent agent){
		if (! shuttedDown && agent.isAlive()) {
			if(logger != null)
				logger.fine("Setting up GUI for "+agent);
			AgentFrame f = new AgentFrame(agent, agent.getName());
			try {
				agent.setupFrame(f);//TODO catch failures because of delegation
			} catch (Exception e) {
				agent.getLogger().severeLog("Cannot initialize frame -> default GUI", e);
				f = new AgentFrame(agent, agent.getName());
			}
			guis.put(agent, f);
			final AgentFrame af = f;
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					if (desktopPane != null) {
						JInternalFrame jf = new AgentInternalFrame(af, GUIManagerAgent.this);
						desktopPane.add(jf);
						jf.setVisible(true);
					} else {
						af.setLocation(checkLocation(af));
						af.setVisible(true);
					}
				}
			});
		}
	}

	private void iconifyAll(boolean iconify) {
		final int code = iconify ? JFrame.ICONIFIED : JFrame.NORMAL;
		for (final JFrame f : guis.values()){
			f.setExtendedState(code);
		}
		for (JInternalFrame ijf : desktopPane.getAllFrames()) {
			try {
				ijf.setIcon(iconify);
			} catch (PropertyVetoException e) {
				e.printStackTrace();
			}
		}
	}

	@SuppressWarnings("unused")
	private void iconifyAll() {
		iconifyAll(true);
	}

	@SuppressWarnings("unused")
	private void deiconifyAll(){
		iconifyAll(false);
	}

	@SuppressWarnings("unused")
	private void disposeAgentGui(AbstractAgent agent) {//TODO event dispatch thread ?
		final JFrame f = guis.remove(agent);
		if (f != null) {
			f.dispose();
		}
		//		closeFrame(guis.remove(agent));
		//		closeFrame(internalFrames.remove(agent));
		//making the javaws jvm quits
		if(isDaemon() && guis.isEmpty() && System.getProperty("javawebstart.version") != null)
			System.exit(0);
	}

	//	private void closeFrame(final Container frame){
	//		if(frame != null && frame.isVisible() && frame.isShowing()){
	//			SwingUtilities.invokeLater(new Runnable() {
	//				public void run() {
	//					if (frame instanceof JFrame) {
	//						((Window) frame).dispose();
	//					} else {
	//						((JInternalFrame) frame).dispose();
	//					}
	//				}
	//			});
	//		}
	//	}

	Point checkLocation(Container c) {
		Dimension dim;
		List<? extends Container> l; 
		if(c instanceof JInternalFrame){
			dim = desktopPane.getSize();
			l = Arrays.asList(desktopPane.getAllFrames());
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
		location.x = location.x > 0 ? location.x : 1;
		location.y = location.y > 0 ? location.y : 1;
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

	//	private void scanMadkitRepo() {
	//		URL[] urls = getMadkitClassLoader().getURLs();
	////		sendMessageAndWaitForReply(kernelAddress, new KernelMessage(MadkitAction.CONNECT_WEB_REPO, (Object) null), 1000);
	//		if (getMadkitClassLoader().getURLs().length != urls.length) {//more than before ?
	////			scanClassPathForAgentClasses();//TODO
	//		}
	//	}

	/**
	 * Kills all the agents that have a GUI
	 */
	private void killAgents() {
		for (final JFrame f : guis.values()) {
			//			if (f.isVisible() && f.isShowing()) {
			f.dispose();
			//			}
		}
		//		if (desktopPane != null) {
		//			for (final JInternalFrame jf : desktopPane.getAllFrames()) {
		//				jf.dispose();
		//			}
		//		}
		//			for (final JInternalFrame jf : internalFrames.values()) {
		//				if (jf.isVisible() && jf.isShowing()) {
		//					jf.dispose();
		//				}
		//			}
		guis.clear();
		//			internalFrames.clear();
	}

	//		/**
	//		 * Kills all the agents that have a GUI
	//		 */
	//		private void killAgents() {
	//			for (final AbstractAgent a : guis.keySet()) {
	//				AgentFrame.killAgent(a, 0);//TODO should be something else
	//			}
	//			for (final AbstractAgent a : internalFrames.keySet()) {
	//				AgentFrame.killAgent(a, 0);
	//			}
	//		}

	private void buildUI() {
		desktopPane = new JDesktopPane();
		desktopPane.setBackground(Color.BLACK);
		myFrame = new JFrame("MadKit "+getMadkitProperty("madkit.version")+" Desktop running on kernel "+getKernelAddress());
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(new MadkitMenu(this));
		menuBar.add(new LaunchAgentsMenu(this));
		menuBar.add(new LaunchSessionsMenu(this));
		JToolBar tb = new MadkitToolBar(this);
		myFrame.setJMenuBar(menuBar);
		tb.setRollover(true);
		tb.setFloatable(false);
		myFrame.add(tb,BorderLayout.PAGE_START);
		myFrame.setPreferredSize(new Dimension(800,600));
		myFrame.setSize(new Dimension(800,600));
//		desktopPane.setPreferredSize(new Dimension(800,600));
		//			setSize(800,600);
		myFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		myFrame.add(desktopPane);
		myFrame.pack();
//		myFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		myFrame.setVisible(true);
		myFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		myFrame.setResizable(true);
	}
}