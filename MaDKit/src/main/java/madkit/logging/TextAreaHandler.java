package madkit.logging;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

import javafx.scene.control.TextArea;

/**
 * This class extends the Handler class to provide a custom log handler that
 * outputs log records to a JavaFX TextArea. It overrides the publish, flush,
 * and close methods to handle log records appropriately.
 *
 * @see Handler
 * @see LogRecord
 * @see TextArea
 *
 * @author Fabien Michel
 */
public class TextAreaHandler extends Handler {

	/** The TextArea to which log records will be appended. */
	private final TextArea area;

	/**
	 * Constructs a new TextAreaHandler with the specified TextArea.
	 *
	 * @param area the TextArea to which log records will be appended
	 */
	public TextAreaHandler(TextArea area) {
		this.area = area;
	}

	/**
	 * Publishes the given log record by appending it to the TextArea if it is
	 * loggable.
	 *
	 * @param log the log record to be published
	 */
	@Override
	public void publish(LogRecord log) {
		if (!isLoggable(log)) {
			return;
		}
		area.appendText(getFormatter().format(log));
	}

	public void flush() {
	}

	/**
	 * Closes the handler and releases any associated resources. This implementation
	 * does nothing.
	 *
	 * @throws SecurityException if a security manager exists and if the caller does
	 *                           not have LoggingPermission("control")
	 */
	@Override
	public void close() throws SecurityException {
	}
}