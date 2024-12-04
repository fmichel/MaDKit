package madkit.kernel;

import madkit.kernel.Agent.ReturnCode;

public final class CGRNotAvailable extends RuntimeException {

	private static final long serialVersionUID = -375379801933609564L;
	private final ReturnCode code;

	/**
	 * @return the code
	 */
	final ReturnCode getCode() {
		return code;
	}

	@Override
	public String toString() {
		return super.toString() + " " + getCode();
	}

	/**
	 * @param notCommunity
	 */
	CGRNotAvailable(ReturnCode code) {
		this.code = code;
	}
}
