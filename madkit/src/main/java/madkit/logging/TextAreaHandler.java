package madkit.logging;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

import javafx.scene.control.TextArea;

/**
 * @author Fabien Michel
 *
 */
public class TextAreaHandler extends Handler {
	
	final private TextArea area;
	
	/**
	 * @param area
	 */
	public TextAreaHandler(TextArea area) {
		this.area = area;
	}

	@Override
	public void publish(LogRecord record) {
		if (!isLoggable(record)) {
			return;
		}
		area.appendText(getFormatter().format(record));
	}

	@Override
	public void flush() {
	}

	@Override
	public void close() throws SecurityException {
	}

}
