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
package madkit.simulation.viewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.ResourceBundle;

import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import madkit.action.ActionInfo;
import madkit.action.BooleanAction;
import madkit.gui.AgentFrame;
import madkit.gui.SwingUtil;
import madkit.gui.menu.DisplayMenu;
import madkit.i18n.I18nUtilities;
import madkit.kernel.Watcher;

/**
 * A very basic simulation viewer agent. This class defines a panel for the simulation rendering and two modes of
 * rendering: Synchronous and asynchronous. The synchronous mode ensures that each simulation frame is displayed. That
 * means that the scheduler will wait the end of the rendering activity to proceed to the next activator, waiting for
 * the swing thread to ends. this is not the case with the asynchronous mode so that the whole simulation process goes
 * faster because some simulation states will not be displayed. An <code>observe</code> method is already defined and is
 * intended to be called by scheduler agents to trigger the rendering. This class could be thus extended to reuse the
 * rendering call mechanism which is defined in here.
 * 
 * @author Fabien Michel
 * @since MaDKit 5.0.0.17
 * @version 1.1
 */
public abstract class SwingViewer extends Watcher {

    private JComponent displayPane;
    private JFrame frame;
    private BooleanAction rendering;
    private BooleanAction synchroPainting;
    private JToolBar toolBar;
    private JComboBox<Integer> comboBox;
    private boolean synchronousPainting = true;
    private boolean renderingOn = true;
    private int renderingInterval;
    private int counter = 1;

