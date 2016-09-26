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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import madkit.kernel.AbstractAgent;
import madkit.kernel.JunitMadkit;
import madkit.testing.util.agent.NoPublicConstructorAA;
import madkit.testing.util.agent.SelfLaunchAA;

import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.10
 * @version 0.9
 * 
 */

public class LaunchAgentClassTest extends JunitMadkit {

	final AbstractAgent target = new AbstractAgent() {
		protected void activate() {
			assertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
			assertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
			assertEquals(ALREADY_LAUNCHED, launchAgent(this));
		}
	};

	final AbstractAgent timeOutAgent = new AbstractAgent() {
		protected void activate() {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	};

	final AbstractAgent faulty = new AbstractAgent() {
		@SuppressWarnings("null")
		protected void activate() {
			Object o = null;
			o.toString();
		}
	};

	@Test
	public void launchFailed() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				try {
					launchAgent((String) null);
					noExceptionFailure();
				} catch (NullPointerException e) {
					throw e;
				}
			}
		}, AGENT_CRASH);
	}

	@Test
	public void launchNotFound() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				assertNull(launchAgent("a"));
			}
		});
	}

	@Test
	public void NoDefaultConstructor() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				assertNull(launchAgent(SelfLaunchAA.class.getName()));
			}
		});
	}

	@Test
	public void NoPublicConstructor() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				assertNotNull(launchAgent(NoPublicConstructorAA.class.getName()));
			}
		});
	}

	@Test
	public void NotPublic() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				assertNotNull(launchAgent(NotPublicAgent.class.getName()));
			}
		});
	}

	@Test
	public void NotAnAgentClass() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				assertNull(launchAgent(Object.class.getName()));
			}
		});
	}

}

class NotPublicAgent extends AbstractAgent{
	
}
