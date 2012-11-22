/*
 * Copyright 1997-2012 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MaDKit.
 * 
 * MaDKit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MaDKit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MaDKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.simulation.viewer;

import java.awt.Color;
import java.awt.Graphics;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import madkit.kernel.Watcher;


/**
 * A very basic simulation viewer agent. This class defines
 * a panel for the simulation rendering and two modes
 * of rendering: Synchronous and asynchronous.
 * An <code>observe</code> method is already defined 
 * and is intended to be called by
 * scheduler agents to trigger the rendering.
 * This class could be thus extended to reuse
 * the rendering call mechanism which is defined in here.
 * 
 * @author Fabien Michel
 * @since MaDKit 5.0.0.17
 * @version 0.9
 * 
 */
public abstract class SwingViewer extends Watcher {

	
	/**
	 * 
	 */
	private static final long	serialVersionUID	= -403113166858504599L;
	private JPanel	displayPane;
	private boolean synchronousPainting = false;
	private boolean rendering = true;
	private JFrame frame;

	
	
	/**
		 * Creates a new agent with a default panel 
		 * for rendering purposes
		 */
		public SwingViewer() {
			displayPane = new JPanel(){
				/**
				 * 
				 */
				private static final long	serialVersionUID	= 3265429181597273604L;

				@Override
				protected void paintComponent(Graphics g) {
	//				if (g != null) {
						super.paintComponent(g);
						render(g);
	//				}
				}
			};
			displayPane.setBackground(Color.WHITE);
			createGUIOnStartUp();
		}
		
		/**
	 * @return <code>true</code> if the rendering 
	 * activity is activated.
	 */
	public boolean isRendering() {
		return rendering;
	}


	
	/**
	 * Enable or disable the rendering activity
	 */
	public void setRendering(boolean activate) {
		this.rendering = activate;
	}


	/**
	 * @return the current panel which is used for display
	 */
	public JPanel getDisplayPane() {
		return displayPane;
	}
	
	
	/**
	 * Could be used to define a customized panel 
	 * instead of the default pane
	 * 
	 * 
	 * @param displayPane the displayPane to set
	 */
	public void setDisplayPane(JPanel displayPane) {
		this.displayPane = displayPane;
	}
	
	/**
	 * Intended to be invoke by a scheduler's activator for 
	 * triggering the rendering. This method
	 * activate the rendering either synchronously or 
	 * Asynchronously depending
	 * on {@link #isSynchronousPainting()}.
	 * 
	 */
	protected void observe()
	{
		if(rendering && isAlive()){
			if (synchronousPainting) {
				try {
					SwingUtilities.invokeAndWait(new Runnable() {
						@Override
						public void run() {
							displayPane.paintImmediately(0, 0, displayPane.getWidth(), displayPane.getHeight());
						}
					});
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			}
			else
				displayPane.repaint();
			}
	}

	

	/**
	 * Override this method to do the rendering in the agent's panel.
	 * This method is automatically called when the <code>observe</code>
	 * method is triggered by a Scheduler
	 * @param g
	 */
	protected abstract void render(Graphics g);

	/**
	 * Tells if the rendering should be done synchronously 
	 * or asynchronously with respect to simulation steps.
	 * 
	 * @return the synchronousPainting
	 */
	public boolean isSynchronousPainting() {
		return synchronousPainting;
	}

	/**
	 * Set the rendering mode to synchronous or asynchronous. 
	 * Synchronous painting is done
	 * for each time step and the simulation does not advance until
	 * all the rendering is done for a step: The simulation is slower but 
	 * more smoothly rendered, making the visualization 
	 * of the simulation dynamics more precise. In asynchronous 
	 * mode, the rendering is done in parallel with the simulation
	 * steps and thus only display snapshot of the simulation's state:
	 * 
	 * @param synchronousPainting the synchronousPainting mode to set
	 */
	public void setSynchronousPainting(boolean synchronousPainting) {
		this.synchronousPainting = synchronousPainting;
	}

	/** Provides a default implementation that 
	 * assigns the default panel to the default frame
	 * 
	 * @see madkit.kernel.AbstractAgent#setupFrame(javax.swing.JFrame)
	 */
	public void setupFrame(javax.swing.JFrame frame) {
		displayPane.setSize(frame.getSize());
		frame.add(displayPane);
		setFrame(frame);
	}

	/**
	 * By default, get the default frame provided by MaDKit in 
	 * {@link #setupFrame(JFrame)} and
	 * set using {@link #setupFrame(JFrame)}.
	 * It can be anything else if {@link #setupFrame(JFrame)} is overridden.
	 * 
	 * @return the working frame
	 */
	public JFrame getFrame() {
		return frame;
	}

	/**
	 * Set the frame which is used so that
	 * subclasses can have access to it
	 * 
	 * @param frame the working frame
	 */
	public void setFrame(JFrame frame) {
		this.frame = frame;
	}
}
