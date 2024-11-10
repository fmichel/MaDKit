package madkit.logging;

import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * @author Fabien Michel
 *
 */

public class MDKFormatter extends Formatter {
	
	final static String HEADER = "[MDK] ";

	@Override
    public String format(final LogRecord record) {
    	return new StringBuilder(record.getLevel().getLocalizedName()).append(" : ").append(record.getMessage()).append('\n').toString();
    }
	
	@Override
	public String getHead(Handler h) {
		return HEADER;
	}

}