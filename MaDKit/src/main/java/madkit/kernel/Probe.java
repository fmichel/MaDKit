package madkit.kernel;

import java.lang.reflect.Field;
import java.util.Map;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;
import madkit.simulation.PropertyProbe;

/**
 * 
 * This class defines a watcher's generic probe. A probe is configured according to a
 * community, a group and a role. It is used to monitor agents that play specific roles in
 * specific groups. Once added to a {@link Watcher} agent, it can use the
 * #findFieldOn(Class, String) method to access the fields of the underlying agents.
 * 
 * @since MaDKit 2.0
 * @version 6.0
 * @see Watcher
 * @see PropertyProbe
 * 
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
		if (field == null)
			throw new NoSuchFieldException(fieldName);
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
				f.setAccessible(true);
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
