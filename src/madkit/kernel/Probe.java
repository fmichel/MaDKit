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
package madkit.kernel;

/**
 * This class defines a generic watcher probe. 
 * A probe is configured according to a community, a group and a role.
 * 
 * @author Fabien Michel since V.2
 * @author Olivier Gutknecht 
 * @since MadKit 2.0
 * @version 5.0
 * 
 */
public class Probe<A extends AbstractAgent> extends Overlooker<A>{

	/**
	 * @param communityName
	 * @param groupName
	 * @param roleName
	 */
	public Probe(final String communityName, final String groupName, final String roleName) {
		super(communityName, groupName, roleName);
	}
}
