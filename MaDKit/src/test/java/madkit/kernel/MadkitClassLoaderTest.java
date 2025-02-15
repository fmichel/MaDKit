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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import org.testng.annotations.Test;

import madkit.test.agents.CGRAgent;
import madkit.test.agents.EmptyAgent;

/**
 * The Class MadkitClassLoaderTest.
 */
public class MadkitClassLoaderTest {
	@Test
	public void givenValidAgentClassName_whenGetAgentInstance_thenReturnAgentInstance() {
		// Given
		String agentClassName = "madkit.test.agents.CGRAgent";

		// When
		Agent agentInstance = MadkitClassLoader.getAgentInstance(agentClassName);

		// Then
		assertNotNull(agentInstance, "The agent instance should not be null");
		assertEquals(agentClassName, agentInstance.getClass().getName(),
				"The agent instance should be of the correct type");
	}

	@Test
	public void givenInvalidAgentClassName_whenGetAgentInstance_thenReturnNull() {
		// Given
		String agentClassName = "madkit.kernel.NonExistentAgent";

		// When
		Agent agentInstance = MadkitClassLoader.getAgentInstance(agentClassName);

		// Then
		assertNull(agentInstance, "The agent instance should be null for a non-existent class");
	}

	@Test
	public void givenNoDefaultConstructorAgent_whenGetAgentInstance_thenReturnNull() {
		// Given
		String agentClassName = "madkit.test.agents.NoDefaultConstructorAgent";

		// When
		Agent agentInstance = MadkitClassLoader.getAgentInstance(agentClassName);

		// Then
		assertNull(agentInstance, "The agent instance should be null for a non-existent class");
	}

	@Test(expectedExceptions = ClassCastException.class)
	public void givenInvalidReturnType_whenGetAgentInstance_thenClassCastException() {
		// Given
		String agentClassName = EmptyAgent.class.getName();

		// When
		CGRAgent agentInstance = MadkitClassLoader.getAgentInstance(agentClassName);
	}

}
