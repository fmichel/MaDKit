/*
 * Copyright or © or Copr. Fabien Michel, Olivier Gutknecht, Jacques Ferber (1997)
 * 
 * fmichel@lirmm.fr
 * olg@no-distance.net
 * ferber@lirmm.fr
 * 
 * This software is a computer program whose purpose is to
 * provide a lightweight Java library for designing and simulating Multi-Agent Systems (MAS).
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 * 
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package com.distrimind.madkit.action;

import java.net.URL;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;

import com.distrimind.madkit.kernel.AbstractAgent;

/**
 * This class encapsulates action information which could be used to easily
 * create a new {@link MDKAbstractAction}.
 * 
 * @author Fabien Michel
 * @since MaDKit 5.0.0.14
 * @version 1.1
 * 
 */
public class ActionInfo {

	final static private String IMAGE_DIR = "/com/distrimind/madkit/action/images/";

	final private int keyEvent;

	private ImageIcon bigIcon;

	private ImageIcon smallIcon;

	private String name;

	final private String shortDescription;

	final private String longDescription;

	/**
	 * Builds a new ActionInfo considering an {@link Enum}. If the considered enum
	 * is from this package, it will be built automatically with values contained in
	 * the madkit.i18n directory
	 * 
	 * @param enumAction the enum action
	 * @param keyEvent the key event
	 * @param resource the resource bundle
	 * @param <E> the enum type
	 */
	public <E extends Enum<E>> ActionInfo(E enumAction, int keyEvent, ResourceBundle resource) {
		this(enumAction.name(), keyEvent, resource);
	}

	/**
	 * Builds a new ActionInfo considering a codeName as a string.
	 * 
	 * @param codeName
	 *            the code name of the action as a string. For instance JCONSOLE.
	 * @param keyEvent the key event
	 * @param resource the resource bundle
	 */
	public ActionInfo(String codeName, int keyEvent, ResourceBundle resource) {
		name = codeName;
		this.keyEvent = keyEvent;
		setIcon(name);
		String[] codes = null;
		try {
			codes = resource.getString(codeName).split(";");
		} catch (MissingResourceException e) {
			e.printStackTrace();
		}
		if (codes != null) {
			shortDescription = codes.length > 1 ? codes[1] : codes[0];
			longDescription = codes.length > 2 ? codes[2] : shortDescription;
			name = codes[0];
		} else {
			shortDescription = longDescription = name;
		}
	}

	/**
	 * 
	 */
	void setIcon(final String fileName) {
		final URL imageUrl = getClass().getResource(IMAGE_DIR + fileName + ".png");
		if (imageUrl != null) {
			bigIcon = new ImageIcon(imageUrl);
			if (bigIcon.getIconWidth() > 16) {
				smallIcon = new ImageIcon(bigIcon.getImage().getScaledInstance(16, 16, java.awt.Image.SCALE_SMOOTH));
			} else {
				smallIcon = bigIcon;
			}
		} else {
			bigIcon = smallIcon = null;
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
	 * Converts the name of an enum object to a Java standardized method name. For
	 * instance, using this on {@link AgentAction#LAUNCH_AGENT} will return
	 * <code>launchAgent</code>. This is especially used by
	 * {@link AbstractAgent#proceedEnumMessage(com.distrimind.madkit.message.EnumMessage)} to
	 * reflexively call the method of an agent which corresponds to the code of such
	 * messages.
	 * 
	 * @param e
	 *            the enum object to convert
	 * @return a string having a Java standardized method name form.
	 * @param <E> the enum type
	 */
	public static <E extends Enum<E>> String enumToMethodName(final E e) {
		final String[] tab = e.name().split("_");
		StringBuilder methodName = new StringBuilder(tab[0].toLowerCase());
		for (int i = 1; i < tab.length; i++) {
			final String s = tab[i];
			methodName.append(s.charAt(0)).append(s.substring(1).toLowerCase());
		}
		return methodName.toString();
	}

}
