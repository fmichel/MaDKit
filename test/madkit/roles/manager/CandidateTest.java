package madkit.roles.manager;

import static org.junit.Assert.assertNotNull;

import java.util.logging.Level;

import madkit.agr.Organization;
import madkit.kernel.Agent;
import madkit.kernel.Gatekeeper;
import madkit.kernel.JunitMadkit;
import madkit.kernel.Madkit.LevelOption;
import madkit.kernel.Message;
import madkit.testing.util.agent.NormalAgent;

import org.junit.Test;


public class CandidateTest extends JunitMadkit {
	
	@Test
	public void createGroupHook() {
		addMadkitArgs(LevelOption.agentLogLevel.toString(), Level.ALL.toString()
//				,LevelOption.kernelLogLevel.toString(),Level.ALL.toString()
				);
		launchTest(new NormalAgent() {
			/**
			 * 
			 */
			private static final long	serialVersionUID	= 5220269193648621017L;

			@Override
			protected void activate() {
				launchAgent(new Agent(){
					/**
					 * 
					 */
					private static final long	serialVersionUID	= -4920636059739869931L;
					@Override
					protected void activate() {
						createGroup(COMMUNITY, GROUP,false,new Gatekeeper() {
							@Override
							public boolean allowAgentToTakeRole(String requesterID, String roleName, Object memberCard) {
								return false;
							}
						});
					}
					@Override
					protected void live() {
						setLogLevel(Level.ALL);
						sendReply(waitNextMessage(), new Message());
					}
				});
						sendMessage(COMMUNITY, 
								GROUP, 
								Organization.GROUP_MANAGER_ROLE, 
								new Message());
						pause(10);
						Message m = waitNextMessage();
						assertNotNull(m);
						System.err.println(m);
			}
		});
		pause(100);
	}

}
