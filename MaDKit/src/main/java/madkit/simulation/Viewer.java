/*******************************************************************************
 * Copyright (c) 2021, 2024 MaDKit Team
 *
 * This software is a computer program whose purpose is to
 * provide a lightweight Java API for developing and simulating 
 * Multi-Agent Systems (MAS) using an organizational perspective.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
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
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 *******************************************************************************/
package madkit.simulation;

import static madkit.simulation.SimuOrganization.ENGINE_GROUP;
import static madkit.simulation.SimuOrganization.SCHEDULER_ROLE;
import static madkit.simulation.SimuOrganization.VIEWER_ROLE;

import madkit.action.SchedulingAction;
import madkit.kernel.Watcher;
import madkit.messages.SchedulingMessage;
import madkit.simulation.viewer.ViewerDefaultGUI;

/**
 * A Viewer is a {@link Watcher} that is designed to display simulation states
 * on screen.
 * <p>
 * By default, a viewer agent is automatically granted the role
 * {@link SimuOrganization#VIEWER_ROLE} in the group
 * {@link SimuOrganization#ENGINE_GROUP} when it is activated. This can be
 * changed by overriding the {@link #onActivation()} method.
 * 
 * <p>
 * To this end, it holds a {@link ViewerDefaultGUI} which is used to render the
 * simulation state through a predefined JavaFX stage. When the viewer agent
 * wants to render the simulation state, it calls the
 * {@link ViewerDefaultGUI#requestRendering()} method which triggers the
 * {@link #render()} in the JavaFX thread if needed, depending on the state of
 * the GUI such as the synchronous painting mode.
 * <p>
 * 
 * @see ViewerDefaultGUI
 */
public abstract class Viewer extends Watcher {

	private ViewerDefaultGUI gui;

	/**
	 * This method is called when the agent is activated. By default, it requests
	 * the role {@link SimuOrganization#VIEWER_ROLE} in the group
	 * {@link SimuOrganization#ENGINE_GROUP}.
	 */
	@Override
	protected void onActivation() {
		requestRole(getCommunity(), ENGINE_GROUP, VIEWER_ROLE);
	}

	/**
	 * When using the default simulation engine setup, this method is automatically
	 * called by the scheduler agent when it is time to render the simulation state.
	 * 
	 * It calls the {@link ViewerDefaultGUI#requestRendering()} method which
	 * triggers the {@link #render()} in the JavaFX thread if needed, depending on
	 * the state of the GUI such as the synchronous painting mode.
	 */
	public void display() {
		if (gui != null) {
			gui.requestRendering();
		}
	}

	/**
	 * This method is called when the agent is ending. By default, it sends a
	 * {@link SchedulingMessage} with the {@link SchedulingAction#SHUTDOWN} action
	 * to the scheduler agent if no other viewer agent is present in the group
	 * {@link SimuOrganization#ENGINE_GROUP} with the role {@link SimuOrganization
	 * #VIEWER_ROLE}.
	 */
	@Override
	protected void onEnd() {
		if (getAgentsWithRole(getCommunity(), ENGINE_GROUP, VIEWER_ROLE).isEmpty()) {
			send(new SchedulingMessage(SchedulingAction.SHUTDOWN), getCommunity(), ENGINE_GROUP, SCHEDULER_ROLE);
		}
	}

	@Override
	public void onSimulationStart() {
		display();
	}

	/**
	 * Renders the simulation state. This method is called by the GUI bounded to
	 * this agent when it is time to render the simulation state. The rendering is
	 * done in the JavaFX thread.
	 */
	public abstract void render();

	/**
	 * Returns the GUI of this viewer.
	 * 
	 * @return the GUI of this viewer
	 */
	@SuppressWarnings("unchecked")
	public <G extends ViewerDefaultGUI> G getGUI() {
		return (G) gui;
	}

	/**
	 * Sets the GUI of this viewer.
	 * 
	 * @param gui the GUI to set
	 */
	public void setGUI(ViewerDefaultGUI gui) {
		this.gui = gui;
	}
}
