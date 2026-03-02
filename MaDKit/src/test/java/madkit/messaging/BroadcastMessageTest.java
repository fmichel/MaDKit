package madkit.messaging;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;

import org.testng.annotations.Test;

import static madkit.kernel.Agent.ReturnCode.NOT_IN_GROUP;
import static madkit.kernel.Agent.ReturnCode.NO_RECIPIENT_FOUND;
import static madkit.kernel.Agent.ReturnCode.ROLE_NOT_HANDLED;
import static madkit.kernel.Agent.ReturnCode.SUCCESS;

import madkit.kernel.Agent;
import madkit.kernel.DefaultTestAgent;
import madkit.kernel.MadkitConcurrentTestCase;
import madkit.kernel.Message;
import madkit.messages.StringMessage;
import madkit.testing.agents.CGRAgent;

/**
 *
 * @since MaDKit 5.0.0.7
 * @version 0.9
 * 
 */

public class BroadcastMessageTest extends MadkitConcurrentTestCase {

	@Test
	public void returnSuccess() {
		runTest(new DefaultTestAgent() {
			@Override
			public void behaviorInActivate() {
				final Agent target = new CGRAgent();
				final DefaultTestAgent target2 = new DefaultTestAgent() {
					@Override
					public void behaviorInActivate() {
						assertThat(requestRole(COMMUNITY, GROUP, ROLE)).as("request role in target2").isEqualTo(SUCCESS);
						resume();
					}
				};
				assertThat(launchAgent(target)).as("launchAgent return code").isEqualTo(SUCCESS);
				assertThat(requestRole(COMMUNITY, GROUP, ROLE)).as("requestRole return code").isEqualTo(SUCCESS);

				// Without role
				getAgentsWithRole(COMMUNITY, GROUP, ROLE);
				assertThat(broadcast(new Message(), getAgentsWithRole(COMMUNITY, GROUP, ROLE)))
						.as("broadcast without role return code").isEqualTo(SUCCESS);
				Message m = target.nextMessage();
				assertThat(broadcast(new StringMessage("test"), getAgentsWithRole(COMMUNITY, GROUP, ROLE)))
						.as("broadcast string message return code").isEqualTo(SUCCESS);
				assertThat((Message) target.nextMessage()).as("target received message").isNotNull();
				assertThat(m.getReceiver().getRole()).as("message receiver role").isEqualTo(ROLE);

				// With role
				assertThat(broadcastWithRole(new Message(), getAgentsWithRole(COMMUNITY, GROUP, ROLE), ROLE))
						.as("broadcastWithRole return code").isEqualTo(SUCCESS);
				m = target.nextMessage();
				assertThat(m).as("message after broadcastWithRole").isNotNull();
				assertThat(m.getReceiver().getRole()).as("message receiver role for with-role").isEqualTo(ROLE);

				// verifying cloning
				assertThat(launchAgent(target2)).as("launchAgent target2 return code").isEqualTo(SUCCESS);
				assertThat(broadcastWithRole(new Message(), getAgentsWithRole(COMMUNITY, GROUP, ROLE), ROLE))
						.as("broadcastWithRole (cloning) return code").isEqualTo(SUCCESS);
				m = target.nextMessage();
				assertThat(m).as("first cloned message").isNotNull();
				Message m2 = target2.nextMessage();
				assertThat(m2.getReceiver().getRole()).as("second cloned message role").isEqualTo(ROLE);
				assertThat(m2.getConversationID()).as("conversation id equality").isEqualTo(m.getConversationID());
				assertThat(m2).isNotSameAs(m);
				resume();
			}
		});
	}

	@Test
	public void returnNotInGroup() {
		runTest(new DefaultTestAgent() {
			@Override
			public void behaviorInActivate() {
				final CGRAgent target = new CGRAgent();
				assertThat(launchAgent(target)).as("launchAgent return code").isEqualTo(SUCCESS);
				// Without role
				assertThat(broadcast(new Message(), getAgentsWithRole(COMMUNITY, GROUP, ROLE)))
						.as("broadcast without role when not in group").isEqualTo(NOT_IN_GROUP);

				// With role
				assertThat(broadcastWithRole(new Message(), getAgentsWithRole(COMMUNITY, GROUP, ROLE), ROLE))
						.as("broadcastWithRole when not in group").isEqualTo(NOT_IN_GROUP);
				resume();
			}
		});
	}

	@Test
	public void returnNotCGR() {
		runTest(new DefaultTestAgent() {
			@Override
			public void behaviorInActivate() {
				final CGRAgent target = new CGRAgent();
				assertThat(launchAgent(target)).as("launchAgent return code").isEqualTo(SUCCESS);
				assertThat(getAgentsWithRole(cgrDontExist(), GROUP, ROLE)).as("agents list for non-existent community")
						.isEqualTo(Collections.EMPTY_LIST);
				assertThat(getAgentsWithRole(COMMUNITY, cgrDontExist(), ROLE)).as("agents list for non-existent group")
						.isEqualTo(Collections.EMPTY_LIST);
				assertThat(getAgentsWithRole(COMMUNITY, GROUP, cgrDontExist())).as("agents list for non-existent role")
						.isEqualTo(Collections.EMPTY_LIST);
				resume();
			}
		});
	}

	@Test
	public void returnRoleNotHandled() {
		runTest(new DefaultTestAgent() {
			@Override
			public void behaviorInActivate() {
				final CGRAgent target = new CGRAgent();
				assertThat(launchAgent(target)).as("launchAgent return code").isEqualTo(SUCCESS);
				assertThat(requestRole(COMMUNITY, GROUP, ROLE)).as("requestRole return code").isEqualTo(SUCCESS);
				assertThat(broadcastWithRole(new Message(), getAgentsWithRole(COMMUNITY, GROUP, ROLE), cgrDontExist()))
						.as("broadcastWithRole role not handled").isEqualTo(ROLE_NOT_HANDLED);
				resume();
			}
		});
	}

	@Test
	public void returnNoRecipientFound() {
		runTest(new DefaultTestAgent() {
			@Override
			public void behaviorInActivate() {
				final CGRAgent target = new CGRAgent();
				assertThat(launchAgent(target)).as("launchAgent return code").isEqualTo(SUCCESS);
				assertThat(requestRole(COMMUNITY, GROUP, ROLE)).as("requestRole return code").isEqualTo(SUCCESS);
				assertThat(target.leaveRole(COMMUNITY, GROUP, ROLE)).as("target leave role").isEqualTo(SUCCESS);
				assertThat(broadcast(new Message(), getAgentsWithRole(COMMUNITY, GROUP, ROLE))).as("broadcast no recipient")
						.isEqualTo(NO_RECIPIENT_FOUND);
				assertThat(broadcastWithRole(new Message(), getAgentsWithRole(COMMUNITY, GROUP, ROLE), ROLE))
						.as("broadcastWithRole no recipient").isEqualTo(NO_RECIPIENT_FOUND);
				resume();
			}
		});
	}

	@Test
	public void nullArgs() {
		runTest(new DefaultTestAgent() {
			@Override
			public void behaviorInActivate() {
				final CGRAgent target = new CGRAgent();
				assertThat(launchAgent(target)).as("launchAgent return code").isEqualTo(SUCCESS);
				try {
					broadcast(null, null);
					noExceptionFailure();
				} catch (NullPointerException e) {
					// e.printStackTrace();
				}
				resume();
			}
		});
	}

}
