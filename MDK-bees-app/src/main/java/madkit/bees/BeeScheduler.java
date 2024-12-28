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

import madkit.kernel.Scheduler;
import madkit.simulation.DateBasedTimer;
import madkit.simulation.Parameter;
import madkit.simulation.activator.MethodActivator;

/**
 * @version 6
 * @author Fabien Michel
 */
public class BeeScheduler extends Scheduler<DateBasedTimer> {

	@Override
	public void onActivation() {
		super.onActivation();
		MethodActivator bees = new MethodActivator(getModelGroup(), AbstractBee.BEE_ROLE, "buzz");
		addActivator(bees);
		addViewersActivator();
	}

	@Override
	public void doSimulationStep() {
		logCurrrentTime();
		getActivators().forEach(a -> a.execute());
		getSimuTimer().addOneTimeUnit();
	}

}
