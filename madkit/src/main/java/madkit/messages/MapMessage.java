
package madkit.messages;

import java.util.HashMap;
import java.util.Map;

/**
 * This class could be used to build message conveying {@link Map} objects between MaDKit agents.
 * 
 * @author Fabien Michel
 * @since MaDKit 5.1.2
 * @version 0.9
 */
public class MapMessage<K, V> extends ObjectMessage<Map<K, V>> {

    private static final long serialVersionUID = -6301488493002636831L;

    /**
     * Builds a <code>MapMessage</code> containing the specified map
     * 
     * @param map
     *            the original map
     */
    public MapMessage(final Map<K, V> map) {
	super(map);
    }

    /**
     * /** Builds a <code>MapMessage</code> containing an empty {@link HashMap}}
     */
    public MapMessage() {
	this(new HashMap<>());
    }

    /**
     * invoke {@link Map#put(Object, Object)} on the map contained in this message.
     * 
     * @param key
     *            key with which the specified value is to be associated
     * @param value
     *            value to be associated with the specified key
     * @return the previous value associated with <tt>key</tt>, or <tt>null</tt> if there was no mapping for <tt>key</tt>.
     *         (A <tt>null</tt> return can also indicate that the map previously associated <tt>null</tt> with <tt>key</tt>,
     *         if the implementation supports <tt>null</tt> values.)
     * @see Map
     */
    public V put(K key, V value) {
	return getContent().put(key, value);
    }

    /**
     * invoke {@link Map#get(Object)} on the map contained in this message
     * 
     */
    public V get(K key) {
	return getContent().get(key);
    }

}
