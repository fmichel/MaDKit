package madkit.kernel;

import java.util.LinkedHashSet;
import java.util.Set;

import madkit.simulation.SimuAgent;

/**
 * This class defines a generic watcher agent. It holds a collection of probes
 * to explore agents' internal properties.
 * 
 * @author Fabien Michel
 * @author Olivier Gutknecht
 * @since MaDKit 2.0
 * @version 5.0
 */
public class Watcher extends SimuAgent {

	private final Set<Probe> probes = new LinkedHashSet<>();

	/**
	 * Adds the probe to the artificial organization so that it starts to probe the
	 * agents which are at the corresponding CGR location.
	 * 
	 * @param probe the probe to add
	 */
	public void addProbe(final Probe probe) {
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
	public void removeProbe(final Probe probe) {
		getOrganization().removeOverlooker(probe);
		probes.remove(probe);
	}

	@Override
	protected void onEnding() {
		removeAllProbes();
		super.onEnding();
	}

	/**
	 * Remove all probes at once.
	 */
	public void removeAllProbes() {
		probes.stream().forEach(p -> getOrganization().removeOverlooker(p));
		probes.clear();
	}

	/**
	 * Returns the probes which have been successfully added
	 * 
	 * @return all the added probes
	 */
	public Set<Probe> getProbes() {
		return probes;
	}

	@Override
	public String toString() {
		return getName() + " " + probes;
	}

}