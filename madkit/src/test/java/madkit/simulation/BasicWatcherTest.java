
package madkit.simulation;

import static madkit.kernel.Agent.ReturnCode.ALREADY_GROUP;
import static madkit.kernel.Agent.ReturnCode.SUCCESS;

import org.testng.annotations.Test;

import madkit.kernel.Agent;
import madkit.kernel.JunitMadkit;
import madkit.kernel.Watcher;
import madkit.kernel.Probe;
import madkit.test.agents.CGRAgent;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.2
 * @version 0.9
 * 
 */

public class BasicWatcherTest extends JunitMadkit {

	@Test
	public void addingNullProbe() {
		launchTestedAgent(new CGRAgent() {
			@Override
			protected void onActivation() {
				super.onActivation();
				Watcher s = new Watcher();
				threadAssertEquals(SUCCESS, launchAgent(s));
				try {
					Probe a = new Probe(null, null, null);
					s.addProbe(a);
					noExceptionFailure();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
				try {
					Probe a = new Probe(COMMUNITY, null, null);
					s.addProbe(a);
					noExceptionFailure();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
				try {
					Probe a = new Probe(COMMUNITY, GROUP, null);
					s.addProbe(a);
					noExceptionFailure();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
				try {
					Probe a = new Probe(null, GROUP, null);
					s.addProbe(a);
					noExceptionFailure();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
				try {
					Probe a = new Probe(null, null, ROLE);
					s.addProbe(a);
					noExceptionFailure();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
			}
		});
	}

	@Test
	public void addingAndRemovingProbes() {
		launchTestedAgent(new CGRAgent() {
			@Override
			protected void onActivation() {
				super.onActivation();
				Watcher s = new Watcher();
				threadAssertEquals(SUCCESS, launchAgent(s));
				ReturnCode code;
				// ///////////////////////// REQUEST ROLE ////////////////////////
				Probe a = new Probe(COMMUNITY, GROUP, ROLE);
				s.addProbe(a);
				threadAssertEquals(1, a.size());

				code = leaveRole(COMMUNITY, GROUP, ROLE);
				threadAssertEquals(SUCCESS, code);
				threadAssertEquals(0, a.size());

				threadAssertEquals(ALREADY_GROUP, createGroup(COMMUNITY, GROUP, false, null));
				threadAssertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE, null));

				threadAssertEquals(1, a.size());

				threadAssertEquals(SUCCESS, leaveGroup(COMMUNITY, GROUP));
				threadAssertEquals(0, a.size());

				// Adding and removing while group does not exist
				s.removeProbe(a);
				threadAssertEquals(0, a.size());
				s.addProbe(a);
				threadAssertEquals(0, a.size());

				threadAssertEquals(SUCCESS, createGroup(COMMUNITY, GROUP, false, null));
				threadAssertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE, null));
				Agent other = new Agent() {
					@Override
					protected void onActivation() {
						threadAssertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE, null));
					}
				};
				threadAssertEquals(SUCCESS, launchAgent(other));

				threadAssertEquals(2, a.size());
				s.removeProbe(a);
				threadAssertEquals(0, a.size());

				s.addProbe(a);
				threadAssertEquals(2, a.size());

				threadAssertEquals(SUCCESS, leaveGroup(COMMUNITY, GROUP));
				threadAssertEquals(1, a.size());
				threadAssertEquals(SUCCESS, other.leaveGroup(COMMUNITY, GROUP));
				threadAssertEquals(0, a.size());

				threadAssertEquals(SUCCESS, createGroup(COMMUNITY, GROUP, false, null));
				threadAssertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE, null));
				threadAssertEquals(SUCCESS, other.requestRole(COMMUNITY, GROUP, ROLE, null));
				threadAssertEquals(2, a.size());

				killAgent(s);
				threadAssertEquals(0, a.size());
			}
		});
	}

}
