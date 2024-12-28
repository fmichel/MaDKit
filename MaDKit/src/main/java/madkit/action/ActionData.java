
package madkit.action;

import java.net.URL;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;

import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCombination;
import madkit.gui.FXAction;
import madkit.i18n.I18nUtilities;

/**
 * Represents the data associated with an action, including descriptions, icons,
 * and keyboard shortcuts. This class is used to manage and retrieve information
 * about actions in the application.
 * 
 * <p>
 * It provides methods to get the name, descriptions, icons, and keyboard
 * shortcuts for the action.
 * </p>
 * 
 * <p>
 * Example usage:
 * </p>
 * 
 * <pre>
 * {@code
 * ActionData actionData = new ActionData("JCONSOLE", KeyEvent.VK_J);
 * String name = actionData.getName();
 * ImageIcon icon = actionData.getBigIcon();
 * }
 * </pre>
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

	/** The large icon for the action. */
	private ImageIcon bigIcon;
	/** The small icon for the action. */
	private ImageIcon smallIcon;
	/** The name of the action. */
	private String name;

	/** The URL of the icon for the action. */
	private URL iconURL;

	/** The FXAction associated with this action data. */
	private FXAction fxAction;

	/** The image for the action. */
	private Image image;
	/** The small image for the action. */
	private Image smallImage;

	/**
	 * Constructs a new ActionData with the specified code name, key event, and
	 * resource bundle.
	 *
	 * @param codeName the code name of the action as a string. For instance,
	 *                 "JCONSOLE".
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
	 * Constructs a new ActionData with the specified code name and key event, using
	 * the default resource bundle.
	 *
	 * @param codeName the code name of the action as a string. For instance,
	 *                 "JCONSOLE".
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
	 * Returns the large icon for the action.
	 *
	 * @return the large icon for the action.
	 */
	public ImageIcon getBigIcon() {
		if (bigIcon == null) {
			bigIcon = new ImageIcon(iconURL);
		}
		return bigIcon;
	}

	/**
	 * Returns the small icon for the action.
	 *
	 * @return the small icon for the action.
	 */
	public ImageIcon getSmallIcon() {
		if (smallIcon == null) {
			if (getBigIcon().getIconWidth() > 16) {
				smallIcon = new ImageIcon(getBigIcon().getImage().getScaledInstance(16, 16, java.awt.Image.SCALE_SMOOTH));
			} else {
				smallIcon = getBigIcon();
			}
		}
		return smallIcon;
	}

	/**
	 * Returns the graphic node for the action.
	 *
	 * @return the graphic node for the action.
	 */
	public Node getGraphic() { // TODO should be fixed in controlsfx: should not bind to a single Node
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
	 * Returns the FXAction associated with this action data.
	 *
	 * @return the FXAction associated with this action data.
	 */
	public FXAction getFXAction() {
		if (fxAction == null) {
			fxAction = new FXAction(this, e -> doAction());
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
