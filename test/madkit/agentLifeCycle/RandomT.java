/**
 * 
 */
package madkit.agentLifeCycle;

import java.util.ArrayList;

import madkit.kernel.AbstractAgent;

/**
 * @author fab
 *
 */
public class RandomT extends LifeCycleTestAgent{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6420300053860559183L;
	public static ArrayList<AbstractAgent> agents;
	public static boolean killingOn = false;

	/* (non-Javadoc)
	 * @see madkit.kernel.Agent#live()
	 */
	@Override
	public void live() {
		for (int i = 0; i < 100; i++) {
			if(logger != null){
				logger.info("living");
			}
			pause((int)(Math.random()*100));
			ReturnCode res = launchAgent(agents.get((int) (Math.random()*agents.size())),Math.random()>.5?0:1);
//			launchAgent(agents.get((int) (Math.random()*agents.size())),Math.random()>.5?0:1);
			if(logger != null)				logger.info("launching result is : "+res);
			killSomebody();
		}
	}

	private void killSomebody() {
		ReturnCode res;
		if (killingOn) {
			AbstractAgent a = agents.get((int) (Math.random() * agents.size()));
			res = killAgent(a, Math.random() > .5 ? 0 : 1);
			if(logger != null)				logger.info("kill on "+a.getName()+" result is : "+res);
		}
	}
	
	/* (non-Javadoc)
	 * @see test.madkit.agentLifeCycle.LifeCycleTestAgent#end()
	 */
	@Override
	public void end() {
		super.end();
		for (int i = 0; i < 100; i++) {
			if(logger != null){
				logger.info("dying");
			}
			pause((int)(Math.random()*100));
			ReturnCode res = launchAgent(agents.get((int) (Math.random()*agents.size())),Math.random()>.5?0:1);
//			launchAgent(agents.get((int) (Math.random()*agents.size())),Math.random()>.5?0:1);
			if(logger != null)				logger.info("launching result is : "+res);
			killSomebody();
		}
	}
}


