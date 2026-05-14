/*******************************************************************************
 * MaDKit - Multi-agent systems Development Kit 
 * 
 * Copyright (c) 1998-2026 Fabien Michel, Olivier Gutknecht, Jacques Ferber...
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
package madkit.sample.messaging.enumdispatch;

/**
 * Enum defining the actions that a {@link DispatchAgent} can process.
 * <p>
 * Each constant maps to a method on the receiving agent via
 * {@link madkit.kernel.Agent#proceedEnumMessage}. The conversion follows
 * the MaDKit {@code enumToMethodName} convention:
 * <ul>
 * <li>{@link #GREET} &rarr; {@code greet()}</li>
 * <li>{@link #COMPUTE} &rarr; {@code compute(int, int)}</li>
 * <li>{@link #REPORT} &rarr; {@code report()}</li>
 * </ul>
 * <p>
 * These constants are used as the code parameter when constructing an
 * {@link madkit.messages.EnumMessage EnumMessage&lt;ActionEnum&gt;}.
 *
 * @see DispatchAgent
 * @see EnumDispatchDemo
 * @see madkit.messages.EnumMessage
 */
public enum ActionEnum {

	/**
	 * Action that triggers a greeting behavior.
	 * <p>
	 * Mapped to {@link DispatchAgent#greet(String)} — expects a single
	 * {@link String} parameter containing the name to greet.
	 */
	GREET,

	/**
	 * Action that triggers a computation behavior.
	 * <p>
	 * Mapped to {@link DispatchAgent#compute(int, int)} — expects two
	 * {@code int} parameters representing the operands to add.
	 */
	COMPUTE,

	/**
	 * Action that triggers a status report behavior.
	 * <p>
	 * Mapped to {@link DispatchAgent#report()} — takes no parameters.
	 */
	REPORT
}
