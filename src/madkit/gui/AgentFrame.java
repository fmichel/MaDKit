/*
 * Copyright or © or Copr. Fabien Michel, Olivier Gutknecht, Jacques Ferber (1997)

fmichel@lirmm.fr
olg@no-distance.net
ferber@lirmm.fr

This software is a computer program whose purpose is to 
provide a lightweight Java library for designing and simulating Multi-Agent Systems (MAS).

This software is governed by the CeCILL-C license under French law and
abiding by the rules of distribution of free software.  You can  use, 
modify and/ or redistribute the software under the terms of the CeCILL-C
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info". 

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability. 

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or 
data to be ensured and,  more generally, to use and operate it in the 
same conditions as regards security. 

The fact that you are presently reading this means that you have had
knowledge of the CeCILL-C license and that you accept its terms.
 */
package madkit.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

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
import madkit.gui.menu.DisplayMenu;
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
public class AgentFrame extends JFrame  implements PrintableFrame{

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
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentMoved(ComponentEvent e) {
				final Component component = e.getComponent();
				SwingUtil.UI_PREFERENCES.putInt(agent.getName() + "_X", component.getX());
				SwingUtil.UI_PREFERENCES.putInt(agent.getName() + "_Y", component.getY());
			}
			@Override
			public void componentResized(ComponentEvent e) {
				final Component component = e.getComponent();
				SwingUtil.UI_PREFERENCES.putInt(agent.getName() + "_WIDTH", component.getWidth());
				SwingUtil.UI_PREFERENCES.putInt(agent.getName() + "_HEIGHT", component.getHeight());
			}
		});
		addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if(evt.getPropertyName().equals("background")){
					SwingUtil.UI_PREFERENCES.putInt(agent.getClass().getName() + "_BGC", getBackground().getRGB());
				}
			}
		});
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
		menuBar.add(new DisplayMenu(this));
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
		for(ComponentListener l : this.getComponentListeners())
			this.internalFrame.addComponentListener(l);
	}

	@Override
	public void setLocation(int x, int y) {
		super.setLocation(x, y);
		if(internalFrame != null){
			internalFrame.setLocation(x, y);
		}
	}
	
	@Override
	public void setSize(int width, int height) {
		super.setSize(width, height);
		if(internalFrame != null){
			internalFrame.setSize(width, height);
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
	
	@Override
	public String toString() {
		return "AFrame for "+agent+" "+super.toString();
	}

	@Override
	public Container getPrintableContainer() {
		if(internalFrame != null){
			return internalFrame.getDesktopPane().getTopLevelAncestor();
		}
		return this;
	}
	
	@Override
	public void setBackground(Color bgColor) {
		super.setBackground(bgColor);
		getContentPane().setBackground(bgColor);
	}

}
