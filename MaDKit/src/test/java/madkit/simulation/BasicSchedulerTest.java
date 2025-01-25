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

import static madkit.kernel.Agent.ReturnCode.ALREADY_GROUP;
import static madkit.kernel.Agent.ReturnCode.SUCCESS;

import org.testng.annotations.Test;

import madkit.kernel.Activator;
import madkit.kernel.Agent.ReturnCode;
import madkit.kernel.MadkitUnitTestCase;
import madkit.simulation.scheduler.TickBasedScheduler;
import madkit.test.agents.CGRAgent;
import madkit.test.agents.SimulatedAgent;

/**
 *
 * @since MaDKit 5.0.0.2
 * @version 0.9
 * 
 */

public class BasicSchedulerTest extends MadkitUnitTestCase {

	@Test
	public void givenSimulationEngine_whenLaunchScheduler_works() {
		TickBasedScheduler s = new TickBasedScheduler();
		launchSimuAgentTest(s);
	}

	@Test
	public void givenNewActivator_whenAddedBeforeAgentsJoin_thenSizeIsCorrect() {
		launchSimuAgentTest(new TickBasedScheduler() {
			@Override
			protected void onActivation() {
				super.onActivation();
				EmptyActivator ea = new EmptyActivator(GROUP, ROLE);
				addActivator(ea);
				launchAgent(new SimulatedAgent());
				threadAssertEquals(1, ea.size());
			}
		});
	}

	@Test
	public void givenNewActivator_whenAddedAfterAgentsJoined_thenSizeIsCorrect() {
		launchSimuAgentTest(new TickBasedScheduler() {
			@Override
			protected void onActivation() {
				super.onActivation();
				launchAgent(new SimulatedAgent());
				EmptyActivator ea = new EmptyActivator(GROUP, ROLE);
				addActivator(ea);
				threadAssertEquals(1, ea.size());
			}
		});
	}

	@Test
	public void addingNullActivatorExceptionPrint() {
		launchTestedAgent(new CGRAgent() {
			@Override
			protected void onActivation() {
				Activator a = new EmptyActivator(null, null);
			}
		}, ReturnCode.AGENT_CRASH);
	}

	@Test
	public void addingAndRemovingActivators() {
		launchSimuAgentTest(new TickBasedScheduler() {
			@Override
			protected void onActivation() {
				// ///////////////////////// REQUEST ROLE ////////////////////////
				Activator a = new EmptyActivator(GROUP, ROLE);
				addActivator(a);
				threadAssertEquals(0, a.size());

				threadAssertEquals(SUCCESS, createSimuGroup(GROUP));
				threadAssertEquals(ALREADY_GROUP, createSimuGroup(GROUP));
				threadAssertEquals(SUCCESS, requestSimuRole(GROUP, ROLE));

				threadAssertEquals(1, a.size());

				threadAssertEquals(SUCCESS, leaveSimuGroup(GROUP));
				threadAssertEquals(0, a.size());

				// Adding and removing while group does not exist
				removeActivator(a);
				threadAssertEquals(0, a.size());
				addActivator(a);
				threadAssertEquals(0, a.size());

				threadAssertEquals(SUCCESS, createSimuGroup(GROUP));
				threadAssertEquals(SUCCESS, requestSimuRole(GROUP, ROLE));
				SimuAgent other = new SimuAgent() {
					@Override
					protected void onActivation() {
						threadAssertEquals(SUCCESS, requestSimuRole(GROUP, ROLE));
					}
				};
				threadAssertEquals(SUCCESS, launchAgent(other));

				threadAssertEquals(2, a.size());
				removeActivator(a);
				threadAssertEquals(0, a.size());
				addActivator(a);
				threadAssertEquals(2, a.size());

				threadAssertEquals(SUCCESS, leaveSimuGroup(GROUP));
				threadAssertEquals(1, a.size());
				threadAssertEquals(SUCCESS, other.leaveSimuGroup(GROUP));
				threadAssertEquals(0, a.size());

				threadAssertEquals(SUCCESS, createSimuGroup(GROUP));
				threadAssertEquals(SUCCESS, requestSimuRole(GROUP, ROLE));
				threadAssertEquals(SUCCESS, other.requestSimuRole(GROUP, ROLE));
				threadAssertEquals(2, a.size());
				killAgent(other);
				threadAssertEquals(1, a.size());
			}
		});
	}

	@Test
	public void addAfterRequestRole() {
		launchSimuAgentTest(new TickBasedScheduler() {
			@Override
			protected void onActivation() {
				threadAssertEquals(SUCCESS, createSimuGroup("system"));
				threadAssertEquals(SUCCESS, requestSimuRole("system", "site"));
				ReturnCode code;
				// ///////////////////////// REQUEST ROLE ////////////////////////
				Activator a = new EmptyActivator("system", "site");
				addActivator(a);
				threadAssertEquals(1, a.size());

				code = leaveSimuRole("system", "site");
				threadAssertEquals(SUCCESS, code);
				threadAssertEquals(0, a.size());
			}
		});
	}

}
