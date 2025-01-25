
package madkit.simulation;

import static madkit.kernel.Agent.ReturnCode.ALREADY_GROUP;
import static madkit.kernel.Agent.ReturnCode.SUCCESS;

import org.testng.annotations.Test;

import madkit.kernel.MadkitUnitTestCase;
import madkit.kernel.Probe;
import madkit.kernel.Watcher;

/**
 *
 * @since MaDKit 5.0.0.2
 * @version 0.9
 * 
 */

public class BasicWatcherTest extends MadkitUnitTestCase {

	// TODO implement a default simulation engine setup
//	@Test
//	public void addingNullProbe() {
//		launchTestedAgent(new CGRAgent() {
//			@Override
//			protected void onActivation() {
//				super.onActivation();
//				Watcher s = new Watcher() {
//					@Override
//					protected void onActivation() {
//						super.onActivation();
//					}
//				};
//				threadAssertEquals(SUCCESS, launchAgent(s));
//				try {
//					Probe a = new Probe(null, null, null);
//					s.addProbe(a);
//					noExceptionFailure();
//				} catch (NullPointerException e) {
//					e.printStackTrace();
//				}
//				try {
//					Probe a = new Probe(COMMUNITY, null, null);
//					s.addProbe(a);
//					noExceptionFailure();
//				} catch (NullPointerException e) {
//					e.printStackTrace();
//				}
//				try {
//					Probe a = new Probe(GROUP, null);
//					s.addProbe(a);
//					noExceptionFailure();
//				} catch (NullPointerException e) {
//					e.printStackTrace();
//				}
//				try {
//					Probe a = new Probe(null, GROUP, null);
//					s.addProbe(a);
//					noExceptionFailure();
//				} catch (NullPointerException e) {
//					e.printStackTrace();
//				}
//				try {
//					Probe a = new Probe(null, null, ROLE);
//					s.addProbe(a);
//					noExceptionFailure();
//				} catch (NullPointerException e) {
//					e.printStackTrace();
//				}
//			}
//		});
//	}

	@Test
	public void addingAndRemovingProbes() {
		launchSimuAgentTest(new Watcher() {
			@Override
			protected void onActivation() {
				super.onActivation();
				// ///////////////////////// REQUEST ROLE ////////////////////////
				threadAssertEquals(SUCCESS, createSimuGroup(GROUP));
				threadAssertEquals(SUCCESS, requestSimuRole(GROUP, ROLE));
				Probe a = new Probe(GROUP, ROLE);
				addProbe(a);
				threadAssertEquals(1, a.size());

				ReturnCode code = leaveSimuRole(GROUP, ROLE);
				threadAssertEquals(SUCCESS, code);
				threadAssertEquals(0, a.size());

				threadAssertEquals(ALREADY_GROUP, createSimuGroup(GROUP));
				threadAssertEquals(SUCCESS, requestSimuRole(GROUP, ROLE));

				threadAssertEquals(1, a.size());

				threadAssertEquals(SUCCESS, leaveSimuGroup(GROUP));
				threadAssertEquals(0, a.size());

				// Adding and removing while group does not exist
				removeProbe(a);
				threadAssertEquals(0, a.size());
				addProbe(a);
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
				removeProbe(a);
				threadAssertEquals(0, a.size());

				addProbe(a);
				threadAssertEquals(2, a.size());

				threadAssertEquals(SUCCESS, leaveSimuGroup(GROUP));
				threadAssertEquals(1, a.size());
				threadAssertEquals(SUCCESS, other.leaveSimuGroup(GROUP));
				threadAssertEquals(0, a.size());

				threadAssertEquals(SUCCESS, createSimuGroup(GROUP));
				threadAssertEquals(SUCCESS, requestSimuRole(GROUP, ROLE));
				threadAssertEquals(SUCCESS, other.requestSimuRole(GROUP, ROLE));
				threadAssertEquals(2, a.size());
			}
		});
	}

}
