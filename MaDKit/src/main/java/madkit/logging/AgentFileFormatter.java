package madkit.logging;

import java.util.logging.LogRecord;

/**
 * This class extends the AgentFormatter to provide a specific file formatter
 * for agents. It overrides the getHeader method to return an empty
 * StringBuilder.
 * 
 * @see AgentFormatter
 * @see LogRecord
 * 
 *      author Fabien Michel
 */
public class AgentFileFormatter extends AgentFormatter {

	/**
	 * Returns an empty StringBuilder as the header for the log record.
	 *
	 * @param record the log record for which the header is to be returned
	 * @return an empty StringBuilder
	 */
	@Override
	protected StringBuilder getHeader(final LogRecord record) {
		return new StringBuilder();
	}
}