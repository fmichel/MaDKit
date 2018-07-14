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
package com.distrimind.madkit.kernel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.logging.Level;

import org.junit.Before;
import org.junit.Test;

import com.distrimind.madkit.kernel.AbstractAgent;
import com.distrimind.madkit.kernel.AgentLogger;
import com.distrimind.madkit.kernel.KernelException;
import com.distrimind.madkit.kernel.Madkit;
import com.distrimind.madkit.kernel.Message;
import com.distrimind.madkit.kernel.AbstractAgent.State;
import com.distrimind.madkit.message.MessageFilter;
import com.distrimind.madkit.message.ObjectMessage;
import com.distrimind.madkit.message.StringMessage;

/**
 * @author Fabien Michel
 * @author Jason Mahdjoub
 * @since MaDKit 5.0.0.5
 * @since MadkitLanEdition 1.0
 * @version 1.0
 * 
 */
public class AbstractAgentTest {

	AbstractAgent a, b;

	@Before
	public void setup() {
		a = new AbstractAgent();
		b = new AbstractAgent();
		new Madkit("--launchAgents", "{" + AbstractAgent.class.getName() + "}");
	}

	@Test
	public void testKernelNull() {
		b.setLogLevel(Level.INFO);
		if (b.logger != null)
			b.logger.info("" + b.getKernel());
		try {
			b.launchAgent(new AbstractAgent(), 0, true);
			fail("exception not thrown");
		} catch (KernelException ignored) {
		}
	}

	@Test
	public void purgeMailbox() {
		assertNull(a.purgeMailbox());
		Message m;
		a.receiveMessage(new Message());
		a.receiveMessage(m = new Message());
		assertEquals(m, a.purgeMailbox());
		assertNull(a.purgeMailbox());
		a.receiveMessage(m = new Message());
		a.receiveMessage(new Message());
		assertNotSame(m, a.purgeMailbox());
		assertNull(a.purgeMailbox());
	}

	@Test
	public void getLastReceivedMessage() {
		assertNull(a.purgeMailbox());
		Message m, m2;
		a.receiveMessage(m = new Message());
		a.receiveMessage(m2 = new Message());
		assertEquals(m2, a.getLastReceivedMessage());
		assertEquals(m, a.getLastReceivedMessage());
		assertNotEquals(m, a.nextMessage());
		assertNull(a.nextMessage());
	}

	@Test
	public void getLastReceivedMessageWithFilter() {
		assertNull(a.purgeMailbox());
		Message m, m2;
		a.receiveMessage(m = new ObjectMessage<>("test"));
		a.receiveMessage(m2 = new Message());
		assertEquals(m, a.getLastReceivedMessage(new MessageFilter() {

			@Override
			public boolean accept(Message om) {
				return om instanceof ObjectMessage<?>;
			}
		}));
		assertEquals(m2, a.getLastReceivedMessage());
		assertNotEquals(m, a.nextMessage());
		assertNull(a.nextMessage());
	}

	@Test
	public void nextMessageTest() {
		assertNull(a.nextMessage());
		Message m;
		a.receiveMessage(new Message());
		a.receiveMessage(m = new Message());
		assertNotSame(m, a.nextMessage());
		assertSame(m, a.nextMessage());
		assertNull(a.nextMessage());
	}

	@Test
	public void nextMessageWithFilter() {
		assertNull(a.nextMessage());
		a.receiveMessage(new Message());
		a.receiveMessage(new StringMessage(null));
		a.receiveMessage(new Message());
		a.receiveMessage(new StringMessage(null));
		a.nextMessage(new MessageFilter() {
			@Override
			public boolean accept(Message m2) {
				return m2 instanceof StringMessage;
			}
		});
		assertFalse(a.nextMessage() instanceof StringMessage);
		assertFalse(a.nextMessage() instanceof StringMessage);
		assertTrue(a.nextMessage() instanceof StringMessage);
		assertNull(a.nextMessage());
	}

