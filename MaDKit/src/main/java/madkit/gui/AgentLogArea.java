package madkit.gui;

import java.util.logging.Handler;

import javafx.scene.control.TextArea;
import madkit.kernel.Agent;
import madkit.kernel.AgentLogger;
import madkit.logging.TextAreaHandler;

/**
 * This class provides a text area for displaying the log of an agent. The log
 * is displayed in the same format as in the agent's log file. 
 */
public class AgentLogArea extends TextArea {

	private TextAreaHandler handler;

	/**
	 * Constructs a text area for displaying the log of the specified
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
	 * @return the handler for this text area
	 */
	public Handler getHandler() {
		return handler;
	}

}
