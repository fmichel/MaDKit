package madkit.kernel;

import static madkit.simulation.SimuOrganization.ENGINE_GROUP;
import static madkit.simulation.SimuOrganization.WATCHER_ROLE;

import java.util.LinkedHashSet;
import java.util.Set;

import madkit.simulation.SimuAgent;
import madkit.simulation.SimuOrganization;

/**
 * A Watcher is an agent that is designed to be part of a simulation engine to
 * monitor and explore agents' internal properties.
 * <p>
 * 
 * To this end, it holds a collection of {@link Probe} which are used to monitor
 * agents that play specific roles in specific groups.
 * <p>
 * 
 * The probes are added to the artificial organization of the simulation engine
 * using {@link #addProbe(Probe)} and removed using {@link #removeProbe(Probe)}.
 * <p>
 * 
 * Moreover, by default, the Watcher agent is automatically granted the role
 * {@link SimuOrganization#WATCHER_ROLE} in the group {@link SimuOrganization#ENGINE_GROUP} when it is
 * activated. This can be changed by overriding the {@link #onActivation()}
 * method.
 * 
 * @since MaDKit 2.0
 * @version 6.0
 */
public abstract class Watcher extends SimuAgent {

	private final Set<Probe> probes = new LinkedHashSet<>();

	/**
	 * This method is called when the agent is activated. By default, it requests
	 * the role {@link SimuOrganization#WATCHER_ROLE} in the group {@link SimuOrganization#ENGINE_GROUP}.
	 */
	@Override
	protected void onActivation() {
		requestRole(getCommunity(), ENGINE_GROUP, WATCHER_ROLE);
	}

	/**
	 * Adds the probe to the artificial organization so that it starts to probe the
	 * agents which belong to the group and play the role defined in the probe.
	 * 
	 * @param probe the probe to add
	 */
	public void addProbe(Probe probe) {
		if (getOrganization().addOverlooker(this, probe))
			probes.add(probe);
		getLogger().fine(() -> "Probe added: " + probe);
	}

	/**
	 * Removes the probe from the artificial organization, thus stopping the probing
	 * activity.
	 * 
	 * @param probe the probe to remove
	 */
	public void removeProbe(Probe probe) {
		getOrganization().removeOverlooker(probe);
		probes.remove(probe);
		getLogger().fine(() -> "Probe removed: " + probe);
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