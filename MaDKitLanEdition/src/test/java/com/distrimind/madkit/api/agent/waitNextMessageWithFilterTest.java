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
package com.distrimind.madkit.api.agent;

import static com.distrimind.madkit.kernel.AbstractAgent.ReturnCode.AGENT_CRASH;
import static com.distrimind.madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.AfterClass;
import org.junit.Test;

import com.distrimind.madkit.kernel.JunitMadkit;
import com.distrimind.madkit.kernel.Message;
import com.distrimind.madkit.message.MessageFilter;
import com.distrimind.madkit.testing.util.agent.ForEverReplierAgent;
import com.distrimind.madkit.testing.util.agent.NormalAgent;
import com.distrimind.madkit.util.message.EmptyMessage;

/**
 * @author Fabien Michel
 * @author Jason Mahdjoub
 * @since MaDKit 5.0.4
 * @since MadkitLanEdition 1.0
 * @version 1.0
 * 
 */
@SuppressWarnings("all")
public class waitNextMessageWithFilterTest extends JunitMadkit {

	protected MessageFilter filter = new MessageFilter() {
		@Override
		public boolean accept(Message m) {
			return m instanceof EmptyMessage;
		}
	};

	@Test
	public void waitSuccess() {
		launchTest(new NormalAgent() {
			@Override
			protected void activate() throws InterruptedException {
				super.activate();
				receiveMessage(new Message());
				assertEquals(SUCCESS, launchAgent(new ForEverReplierAgent(EmptyMessage.class)));
				sendMessage(GROUP, ROLE, new Message());
				pause(20);
				receiveMessage(new Message());
				assertNotNull(waitNextMessage(filter));
				assertEquals(3, nextMessages(null).size());
			}
		});
	}

	@Test
	public void waitReturnNull() {
		launchTest(new NormalAgent() {
			@Override
			protected void activate() throws InterruptedException {
				super.activate();
				receiveMessage(new Message());
				assertEquals(SUCCESS, launchAgent(new ForEverReplierAgent()));
				sendMessage(GROUP, ROLE, new Message());
				pause(20);
				receiveMessage(new Message());
				assertNull(waitNextMessage(100, filter));
				assertEquals(4, nextMessages(null).size());
			}
		});
	}

	@Test
	public void nullArg() {
		launchTest(new NormalAgent() {
			@Override
			protected void activate() throws InterruptedException {
				waitNextMessage(1, null);// not fail when messagebox is empty
				try {
					receiveMessage(new Message());
					waitNextMessage(1, null);
					noExceptionFailure();
				} catch (NullPointerException e) {
					throw e;
				}
			}
		}, AGENT_CRASH);
	}

}
