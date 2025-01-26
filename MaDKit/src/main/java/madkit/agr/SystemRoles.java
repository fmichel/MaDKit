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
package madkit.agr;

import madkit.kernel.Agent;

/**
 * Defines key roles used by the MaDKit kernel or regular agents to achieve specific CGR
 * queries. See
 * {@link Agent#createGroup(String, String, boolean, madkit.kernel.Gatekeeper)} for
 * instance.
 * 
 * @since MaDKit 5.2
 * @version 6.0.1
 */
public class SystemRoles {

	/**
	 * Utility class
	 */
	private SystemRoles() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * This role is automatically given to agents that create a group. The value of this
	 * constant is {@value}.
	 */
	public static final String GROUP_MANAGER = "manager";
	/**
	 * This role is a temporary role used to exchange messages with a group's manager that one
	 * agent is not part of. The value of this constant is {@value}.
	 */
	public static final String GROUP_CANDIDATE = "candidate";

}