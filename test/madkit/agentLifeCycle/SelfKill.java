/**
 * 
 */
package madkit.agentLifeCycle;

import madkit.kernel.AbstractAgent;
import madkit.testing.util.agent.DoItDuringLifeCycleAgent;

/**
 * @author fab
 *
 */
public class SelfKill extends DoItDuringLifeCycleAgent{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4863709147048119864L;

	/**
	 * @param inActivate
	 * @param inLive
	 * @param inEnd
	 */
	public SelfKill(boolean inActivate, boolean inLive, boolean inEnd) {
		super(inActivate, inLive, inEnd);
	}

	public SelfKill(boolean inActivate, boolean inLive) {
		super(inActivate, inLive, false);
	}

	public SelfKill(boolean inActivate) {
		super(inActivate, false, false);
	}

	static boolean immediateKill=false;
	
	/* (non-Javadoc)
	 * @see test.madkit.agentLifeCycle.DoItDuringLifeCycleAgent#getName()
	 */
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return super.getName()+(immediateKill?"-NoTimeOut-":"-WithTimeOut");
	}

	@Override
	public void end() {
		// TODO Auto-generated method stub
		super.end();
		killAgent(this);
		for (int i = 0; i < 10000; i++) {
			requestRole("test", "t", "r", null);
			System.err.print("doing in activate "+i);
		}
	}

	/* (non-Javadoc)
	 * @see test.madkit.agentLifeCycle.DoItDuringLifeCycleAgent#doIt()
	 */
	@Override
	public void doIt() {
		killAgent(this);
		for (int i = 0; i < 10000; i++) {
			requestRole("test", "t", "r", null);
			System.err.print("doing in activate "+i);
		}		
	}
	
	/* (non-Javadoc)
	 * @see madkit.kernel.AbstractAgent#killAgent(madkit.kernel.AbstractAgent)
	 */
	@Override
	public ReturnCode killAgent(AbstractAgent target) {
		if(logger != null){
			logger.info("killing myself");
		}
		if (immediateKill) {
			return killAgent(target, 0);
		}
		else{
			return super.killAgent(target);
		}
	}
}


