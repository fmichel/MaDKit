/*
 * MadKitLanEdition (created by Jason MAHDJOUB (jason.mahdjoub@distri-mind.fr)) Copyright (c)
 * 2015 is a fork of MadKit and MadKitGroupExtension. 
 * 
 * Copyright or © or Copr. Jason Mahdjoub, Fabien Michel, Olivier Gutknecht, Jacques Ferber (1997)
 * 
 * jason.mahdjoub@distri-mind.fr
 * fmichel@lirmm.fr
 * olg@no-distance.net
 * ferber@lirmm.fr
 * 
 * This software is a computer program whose purpose is to
 * provide a lightweight Java library for designing and simulating Multi-Agent Systems (MAS).
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 * 
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package com.distrimind.madkit.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyVetoException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenuBar;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import com.distrimind.madkit.action.GUIManagerAction;
import com.distrimind.madkit.action.KernelAction;
import com.distrimind.madkit.agr.LocalCommunity.Groups;
import com.distrimind.madkit.agr.LocalCommunity.Roles;
import com.distrimind.madkit.i18n.Words;
import com.distrimind.madkit.kernel.AbstractAgent;
import com.distrimind.madkit.kernel.Agent;
import com.distrimind.madkit.kernel.Message;
import com.distrimind.madkit.message.GUIMessage;
import com.distrimind.madkit.message.KernelMessage;

/**
 * The GUI manager agent is responsible for setting and managing agents UI which
 * are created by the default mechanism of MaDKit.
 * 
 * @author Fabien Michel
 * @author Jason Mahdjoub
 * @since MaDKit 5.0.0.6
 * @since MadkitLanEdition 1.0
 * @version 0.92
 * 
 */
// * By default the kernel always launch this agent. Although, this agent is
// * extremely light weight, it is possible to tell the kernel to not launch it
// * by using the {@link BooleanOption#noGUIManager} option when launching
// MaDKit.
class GUIManagerAgent extends Agent {

	final private ConcurrentMap<AbstractAgent, JFrame> guis;
	private boolean shuttedDown = false;

	JDesktopPane desktopPane;

	private MDKDesktopFrame myFrame;
	private Constructor<? extends AgentFrame> agentFrameConstrutor;

