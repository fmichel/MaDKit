
package madkit.kernel;

import madkit.kernel.Agent.ReturnCode;

/**
 * @author Fabien Michel
 * @version 0.91
 * @since MaDKit 5.0
 * 
 */
class MadkitWarning extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2819758946574640705L;
	/**
	 * 
	 */
	protected final ReturnCode code;

	MadkitWarning(String message, ReturnCode code) {
		super(message);
		this.code = code;
	}

	MadkitWarning(ReturnCode code) {
		this.code = code;
	}

	@Override
	public String getMessage() {
		String msg = super.getMessage();
		return code.name() + ": " + (msg == null ? "" : msg + " ") + code.toString();
	}

}