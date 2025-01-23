package madkit.action;

import madkit.kernel.Agent;

public class RequestAgentMessageAction extends ActionWithData {

	public RequestAgentMessageAction(Agent agent, String methodName, Object... params) {
		super(methodName, _ -> {
			agent.receiveMessage(new RequestActionMessage(methodName, params));
		});
	}

}
