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

/**
 * Demonstrates MaDKit's enum-based message dispatch mechanism.
 * <p>
 * This package shows how {@link madkit.messages.EnumMessage} can be used in
 * conjunction with {@link madkit.kernel.Agent#proceedEnumMessage} to build a
 * reflective command-dispatch pattern. An enum constant in the message
 * is automatically mapped to a method name on the receiving agent, which
 * is then invoked with the message's parameters.
 * <p>
 * This pattern is particularly useful for GUI actions that need to
 * trigger specific agent behaviors through a single, type-safe messaging
 * channel.
 * <p>
 * Classes in this package:
 * <ul>
 * <li>{@link madkit.sample.messaging.enumdispatch.ActionEnum} &mdash;
 *     enum defining the dispatched actions ({@code GREET}, {@code COMPUTE},
 *     {@code REPORT}).</li>
 * <li>{@link madkit.sample.messaging.enumdispatch.DispatchAgent} &mdash;
 *     a threaded agent that receives {@code EnumMessage<ActionEnum>} and
 *     dispatches them to matching methods.</li>
 * <li>{@link madkit.sample.messaging.enumdispatch.EnumDispatchDemo} &mdash;
 *     a launcher that creates a {@code DispatchAgent} and sends several
 *     enum messages to it.</li>
 * </ul>
 *
 * @see madkit.messages.EnumMessage
 * @see madkit.kernel.Agent#proceedEnumMessage
 */
package madkit.sample.messaging.enumdispatch;
