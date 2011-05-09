package PingPongMultiAgent.src;



import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;

import madkit.kernel.AbstractAgent;
import madkit.kernel.Agent;
import madkit.kernel.Madkit;

/**
 * @author fab
 *
 */
@SuppressWarnings("serial")
public class PingPongDemoLauncherAgent extends Agent {

	// codes r�alis�s par PRADEILLES Vincent et HISLER Gaelle

	private List<AbstractAgent> agentsList = new ArrayList<AbstractAgent>();
	private Arbitre arbitre;
	/* (non-Javadoc)
	 * @see madkit.kernel.AbstractAgent#activate()
	 */
	@Override
	protected void activate() {
		int initialPause = 2000;
		int screenWidth = (int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth()-200);
		int screenHeight = (int) (Toolkit.getDefaultToolkit().getScreenSize().getHeight()-200);
	    arbitre = new Arbitre();
		if(launchAgent(arbitre, true) == ReturnCode.SUCCESS){
			if(logger != null)
				logger.info("Arbitre launched");
		}
		for (int i = 0; i < screenWidth;i=i+400) {
			for (int j = 0; j < screenHeight; j = j +300) {
//				if(j==900) return;
				PingPong p = new PingPong();
				if(launchAgent(p, true) == ReturnCode.SUCCESS){
					agentsList.add(p);
					if(logger != null)
						logger.info("Ping Pong launched");
					p.setFrameLocation(i,j);
					if(logger != null)
						logger.info("Ping Pong launched");
					pause((initialPause > 0 ? initialPause : 20));
					initialPause-=Math.random()*100;
				}
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see madkit.kernel.Agent#live()
	 */
	@Override
	protected void live() {
		pause(10000);
//		pause(1000);
	}
	
	/* (non-Javadoc)
	 * @see madkit.kernel.AbstractAgent#end()
	 */
	@Override
	protected void end() {
		int initialPause = 2000;
		while(! agentsList.isEmpty()){
			AbstractAgent ag = agentsList.remove((int) (agentsList.size()*Math.random())); 
			killAgent(ag);
			pause((initialPause > 0 ? initialPause : 100));
			initialPause-=Math.random()*100;
			if(logger != null)
				logger.info("living "+agentsList);
		}
		killAgent(this.arbitre);
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String[] argss = {"--agentLogLevel","INFO","--launchAgents",PingPongDemoLauncherAgent.class.getName()};
		Madkit.main(argss);
	}

}
