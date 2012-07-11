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
package madkit.i18n;

import java.util.ResourceBundle;

/**
 * Internationalization help class containing only static methods.
 * 
 * @author Fabien Michel
 * @since MaDKit 5.0.0.10
 * @version 0.9
 * 
 */
public class I18nUtilities {
	
	public final static ResourceBundle getResourceBundle(String baseName){
			return ResourceBundle.getBundle("madkit/i18n/"+baseName);
	}
	
	public static String getCGRString(final String community){
		return getCGRString(community,null,null);
	}
	
	public static String getCGRString(final String community, final String group){
		return getCGRString(community,group,null);
	}

	public static String getCGRString(final String community, final String group, final String role){
		if(role != null)
			return Words.ROLE+" <"+community+","+group+","+role+"> ";
		if(group != null)
			return Words.GROUP+" <"+community+","+group+"> ";
		return Words.COMMUNITY+" <"+community+"> ";
	}	
}