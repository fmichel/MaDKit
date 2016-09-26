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
package madkit.logging;

import static madkit.kernel.AbstractAgent.ReturnCode.AGENT_CRASH;
import static madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static org.junit.Assert.assertEquals;

import java.util.logging.Level;

import org.junit.Before;
import org.junit.Test;

import madkit.kernel.AbstractAgent;
import madkit.kernel.Agent;
import madkit.kernel.JunitMadkit;
import madkit.kernel.Madkit.LevelOption;

/**
* @author Fabien Michel
*/
public class LoggingWithGUIStartingWithOFFTest extends JunitMadkit {

	@Before
	public void init(){
		addMadkitArgs(LevelOption.agentLogLevel.toString(),Level.OFF.toString());
	}
	@Test
	public void setNameTest() {
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				assertEquals(SUCCESS , launchAgent(new Agent() {
					protected void activate() {
						setName("Test");
					}
				}, true));
			}
		});
	}

	@Test
	public void setLogLevelInLife() {
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				assertEquals(SUCCESS , launchAgent(new Agent() {
					protected void activate() {
						setLogLevel(Level.ALL);
					}
				}, true));
			}
		});
	}

	@Test
	public void setLogLevelNullInLife() {
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				assertEquals(AGENT_CRASH, launchAgent(new Agent() {
					protected void activate() {
						setLogLevel(null);
					}
				}, true));
			}
		});
	}

	@Test
	public void setLogWarningLevelNullInLife() {
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				assertEquals(AGENT_CRASH, launchAgent(new Agent() {
					protected void activate() {
						getLogger().setWarningLogLevel(null);
					}
				}, true));
			}
		});
	}

	@Test
	public void setLogWarningLevelInLife() {
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				assertEquals(SUCCESS , launchAgent(new Agent() {
					protected void activate() {
						getLogger().setWarningLogLevel(Level.ALL);
					}
				}, true));
			}
		});
	}
}