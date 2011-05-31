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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import madkit.kernel.AbstractAgent.ReturnCode;

import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.10
 * @version 0.9
 * 
 */
public class MadkitClassLoaderTest extends JunitMadKit{

	@Test
	public void testReloadClass() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testLoadJarsFromPath() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testAddJar() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testLoadClassString() {
		launchTest(new AbstractAgent(){
			@Override
			protected void activate() {
				try {
					getMadkitClassLoader().loadClass(null);
					fail("Not thrown"); // TODO
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (NullPointerException e) {
					throw e;
				}
				try {
					assertNotNull(getMadkitClassLoader().loadClass("madkit.kernel.AbstractAgent"));
				} catch (ClassNotFoundException e) {
					fail(e.getMessage()); // TODO
				} 
			}
		},ReturnCode.AGENT_CRASH);
	}

}
