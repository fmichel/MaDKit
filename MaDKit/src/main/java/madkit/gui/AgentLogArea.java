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

import java.util.logging.Handler;

import javafx.scene.control.TextArea;
import madkit.kernel.Agent;
import madkit.kernel.AgentLogger;
import madkit.logging.TextAreaHandler;

/**
 * This class provides a text area for displaying the log of an agent. The log is
 * displayed in the same format as in the agent's log file.
 */
public class AgentLogArea extends TextArea {

	private TextAreaHandler handler;

	/**
	 * Constructs a text area for displaying the log of the specified
	 * 
	 * @param agent the agent whose log is displayed in this text area
	 */
	public AgentLogArea(Agent agent) {
		super.setEditable(false);
		super.setWrapText(true);
		handler = new TextAreaHandler(this);
		handler.setFormatter(AgentLogger.AGENT_FILE_FORMATTER);
		agent.getLogger().addHandler(handler);

	}

	/**
	 * Gets the handler that writes log records to this text area.
	 * 
	 * @return the handler for this text area
	 */
	public Handler getHandler() {
		return handler;
	}
}
