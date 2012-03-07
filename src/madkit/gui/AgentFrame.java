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

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenuBar;
import javax.swing.WindowConstants;

import madkit.action.KernelAction;
import madkit.agr.LocalCommunity;
import madkit.agr.LocalCommunity.Groups;
import madkit.agr.Organization;
import madkit.gui.menu.AgentLogLevelMenu;
import madkit.gui.menu.AgentMenu;
import madkit.gui.menu.MadkitMenu;
import madkit.kernel.AbstractAgent;
import madkit.message.KernelMessage;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.9
 * @version 0.9
 * 
 */
final class AgentFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6337250099157352055L;
	private JInternalFrame internalFrame;
	
	AgentFrame(final AbstractAgent agent, String name) {
		super(name);
		setIconImage(GUIManagerAgent.MADKIT_LOGO);
		setJMenuBar(createMenuBarFor(agent));

		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			
			@Override
			public void windowClosed(WindowEvent e) {
				closeProcess(agent);
			}
			private void closeProcess(final AbstractAgent agent) {
				if (agent.isAlive()) {
					setTitle("Closing " + agent.getName());
					killAgent(agent, 2);
				}
			}
			public void windowClosing(java.awt.event.WindowEvent e) {
				closeProcess(agent);
			}
//			@Override
//			public void windowClosed(WindowEvent e) {
//				killAgent(agent,0);
//			}
		});
//		addWindowListener(new WindowAdapter() {
//			@Override
//			public void windowClosing(WindowEvent e) {
//				setTitle(getTitle()+ " closing...");
//				if (agent.isAlive()) {
////					agent.sendMessage(LocalCommunity.NAME, Groups.SYSTEM, Roles.KERNEL, new KernelMessage(MadkitAction.MADKIT_KILL_AGENT, agent, 2));
//					agent.killAgent(agent,1);
//				}
////				dispose();
//			}
//		});
		setSize(400,300);
		setLocationRelativeTo(null);
	}
	
	@Override
	public void dispose() {
		if(internalFrame != null){
			internalFrame.dispose();
		}
//		removeAll();
		super.dispose();
	}

	private JMenuBar createMenuBarFor(AbstractAgent agent) {
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(new MadkitMenu(agent));
		menuBar.add(new AgentMenu(agent));
		menuBar.add(new AgentLogLevelMenu(agent));
		menuBar.add(Box.createHorizontalGlue());
		menuBar.add(new AgentStatusPanel(agent));
		return menuBar;
	}

	/**
	 * @param internalFrame the internalFrame to set
	 */
	void setInternalFrame(JInternalFrame internalFrame) {
		this.internalFrame = internalFrame;
	}

	/**
	 * @return the internalFrame
	 */
	JInternalFrame getInternalFrame() {
		return internalFrame;
	}

	@Override
	public void setLocation(int x, int y) {
		super.setLocation(x, y);
		if(internalFrame != null){
			internalFrame.setLocation(x, y);
		}
	}

	/**
	 * @param agent
	 */
	static void killAgent(final AbstractAgent agent,int timeOutSeconds) {//TODO move that
		if (agent.isAlive()) {
			agent.sendMessage(
					LocalCommunity.NAME, 
					Groups.SYSTEM, 
					Organization.GROUP_MANAGER_ROLE, 
					new KernelMessage(KernelAction.KILL_AGENT, agent, timeOutSeconds));
		}
	}
}

//final class AgentInternalFrame extends JInternalFrame{
//	/**
//	 * 
//	 */
//	private static final long serialVersionUID = -7697151858719380860L;
//	final private GUIManagerAgent desk;
//	
//	AgentInternalFrame(final AgentFrame f, GUIManagerAgent desk){
//		super(f.getTitle(),true,true,true,true);
//		this.desk = desk;
//		setSize(f.getSize());
////		setLocation(f.getLocation());
//		setContentPane(f.getContentPane());
//		setJMenuBar(f.getJMenuBar());
//		f.setInternalFrame(this);//TODO ??
//		addInternalFrameListener(new InternalFrameAdapter() {
//			@Override
//			public void internalFrameClosed(InternalFrameEvent e) {
//				for (WindowListener wl : f.getWindowListeners()) {
//					wl.windowClosing(null);
//				}
//			}
//		});
//	}
//	
////	@Override
////	public void setLocation(int x, int y) {
////		super.setLocation(x, y);
////		final Point loc = desk.checkLocation(this);
////		super.setLocation(loc.x,loc.y);
////	}
//	
//	
//}
