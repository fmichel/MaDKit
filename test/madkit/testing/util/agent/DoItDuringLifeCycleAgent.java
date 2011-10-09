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
package madkit.testing.util.agent;

import madkit.kernel.Agent;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.6
 * @version 0.9
 * 
 */
public abstract class DoItDuringLifeCycleAgent extends Agent{


	/**
	 * 
	 */
	private static final long serialVersionUID = 6981758600598841907L;
	private boolean inActivate=false,inLive=false,inEnd=false;
	
	public DoItDuringLifeCycleAgent(boolean inActivate, boolean inLive, boolean inEnd){
		this.inActivate = inActivate;
		this.inLive = inLive;
		this.inEnd = inEnd;
	}
	
	public DoItDuringLifeCycleAgent(boolean inActivate, boolean inLive){
		this(inActivate, inLive,false);
	}
	
	public DoItDuringLifeCycleAgent(boolean inActivate){
		this(inActivate, false, false);
	}
	
	public DoItDuringLifeCycleAgent(){
		this(false, true, false);
	}
	
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return super.getName()+(inActivate?"-inActivate-":"")+(inLive?"-inLive-":"")+(inEnd?"-inEnd-":"");
	}
	
	public void activate() {
		if (inActivate) {
			if(logger != null)
				logger.info("Doing It in activate!!");
 			doIt();
		}
	}
	
	public void live() {
		if (inLive) {
			if(logger != null)
				logger.info("Doing It in live!!");
			doIt();
		}
	}
	
	public void end() {
		super.end();
		if (inEnd) {
			if(logger != null)
				logger.info("Doing It in end!!");
			doIt();
		}
	}

	public void doIt(){
		if(logger != null)
			logger.info("I am in "+getState());
	}
}


