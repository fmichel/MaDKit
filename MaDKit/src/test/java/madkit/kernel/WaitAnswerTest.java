package madkit.kernel;

import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

import static madkit.kernel.Agent.ReturnCode.SUCCESS;

import madkit.messaging.ForEverReplierAgent;
import madkit.testing.agents.CGRAgent;

/**
 *
 * @since MaDKit 5.0.0.8
 * @version 6.0.5
 * 
 */
@SuppressWarnings("all")
public class WaitAnswerTest extends MadkitConcurrentTestCase {

	@Test
	public void replyWithSameMessage() {
		runTest(new CGRAgent() {
			@Override
			public void behaviorInActivate() {
				launchAgent(new ForEverReplierAgent());
				Assertions.assertThat(send(new Message(), COMMUNITY, GROUP, ROLE)).isEqualTo(SUCCESS);
				Assertions.assertThat((Message) waitNextMessage(1000)).isNotNull();
				resume();
			}
		});
	}

//	@Test
//	public void returnSuccessOnCandidateRole() {
//		runTest(new CGRAgent() {
//			@Override
//			public void behaviorInActivate() {
//				threadAssertEquals(SUCCESS, launchAgent(target2));
//
//				// Without role
//				AgentAddress aa = getAgentWithRole(COMMUNITY, GROUP, SystemRoles.GROUP_MANAGER);
//				threadAssertNotNull(aa);
//				Message m = sendWaitReply(new Message(), aa, 1000);
//				threadAssertNotNull(m);
//				threadAssertEquals("reply", ((StringMessage) m).getContent());
//				threadAssertEquals(SystemRoles.GROUP_CANDIDATE, m.getReceiver().getRole());
//				threadAssertEquals(SystemRoles.GROUP_MANAGER, m.getSender().getRole());
//
//				// With role
//				m = sendWithRoleWaitReply(new Message(), aa, SystemRoles.GROUP_CANDIDATE, 1000);
//				threadAssertNotNull(m);
//				threadAssertEquals("reply2", ((StringMessage) m).getContent());
//				threadAssertEquals(SystemRoles.GROUP_CANDIDATE, m.getReceiver().getRole());
//				threadAssertEquals(SystemRoles.GROUP_MANAGER, m.getSender().getRole());
//			}
//		});
//	}

	@Test
	public void givenMessage_whenReplyAndWait_thenReplyReceived() { // Renamed method to follow Given-When-Then schema
		runTest(new CGRAgent() {
			@Override
			public void behaviorInActivate() {
				// launchAgent does not take a timeout here
				launchAgent(new ForEverReplierAgent());
				Message m = sendWaitReply(new Message(), COMMUNITY, GROUP, ROLE, null);
				Message reply = new Message();
				reply(reply, m);
				m = waitAnswer(reply);
				Assertions.assertThat(m).isNotNull();
				reply = new Message();
				reply(reply, m);
				m = waitAnswer(reply);
				Assertions.assertThat(m).isNotNull();
				resume();
			}
		});
	}

	@Test
	public void returnBadCGR() {
		runTest(new CGRAgent() {
			@Override
			public void behaviorInActivate() {
				launchAgent(new ForEverReplierAgent());

				AgentAddress aa = getAgentWithRole(COMMUNITY, GROUP, ROLE);
				Assertions.assertThat((Message) sendWithRoleWaitReply(new Message(), aa, cgrDontExist(), 1000)).isNull();// not
				// role
				// warning
				Assertions.assertThat(leaveGroup(COMMUNITY, GROUP)).isEqualTo(SUCCESS);
				Assertions.assertThat((Message) sendWaitReply(new Message(), aa, 1000)).isNull();// not in
				// group
				// warning
				resume();
			}
		});
	}

	@Test
	public void nullArgs() {
		runTest(new CGRAgent() {
			@Override
			public void behaviorInActivate() {
				AgentAddress aa = getAgentWithRole(COMMUNITY, GROUP, ROLE);
				try {
					sendWaitReply(null, null, 1000); // Message=null, AgentAddress=null
					noExceptionFailure();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
				try {
					sendWaitReply(new Message(), null, 1000); // Message non-null, AgentAddress=null
					noExceptionFailure();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
				try {
					sendWaitReply(null, aa, 1000); // Message=null, AgentAddress non-null
					noExceptionFailure();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
				resume();
			}
		});
	}

}