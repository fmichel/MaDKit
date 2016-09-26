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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import madkit.kernel.AbstractAgent;
import madkit.kernel.AbstractAgent.ReturnCode;
import madkit.kernel.AgentAddress;
import madkit.kernel.JunitMadkit;

import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.15
 * @version 0.9
 * 
 */


public class GetAgentAddressInTest extends JunitMadkit {

	@Test
	public void success() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				createDefaultCGR(this);
				assertNotNull(getAgentAddressIn(COMMUNITY, GROUP, ROLE));
			}
		});
	}

	@Test
	public void nullAfterLeaveRole() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				createDefaultCGR(this);
				AgentAddress aa = getAgentAddressIn(COMMUNITY, GROUP, ROLE);
				assertNotNull(aa);
				assertTrue(checkAgentAddress(aa));
				leaveRole(COMMUNITY, GROUP, ROLE);
				assertFalse(checkAgentAddress(aa));
				aa = getAgentAddressIn(COMMUNITY, GROUP, ROLE);
				assertNull(aa);
			}
		});
	}

	@Test
	public void nullAfterLeaveGroup() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				createDefaultCGR(this);
				AgentAddress aa = getAgentAddressIn(COMMUNITY, GROUP, ROLE);
				assertNotNull(aa);
				assertTrue(checkAgentAddress(aa));
				leaveGroup(COMMUNITY, GROUP);
				assertFalse(checkAgentAddress(aa));
				aa = getAgentAddressIn(COMMUNITY, GROUP, ROLE);
				assertNull(aa);
			}
		});
	}

	@Test
	public void nullCommunity() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				createDefaultCGR(this);
				try {
					assertNotNull(getAgentAddressIn(null, GROUP, ROLE));
					noExceptionFailure();
				} catch (NullPointerException e) {
					throw e;
				}
			}
		}, ReturnCode.AGENT_CRASH);
	}

	@Test
	public void nullGroup() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				createDefaultCGR(this);
				try {
					assertNotNull(getAgentAddressIn(COMMUNITY, null, ROLE));
					noExceptionFailure();
				} catch (NullPointerException e) {
					throw e;
				}
			}
		}, ReturnCode.AGENT_CRASH);
	}

	@Test
	public void nullRole() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				createDefaultCGR(this);
				try {
					assertNotNull(getAgentAddressIn(COMMUNITY, GROUP, null));
					noExceptionFailure();
				} catch (NullPointerException e) {
					throw e;
				}
			}
		}, ReturnCode.AGENT_CRASH);
	}

	@Test
	public void roleNotExist() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				createDefaultCGR(this);
				assertNull(getAgentAddressIn(COMMUNITY, GROUP, aa()));
			}
		}, ReturnCode.SUCCESS);
	}

	@Test
	public void roleNotHandled() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				createDefaultCGR(this);
				launchAgent(new AbstractAgent(){
					@Override
					protected void activate() {
						requestRole(COMMUNITY, GROUP, "a");
						createGroup(COMMUNITY, "a");
						requestRole(COMMUNITY, "a", "a");
					}
				});
				assertNull(getAgentAddressIn(COMMUNITY, GROUP, "a"));
				assertNull(getAgentAddressIn(COMMUNITY, "a", "a"));
			}
		}, ReturnCode.SUCCESS);
	}

	@Test
	public void groupNotExist() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				createDefaultCGR(this);
				assertNull(getAgentAddressIn(COMMUNITY, aa(), aa()));
			}
		}, ReturnCode.SUCCESS);
	}

	@Test
	public void communityNotExist() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				createDefaultCGR(this);
				assertNull(getAgentAddressIn(aa(), aa(), aa()));
			}
		}, ReturnCode.SUCCESS);
	}

}
