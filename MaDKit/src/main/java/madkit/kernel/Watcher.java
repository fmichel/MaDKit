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
package madkit.kernel;

import static madkit.simulation.SimuOrganization.ENGINE_GROUP;
import static madkit.simulation.SimuOrganization.WATCHER_ROLE;

import java.util.LinkedHashSet;
import java.util.Set;

import madkit.simulation.SimuAgent;
import madkit.simulation.SimuOrganization;

/**
 * A Watcher is an agent that is designed to be part of a simulation engine to monitor and
 * explore agents' internal properties.
 * <p>
 * 
 * To this end, it holds a collection of {@link Probe} which are used to monitor agents
 * that play specific roles in specific groups.
 * <p>
 * 
 * The probes are added to the artificial organization of the simulation engine using
 * {@link #addProbe(Probe)} and removed using {@link #removeProbe(Probe)}.
 * <p>
 * 
 * Moreover, by default, the Watcher agent is automatically granted the role
 * {@link SimuOrganization#WATCHER_ROLE} in the group
 * {@link SimuOrganization#ENGINE_GROUP} when it is activated. This can be changed by
 * overriding the {@link #onActivation()} method.
 * 
 * @since MaDKit 2.0
 * @version 6.0.1
 */
public abstract class Watcher extends SimuAgent {

	private final Set<Probe> probes = new LinkedHashSet<>();

	/**
	 * This method is called when the agent is activated. By default, it requests the role
	 * {@link SimuOrganization#WATCHER_ROLE} in the group
	 * {@link SimuOrganization#ENGINE_GROUP}.
	 */
	@Override
	protected void onActivation() {
		requestRole(getCommunity(), ENGINE_GROUP, WATCHER_ROLE);
	}

	/**
	 * Adds the probe to the artificial organization so that it starts to probe the agents
	 * which belong to the group and play the role defined in the probe.
	 * 
	 * @param probe the probe to add
	 */
	public void addProbe(Probe probe) {
		if (getOrganization().addOverlooker(this, probe)) {
			probes.add(probe);
		}
		getLogger().finer(() -> "Probe added: " + probe);
	}

	/**
	 * Removes the probe from the artificial organization, thus stopping the probing activity.
	 * 
	 * @param probe the probe to remove
	 */
	public void removeProbe(Probe probe) {
		getOrganization().removeOverlooker(probe);
		probes.remove(probe);
		getLogger().finer(() -> "Probe removed: " + probe);
	}

	/**
	 * Removes all probes when the agent is ending
	 */
	@Override
	protected void onEnd() {
		removeAllProbes();
		super.onEnd();
	}

	/**
	 * Remove all probes at once.
	 */
	public void removeAllProbes() {
		probes.forEach(p -> getOrganization().removeOverlooker(p));
		probes.clear();
		getLogger().fine(() -> "All probes removed");
	}

	/**
	 * Returns the probes which are currently added to the artificial organization.
	 * 
	 * @return the probes which are currently added to the artificial organization
	 */
	public Set<Probe> getProbes() {
		return probes;
	}

	/**
	 * Returns the watcher's name followed by the list of probes it holds if any.
	 */
	@Override
	public String toString() {
		return getName() + (probes.isEmpty() ? "" : probes.toString());
	}

}