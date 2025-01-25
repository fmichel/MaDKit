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
package madkit.gui;

import javafx.scene.control.Button;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import madkit.kernel.Agent;

/**
 * The `FXOutputPane` class extends the JavaFX {@link BorderPane} class to provide a panel
 * that displays an agent's logging activity. This component is the default panel used for
 * the frames created using the default {@link Agent#setupDefaultGUI() implementation}.
 * 
 * @version 6.0
 * @see Agent#setupDefaultGUI()
 */
public class FXOutputPane extends BorderPane {

	private final TextArea textArea;

	/**
	 * Builds the panel for the agent
	 * 
	 * @param agent the agent for which this panel is created
	 */
	public FXOutputPane(Agent agent) {
		MenuBar menuBar = Menus.createMenuBarFor(agent);
		setTop(menuBar);
		textArea = new AgentLogArea(agent);
		setCenter(textArea);
		setRight(PropertySheetFactory.getTitledPaneSheet(agent));
		Button clear = new Button("clear");
		setBottom(clear);
		clear.setOnAction(_ -> textArea.clear());
	}

	/**
	 * Returns the text area used to display the agent's logging activity
	 * 
	 * @return the textArea used to display the agent's logging
	 */
	public TextArea getTextArea() {
		return textArea;
	}

	/**
	 * Write to text area.
	 *
	 * @param text the text
	 */
	void writeToTextArea(String text) {
		textArea.appendText(text);
	}

	/**
	 * Remove all the text contained in the text area
	 */
	public void clearOutput() {
		textArea.clear();
	}

	/**
	 * Sets the background color of the text area
	 * 
	 * @param bg the color to set
	 */
	public void setBackground(javafx.scene.paint.Color bg) {
		textArea.setStyle(bg.toString());
	}

}
