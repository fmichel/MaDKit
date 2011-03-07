/**
 * 
 */
package madkit.testing.util.agent;

import madkit.kernel.Agent;

/**
 * @author fab
 *
 */
public class KillTargetAgent extends DoItDuringLifeCycleAgent {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5186267329069400681L;
	Agent toKill;
	/**
	 * @param a
	 */
	public KillTargetAgent(Agent a) {
		this(a,false,false,false);
	}

	public KillTargetAgent(Agent a,boolean inActivate, boolean inLive, boolean inEnd) {
		super(inActivate, inLive, inEnd);
		toKill = a;
	}

	public KillTargetAgent(Agent a,boolean inActivate, boolean inLive) {
		this(a,inActivate, inLive,false);
	}

	public KillTargetAgent(Agent a,boolean inActivate) {
		this(a,inActivate,false,false);
	}

	@Override
	public void live() {
		killAgent(toKill, 2);
	}
}
