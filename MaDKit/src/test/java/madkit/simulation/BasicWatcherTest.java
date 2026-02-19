package madkit.simulation;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

import static madkit.kernel.Agent.ReturnCode.ALREADY_GROUP;
import static madkit.kernel.Agent.ReturnCode.SUCCESS;

import madkit.kernel.MadkitConcurrentTestCase;
import madkit.kernel.Probe;
import madkit.kernel.Watcher;

/**
 *
 * @since MaDKit 5.0.0.2
 * @version 6.0.5
 * 
 */

public class BasicWatcherTest extends MadkitConcurrentTestCase {

	// TODO implement a default simulation engine setup
	@Test
	public void addingNullProbe() {
		runSimuTest(new SimuAgent() {
			@Override
			public void onActivation() {
				Watcher s = new Watcher() {
					@Override
					protected void onActivation() {
						super.onActivation();
					}
				};
				assertThat(launchAgent(s)).as("launch watcher").isEqualTo(SUCCESS);
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
					Probe a = new Probe(GROUP, null);
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
				resume();
			}
		});
	}

	@Test
	public void addingAndRemovingProbes() {
		runSimuTest(new Watcher() {
			@Override
			protected void onActivation() {
				super.onActivation();
				// ///////////////////////// REQUEST ROLE ////////////////////////
				assertThat(createSimuGroup(GROUP)).as("createSimuGroup").isEqualTo(SUCCESS);
				assertThat(requestSimuRole(GROUP, ROLE)).as("requestSimuRole").isEqualTo(SUCCESS);
				Probe a = new Probe(GROUP, ROLE);
				addProbe(a);
				assertThat(a.size()).as("probe size after add").isEqualTo(1);

				ReturnCode code = leaveSimuRole(GROUP, ROLE);
				assertThat(code).as("leaveSimuRole return code").isEqualTo(SUCCESS);
				assertThat(a.size()).as("probe size after leave").isEqualTo(0);

				assertThat(createSimuGroup(GROUP)).as("createSimuGroup already").isEqualTo(ALREADY_GROUP);
				assertThat(requestSimuRole(GROUP, ROLE)).as("requestSimuRole again").isEqualTo(SUCCESS);

				assertThat(a.size()).as("probe size after request").isEqualTo(1);

				assertThat(leaveSimuGroup(GROUP)).as("leaveSimuGroup").isEqualTo(SUCCESS);
				assertThat(a.size()).as("probe size after leave group").isEqualTo(0);

				// Adding and removing while group does not exist
				removeProbe(a);
				assertThat(a.size()).as("probe size after remove").isEqualTo(0);
				addProbe(a);
				assertThat(a.size()).as("probe size after add").isEqualTo(0);

				assertThat(createSimuGroup(GROUP)).as("createSimuGroup before second requests").isEqualTo(SUCCESS);
				assertThat(requestSimuRole(GROUP, ROLE)).as("requestSimuRole second").isEqualTo(SUCCESS);
				SimuAgent other = new SimuAgent() {
					@Override
					protected void onActivation() {
						super.onActivation();
						assertThat(requestSimuRole(GROUP, ROLE)).as("other request simu role").isEqualTo(SUCCESS);
						resume();
					}
				};
				assertThat(launchAgent(other)).as("launch other simu agent").isEqualTo(SUCCESS);

				assertThat(a.size()).as("probe size after two agents").isEqualTo(2);
				removeProbe(a);
				assertThat(a.size()).as("probe size after remove probe").isEqualTo(0);

				addProbe(a);
				assertThat(a.size()).as("probe size after add probe").isEqualTo(2);

				assertThat(leaveSimuGroup(GROUP)).as("leaveSimuGroup again").isEqualTo(SUCCESS);
				assertThat(a.size()).as("probe size after leave").isEqualTo(1);
				assertThat(other.leaveSimuGroup(GROUP)).as("other leave").isEqualTo(SUCCESS);
				assertThat(a.size()).as("probe size after other leave").isEqualTo(0);

				assertThat(createSimuGroup(GROUP)).as("createSimuGroup again").isEqualTo(SUCCESS);
				assertThat(requestSimuRole(GROUP, ROLE)).as("requestSimuRole third").isEqualTo(SUCCESS);
				assertThat(other.requestSimuRole(GROUP, ROLE)).as("other request simu role third").isEqualTo(SUCCESS);
				assertThat(a.size()).as("probe size after both request").isEqualTo(2);
				resume();
			}
		});
	}

}
