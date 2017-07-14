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
import static madkit.kernel.AbstractAgent.ReturnCode.INVALID_AGENT_ADDRESS;
import static madkit.kernel.AbstractAgent.ReturnCode.NOT_COMMUNITY;
import static madkit.kernel.AbstractAgent.ReturnCode.NOT_GROUP;
import static madkit.kernel.AbstractAgent.ReturnCode.NOT_IN_GROUP;
import static madkit.kernel.AbstractAgent.ReturnCode.NOT_ROLE;
import static madkit.kernel.AbstractAgent.ReturnCode.NO_RECIPIENT_FOUND;
import static madkit.kernel.AbstractAgent.ReturnCode.ROLE_NOT_HANDLED;
import static madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import madkit.agr.DefaultMaDKitRoles;
import madkit.kernel.AbstractAgent;
import madkit.kernel.AgentAddress;
import madkit.kernel.JunitMadkit;
import madkit.kernel.Message;

import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.7
 * @version 0.9
 * 
 */

public class SendMessageWithCGRTest extends JunitMadkit {

	final AbstractAgent target = new AbstractAgent() {
		protected void activate() {
			assertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
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
				assertEquals(SUCCESS, sendMessage(COMMUNITY, GROUP, ROLE, new Message()));
				Message m = target.nextMessage();
				assertNotNull(m);
				assertEquals(ROLE, m.getReceiver().getRole());

				// With role
				assertEquals(SUCCESS, sendMessageWithRole(COMMUNITY, GROUP, ROLE, new Message(), ROLE));
				m = target.nextMessage();
				assertNotNull(m);
				assertEquals(ROLE, m.getReceiver().getRole());
			}
		});
	}

	@Test
	public void returnSuccessOnCandidateRole() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				assertEquals(SUCCESS, launchAgent(target));

				// Without role
				assertEquals(SUCCESS, sendMessage(COMMUNITY, GROUP, DefaultMaDKitRoles.GROUP_MANAGER_ROLE, new Message()));
				Message m = target.nextMessage();
				assertNotNull(m);
				assertEquals(DefaultMaDKitRoles.GROUP_MANAGER_ROLE, m.getReceiver().getRole());
				assertEquals(DefaultMaDKitRoles.GROUP_CANDIDATE_ROLE, m.getSender().getRole());

				// With role
				assertEquals(
						SUCCESS,
						sendMessageWithRole(COMMUNITY, GROUP, DefaultMaDKitRoles.GROUP_MANAGER_ROLE, new Message(),
								DefaultMaDKitRoles.GROUP_CANDIDATE_ROLE));
				m = target.nextMessage();
				assertNotNull(m);
				assertEquals(DefaultMaDKitRoles.GROUP_MANAGER_ROLE, m.getReceiver().getRole());
				assertEquals(DefaultMaDKitRoles.GROUP_CANDIDATE_ROLE, m.getSender().getRole());
			}
		});
	}

	@Test
	public void returnInvalidAA() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				assertEquals(SUCCESS, launchAgent(target));
				assertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
				AgentAddress aa = getAgentWithRole(COMMUNITY, GROUP, ROLE);
				assertEquals(SUCCESS, target.leaveRole(COMMUNITY, GROUP, ROLE));
				assertEquals(INVALID_AGENT_ADDRESS, sendMessage(aa, new Message()));
				// With role
				assertEquals(INVALID_AGENT_ADDRESS, sendMessageWithRole(aa, new Message(), ROLE));
			}
		});
	}

	@Test
	public void returnNotInGroup() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				assertEquals(SUCCESS, launchAgent(target));
				assertEquals(NOT_IN_GROUP, sendMessage(COMMUNITY, GROUP, ROLE, new Message()));
				// With role
				assertEquals(NOT_IN_GROUP, sendMessageWithRole(COMMUNITY, GROUP, ROLE, new Message(), ROLE));
			}
		});
	}

	@Test
	public void returnNotCGR() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				assertEquals(SUCCESS, launchAgent(target));
				assertEquals(NOT_COMMUNITY, sendMessage(aa(), GROUP, ROLE, new Message()));
				assertEquals(NOT_GROUP, sendMessage(COMMUNITY, aa(), ROLE, new Message()));
				assertEquals(NOT_ROLE, sendMessage(COMMUNITY, GROUP, aa(), new Message()));

				// With role
				assertEquals(NOT_COMMUNITY, sendMessageWithRole(aa(), GROUP, ROLE, new Message(), ROLE));
				assertEquals(NOT_GROUP, sendMessageWithRole(COMMUNITY, aa(), ROLE, new Message(), ROLE));
				assertEquals(NOT_ROLE, sendMessageWithRole(COMMUNITY, GROUP, aa(), new Message(), ROLE));
			}
		});
	}

	@Test
	public void returnRoleNotHandled() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				assertEquals(SUCCESS, launchAgent(target));
				assertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
				assertEquals(ROLE_NOT_HANDLED, sendMessageWithRole(COMMUNITY, GROUP, ROLE, new Message(), aa()));
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
				assertEquals(NO_RECIPIENT_FOUND, sendMessage(COMMUNITY, GROUP, ROLE, new Message()));
				assertEquals(NO_RECIPIENT_FOUND, sendMessageWithRole(COMMUNITY, GROUP, ROLE, new Message(), ROLE));
			}
		});
	}

	@Test
	public void nullCommunity() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				assertEquals(SUCCESS, launchAgent(target));
				try {
					sendMessage(null, aa(), aa(), new Message());
					noExceptionFailure();
				} catch (NullPointerException e) {
					throw e;
				}
			}
		}, AGENT_CRASH);
	}

	@Test
	public void nullGroup() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				assertEquals(SUCCESS, launchAgent(target));
				try {
					sendMessage(COMMUNITY, null, aa(), new Message());
					noExceptionFailure();
				} catch (NullPointerException e) {
					throw e;
				}
			}
		}, AGENT_CRASH);
	}

	@Test
	public void nullRole() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				assertEquals(SUCCESS, launchAgent(target));
				try {
					sendMessage(COMMUNITY, GROUP, null, new Message());
					noExceptionFailure();
				} catch (NullPointerException e) {
					throw e;
				}
			}
		}, AGENT_CRASH);
	}

	@Test
	public void nullMessage() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				assertEquals(SUCCESS, launchAgent(target));
				assertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
				try {
					sendMessage(COMMUNITY, GROUP, ROLE, null);
					noExceptionFailure();
				} catch (NullPointerException e) {
					throw e;
				}
			}
		}, AGENT_CRASH);
	}

}
