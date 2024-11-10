package madkit.kernel;

import java.util.Arrays;
import java.util.Iterator;
import java.util.logging.Level;

import org.apache.commons.configuration2.PropertiesConfiguration;

/**
 * @author Fabien Michel
 *
 */
public class KernelConfig extends PropertiesConfiguration {

	public Level getLevel(final String key) {
		return get(Level.class, key);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("");
		for (Iterator<String> iterator = getKeysInternal(); iterator.hasNext();) {
			String next = iterator.next();
			sb.append(next).append(" -> ").append(getProperty(next)).append('\n');
		}
		return sb.toString();
	}
	
	/**
	 * @param c the class to take fields as properties from
	 */
	public void addPropertiesFromFields(Object o) {
		Arrays.stream(o.getClass().getDeclaredFields()).forEach(f -> {
			try {
				f.setAccessible(true);
				setProperty(f.getName(), f.get(o));
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		});
	}


}
