/*******************************************************************************
 * MaDKit - Multi-agent systems Development Kit 
 * 
 * Copyright (c) 1998-2025 Fabien Michel, Olivier Gutknecht, Jacques Ferber...
 * 
 * This software is a computer program whose purpose is to
 * provide a lightweight Java API for developing and simulating 
 * Multi-Agent Systems (MAS) using an organizational perspective.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.You can use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty and the software's author, the holder of the
 * economic rights, and the successive licensors have only limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading, using, modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean that it is complicated to manipulate, and that also
 * therefore means that it is reserved for developers and experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and, more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 *******************************************************************************/
package madkit.logging;

import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import madkit.kernel.AgentLogger;

/**
 * This class extends the Formatter class to provide a custom log formatter for agents. It
 * formats log records based on their level and includes a header with the logger name.
 * 
 * @see Formatter
 * @see LogRecord
 * @see Level
 * @see AgentLogger
 */
public class AgentFormatter extends Formatter {

	/**
	 * Formats the given log record. If the log level is AgentLogger.TALK, only the message is
	 * returned. Otherwise, a formatted string with the log level and message is returned.
	 *
	 * @param record the log record to be formatted
	 * @return the formatted log record as a string
	 */
	@Override
	public String format(LogRecord record) {// NOSONAR
		final Level lvl = record.getLevel();
		if (lvl.equals(AgentLogger.TALK)) {
			return record.getMessage();
		}
		return getHeader(record).append('{').append(lvl.getLocalizedName()).append("} ").append(record.getMessage())
				.append('\n').toString();
	}

	/**
	 * Returns a StringBuilder containing the logger name as the header for the log record.
	 *
	 * @param record the log record for which the header is to be returned
	 * @return a StringBuilder containing the logger name
	 */
	protected StringBuilder getHeader(LogRecord record) {// NOSONAR respect the signature
		return new StringBuilder(record.getLoggerName());
	}
}