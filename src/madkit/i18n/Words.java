package madkit.i18n;

import java.util.ResourceBundle;

public enum Words {
	
	FAILED,COMMUNITY,GROUP,ROLE,
	LAUNCH,
	RELOAD, DIRECTORY;
	
	final static ResourceBundle messages = I18nUtilities.getResourceBundle(Words.class.getSimpleName());
//	static ResourceBundle messages = I18nUtilities.getResourceBundle(ReturnCode.class);
	
	public String toString() {
		return messages.getString(name());
	}
}
