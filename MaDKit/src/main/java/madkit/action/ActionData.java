/*******************************************************************************
 * MaDKit - Multi-agent systems Development Kit 
 * 
 * Copyright (c) 1998-2025 Fabien Michel, Olivier Gutknecht, Jacques Ferber...
 * 
 * This software is a computer program whose purpose is to
 * provide a lightweight Java API for developing and simulating 
 * Multi-Agent Systems (MAS) using an organizational perspective.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.You can use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty and the software's author, the holder of the
 * economic rights, and the successive licensors have only limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading, using, modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean that it is complicated to manipulate, and that also
 * therefore means that it is reserved for developers and experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and, more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 *******************************************************************************/

package madkit.action;

import java.net.URL;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCombination;
import madkit.gui.ActionFromEnum;
import madkit.i18n.I18nUtilities;

/**
 * Represents the data associated with an action, including descriptions, icons, and
 * keyboard shortcuts. This class is used to manage and retrieve information about actions
 * in the application.
 * 
 * It provides methods to get the name, descriptions, icons, and keyboard shortcuts for
 * the action.
 * 
 */
public class ActionData {

	/** The directory where the action images are stored. */
	private static final String IMAGE_DIR = "/madkit/controls/images/";

	/** The default resource bundle for internationalization. */
	private static ResourceBundle defaultBundle = I18nUtilities.getResourceBundle("Actions");

	/** The short description of the action. */
	private final String shortDescription;
	/** The long description of the action. */
	private final String longDescription;
	/** The keyboard shortcut for the action. */
	private final KeyCombination accelerator;

	/** The name of the action. */
	private String name;

	/** The URL of the icon for the action. */
	private URL iconURL;

	/** The ActionFromEnum associated with this action data. */
	private ActionFromEnum fxAction;

	/** The image for the action. */
	private Image image;
	/** The small image for the action. */
	private Image smallImage;

	/**
	 * Constructs a new ActionData with the specified code name, key event, and resource
	 * bundle.
	 *
	 * @param codeName the code name of the action as a string. For instance, "JCONSOLE".
	 * @param keyEvent the key event associated with the action.
	 * @param resource the resource bundle for internationalization.
	 */
	public ActionData(String codeName, int keyEvent, ResourceBundle resource) {
		name = codeName;
		accelerator = KeyCombination.keyCombination("ctrl+" + Character.valueOf((char) keyEvent));
		setIconURL(name);
		String[] codes = null;
		try {
			codes = resource.getString(codeName).split(";");
		} catch (MissingResourceException e) {
			e.printStackTrace(); // NOSONAR
		}
		if (codes != null) {
			shortDescription = codes[0];
			longDescription = codes.length > 1 ? codes[1] : shortDescription;
			name = codes[0];
		} else {
			shortDescription = longDescription = name;
		}
	}

	/**
	 * Constructs a new ActionData with the specified code name and key event, using the
	 * default resource bundle.
	 *
	 * @param codeName the code name of the action as a string. For instance, "JCONSOLE".
	 * @param keyEvent the key event associated with the action.
	 */
	public ActionData(String codeName, int keyEvent) {
		this(codeName, keyEvent, defaultBundle);
	}

	/**
	 * Returns the name of the action.
	 *
	 * @return the name of the action.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the short description of the action.
	 *
	 * @return the short description of the action.
	 */
	public String getShortDescription() {
		return shortDescription;
	}

	/**
	 * Returns the long description of the action.
	 *
	 * @return the long description of the action.
	 */
	public String getLongDescription() {
		return longDescription;
	}

	/**
	 * Sets the URL of the icon for the action.
	 *
	 * @param fileName the file name of the icon.
	 */
	void setIconURL(final String fileName) {
		iconURL = getClass().getResource(IMAGE_DIR + fileName + ".png");
	}

	/**
	 * Returns the graphic node for the action.
	 *
	 * @return the graphic node for the action.
	 */
	public Node getGraphic() {
		return new ImageView(getImage());
	}

	/**
	 * Returns the image for the action.
	 *
	 * @return the image for the action.
	 */
	public Image getImage() {
		if (image == null) {
			image = new Image(iconURL.toString(), 32, 32, false, true);
		}
		return image;
	}

	/**
	 * Returns the small image for the action.
	 *
	 * @return the small image for the action.
	 */
	public Image getSmallImage() {
		if (smallImage == null) {
			if (getImage().getWidth() > 16) {
				smallImage = new Image(iconURL.toString(), 16, 16, false, true);
			} else {
				smallImage = getImage();
			}
		}
		return smallImage;
	}

	/**
	 * Returns the graphic node for the action menu.
	 *
	 * @return the graphic node for the action menu.
	 */
	public ImageView getGraphicMenu() {
		return new ImageView(getSmallImage());
	}

	/**
	 * Returns the keyboard shortcut for the action.
	 *
	 * @return the keyboard shortcut for the action.
	 */
	public KeyCombination getAccelerator() {
		return accelerator;
	}

	/**
	 * Returns the ActionFromEnum associated with this action data.
	 *
	 * @return the ActionFromEnum associated with this action data.
	 */
	public ActionFromEnum getFXAction() {
		if (fxAction == null) {
			fxAction = new ActionFromEnum(this, _ -> doAction());
		}
		return fxAction;
	}

	/**
	 * Performs the action.
	 */
	public void doAction() {
		// Action implementation
	}
}
