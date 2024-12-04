
package madkit.messages;

import java.util.Arrays;

/**
 * This parameterizable class could be used to build a message tagged with an
 * enumeration and conveying any java objects using an array of Object.
 * 
 * @author Fabien Michel
 * @version 5.0
 * @since MaDKit 5.0.0.14
 *
 */
public class EnumMessage<E extends Enum<E>> extends ObjectMessage<Object[]> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2129358510239154730L;
	private final E code;

	/**
	 * Builds a message with the specified content
	 * 
	 * @param code       an enum constant of type E
	 * @param parameters a list of objects
	 */
	public EnumMessage(E code, final Object... parameters) {
		super(parameters);
		this.code = code;
	}

	@Override
	public String toString() {
		String s = super.toString() + "\n" + (getClass().getSimpleName() + getConversationID()).replaceAll(".", " ");
		return s + "    command: " + code.name() + " {" + Arrays.deepToString(getContent()) + "}";
	}

	/**
	 * @return the enum constant which has been used to construct this message
	 */
	public E getCode() {
		return code;
	}
}