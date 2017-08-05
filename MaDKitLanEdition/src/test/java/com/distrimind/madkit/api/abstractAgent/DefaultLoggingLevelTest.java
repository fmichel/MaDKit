/*
 * MadKitLanEdition (created by Jason MAHDJOUB (jason.mahdjoub@distri-mind.fr)) Copyright (c)
 * 2015 is a fork of MadKit and MadKitGroupExtension. 
 * 
 * Copyright or © or Copr. Jason Mahdjoub, Fabien Michel, Olivier Gutknecht, Jacques Ferber (1997)
 * 
 * jason.mahdjoub@distri-mind.fr
 * fmichel@lirmm.fr
 * olg@no-distance.net
 * ferber@lirmm.fr
 * 
 * This software is a computer program whose purpose is to
 * provide a lightweight Java library for designing and simulating Multi-Agent Systems (MAS).
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 * 
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package com.distrimind.madkit.api.abstractAgent;

import static com.distrimind.madkit.kernel.AbstractAgent.ReturnCode.NOT_COMMUNITY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.junit.Test;

import com.distrimind.madkit.kernel.AbstractAgent;
import com.distrimind.madkit.kernel.JunitMadkit;
import com.distrimind.madkit.kernel.Message;

/**
 * @author Fabien Michel
 * @author Jason Mahdjoub
 * @since MaDKit 5.0.0.8
 * @since MaDKitLanEdition 1.0
 * @version 1.0
 * 
 */

public class DefaultLoggingLevelTest extends JunitMadkit {

	@Test
	public void noLog() {
		addMadkitArgs("--agentLogLevel", "OFF", "--warningLogLevel", "OFF");
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				assertNull(logger);
				assertEquals(NOT_COMMUNITY, broadcastMessage(GROUP, ROLE, new Message()));
			}
		});
		addMadkitArgs("--agentLogLevel", "OFF", "--warningLogLevel", "INFO");
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				assertNull(logger);
				assertEquals(NOT_COMMUNITY, broadcastMessage(GROUP, ROLE, new Message()));
			}
		});
		addMadkitArgs("--agentLogLevel", "OFF", "--warningLogLevel", "ALL");
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				assertNull(logger);
				assertEquals(NOT_COMMUNITY, broadcastMessage(GROUP, ROLE, new Message()));
			}
		});
	}

	@Test
	public void warningLogs() {
		addMadkitArgs("--agentLogLevel", "INFO", "--warningLogLevel", "OFF");
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				assertNotNull(getLogger());
				TestHandler h = new TestHandler();
				getLogger().addHandler(h);
				assertEquals(NOT_COMMUNITY, broadcastMessage(GROUP, ROLE, new Message()));
				assertTrue(h.hasBeenUsed());
			}
		});
	}

	@Test
	public void noWarningLogs() {
		addMadkitArgs("--agentLogLevel", "INFO", "--warningLogLevel", "FINE");
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				assertNotNull(getLogger());
				assertEquals(Level.FINE, getLogger().getWarningLogLevel());
				TestHandler h = new TestHandler();
				getLogger().addHandler(h);
				assertEquals(NOT_COMMUNITY, broadcastMessage(GROUP, ROLE, new Message()));
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