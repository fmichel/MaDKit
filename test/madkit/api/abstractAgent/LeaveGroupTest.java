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

import static madkit.kernel.AbstractAgent.ReturnCode.ALREADY_GROUP;
import static madkit.kernel.AbstractAgent.ReturnCode.NOT_COMMUNITY;
import static madkit.kernel.AbstractAgent.ReturnCode.NOT_GROUP;
import static madkit.kernel.AbstractAgent.ReturnCode.NOT_IN_GROUP;
import static madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import madkit.kernel.AbstractAgent;
import madkit.kernel.JunitMadkit;

import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.7
 * @version 0.9
 * 
 */

public class LeaveGroupTest extends JunitMadkit {

	final AbstractAgent target = new AbstractAgent() {
		protected void activate() {
			assertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
		}
	};

	@Test
	public void nullArgs() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				assertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
				try {
					assertEquals(NOT_COMMUNITY, leaveGroup(null, null));
					noExceptionFailure();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
				try {
					assertEquals(NOT_COMMUNITY, leaveGroup(COMMUNITY, null));
					noExceptionFailure();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
				try {
					assertEquals(NOT_COMMUNITY, leaveGroup(null, GROUP));
					noExceptionFailure();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
				assertEquals(NOT_COMMUNITY, leaveGroup(aa(), aa()));
			}
		});
	}

	@Test
	public void notInGroup() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				launchAgent(target);
				assertTrue(isCommunity(COMMUNITY));
				assertTrue(isGroup(COMMUNITY, GROUP));
				assertEquals(NOT_IN_GROUP, leaveGroup(COMMUNITY, GROUP));
			}
		});
	}

	@Test
	public void notGroupNotCommunity() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				assertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
				assertEquals(NOT_GROUP, leaveGroup(COMMUNITY, aa()));
				assertEquals(NOT_COMMUNITY, leaveGroup(aa(), GROUP));
			}
		});
	}

	@Test
	public void leaveGroup() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				assertFalse(isGroup(COMMUNITY, GROUP));
				assertEquals(NOT_COMMUNITY, leaveGroup(COMMUNITY, GROUP));
				assertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
				assertTrue(isGroup(COMMUNITY, GROUP));
				assertEquals(SUCCESS, leaveGroup(COMMUNITY, GROUP));
				assertFalse(isCommunity(COMMUNITY));
				assertFalse(isGroup(COMMUNITY, GROUP));

				// second run
				assertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
				assertTrue(isGroup(COMMUNITY, GROUP));
				assertEquals(SUCCESS, leaveGroup(COMMUNITY, GROUP));
				assertFalse(isCommunity(COMMUNITY));
				assertFalse(isGroup(COMMUNITY, GROUP));
			}
		});
	}

	@Test
	public void killBeforeLeaveGroup() {

		launchTest(new AbstractAgent() {
			protected void activate() {
				assertFalse(isCommunity(COMMUNITY));
				launchAgent(target);
				assertEquals(ALREADY_GROUP, createGroup(COMMUNITY, GROUP));
				assertEquals(SUCCESS, target.leaveGroup(COMMUNITY, GROUP));
				assertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
				assertEquals(NOT_IN_GROUP, target.leaveGroup(COMMUNITY, GROUP));
				assertEquals(SUCCESS, leaveGroup(COMMUNITY, GROUP));
				assertEquals(SUCCESS, target.createGroup(COMMUNITY, GROUP));
				killAgent(target);
				assertFalse(isCommunity(COMMUNITY));
			}
		});
	}

}
