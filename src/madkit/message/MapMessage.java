/*
 * Copyright or © or Copr. Fabien Michel, Olivier Gutknecht, Jacques Ferber (1997)

fmichel@lirmm.fr
olg@no-distance.net
ferber@lirmm.fr

This software is a computer program whose purpose is to 
provide a lightweight Java library for designing and simulating Multi-Agent Systems (MAS).

This software is governed by the CeCILL-C license under French law and
abiding by the rules of distribution of free software.  You can  use, 
modify and/ or redistribute the software under the terms of the CeCILL-C
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info". 

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability. 

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or 
data to be ensured and,  more generally, to use and operate it in the 
same conditions as regards security. 

The fact that you are presently reading this means that you have had
knowledge of the CeCILL-C license and that you accept its terms.
 */
package madkit.message;

import java.util.HashMap;
import java.util.Map;

/**
 * This class could be used to build message conveying {@link Map} 
 * objects between MaDKit agents.
 * 
 * @author Fabien Michel
 * @since MaDKit 5.1.2
 * @version 0.9
 *
 */
public class MapMessage<K,V> extends ObjectMessage<Map<K,V>> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6301488493002636831L;

	/**
	 * Builds a <code>MapMessage</code> containing the specified map
	 * @param map the original map
	 */
	public MapMessage(final Map<K,V> map) {
		super(map);
	}

	/**
	/**
	 * Builds a <code>MapMessage</code> containing an empty {@link HashMap}}
	 */
	public MapMessage() {
		this(new HashMap<>());
	}

	/**
	 * invoke {@link Map#put(K, V)} on the map contained in this message
	 * 
	 * @param key key with which the specified value is to be associated
	 * @param value value to be associated with the specified key
	 * @return the previous value associated with <tt>key</tt>, or
	 *         <tt>null</tt> if there was no mapping for <tt>key</tt>.
	 *         (A <tt>null</tt> return can also indicate that the map
	 *         previously associated <tt>null</tt> with <tt>key</tt>,
	 *         if the implementation supports <tt>null</tt> values.)
	 *         
	 * @see Map#put(K, V)  
	 */
	public V put(K key, V value){
		return getContent().put(key, value);
	}

	/**
	 * invoke {@link Map#get(K)} on the map contained in this message
	 * 
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with <tt>key</tt>, or
     *         <tt>null</tt> if there was no mapping for <tt>key</tt>.
     *         (A <tt>null</tt> return can also indicate that the map
     *         previously associated <tt>null</tt> with <tt>key</tt>,
     *         if the implementation supports <tt>null</tt> values.)
 	 */
	public V get(K key){
		return getContent().get(key);
	}

}
