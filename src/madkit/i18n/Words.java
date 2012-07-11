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
package madkit.i18n;

import java.util.ResourceBundle;

public enum Words {
	
	FAILED,COMMUNITY,GROUP,ROLE,
	LAUNCH,
	ENTERING,
	EXITING,
	TERMINATED,
	INITIAL_CONFIG,
	RELOAD, DIRECTORY, MAS, PAUSE, NEW_MSG, ABOUT, HELP, TUTORIALS;
	
	final static ResourceBundle messages = I18nUtilities.getResourceBundle(Words.class.getSimpleName());
//	static ResourceBundle messages = I18nUtilities.getResourceBundle(ReturnCode.class);
	
	public String toString() {
		return messages.getString(name());
	}
}
