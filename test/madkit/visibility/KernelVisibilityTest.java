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
package madkit.visibility;

import static org.junit.Assert.assertSame;
import madkit.kernel.AgentAddress;
import madkit.kernel.AgentLogger;
import madkit.kernel.KernelAddress;
import madkit.kernel.Madkit;
import madkit.kernel.Message;

import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.9
 * @version 0.9
 * 
 */
public class KernelVisibilityTest {

	@Test
	public void testKernelAddressVisibility() {
		assertSame(0, KernelAddress.class.getConstructors().length);
	}

	@Test
	public void testMadkitVisibility() {
		assertSame(1, Madkit.class.getConstructors().length);
	}

	@Test
	public void testAgentAddressVisibility() {
		assertSame(0, AgentAddress.class.getConstructors().length);
	}

	@Test
	public void testAgentLoggerVisibility() {
		assertSame(0, AgentLogger.class.getConstructors().length);
	}

	@Test
	public void testMessageVisibility() {
		assertSame(1, Message.class.getConstructors().length);
	}

}
