package madkit.logging;

import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import madkit.kernel.AgentLogger;

/**
 * @author Fabien Michel
 *
 */
public class AgentFormatter extends Formatter {

	@Override
	public String format(final LogRecord record) {
		final Level lvl = record.getLevel();
		if (lvl.equals(AgentLogger.TALK)) {
			return record.getMessage();
		}
		return getHeader(record)
				.append('{')
				.append(lvl.getLocalizedName())
				.append("} ")
				.append(record.getMessage())
				.append('\n')
				.toString();
	}

	protected StringBuilder getHeader(final LogRecord record) {
		return new StringBuilder(record.getLoggerName());
	}

}
