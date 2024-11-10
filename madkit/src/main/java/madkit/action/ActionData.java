package madkit.action;

import java.net.URL;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;

import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCombination;
import madkit.gui.fx.FXAction;
import madkit.i18n.I18nUtilities;

/**
 * @author Fabien Michel
 *
 */
public class ActionData {

	private static final String IMAGE_DIR = "/madkit/controls/images/";

	private static ResourceBundle defaultBundle = I18nUtilities.getResourceBundle("Actions");

	private final String shortDescription;
	private final String longDescription;
	private final KeyCombination accelerator;

	private ImageIcon bigIcon;
	private ImageIcon smallIcon;
	private String name;

	private URL iconURL;

	private FXAction fxAction;

	private Image image;
	private Image smallImage;


	/**
	 * Builds a new ActionInfo considering a codeName as a string.
	 * 
	 * @param codeName the code name of the action as a string. For instance
	 *                 JCONSOLE.
	 * @param keyEvent key
	 * @param resource i18n
	 */
	public ActionData(String codeName, int keyEvent, ResourceBundle resource) {
		name = codeName;
		accelerator = KeyCombination.keyCombination("ctrl+" + Character.valueOf((char) keyEvent));
		setIconURL(name);
		String[] codes = null;
		try {
			codes = resource.getString(codeName).split(";");
		} catch (MissingResourceException e) {
			e.printStackTrace();// NOSONAR
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
	 * Builds a new ActionInfo considering a codeName as a string.
	 * 
	 * @param codeName the code name of the action as a string. For instance
	 *                 JCONSOLE.
	 * @param keyEvent key
	 */
	public ActionData(String codeName, int keyEvent) {
		this(codeName, keyEvent, defaultBundle);
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

	void setIconURL(final String fileName) {
		iconURL = getClass().getResource(IMAGE_DIR + fileName + ".png");
	}

	/**
	 * @return the bigIcon
	 */
	public ImageIcon getBigIcon() {
		if (bigIcon == null) {
			bigIcon = new ImageIcon(iconURL);
		}
		return bigIcon;
	}

	/**
	 * @return the smallIcon
	 */
	public ImageIcon getSmallIcon() {
		if (smallIcon == null) {
			if (getBigIcon().getIconWidth() > 16) {
				smallIcon = new ImageIcon(
						getBigIcon().getImage().getScaledInstance(16, 16, java.awt.Image.SCALE_SMOOTH));
			} else {
				smallIcon = getBigIcon();
			}
		}
		return smallIcon;
	}

	/**
	 * @return the graphic
	 */
	public Node getGraphic() { //TODO should be fixed in controlsfx : should not bind to a single Node 
			return new ImageView(getImage());
	}
	
	public Image getImage() {
		if (image == null) {
			image = new Image(iconURL.toString(), 32, 32, false, true);
		}
		return image;
	}

	public Image getSmallImage() {
		if (smallImage == null) {
			if(getImage().getWidth() > 16) {
				smallImage = new Image(iconURL.toString(), 16, 16, false, true);
			} else {
				smallImage = getImage();
			}
		}
		return smallImage;
	}

	/**
	 * @return the graphicMenu
	 */
	public ImageView getGraphicMenu() {
		return new ImageView(getSmallImage());
	}

	/**
	 * @return the accelerator
	 */
	public KeyCombination getAccelerator() {
		return accelerator;
	}

	public FXAction getFXAction() {
		if (fxAction == null) {
			fxAction = new FXAction(this, e -> doAction());
		}
		return fxAction;
	}

	public void doAction() {

	}
	
}
