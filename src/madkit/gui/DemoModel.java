/*
 * Copyright 1997-2011 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MadKit.
 * 
 * MadKit is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * MadKit is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with MadKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.gui;

import java.util.Arrays;

/**
 * This represents a MadKit session
 * 
 * @author Fabien Michel
 * @since MadKit 5.0.0.9
 * @version 0.9
 * 
 */
final public class DemoModel implements Comparable<DemoModel> {
	
	final private String[] sessionCfg;
	final private String description;
	final private String name;

	public DemoModel(String demoName, String[] args, String description) {
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
		return name.equals(((DemoModel) obj).getName());
	}
	
	@Override
	public String toString() {
		return "MK demo "+name+" : "+description+" ;"+"args = "+Arrays.deepToString(sessionCfg);
	}

	@Override
	public int compareTo(DemoModel o) {
		return name.compareTo(o.getName());
	}
}
