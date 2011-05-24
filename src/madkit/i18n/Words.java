package madkit.i18n;

import java.util.ResourceBundle;

import madkit.kernel.AbstractAgent.ReturnCode;

public enum Words {
	
	FAILED,COMMUNITY,GROUP,ROLE;
	
	final static ResourceBundle messages = I18nMadkitClass.getResourceBundle(Words.class.getSimpleName());
//	static ResourceBundle messages = I18nMadkitClass.getResourceBundle(ReturnCode.class);
	
	public String toString() {
		return messages.getString(name());
	}
}
