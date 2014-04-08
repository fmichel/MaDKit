/*
 * Copyright 1997-2013 Fabien Michel, Olivier Gutknecht, Jacques Ferber
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
package madkit.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.logging.Level;

import javax.xml.parsers.ParserConfigurationException;

import madkit.kernel.AbstractAgent;
import madkit.kernel.JunitMadkit;
import madkit.kernel.Madkit.LevelOption;
import madkit.kernel.Madkit.Option;
import madkit.util.XMLUtilities;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.1
 * @version 0.9
 * 
 */

public class XmlFeaturesTest extends JunitMadkit {

	@Test
	public void launchNodeSuccess() {
		addMadkitArgs(LevelOption.kernelLogLevel.toString(),"FINER");
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				Document dom;
				try {
					dom = XMLUtilities.getDOM("madkit/xml/success.xml");
					NodeList nodes = dom.getElementsByTagName(XMLUtilities.AGENT);
					for (int i = 0; i < nodes.getLength(); i++) {
						assertEquals(ReturnCode.SUCCESS,launchNode(nodes.item(i)));
					}
					nodes = dom.getElementsByTagName("Agents");
				} catch (SAXException | IOException
						| ParserConfigurationException e) {
					fail();
				}
			}
		});
	}

	@Test
	public void launchNodeSuccessFromConfig() {
		addMadkitArgs(Option.configFile.toString(),"madkit/xml/success.xml");
		addMadkitArgs(LevelOption.kernelLogLevel.toString(),"ALL");
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				createGroup(COMMUNITY, GROUP);
				int i = 0;
				final int expected = 3;
				while((getAgentsWithRole(COMMUNITY, GROUP, ROLE, false) == null || getAgentsWithRole(COMMUNITY, GROUP, ROLE, false).size() != expected) && i ++ < 10)
					pause(200);
				assertEquals(expected, getAgentsWithRole(COMMUNITY, GROUP, ROLE).size());
			}
		});
	}

	@Test
	public void launchNodeFailureFromConfig() {
		addMadkitArgs(Option.configFile.toString(),"madkit/xml/failure.xml");
//		addMadkitArgs(LevelOption.kernelLogLevel.toString(),"ALL");
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
					pause(1000);
					assertEquals("yes",getMadkitProperty("test"));
					assertKernelIsAlive();
			}
		});
	}

	@Test
	public void launchNodeBenchFromConfig() {
		addMadkitArgs(Option.configFile.toString(),"madkit/xml/bench.xml");
		addMadkitArgs(LevelOption.agentLogLevel.toString(),"INFO");
		addMadkitArgs(LevelOption.kernelLogLevel.toString(),"FINER");
	launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				final int expected = 100100;
				int i = 0;
				while((getAgentsWithRole(COMMUNITY, GROUP, ROLE, false) == null || getAgentsWithRole(COMMUNITY, GROUP, ROLE, false).size() != expected) && i ++ < 20)
					pause(200);
				assertEquals(expected, getAgentsWithRole(COMMUNITY, GROUP, ROLE).size());
				assertKernelIsAlive();
			}
		});
	}


	@Test
	public void configLoadSuccess() {
		addMadkitArgs(LevelOption.kernelLogLevel.toString(),"FINER");
		addMadkitArgs(Option.configFile.toString(),"madkit/xml/success.xml");
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				assertEquals("yes",getMadkitProperty("test"));
			}
		});
	}

	@Test
	public void benchTest() {
		addMadkitArgs(LevelOption.kernelLogLevel.toString(),"FINER");
		addMadkitArgs(LevelOption.agentLogLevel.toString(),"INFO");
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				setLogLevel(Level.INFO);
				createGroup(COMMUNITY, GROUP);
				Document dom;
				try {
					dom = XMLUtilities.getDOM("madkit/xml/bench.xml");
					NodeList nodes = dom.getElementsByTagName(XMLUtilities.AGENT);
					for (int i = 0; i < nodes.getLength(); i++) {
						assertEquals(ReturnCode.SUCCESS,launchNode(nodes.item(i)));
					}
					assertEquals(100100,getAgentsWithRole(COMMUNITY,GROUP,ROLE).size());
				} catch (SAXException | IOException
						| ParserConfigurationException e) {
					e.printStackTrace();
					fail();
				}
			}
		});
	}

	@Test
	public void launchNodeFail() {
//	addMadkitArgs(LevelOption.kernelLogLevel.toString(),"ALL");
	launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				Document dom;
				try {
					dom = XMLUtilities.getDOM("madkit/xml/failure.xml");
					NodeList nodes = dom.getElementsByTagName(XMLUtilities.AGENT);
					for (int i = 0; i < nodes.getLength(); i++) {
						assertEquals(ReturnCode.SEVERE,launchNode(nodes.item(i)));
					}
				} catch (SAXException | IOException
						| ParserConfigurationException e) {
					e.printStackTrace();
					fail();
				}
				assertKernelIsAlive();
			}
		});
	}

	@Test
	public void noMadkitOptions() {
	addMadkitArgs(Option.configFile.toString(),"madkit/xml/failure.xml");
	addMadkitArgs(LevelOption.kernelLogLevel.toString(),"ALL");
	launchTest(new AbstractAgent(){
		@Override
		protected void activate() {
			assertKernelIsAlive();
		}
	});
	}
}
