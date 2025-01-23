package madkit.action;

import madkit.kernel.Agent;

public class AgentMethodAction extends ActionWithData {

	public AgentMethodAction(Agent agent, String methodName, Object... params) {
		super(methodName, _ -> {
			agent.handleRequestActionMessage(new RequestActionMessage(methodName, params));
		});

	}
}
