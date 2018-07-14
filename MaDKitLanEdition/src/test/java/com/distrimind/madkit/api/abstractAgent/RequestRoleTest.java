/*
 * MadKitLanEdition (created by Jason MAHDJOUB (jason.mahdjoub@distri-mind.fr)) Copyright (c)
 * 2015 is a fork of MadKit and MadKitGroupExtension. 
 * 
 * Copyright or © or Copr. Jason Mahdjoub, Fabien Michel, Olivier Gutknecht, Jacques Ferber (1997)
 * 
 * jason.mahdjoub@distri-mind.fr
 * fmichel@lirmm.fr
 * olg@no-distance.net
 * ferber@lirmm.fr
 * 
 * This software is a computer program whose purpose is to
 * provide a lightweight Java library for designing and simulating Multi-Agent Systems (MAS).
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 * 
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package com.distrimind.madkit.api.abstractAgent;

import static com.distrimind.madkit.kernel.AbstractAgent.ReturnCode.ACCESS_DENIED;
import static com.distrimind.madkit.kernel.AbstractAgent.ReturnCode.AGENT_CRASH;
import static com.distrimind.madkit.kernel.AbstractAgent.ReturnCode.NOT_COMMUNITY;
import static com.distrimind.madkit.kernel.AbstractAgent.ReturnCode.ROLE_ALREADY_HANDLED;
import static com.distrimind.madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.junit.Test;

import com.distrimind.madkit.agr.Organization;
import com.distrimind.madkit.kernel.AbstractAgent;
import com.distrimind.madkit.kernel.AgentNetworkID;
import com.distrimind.madkit.kernel.Gatekeeper;
import com.distrimind.madkit.kernel.Group;
import com.distrimind.madkit.kernel.JunitMadkit;
import com.distrimind.madkit.util.ExternalizableAndSizable;

/**
 * @author Fabien Michel
 * @author Jason Mahdjoub
 * @since MaDKit 5.0.0.7
 * @since MadkitLanEdition 1.0
 * @version 1.0
 * 
 * 
 */

public class RequestRoleTest extends JunitMadkit {

	final AbstractAgent helper = new AbstractAgent() {
		@Override
		protected void activate() {
			assertEquals(ROLE_ALREADY_HANDLED, requestRole(GROUP, Organization.GROUP_MANAGER_ROLE));
			assertEquals(SUCCESS, requestRole(GROUP, ROLE));
		}
	};

	final AbstractAgent helper2 = new AbstractAgent() {
		@Override
		protected void activate() {
			assertEquals(ROLE_ALREADY_HANDLED, requestRole(GROUP, Organization.GROUP_MANAGER_ROLE));
			assertEquals(SUCCESS, requestRole(GROUP, ROLE));
		}
	};

	final Gatekeeper alwaysDeny = new Gatekeeper() {

		@Override
		public boolean allowAgentToTakeRole(Group _group, String _roleName,
				final Class<? extends AbstractAgent> requesterClass, AgentNetworkID _agentNetworkID,
				Object _memberCard) {
			return false;
		}

		@Override
		public boolean allowAgentToCreateSubGroup(Group _parent_group, Group _sub_group,
				final Class<? extends AbstractAgent> requesterClass, AgentNetworkID _agentNetworkID,
				Object _memberCard) {
			return false;
		}
	};

	final Gatekeeper alwaysAccept = new Gatekeeper() {
		@Override
		public boolean allowAgentToTakeRole(Group _group, String _roleName,
				final Class<? extends AbstractAgent> requesterClass, AgentNetworkID _agentNetworkID,
				Object _memberCard) {
			return true;
		}

		@Override
		public boolean allowAgentToCreateSubGroup(Group _parent_group, Group _sub_group,
				final Class<? extends AbstractAgent> requesterClass, AgentNetworkID _agentNetworkID,
				Object _memberCard) {
			return true;
		}
	};

	final Gatekeeper buggyIdentifier = new Gatekeeper() {
		@SuppressWarnings("null")
		@Override
		public boolean allowAgentToTakeRole(Group _group, String _roleName,
				final Class<? extends AbstractAgent> requesterClass, AgentNetworkID _agentNetworkID,
				Object _memberCard) {
			Object o = null;
			o.toString();
			return true;
		}

		@SuppressWarnings("null")
		@Override
		public boolean allowAgentToCreateSubGroup(Group _parent_group, Group _sub_group,
				final Class<? extends AbstractAgent> requesterClass, AgentNetworkID _agentNetworkID,
				Object _memberCard) {
			Object o = null;
			o.toString();
			return true;
		}

	};

