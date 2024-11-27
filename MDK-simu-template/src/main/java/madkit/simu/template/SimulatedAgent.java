package madkit.simu.template;

import java.time.LocalDate;

import madkit.simulation.Parameter;
import madkit.simulation.SimuAgent;
import madkit.simulation.environment.Environment2D;

@SuppressWarnings("unchecked")
public class SimulatedAgent extends SimuAgent{
	
	private double x;

	private double y;

    //GLOBAL
    @Parameter(category="simulation",displayName="initial date")
    static private LocalDate startingDate = LocalDate.now();
    
    @Parameter(displayName="fast mode")
    private static boolean fastMode = false;
    
	public static LocalDate getStartingDate() {
		return startingDate;
	}

	public static void setStartingDate(LocalDate date) {
		startingDate = date;
	}
	
	@Override
	protected void onActivation() {
		requestRole(getCommunity(), getModelGroup(), "simuAgent");
		x = prng().nextInt(getEnvironment().getWidth());
		y = prng().nextInt(getEnvironment().getHeight());
		getLogger().createLogFile();
	}
	
	@Override
	public Environment2D getEnvironment() {
		return super.getEnvironment();
	}

	private void doIt() {
		moveRandomly();
		getLogger().talk(x+"\n");
	}

	/**
	 * 
	 */
	private void moveRandomly() {
		x += prng().nextInt(3)*(prng().nextBoolean() ? 1 : -1);
		x %= getEnvironment().getWidth();
		if(x<0)
			x+=getEnvironment().getWidth();
		y += prng().nextDouble()*(prng().nextBoolean() ? 1 : -1);
		y %= getEnvironment().getHeight();
		if(y<0)
			y+=getEnvironment().getHeight();
		}
	
	public static boolean isFastMode() {
		return fastMode;
	}

	public static void setFastMode(boolean fastMode) {
		SimulatedAgent.fastMode = fastMode;
	}

}
