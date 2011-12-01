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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Arrays;

import madkit.kernel.AbstractAgent.ReturnCode;

import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.10
 * @version 0.9
 * 
 */
@SuppressWarnings("serial")
public class MadkitClassLoaderTest extends JunitMadKit {

	@Test
	public void testLoadJarsFromPath() {
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				try {
					getMadkitClassLoader().loadClass("madkit.pingpong.PingPong");
					fail("Not thrown"); // TODO
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				MadkitClassLoader mcl = (MadkitClassLoader) getMadkitClassLoader();
				System.err.println(System.getProperty("user.dir"));
				mcl.loadJarsFromPath(System.getProperty("user.dir") + File.separator + "demos");
				try {
					assertNotNull(getMadkitClassLoader().loadClass("madkit.pingpong.PingPong"));
				} catch (ClassNotFoundException e) {
					fail(e.getMessage()); // TODO
				}
			}
		});
	}
	
	@Test
	public void testURLs() {
		launchTest(new AbstractAgent() {
			@SuppressWarnings("deprecation")
			@Override
			protected void activate() {
					MadkitClassLoader mcl = (MadkitClassLoader) getMadkitClassLoader();
					try {
						mcl.addToClasspath(new File(".").toURL());
						int n = mcl.getURLs().length;
						mcl.addToClasspath(new File(".").toURL());
						assertEquals(n,mcl.getURLs().length);
						System.err.println(Arrays.deepToString(mcl.getURLs()));
					} catch (MalformedURLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		}});
	}
	
	

	@Test
	public void testLoadClassString() {
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				try {
					assertNotNull(getMadkitClassLoader().loadClass("madkit.kernel.AbstractAgent"));
				} catch (ClassNotFoundException e) {
					fail(e.getMessage()); // TODO
				}
				try {
					getMadkitClassLoader().loadClass(null);
					fail("Not thrown"); // TODO
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (NullPointerException e) {
					throw e;
				}
			}
		}, ReturnCode.AGENT_CRASH);
	}

}
