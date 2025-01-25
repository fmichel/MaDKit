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

package madkit.i18n;

import java.util.ResourceBundle;

/**
 * Internationalization help class containing only static methods.
 * 
 *
 * @since MaDKit 5.0.0.10
 * @version 0.91
 * 
 */
public class I18nUtilities {

	/**
	 * The directory where the i18n files are stored. By default, it is
	 * "madkit/i18n/" of the archive.
	 */
	public static final String I18N_DIRECTORY = "madkit/i18n/";

	private I18nUtilities() {
		throw new IllegalStateException(" Utility class ");
	}

	/**
	 * Returns the resource bundle for the given base name.
	 * 
	 * @param baseName the base name of the resource bundle file
	 * @return the resource bundle for the given base name
	 */
	public static final ResourceBundle getResourceBundle(String baseName) {
		return ResourceBundle.getBundle(I18N_DIRECTORY + baseName);
	}

	/**
	 * Returns a string formatted according to the presentation of a CGR location.
	 * 
	 * @param community the community name
	 * @return the formatted string for the CGR location
	 */
	public static String getCGRString(String community) {
		return getCGRString(community, null, null);
	}

	/**
	 * Returns a string formatted according to the presentation of a CGR location.
	 * 
	 * @param community the community name
	 * @param group     the group name
	 * @return the formatted string for the CGR location
	 */
	public static String getCGRString(String community, String group) {
		return getCGRString(community, group, null);
	}

	/**
	 * Returns a string formatted according to the presentation of a CGR location.
	 * 
	 * @param community the community name
	 * @param group     the group name
	 * @param role      the role name
	 * @return the formatted string for the CGR location
	 */
	public static String getCGRString(String community, String group, String role) {
		if (role != null)
			return Words.ROLE + " <" + community + "," + group + "," + role + "> ";
		if (group != null)
			return Words.GROUP + " <" + community + "," + group + "> ";
		return Words.COMMUNITY + " <" + community + "> ";
	}

}