/*
 * Copyright or © or Copr. Fabien Michel, Olivier Gutknecht, Jacques Ferber (1997)

fmichel@lirmm.fr
olg@no-distance.net
ferber@lirmm.fr

This software is a computer program whose purpose is to 
provide a lightweight Java library for designing and simulating Multi-Agent Systems (MAS).

This software is governed by the CeCILL-C license under French law and
abiding by the rules of distribution of free software.  You can  use, 
modify and/ or redistribute the software under the terms of the CeCILL-C
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info". 

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability. 

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or 
data to be ensured and,  more generally, to use and operate it in the 
same conditions as regards security. 

The fact that you are presently reading this means that you have had
knowledge of the CeCILL-C license and that you accept its terms.
 */
package madkit.simulation.probe;

import java.lang.reflect.Field;

import madkit.kernel.AbstractAgent;
import madkit.kernel.Probe;
import madkit.simulation.SimulationException;

/**
 * This probe inspects fields of type T on only one agent of type A and its subclasses.
 * This is designed for probing one single agent, i.e. methods are designed  
 * and optimized in this respect.
 * 
 * @param <A> the most common class type expected in this group (e.g. AbstractAgent)
 * @param <T> the type of the property, i.e. Integer (this works if the field is an int, i.e. a primitive type)
 * @author Fabien Michel
 * @since MaDKit 5.0.0.18
 * @version 1.0
 * 
 */
public class SingleAgentProbe<A extends AbstractAgent,T> extends Probe<A>//TODO make a thread safe version
{ 
	final private String fieldName;
	private Field field;
	private A probedAgent;

	/**
	 * Builds a new SingleAgentProbe considering a CGR location and the name of the class's field.
	 * 
	 * @param community
	 * @param group
	 * @param role
	 * @param fieldName the name of a field which is encapsulated in the type <A>
	 */
	public SingleAgentProbe(String community, String group, String role, String fieldName)
	{
		super(community, group, role);
		this.fieldName = fieldName;
	}
	
	@Override
	protected void adding(A agent) {
		if(field == null){//TODO replace or not
			try {
				field = findFieldOn(agent.getClass(), fieldName);
				probedAgent = agent;
			} catch (NoSuchFieldException e) {
				throw new SimulationException(toString()+" on "+agent,e);
			}
		}
	}
	
	@Override
	protected void removing(A agent) {
		super.removing(agent);
		field = null;
	}
	
	/**
	 * Get the current probed agent.
	 * @return the agent which is currently probed
	 */
	public A getProbedAgent(){
		return probedAgent;
	}

	/**
	 * Returns the current value of the agent's field 
	 * 
	 * @return the value of the agent's field 
	 */
	@SuppressWarnings("unchecked")
	public T getPropertyValue() {
		try {
			return (T) field.get(probedAgent);
		} catch (IllegalAccessException e) {
			throw new SimulationException(toString()+" on "+probedAgent,e);
		}
	}
	
	@Override
	public String toString() {
		return super.toString() + (probedAgent == null ? "" : " : "+probedAgent);
	}
	
	/**
	 * Should be used to work with primitive types
	 * or fields which are initially <code>null</code>
	 * @param value
	 */
	public void setPropertyValue(final T value){
		try {
			field.set(probedAgent, value);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new SimulationException(toString()+" on "+probedAgent,e);
		}
	}

}