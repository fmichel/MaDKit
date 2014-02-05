/*
 * Copyright 2013 Fabien Michel
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
package madkit.xml;

import static madkit.kernel.JunitMadkit.COMMUNITY;
import static madkit.kernel.JunitMadkit.GROUP;
import static madkit.kernel.JunitMadkit.ROLE;
import madkit.kernel.AbstractAgent;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.2
 * @version 0.9
 * 
 */
@SuppressWarnings("unused")
public class XMLBenchTestAgent extends AbstractAgent {
	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;
	private int speed = 0;
	private Integer i = 10;
	private String s="t";
	
	@Override
	protected void activate() {
		createGroup(COMMUNITY, GROUP,false,null);
		requestRole(COMMUNITY,GROUP,ROLE,null);
	}
}
