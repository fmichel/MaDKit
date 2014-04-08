package madkit.networking.messaging;

import static madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.logging.Level;

import madkit.kernel.JunitMadkit;
import madkit.kernel.Madkit.BooleanOption;
import madkit.kernel.Madkit.LevelOption;
import madkit.kernel.Message;
import madkit.testing.util.agent.ForEverAgent;
import madkit.testing.util.agent.ForEverReplierAgent;
import madkit.testing.util.agent.NormalAgent;
import madkit.testing.util.agent.OneReplyAndQuitAgent;

import org.junit.Test;


public class NetworkReplyingSystemTest extends JunitMadkit {
	
	@Test
	public void ping() {
		addMadkitArgs(BooleanOption.network.toString(), LevelOption.kernelLogLevel.toString(), "ALL"
				,LevelOption.networkLogLevel.toString(), "FINE"
				);
		launchTest(new NormalAgent() {
			protected void activate() {
				assertEquals(SUCCESS, createGroup(COMMUNITY, GROUP, true));
				assertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
				final ForEverAgent agent = new ForEverAgent();
				launchAgent(agent);
				setLogLevel(Level.ALL);
				assertTrue(isKernelOnline());
				Message m = new Message(); 
				for (int i = 0; i < 10; i++) {
					sendMessage(COMMUNITY, GROUP, ROLE, new Message());
				}
				killAgent(agent);
				launchExternalNetworkInstance(NetworkMessageAgent.class);
				pause(5000);
				Message message = new Message();
				sendMessage(COMMUNITY, GROUP, ROLE, message );
				if(logger != null)
					logger.info(message.toString());
				Message m2 = sendMessageAndWaitForReply(COMMUNITY, GROUP, ROLE, m, 10000);
				cleanHelperMDKs(1000);
				System.err.println(m);
				assertNull(m2);
				System.err.println(nextMessage());
				System.err.println(nextMessage());
				System.err.println(nextMessage());
				System.err.println(nextMessage());
			}
		});
	}

	@Test
	public void sendReplyTest() {
		addMadkitArgs(BooleanOption.network.toString(), LevelOption.kernelLogLevel.toString(), "ALL"
				,LevelOption.networkLogLevel.toString(), "FINE"
				);
		launchTest(new NormalAgent() {
			protected void activate() {
				super.activate();
				assertTrue(isKernelOnline());
				launchExternalNetworkInstance(ForEverReplierAgent.class);
				waitNextMessage(10000);
				Message m = sendMessageAndWaitForReply(COMMUNITY, GROUP, ROLE, new Message());
				m = sendReplyAndWaitForReply(m, new Message());
				assertNotNull(m);
				m = sendReplyAndWaitForReply(m, new Message());
				assertNotNull(m);
			}
		});
	}


	@Test
	public void sendReplyAfterDeconnection() {
		addMadkitArgs(BooleanOption.network.toString(), LevelOption.kernelLogLevel.toString(), "ALL"
				,LevelOption.networkLogLevel.toString(), "FINE"
				);
		launchTest(new NormalAgent() {
			protected void activate() {
				super.activate();
				assertTrue(isKernelOnline());
				launchExternalNetworkInstance(OneReplyAndQuitAgent.class);
				waitNextMessage(10000);
				Message m = sendMessageAndWaitForReply(COMMUNITY, GROUP, ROLE, new Message());
				assertNotNull(m);
				cleanHelperMDKs(2000);
				sendMessage(m.getSender(), new Message());
			}
		});
	}


}
