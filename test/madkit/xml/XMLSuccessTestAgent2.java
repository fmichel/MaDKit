/*
 * Copyright 2013 Fabien Michel
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
package madkit.xml;

import madkit.kernel.AbstractAgent;
import static org.junit.Assert.*;
import static madkit.kernel.JunitMadkit.*;
import static madkit.kernel.JunitMadkit.*;


/**
 * @author Fabien Michel
 * @since MadKit 5.0.2
 * @version 0.9
 * 
 */
public class XMLSuccessTestAgent2 extends XMLSuccessTestAgent {
	
	@Override
	protected void activate() {
		assertEquals(3, getSpeed());
		assertEquals(5, getI().intValue());
		assertEquals("blabla", getS());
		assertFalse(hasGUI());
		bucketModeRequestRole(COMMUNITY, GROUP, ROLE, null);
	}
}
