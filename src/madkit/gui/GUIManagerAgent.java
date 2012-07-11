/*
 * Copyright 1997-2012 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MaDKit.
 * 
 * MaDKit is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * MaDKit is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MaDKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Point;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.swing.ImageIcon;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenuBar;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import madkit.action.ActionInfo;
import madkit.action.GUIManagerAction;
import madkit.agr.LocalCommunity;
import madkit.agr.LocalCommunity.Groups;
import madkit.gui.menu.HelpMenu;
import madkit.gui.menu.LaunchAgentsMenu;
import madkit.gui.menu.LaunchMAS;
import madkit.gui.menu.MadkitMenu;
import madkit.gui.toolbar.MadkitToolBar;
import madkit.kernel.AbstractAgent;
import madkit.kernel.Agent;
import madkit.kernel.Madkit.BooleanOption;
import madkit.kernel.Message;
import madkit.message.GUIMessage;
import madkit.message.KernelMessage;

/**
 * The GUI manager agent is responsible for setting and managing
 * agents UI which are created by the default mechanism of MaDKit.
 * 
 * @author Fabien Michel
 * @since MaDKit 5.0.0.6
 * @version 0.9
 * 
 */
// * By default the kernel always launch this agent. Although, this agent is
// * extremely light weight, it is possible to tell the kernel to not launch it
// * by using the {@link BooleanOption#noGUIManager} option when launching MaDKit.
class GUIManagerAgent extends Agent {

	private static final long									serialVersionUID	= 8026421822077510523L;
	final private ConcurrentMap<AbstractAgent, JFrame>	guis;
	private boolean												shuttedDown			= false;

	private JDesktopPane											desktopPane;

	private JFrame													myFrame;

	GUIManagerAgent(boolean asDaemon) { // NO_UCD use by reflection
		super(asDaemon);
		guis = new ConcurrentHashMap<AbstractAgent, JFrame>();
	}

	@Override
	protected void activate() {// TODO parallelize that
	// setLogLevel(Level.ALL);
	// requestRole(LocalCommunity.NAME, Groups.SYSTEM, Roles.GUI_MANAGER);//no need: I am a manager
		if (!isDaemon()) {// use to detect desktop mode
			try {
				buildUI();
				if(ActionInfo.javawsIsOn)
					setMadkitProperty(BooleanOption.autoConnectMadkitWebsite.name(), "true");
			} catch (HeadlessException e) {
				headlessLog(e);
				return;
			}
		}
		createGroup(LocalCommunity.NAME, Groups.GUI);
	}

	/**
	 * @param e
	 */
	private void headlessLog(HeadlessException e) {
		getLogger().severe(
				"\t" + e.getMessage() + "\n\tNo graphic environment, quitting");
		shuttedDown = true;
	}

	@Override
	protected void live() {
		while (!shuttedDown) {
			final Message m = waitNextMessage();
			if (m instanceof GUIMessage) {
				proceedCommandMessage((GUIMessage) m);
			}
			else
				if (m instanceof KernelMessage) {
					proceedEnumMessage((KernelMessage) m);
				}
				else
					if (logger != null)
						logger.warning("I received a message that I do not understand. Discarding "
								+ m);
		}
	}

	private void proceedCommandMessage(GUIMessage cm) {
		if (isAlive()) {
			if (cm.getCode() == GUIManagerAction.SETUP_AGENT_GUI) {// because it needs a reply
				try {
					setupAgentGui((AbstractAgent) cm.getContent()[0]);
					sendReply(cm, cm);
				} catch (HeadlessException e) {
					headlessLog(e);
				}
			}
			else {
				super.proceedEnumMessage(cm);
			}
		}
	}

	@Override
	protected void end() {
		if (logger != null)
			logger.finer("Ending: Disposing frames");
		// SwingUtilities.invokeLater(
		// new Runnable() {
		// public void run() {
		killAgents(); // no need because it closes internal frames too
		if (desktopPane != null) {// TODO swing thread or cleaner shutdown
			myFrame.dispose();
		}
		// }});
	}

	@SuppressWarnings("unused")
	private void exit() {
		shuttedDown = true;
	}