    /**
     * Creates a new agent with a default panel for rendering purposes
     */
    public SwingViewer() {
	initActionsAndGUIComponent();
	setRenderingInterval(1);
	setSynchronousPainting(true);
	displayPane = new JPanel() {

	    /**
	     * 
	     */
	    private static final long serialVersionUID = 3265429181597273604L;

	    @Override
	    protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		render(g);
	    }
	};
	displayPane.setBackground(Color.WHITE);
	createGUIOnStartUp();
    }

    /**
     * 
     */
    private void initActionsAndGUIComponent() {
	final ResourceBundle messages = I18nUtilities.getResourceBundle(SwingViewer.class.getSimpleName());
	initRenderingIntervalComboBox(messages.getString("UPDATE_INTERVAL"));
	rendering = new BooleanAction(new ActionInfo("DISABLE", KeyEvent.VK_A, messages)) {

	    private static final long serialVersionUID = 1L;

	    @Override
	    public void onUpdate(boolean isSelected) {
		renderingOn = isSelected;
	    }
	};
	setRendering(renderingOn);

	synchroPainting = new BooleanAction(new ActionInfo("SYNCHRO_PAINTING", KeyEvent.VK_Z, messages)) {

	    private static final long serialVersionUID = 1L;

	    @Override
	    public void onUpdate(boolean isSelected) {
		synchronousPainting = !isSelected;
		comboBox.setVisible(synchronousPainting);
	    }
	};
	rendering.putValue(Action.SELECTED_KEY, !synchronousPainting);
    }

    /**
     * @return <code>true</code> if the rendering activity is activated.
     */
    public boolean isRendering() {
	return renderingOn;
    }

    /**
     * Enable or disable the rendering activity
     */
    public void setRendering(boolean activated) {
	rendering.putValue(Action.SELECTED_KEY, !activated);
    }

    /**
     * @return the current panel which is used for display
     */
    public JComponent getDisplayPane() {
	return displayPane;
    }

    /**
     * Could be used to define a customized panel instead of the default pane
     * 
     * @param displayPane
     *            the displayPane to set
     */
    public void setDisplayPane(JComponent displayPane) {
	if (this.displayPane != displayPane) {
	    if (this.displayPane != null) {
		getFrame().remove(this.displayPane);
	    }
	    getFrame().add(displayPane);
	    this.displayPane = displayPane;
	}
    }

    /**
     * Intended to be invoked by a scheduler's activator for triggering the rendering. This method activate the rendering
     * either synchronously or asynchronously depending on {@link #isSynchronousPainting()}.
     */
    protected void observe() {
	if (renderingOn && isAlive()) {
	    if (synchronousPainting) {
		if (counter > renderingInterval) {
		    try {
			SwingUtilities.invokeAndWait(new Runnable() {

			    @Override
			    public void run() {
				displayPane.repaint();
			    }
			});
		    }
		    catch(InterruptedException e) {
			Thread.currentThread().interrupt();
		    }
		    catch(InvocationTargetException e) {
			e.printStackTrace();
		    }
		    counter = 2;
		}
		else {
		    counter++;
		}
	    }
	    else
		displayPane.repaint();
	}
    }

    /**
     * Override this method to do the rendering in the agent's panel. This method is automatically called when the
     * <code>observe</code> method is triggered by a Scheduler
     * 
     * @param g
     */
    protected abstract void render(Graphics g);

    /**
     * Tells if the rendering should be done synchronously or asynchronously with respect to simulation steps.
     * 
     * @return the synchronousPainting
     */
    public boolean isSynchronousPainting() {
	return synchronousPainting;
    }

    /**
     * Set the rendering mode to synchronous or asynchronous. Synchronous painting is done for each time step and the
     * simulation does not advance until all the rendering is done for a step: The simulation is slower but more smoothly
     * rendered, making the visualization of the simulation dynamics more precise. In asynchronous mode, the rendering is
     * done in parallel with the simulation steps and thus only display snapshot of the simulation's state:
     * 
     * @param synchronousPainting
     *            the synchronousPainting mode to set
     */
    public void setSynchronousPainting(boolean synchronousPainting) {
	synchroPainting.putValue(Action.SELECTED_KEY, !synchronousPainting);
    }

    /**
     * Provides a default implementation that assigns the default panel to the default frame
     * 
     * @see madkit.kernel.AbstractAgent#setupFrame(AgentFrame)
     */
    @Override
    public void setupFrame(AgentFrame frame) {
	setFrame(frame);
	displayPane.setSize(frame.getSize());
	frame.add(displayPane);
	frame.getJMenuBar().remove(3);
	frame.getJMenuBar().add(getDisplayMenu(), 2);
	frame.add(getToolBar(), BorderLayout.PAGE_START);
    }

    /**
     * Returns a menu which could be used in any viewer GUI.
     * 
     * @return a menu controlling the viewer's options
     */
    public JMenu getDisplayMenu() {
	JMenu myMenu = new DisplayMenu(getFrame());
	myMenu.remove(1);// TODO dirty hack
	myMenu.setMnemonic(KeyEvent.VK_O);
	myMenu.add(new JCheckBoxMenuItem(rendering));
	myMenu.add(new JCheckBoxMenuItem(synchroPainting));
	return myMenu;
    }

    /**
     * By default, get the default frame provided by MaDKit in {@link #setupFrame(AgentFrame)} and set using
     * {@link #setupFrame(AgentFrame)}. It can be any {@link JFrame} if {@link #setupFrame(AgentFrame)} has been overridden.
     * 
     * @return the working frame
     */
    public JFrame getFrame() {
	return frame;
    }

    public Action getSynchroPaintingAction() {
	return synchroPainting;
    }

    /**
     * Set the frame which is used so that subclasses can have access to it
     * 
     * @param frame
     *            the working frame
     */
    private void setFrame(final JFrame frame) {
	this.frame = frame;
    }

    /**
     * Set the number of states between display updates. If set to 1, every simulation states will be displayed
     * 
     * @param interval
     *            an int > 0
     */
    public void setRenderingInterval(int interval) {
	renderingInterval = interval > 0 ? interval : 1;
	if ((int) comboBox.getSelectedItem() != renderingInterval) {
	    comboBox.setSelectedItem(renderingInterval);
	}
    }

    @SuppressWarnings("serial")
    private void initRenderingIntervalComboBox(String titleAndTooltip) {
	final Integer[] defaultValues = { 1, 5, 10, 20, 50, 100, 200, 500, 1000, 5000, 10000, 50000, 100000, 200000, 500000 };
	comboBox = new JComboBox<Integer>(defaultValues) {

	    public java.awt.Dimension getMaximumSize() {
		return new Dimension(125, 38);
	    }
	};
	final String[] tatt = titleAndTooltip.split(";");
	comboBox.setBorder(new TitledBorder(tatt[0]));
	comboBox.setToolTipText(tatt[1]);
	comboBox.setEditable(true);
	comboBox.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		try {
		    setRenderingInterval((int) comboBox.getSelectedItem());
		}
		catch(ClassCastException e1) {
		    comboBox.setSelectedItem(1);
		}
	    }
	});
    }

    /**
     * Returns the viewer's toolbar.
     * 
     * @return a toolBar controlling the viewer's actions
     */
    public JToolBar getToolBar() {
	if (toolBar == null) {
	    toolBar = new JToolBar("viewer toolbar");
	    toolBar.setAlignmentX(JToolBar.RIGHT_ALIGNMENT);
	    SwingUtil.addBooleanActionTo(toolBar, rendering);
	    SwingUtil.addBooleanActionTo(toolBar, synchroPainting);
	    toolBar.add(comboBox);
	    SwingUtil.scaleAllAbstractButtonIconsOf(toolBar, 24);
	}
	return toolBar;
    }

}
