package madkit.simulation;

import static madkit.simulation.DefaultOrganization.ENVIRONMENT_ROLE;

import java.util.function.Consumer;

import madkit.kernel.Watcher;

/**
 * @author Fabien Michel
 *
 */
public class Environment extends Watcher {

	private static Consumer<Environment> initialize;
	private static Consumer<Environment> onStart;

	@Override
	protected void onActivation() {
		getSimuEngine().setEnvironment(this);
		createGroup(getCommunity(), getModelGroup());
		requestRole(getCommunity(), getModelGroup(), ENVIRONMENT_ROLE);
		requestRole(getCommunity(), getEngineGroup(), ENVIRONMENT_ROLE);
	}

//	public static void setInitializeProcedure(Consumer<Environment> r) {
//		initialize = r;
//	}
//
//	/**
//	 * @return the initialize
//	 */
//	public static Consumer<Environment> getInitialize() {
//		return initialize;
//	}
//	
//	/**
//	 * 
//	 */
//	public void onInit() {
//		initialize = onStart;
//		if (initialize != null) {
//			initialize.accept(this);
//		}
//	}
//
//	/**
//	 * @return the onStart
//	 */
//	public static Consumer<Environment> getOnStart() {
//		return onStart;
//	}
//	
//	public void onStart() {
//		if(onStart != null) {
//			onStart.accept(this);
//		}
//	}
//
//	/**
//	 * @param onStart the onStart to set
//	 */
//	public static void setOnStart(Consumer<Environment> onStart) {
//		Environment.onStart = onStart;
//	}

}
