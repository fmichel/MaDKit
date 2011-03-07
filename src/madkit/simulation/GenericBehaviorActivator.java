/*
 * Copyright 1997-2011 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MadKit.
 * 
 * MadKit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MadKit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with MadKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.simulation;

import java.util.List;
import madkit.kernel.AbstractAgent;
import madkit.kernel.Activator;

/**
 * An activator that invoke a simple method with no parameters
 * 
 * @author Fabien Michel
 * @since MadKit 5.0.0.1
 * @version 0.9
 * 
 */
public class GenericBehaviorActivator<A extends AbstractAgent> extends Activator<A>
{     
	final BehaviorInvoker<A> agentBehaviors;

	public GenericBehaviorActivator(final String communityName, final String groupName, final String roleName,final String theBehaviorToActivate)
	{
		super(communityName, groupName, roleName);
		agentBehaviors = new BehaviorInvoker<A>(theBehaviorToActivate);
	}

	public String getBehaviorName()	{
		return agentBehaviors.getBehaviorName();
	}
	
	public void setBehaviorName(final String theBehaviorToActivate){
		agentBehaviors.setMethodName(theBehaviorToActivate);
	}

	public void execute() {
//		List<A> l = getCurrentAgentsList();
		for (A a : getCurrentAgentsList()) {
//			for (final A a : l) {
//			if(a == null){
//				System.err.println(l);
//			}
			agentBehaviors.executeBehaviorOf(a);
		}
	}

}









