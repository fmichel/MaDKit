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

	/**
	 * Constructs an {@link AgentRuntimeException} with the specified detail message and
	 * cause.
	 * 
	 * @param message the detail
	 * @param cause   the cause
	 */
	public AgentRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}
}
