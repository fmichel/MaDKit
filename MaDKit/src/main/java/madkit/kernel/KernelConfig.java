
package madkit.kernel;

import java.util.Arrays;
import java.util.Iterator;
import java.util.logging.Level;

import org.apache.commons.configuration2.PropertiesConfiguration;

/**
 * The `KernelConfig` class extends `PropertiesConfiguration` to provide
 * additional configuration capabilities specific to the MaDKit kernel.
 * <p>
 * This class allows retrieving logging levels and adding properties from an
 * object's fields.
 * </p>
 * 
 * @author Fabien Michel
 */
public class KernelConfig extends PropertiesConfiguration {

	/**
	 * Retrieves the logging level associated with the specified key.
	 *
	 * @param key the key for which to retrieve the logging level
	 * @return the logging level associated with the specified key
	 */
	public Level getLevel(String key) {
		return get(Level.class, key);
	}

	/**
	 * Returns a string representation of the configuration.
	 *
	 * @return a string representation of the configuration
	 */
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
	 * Adds properties to the configuration from the fields of the specified object.
	 *
	 * @param o the object from which to take fields as properties
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
