/**
 * 
 */
package madkit.performance;

import madkit.kernel.AbstractAgent;

public class MiniAgent extends AbstractAgent{
	/**
	 * 
	 */
	private static final long serialVersionUID = -1905625922120769289L;

	protected void activate() {
		requestRole("comm", "group","role");
	}
}