	private void setupAgentGui(final AbstractAgent agent) {
		if (!shuttedDown && agent.isAlive()) {
			if (logger != null)
				logger.fine("Setting up GUI for " + agent);
			AgentFrame f = new AgentFrame(agent, agent.getName());
			try{
				agent.setupFrame(f);// TODO catch failures because of delegation
			} catch (Exception e) {
				agent.getLogger().severeLog(
						"Frame setup problem -> default GUI settings", e);
				f = new AgentFrame(agent, agent.getName());
			}
			guis.put(agent, f);
			final AgentFrame af = f;
			SwingUtilities.invokeLater(new Runnable() {

				public void run() {
					if (desktopPane != null) {
						// JInternalFrame jf = new AgentInternalFrame(af, GUIManagerAgent.this);
						final JInternalFrame jf = buildInternalFrame(af);
						desktopPane.add(jf);
						jf.setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE);
						jf.addInternalFrameListener(new InternalFrameAdapter() {

							@Override
							public void internalFrameClosing(InternalFrameEvent e) {
								if (agent.isAlive()) {
									jf.setTitle("Closing " + agent.getName());
									AgentFrame.killAgent(agent, 2);
								}
								else {
									jf.dispose();
								}
							}
						});
						jf.setLocation(checkLocation(jf));
						jf.setVisible(true);
					}
					else {
						af.setLocation(checkLocation(af));
						af.setVisible(true);
					}
				}
			});
		}
	}

	private JInternalFrame buildInternalFrame(final AgentFrame af) {
		final JInternalFrame ijf = new JInternalFrame(af.getTitle(), true, true,
				true, true);
		ijf.setFrameIcon(new ImageIcon(af.getIconImage().getScaledInstance(14,
				14, java.awt.Image.SCALE_SMOOTH)));
		ijf.setSize(af.getSize());
		ijf.setLocation(af.getLocation());
		ijf.setContentPane(af.getContentPane());
		ijf.setJMenuBar(af.getJMenuBar());
		af.setInternalFrame(ijf);
		return ijf;
	}

	private void iconifyAll(boolean iconify) {
		final int code = iconify ? JFrame.ICONIFIED : JFrame.NORMAL;
		for (final JFrame f : guis.values()) {
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
	private void deiconifyAll() {
		iconifyAll(false);
	}

	@SuppressWarnings("unused")
	private void disposeAgentGui(AbstractAgent agent) {// TODO event dispatch thread ?
		final JFrame f = guis.remove(agent);
		if (f != null) {
			f.dispose();
		}
		// making the javaws jvm quits //TODO
		if (isDaemon() && guis.isEmpty()	&& ActionInfo.javawsIsOn)
			System.exit(0);
	}

	private Point checkLocation(Container c) {
		Dimension dim;
		List<? extends Container> l;
		if (c instanceof JInternalFrame) {
			dim = desktopPane.getSize();
			l = Arrays.asList(desktopPane.getAllFrames());
		}
		else {
			dim = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
			l = new ArrayList<Container>(guis.values());
		}
		// dim.setSize(dim.width, dim.height-25);
		Dimension size = c.getSize();
		if (size.width > dim.width)
			size.width = dim.width;
		if (size.height > dim.height)
			size.height = dim.height;
		c.setSize(size);
		dim.width -= 20;
		boolean notGood = true;
		Point location = c.getLocation();
		location.x = location.x > 0 ? location.x : 1;
		location.y = location.y > 0 ? location.y : 1;
		location.x = location.x <= dim.width ? location.x : location.x
				% dim.width;
		location.y = location.y <= dim.height ? location.y : location.y
				% dim.height;
		while (notGood) {
			notGood = false;
			for (Container cs : l) {
				if (cs != c && location.equals(cs.getLocation())) {
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

	/**
	 * Kills all the agents that have a GUI
	 */
	private void killAgents() {
		for (final JFrame f : guis.values()) {
			f.dispose();
		}
		guis.clear();
	}

	private void buildUI() {
		myFrame = new JFrame("MaDKit " + getMadkitProperty("madkit.version")
				+ " Desktop running on kernel " + getKernelAddress());
		desktopPane = new JDesktopPane()
		// {
		// @Override
		// protected void paintComponent(Graphics g) {
		// super.paintComponent(g);
		// Graphics2D g2d = (Graphics2D) g;
		// int x = (this.getWidth() - image.getWidth(null)) / 2;
		// int y = (this.getHeight() - image.getHeight(null)) / 2;
		// g2d.drawImage(image, x, y, null);
		// }
		// }
		;
		desktopPane.setBackground(Color.BLACK);
		myFrame.setIconImage(SwingUtil.MADKIT_LOGO.getImage());
		final JMenuBar menuBar = new JMenuBar();
		menuBar.add(new MadkitMenu(this));
		menuBar.add(new LaunchAgentsMenu(this,true));
		menuBar.add(new LaunchMAS(this));
		menuBar.add(new HelpMenu());

		JToolBar tb = new MadkitToolBar(this);
		myFrame.setJMenuBar(menuBar);
		tb.setRollover(true);
		tb.setFloatable(false);
		myFrame.add(tb, BorderLayout.PAGE_START);
		myFrame.setPreferredSize(new Dimension(800, 600));
		myFrame.setSize(new Dimension(800, 600));
		// desktopPane.setPreferredSize(new Dimension(800,600));
		// setSize(800,600);
		myFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		myFrame.add(desktopPane);
		myFrame.pack();
		// myFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		myFrame.setVisible(true);
		myFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		myFrame.setResizable(true);
	}

}