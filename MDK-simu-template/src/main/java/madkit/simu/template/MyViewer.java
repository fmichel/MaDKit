/*******************************************************************************
 * MaDKit - Multi-agent systems Development Kit 
 * 
 * Copyright (c) 1998-2025 Fabien Michel, Olivier Gutknecht, Jacques Ferber...
 * 
 * This software is a computer program whose purpose is to
 * provide a lightweight Java API for developing and simulating 
 * Multi-Agent Systems (MAS) using an organizational perspective.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.You can use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty and the software's author, the holder of the
 * economic rights, and the successive licensors have only limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading, using, modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean that it is complicated to manipulate, and that also
 * therefore means that it is reserved for developers and experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and, more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 *******************************************************************************/

package madkit.simu.template;

import static javafx.scene.paint.Color.BLACK;

import madkit.kernel.Agent;
import madkit.simulation.PropertyProbe;
import madkit.simulation.viewer.Viewer2D;

/**
 * The `MyViewer` class extends the `Viewer2D` class and is responsible for rendering the
 * simulation. It uses property probes to get the positions of agents and draws them on
 * the screen.
 */
public class MyViewer extends Viewer2D {

	// Property probe to get the X position of agents
	PropertyProbe<Double> positionX;
	// Property probe to get the Y position of agents
	PropertyProbe<Double> positionY;

	/**
	 * This method is called when the viewer is activated. It initializes the property probes
	 * for the X and Y positions of agents and adds them to the viewer.
	 * <p>
	 * Call the superclass's onActivation method on the last line so that the building of the
	 * GUI is done after the probes are added to the viewer, thus enabling the automatic
	 * update of the GUI with the static bean properties of the classes of the agents
	 * referenced by the probes
	 * 
	 */
	@Override
	protected void onActivation() {
		// Initialize the property probe for the X position of agents
		positionX = new PropertyProbe<>(getModelGroup(), "simuAgent", "x");
		// Initialize the property probe for the Y position of agents
		positionY = new PropertyProbe<>(getModelGroup(), "simuAgent", "y");
		// Add the X position probe to the viewer
		addProbe(positionX);
		// Add the Y position probe to the viewer
		addProbe(positionY);
		// Call the superclass's onActivation method
		super.onActivation();
	}

	/**
	 * This method is called to render the simulation. It clears the screen and draws the
	 * agents as black ovals at their respective positions.
	 */
	@Override
	public void render() {
		super.render();
		getGraphics().setFill(BLACK);
		for (Agent a : positionX.getAgents()) {
			getGraphics().fillOval(positionX.getPropertyValue(a), positionY.getPropertyValue(a), 10, 10);
		}
	}
}