	@Test
	public void nextMessagesWithFilter() {
		assertNull(a.nextMessage());
		a.receiveMessage(new Message());
		a.receiveMessage(new StringMessage(null));
		a.receiveMessage(new Message());
		a.receiveMessage(new StringMessage(null));
		a.receiveMessage(new StringMessage(null));
		List<Message> l = a.nextMessages(new MessageFilter() {
			@Override
			public boolean accept(Message m2) {
				return m2 instanceof StringMessage;
			}
		});
		assertEquals(3, l.size());
		System.err.println(l);
		assertFalse(a.nextMessage() instanceof StringMessage);
		assertFalse(a.nextMessage() instanceof StringMessage);
		assertNull(a.nextMessage());
	}

	public void waitingAnswersTest() {
		fail("not implemented");
		// Message m;
		// a.receiveMessage(new Message());
		// a.receiveMessage(m = new Message());
		// l = a.waitAnswers(m, size, timeOutMilliSeconds)
	}

	/**
	 * Test method for {@link java.lang.Object#toString()}.
	 */
	@Test
	public final void testToString() {
		System.err.println(a);
	}

	@Test
	public final void actionCreation() {
	}

	@Test
	public final void testCompareTo() {
		assertTrue(a.compareTo(b) < 0);
		assertTrue(b.compareTo(a) > 0);
		assertEquals(0, a.compareTo(a));
	}

	/**
	 * Test method for
	 * {@link com.distrimind.madkit.kernel.AbstractAgent#equals(java.lang.Object)}.
	 */
	@SuppressWarnings({"ConstantConditions", "ObjectEqualsNull", "SimplifiableJUnitAssertion"})
	@Test
	public final void testEqualsObject() {
		assertNotEquals(a, b);
		assertFalse(a.equals(null));
		assertEquals(a, a);
		assertEquals(b, b);
	}

	/**
	 * Test method for
	 * {@link com.distrimind.madkit.kernel.AbstractAgent#getKernel()}.
	 */
	@Test
	public final void testGetKernel() {
		assertNotNull(a.getKernel());
	}

	/**
	 * Test method for {@link com.distrimind.madkit.kernel.AbstractAgent#isAlive()}.
	 */
	@Test
	public final void testIsAlive() {
		assertFalse(a.isAlive());
	}

	/**
	 * Test method for
	 * {@link com.distrimind.madkit.kernel.AbstractAgent#getLogger()}.
	 */
	@Test
	public final void testGetLogger() {
		assertNotSame(a.getLogger(), AgentLogger.defaultAgentLogger);
	}

	/**
	 * Test method for {@link com.distrimind.madkit.kernel.AbstractAgent#getName()}.
	 */
	@Test
	public final void testGetName() {
		assertNotNull(a.getName());
	}

	/**
	 * Test method for
	 * {@link com.distrimind.madkit.kernel.AbstractAgent#setName(java.lang.String)}.
	 */
	@Test
	public final void testSetName() {
		assertNotNull(a.getName());
		a.setName("test");
		assertEquals("test", a.getName());
		// assertNull
	}

	/**
	 * Test method for
	 * {@link com.distrimind.madkit.kernel.AbstractAgent#setLogLevel(java.util.logging.Level)}.
	 */
	@Test
	public final void testSetLogLevel() {
		assertEquals(a.logger, AgentLogger.defaultAgentLogger);
		a.setLogLevel(Level.OFF);
		assertNull(a.logger);
		a.setLogLevel(Level.INFO);
		assertNotNull(a.logger);
		System.err.println(a.getLogger().getName());
		a.setName("new");
		System.err.println(a.getName());
		System.err.println(a.getLogger().getName());
		assertEquals("new", a.getName());
		assertEquals("[AbstractAgent-" + a.getAgentID() + "]", a.getLogger().getName());
		a.setLogLevel(Level.OFF);
		assertNull(a.logger);
	}


	@Test
	public final void testGetRunState() {
		assertSame(a.getState(), State.NOT_LAUNCHED);
	}

	/**
	 * Test method for
	 * {@link com.distrimind.madkit.kernel.AbstractAgent#getState()}.
	 */
	@Test
	public final void testGetAgentState() {
		assertEquals(State.NOT_LAUNCHED, a.getState());
	}

	/**
	 * Test method for {@link java.lang.Object#equals(java.lang.Object)}.
	 */
	@Test
	public final void testEqualsObject1() {
		assertNotEquals(b, a);
		assertEquals(a, a);
	}

}
