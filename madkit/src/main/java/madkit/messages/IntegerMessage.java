
package madkit.messages;

/**
 * A message class that conveys an integer.
 * 
 * @author Fabien Michel
 * @since MaDKit 5.0.1
 * @version 0.9
 */
public class IntegerMessage extends ObjectMessage<Integer> {

	private static final long serialVersionUID = 1L;

	/**
	 * Builds a new message containing the integer i
	 * 
	 * @param i the integer
	 */
	public IntegerMessage(Integer i) {
		super(i);
	}

	/**
	 * Builds a new message containing the integer 0
	 * 
	 */
	public IntegerMessage() {
		super(0);
	}

}
