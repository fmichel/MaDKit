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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenuBar;
import javax.swing.JToolBar;
import javax.swing.WindowConstants;

import madkit.action.KernelAction;
import madkit.agr.LocalCommunity;
import madkit.agr.LocalCommunity.Groups;
import madkit.agr.Organization;
import madkit.gui.menu.AgentLogLevelMenu;
import madkit.gui.menu.AgentMenu;
import madkit.gui.menu.HelpMenu;
import madkit.gui.menu.MadkitMenu;
import madkit.kernel.AbstractAgent;
import madkit.message.KernelMessage;

/**
 * The default frame which is used for the agents in the GUI engine of MaDKit.
 * Subclasses could be defined to obtain customized frames.
 * 
 * @author Fabien Michel
 * @since MaDKit 5.0.0.9
 * @version 0.91
 * 
 */
public class AgentFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6337250099157352055L;
	private JInternalFrame internalFrame;
	private final AbstractAgent agent;
	
	/**
	 * TThis constructor is protected because this class
	 * should not be directly instantiated as it is used
	 * by the MaDKit GUI manager.
	 * 
	 * @param agent the considered agent
	 */
	protected AgentFrame(final AbstractAgent agent) {
		super(agent.getName());
		this.agent = agent;
		setIconImage(SwingUtil.MADKIT_LOGO.getImage());
		setJMenuBar(createMenuBar());
		JToolBar tb = createJToolBar();
		if (tb != null) {
			add(tb, BorderLayout.PAGE_START);
		}
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			
			@Override
			public void windowClosed(WindowEvent e) {
				closeProcess();
			}
			private void closeProcess() {
				if (agent.isAlive()) {
					setTitle("Closing " + agent.getName());
					killAgent(agent, 4);
				}
			}
			public void windowClosing(java.awt.event.WindowEvent e) {
				closeProcess();
			}
		});
		setSize(400,300);
		setLocationRelativeTo(null);
	}
	
	@Override
	public void dispose() {
		if(internalFrame != null){
			internalFrame.dispose();
		}
		super.dispose();
	}

	/**
	 * Builds the menu bar that will be used for this frame.
	 * By default it creates a {@link JMenuBar} featuring: 
	 * <ul>
	 * <li> {@link MadkitMenu}
	 * <li> {@link AgentMenu}
	 * <li> {@link AgentLogLevelMenu}
	 * <li> {@link HelpMenu}
	 * <li> {@link AgentStatusPanel}
	 * </ul>
	 * 
	 * @return a menu bar 
	 */
	public JMenuBar createMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(new MadkitMenu(agent));
		menuBar.add(new AgentMenu(agent));
		menuBar.add(new AgentLogLevelMenu(agent));
		menuBar.add(new HelpMenu());
		menuBar.add(Box.createHorizontalGlue());
		menuBar.add(new AgentStatusPanel(agent));
		return menuBar;
	}
	
	/**
	 * Builds the tool bar that will be used. By default, 
	 * it returns <code>null</code> so that there is no toll bar 
	 * in the default agent frames.
	 * 
	 * @return a tool bar
	 */
	public JToolBar createJToolBar(){
		return null;
	}

	/**
	 * @param internalFrame the internalFrame to set
	 */
	void setInternalFrame(JInternalFrame internalFrame) {
		this.internalFrame = internalFrame;
	}

	@Override
	public void setLocation(int x, int y) {
		super.setLocation(x, y);
		if(internalFrame != null){
			internalFrame.setLocation(x, y);
		}
	}
	
	@Override
	public void pack() {
		super.pack();
		if(internalFrame != null){
			internalFrame.pack();
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

	/**
	 * @return the agent for which this frame has been created.
	 */
	public AbstractAgent getAgent() {
		return agent;
	}

	/**
	 * Override to customize the agent frame that should be created
	 * by the GUI engine.
	 * 
	 * @param agent the related agent 
	 * @return the created frame 
	 */
	public static AgentFrame createAgentFrame(final AbstractAgent agent) {
		return new AgentFrame(agent);
	}
}
