package madkit.i18n;

import java.io.IOException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

public abstract class I18nMadkitClass {
	
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
	
	public static String getCGRString(final String community){
		return getCGRString(community,null,null);
	}
	
	public static String getCGRString(final String community, final String group){
		return getCGRString(community,group,null);
	}

	public static String getCGRString(final String community, final String group, final String role){
		if(role != null)
			return Words.ROLE+" <"+community+","+group+","+role+"> ";
		if(group != null)
			return Words.GROUP+" <"+community+","+group+"> ";
		return Words.COMMUNITY+" <"+community+"> ";
	}
	
	
	
}

//			try {
//				messages = new PropertyResourceBundle(ClassLoader.getSystemResourceAsStream("madkit/i18n/"+baseName+".properties"));
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}