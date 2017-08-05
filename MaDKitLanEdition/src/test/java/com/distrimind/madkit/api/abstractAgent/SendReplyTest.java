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

import static com.distrimind.madkit.kernel.AbstractAgent.ReturnCode.INVALID_AGENT_ADDRESS;
import static com.distrimind.madkit.kernel.AbstractAgent.ReturnCode.NOT_IN_GROUP;
import static com.distrimind.madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static com.distrimind.madkit.kernel.JunitMadkit.GROUP;
import static com.distrimind.madkit.kernel.JunitMadkit.ROLE;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.distrimind.madkit.kernel.AbstractAgent;
import com.distrimind.madkit.kernel.JunitMadkit;
import com.distrimind.madkit.kernel.Message;
import com.distrimind.madkit.kernel.AbstractAgent.ReturnCode;
import com.distrimind.madkit.testing.util.agent.NormalAA;

/**
 * @author Fabien Michel
 * @author Jason Mahdjoub
 * @since MaDKit 5.0.0.15
 * @since MadkitLanEdition 1.0
 * @version 1.0
 * 
 */

public class SendReplyTest extends JunitMadkit {

	@Test
	public void returnNotInGroup() {
		launchTest(new Replier() {
			@Override
			protected void activate() {
				super.activate();
				assertEquals(SUCCESS, leaveRole(GROUP, ROLE));
				assertEquals(NOT_IN_GROUP, sendReply(nextMessage(), new Message()));
			}
		});
	}

	@Test
	public void returnInvalidAA() {
		launchTest(new Replier() {
			@Override
			protected void activate() {
				super.activate();
				target.leaveGroup(GROUP);
				assertEquals(INVALID_AGENT_ADDRESS, sendReply(nextMessage(), new Message()));
			}
		});
	}

	@Test
	public void returnSuccess() {
		launchTest(new Replier() {
			@Override
			protected void activate() {
				super.activate();
				assertEquals(SUCCESS, sendReply(nextMessage(), new Message()));
			}
		});
	}

	@Test
	public void nullArg() {
		launchTest(new Replier() {
			@Override
			protected void activate() {
				try {
					assertEquals(SUCCESS, sendReply(nextMessage(), null));
					noExceptionFailure();
				} catch (NullPointerException e) {
					throw e;
				}
			}
		}, ReturnCode.AGENT_CRASH);

		launchTest(new Replier() {
			@Override
			protected void activate() {
				try {
					assertEquals(SUCCESS, sendReply(null, new Message()));
					noExceptionFailure();
				} catch (NullPointerException e) {
					throw e;
				}
			}
		}, ReturnCode.AGENT_CRASH);

		launchTest(new Replier() {
			@Override
			protected void activate() {
				try {
					assertEquals(SUCCESS, sendReply(null, null));
					noExceptionFailure();
				} catch (NullPointerException e) {
					throw e;
				}
			}
		}, ReturnCode.AGENT_CRASH);
	}

}

class Replier extends AbstractAgent {

	protected AbstractAgent target;

	@Override
	protected void activate() {
		assertEquals(SUCCESS, launchAgent(target = new NormalAA()));
		assertEquals(SUCCESS, requestRole(GROUP, ROLE));
		assertEquals(SUCCESS, target.sendMessage(GROUP, ROLE, new Message()));
	}

}