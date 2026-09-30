/*******************************************************************************
 * MaDKit - Multi-agent systems Development Kit 
 * 
 * Copyright (c) 1998-2025 Fabien Michel, Olivier Gutknecht, Jacques Ferber...
 * 
 * This software is a computer program whose purpose is to
 * provide a lightweight Java API for developing and simulating 
 * Multi-Agent Systems (MAS) using an organizational perspective.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.You can use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty and the software's author, the holder of the
 * economic rights, and the successive licensors have only limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading, using, modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean that it is complicated to manipulate, and that also
 * therefore means that it is reserved for developers and experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and, more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 *******************************************************************************/

package madkit.kernel;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import org.apache.commons.configuration2.PropertiesConfiguration;

/**
 * The `KernelConfig` class extends `PropertiesConfiguration` to provide additional
 * configuration capabilities specific to the MaDKit kernel.
 * <p>
 * This class allows retrieving logging levels and adding properties from an object's
 * fields.
 * 
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

	public Level getAgentLogLevel() {
		return getLevel(MDKCommandLine.AGENT_LOG_LEVEL);
	}

	public Level getKernelLogLevel() {
		return getLevel(MDKCommandLine.KERNEL_LOG_LEVEL);
	}

	public Level getMadkitLogLevel() {
		return getLevel(MDKCommandLine.MADKIT_LOG_LEVEL);
	}

	public boolean isDebug() {
		return getBoolean(MDKCommandLine.DEBUG, false);
	}

	public boolean isDesktop() {
		return getBoolean(MDKCommandLine.DESKTOP, false);
	}

	public boolean isHeadless() {
		return getBoolean(MDKCommandLine.HEADLESS_MODE, false);
	}

	public boolean isNetworkEnabled() {
		return getBoolean(MDKCommandLine.NETWORK, false);
	}

	public boolean isNoLog() {
		return getBoolean(MDKCommandLine.NO_LOG, false);
	}

	public boolean isNoRandomizedFields() {
		return getBoolean(MDKCommandLine.NO_RANDOM, false);
	}

	public boolean isCreateLogFiles() {
		return getBoolean(MDKCommandLine.CREATE_LOG_FILES, false);
	}

	public boolean isAutoStart() {
		return getBoolean(MDKCommandLine.START, false);
	}

	public int getSeed() {
		return getInt(MDKCommandLine.SEED, Integer.MIN_VALUE);
	}

	public String getLogDirectory() {
		return getString(MDKCommandLine.LOG_DIRECTORY);
	}

	public List<String> getAgents() {
		List<String> values = getList(String.class, MDKCommandLine.AGENTS);
		return values == null ? List.of() : values;
	}

	public List<String> getViewers() {
		List<String> values = getList(String.class, MDKCommandLine.VIEWERS);
		return values == null ? List.of() : values;
	}

	public String getScheduler() {
		return getString(MDKCommandLine.SCHEDULER);
	}

	public String getEnvironment() {
		return getString(MDKCommandLine.ENVIRONMENT);
	}

	public String getModel() {
		return getString(MDKCommandLine.MODEL);
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
				f.setAccessible(true);// NOSONAR
				setProperty(f.getName(), f.get(o));
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		});
	}

}
