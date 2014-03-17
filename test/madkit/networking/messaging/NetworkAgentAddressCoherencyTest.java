package madkit.networking.messaging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import madkit.kernel.AgentAddress;
import madkit.kernel.JunitMadkit;
import madkit.kernel.Madkit.BooleanOption;
import madkit.kernel.Madkit.LevelOption;
import madkit.kernel.Message;
import madkit.message.ObjectMessage;
import madkit.testing.util.agent.ForEverSendRepliesWithTheSameMessageAgent;
import madkit.testing.util.agent.NormalAgent;

import org.junit.Test;


public class NetworkAgentAddressCoherencyTest extends JunitMadkit {
	
	@Test
	public void isCorrectlyLocalAfterTravelingTheNetwork() {
		addMadkitArgs(BooleanOption.network.toString(), LevelOption.kernelLogLevel.toString(), "ALL"
				,LevelOption.networkLogLevel.toString(), "FINE"
				);
		launchTest(new NormalAgent() {
			protected void activate() {
				super.activate();
				assertTrue(isKernelOnline());
				launchExternalNetworkInstance(NetworkMessageAgent.class);
				Message message = waitNextMessage(10000);
				if(logger != null)
					logger.info(message.toString());
				assertFalse(message.getSender().isFrom(getKernelAddress()));
				AgentAddress local = getAgentAddressIn(COMMUNITY, GROUP, ROLE);
				assertTrue(local.isFrom(getKernelAddress()));
				final AgentAddress receiver = message.getReceiver();
				assertTrue(receiver.equals(local));
				assertTrue(receiver.isFrom(getKernelAddress()));
				cleanHelperMDKs();
			}
		});
	}

	@Test
	public void couldBeUsedLocallyAfterTravelingTheNetwork() {
		addMadkitArgs(BooleanOption.network.toString(), LevelOption.kernelLogLevel.toString(), "ALL"
//				,LevelOption.networkLogLevel.toString(), "FINEST"
				,BooleanOption.network.toString()
				);
		launchTest(new NormalAgent() {
			protected void activate() {
				super.activate();
				requestRole(COMMUNITY, GROUP, ROLE2);
				launchExternalNetworkInstance(ForEverSendRepliesWithTheSameMessageAgent.class);
				Message message = waitNextMessage(10000);
				if(logger != null){
					logger.info(message.toString());
					logger.info("sender is local ?"+message.getSender().isFrom(getKernelAddress()));
				}
				assertFalse(message.getSender().isFrom(getKernelAddress()));
				AgentAddress local = getAgentAddressIn(COMMUNITY, GROUP, ROLE2);
				assertTrue(local.isFrom(getKernelAddress()));
				message = sendMessageAndWaitForReply(COMMUNITY, GROUP, ROLE, new ObjectMessage<>(local),10000);
				final AgentAddress receiver = message.getReceiver();
				assertFalse(message.getSender().isFrom(getKernelAddress()));
				assertTrue(receiver.isFrom(getKernelAddress()));
				
				logger.info(message.toString());
				ObjectMessage<AgentAddress> m = (ObjectMessage<AgentAddress>) message;
				final AgentAddress myAA = m.getContent();
				logger.info(local.toString());
				logger.info(myAA.toString());
				assertTrue(local.equals(myAA));
				assertTrue(myAA.isFrom(getKernelAddress()));
				sendMessage(myAA, new Message());
				assertNotNull(nextMessage());
				assertTrue(checkAgentAddress(myAA));
				assertEquals(ReturnCode.SUCCESS, leaveRole(COMMUNITY, GROUP, ROLE2));
				assertFalse(checkAgentAddress(myAA));
				cleanHelperMDKs();
			}
		});
	}

	@Test
	public void isCorrectlyLocalAfterEncapsulatingAAInMessage() {
		addMadkitArgs(BooleanOption.network.toString(), LevelOption.kernelLogLevel.toString(), "ALL"
				,LevelOption.networkLogLevel.toString(), "FINE"
				);
		launchTest(new NormalAgent() {
			protected void activate() {
				super.activate();
				assertTrue(isKernelOnline());
				launchExternalNetworkInstance(ForEverSendRepliesWithTheSameMessageAgent.class);
				Message message = waitNextMessage(10000);
				if(logger != null){
					logger.info(message.toString());
					logger.info("sender is local ?"+message.getSender().isFrom(getKernelAddress()));
				}
				assertFalse(message.getSender().isFrom(getKernelAddress()));
				AgentAddress local = getAgentAddressIn(COMMUNITY, GROUP, ROLE);
				assertTrue(local.isFrom(getKernelAddress()));
				message = sendMessageAndWaitForReply(COMMUNITY, GROUP, ROLE, new ObjectMessage<>(local),10000);
				final AgentAddress receiver = message.getReceiver();
				assertTrue(receiver.equals(local));
				assertTrue(receiver.isFrom(getKernelAddress()));
				ObjectMessage<AgentAddress> m = (ObjectMessage<AgentAddress>) message;
				assertTrue(receiver.equals(m.getContent()));
				assertTrue(m.getContent().isFrom(getKernelAddress()));
				cleanHelperMDKs();
			}
		});
	}


}
