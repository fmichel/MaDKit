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
package madkit.simulation;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

import static madkit.kernel.Agent.ReturnCode.ALREADY_GROUP;
import static madkit.kernel.Agent.ReturnCode.SUCCESS;

import madkit.kernel.Activator;
import madkit.kernel.Agent;
import madkit.kernel.Agent.ReturnCode;
import madkit.kernel.MadkitConcurrentTestCase;
import madkit.simulation.scheduler.TickBasedScheduler;
import madkit.testing.agents.CGRAgent;
import madkit.testing.agents.SimulatedAgent;

/**
 *
 * @since MaDKit 5.0.0.2
 * @version 0.9
 * 
 */

public class BasicSchedulerTest extends MadkitConcurrentTestCase {

	@Test
	public void givenNewActivator_whenAddedBeforeAgentsJoin_thenSizeIsCorrect() {
		runSimuTest(new TickBasedScheduler() {
			@Override
			protected void onActivation() {
				EmptyActivator ea = new EmptyActivator(GROUP, ROLE);
				addActivator(ea);
				launchAgent(new SimulatedAgent());
				assertThat(ea.size()).as("activator size after agent join").isEqualTo(1);
				resume();
			}
		});
	}

	@Test
	public void givenNewActivator_whenAddedAfterAgentsJoined_thenSizeIsCorrect() {
		runSimuTest(new TickBasedScheduler() {
			@Override
			protected void onActivation() {
				launchAgent(new SimulatedAgent());
				EmptyActivator ea = new EmptyActivator(GROUP, ROLE);
				addActivator(ea);
				assertThat(ea.size()).as("activator size after adding activator").isEqualTo(1);
				resume();
			}
		});
	}

	@Test
	public void addingNullActivatorExceptionPrint() {
		runTest(new CGRAgent() {
			@Override
			public void behaviorInActivate() {
				// launching an agent that creates an activator with null args should crash
				assertThat(launchAgent(new Agent() {
					@Override
					protected void onActivation() {
						Activator a = new EmptyActivator(null, null);
						resume();
					}
				})).as("launch agent creating null activator").isEqualTo(ReturnCode.AGENT_CRASH);
				resume();
			}
		});
	}

	@Test
	public void addingAndRemovingActivators() {
		runSimuTest(new TickBasedScheduler() {
			@Override
			public void onActivation() {
				// ///////////////////////// REQUEST ROLE ////////////////////////
				Activator a = new EmptyActivator(GROUP, ROLE);
				addActivator(a);
				assertThat(a.size()).as("activator initial size").isEqualTo(0);

				assertThat(createSimuGroup(GROUP)).as("createSimuGroup first").isEqualTo(SUCCESS);
				assertThat(createSimuGroup(GROUP)).as("createSimuGroup already").isEqualTo(ALREADY_GROUP);
				assertThat(requestSimuRole(GROUP, ROLE)).as("requestSimuRole").isEqualTo(SUCCESS);

				assertThat(a.size()).as("activator size after request").isEqualTo(1);

				assertThat(leaveSimuGroup(GROUP)).as("leaveSimuGroup").isEqualTo(SUCCESS);
				assertThat(a.size()).as("activator size after leave").isEqualTo(0);

				// Adding and removing while group does not exist
				removeActivator(a);
				assertThat(a.size()).as("activator size after remove").isEqualTo(0);
				addActivator(a);
				assertThat(a.size()).as("activator size after add").isEqualTo(0);

				assertThat(createSimuGroup(GROUP)).as("createSimuGroup").isEqualTo(SUCCESS);
				assertThat(requestSimuRole(GROUP, ROLE)).as("requestSimuRole second").isEqualTo(SUCCESS);
				SimuAgent other = new SimuAgent() {
					@Override
					protected void onActivation() {
						assertThat(requestSimuRole(GROUP, ROLE)).as("other requestSimuRole").isEqualTo(SUCCESS);
					}
				};
				assertThat(launchAgent(other)).as("launch other simu agent").isEqualTo(SUCCESS);

				assertThat(a.size()).as("activator size after two agents").isEqualTo(2);
				removeActivator(a);
				assertThat(a.size()).as("activator size after remove activator").isEqualTo(0);
				addActivator(a);
				assertThat(a.size()).as("activator size after add activator").isEqualTo(2);

				assertThat(leaveSimuGroup(GROUP)).as("leaveSimuGroup").isEqualTo(SUCCESS);
				assertThat(a.size()).as("activator size after leave").isEqualTo(1);
				assertThat(other.leaveSimuGroup(GROUP)).as("other leave").isEqualTo(SUCCESS);
				assertThat(a.size()).as("activator size after other leave").isEqualTo(0);

				assertThat(createSimuGroup(GROUP)).as("createSimuGroup again").isEqualTo(SUCCESS);
				assertThat(requestSimuRole(GROUP, ROLE)).as("requestSimuRole third").isEqualTo(SUCCESS);
				assertThat(other.requestSimuRole(GROUP, ROLE)).as("other request simu role").isEqualTo(SUCCESS);
				assertThat(a.size()).as("activator size after both request").isEqualTo(2);
				killAgent(other);
				assertThat(a.size()).as("activator size after kill other").isEqualTo(1);
				resume();
			}
		});
	}

	@Test
	public void addAfterRequestRole() {
		runSimuTest(new TickBasedScheduler() {
			@Override
			public void onActivation() {
				assertThat(createSimuGroup("system")).as("create system group").isEqualTo(SUCCESS);
				assertThat(requestSimuRole("system", "site")).as("request site role").isEqualTo(SUCCESS);
				ReturnCode code;
				// ///////////////////////// REQUEST ROLE ////////////////////////
				Activator a = new EmptyActivator("system", "site");
				addActivator(a);
				assertThat(a.size()).as("activator size after add").isEqualTo(1);

				code = leaveSimuRole("system", "site");
				assertThat(code).as("leaveSimuRole return code").isEqualTo(SUCCESS);
				assertThat(a.size()).as("activator size after leave").isEqualTo(0);
				resume();
			}
		});
	}

}
