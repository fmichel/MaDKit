package madkit.logging;

import java.util.logging.LogRecord;

/**
 * @author Fabien Michel
 *
 */
public class AgentFileFormatter extends AgentFormatter {

	@Override
	protected StringBuilder getHeader(final LogRecord record) {
	    return new StringBuilder();
	}
}
