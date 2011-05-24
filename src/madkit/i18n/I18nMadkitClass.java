package madkit.i18n;

import java.io.IOException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

public class I18nMadkitClass {
	static private ResourceBundle messages;
	static String baseName;
	
	final static ResourceBundle getResourceBundle(){
		if(messages == null)
			messages = ResourceBundle.getBundle("madkit/i18n/"+baseName);
		return messages;
	}

	public final static ResourceBundle getResourceBundle(String baseName){
			return ResourceBundle.getBundle("madkit/i18n/"+baseName);
	}
}

//			try {
//				messages = new PropertyResourceBundle(ClassLoader.getSystemResourceAsStream("madkit/i18n/"+baseName+".properties"));
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}