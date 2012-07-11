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
package madkit.agr;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.10
 * @version 0.9
 * 
 */
public class LocalCommunity implements Organization{


	public static final String NAME = "local";

	public static final class Groups {

		public static final String NETWORK = "network";
		/**
		 * The value of this constant is {@value}.
		 */
		public static final String SYSTEM = "system";
		public static final String GUI = "gui";
	}

	
	/**
	 * Default roles within a MaDKit organization.
	 * @since MaDKit 5.0.0.10
	 */
	public static final class Roles {

		/**
		 * The value of this constant is {@value}.
		 */
		public static final String KERNEL = "kernel";
		public static final String NET_AGENT = "net agent";
		public static final String UPDATER = "updater";
		public static final String EMMITER = "emmiter";
		
	}
	/**
	 * The value of this constant is {@value}.
	 */

	
	
}
