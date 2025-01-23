package madkit.agr;

import madkit.kernel.Agent;

/**
 * Defines key roles used by the MaDKit kernel or regular agents to achieve
 * specific CGR queries. See
 * {@link Agent#createGroup(String, String, boolean, madkit.kernel.Gatekeeper)}
 * for instance.
 * 
 * @author Fabien Michel
 * @since MaDKit 5.2
 * @version 6.0
 */
public class SystemRoles {

	/**
	 * Utility class
	 */
	private SystemRoles() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * This role is automatically given to agents that create a group. The value of
	 * this constant is {@value}.
	 */
	public static final String GROUP_MANAGER = "manager";
	/**
	 * This role is a temporary role used to exchange messages with a group's
	 * manager that one agent is not part of. The value of this constant is
	 * {@value}.
	 */
	public static final String GROUP_CANDIDATE = "candidate";

}
