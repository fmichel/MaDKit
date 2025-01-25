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

import java.lang.reflect.Field;
import java.util.Map;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;
import madkit.simulation.PropertyProbe;

/**
 * This class defines a watcher's generic probe. A probe is configured according to a
 * community, a group and a role. It is used to monitor agents that play specific roles in
 * specific groups. Once added to a {@link Watcher} agent, it can use the
 * #findFieldOn(Class, String) method to access the fields of the underlying agents.
 *
 * @version 6.0
 * @see Watcher
 * @see PropertyProbe
 * @since MaDKit 2.0
 */
public class Probe extends Overlooker {

	/**
	 * Class -> <fieldName -> Field>
	 */
	private static final Reference2ReferenceMap<Class<?>, Reference2ReferenceMap<String, Field>> fieldsTable = new Reference2ReferenceArrayMap<>();

	/**
	 * Builds a new Probe on the given CGR location of the artificial society. Once created,
	 * it has to be added by a {@link Watcher} agent using the {@link Watcher#addProbe(Probe)}
	 * method.
	 * 
	 * @param communityName the community name
	 * @param groupName     the group name
	 * @param roleName      the role name
	 */
	public Probe(String communityName, String groupName, String roleName) {
		super(communityName, groupName, roleName);
	}

	/**
	 * Builds a new Probe on the given CGR location, without specifying the community. This
	 * constructor is used to simplify declaration when used with the default implementation
	 * of a simulation engine provided in the madkit.simulation package. Once created, it has
	 * to be added by a {@link Watcher} agent using the {@link Watcher#addProbe(Probe)}
	 * method.
	 * 
	 * @param groupName the group name
	 * @param roleName  the role name
	 */
	public Probe(String groupName, String roleName) {
		this(null, groupName, roleName);
	}

	/**
	 * Returns the agent's field named <code>fieldName</code>. This also works on
	 * <code>private</code> fields, even inherited ones.
	 * 
	 * @param agentClass the targeted agent's class
	 * @param fieldName  the name of the field
	 * @return the agent's field named <code>fieldName</code>
	 * @throws NoSuchFieldException if the field was not found
	 */
	public static Field findFieldOn(Class<?> agentClass, String fieldName) throws NoSuchFieldException {
		Field field = getFieldTable(agentClass).computeIfAbsent(fieldName, _ -> getField(agentClass, fieldName));
		if (field == null) {
			throw new NoSuchFieldException(fieldName);
		}
		return field;
	}

	private static Map<String, Field> getFieldTable(Class<?> agentClass) {
		return fieldsTable.computeIfAbsent(agentClass, _ -> new Reference2ReferenceArrayMap<String, Field>());
	}

	/**
	 * Returns the agent's field named <code>fieldName</code>. This also works on
	 * <code>private</code> fields, even inherited ones.
	 * 
	 * No exception is thrown if the field does not exist. In that case, the method returns
	 * <code>null</code> because the exception cannot handle by the computeIffAbsent method.
	 * 
	 * @param agentClass the targeted agent's class
	 * @param fieldName  the name of the field
	 * @return the agent's field named <code>fieldName
	 */
	private static Field getField(Class<?> agentClass, String fieldName) {
		while (agentClass != Object.class) {
			try {
				Field f = agentClass.getDeclaredField(fieldName);
				f.setAccessible(true);// NOSONAR
				return f;
			} catch (NoSuchFieldException e) {
				agentClass = agentClass.getSuperclass();
			} catch (SecurityException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

}
