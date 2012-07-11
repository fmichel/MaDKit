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


/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.12
 * @version 1.0
 * 
 */
final class TerminatedKernel extends FakeKernel{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6872641432509497194L;

	final String buildFailString(final AbstractAgent agent){
//		if(agent instanceof AbstractAgent && Thread.currentThread().getThreadGroup() == MadkitKernel.A_LIFE){
//			throw new KilledException((String)null);//TODO something else
//		}
		return agent != null ? agent.getLoggingName() : ""+AbstractAgent.State.TERMINATED;
	}
}
