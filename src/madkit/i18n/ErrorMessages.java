package madkit.i18n;

import java.util.ResourceBundle;

import madkit.kernel.AbstractAgent.ReturnCode;

public enum ErrorMessages {
	
	FAILED,
	C_NULL,
	G_NULL,
	R_NULL,
	CANT_LAUNCH;
	
	final static ResourceBundle messages = I18nMadkitClass.getResourceBundle(ErrorMessages.class.getSimpleName());
//	static ResourceBundle messages = I18nMadkitClass.getResourceBundle(ReturnCode.class);
	
	public String toString() {
		return messages.getString(name());
	}
}
