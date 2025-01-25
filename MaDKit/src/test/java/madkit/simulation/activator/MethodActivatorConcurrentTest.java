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
package madkit.simulation.activator;

import java.util.List;

import org.testng.annotations.Test;

import madkit.kernel.Agent;
import madkit.kernel.GenericTestAgent;
import madkit.kernel.MadkitConcurrentTestCase;
import madkit.simulation.scheduler.MethodActivator;

/**
 * The Class MethodActivatorConcurrentTest.
 */
public class MethodActivatorConcurrentTest extends MadkitConcurrentTestCase {

	@Test
	public void executeTest() {
		runTest(new Agent() {
			@Override
			protected void onActivation() {
				List<GenericTestAgent> agents = getNewAgentsList(this);
				MethodActivator activator = new MethodActivator("test", "test", "privateMethod");
				activator.execute(agents);
				checkActivation(agents);
				resume();
			}
		});
	}

	@Test
	public void givenAnActivatorWithTypes_whenExecute_thenShouldWorks() {
		runTest(new Agent() {
			@Override
			protected void onActivation() {
				List<GenericTestAgent> agents = getNewAgentsList(this);
				MethodActivator activator = new MethodActivator("test", "test", "privateMethodWithPrimitiveArgs",
						String.class, int.class);
				activator.execute(agents, "hello", 1);
				checkActivation(agents);
				resume();
			}
		});
	}

	@Test
	public void givenAnActivatorNoTypesAtCreation_whenExecute_thenShouldWorks() {
		runTest(new Agent() {
			@Override
			protected void onActivation() {
				List<GenericTestAgent> agents = getNewAgentsList(this);
				MethodActivator activator = new MethodActivator("test", "test", "privateMethodWithPrimitiveArgs");
				activator.execute(agents, "hello", 1);
				checkActivation(agents);
				resume();
			}
		});
	}

	@Test
	public void givenAgentList_whenExecuteInParallel_shouldWorks() {
		runTest(new Agent() {
			@Override
			protected void onActivation() {
				List<GenericTestAgent> agents = getNewAgentsList(this);
				MethodActivator activator = new MethodActivator("test", "test", "privateMethod");
				activator.executeInParallel(agents);
				checkActivation(agents);
				resume();
			}
		});
	}

	@Test
	public void givenAnActivatorNoTypesAtCreation_whenExecuteInParallel_thenShouldWorks() {
		runTest(new Agent() {
			@Override
			protected void onActivation() {
				List<GenericTestAgent> agents = getNewAgentsList(this);
				MethodActivator activator = new MethodActivator("test", "test", "privateMethodWithPrimitiveArgs");
				activator.executeInParallel(agents, "hello", 1);
				checkActivation(agents);
				resume();
			}
		});
	}

	void checkActivation(List<GenericTestAgent> agents) {
		for (GenericTestAgent agent : agents) {
			threadAssertTrue(agent.isOneMethodHasBeenActivated());
		}
	}

	List<GenericTestAgent> getNewAgentsList(Agent creator) {
		GenericTestAgent a1 = creator.launchAgent(GenericTestAgent.class.getName(), 1);
		GenericTestAgent a2 = creator.launchAgent(GenericTestAgent.class.getName(), 1);
		return List.of(a1, a2);
	}
}
