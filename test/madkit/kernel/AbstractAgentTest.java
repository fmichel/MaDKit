/*
 * Copyright 1997-2011 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MadKit.
 * 
 * MadKit is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * MadKit is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with MadKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.kernel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.logging.Level;

import madkit.gui.actions.MadkitAction;
import madkit.kernel.AbstractAgent.State;
import madkit.kernel.Madkit.BooleanOption;
import madkit.testing.util.agent.NormalLife;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.5
 * @version 0.9
 * 
 */
public class AbstractAgentTest {

	AbstractAgent a, b;

	@Before
	public void setup() {
		a = new AbstractAgent();
		b = new AbstractAgent();
		String[] args = { BooleanOption.desktop.toString(), "false" };
		new Madkit(args);
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
		try {
			MadkitAction.LOAD_JAR_FILE.getAction(b);
			fail("exception not thrown");
		} catch (KernelException e) {
		}
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
		AbstractAgent b = new AbstractAgent();
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
		AbstractAgent b = new AbstractAgent();
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
		assertEquals(a.getLogger().getName(), a.getLoggingName());
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
	 * Test method for {@link madkit.kernel.AbstractAgent#getAgentExecutor()}.
	 */
	@Test
	public final void testGetAgentExecutor() {
		assertNull(a.getMyLifeCycle());
	}

	/**
	 * Test method for {@link java.lang.Object#equals(java.lang.Object)}.
	 */
	@Test
	public final void testEqualsObject1() {
		AbstractAgent b = new AbstractAgent();
		assertFalse(b.equals(a));
		assertTrue(a.equals(a));
	}

}
