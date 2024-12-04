package madkit.gui.fx;

import java.util.logging.ErrorManager;
import java.util.logging.Filter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

import javafx.scene.control.Button;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import madkit.gui.fx.menus.Menus;
import madkit.kernel.Agent;
import madkit.kernel.AgentLogger;
import madkit.logging.TextAreaHandler;

/**
 * A scrollable panel that prints the agent's logging activity. This component
 * is the default panel which is used for the frames created using the default
 * {@link Agent#setupGUI() implementation}.
 * 
 * @author Fabien Michel
 * @since MaDKit 6.0
 * @version 6.0
 * @see Agent#setupGUI()
 */
public class FXOutputPane extends BorderPane {

	private final TextArea textArea;

	private Handler handler;

	/**
	 * Builds the panel for the agent
	 * 
	 * @param agent
	 */
	public FXOutputPane(final Agent agent) {
		textArea = new TextArea();
		textArea.setEditable(false);
		textArea.setWrapText(true);
		MenuBar menuBar = Menus.createMenuBarFor(agent);
		setTop(menuBar);
		handler = new TextAreaHandler(textArea);
		handler.setFormatter(AgentLogger.AGENT_FILE_FORMATTER);
		agent.getLogger().addHandler(getHandler());
		setCenter(textArea);
		Button clear = new Button("clear");
		setBottom(clear);
		clear.setOnAction(action -> clearOutput());
	}

	/**
	 * @return the textArea
	 */
	public TextArea getTextArea() {
		return textArea;
	}

	public void writeToTextArea(String text) {
		textArea.appendText(text);
	}

	/**
	 * Remove all the contained text.
	 */
	public void clearOutput() {
		textArea.clear();
	}

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

	private void initHandler() {
		handler = new StreamHandler() {

			@Override
			public synchronized void publish(LogRecord record) {
				if (!isLoggable(record)) {
					return;
				}
				String msg;
				try {
					msg = getFormatter().format(record);
				} catch (Exception ex) {
					// We don't want to throw an exception here, but we
					// report the exception to any registered ErrorManager.
					reportError(null, ex, ErrorManager.FORMAT_FAILURE);
					return;
				}
				writeToTextArea(msg);

			}

			@Override
			public boolean isLoggable(LogRecord record) {
				final int levelValue = getLevel().intValue();
				if (record.getLevel().intValue() < levelValue || levelValue == Level.OFF.intValue()) {
					return false;
				}
				final Filter filter = getFilter();
				if (filter == null) {
					return true;
				}
				return filter.isLoggable(record);
			}
		};
	}

}
