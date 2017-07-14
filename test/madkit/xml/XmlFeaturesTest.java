/*
 * Copyright or © or Copr. Fabien Michel, Olivier Gutknecht, Jacques Ferber (1997)

fmichel@lirmm.fr
olg@no-distance.net
ferber@lirmm.fr

This software is a computer program whose purpose is to 
provide a lightweight Java library for designing and simulating Multi-Agent Systems (MAS).

This software is governed by the CeCILL-C license under French law and
abiding by the rules of distribution of free software.  You can  use, 
modify and/ or redistribute the software under the terms of the CeCILL-C
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info". 

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability. 

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or 
data to be ensured and,  more generally, to use and operate it in the 
same conditions as regards security. 

The fact that you are presently reading this means that you have had
knowledge of the CeCILL-C license and that you accept its terms.
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
				getLogger().setLevel(Level.INFO);
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
