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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import madkit.agr.DefaultMaDKitRoles;
import madkit.kernel.AbstractAgent;
import madkit.kernel.JunitMadkit;

import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.7
 * @version 0.9
 * 
 */

public class GetAgentsWithRoleTest extends JunitMadkit {

	final AbstractAgent target = new AbstractAgent() {
		protected void activate() {
			assertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
		}
	};

	@Test
	public void nullArgs() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				try {
					getAgentsWithRole(null, null, null);
					noExceptionFailure();
				} catch (NullPointerException e) {
					// e.printStackTrace();
				}
				try {
					assertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
					assertNull(getAgentsWithRole(COMMUNITY, null, null));
					noExceptionFailure();
				} catch (NullPointerException e) {
					// e.printStackTrace();
				}
				try {
					assertNull(getAgentsWithRole(COMMUNITY, GROUP, null));
					noExceptionFailure();
				} catch (NullPointerException e) {
					// e.printStackTrace();
				}
				try {
					assertNull(getAgentsWithRole(null, GROUP, ROLE));
					noExceptionFailure();
				} catch (NullPointerException e) {
					// e.printStackTrace();
				}
				try {
					assertNull(getAgentsWithRole(null, null, ROLE));
					noExceptionFailure();
				} catch (NullPointerException e) {
					// e.printStackTrace();
				}
				try {
					assertNull(getAgentsWithRole(COMMUNITY, null, ROLE));
					noExceptionFailure();
				} catch (NullPointerException e) {
					// e.printStackTrace();
				}
			}
		});
	}

	@Test
	public void returnNull() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				launchAgent(target);
				assertEquals(SUCCESS, target.requestRole(COMMUNITY, GROUP, ROLE));
				assertNull(getAgentsWithRole(aa(), GROUP, ROLE));
				assertNull(getAgentsWithRole(COMMUNITY, aa(), ROLE));
				assertNull(getAgentsWithRole(COMMUNITY, GROUP, aa()));
				assertNotNull(getAgentsWithRole(COMMUNITY, GROUP, ROLE));
				assertEquals(SUCCESS, target.leaveRole(COMMUNITY, GROUP, ROLE));
				assertNull(getAgentsWithRole(COMMUNITY, GROUP, ROLE));
			}
		});
	}

	@Test
	public void returnSuccess() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				assertNull(getAgentsWithRole(COMMUNITY, GROUP, ROLE));
				launchAgent(target);
				assertEquals(SUCCESS, target.requestRole(COMMUNITY, GROUP, ROLE));
				assertNotNull(getAgentsWithRole(COMMUNITY, GROUP, ROLE));
				assertEquals(1, getAgentsWithRole(COMMUNITY, GROUP, ROLE).size());
				launchAgent(new AbstractAgent() {
					protected void activate() {
						assertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
					}
				});
				assertNotNull(getAgentsWithRole(COMMUNITY, GROUP, ROLE));
				assertEquals(2, getAgentsWithRole(COMMUNITY, GROUP, ROLE).size());
			}
		});
	}

	@Test
	public void returnNotNullOnManagerRole() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				launchAgent(target);
				assertNotNull(getAgentsWithRole(COMMUNITY, GROUP, DefaultMaDKitRoles.GROUP_MANAGER_ROLE));
				assertEquals(1, getAgentsWithRole(COMMUNITY, GROUP, DefaultMaDKitRoles.GROUP_MANAGER_ROLE).size());
			}
		});
	}

	@Test
	public void returnNullOnManagerRole() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				assertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
				assertNull(getAgentsWithRole(COMMUNITY, GROUP, DefaultMaDKitRoles.GROUP_MANAGER_ROLE));
			}
		});
	}
}
