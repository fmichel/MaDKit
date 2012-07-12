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
package madkit.action;

import java.net.URL;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;

import madkit.kernel.AbstractAgent;

/**
 * This class encapsulates action information which could be used
 * to easily create {@link MDKAbstractAction}.
 * 
 * @author Fabien Michel
 * @since MaDKit 5.0.0.14
 * @version 1
 * 
 */
public class ActionInfo {

	/**
	 * Shortcut for System.getProperty("javawebstart.version") != null;
	 */
	final public static boolean javawsIsOn = System.getProperty("javawebstart.version") != null;

	final static private String imageDir = "images/";
	
	final private int keyEvent;

	final private ImageIcon bigIcon;

	final private ImageIcon smallIcon;
	
	private String name;

	final private String shortDescription;

	final private String longDescription;
	
	/**
	 * Builds a new ActionInfo considering an {@link Enum}.
	 * If the considered enum is from this package, it will be
	 * built automatically with values contained in the madkit.i18n directory
	 * 
	 * @param enumAction
	 * @param keyEvent
	 */
	<E extends Enum<E>> ActionInfo(E enumAction, int keyEvent, ResourceBundle resource) {
		this.keyEvent = keyEvent;
		name = enumAction.name();
		final URL imageUrl = enumAction.getClass().getResource(imageDir + name + ".png");
		if (imageUrl != null) {
			bigIcon = new ImageIcon(imageUrl);
			if (bigIcon.getIconWidth() > 16) {
				smallIcon = new ImageIcon(bigIcon.getImage().getScaledInstance(16, 16, java.awt.Image.SCALE_SMOOTH));
			} else {
				smallIcon = bigIcon;
			}
		}
		else{
			bigIcon = smallIcon = null;
		}
		String[] codes = null;
		try {
			codes = resource.getString(name).split(";");
		} catch (MissingResourceException e) {
		}
		if (codes != null) {
			shortDescription = codes.length > 1 ? codes[1] : codes[0];
			longDescription = codes.length > 2 ? codes[2] : shortDescription;
			name = codes[0];
		}
		else{
			shortDescription = longDescription = enumAction.getClass().getSimpleName();
		}
	}

	/**
	 * @return the keyEvent
	 */
	public int getKeyEvent() {
		return keyEvent;
	}

	/**
	 * @return the bigIcon
	 */
	public ImageIcon getBigIcon() {
		return bigIcon;
	}

	/**
	 * @return the smallIcon
	 */
	public ImageIcon getSmallIcon() {
		return smallIcon;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the shortDescription
	 */
	public String getShortDescription() {
		return shortDescription;
	}

	/**
	 * @return the longDescription
	 */
	public String getLongDescription() {
		return longDescription;
	}

	/**
	 * Converts the name of an enum object to a java
	 * standardized method name. For instance, using this on
	 * {@link AgentAction#LAUNCH_AGENT}
	 * will return <code>launchAgent</code>. This is especially used by 
	 * {@link AbstractAgent#proceedEnumMessage(madkit.message.EnumMessage)}
	 * to reflexively call the method of an agent which corresponds 
	 * to the code of such messages.
	 * 
	 * @param e the enum object to convert
	 * @return a string having a java
	 * standardized method name form.
	 */
	public static <E extends Enum<E>> String enumToMethodName(E e){
		final String[] tab = e.name().split("_");
		String methodName = tab[0].toLowerCase();
		for (int i = 1; i < tab.length; i++) {
			String s = tab[i];
			methodName += s.charAt(0) + s.substring(1).toLowerCase();
		}
		return methodName;
	}
	

}
