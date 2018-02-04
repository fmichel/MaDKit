/*
 * Copyright 1997-2012 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MaDKit_Demos.
 * 
 * MaDKit_Demos is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MaDKit_Demos is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MaDKit_Demos. If not, see <http://www.gnu.org/licenses/>.
 */
package com.distrimind.madkitdemos.bees;

import java.awt.Dimension;

import javax.swing.DefaultBoundedRangeModel;

/**
 * Class representing the size of the environment and some modeling parameters.
 * 
 * @version 2.0.0.3
 * @author Fabien Michel
 */
public class BeeEnvironment {

	private Dimension envSize;

	private final DefaultBoundedRangeModel queenAcceleration = new DefaultBoundedRangeModel(5, 1, 0, 21);
	private final DefaultBoundedRangeModel beeAcceleration = new DefaultBoundedRangeModel(3, 1, 0, 21);
	private final DefaultBoundedRangeModel queenVelocity = new DefaultBoundedRangeModel(12, 1, 0, 21);
	private final DefaultBoundedRangeModel beeVelocity = new DefaultBoundedRangeModel(9, 1, 0, 21);

	/**
	 * @return the queenAcceleration
	 */
	public DefaultBoundedRangeModel getQueenAcceleration() {
		return queenAcceleration;
	}

	/**
	 * @return the beeAcceleration
	 */
	public DefaultBoundedRangeModel getBeeAcceleration() {
		return beeAcceleration;
	}

	/**
	 * @return the queenVelocity
	 */
	public DefaultBoundedRangeModel getQueenVelocity() {
		return queenVelocity;
	}

	/**
	 * @return the beeVelocity
	 */
	public DefaultBoundedRangeModel getBeeVelocity() {
		return beeVelocity;
	}

	public int getWidth() {
		return envSize.width;
	}

	public int getHeight() {
		return envSize.height;
	}

	public BeeEnvironment() {
		this(new Dimension());
	}

	/**
	 * @param envSize the environment size
	 */
	public BeeEnvironment(Dimension envSize) {
		this.envSize = envSize;
	}

	/**
	 * @param envSize
	 *            the envSize to set
	 */
	public final void setEnvSize(Dimension envSize) {
		this.envSize = envSize;
	}

}
