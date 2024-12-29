package madkit.gui;

import java.util.logging.Handler;

import javafx.scene.control.Button;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import madkit.kernel.Agent;

/**
 * The `FXOutputPane` class extends the JavaFX {@link BorderPane} class to
 * provide a panel that displays an agent's logging activity. This component is
 * the default panel used for the frames created using the default
 * {@link Agent#setupDefaultGUI() implementation}.

 * @version 6.0
 * @see Agent#setupDefaultGUI()
 */
public class FXOutputPane extends BorderPane {

	private final TextArea textArea;

	private Handler handler;

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
		clear.setOnAction(action -> textArea.clear());
	}

	/**
	 * Returns the text area used to display the agent's logging activity
	 * 
	 * @return the textArea used to display the agent's logging
	 */
	public TextArea getTextArea() {
		return textArea;
	}

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

	/**
	 * Returns the handler which has been created for the agent's GUI
	 * 
	 * @return the handler associated with this panel
	 */
	public Handler getHandler() {
		return handler;
	}
}
