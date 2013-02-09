import madkit.kernel.AbstractAgent;
import madkit.kernel.Madkit;
import madkit.kernel.Madkit.BooleanOption;
import madkit.kernel.Madkit.Option;



public class NoPackage extends AbstractAgent {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;
	
	@Override
	protected void activate() {
		if(logger != null)
			logger.info("test");
	}

	/**
	 * @param args
	 */
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		new Madkit(Option.launchAgents.toString(),AbstractAgent.class.getName()+",true",BooleanOption.desktop.toString());
	}

}
