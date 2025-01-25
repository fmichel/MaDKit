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

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class represents the conversation ID to which a message belongs. When a message is
 * created, it is given an ID that will be used to tag all the messages that will be
 * created for answering this message using {@link Agent#reply(Message, Message)} like
 * methods. Especially, if the answer is again used for replying, the ID will be used
 * again to tag this new answer, and so on.
 * 
 * @since MadKit 5.0.4
 */
public final class ConversationID implements Serializable {

	private static final long serialVersionUID = 4280603137316237711L;
	private static final AtomicInteger ID_COUNTER = new AtomicInteger();
	private final int id;
	private KernelAddress origin;

	/**
	 * Instantiates a new conversation ID.
	 */
	ConversationID() {
		id = ID_COUNTER.getAndIncrement();
	}

	/**
	 * Returns a string representation of the conversation ID.s
	 * 
	 *
	 * @return the string
	 */
	@Override
	public String toString() {
		return id + (origin == null ? "" : "-" + origin);
	}

	/**
	 * Tests if this conversation ID is equal to another.
	 * 
	 *
	 * @param obj the obj to compare
	 * @return <code>true</code>, if the conversation IDs are equal
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof ConversationID ci && (origin != null)) {// obj necessarily comes from the network or is
																						// different, so origin should have
			// been set priorly if there is a chance of equality
			return this.id == ci.id && origin.equals(ci.origin);

		}
		return false;
	}

	/**
	 * Sets the origin.
	 *
	 * @param origin the new origin
	 */
	void setOrigin(KernelAddress origin) {
		if (this.origin == null) {
			this.origin = origin;
		}
	}

	/**
	 * Hash code.
	 *
	 * @return the int
	 */
	@Override
	public int hashCode() {
		return id;
	}
}