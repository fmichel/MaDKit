
package madkit.messages;

import madkit.action.KernelAction;

/**
 * KernelMessage is a message that can be sent to the kernel agent. It is used
 * to send MaDKit commands to the kernel agent.
 * 
 * @version 6
 * @since MaDKit 1.0
 *
 */
public class KernelMessage extends EnumMessage<KernelAction> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4125669035665965672L;

	/**
	 * Builds a kernel message with the specified enum code and command options
	 * 
	 * @param code           the enum constant from the {@link KernelAction}
	 *                       enumeration
	 * @param commandOptions a list of objects representing the command options
	 */
	public KernelMessage(KernelAction code, Object... commandOptions) {
		super(code, commandOptions);
	}
}
