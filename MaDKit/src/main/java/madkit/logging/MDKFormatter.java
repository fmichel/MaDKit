package madkit.logging;

import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * This class extends the Formatter class to provide a custom log formatter for
 * MDK. It includes a static header and formats log records with their level and
 * message.
 *
 * @see Formatter
 * @see LogRecord
 * @see Handler
 *
 * @author Fabien Michel
 */
public class MDKFormatter extends Formatter {

	/** Static header for the log records. */
	static final String HEADER = "[MDK] ";

	/**
	 * Formats the given log record. The format includes the log level and the
	 * message.
	 *
	 * @param record the log record to be formatted
	 * @return the formatted log record as a string
	 */
	@Override
	public String format(final LogRecord record) {
		return new StringBuilder(record.getLevel().getLocalizedName()).append(" : ").append(record.getMessage())
				.append('\n').toString();
	}

	/**
	 * Returns the static header for the log records.
	 *
	 * @param h the handler for which the header is to be returned
	 * @return the static header as a string
	 */
	@Override
	public String getHead(Handler h) {
		return HEADER;
	}
}
