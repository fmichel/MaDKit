/*******************************************************************************
 * MaDKit - Multi-agent systems Development Kit 
 * 
 * Copyright (c) 1998-2025 Fabien Michel, Olivier Gutknecht, Jacques Ferber...
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
package madkit.simulation.viewer;

import java.util.List;

import madkit.agr.SystemRoles;
import madkit.kernel.Probe;
import madkit.kernel.Role;
import madkit.simulation.SimuOrganization;

/**
 * A Viewer that displays the population of roles in the artificial organization
 */
public class RolesPopulationLineChartDrawer extends LineChartDrawer<Probe> {

	@Override
	protected void onActivation() {
		super.onActivation();
		List<Role> roles = getOrganization().getGroup(getCommunity(), getModelGroup()).getRoles();
		roles.stream()
				.filter(role -> !(role.getName().equals(SystemRoles.GROUP_MANAGER)
						|| role.getName().equals(SimuOrganization.ENVIRONMENT_ROLE)))
				.forEach(role -> addRoleToMonitoring(getModelGroup(), role.getName()));
	}

	/**
	 * Gets the title of the line chart
	 */
	@Override
	protected String getLineChartTitle() {
		return "Roles population";
	}

	/**
	 * Gets the label of the y axis
	 */
	@Override
	protected String getyAxisLabel() {
		return "Population count";
	}

	/**
	 * Gets the label of the x axis
	 */
	@Override
	protected String getxAxisLabel() {
		return "Time";
	}

	/**
	 * The display method called by the simulation engine. It displays the population of roles
	 * at the current time in the simulation.
	 */
	@Override
	public void display() {
		getProbes().forEach(probe -> {
			addData(probe, getSimuTimer().toString(), probe.size());
		});
	}

	/**
	 * Adds a role to the monitoring
	 * 
	 * @param group the group to monitor
	 * @param role  the role to monitor
	 */
	protected void addRoleToMonitoring(String group, String role) {
		Probe probe = new Probe(getCommunity(), group, role);
		addProbe(probe);
		addSerie(probe, probe.getRole());
	}

	@Override
	public void render() {
	}
}
