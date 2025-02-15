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

import static madkit.kernel.MadkitUnitTestCase.COMMUNITY;
import static madkit.kernel.MadkitUnitTestCase.GROUP;
import static madkit.kernel.MadkitUnitTestCase.ROLE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import java.time.LocalDateTime;

import static madkit.kernel.Agent.ReturnCode.SUCCESS;

/**
 *
 *
 */
public class GenericTestAgent extends Agent implements TestHelpAgent {

	private boolean goThroughEnd = false;
	private boolean oneMethodHasBeenActivated = false;

	/**
	 * 
	 */
	public GenericTestAgent() {
//		getLogger().setLevel(Level.ALL);
//		getLogger().info("********* INIT ***********");
	}

	@Override
	public Agent getAgent() {
		return this;
	}

	@Override
	protected void onActivation() {
		orgInActivate();
		behaviorInActivate();
	}

	@Override
	protected void onEnd() {
		goThroughEnd = true;
		orgInEnd();
		behaviorInEnd();
	}

	public boolean didPassThroughEnd() {
		return goThroughEnd;
	}

	protected void checkTermination() {
		assertFalse(alive.get());
		assertEquals(kernel, KernelAgent.deadKernel);
	}

	@Override
	public void createDefaultCGR() {
		createGroup(COMMUNITY, GROUP, false, null);
		assertEquals(requestRole(COMMUNITY, GROUP, ROLE, null), SUCCESS);
	}

	protected void replyToLastReiceivedMessage() {
		Message m = waitNextMessage();
		reply(new Message(), m);
	}

	@SuppressWarnings("unused")
	private void privateMethod() {
		System.out.println("private method");
		setOneMethodHasBeenActivated(true);
	}

	protected void protectedMethod() {
		System.out.println("protected method");
	}

	public void publicMethod() {
		System.out.println("public method");
	}

	@SuppressWarnings("unused")
	private void privateMethodWithArgs(String s, Object o) {
		System.err.println(s);
		System.err.println(o);
		setOneMethodHasBeenActivated(true);
	}

	@SuppressWarnings("unused")
	private void privateMethodWithPrimitiveArgs(String s, int i) {
		System.err.println(s);
		System.err.println(i);
		setOneMethodHasBeenActivated(true);
	}

	public void publicMethodWithPrimitiveArgs(String s, int i) {
		System.err.println(s);
		System.err.println(this.toString() + i);
		setOneMethodHasBeenActivated(true);
	}

	public void printMailbox() {
		getLogger().info(getMailbox().toString());
	}

	/**
	 * @return the oneMethodHasBeenActivated
	 */
	public boolean isOneMethodHasBeenActivated() {
		return oneMethodHasBeenActivated;
	}

	/**
	 * @param oneMethodHasBeenActivated the oneMethodHasBeenActivated to set
	 */
	public void setOneMethodHasBeenActivated(boolean oneMethodHasBeenActivated) {
		this.oneMethodHasBeenActivated = oneMethodHasBeenActivated;
	}

	@Override
	public LocalDateTime getNextEventDate() {
		// TODO Auto-generated method stub
		return null;
	}

}
