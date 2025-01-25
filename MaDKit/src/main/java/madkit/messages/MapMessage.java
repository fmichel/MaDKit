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
package madkit.messages;

import java.util.HashMap;
import java.util.Map;

/**
 * This class could be used to build message conveying {@link Map} objects between MaDKit
 * agents.
 * 
 * @since MaDKit 5.1.2
 * @version 6.0
 * @param <K> Type of the keys in the map
 * @param <V> Type of the values in the map
 */
public class MapMessage<K, V> extends ObjectMessage<Map<K, V>> {

	private static final long serialVersionUID = -6301488493002636831L;

	/**
	 * Builds a <code>MapMessage</code> containing the specified map
	 * 
	 * @param map the original map
	 */
	public MapMessage(Map<K, V> map) {
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
	 *
	 * @param key   key with which the specified value is to be associated
	 * @param value value to be associated with the specified key
	 * @return the previous value associated with {@code key}, or {@code null} if there was no
	 *         mapping for {@code key}. (A {@code null} return can also indicate that the map
	 *         previously associated {@code null} with {@code key}, if the implementation
	 *         supports {@code null} values.)
	 * @throws UnsupportedOperationException if the {@code put} operation is not supported by
	 *                                       this map
	 * @throws ClassCastException            if the class of the specified key or value
	 *                                       prevents it from being stored in this map
	 * @throws NullPointerException          if the specified key or value is null and this
	 *                                       map does not permit null keys or values
	 * @throws IllegalArgumentException      if some property of the specified key or value
	 *                                       prevents it from being stored in this map
	 */
	public V put(K key, V value) {
		return getContent().put(key, value);
	}

	/**
	 * invoke {@link Map#get(Object)} on the map contained in this message
	 * 
	 * @param key the key whose associated value is to be returned
	 * @return the value to which the specified key is mapped, or {@code null} if this map
	 *         contains no mapping for the key
	 */
	public V get(K key) {
		return getContent().get(key);
	}

}
