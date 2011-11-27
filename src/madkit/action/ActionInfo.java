package madkit.action;

import java.net.URL;
import java.util.MissingResourceException;

import javax.swing.ImageIcon;

import madkit.i18n.I18nUtilities;

public class ActionInfo {

	final static private String imageDir = "/madkit/gui/images/";
	
	final private int keyEvent;

	final private ImageIcon bigIcon;

	final private ImageIcon smallIcon;
	
	private String name;

	final private String shortDescription;

	final private String longDescription;
	
	public <E extends Enum<E>> ActionInfo(E enumAction, int keyEvent) {
		this.keyEvent = keyEvent;
		final String enumClassName = enumAction.getClass().getSimpleName();
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
			codes = I18nUtilities.getResourceBundle(enumClassName).getString(name).split(";");
		} catch (MissingResourceException e) {
		}
		if (codes != null) {
			shortDescription = codes.length > 1 ? codes[1] : codes[0];
			longDescription = codes.length > 2 ? codes[2] : shortDescription;
			name = codes[0];
		}
		else{
			shortDescription = longDescription = enumClassName;
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
	

}
