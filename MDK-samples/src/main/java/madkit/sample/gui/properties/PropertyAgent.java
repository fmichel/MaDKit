/*******************************************************************************
 * MaDKit - Multi-agent systems Development Kit 
 * 
 * Copyright (c) 1998-2026 Fabien Michel, Olivier Gutknecht, Jacques Ferber...
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
package madkit.sample.gui.properties;

import madkit.gui.DefaultAgentGUI;
import madkit.gui.SliderProperty;
import madkit.gui.UIProperty;
import madkit.kernel.Agent;

/**
 * Demonstrates the {@link UIProperty} and {@link SliderProperty} annotations for
 * automatic property-sheet generation in a {@link DefaultAgentGUI}.
 * <p>
 * When a {@link DefaultAgentGUI} is created for this agent, the right panel automatically
 * scans for fields annotated with {@code @UIProperty} and displays them as editable
 * controls grouped by {@link UIProperty#category()}. Fields also annotated with
 * {@code @SliderProperty} are rendered as sliders with configurable min, max, and scroll
 * precision.
 * <p>
 * This sample showcases several field types: {@code double} with slider, {@code double}
 * without slider, {@code int}, {@code boolean}, and {@link String}.
 *
 * @see UIProperty
 * @see SliderProperty
 * @see DefaultAgentGUI
 * @see madkit.gui.PropertySheetFactory
 */
public class PropertyAgent extends Agent {

	/**
	 * The movement speed of the agent, displayed as a slider ranging from 0 to 100.
	 */
	@UIProperty(category = "Movement", displayName = "Speed")
	@SliderProperty(min = 0.0, max = 100.0, scrollPrecision = 0.5)
	private double speed = 50.0;

	/**
	 * The movement direction of the agent in degrees.
	 */
	@UIProperty(category = "Movement", displayName = "Direction")
	private double direction = 0.0;

	/**
	 * The combat strength of the agent.
	 */
	@UIProperty(category = "Combat", displayName = "Strength")
	private int strength = 10;

	/**
	 * Whether the agent is currently active.
	 */
	@UIProperty(category = "Combat", displayName = "Active")
	private boolean active = true;

	/**
	 * The display name of the agent.
	 */
	@UIProperty(category = "Identity", displayName = "Agent Name")
	private String agentName = "DefaultAgent";

	/**
	 * Called when the agent is launched. Creates a {@link DefaultAgentGUI} whose right panel
	 * automatically displays a property sheet for all {@code @UIProperty}-annotated fields,
	 * then logs the current field values.
	 */
	@Override
	protected void onActivation() {
		new DefaultAgentGUI(this);
		getLogger().info(() -> "speed = " + speed);
		getLogger().info(() -> "direction = " + direction);
		getLogger().info(() -> "strength = " + strength);
		getLogger().info(() -> "active = " + active);
		getLogger().info(() -> "agentName = " + agentName);
	}

	/**
	 * Called after activation. Pauses for 5 seconds to allow time for interaction with the
	 * property sheet before the agent ends. During this time, users can modify the properties
	 * in the GUI and observe how the agent's state changes accordingly.
	 */
	@Override
	protected void onLive() {
		pause(5000);
	}

	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}

	public double getDirection() {
		return direction;
	}

	public void setDirection(double direction) {
		this.direction = direction;
	}

	public int getStrength() {
		return strength;
	}

	public void setStrength(int strength) {
		this.strength = strength;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getAgentName() {
		return agentName;
	}

	public void setAgentName(String agentName) {
		this.agentName = agentName;
	}

	/**
	 * Launches a single instance of {@link PropertyAgent}.
	 *
	 * @param args MaDKit command-line options (e.g. {@code --agentLogLevel FINE})
	 */
	public static void main(String[] args) {
		executeThisAgent(args);
	}
}
