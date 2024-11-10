package madkit.kernel;

import java.io.File;
import java.net.URL;

import org.apache.commons.configuration2.ConfigurationConverter;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;

/**
 * @author Fabien Michel
 *
 */
public class TestConfiguration {

	public static void main(String[] args) throws ConfigurationException {
		Configurations configs = new Configurations();
		File propertiesFile = new File("config.properties");
		URL url = TestConfiguration.class.getResource("madkit.properties");
		PropertiesConfiguration config = configs.properties(url);
		System.err.println(config.getString("madkit.jar.name"));
		System.err.println(ConfigurationConverter.getProperties(config));
	}
}
