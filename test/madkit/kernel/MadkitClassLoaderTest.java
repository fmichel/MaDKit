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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Arrays;

import madkit.kernel.AbstractAgent.ReturnCode;
import madkit.kernel.Madkit.LevelOption;

import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.10
 * @version 0.9
 * 
 */
@SuppressWarnings("serial")
public class MadkitClassLoaderTest extends JunitMadkit {

	@Test
	public void scanFolderForMDKConfigFileAgentClassesTest(){
		addMadkitArgs(LevelOption.kernelLogLevel.toString(),"ALL");
			launchTest(new AbstractAgent() {
				@Override
				protected void activate() {
						assertFalse(MadkitClassLoader.getAllAgentClasses().isEmpty());
						System.err.println(MadkitClassLoader.getMDKFiles());
						//ugly : inside and outside Eclipse
						assertTrue(1 == MadkitClassLoader.getMDKFiles().size() || 5 == MadkitClassLoader.getMDKFiles().size());
						System.err.println(MadkitClassLoader.getXMLConfigurations());
						assertTrue(3 == MadkitClassLoader.getXMLConfigurations().size() || 16 == MadkitClassLoader.getXMLConfigurations().size());
			}});
	}

	@Test
	public void testLoadJarsFromPath() {
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				try {
					MadkitClassLoader.getLoader().loadClass("madkit.pingpong.PingPong");
					fail("Not thrown");
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				System.err.println(System.getProperty("user.dir"));
				 MadkitClassLoader.loadJarsFromDirectory(System.getProperty("user.dir") + File.separator + "test");
				try {
					assertNotNull(MadkitClassLoader.getLoader().loadClass("madkit.pingpong.PingPong"));
				} catch (ClassNotFoundException e) {
					fail(e.getMessage());
				}
			}
		});
	}
	
	@Test
	public void testURLs() {
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
					@SuppressWarnings("resource")
					MadkitClassLoader mcl = MadkitClassLoader.getLoader();
					try {
						MadkitClassLoader.loadUrl(new File(".").toURI().toURL());
						int n = mcl.getURLs().length;
						MadkitClassLoader.loadUrl(new File(".").toURI().toURL());
						assertEquals(n,mcl.getURLs().length);
						System.err.println(Arrays.deepToString(mcl.getURLs()));
					} catch (MalformedURLException e) {
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
					assertNotNull(MadkitClassLoader.getLoader().loadClass("madkit.kernel.AbstractAgent"));
				} catch (ClassNotFoundException e) {
					fail(e.getMessage()); 
				}
				try {
					MadkitClassLoader.getLoader().loadClass(null);
					fail("Not thrown"); 
				} catch (ClassNotFoundException e) {
					fail("Not the one");
					e.printStackTrace();
				} catch (NullPointerException e) {
					throw e;
				}
			}
		}, ReturnCode.AGENT_CRASH);
	}

}
