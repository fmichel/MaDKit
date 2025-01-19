package madkit.kernel;

/**
 * This Exception is thrown when an agent runtime error occurs. It is thrown by agents
 * when they encounter an error that prevents them from executing.
 * 
 */
public class AgentRuntimeException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructs an {@link AgentRuntimeException} with the specified detail message.
	 * 
	 * @param message the detail message
	 */
	public AgentRuntimeException(String message) {
		super(message);
	}
}
