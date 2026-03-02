/*******************************************************************************
 * MaDKit - Multi-agent systems Development Kit 
 * 
 * Copyright (c) 1998-2025 Fabien Michel, Olivier Gutknecht, Jacques Ferber...
 * 
 * This software is a computer program whose purpose is to
 * provide a lightweight Java API for developing and simulating 
 * Multi-Agent Systems (MAS) using an organizational perspective.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.You can use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty and the software's author, the holder of the
 * economic rights, and the successive licensors have only limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading, using, modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean that it is complicated to manipulate, and that also
 * therefore means that it is reserved for developers and experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and, more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 *******************************************************************************/
package madkit.kernel;

import static madkit.kernel.MadkitTestConstants.COMMUNITY;
import static madkit.kernel.MadkitTestConstants.GROUP;
import static madkit.kernel.MadkitTestConstants.ROLE;
import static org.testng.Assert.assertEquals;

import static madkit.kernel.Agent.ReturnCode.SUCCESS;

import madkit.kernel.Agent;
import madkit.kernel.Agent.ReturnCode;
import madkit.kernel.AgentInterruptedException;
import madkit.kernel.AgentLogger;
import madkit.kernel.Message;

/**
 * Interface providing reusable test support behaviors for MaDKit agents. Implementors must
 * be {@link Agent} subclasses, which gives them access to protected Agent methods. This
 * interface delegates to those methods via abstract hooks that concrete classes implement.
 */
public interface TestAgentSupport {

	/**
	 * Gets the agent.
	 *
	 * @return the agent
	 */
	public abstract Agent getAgent();

	/**
	 * Checks if the current agent's thread has been interrupted and throws
	 * {@link AgentInterruptedException} if so. Implementors should delegate to
	 * {@link Agent#exitOnKill()}.
	 *
	 * @throws AgentInterruptedException if the thread has been interrupted
	 */
	public void checkExitOnKill() throws AgentInterruptedException;

	/**
	 * Blocking wait for the next message. Implementors should delegate to
	 * {@link Agent#waitNextMessage()}.
	 *
	 * @return the next message
	 */
	public Message doWaitNextMessage();

	public default void behaviorInActivate() {
	}

	public default void behaviorInLive() {
	}

	public default void behaviorInEnd() {
	}

	public default void orgInActivate() {
	}

	public default void orgInLive() {
	}

	public default void orgInEnd() {
	}

	public default void bug() {
		throw new NullPointerException();
	}

	/**
	 * Gets the logger.
	 *
	 * @return the logger
	 */
	public abstract AgentLogger getLogger();

	public default void takeDefaultLocalCGR() {
		getAgent().createGroup(COMMUNITY, GROUP, false, null);
		assertEquals(getAgent().requestRole(COMMUNITY, GROUP, ROLE, null), SUCCESS);
	}

	public default void takeDefaultDistributedCGR() {
		getAgent().createGroup(COMMUNITY, GROUP, true, null);
		assertEquals(getAgent().requestRole(COMMUNITY, GROUP, ROLE, null), SUCCESS);
	}

//	public abstract void takeDefaultLocalCGR();
//
//	public abstract void takeDefaultDistributedCGR();

	public default void computeForEver() {
		for (int i = 0; i < Integer.MAX_VALUE; i++) {
			checkExitOnKill();
			Math.cos(Math.random());
			if (i % 1000000 == 0) {
				getLogger().info("computing... step " + i);
//				sleep(1);
			}
		}
	}

	/**
	 * 
	 * need to implement this for not affecting the visibility of the agent's one
	 * 
	 * @param milliSeconds
	 */
	public default void sleep(final int milliSeconds) {
		try {
			Thread.sleep(milliSeconds);
		} catch (InterruptedException e) {
			throw new AgentInterruptedException();
		}
	}

	public default void blockForever() {
		try {
			Object o = new Object();
			synchronized (o) {
				getLogger().info(() -> "BLOCKING MYSELF ");
				o.wait();
			}
		} catch (InterruptedException e) {
			getLogger().info(() -> "INTERRUPTED ");
			throw new AgentInterruptedException();
		}
	}

	public default void waitMessageAndReply() {
		Message waitNextMessage = doWaitNextMessage();
		sleep(100);
		reply(createNewMessage(), waitNextMessage);
	}

	/**
	 * @return a new message
	 */
	public default <M extends Message> Message createNewMessage() {
		return new Message();
	}

	public void setMadkitConcurrentTestCase(MadkitConcurrentTestCase mdkitConcurrentTestCase);

	public MadkitConcurrentTestCase getMadkitConcurrentTestCase();

	//////////////////////////////////// AgentInterface

	ReturnCode reply(Message reply, Message messageToReplyTo);

}
