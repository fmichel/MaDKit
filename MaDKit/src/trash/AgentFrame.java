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
package madkit.kernel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.util.concurrent.CompletableFuture;

import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenuBar;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import madkit.gui.PrintableFrame;
import madkit.gui.menu.AgentMenu;
import madkit.gui.menu.HelpMenu;
import madkit.gui.menu.MadkitMenu;
import madkit.gui.swing.SwingUtil;

/**
 * The default frame which is used for the agents in the GUI engine of MaDKit.
 * Subclasses could be defined to obtain customized frames.
 * 
 *
 * @since MaDKit 5.0.0.9
 * @version 0.92
 */
public class AgentFrame extends JFrame implements PrintableFrame {// NOSONAR

	private final transient Agent agent;
    private JInternalFrame internalFrame;

	/**
	 * TThis constructor is protected because this class should not be directly
	 * instantiated as it is used by the MaDKit GUI manager.
	 * 
	 * @param agent the considered agent
	 */
	public AgentFrame(final Agent agent,  boolean autoCloseOnAgentEnd) {
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

			private void closeProcess() {
					setTitle("Closing " + agent.getName());
						agent.killAgent(agent, 2);
			}

			@Override
			public void windowClosing(java.awt.event.WindowEvent e) {
				closeProcess();
			}
		});
		setSize(400, 300);
		getContentPane().setBackground(Color.WHITE);
		setBackground(Color.WHITE);
		setLocationRelativeTo(null);

		if(autoCloseOnAgentEnd) {
			CompletableFuture.runAsync(() -> {
				try {
					synchronized (agent.alive) {
						agent.alive.wait();
					}
					SwingUtilities.invokeLater(() -> dispose());
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
			});
		}
	}

	@Override
	public void dispose() {
		if (internalFrame != null) {
			internalFrame.dispose();
		}
		super.dispose();
	}

	/**
	 * Builds the menu bar that will be used for this frame. By default it creates a
	 * {@link JMenuBar} featuring:
	 * <ul>
	 * <li>{@link MadkitMenu}
	 * <li>{@link AgentMenu}
	 * <li>{@link AgentLogLevelMenu}
	 * <li>{@link HelpMenu}
	 * <li>{@link AgentStatusPanel}
	 * </ul>
	 * 
	 * @return a menu bar
	 */
	public JMenuBar createMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(new MadkitMenu(agent));
//		menuBar.add(new AgentMenu(agent));
//		menuBar.add(new AgentLogLevelMenu(agent));
//		menuBar.add(new DisplayMenu(this));
//		menuBar.add(new HelpMenu());
		menuBar.add(Box.createHorizontalGlue());
//		menuBar.add(new AgentStatusPanel(agent));
		return menuBar;
	}

	/**
	 * Builds the tool bar that will be used. By default, it returns
	 * <code>null</code> so that there is no toll bar in the default agent frames.
	 * 
	 * @return a tool bar
	 */
	public JToolBar createJToolBar() {
		return null;
	}

	/**
	 * @param internalFrame the internalFrame to set
	 */
	void setInternalFrame(JInternalFrame internalFrame) {
		this.internalFrame = internalFrame;
		for (ComponentListener l : this.getComponentListeners())
			this.internalFrame.addComponentListener(l);
	}

	@Override
	public void setLocation(int x, int y) {
		super.setLocation(x, y);
		if (internalFrame != null) {
			internalFrame.setLocation(x, y);
		}
	}

	@Override
	public void setSize(int width, int height) {
		super.setSize(width, height);
		if (internalFrame != null) {
			internalFrame.setSize(width, height);
		}
	}

	@Override
	public void pack() {
		super.pack();
		if (internalFrame != null) {
			internalFrame.pack();
		}
	}

	/**
	 * @return the agent for which this frame has been created.
	 */
	public Agent getAgent() {
		return agent;
	}

	@Override
	public String toString() {
		return "AFrame for " + agent + " " + super.toString();
	}

	@Override
	public Container getPrintableContainer() {
		if (internalFrame != null) {
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
