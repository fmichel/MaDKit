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
package madkit.api.abstractAgent;

import static madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static madkit.kernel.AbstractAgent.ReturnCode.TIMEOUT;
import static org.junit.Assert.assertEquals;

import java.util.logging.Level;

import madkit.kernel.AbstractAgent;
import madkit.kernel.JunitMadkit;
import madkit.testing.util.agent.UnstopableAgent;

import org.junit.Test;

/**
* @author Fabien Michel
*/
public class KillUnstoppableAgentTest extends JunitMadkit {

	@Test
	public void killUnstoppableInActivate() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				AbstractAgent unstopableAgent = new UnstopableAgent(true);
				unstopableAgent.setLogLevel(Level.ALL);
				startTimer();
				assertEquals(TIMEOUT, launchAgent(unstopableAgent, 1));
				stopTimer("launch time out ");
				assertEquals(SUCCESS, killAgent(unstopableAgent, 1));
				assertAgentIsTerminated(unstopableAgent);
			}
		});
	}

	@Test
	public void brutalKillUnstoppableInActivateAndLive() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				AbstractAgent unstopableAgent = new UnstopableAgent(true, true, false);
				assertEquals(TIMEOUT, launchAgent(unstopableAgent, 1));
				if (logger != null)
					logger.info(unstopableAgent.getState().toString());
				assertEquals(SUCCESS, killAgent(unstopableAgent, 0));
				assertAgentIsTerminated(unstopableAgent);
				pause(1000);
			}
		});
	}

	@Test
	public void brutalKillUnstoppableInActivateAndLiveAndEnd() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				AbstractAgent unstopableAgent = new UnstopableAgent(true, true, true);
				assertEquals(TIMEOUT, launchAgent(unstopableAgent, 1));
				if (logger != null)
					logger.info(unstopableAgent.getState().toString());
				assertEquals(TIMEOUT, killAgent(unstopableAgent, 1));
				assertAgentIsTerminated(unstopableAgent);
				pause(1000);
			}
		});
	}

	@Test
	public void brutalKillUnstoppableUsingSelfRef() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				AbstractAgent unstopableAgent = new UnstopableAgent(true);
				assertEquals(TIMEOUT, launchAgent(unstopableAgent, 1));
				assertEquals(SUCCESS, unstopableAgent.killAgent(unstopableAgent, 1));
				assertAgentIsTerminated(unstopableAgent);
				pause(1000);
				unstopableAgent = new UnstopableAgent(true, false, true);
				assertEquals(TIMEOUT, launchAgent(unstopableAgent, 1));
				assertEquals(TIMEOUT, unstopableAgent.killAgent(unstopableAgent, 1));
				assertAgentIsTerminated(unstopableAgent);
				pause(1000);
			}
		});
	}

}
