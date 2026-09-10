/*******************************************************************************
 * MaDKit - Multi-agent systems Development Kit 
 * 
 * Copyright (c) 1998-2026 Fabien Michel, Olivier Gutknecht, Jacques Ferber...
 * 
 * This software is a computer program whose purpose is to
 * provide a lightweight Java API for developing and simulating 
 * Multi-Agent Systems (MAS) using an organizational perspective.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.You can use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty and the software's author, the holder of the
 * economic rights, and the successive licensors have only limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading, using, modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean that it is complicated to manipulate, and that also
 * therefore means that it is reserved for developers and experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and, more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 *******************************************************************************/
package madkit.sample.chartfx;

import madkit.simulation.viewer.chartfx.ProbeXYChartViewer;

/**
 * A chart-fx viewer that plots the population of organizational roles over simulation
 * time.
 *
 * <p>
 * This is the chart-fx counterpart of
 * {@link madkit.simulation.viewer.RolesPopulationLineChartDrawer}. It uses
 * {@link ProbeXYChartViewer} to automatically monitor the "worker" and "master" role
 * populations through probes, plotting their sizes as the simulation evolves.
 *
 * <p>
 * The non-blocking chart-fx data path ensures that data updates do not synchronize on the
 * JavaFX Application Thread. Chart-fx picks up the new data on its own rendering pulse.
 *
 * @see PopulationAgent
 * @see PopulationChartLauncher
 * @see ProbeXYChartViewer
 */
public class PopulationChartViewer extends ProbeXYChartViewer {

	/**
	 * Activates this viewer and registers probes for the "worker" and "master" roles.
	 *
	 * <p>
	 * After calling {@code super.onActivation()}, this method adds probe datasets for each
	 * role in the model group. The inherited {@link ProbeXYChartViewer#display()} method will
	 * automatically plot their populations at each simulation step.
	 */
	@Override
	protected void onActivation() {
		super.onActivation();
		addProbeDataSet(getModelGroup(), PopulationAgent.WORKER);
		addProbeDataSet(getModelGroup(), PopulationAgent.MASTER);
	}

	/**
	 * Returns the chart title.
	 *
	 * @return {@code "Role Populations (chart-fx)"}
	 */
	@Override
	protected String getChartTitle() {
		return "Role Populations (chart-fx)";
	}
}
