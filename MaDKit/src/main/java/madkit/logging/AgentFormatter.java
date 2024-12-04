package madkit.logging;

import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import madkit.kernel.AgentLogger;

/**
 * This class extends the Formatter class to provide a custom log formatter for
 * agents. It formats log records based on their level and includes a header
 * with the logger name.
 * 
 * @see Formatter
 * @see LogRecord
 * @see Level
 * @see AgentLogger
 * 
 * @author Fabien Michel
 */
public class AgentFormatter extends Formatter {

	/**
	 * Formats the given log record. If the log level is AgentLogger.TALK, only the
	 * message is returned. Otherwise, a formatted string with the log level and
	 * message is returned.
	 *
	 * @param record the log record to be formatted
	 * @return the formatted log record as a string
	 */
	@Override
	public String format(final LogRecord record) {
		final Level lvl = record.getLevel();
		if (lvl.equals(AgentLogger.TALK)) {
			return record.getMessage();
		}
		return getHeader(record).append('{').append(lvl.getLocalizedName()).append("} ").append(record.getMessage())
				.append('\n').toString();
	}

	/**
	 * Returns a StringBuilder containing the logger name as the header for the log
	 * record.
	 *
	 * @param record the log record for which the header is to be returned
	 * @return a StringBuilder containing the logger name
	 */
	protected StringBuilder getHeader(final LogRecord record) {
		return new StringBuilder(record.getLoggerName());
	}
}