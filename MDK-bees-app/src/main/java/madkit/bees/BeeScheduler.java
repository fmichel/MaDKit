/*
 * Copyright 1997-2012 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 *
 * This file is part of MaDKit_Demos.
 *
 * MaDKit_Demos is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MaDKit_Demos is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MaDKit_Demos. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.bees;

import static madkit.simulation.DefaultOrganization.VIEWER_ROLE;

import madkit.kernel.AbstractScheduler;
import madkit.simulation.DateBasedTimer;
import madkit.simulation.Parameter;
import madkit.simulation.activator.MethodActivator;
import madkit.simulation.activator.MethodHandleActivator;

/**
 * @version 6
 * @author Fabien Michel
 */
public class BeeScheduler extends AbstractScheduler<DateBasedTimer> {

	private static MethodHandleActivator bees;

	@Parameter(category = "engine", displayName = "multicore")
	private static boolean multicore = false;


	/**
	 * @return the multicore
	 */
	public static boolean isMulticore() {
		return multicore;
	}
	
//	@Override
//	protected void initializeTime() {
//		setSimulationTime(new TickBasedTime());
//	}


	/**
	 * @param multicore the multicore to set
	 */
	public static void setMulticore(boolean multicore) {
		BeeScheduler.multicore = multicore;
		bees.setMulticoreOn(multicore);
	}

	@Override
	public void onActivation() {
//		getLogger().setLevel(Level.ALL);
		super.onActivation();
		bees = new MethodHandleActivator(getCommunity(), getModelGroup(), AbstractBee.BEE_ROLE, "buzz");
		addActivator(bees);
		MethodActivator viewer = new MethodActivator(getCommunity(), getEngineGroup(), VIEWER_ROLE, "observe");
		addActivator(viewer);
		// auto starting myself the agent way
	}
	
	@Override
	public void onSimulationStep() {
		logCurrrentStep();
		executeActivators();
		getSimuTimer().addOneTimeUnit();
//		long start = System.nanoTime();
//		System.err.println("STEP ------------------ " + (System.nanoTime() - start));
	}
//
//	/**
//	 * Overriding just for adding the multicore option
//	 *
//	 * @see madkit.kernel.Scheduler#checkMail(madkit.kernel.Message)
//	 */
//	@SuppressWarnings("unchecked")
//	@Override
//	protected void checkMail(Message m) {
//		if (m != null) {
//			try {
//				boolean mutiCore = ((ObjectMessage<Boolean>) m).getContent();
//				if (mutiCore) {
//					bees.useMulticore(Runtime.getRuntime().availableProcessors());
//				} else {
//					bees.useMulticore(1);
//				}
//			} catch (ClassCastException e) {
//				super.checkMail(m);// default behavior
//			}
//		}
//	}

}
