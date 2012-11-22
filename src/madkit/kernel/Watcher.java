/*
 * Copyright 1997-2012 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MaDKit.
 * 
 * MaDKit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MaDKit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MaDKit. If not, see <http://www.gnu.org/licenses/>.
 */

package madkit.kernel;

import java.util.LinkedHashSet;
import java.util.Set;

/** 
 * This class defines a generic watcher agent. It holds a collection of probes to explore agents' internal properties.
 * 
 * @author Fabien Michel
 * @author Olivier Gutknecht 
 * @since MaDKit 2.0
 * @version 5.0
*/
public class Watcher extends AbstractAgent
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -4999802785768614838L;
	final private Set<Probe<? extends AbstractAgent>> probes = new LinkedHashSet<Probe<? extends AbstractAgent>> ();

	/**
	 * Adds the probe to the artificial organization so that it starts 
	 * to probe the agents which are at the corresponding CGR location.
	 * 
	 * @param probe the probe to add
	 */
	public void addProbe(final Probe<? extends AbstractAgent> probe){
		if(kernel.addOverlooker(this, probe))
			probes.add(probe);
		if(logger != null)
			logger.fine("Probe added: "+probe);
	}
	
	/**
	 * Removes the probe from the artificial organization, thus stopping the probing activity.
	 * 
	 * @param probe the probe to remove
	 */
	public void removeProbe(final Probe<? extends AbstractAgent> probe){
		kernel.removeOverlooker(this, probe);
		probes.remove(probe);
	}
	
	@Override
	protected void activate() {
		if(logger != null)
			logger.talk("\n\tHi human !\n\n I am an instance of the madkit.kernel.Watcher class.\n I am specialized in simulation probing.\n I use probes on the artificial society to analyze and\n visualize what is going on in a simulation.\n You can extend me to create your own\n simulation analyzing and visualizing tools !\n");
	}
	
	/**
	 * @see madkit.kernel.AbstractAgent#terminate()
	 */
	@Override
	final void terminate() {
		removeAllProbes();
		super.terminate();
	}

	/**
	 * Remove all probes at once.
	 */
	public void removeAllProbes() {
		for(final Probe<? extends AbstractAgent> p : probes ){
			kernel.removeOverlooker(this,p);
		}
		probes.clear();
	}
	
	public Probe<AbstractAgent>[] allProbes(){
		return probes.toArray(new Probe[probes.size()]);
//		return  (Probe<AbstractAgent>[]) probes.toArray();
	}
	
	@Override
	public String toString() {
		return getName() + " "+allProbes();
	}

}