/*
 * Copyright 1997-2012 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MaDKit.
 * 
 * MaDKit is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * MaDKit is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MaDKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.kernel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.logging.Level;

import madkit.kernel.AbstractAgent.State;
import madkit.kernel.Madkit.Option;
import madkit.message.MessageFilter;
import madkit.message.StringMessage;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.5
 * @version 0.9
 * 
 */
public class AbstractAgentTest {

	AbstractAgent a, b;

	@SuppressWarnings("unused")
	@Before
	public void setup() {
		a = new AbstractAgent();
		b = new AbstractAgent();
		new Madkit(Option.launchAgents.toString(),AbstractAgent.class.getName());
	}

	@Test
	public void testKernelNull() {
		b.setLogLevel(Level.INFO);
		if (b.logger != null)
			b.logger.info("" + b.getKernel());
		try {
			b.launchAgent(new AbstractAgent(), 0, true);
			fail("exception not thrown");
		} catch (KernelException e) {
		}
	}
	
	@Test
	public void purgeMailbox(){
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
	public void nextMessageTest(){
		assertNull(a.nextMessage());
		Message m;
		a.receiveMessage(new Message());
		a.receiveMessage(m = new Message());
		assertNotSame(m, a.nextMessage());
		assertSame(m, a.nextMessage());
		assertNull(a.nextMessage());
	}
	
	@Test
	public void nextMessageWithFilter(){
		assertNull(a.nextMessage());
		Message m;
		a.receiveMessage(new Message());
		a.receiveMessage(new StringMessage(null));
		a.receiveMessage(new Message());
		a.receiveMessage(new StringMessage(null));
		m = a.nextMessage(new MessageFilter() {
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
	public void nextMessagesWithFilter(){
		assertNull(a.nextMessage());
		Message m;
		a.receiveMessage(new Message());
		a.receiveMessage(new StringMessage(null));
		a.receiveMessage(m = new Message());
		a.receiveMessage(m = new StringMessage(null));
		a.receiveMessage(m = new StringMessage(null));
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
	
	public void waitingAnswersTest(){
		fail("not implemented");
//		Message m;
//		a.receiveMessage(new Message());
//		a.receiveMessage(m = new Message());
//		l = a.waitAnswers(m, size, timeOutMilliSeconds)
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
		assertTrue(a.compareTo(a) == 0);
	}

	/**
	 * Test method for
	 * {@link madkit.kernel.AbstractAgent#equals(java.lang.Object)}.
	 */
	@Test
	public final void testEqualsObject() {
		assertFalse(a.equals(b));
		assertFalse(a.equals(null));
		assertTrue(a.equals(a));
		assertTrue(b.equals(b));
	}

	/**
	 * Test method for {@link madkit.kernel.AbstractAgent#getKernel()}.
	 */
	@Test
	public final void testGetKernel() {
		assertNotNull(a.getKernel());
	}

	/**
	 * Test method for {@link madkit.kernel.AbstractAgent#isAlive()}.
	 */
	@Test
	public final void testIsAlive() {
		assertFalse(a.isAlive());
	}

	/**
	 * Test method for {@link madkit.kernel.AbstractAgent#getLogger()}.
	 */
	@Test
	public final void testGetLogger() {
		assertNotSame(a.getLogger(), AgentLogger.defaultAgentLogger);
	}

	/**
	 * Test method for {@link madkit.kernel.AbstractAgent#getName()}.
	 */
	@Test
	public final void testGetName() {
		assertNotNull(a.getName());
	}

	/**
	 * Test method for
	 * {@link madkit.kernel.AbstractAgent#setName(java.lang.String)}.
	 */
	@Test
	public final void testSetName() {
		assertNotNull(a.getName());
		a.setName("test");
		assertTrue(a.getName().equals("test"));
		// assertNull
	}

	/**
	 * Test method for
	 * {@link madkit.kernel.AbstractAgent#setLogLevel(java.util.logging.Level)}.
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
		assertEquals("[AbstractAgent-"+a.hashCode()+"]", a.getLogger().getName());
		a.setLogLevel(Level.OFF);
		assertNull(a.logger);
	}

	/**
	 * Test method for {@link madkit.kernel.AbstractAgent#getRunState()}.
	 */
	@Test
	public final void testGetRunState() {
		assertTrue(a.getState() == AbstractAgent.State.NOT_LAUNCHED);
	}

	/**
	 * Test method for {@link madkit.kernel.AbstractAgent#getState()}.
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
		assertFalse(b.equals(a));
		assertTrue(a.equals(a));
	}

}
