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

import static madkit.kernel.AbstractAgent.ReturnCode.NOT_COMMUNITY;
import static madkit.kernel.AbstractAgent.ReturnCode.NOT_GROUP;
import static madkit.kernel.AbstractAgent.ReturnCode.NOT_IN_GROUP;
import static madkit.kernel.AbstractAgent.ReturnCode.NOT_ROLE;
import static madkit.kernel.AbstractAgent.ReturnCode.NO_RECIPIENT_FOUND;
import static madkit.kernel.AbstractAgent.ReturnCode.ROLE_NOT_HANDLED;
import static madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import madkit.kernel.AbstractAgent;
import madkit.kernel.JunitMadkit;
import madkit.kernel.Message;
import madkit.message.StringMessage;

import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.7
 * @version 0.9
 * 
 */

public class BroadcastMessageTest extends JunitMadkit {

	final AbstractAgent target = new AbstractAgent() {
		protected void activate() {
			assertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
			assertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
		}
	};

	final AbstractAgent target2 = new AbstractAgent() {
		protected void activate() {
			assertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
		}
	};

	@Test
	public void returnSuccess() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				assertEquals(SUCCESS, launchAgent(target));
				assertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));

				// Without role
				assertEquals(SUCCESS, broadcastMessage(COMMUNITY, GROUP, ROLE, new Message()));
				Message m = target.nextMessage();
				assertEquals(SUCCESS, broadcastMessage(COMMUNITY, GROUP, ROLE, new StringMessage("test")));
				assertNotNull(target.nextMessage());
				assertEquals(ROLE, m.getReceiver().getRole());

				// With role
				assertEquals(SUCCESS, broadcastMessageWithRole(COMMUNITY, GROUP, ROLE, new Message(), ROLE));
				m = target.nextMessage();
				assertNotNull(m);
				assertEquals(ROLE, m.getReceiver().getRole());

				// verifying cloning
				launchAgent(target2);
				assertEquals(SUCCESS, broadcastMessageWithRole(COMMUNITY, GROUP, ROLE, new Message(), ROLE));
				m = target.nextMessage();
				assertNotNull(m);
				Message m2 = target2.nextMessage();
				assertEquals(ROLE, m2.getReceiver().getRole());
				assertEquals(m.getConversationID(), m2.getConversationID());
				assertNotSame(m2, m);
			}
		});
	}

	@Test
	public void returnNotInGroup() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				assertEquals(SUCCESS, launchAgent(target));
				// Without role
				assertEquals(NOT_IN_GROUP, broadcastMessage(COMMUNITY, GROUP, ROLE, new Message()));

				// With role
				assertEquals(NOT_IN_GROUP, broadcastMessageWithRole(COMMUNITY, GROUP, ROLE, new Message(), ROLE));
			}
		});
	}

	@Test
	public void returnNotCGR() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				assertEquals(SUCCESS, launchAgent(target));
				assertEquals(NOT_COMMUNITY, broadcastMessage(aa(), GROUP, ROLE, new Message()));
				assertEquals(NOT_GROUP, broadcastMessage(COMMUNITY, aa(), ROLE, new Message()));
				assertEquals(NOT_ROLE, broadcastMessage(COMMUNITY, GROUP, aa(), new Message()));

				// With role
				assertEquals(NOT_COMMUNITY, broadcastMessageWithRole(aa(), GROUP, ROLE, new Message(), ROLE));
				assertEquals(NOT_GROUP, broadcastMessageWithRole(COMMUNITY, aa(), ROLE, new Message(), ROLE));
				assertEquals(NOT_ROLE, broadcastMessageWithRole(COMMUNITY, GROUP, aa(), new Message(), ROLE));
			}
		});
	}

	@Test
	public void returnRoleNotHandled() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				assertEquals(SUCCESS, launchAgent(target));
				assertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
				assertEquals(ROLE_NOT_HANDLED, broadcastMessageWithRole(COMMUNITY, GROUP, ROLE, new Message(), aa()));
			}
		});
	}

	@Test
	public void returnNoRecipientFound() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				assertEquals(SUCCESS, launchAgent(target));
				assertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
				assertEquals(SUCCESS, target.leaveRole(COMMUNITY, GROUP, ROLE));
				assertEquals(NO_RECIPIENT_FOUND, broadcastMessage(COMMUNITY, GROUP, ROLE, new Message()));
				assertEquals(NO_RECIPIENT_FOUND, broadcastMessageWithRole(COMMUNITY, GROUP, ROLE, new Message(), ROLE));
			}
		});
	}

	@Test
	public void nullArgs() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				assertEquals(SUCCESS, launchAgent(target));
				try {
					broadcastMessage(null, null, null, null);
					noExceptionFailure();
				} catch (NullPointerException e) {
					// e.printStackTrace();
				}
				try {
					broadcastMessage(COMMUNITY, null, null, null);
					noExceptionFailure();
				} catch (NullPointerException e) {
					// e.printStackTrace();
				}
				try {
					broadcastMessage(COMMUNITY, GROUP, null, null);
					noExceptionFailure();
				} catch (NullPointerException e) {
					// e.printStackTrace();
				}
				try {
					assertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
					broadcastMessage(COMMUNITY, GROUP, ROLE, null);
					noExceptionFailure();
				} catch (NullPointerException e) {
					// e.printStackTrace();
				}
				try {
					broadcastMessage(null, GROUP, ROLE, null);
					noExceptionFailure();
				} catch (NullPointerException e) {
					// e.printStackTrace();
				}
			}
		});
	}

}
