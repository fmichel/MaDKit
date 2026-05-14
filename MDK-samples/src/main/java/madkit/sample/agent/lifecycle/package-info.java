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
 * Demonstrates the full agent lifecycle: activation, live, and end phases.
 * <p>
 * Includes examples of the normal lifecycle flow, launching and observing agents,
 * and what happens when agents crash in different phases. Key concepts:
 * <ul>
 * <li>{@link madkit.kernel.Agent#onActivation()} — first phase, initialisation</li>
 * <li>{@link madkit.kernel.Agent#onLive()} — main behavior (threaded agents only)</li>
 * <li>{@link madkit.kernel.Agent#onEnd()} — cleanup, always called after live (or after a crash in live)</li>
 * <li>{@link madkit.kernel.Agent#isAlive()} — tracks whether the agent is between activation and end</li>
 * <li>{@link madkit.kernel.Agent.ReturnCode#AGENT_CRASH} — returned when an agent crashes during activation</li>
 * </ul>
 */
package madkit.sample.agent.lifecycle;
