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

import static madkit.kernel.AbstractAgent.ReturnCode.AGENT_CRASH;
import static madkit.kernel.AbstractAgent.ReturnCode.ALREADY_LAUNCHED;
import static madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static madkit.kernel.AbstractAgent.ReturnCode.TIMEOUT;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import madkit.kernel.AbstractAgent;
import madkit.kernel.Agent;
import madkit.kernel.JunitMadkit;
import madkit.testing.util.agent.SelfLaunch;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.5
 * @version 0.9
 * 
 */

public class LaunchAgentTest extends JunitMadkit {

    final AbstractAgent target = new Agent() {
	protected void activate() {
	    assertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
	    assertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
	    assertEquals(ALREADY_LAUNCHED, launchAgent(this));
	}
    };

    final AbstractAgent timeOutAgent = new Agent() {
	protected void activate() {
	    pause(2000);
	}
    };

    final AbstractAgent faulty = new Agent() {
	@SuppressWarnings("null")
	protected void activate() {
	    Object o = null;
	    o.toString();
	}
    };

    @Test
    public void returnSuccessAndAlreadyLaunch() {
	launchTestV2(new AbstractAgent() {
	    protected void activate() {
		assertEquals(SUCCESS, launchAgent(target));
		assertEquals(ALREADY_LAUNCHED, launchAgent(target));
		assertEquals(ALREADY_LAUNCHED, launchAgent(this));
	    }
	});
    }

    @Test
    public void returnTimeOut() {
	launchTestV2(new AbstractAgent() {
	    protected void activate() {
		assertEquals(TIMEOUT, launchAgent(timeOutAgent, 1));
		assertEquals(ALREADY_LAUNCHED, launchAgent(timeOutAgent));
	    }
	});
    }

    @Test
    public void returnAleradyLaunch() {
	launchTestV2(new AbstractAgent() {
	    protected void activate() {
		assertEquals(TIMEOUT, launchAgent(timeOutAgent, 1));
		assertEquals(ALREADY_LAUNCHED, launchAgent(timeOutAgent));
	    }
	});
    }

    @Test
    public void killLauncher() {
	launchTestV2(new AbstractAgent() {
	    protected void activate() {
		AbstractAgent a;
		launchAgent(a = new AbstractAgent() {
		    @Override
		    protected void activate() {
			assertEquals(TIMEOUT, launchAgent(timeOutAgent, 1));
		    }
		}, 1);
		killAgent(a);
		assertAgentIsTerminated(a);
	    }
	}, true);
	pause(1000);
    }

    @Test
    public void returnAgentCrash() {
	launchTestV2(new AbstractAgent() {
	    protected void activate() {
		assertEquals(AGENT_CRASH, launchAgent(faulty, 1));
		assertEquals(ALREADY_LAUNCHED, launchAgent(faulty));
	    }
	});
    }

    @Test
    public void selfLaunching() {
	addMadkitArgs("--kernelLogLevel", "ALL");
	launchTestV2(new AbstractAgent() {
	    protected void activate() {
		SelfLaunch a = new SelfLaunch(true);
		assertEquals(SUCCESS, launchAgent(a, 1));
		a = new SelfLaunch(false, true);
		assertEquals(SUCCESS, launchAgent(a, 1));
		a = new SelfLaunch(false, false, true);
		assertEquals(SUCCESS, launchAgent(a, 1));
	    }
	});
    }

    @Test
    public void nullArgs() {
	addMadkitArgs("--kernelLogLevel", "ALL");
	launchTestV2(new AbstractAgent() {
	    protected void activate() {
		assertEquals(TIMEOUT, launchAgent(new AbstractAgent(), -1, true));
	    }
	});
    }

}
