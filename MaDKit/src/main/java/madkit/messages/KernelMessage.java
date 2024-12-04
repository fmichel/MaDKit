
package madkit.messages;

import madkit.action.KernelAction;

/**
 * The brand new version of KernelMessage. For now its purpose is to allow
 * agents to send to the kernel agent some MaDKit commands such as launchAgent.
 * 
 * @author Olivier Gutknecht
 * @author Fabien Michel
 * @version 6
 * @since MaDKit 1.0
 *
 */
public class KernelMessage extends EnumMessage<KernelAction> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4125669035665965672L;

//	public KernelMessage(madkit.newact.KernelAction code, Object... commandOptions) {
//		super(code, commandOptions);
//	}
	public KernelMessage(madkit.action.KernelAction code, Object... commandOptions) {
		super(code, commandOptions);
	}
}
