/*
 * Copyright 1997-2014 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MaDKit.
 * 
 * MaDKit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MaDKit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MaDKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.util;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import madkit.kernel.AbstractAgent;
import madkit.kernel.Madkit.BooleanOption;
import madkit.kernel.Madkit.LevelOption;
import madkit.kernel.Madkit.Option;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;


/**
 * XML shortcuts as static methods
 * 
 * @author Fabien Michel
 * @since MadKit 5.0.2
 * @version 0.9
 * 
 */
public class XMLUtilities {

	/**
	 * return the DOM from an xml file.
	 * 
	 * @param xmlFile
	 * @return the DOM from an xml file or <code>null</code> if not found or invalid
	 */
	public static Document getDOM(String xmlFile) throws SAXException,IOException,ParserConfigurationException{
		try (final InputStream is = MadkitProperties.getInputStream(xmlFile)) {//for closing the stream
			return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
		} catch (SAXException | IOException | ParserConfigurationException e) {
			throw e;
		}
	}
	
	public static String nodeToString(Node node){
		String s = node.getNodeName()+"( ";
		NamedNodeMap map = node.getAttributes();
		if (map != null) {
			for (int i = 0; i < map.getLength(); i++) {
				s += map.item(i).getNodeName() + "="
						+ map.item(i).getNodeValue() + " ";
			}
		}
		return s+=" )";
	}
	

	/**
	 * Valid attribute of an agent node : {@value} in a MDK xml config file
	 */
	public static final String	NB_OF_INSTANCES	= "nbOfInstances";

	/**
	 * Valid attribute of an agent node : {@value} in a MDK xml config file
	 */
	public static final String	GUI	= "GUI";

	/**
	 * The name of the child node of {@link #AGENT} containing 
	 * attributes that have to be set on the agent: {@value}
	 */
	public static final String	ATTRIBUTES	= "Attributes";

	/**
	 * Valid attribute of an agent node determining the agent
	 * class to launch, value is {@value}
	 */
	public static final String	CLASS	= "class";

	/**
	 * The name of a node containing a launch configuration: {@value}
	 */
	public static final String	AGENT	= "Agent";

	/**
	 * The name of a node containing properties for MaDKit : {@value}
	 * 
	 * @see Option
	 * @see LevelOption
	 * @see BooleanOption
	 */
	public static final String	MADKIT_PROPERTIES	= "MadkitProperties";

	/**
	 * Valid attribute of an agent node determining the agent
	 * log level to be set, the name of the attribute is {@value}
	 */
	public static final String	LOG_LEVEL	= "logLevel";

	/**
	 * Name of the root node of MaDKit xml fles, value is {@value}
	 */
	public static final String	MDK	= "MDK";

	/**
	 * Valid attribute of an agent node determining if the launch
	 * should be done using the bucket mode. For instance :
 	 * <pre>
	 * {@code
	 *    	<Agent class="madkit.xml.XMLBenchTestAgent" nbOfInstances="100000" bucketMode="true">
	 * 		<Attributes speed="3" i="5" s="blabla"/>
	 * </Agent>
	 * }
	 * 
	 * 
	 * If the {@link #BUCKET_MODE_ROLE} tag is used, bucketMode is automatically 
	 * added and set to <code>true</code>.
	 * </pre>
	 * 
	 * 
	 * see {@link AbstractAgent#launchAgentBucket(java.util.List, int, String...)}
	 */
	public static final String BUCKET_MODE = "bucketMode";

	/**
	 * The name of the child node of {@link #AGENT} containing
	 * the roles which should be set when using the bucket mode.
	 * e.g.
	 * 
	 * <pre>
	 * {@code
	 *    	<Agent class="madkit.xml.XMLBenchTestAgent" nbOfInstances="100000" bucketMode="true">
	 *    
	 * 		<Attributes speed="3" i="5" s="blabla"/>
	 * 
	 * 		<bucketModeRole community="Tcommunity" group="Tgroup" role="Trole"/>
	 * </Agent>
	 * }
	 * 
	 * </pre>
	 */
	public static final String BUCKET_MODE_ROLE = "bucketModeRole";

}
