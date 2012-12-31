/*
 * Copyright 2012 Fabien Michel
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

import java.util.logging.Level;

import madkit.agr.LocalCommunity;


/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.20
 * @version 0.9
 * 
 */
abstract class MicroAgent<T> extends Agent {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= -655958680729623049L;
	private T result;
	
	MicroAgent() {
		setLogLevel(Level.OFF);
	}
	
	@Override
	protected void activate() {
		requestRole(LocalCommunity.NAME, "kernels", "ma");//TODO
	}
	
	@Override
	protected void live() {
	}

	/**
	 * @return the result
	 */
	public T getResult() {
		return result;
	}

	/**
	 * @param result the result to set
	 */
	public void setResult(T result) {
		this.result = result;
	}

}
