package madkit.samples;

import static madkit.kernel.Agent.ReturnCode.SUCCESS;

import madkit.kernel.DefaultTestAgent;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

import madkit.kernel.MadkitConcurrentTestCase;

/**
 * Proof-of-concept test demonstrating that MDK-samples can consume MaDKit's
 * shared test fixtures to write kernel-dependent agent tests.
 */
public class AgentCGRTest extends MadkitConcurrentTestCase {

	@Test
	void givenAgent_whenCreateGroupAndRequestRole_thenSuccess() {
		// Given
		runTest(new DefaultTestAgent() {
			@Override
			public void behaviorInActivate() {
				// When
				var createResult = createGroup(COMMUNITY, GROUP);
				var requestResult = requestRole(COMMUNITY, GROUP, ROLE);

				// Then
				assertThat(createResult).isEqualTo(SUCCESS);
				assertThat(requestResult).isEqualTo(SUCCESS);
				resume();
			}
		});
	}
}
