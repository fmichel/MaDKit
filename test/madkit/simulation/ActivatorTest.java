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
package madkit.simulation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import madkit.kernel.AbstractAgent;
import madkit.kernel.Activator;
import madkit.kernel.Agent;
import madkit.testing.util.agent.NormalLife;

import org.junit.Before;
import org.junit.Test;

/**
 * Has to be outside of madkit.kernel for really testing visibility
 * 
 * @author Fabien Michel
 * @since MaDKit 5.0.0.13
 * @version 0.9
 * 
 */
public class ActivatorTest {

	Activator<AbstractAgent> a;
	Agent agt;

	@Before
	public void setUp() throws Exception {
		a = new EmptyActivator("t", "t", "t");
		agt = new Agent();
	}

	@Test
	public void testToString() {
		System.err.println(a);
	}

	@Test
	public void testFindMethodOn() {
		try {
			Method m;
			m = Activator.findMethodOn(AbstractAgent.class, "activate");
			System.err.println(m);
			m = Activator.findMethodOn(NormalLife.class, "live");//protected
			System.err.println(m);
			m = Activator.findMethodOn(NormalLife.class, "privateMethod");//private
			System.err.println(m);
			m.invoke(new NormalLife());
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			fail("Oo");
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			fail("Oo");
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			fail("Oo");
		} catch (InvocationTargetException e) {
		}
	}

	@Test
	public void testIsMulticoreModeOn() {
		assertFalse(a.isMulticoreModeOn());
		a.useMulticore(1);
		assertFalse(a.isMulticoreModeOn());
		a.useMulticore(2);
		assertTrue(a.isMulticoreModeOn());
		a.useMulticore(1);
		assertFalse(a.isMulticoreModeOn());
		a.useMulticore(-1);
		assertFalse(a.isMulticoreModeOn());
	}

	@Test
	public void testSetMulticore() {
		a.useMulticore(-1);
		assertEquals(1, a.nbOfParallelTasks());
		a.useMulticore(2);
		assertEquals(2, a.nbOfParallelTasks());
	}

}
