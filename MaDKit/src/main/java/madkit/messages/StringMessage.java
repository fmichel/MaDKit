package madkit.messages;

/**
 * A message class that conveys a string.
 * 
 * @author Fabien Michel
 * @since MaDKit 5.0.0.14
 * @version 0.9
 * 
 */
public class StringMessage extends ObjectMessage<String> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3111467569749360801L;

	/**
	 * Builds a new message containing the string s
	 * 
	 * @param s the string
	 */
	public StringMessage(String s) {
		super(s);
	}

	/**
	 * Builds a new message containing an empty string
	 */
	public StringMessage() {
		this("");
	}

}