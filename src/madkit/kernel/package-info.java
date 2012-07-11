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

/**
 * Provides classes that are fundamental to the design of MaDKit applications.
 * This package is the heart of MaDKit. Especially, it contains the agent main classes:
 * {@link madkit.kernel.AbstractAgent} and {@link madkit.kernel.Agent}.
 * {@link madkit.kernel.AbstractAgent} is not threaded while {@link madkit.kernel.Agent}
 * is. The former is useful to develop simulation applications and the later
 * could be used where you need autonomy of execution.
 * 
* @since MaDKit 1.0
* @author Fabien Michel, Olivier Gutknecht, Jacques Ferber
* @version 5.0
* 
*/ 
package madkit.kernel;


