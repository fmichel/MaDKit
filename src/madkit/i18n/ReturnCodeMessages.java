package madkit.i18n;

import java.util.ResourceBundle;

import madkit.kernel.AbstractAgent.ReturnCode;

public final class ReturnCodeMessages extends I18nMadkitClass{
	
	static {
		baseName = "returnCodesMessages";
	}
	
	public static String getString(ReturnCode code){
		return getResourceBundle().getString(code.name());
	}
	
}
