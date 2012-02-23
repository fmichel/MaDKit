package madkit.i18n;

import java.util.ResourceBundle;

public enum ErrorMessages {
	
	FAILED,
	C_NULL,
	G_NULL,
	R_NULL,
	CANT_LAUNCH, 
	OPTION_MISUSED,
	CANT_FIND,
	CANT_LOAD,
	CANT_CONNECT,
	MUST_BE_LAUNCHED;
	
	final static ResourceBundle messages = I18nUtilities.getResourceBundle(ErrorMessages.class.getSimpleName());
//	static ResourceBundle messages = I18nUtilities.getResourceBundle(ReturnCode.class);
	
	public String toString() {
		return messages.getString(name())+" ";
	}
}
