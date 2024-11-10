
package madkit.messages;


/**
 * A message class that conveys a boolean value.
 * 
 * @author Fabien Michel
 * @since MadKit 5.0.0.20
 * @version 0.9
 * 
 */
public class BooleanMessage extends ObjectMessage<Boolean> {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 6520364212151479221L;

	/**
	 * Builds a new message containing the boolean b
	 * @param b a boolean value
	 */
	public BooleanMessage(Boolean b) {
		super(b);
	}

	
}