	GUIManagerAgent(boolean asDaemon) { // NO_UCD use by reflection
		super(asDaemon);
		guis = new ConcurrentHashMap<>();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void activate() {// TODO parallelize that
		try {
			agentFrameConstrutor = (Constructor<? extends AgentFrame>) getMadkitConfig().agentFrameClass
					.getDeclaredConstructor(AbstractAgent.class);
		} catch (NoSuchMethodException | SecurityException | ClassCastException | NullPointerException e1) {
			e1.printStackTrace();
			try {
				agentFrameConstrutor = AgentFrame.class.getConstructor(AbstractAgent.class);
			} catch (NoSuchMethodException | SecurityException e) {
			}
		}
		// setThreadPriority(Thread.MAX_PRIORITY);
		// setLogLevel(Level.ALL);
		// requestRole(LocalCommunity.NAME, Groups.SYSTEM, Roles.GUI_MANAGER);//no need:
		// I am a manager
		if (!isDaemon()) {// use to detect desktop mode
			try {
				buildUI();
				/*
				 * if(MadkitProperties.JAVAWS_IS_ON)
				 * getMadkitConfig().autoConnectMadkitWebsite=true;
				 */
			} catch (HeadlessException e) {
				headlessLog(e);
				return;
			} catch (InstantiationException | IllegalAccessException | NullPointerException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
				getLogger().severeLog(Words.FAILED.toString() + " : UI creation", e);
			} 
		}
		requestRole(Groups.GUI,Roles.GUI);
	}

	/**
	 * @param e
	 */
	private void headlessLog(HeadlessException e) {
		getLogger().severe("\t" + e.getMessage() + "\n\tNo graphic environment, quitting");
		shuttedDown = true;
	}

	@Override
	protected void liveCycle() throws InterruptedException {
		final Message m = waitNextMessage();
		if (m instanceof GUIMessage) {
			proceedCommandMessage((GUIMessage) m);
		} else if (m instanceof KernelMessage) {
			proceedEnumMessage((KernelMessage) m);
		} else if (logger != null)
				logger.warning("I received a message that I do not understand. Discarding " + m);
		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isAlive() {
		return super.isAlive() && !shuttedDown;
	}

	private void proceedCommandMessage(GUIMessage cm) {
		if (isAlive()) {
			if (cm.getCode() == GUIManagerAction.SETUP_AGENT_GUI) {// because it needs a reply
				try {
					setupAgentGui((AbstractAgent) cm.getContent()[0]);
				} catch (HeadlessException e) {
					headlessLog(e);
				} finally {
					sendReply(cm, cm);
				}
			} else {
				super.proceedEnumMessage(cm);
			}
		}
	}

	@Override
	protected void end() {
		// SwingUtilities.invokeLater(
		// new Runnable() {
		// public void run() {
		killAgents(); // no need because it closes internal frames too
		if (myFrame != null) {// TODO swing thread or cleaner shutdown
			
			myFrame.dispose();
			
			myFrame=null;
			desktopPane=null;
		}
		if (logger != null)
			logger.finer("Ending: Frames disposed");
		// }});
	}
	
	@SuppressWarnings("unused")
	private void exit() {
		shuttedDown = true;
	}

	private void setupAgentGui(final AbstractAgent agent) {
		if (!shuttedDown && agent.isAlive()) {
			if (logger != null && logger.isLoggable(Level.FINE))
				logger.fine("Setting up GUI for " + agent);
			AgentFrame f = null;
			try {
				f = agentFrameConstrutor.newInstance(agent);
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e1) {
				e1.printStackTrace();
			}
			try {
				agent.setupFrame(f);
			} catch (Exception e) {
				agent.getLogger().severeLog("Frame setup problem -> default GUI settings", e);
				f = AgentFrame.createAgentFrame(agent);
			}
			guis.put(agent, f);
			final AgentFrame af = f;
			SwingUtilities.invokeLater(new Runnable() {

				public void run() {
					if (desktopPane != null) {
						final JInternalFrame jf = buildInternalFrame(af);
						desktopPane.add(jf);
						jf.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
						jf.addInternalFrameListener(new InternalFrameAdapter() {
							@Override
							public void internalFrameClosing(InternalFrameEvent e) {
								if (agent.isAlive()) {
									jf.setTitle("Closing " + agent.getName());
									AgentFrame.killAgent(agent, 2);
								} else {
									jf.dispose();
								}
							}
						});
						jf.setLocation(checkLocation(jf));
						jf.setVisible(true);
					} else {
						af.setLocation(checkLocation(af));
						af.setVisible(true);
					}
				}
			});
		}
	}

	JInternalFrame buildInternalFrame(final AgentFrame af) {
		final JInternalFrame ijf = new JInternalFrame(af.getTitle(), true, true, true, true);
		ijf.setFrameIcon(SwingUtil.MADKIT_LOGO_SMALL);
		ijf.setSize(af.getSize());
		ijf.setLocation(af.getLocation());
		ijf.setContentPane(af.getContentPane());
		ijf.setJMenuBar(af.getJMenuBar());
		af.setInternalFrame(ijf);
		return ijf;
	}

	private void iconifyAll(boolean iconify) {
		final int code = iconify ? Frame.ICONIFIED : Frame.NORMAL;
		for (final JFrame f : guis.values()) {
			f.setExtendedState(code);
		}
		if (myFrame != null) {// FIXME no need now, because this is only in desktop mode, but probably in the
								// future
			for (JInternalFrame ijf : desktopPane.getAllFrames()) {
				try {
					ijf.setIcon(iconify);
				} catch (PropertyVetoException e) {
					e.printStackTrace();
				}
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
	}

	Point checkLocation(Container c) {
		Dimension dim;
		List<? extends Container> l;
		if (c instanceof JInternalFrame) {
			dim = desktopPane.getSize();
			l = Arrays.asList(desktopPane.getAllFrames());
		} else {
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
		location.x = location.x <= dim.width ? location.x : location.x % dim.width;
		location.y = location.y <= dim.height ? location.y : location.y % dim.height;
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

	private void buildUI() throws InstantiationException, IllegalAccessException, NullPointerException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		myFrame = (MDKDesktopFrame) getMadkitConfig().desktopFrameClass.getDeclaredConstructor().newInstance();
		desktopPane = myFrame.getDesktopPane();
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
		JMenuBar menuBar = myFrame.getMenuBar(this);
		myFrame.setJMenuBar(menuBar);
		JToolBar tb = myFrame.getToolBar(this);
		tb.setRollover(true);
		tb.setFloatable(false);
		myFrame.add(tb, BorderLayout.PAGE_START);
		myFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		myFrame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				KernelAction.EXIT.getActionFor(GUIManagerAgent.this).actionPerformed(null);
			}
		});
		myFrame.pack();
		myFrame.setVisible(true);
		myFrame.setExtendedState(Frame.MAXIMIZED_BOTH);
		myFrame.setResizable(true);

	}
	

}