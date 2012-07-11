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

import java.lang.reflect.Field;

import madkit.simulation.probe.PropertyProbe;

/**
 * This class defines a watcher's generic probe. 
 * A probe is configured according to a community, a group and a role.
 * 
 * @author Fabien Michel
 * @author Olivier Gutknecht 
 * @since MaDKit 2.0
 * @version 5.0
 * @see Watcher
 * @see PropertyProbe
 * 
 */
public class Probe<A extends AbstractAgent> extends Overlooker<A>{

	/**
	 * Builds a new Probe on the given CGR location of the
	 * artificial society. Once created, it has to be added by a {@link Watcher} 
	 * agent using the {@link Watcher#addProbe(Probe)} method.
	 * @param communityName
	 * @param groupName
	 * @param roleName
	 * @see Watcher
	 */
	public Probe(final String communityName, final String groupName, final String roleName) {
		super(communityName, groupName, roleName);
	}

	/**
	 * Returns the agent's field named <code>fieldName</code>.
	 * This also works on <code>private</code> fields, even
	 * inherited ones.
	 * 
	 * @param agentClass the targeted agent 
	 * @param fieldName the name of the field
	 * @return the agent's field named <code>fieldName</code>
	 * @throws NoSuchFieldException
	 */
	@SuppressWarnings("unchecked")
	public Field findFieldOn(Class<? extends A> agentClass, final String fieldName) throws NoSuchFieldException{
		Field f = null;
		while(true) {
			try {
				f = agentClass.getDeclaredField(fieldName);
				if(f != null){
					if (! f.isAccessible()) {//TODO seems to be always the case the first time
						f.setAccessible(true);
					}
					return f;
				}
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				agentClass = (Class<? extends A>) agentClass.getSuperclass();//TODO not go further than A
				if (agentClass == AbstractAgent.class) {//TODO bench vs local variable or Object ?
					throw e;
				}
			}
		} 
	}


}
