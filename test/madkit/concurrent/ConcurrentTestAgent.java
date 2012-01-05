package madkit.concurrent;

import org.omg.PortableInterceptor.SUCCESSFUL;

import madkit.kernel.Agent;
import static madkit.kernel.JunitMadKit.*;

public class ConcurrentTestAgent extends Agent {



	private boolean playing = false;

	@Override
	protected void live() {
		while (true) {
			if (Math.random() < .5) {
				if (createGroup(COMMUNITY, GROUP) == ReturnCode.SUCCESS) {
					playing = true;
					if (requestRole(COMMUNITY, GROUP, ROLE) != ReturnCode.SUCCESS) {
						testFails(new Exception());
						return;
					}
				} 
				else {
					requestRole(COMMUNITY, GROUP, ROLE);
				}
			}
			else if(playing){
				if (Math.random() < .5) {
					if (leaveRole(COMMUNITY, GROUP, ROLE) != ReturnCode.SUCCESS) {
						testFails(new Exception());
						return;
					}
					playing = false;
				} else {
					if (leaveGroup(COMMUNITY, GROUP) != ReturnCode.SUCCESS){
						testFails(new Exception());
						return;
					}
					playing = false;
				}
			}
		}
	}
}

class ConcurrentTestAgentBis extends ConcurrentTestAgent{
	@Override
	protected void live() {
		while (true) {
			String groupName = GROUP+((int) (Math.random()*10));
			if (Math.random() < .5) {
				if (createGroup(COMMUNITY, groupName) == ReturnCode.SUCCESS) {
					if (requestRole(COMMUNITY, groupName, ROLE) != ReturnCode.SUCCESS) {
						testFails(new Exception());
						return;
					}
				} 
				else {
					requestRole(COMMUNITY, groupName, ROLE);
				}
			}
			else{
				if (Math.random() < .5) {
					leaveRole(COMMUNITY, groupName, ROLE);
				} else {
					leaveGroup(COMMUNITY, groupName);
				}
			}
		}
	}
	
}