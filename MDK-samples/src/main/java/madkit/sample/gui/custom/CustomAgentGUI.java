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
package madkit.sample.gui.custom;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import madkit.gui.AgentLogArea;
import madkit.gui.DefaultAgentGUI;
import madkit.kernel.Agent;

/**
 * A custom GUI that extends {@link DefaultAgentGUI} and overrides panel creation methods
 * to demonstrate how to build a tailored agent interface.
 * <p>
 * This class overrides:
 * <ul>
 * <li>{@link #createCenterNode()} — replaces the default log area with a
 * {@link VBox} containing a label, a button, and an {@link AgentLogArea}</li>
 * <li>{@link #createLeftNode()} — adds a simple label in the left region</li>
 * </ul>
 * <p>
 * All other regions (top menu bar, right property sheet, bottom toolbar) retain their
 * default behavior from {@link DefaultAgentGUI}.
 *
 * @see DefaultAgentGUI
 * @see AgentLogArea
 */
public class CustomAgentGUI extends DefaultAgentGUI {

	/**
	 * Constructs a custom GUI for the specified agent. The constructor delegates to
	 * {@link DefaultAgentGUI#DefaultAgentGUI(Agent)}, which builds and shows the window.
	 *
	 * @param agent the agent for which the GUI is created
	 */
	public CustomAgentGUI(Agent agent) {
		super(agent);
	}

	/**
	 * Creates a custom center node containing a label, an interactive button, and an
	 * {@link AgentLogArea} for log output.
	 * <p>
	 * Clicking the button logs an informational message through the agent's logger,
	 * which then appears in the log area below.
	 *
	 * @return a {@link VBox} with centered content
	 */
	@Override
	protected Node createCenterNode() {
		VBox vbox = new VBox(10);
		vbox.setAlignment(Pos.CENTER);
		vbox.getChildren().add(new Label("Custom Center Panel"));
		Button button = new Button("Click me!");
		button.setOnAction(event -> getAgent().getLogger().info(() -> "Button clicked!"));
		vbox.getChildren().add(button);
		vbox.getChildren().add(new AgentLogArea(getAgent()));
		return vbox;
	}

	/**
	 * Creates a label for the left region of the {@link javafx.scene.layout.BorderPane}.
	 * The default implementation returns {@code null} (no left node), so this override
	 * demonstrates how to populate that region.
	 *
	 * @return a {@link Label} displayed on the left side of the GUI
	 */
	@Override
	protected Node createLeftNode() {
		return new Label("Left Panel");
	}
}
