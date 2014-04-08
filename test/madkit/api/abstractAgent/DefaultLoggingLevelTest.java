/*
 * Copyright 1997-2012 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MaDKit.
 * 
 * MaDKit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MaDKit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MaDKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.api.abstractAgent;

import static madkit.kernel.AbstractAgent.ReturnCode.NOT_COMMUNITY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import madkit.kernel.AbstractAgent;
import madkit.kernel.JunitMadkit;

import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.8
 * @version 0.9
 * 
 */

public class DefaultLoggingLevelTest extends JunitMadkit {

	@Test
	public void noLog() {
		addMadkitArgs("--agentLogLevel", "OFF", "--warningLogLevel", "OFF");
		launchTest(new AbstractAgent() {
			protected void activate() {
				assertNull(logger);
				assertEquals(NOT_COMMUNITY, requestRole(COMMUNITY, GROUP, ROLE));
			}
		});
		addMadkitArgs("--agentLogLevel", "OFF", "--warningLogLevel", "INFO");
		launchTest(new AbstractAgent() {
			protected void activate() {
				assertNull(logger);
				assertEquals(NOT_COMMUNITY, requestRole(COMMUNITY, GROUP, ROLE));
			}
		});
		addMadkitArgs("--agentLogLevel", "OFF", "--warningLogLevel", "ALL");
		launchTest(new AbstractAgent() {
			protected void activate() {
				assertNull(logger);
				assertEquals(NOT_COMMUNITY, requestRole(COMMUNITY, GROUP, ROLE));
			}
		});
	}

	@Test
	public void warningLogs() {
		addMadkitArgs("--agentLogLevel", "INFO", "--warningLogLevel", "OFF");
		launchTest(new AbstractAgent() {
			protected void activate() {
				assertNotNull(getLogger());
				TestHandler h = new TestHandler();
				getLogger().addHandler(h);
				assertEquals(NOT_COMMUNITY, requestRole(COMMUNITY, GROUP, ROLE));
				assertTrue(h.hasBeenUsed());
			}
		});
	}

	@Test
	public void noWarningLogs() {
		addMadkitArgs("--agentLogLevel", "INFO", "--warningLogLevel", "FINE");
		launchTest(new AbstractAgent() {
			protected void activate() {
				assertNotNull(getLogger());
				assertEquals(Level.FINE, getLogger().getWarningLogLevel());
				TestHandler h = new TestHandler();
				getLogger().addHandler(h);
				assertEquals(NOT_COMMUNITY, requestRole(COMMUNITY, GROUP, ROLE));
				assertFalse(h.hasBeenUsed());
			}
		});
	}

}

class TestHandler extends ConsoleHandler {

	private boolean logReceived = false;

	@Override
	public void publish(LogRecord record) {
		logReceived = true;
	}

	public boolean hasBeenUsed() {
		return logReceived;
	}
}