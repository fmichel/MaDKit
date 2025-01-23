package madkit.action;

import madkit.messages.StringMessage;

public class RequestActionMessage extends StringMessage {

	private Object[] parameters;

	public RequestActionMessage(String methodName, Object... parameters) {
		super(methodName);
		this.parameters = parameters;
	}

	/**
	 * @return the parameters
	 */
	public Object[] getParameters() {
		return parameters;
	}
}
