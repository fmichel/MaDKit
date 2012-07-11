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
package madkit.gui;

import java.util.Arrays;

/**
 * This represents a MaDKit session
 * 
 * @author Fabien Michel
 * @since MaDKit 5.0.0.9
 * @version 0.91
 * 
 */
final public class MASModel implements Comparable<MASModel> {
	
	final private String[] sessionCfg;
	final private String description;
	final private String name;

	public MASModel(String demoName, String[] args, String description) {
		this.description = description;
		this.sessionCfg = args;
		name = demoName;
	}

	public String[] getSessionArgs() {
		return sessionCfg;
	}

	public String getDescription() {
		return description;
	}

	public String getName() {
		return name;
	}
	
	@Override
	public boolean equals(Object obj) {
		return name.equals(((MASModel) obj).getName());
	}
	
	@Override
	public String toString() {
		return name+" : "+Arrays.deepToString(sessionCfg);
	}

	@Override
	public int compareTo(MASModel o) {
		return name.compareTo(o.getName());
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}
}