	@Test
	public void returnSuccess() {
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				assertEquals(SUCCESS, createGroup(GROUP));
				assertEquals(SUCCESS, requestRole(GROUP, ROLE));
				assertTrue(isCreatedRole(GROUP, Organization.GROUP_MANAGER_ROLE));
				assertTrue(isCreatedRole(GROUP, ROLE));
			}
		});
	}

	@Test
	public void returnAlreadyHandled() {
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				assertEquals(SUCCESS, createGroup(GROUP));
				assertEquals(ROLE_ALREADY_HANDLED, requestRole(GROUP, Organization.GROUP_MANAGER_ROLE));
				assertEquals(SUCCESS, requestRole(GROUP, ROLE));
				assertEquals(ROLE_ALREADY_HANDLED, requestRole(GROUP, ROLE));
			}
		});
	}

	@Test
	public void returnAccessDenied() {
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				Group g = new Group(false, alwaysDeny, false, GROUP.getCommunity(), "GD");
				assertEquals(SUCCESS, createGroup(g));
				g = new Group(false, GROUP.getCommunity(), "GD");
				assertEquals(ACCESS_DENIED, requestRole(g, ROLE));
				assertEquals(ACCESS_DENIED, requestRole(g, ROLE, null));
			}
		});
	}

	@Test
	public void returnAccessGranted() {
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				Group g = new Group(false, alwaysAccept, false, GROUP.getCommunity(), "GA");
				assertEquals(SUCCESS, createGroup(g));
				g = new Group(GROUP.getCommunity(), "GA");
				assertEquals(SUCCESS, requestRole(g, ROLE));
				assertEquals(SUCCESS, requestRole(g, aa(), null));
			}
		});
	}

	@Test
	public void buggyIdentifier() {
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				Group g = new Group(false, buggyIdentifier, false, GROUP.getCommunity(), "GBuggy");
				assertEquals(SUCCESS, createGroup(g));// TODO
				g = new Group(GROUP.getCommunity(), "GBuggy"); // think
				// about
				// that
				// issue
				assertEquals(SUCCESS, requestRole(g, aa(), null));
			}
		}, AGENT_CRASH);
	}

	@Test
	public void returnNullRole() {
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				assertEquals(SUCCESS, createGroup(GROUP, alwaysDeny));
			}
		});
	}

	@Test
	public void defaultRole() {
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				assertEquals(SUCCESS, createGroup(GROUP));
				assertTrue(isCreatedGroup(GROUP));
				assertTrue(isCreatedRole(GROUP, Organization.GROUP_MANAGER_ROLE));
				assertEquals(SUCCESS, leaveGroup(GROUP));
				assertFalse(isCreatedGroup(GROUP));
			}
		});
	}

	@Test
	public void nullArgs() {
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				try {
					assertEquals(NOT_COMMUNITY, requestRole(null, null, null));
					noExceptionFailure();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}

				assertEquals(SUCCESS, createGroup(GROUP));
				try {
					assertEquals(SUCCESS, requestRole(GROUP, null, null));
					noExceptionFailure();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
				try {
					assertEquals(NOT_COMMUNITY, requestRole(new Group(null, G), null));
					noExceptionFailure();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
				try {
					assertEquals(NOT_COMMUNITY, requestRole(null, ROLE));
					noExceptionFailure();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
				try {
					assertEquals(SUCCESS, requestRole(GROUP, null, new ExternalizableAndSizable() {
						
						/**
						 * 
						 */
						private static final long serialVersionUID = 6344115606412187437L;

						@Override
						public int getInternalSerializedSize() {
							return 0;
						}

						@Override
						public void writeExternal(ObjectOutput out) {
							
						}

						@Override
						public void readExternal(ObjectInput in) {
							
						}
					}));
					noExceptionFailure();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
			}
		});
	}

	@Test
	public void onlyOneManager() {
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				assertEquals(SUCCESS, createGroup(GROUP));
				assertEquals(ROLE_ALREADY_HANDLED, requestRole(GROUP, Organization.GROUP_MANAGER_ROLE));
				assertEquals(SUCCESS, requestRole(GROUP, ROLE));
				assertEquals(SUCCESS, launchAgent(helper));
				assertEquals(SUCCESS, launchAgent(helper2));
			}
		});
	}

	@Test
	public void leaveGroupByLeavingRoles() {
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				assertFalse(isCommunity(C));
				assertFalse(isCreatedGroup(GROUP));
				assertEquals(SUCCESS, createGroup(GROUP));
				assertEquals(SUCCESS, requestRole(GROUP, ROLE));
			}
		});
	}

}
