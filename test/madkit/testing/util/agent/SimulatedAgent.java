/*
 * Copyright 1997-2012 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MaDKit.
 * 
 * MaDKit is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * MaDKit is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MaDKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.testing.util.agent;

import static madkit.kernel.JunitMadkit.COMMUNITY;
import static madkit.kernel.JunitMadkit.GROUP;
import static madkit.kernel.JunitMadkit.ROLE;

import java.util.ArrayList;
import java.util.List;

import madkit.api.abstractAgent.LaunchAgentBucketWithRolesWithListTest;
import madkit.kernel.AbstractAgent;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.13
 * @version 0.9
 * 
 */
public class SimulatedAgent extends AbstractAgent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int privatePrimitiveField = 1;
	public double publicPrimitiveField = 2;
	private boolean activated = false;

	@Override
	protected void activate() {
//		setLogLevel(Level.ALL);
		createGroup(COMMUNITY, GROUP, false, null);
		requestRole(COMMUNITY, GROUP, ROLE, null);
		activated  = true;
	}
	
	
	private void launchAgentBucketWithRoles() {
		List<AbstractAgent> l = new ArrayList<AbstractAgent>();
		for (int i = 0; i < 10; i++) {
			l.add(new SimulatedAgent());
		}
		launchAgentBucket(l, COMMUNITY + ";" + GROUP + ";" + ROLE);
		LaunchAgentBucketWithRolesWithListTest.testAgents(l);
	}
	
	
	public void doIt() {
		if(logger != null)
			logger.info("doing it");
	}
	
	public boolean goneThroughActivate(){
		return activated;
	}

	/**
	 * @return the privatePrimitiveField
	 */
	public int getPrivatePrimitiveField() {
		return privatePrimitiveField;
	}

	/**
	 * @param privatePrimitiveField
	 *           the privatePrimitiveField to set
	 */
	public void setPrivatePrimitiveField(int privatePrimitiveField) {
		this.privatePrimitiveField = privatePrimitiveField;
	}
}
