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
package madkit.simulation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import madkit.kernel.Activator;
import madkit.kernel.Agent;
import madkit.kernel.JunitMadKit;

import org.junit.Before;
import org.junit.Test;

/**
 * Has to be outside of madkit.kernel for really testing visibility
 * 
 * @author Fabien Michel
 * @since MadKit 5.0.0.13
 * @version 0.9
 * 
 */
public class ActivatorTest {
	
	Activator<Agent> a ;
	Agent agt;

	@Before
	public void setUp() throws Exception {
		a = new Activator<Agent>("t", "t", "t");
		agt = new Agent();
	}

	@Test
	public void testToString() {
		System.err.println(a);
	}

	@Test
	public void testExecute() {
		try {
			a.execute();
			JunitMadKit.noExceptionFailure();
		} catch (UnsupportedOperationException e) {
		}
	}

	@Test
	public void testMulticoreExecute() {
		try {
			a.multicoreExecute();
			JunitMadKit.noExceptionFailure();
		} catch (UnsupportedOperationException e) {
		}
	}

	@Test
	public void testIsMulticoreModeOn() {
		assertFalse(a.isMulticoreModeOn());
		a.setMulticore(1);
		assertFalse(a.isMulticoreModeOn());
		a.setMulticore(2);
		assertTrue(a.isMulticoreModeOn());
		a.setMulticore(1);
		assertFalse(a.isMulticoreModeOn());
		a.setMulticore(-1);
		assertFalse(a.isMulticoreModeOn());
	}

	@Test
	public void testSetMulticore() {
		a.setMulticore(-1);
		assertEquals(1, a.nbOfSimultaneousTasks());
		a.setMulticore(2);
		assertEquals(2, a.nbOfSimultaneousTasks());
	}

}
